package com.mumu.woodlin.dsl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * NormMatchEngine 单元测试
 *
 * @author mumu
 * @since 2025-10-28
 */
class NormMatchEngineTest {

    private fun seg(
        id: Long,
        code: String = "seg_$id",
        name: String = "Segment $id",
        gender: String? = null,
        ageMin: Int? = null,
        ageMax: Int? = null,
        edu: String? = null,
        region: String? = null,
        priority: Int = 0
    ) = NormMatchEngine.SegmentDescriptor(id, code, name, gender, ageMin, ageMax, edu, region, priority)

    // -----------------------------------------------------------------------
    // Empty / null guard tests
    // -----------------------------------------------------------------------

    @Test
    fun `empty candidates returns null`() {
        val ctx = NormMatchEngine.DemographicContext(gender = "M", age = 25)
        assertNull(NormMatchEngine.match(ctx, emptyList()))
    }

    // -----------------------------------------------------------------------
    // Stage 1: full match
    // -----------------------------------------------------------------------

    @Test
    fun `full match with gender age education region`() {
        val ctx = NormMatchEngine.DemographicContext(
            gender = "M", age = 25, educationLevel = "bachelor", regionCode = "110105"
        )
        val segments = listOf(
            seg(1, gender = "F", ageMin = 18, ageMax = 25, edu = "bachelor", region = "110"),  // wrong gender
            seg(2, gender = "M", ageMin = 18, ageMax = 30, edu = "bachelor", region = "110", priority = 0),  // full match
            seg(3, priority = 10)  // default segment
        )
        val result = NormMatchEngine.match(ctx, segments)
        assertNotNull(result)
        assertEquals(2L, result!!.segment.segmentId)
        assertEquals("full_match", result.matchPath)
        assertFalse(result.isFallback)
    }

    @Test
    fun `full match uses region prefix`() {
        val ctx = NormMatchEngine.DemographicContext(
            gender = "F", age = 22, regionCode = "110105"
        )
        val seg = seg(1, gender = "F", ageMin = 18, ageMax = 25, region = "110")
        val result = NormMatchEngine.match(ctx, listOf(seg))
        assertNotNull(result)
        assertEquals("full_match", result!!.matchPath)
    }

    @Test
    fun `full match uses provinceCode when regionCode missing`() {
        val ctx = NormMatchEngine.DemographicContext(
            gender = "F", age = 22, regionCode = null, provinceCode = "110000"
        )
        val seg = seg(1, gender = "F", ageMin = 18, ageMax = 25, region = "110")
        val result = NormMatchEngine.match(ctx, listOf(seg))
        assertNotNull(result)
        assertEquals("full_match", result!!.matchPath)
    }

    // -----------------------------------------------------------------------
    // Stage 2: fallback_no_region
    // -----------------------------------------------------------------------

    @Test
    fun `fallback no region when region does not match but gender age edu match`() {
        val ctx = NormMatchEngine.DemographicContext(
            gender = "M", age = 25, educationLevel = "bachelor", regionCode = "310000"
        )
        // Only a seg that requires Beijing region (110...) and one default
        val segments = listOf(
            seg(1, gender = "M", ageMin = 18, ageMax = 30, edu = "bachelor", region = "110"),
            seg(2)  // default
        )
        val result = NormMatchEngine.match(ctx, segments)
        assertNotNull(result)
        // seg1 matches gender+age+edu but not region -> fallback_no_region
        assertEquals(1L, result!!.segment.segmentId)
        assertEquals("fallback_no_region", result.matchPath)
        assertTrue(result.isFallback)
    }

    // -----------------------------------------------------------------------
    // Stage 3: fallback_gender_age_only
    // -----------------------------------------------------------------------

    @Test
    fun `fallback gender age only when education does not match`() {
        val ctx = NormMatchEngine.DemographicContext(gender = "M", age = 25, educationLevel = "high_school")
        val segments = listOf(
            seg(1, gender = "M", ageMin = 18, ageMax = 30, edu = "bachelor"),  // edu mismatch
            seg(2)  // default
        )
        val result = NormMatchEngine.match(ctx, segments)
        assertNotNull(result)
        // seg1 matches gender+age, not edu -> fallback_gender_age_only
        assertEquals(1L, result!!.segment.segmentId)
        assertEquals("fallback_gender_age_only", result.matchPath)
    }

    // -----------------------------------------------------------------------
    // Stage 4: fallback_gender_only
    // -----------------------------------------------------------------------

    @Test
    fun `fallback gender only when age out of range`() {
        val ctx = NormMatchEngine.DemographicContext(gender = "F", age = 60)
        val segments = listOf(
            seg(1, gender = "F", ageMin = 18, ageMax = 25),  // age mismatch
            seg(2)  // default
        )
        val result = NormMatchEngine.match(ctx, segments)
        assertNotNull(result)
        // seg1 matches gender only (age 60 out of 18-25)
        assertEquals(1L, result!!.segment.segmentId)
        assertEquals("fallback_gender_only", result.matchPath)
    }

    // -----------------------------------------------------------------------
    // Stage 5: fallback_default_segment
    // -----------------------------------------------------------------------

