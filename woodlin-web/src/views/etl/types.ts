/**
 * ETL 视图层共享类型
 */

import type { SelectOption } from 'naive-ui'
import type { TableMetadata } from '@/api/datasource'

/**
 * 带选择键的源表行
 */
export interface EtlSelectableTable extends TableMetadata {
  mappingKey: string
}

/**
 * 下拉选项
 */
export type EtlSelectOption = SelectOption
