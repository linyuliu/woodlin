#!/usr/bin/env node

import { existsSync, mkdirSync, writeFileSync } from 'fs'
import { dirname, join } from 'path'
import { fileURLToPath } from 'url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

const API_DOCS_URL = process.env.API_DOCS_URL || 'http://localhost:8080/api/v3/api-docs'
const OUTPUT_DIR = join(__dirname, '../src/api/generated')
const SUPPORTED_METHODS = new Set(['get', 'post', 'put', 'delete', 'patch'])

function logInfo(...args) {
  console.warn(...args)
}

function logError(...args) {
  console.error(...args)
}

function exitWithError(message, error) {
  logError(message, error instanceof Error ? error.message : error)
  process.exit(1)
}

function pathToFunctionName(path, method) {
  const cleaned = path.replace(/^\//, '').replace(/\{[^}]+\}/g, '')
  const parts = cleaned.split('/').filter(Boolean)
  const name = parts
    .map((part, index) => (index === 0 ? part : part.charAt(0).toUpperCase() + part.slice(1)))
    .join('')

  return `${method}${name.charAt(0).toUpperCase() + name.slice(1)}`
}

function mapSchemaType(schema) {
  if (!schema) {
    return 'any'
  }

  if (schema.$ref) {
    return schema.$ref.split('/').pop()
  }

  if (schema.type === 'array') {
    return `${mapSchemaType(schema.items)}[]`
  }

  const typeMap = {
    string: 'string',
    number: 'number',
    integer: 'number',
    boolean: 'boolean',
    object: 'Record<string, any>'
  }

  return typeMap[schema.type] || 'any'
}

function fetchSwaggerDocs() {
  logInfo(`正在从 ${API_DOCS_URL} 获取 API 文档...`)

  return fetch(API_DOCS_URL)
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`)
      }
      return response.json()
    })
    .then((docs) => {
      logInfo('API 文档获取成功')
      return docs
    })
    .catch((error) => {
      logInfo('')
      logInfo('请确保:')
      logInfo('  1. 后端服务已启动')
      logInfo(`  2. API 文档地址可访问: ${API_DOCS_URL}`)
      logInfo('  3. 如需使用其他地址，请设置环境变量: API_DOCS_URL')
      exitWithError('获取 API 文档失败:', error)
    })
}

function generateTypes(schemas) {
  if (!schemas) {
    return ''
  }

  let types = '// 自动生成的类型定义\n\n'

  for (const [name, schema] of Object.entries(schemas)) {
    if (schema.type !== 'object' || !schema.properties) {
      continue
    }

    types += `export interface ${name} {\n`
    for (const [propName, prop] of Object.entries(schema.properties)) {
      const optional = schema.required?.includes(propName) ? '' : '?'
      types += `  ${propName}${optional}: ${mapSchemaType(prop)}\n`
    }
    types += '}\n\n'
  }

  return types
}

function getOperationParams(operation) {
  const params = operation.parameters || []
  return {
    params,
    pathParams: params.filter((param) => param.in === 'path'),
    queryParams: params.filter((param) => param.in === 'query')
  }
}

function buildFunctionParams(operationParams, requestBody) {
  const { pathParams, queryParams } = operationParams
  const functionParams = pathParams.map((param) => `${param.name}: ${mapSchemaType(param.schema)}`)

  if (queryParams.length > 0) {
    functionParams.push('params?: Record<string, any>')
  }

  if (requestBody) {
    functionParams.push('data?: any')
  }

  return functionParams
}

function buildRequestUrl(path, pathParams) {
  return pathParams.reduce(
    (requestUrl, param) => requestUrl.replace(`{${param.name}}`, `\${${param.name}}`),
    path
  )
}

function buildParamDocs(params) {
  return params.map((param) => ` * @param ${param.name} ${param.description || ''}\n`).join('')
}

function buildRequestConfig(method, requestUrl, queryParams, requestBody) {
  let config = `  return request({\n    url: \`${requestUrl}\`,\n    method: '${method}'`

  if (queryParams.length > 0) {
    config += ',\n    params'
  }

  if (requestBody) {
    config += ',\n    data'
  }

  return `${config}\n  })\n`
}

function generateApiFunction(path, method, operation) {
  const { params, pathParams, queryParams } = getOperationParams(operation)
  const summary = operation.summary || operation.description || ''
  const functionName = pathToFunctionName(path, method)
  const functionParams = buildFunctionParams({ pathParams, queryParams }, operation.requestBody)
  const requestUrl = buildRequestUrl(path, pathParams)

  return [
    '/**',
    ` * ${summary}`,
    buildParamDocs(params).trimEnd(),
    ' */',
    `export function ${functionName}(${functionParams.join(', ')}) {`,
    buildRequestConfig(method, requestUrl, queryParams, operation.requestBody).trimEnd(),
    '}',
    ''
  ]
    .filter(Boolean)
    .join('\n')
}

function generateApiFunctions(paths) {
  const blocks = []

  for (const [path, methods] of Object.entries(paths || {})) {
    for (const [method, operation] of Object.entries(methods)) {
      if (!SUPPORTED_METHODS.has(method)) {
        continue
      }
      blocks.push(generateApiFunction(path, method, operation))
    }
  }

  return blocks.join('\n')
}

function generateApiFile(docs) {
  let content = `/**
 * 自动生成的 API 服务
 *
 * 警告: 此文件由工具自动生成，请勿手动修改
 * 生成时间: ${new Date().toISOString()}
 * API 版本: ${docs.info?.version || 'unknown'}
 */

import request from '@/utils/request'

`

  if (docs.components?.schemas) {
    content += generateTypes(docs.components.schemas)
  }

  if (docs.paths) {
    content += generateApiFunctions(docs.paths)
  }

  return content
}

function ensureOutputDir() {
  if (existsSync(OUTPUT_DIR)) {
    return
  }

  mkdirSync(OUTPUT_DIR, { recursive: true })
  logInfo(`创建输出目录: ${OUTPUT_DIR}`)
}

async function main() {
  logInfo('Swagger API 代码生成工具')
  logInfo('')

  try {
    const docs = await fetchSwaggerDocs()
    ensureOutputDir()

    logInfo('正在生成 API 代码...')
    const outputFile = join(OUTPUT_DIR, 'api.ts')
    writeFileSync(outputFile, generateApiFile(docs), 'utf-8')

    logInfo('API 代码生成成功')
    logInfo(`输出文件: ${outputFile}`)
    logInfo('使用方法: import { getFunctionName } from \'@/api/generated/api\'')
  } catch (error) {
    exitWithError('生成失败:', error)
  }
}

void main()
