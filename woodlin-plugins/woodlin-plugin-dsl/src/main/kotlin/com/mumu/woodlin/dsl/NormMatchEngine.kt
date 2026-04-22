package com.mumu.woodlin.dsl

/**
 * 常模匹配引擎 - 基于人口学/地区属性从候选分层中选择最合适的常模分层。
 *
 * 匹配逻辑（优先级从高到低，越精确越优先）：
 * 1. 精确匹配所有非空过滤条件（性别 + 年龄 + 学历 + 地区前缀）
 * 2. 忽略地区，匹配性别 + 年龄 + 学历
 * 3. 仅匹配性别 + 年龄
 * 4. 仅匹配性别
 * 5. 使用默认 segment（所有过滤条件均为 null 的分层）
 * 6. 最后兜底：返回 sortPriority 最小的分层
 *
 * <p>每个匹配步骤都记录在 [MatchResult.matchPath] 中，便于审计和问题定位。
 *
 * @author mumu
 * @since 2025-10-28
 */
object NormMatchEngine {

    /**
     * 人口学快照（由调用方从数据库 / DTO 填充）。
     */
    data class DemographicContext(
        val gender: String? = null,
        val age: Int? = null,
        val educationLevel: String? = null,
        val regionCode: String? = null,
        val provinceCode: String? = null
    )

    /**
     * 常模分层描述（由调用方从 sys_assessment_norm_segment 映射）。
     */
    data class SegmentDescriptor(
        val segmentId: Long,
        val segmentCode: String,
        val segmentName: String,
        val genderFilter: String? = null,
        val ageMin: Int? = null,
        val ageMax: Int? = null,
        val educationFilter: String? = null,
        val regionCodeFilter: String? = null,
        val sortPriority: Int = 0
    )

    /**
     * 匹配结果，包含命中的分层及匹配路径说明（用于审计）。
     *
     * @property segment 命中的分层
     * @property matchPath 匹配路径描述，如 "full_match" / "fallback_no_region" 等
     * @property isFallback 是否为降级命中（非精确匹配）
     */
    data class MatchResult(
        val segment: SegmentDescriptor,
        val matchPath: String,
        val isFallback: Boolean
    )

    /**
     * 为给定人口学上下文从候选分层列表中选择最优常模分层。
     *
     * @param ctx 人口学上下文
     * @param candidates 候选分层列表（来自同一 norm_set）
     * @return 匹配结果；若 candidates 为空则返回 null
     */
    @JvmStatic
    fun match(ctx: DemographicContext, candidates: List<SegmentDescriptor>): MatchResult? {
        if (candidates.isEmpty()) return null

        val sorted = candidates.sortedBy { it.sortPriority }

        // Stage 1: full match (match all declared restrictions, but keep default segment as explicit fallback)
        sorted.firstOrNull { !isDefaultSegment(it) && matchesAll(it, ctx) }
            ?.let { return MatchResult(it, "full_match", false) }

        // Stage 2: gender + age + education (skip region)
        sorted.firstOrNull { canFallbackNoRegion(it) && matchesGenderAgeEducation(it, ctx) }
            ?.let { return MatchResult(it, "fallback_no_region", true) }

        // Stage 3: gender + age only
        sorted.firstOrNull { canFallbackGenderAge(it) && matchesGenderAge(it, ctx) }
            ?.let { return MatchResult(it, "fallback_gender_age_only", true) }

        // Stage 4: gender only
        sorted.firstOrNull { canFallbackGenderOnly(it) && matchesGenderOnly(it, ctx) }
            ?.let { return MatchResult(it, "fallback_gender_only", true) }

        // Stage 5: default segment (all filters null = universal)
        sorted.firstOrNull { isDefaultSegment(it) }
            ?.let { return MatchResult(it, "fallback_default_segment", true) }

        // Stage 6: last resort - first available
        return MatchResult(sorted.first(), "fallback_first_available", true)
    }

    // ---------------------------------------------------------------------------
    // Composite predicates
    // ---------------------------------------------------------------------------

