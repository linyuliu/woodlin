/**
 * VuePress 客户端配置
 * 
 * @author mumu
 * @description 客户端增强配置，包括自定义字体加载
 */

import { defineClientConfig } from "vuepress/client"

export default defineClientConfig({
  enhance({ app, router, siteData }) {
    // 在这里可以添加客户端增强逻辑
  },
  setup() {
    // 在这里可以添加组合式 API
  },
  rootComponents: [],
})