    @Test
    fun `fallback default segment when gender does not match any filter`() {
        val ctx = NormMatchEngine.DemographicContext(gender = "M", age = 25)
        val segments = listOf(
            seg(1, gender = "F"),  // wrong gender
            seg(2)                  // default (no filters)
        )
        val result = NormMatchEngine.match(ctx, segments)
        assertNotNull(result)
        assertEquals(2L, result!!.segment.segmentId)
        assertEquals("fallback_default_segment", result.matchPath)
    }

    // -----------------------------------------------------------------------
    // Stage 6: fallback_first_available
    // -----------------------------------------------------------------------

    @Test
    fun `fallback first available when no default segment exists`() {
        val ctx = NormMatchEngine.DemographicContext(gender = "M")
        val segments = listOf(
            seg(1, gender = "F", priority = 5),
            seg(2, gender = "F", priority = 1)
        )
        val result = NormMatchEngine.match(ctx, segments)
        assertNotNull(result)
        // sorted by priority: seg(2) first (priority=1)
        assertEquals(2L, result!!.segment.segmentId)
        assertEquals("fallback_first_available", result.matchPath)
        assertTrue(result.isFallback)
    }

    // -----------------------------------------------------------------------
    // Priority ordering tests
    // -----------------------------------------------------------------------

    @Test
    fun `higher priority segment wins over lower priority when both match`() {
        val ctx = NormMatchEngine.DemographicContext(gender = "M", age = 25)
        val segments = listOf(
            seg(1, gender = "M", ageMin = 18, ageMax = 30, priority = 10),
            seg(2, gender = "M", ageMin = 20, ageMax = 30, priority = 5)
        )
        val result = NormMatchEngine.match(ctx, segments)
        assertNotNull(result)
        // seg2 has lower priority number -> higher priority -> selected first
        assertEquals(2L, result!!.segment.segmentId)
    }

    // -----------------------------------------------------------------------
    // Null demographic context tests
    // -----------------------------------------------------------------------

    @Test
    fun `null demographic context falls back to default segment`() {
        val ctx = NormMatchEngine.DemographicContext()
        val segments = listOf(
            seg(1, gender = "M"),
            seg(2)  // default
        )
        val result = NormMatchEngine.match(ctx, segments)
        assertNotNull(result)
        // gender "M" filter with null gender in ctx -> stage 4 fails -> stage 5 default
        assertEquals(2L, result!!.segment.segmentId)
        assertEquals("fallback_default_segment", result.matchPath)
    }

    @Test
    fun `single segment always returned`() {
        val ctx = NormMatchEngine.DemographicContext()
        val seg = seg(99, gender = "X")
        val result = NormMatchEngine.match(ctx, listOf(seg))
        assertNotNull(result)
        assertEquals(99L, result!!.segment.segmentId)
        assertEquals("fallback_first_available", result.matchPath)
    }

    // -----------------------------------------------------------------------
    // Region matching edge cases
    // -----------------------------------------------------------------------

    @Test
    fun `region filter must be prefix of regionCode`() {
        val ctx = NormMatchEngine.DemographicContext(regionCode = "110105")
        val exactFilter = seg(1, region = "110105")
        val prefixFilter = seg(2, region = "110")
        val noMatch = seg(3, region = "120")
        val default = seg(4)

        // Test exact filter
        assertEquals(1L, NormMatchEngine.match(ctx, listOf(exactFilter))!!.segment.segmentId)

        // Test prefix filter
        assertEquals(2L, NormMatchEngine.match(ctx, listOf(prefixFilter))!!.segment.segmentId)

        // Test no match falls to default
        assertEquals(4L, NormMatchEngine.match(ctx, listOf(noMatch, default))!!.segment.segmentId)
    }

    @Test
    fun `age boundary inclusive check`() {
        val atMin = NormMatchEngine.DemographicContext(age = 18)
        val atMax = NormMatchEngine.DemographicContext(age = 60)
        val below = NormMatchEngine.DemographicContext(age = 17)
        val above = NormMatchEngine.DemographicContext(age = 61)
        val seg = seg(1, ageMin = 18, ageMax = 60)
        val default = seg(2)

        assertFalse(NormMatchEngine.match(atMin, listOf(seg))!!.isFallback)
        assertFalse(NormMatchEngine.match(atMax, listOf(seg))!!.isFallback)
        // below min: seg has age filter, ctx.age=17 < ageMin=18 -> no full match,
        // then gender=null so stage 4 passes (matchesGenderOnly), stage 3 fails (age check)
        // Actually stage 4 matches gender (null filter = no restriction), stage 3 needs age match too
        // So: below age range -> fallback path (segment still matches in gender-only fallback since age filter applies)
        // seg has ageMin=18, ageMax=60 -> matchesAge fails for age=17 -> fails stages 1-3
        // Stage 4: matchesGenderOnly -> gender filter is null -> returns true -> fallback_gender_only
        val belowResult = NormMatchEngine.match(below, listOf(seg, default))
        assertNotNull(belowResult)
        assertTrue(belowResult!!.isFallback)
    }
}
