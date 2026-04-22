import type {AssessmentSchemaAggregateDTO} from '@/api/assessment'

export function ensureSchemaDefaults(source: AssessmentSchemaAggregateDTO): AssessmentSchemaAggregateDTO {
  source.sections = source.sections ?? []
  source.dimensions = source.dimensions ?? []
  source.rules = source.rules ?? []
  if (!source.sections.find(section => section.sectionCode === 'DEMOGRAPHIC')) {
    source.sections.unshift({
      sectionCode: 'DEMOGRAPHIC',
      sectionTitle: '人口学前置',
      sectionDesc: '',
      displayMode: 'paged',
      randomStrategy: 'none',
      sortOrder: 1,
      isRequired: true,
      items: []
    })
  }
  source.sections.forEach(section => {
    section.items = section.items ?? []
    section.displayMode = section.displayMode ?? 'paged'
    section.randomStrategy = section.sectionCode === 'DEMOGRAPHIC' ? 'none' : section.randomStrategy ?? 'none'
    section.items.forEach(item => {
      item.options = item.options ?? []
      item.dimensionBindings = item.dimensionBindings ?? []
      if (section.sectionCode === 'DEMOGRAPHIC') {
        item.isDemographic = true
        item.isScored = false
      }
    })
  })
  source.assessmentType = source.assessmentType ?? 'survey'
  source.randomStrategy = source.randomStrategy ?? 'none'
  return source
}

export function createEmptySchema(): AssessmentSchemaAggregateDTO {
  return ensureSchemaDefaults({
    assessmentType: 'survey',
    randomStrategy: 'none',
    sections: [],
    dimensions: [],
    rules: []
  })
}
