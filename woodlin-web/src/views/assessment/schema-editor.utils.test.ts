import {describe, expect, it} from 'vitest'
import {createEmptySchema, ensureSchemaDefaults} from './schema-editor.utils'

describe('schema-editor utils', () => {
  it('injects demographic section and normalizes demographic scoring flags', () => {
    const schema = ensureSchemaDefaults({
      assessmentType: 'scale',
      randomStrategy: 'random_items',
      sections: [
        {
          sectionCode: 'SEC_001',
          sectionTitle: '正式题',
          items: []
        }
      ],
      dimensions: [],
      rules: []
    })

    expect(schema.sections[0].sectionCode).toBe('DEMOGRAPHIC')
    expect(schema.sections[0].randomStrategy).toBe('none')
  })

  it('creates an editable empty schema with canonical defaults', () => {
    const schema = createEmptySchema()

    expect(schema.assessmentType).toBe('survey')
    expect(schema.randomStrategy).toBe('none')
    expect(schema.sections[0].sectionCode).toBe('DEMOGRAPHIC')
  })
})
