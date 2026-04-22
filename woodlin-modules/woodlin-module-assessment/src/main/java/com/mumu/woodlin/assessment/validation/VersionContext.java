package com.mumu.woodlin.assessment.validation;

import com.mumu.woodlin.assessment.model.entity.AssessmentDimension;
import com.mumu.woodlin.assessment.model.entity.AssessmentDimensionItem;
import com.mumu.woodlin.assessment.model.entity.AssessmentFormVersion;
import com.mumu.woodlin.assessment.model.entity.AssessmentItem;
import com.mumu.woodlin.assessment.model.entity.AssessmentOption;
import com.mumu.woodlin.assessment.model.entity.AssessmentPublish;
import com.mumu.woodlin.assessment.model.entity.AssessmentRule;
import com.mumu.woodlin.assessment.model.entity.AssessmentSchema;
import com.mumu.woodlin.assessment.model.entity.AssessmentSection;

import java.util.ArrayList;
import java.util.List;

/**
 * Snapshot of all domain data required for publish-time validation of one assessment version.
 *
 * <p>Populated by {@link com.mumu.woodlin.assessment.service.IAssessmentPublishValidationService}
 * and consumed by {@link AssessmentPublishValidator}.</p>
 *
 * @author mumu
 * @since 2025-01-01
 */
public final class VersionContext {

    private final Long versionId;
    private final AssessmentFormVersion version;
    private final AssessmentSchema schema;
    private final List<AssessmentItem> items;
    private final List<AssessmentOption> options;
    private final List<AssessmentDimension> dimensions;
    private final List<AssessmentDimensionItem> dimensionItems;
    private final List<AssessmentSection> sections;
    private final List<AssessmentRule> rules;
    /** May be {@code null} when validating a version rather than a concrete publish instance. */
    private final AssessmentPublish publish;

    private VersionContext(Builder builder) {
        this.versionId = builder.versionId;
        this.version = builder.version;
        this.schema = builder.schema;
        this.items = List.copyOf(builder.items);
        this.options = List.copyOf(builder.options);
        this.dimensions = List.copyOf(builder.dimensions);
        this.dimensionItems = List.copyOf(builder.dimensionItems);
        this.sections = List.copyOf(builder.sections);
        this.rules = List.copyOf(builder.rules);
        this.publish = builder.publish;
    }

    /** @return the version ID being validated */
    public Long getVersionId() { return versionId; }

    /** @return the version entity */
    public AssessmentFormVersion getVersion() { return version; }

    /** @return the schema entity (may be {@code null} if not yet compiled) */
    public AssessmentSchema getSchema() { return schema; }

    /** @return all items belonging to this version */
    public List<AssessmentItem> getItems() { return items; }

    /** @return all options for items in this version */
    public List<AssessmentOption> getOptions() { return options; }

    /** @return all dimensions for this version */
    public List<AssessmentDimension> getDimensions() { return dimensions; }

    /** @return all dimension-item mappings for this version */
    public List<AssessmentDimensionItem> getDimensionItems() { return dimensionItems; }

    /** @return all sections for this version */
    public List<AssessmentSection> getSections() { return sections; }

    /** @return all rules for this version */
    public List<AssessmentRule> getRules() { return rules; }

    /** @return the publish instance, or {@code null} for version-only validation */
    public AssessmentPublish getPublish() { return publish; }

    /** Builder for {@link VersionContext}. */
    public static final class Builder {
        private Long versionId;
        private AssessmentFormVersion version;
        private AssessmentSchema schema;
        private List<AssessmentItem> items = new ArrayList<>();
        private List<AssessmentOption> options = new ArrayList<>();
        private List<AssessmentDimension> dimensions = new ArrayList<>();
        private List<AssessmentDimensionItem> dimensionItems = new ArrayList<>();
        private List<AssessmentSection> sections = new ArrayList<>();
        private List<AssessmentRule> rules = new ArrayList<>();
        private AssessmentPublish publish;

        /** @param versionId version to validate */
        public Builder versionId(Long versionId) { this.versionId = versionId; return this; }

        /** @param version version entity */
        public Builder version(AssessmentFormVersion version) { this.version = version; return this; }

        /** @param schema schema entity */
        public Builder schema(AssessmentSchema schema) { this.schema = schema; return this; }

        /** @param items items for this version */
        public Builder items(List<AssessmentItem> items) { this.items = items; return this; }

        /** @param options options for items in this version */
        public Builder options(List<AssessmentOption> options) { this.options = options; return this; }

        /** @param dimensions dimensions for this version */
        public Builder dimensions(List<AssessmentDimension> dimensions) { this.dimensions = dimensions; return this; }

        /** @param dimensionItems dimension-item mappings */
        public Builder dimensionItems(List<AssessmentDimensionItem> dimensionItems) { this.dimensionItems = dimensionItems; return this; }

        /** @param sections sections for this version */
        public Builder sections(List<AssessmentSection> sections) { this.sections = sections; return this; }

        /** @param rules rules for this version */
        public Builder rules(List<AssessmentRule> rules) { this.rules = rules; return this; }

        /** @param publish optional publish instance */
        public Builder publish(AssessmentPublish publish) { this.publish = publish; return this; }

        /** @return built context */
        public VersionContext build() { return new VersionContext(this); }
    }
}
