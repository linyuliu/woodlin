#!/usr/bin/env node
/**
 * API代码生成脚本
 * 
 * @author mumu
 * @description 从后端Swagger/OpenAPI文档自动生成TypeScript API服务
 * @since 2025-01-01
 */

import { writeFileSync, mkdirSync, existsSync } from 'fs'
import { join, dirname } from 'path'
import { fileURLToPath } from 'url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

const API_DOCS_URL = process.env.API_DOCS_URL || 'http://localhost:8080/api/v3/api-docs'
const OUTPUT_DIR = join(__dirname, '../src/api/generated')

console.log('🚀 Swagger API 代码生成工具')
console.log('')

/**
 * 获取 Swagger 文档
 */
async function fetchSwaggerDocs() {
  try {
    console.log(`📡 正在从 ${API_DOCS_URL} 获取 API 文档...`)
    const response = await fetch(API_DOCS_URL)
    
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`)
    }
    
    const docs = await response.json()
    console.log('✅ API 文档获取成功')
    return docs
  } catch (error) {
    console.error('❌ 获取 API 文档失败:', error.message)
    console.log('')
    console.log('💡 请确保:')
    console.log('   1. 后端服务已启动')
    console.log(`   2. API 文档地址可访问: ${API_DOCS_URL}`)
    console.log('   3. 如需使用其他地址，请设置环境变量: API_DOCS_URL')
    process.exit(1)
  }
}

/**
 * 将路径转换为函数名
 */
function pathToFunctionName(path, method) {
  // 移除前缀斜杠和参数
  const cleaned = path.replace(/^\//, '').replace(/\{[^}]+\}/g, '')
  // 分割路径并转换为驼峰命名
  const parts = cleaned.split('/').filter(Boolean)
  const name = parts.map((part, index) => 
    index === 0 ? part : part.charAt(0).toUpperCase() + part.slice(1)
  ).join('')
  
  return `${method}${name.charAt(0).toUpperCase() + name.slice(1)}`
}

/**
 * 生成 TypeScript 类型定义
 */
function generateTypes(schemas) {
  if (!schemas) {return ''}
  
  let types = '// 自动生成的类型定义\n\n'
  
  for (const [name, schema] of Object.entries(schemas)) {
    if (schema.type === 'object' && schema.properties) {
      types += `export interface ${name} {\n`
      for (const [propName, prop] of Object.entries(schema.properties)) {
        const optional = schema.required?.includes(propName) ? '' : '?'
        const type = mapSchemaType(prop)
        types += `  ${propName}${optional}: ${type}\n`
      }
      types += '}\n\n'
    }
  }
  
  return types
}

/**
 * 映射 OpenAPI 类型到 TypeScript 类型
 */
function mapSchemaType(schema) {
  if (!schema) {return 'any'}
  
  if (schema.$ref) {
    const typeName = schema.$ref.split('/').pop()
    return typeName
  }
  
  if (schema.type === 'array') {
    const itemType = mapSchemaType(schema.items)
    return `${itemType}[]`
  }
  
  const typeMap = {
    'string': 'string',
    'number': 'number',
    'integer': 'number',
    'boolean': 'boolean',
    'object': 'Record<string, any>'
  }
  
  return typeMap[schema.type] || 'any'
}

/**
 * 生成 API 函数
 */
function generateApiFunctions(paths) {
  let code = ''
  
  for (const [path, methods] of Object.entries(paths)) {
    for (const [method, operation] of Object.entries(methods)) {
      if (['get', 'post', 'put', 'delete', 'patch'].includes(method)) {
        const funcName = pathToFunctionName(path, method)
        const summary = operation.summary || operation.description || ''
        
        code += `/**\n * ${summary}\n`
        
        // 添加参数注释
        const params = operation.parameters || []
        params.forEach(param => {
          code += ` * @param ${param.name} ${param.description || ''}\n`
        })
        
        code += ` */\n`
        code += `export function ${funcName}(`
        
        // 生成函数参数
        const funcParams = []
        const pathParams = params.filter(p => p.in === 'path')
        const queryParams = params.filter(p => p.in === 'query')
        
        if (pathParams.length > 0) {
          pathParams.forEach(param => {
            const type = mapSchemaType(param.schema)
            funcParams.push(`${param.name}: ${type}`)
          })
        }
        
        if (queryParams.length > 0) {
          funcParams.push('params?: Record<string, any>')
        }
        
        if (operation.requestBody) {
          funcParams.push('data?: any')
        }
        
        code += funcParams.join(', ')
        code += `) {\n`
        
        // 生成请求代码
        let url = path
        pathParams.forEach(param => {
          url = url.replace(`{${param.name}}`, `\${${param.name}}`)
        })
        
        code += `  return request({\n`
        code += `    url: \`${url}\`,\n`
        code += `    method: '${method}'`
        
        if (queryParams.length > 0) {
          code += `,\n    params`
        }
        
        if (operation.requestBody) {
          code += `,\n    data`
        }
        
        code += `\n  })\n`
        code += `}\n\n`
      }
    }
  }
  
  return code
}

/**
 * 生成完整的 API 文件
 */
function generateApiFile(docs) {
  let content = `/**
 * 自动生成的 API 服务
 * 
 * ⚠️ 警告: 此文件由工具自动生成，请勿手动修改
 * 生成时间: ${new Date().toISOString()}
 * API 版本: ${docs.info?.version || 'unknown'}
 */

import request from '@/utils/request'

`
  
  // 生成类型定义
  if (docs.components?.schemas) {
    content += generateTypes(docs.components.schemas)
  }
  
  // 生成 API 函数
  if (docs.paths) {
    content += generateApiFunctions(docs.paths)
  }
  
  return content
}

/**
 * 主函数
 */
async function main() {
  try {
    // 获取 Swagger 文档
    const docs = await fetchSwaggerDocs()
    
    // 创建输出目录
    if (!existsSync(OUTPUT_DIR)) {
      mkdirSync(OUTPUT_DIR, { recursive: true })
      console.log(`📁 创建输出目录: ${OUTPUT_DIR}`)
    }
    
    // 生成 API 文件
    console.log('🔨 正在生成 API 代码...')
    const apiContent = generateApiFile(docs)
    const outputFile = join(OUTPUT_DIR, 'api.ts')
    writeFileSync(outputFile, apiContent, 'utf-8')
    
    console.log('✅ API 代码生成成功!')
    console.log(`📄 输出文件: ${outputFile}`)
    console.log('')
    console.log('💡 使用方法:')
    console.log('   import { getFunctionName } from \'@/api/generated/api\'')
    
  } catch (error) {
    console.error('❌ 生成失败:', error.message)
    process.exit(1)
  }
}

main()