    private fun matchesAll(seg: SegmentDescriptor, ctx: DemographicContext) =
        matchesGender(seg, ctx) && matchesAge(seg, ctx) && matchesEducation(seg, ctx) && matchesRegion(seg, ctx)

    private fun matchesGenderAgeEducation(seg: SegmentDescriptor, ctx: DemographicContext) =
        matchesGender(seg, ctx) && matchesAge(seg, ctx) && matchesEducation(seg, ctx)

    private fun matchesGenderAge(seg: SegmentDescriptor, ctx: DemographicContext) =
        matchesGender(seg, ctx) && matchesAge(seg, ctx)

    private fun matchesGenderOnly(seg: SegmentDescriptor, ctx: DemographicContext) =
        matchesGender(seg, ctx)

    private fun isDefaultSegment(seg: SegmentDescriptor) =
        seg.genderFilter == null && seg.ageMin == null && seg.ageMax == null &&
                seg.educationFilter == null && seg.regionCodeFilter == null

    private fun hasGenderRestriction(seg: SegmentDescriptor) = seg.genderFilter != null

    private fun hasAgeRestriction(seg: SegmentDescriptor) = seg.ageMin != null || seg.ageMax != null

    private fun hasEducationRestriction(seg: SegmentDescriptor) = seg.educationFilter != null

    private fun hasRegionRestriction(seg: SegmentDescriptor) = seg.regionCodeFilter != null

    /**
     * Stage 2 means "this segment would match if we ignored region only".
     * Default segments and pure-region segments should not preempt later fallbacks.
     */
    private fun canFallbackNoRegion(seg: SegmentDescriptor) =
        hasRegionRestriction(seg) && (hasGenderRestriction(seg) || hasAgeRestriction(seg) || hasEducationRestriction(seg))

    /**
     * Stage 3 keeps gender/age and relaxes region/education. Require both:
     * 1. at least one retained restriction (gender or age)
     * 2. at least one relaxed restriction (education or region)
     */
    private fun canFallbackGenderAge(seg: SegmentDescriptor) =
        (hasGenderRestriction(seg) || hasAgeRestriction(seg)) &&
                (hasEducationRestriction(seg) || hasRegionRestriction(seg))

    /**
     * Stage 4 keeps gender and relaxes age/education/region.
     * Pure gender-only segments already qualify as full matches in stage 1.
     */
    private fun canFallbackGenderOnly(seg: SegmentDescriptor) =
        hasGenderRestriction(seg) && (hasAgeRestriction(seg) || hasEducationRestriction(seg) || hasRegionRestriction(seg))

    // ---------------------------------------------------------------------------
    // Atomic predicates
    // ---------------------------------------------------------------------------

    private fun matchesGender(seg: SegmentDescriptor, ctx: DemographicContext): Boolean {
        val filter = seg.genderFilter ?: return true    // null = no restriction
        val gender = ctx.gender ?: return false         // no demographic data = no match
        return filter.equals(gender, ignoreCase = true)
    }

    private fun matchesAge(seg: SegmentDescriptor, ctx: DemographicContext): Boolean {
        if (seg.ageMin == null && seg.ageMax == null) return true
        val age = ctx.age ?: return false
        val minOk = seg.ageMin == null || age >= seg.ageMin
        val maxOk = seg.ageMax == null || age <= seg.ageMax
        return minOk && maxOk
    }

    private fun matchesEducation(seg: SegmentDescriptor, ctx: DemographicContext): Boolean {
        val filter = seg.educationFilter ?: return true
        val edu = ctx.educationLevel ?: return false
        return filter.equals(edu, ignoreCase = true)
    }

    /**
     * 支持前缀匹配：regionCodeFilter="110" 匹配 "110105"（北京朝阳区）。
     * 同时使用 provinceCode 作备选（用于省级粒度常模）。
     */
    private fun matchesRegion(seg: SegmentDescriptor, ctx: DemographicContext): Boolean {
        val filter = seg.regionCodeFilter ?: return true
        if (ctx.regionCode != null && ctx.regionCode.startsWith(filter)) return true
        if (ctx.provinceCode != null && ctx.provinceCode.startsWith(filter)) return true
        return false
    }
}
