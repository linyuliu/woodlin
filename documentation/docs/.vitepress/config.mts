/**
 * VitePress 配置文件
 * 
 * @author mumu
 * @description Woodlin 项目文档配置
 * @see https://vitepress.dev/reference/site-config
 */

import { defineConfig } from 'vitepress'

/**
 * VitePress 配置
 */
export default defineConfig({
  title: "Woodlin 文档",
  description: "Woodlin 多租户中后台管理系统完整文档",
  lang: 'zh-CN',
  
  // 主题配置
  themeConfig: {
    // 网站标题
    siteTitle: 'Woodlin 文档',
    
    // Logo
    logo: '/logo.svg',
    
    // 导航栏
    nav: [
      { text: '首页', link: '/' },
      { text: '快速开始', link: '/guide/getting-started' },
      { text: '模块文档', link: '/modules/overview' },
      { text: '开发指南', link: '/development/code-style' },
      { text: '部署指南', link: '/deployment/overview' },
      { text: 'API 文档', link: '/api/overview' }
    ],

    // 侧边栏
    sidebar: {
      '/guide/': [
        {
          text: '指南',
          items: [
            { text: '项目介绍', link: '/guide/introduction' },
            { text: '快速开始', link: '/guide/getting-started' },
            { text: '技术架构', link: '/guide/architecture' },
            { text: '目录结构', link: '/guide/directory-structure' },
            { text: '配置说明', link: '/guide/configuration' }
          ]
        }
      ],
      
      '/modules/': [
        {
          text: '模块概览',
          items: [
            { text: '模块总览', link: '/modules/overview' }
          ]
        },
        {
          text: '基础模块',
          items: [
            { text: 'Dependencies - 依赖管理', link: '/modules/dependencies' },
            { text: 'Common - 通用模块', link: '/modules/common' },
            { text: 'Security - 安全认证', link: '/modules/security' }
          ]
        },
        {
          text: '核心模块',
          items: [
            { text: 'System - 系统管理', link: '/modules/system' },
            { text: 'Tenant - 多租户', link: '/modules/tenant' },
            { text: 'File - 文件管理', link: '/modules/file' },
            { text: 'Task - 任务调度', link: '/modules/task' },
            { text: 'Generator - 代码生成', link: '/modules/generator' },
            { text: 'SQL2API - 动态接口', link: '/modules/sql2api' }
          ]
        },
        {
          text: '应用模块',
          items: [
            { text: 'Admin - 管理后台', link: '/modules/admin' },
            { text: 'Web - 前端应用', link: '/modules/web' }
          ]
        }
      ],
      
      '/development/': [
        {
          text: '开发指南',
          items: [
            { text: '代码规范', link: '/development/code-style' },
            { text: '开发环境搭建', link: '/development/environment-setup' },
            { text: '调试技巧', link: '/development/debugging' },
            { text: '测试指南', link: '/development/testing' },
            { text: '提交规范', link: '/development/commit-convention' },
            { text: '贡献指南', link: '/development/contributing' }
          ]
        },
        {
          text: '进阶开发',
          items: [
            { text: '自定义模块', link: '/development/custom-module' },
            { text: '扩展开发', link: '/development/extension' },
            { text: '性能优化', link: '/development/performance' }
          ]
        }
      ],
      
      '/deployment/': [
        {
          text: '部署指南',
          items: [
            { text: '部署概览', link: '/deployment/overview' },
            { text: '本地部署', link: '/deployment/local' },
            { text: 'Docker 部署', link: '/deployment/docker' },
            { text: 'Kubernetes 部署', link: '/deployment/kubernetes' },
            { text: '生产环境配置', link: '/deployment/production' },
            { text: '监控与运维', link: '/deployment/monitoring' }
          ]
        }
      ],
      
      '/api/': [
        {
          text: 'API 文档',
          items: [
            { text: 'API 概览', link: '/api/overview' },
            { text: '认证授权', link: '/api/authentication' },
            { text: '系统管理', link: '/api/system' },
            { text: '用户管理', link: '/api/user' },
            { text: '角色权限', link: '/api/role' },
            { text: '租户管理', link: '/api/tenant' },
            { text: '文件管理', link: '/api/file' },
            { text: '任务调度', link: '/api/task' },
            { text: 'SQL2API', link: '/api/sql2api' }
          ]
        }
      ]
    },

    // 社交链接
    socialLinks: [
      { icon: 'github', link: 'https://github.com/linyuliu/woodlin' }
    ],

    // 页脚
    footer: {
      message: '基于 MIT 许可发布',
      copyright: 'Copyright © 2024-present mumu'
    },

    // 搜索
    search: {
      provider: 'local',
      options: {
        locales: {
          root: {
            translations: {
              button: {
                buttonText: '搜索文档',
                buttonAriaLabel: '搜索文档'
              },
              modal: {
                noResultsText: '无法找到相关结果',
                resetButtonTitle: '清除查询条件',
                footer: {
                  selectText: '选择',
                  navigateText: '切换',
                  closeText: '关闭'
                }
              }
            }
          }
        }
      }
    },

    // 编辑链接
    editLink: {
      pattern: 'https://github.com/linyuliu/woodlin/edit/main/documentation/docs/:path',
      text: '在 GitHub 上编辑此页面'
    },

    // 最后更新时间
    lastUpdated: {
      text: '最后更新于',
      formatOptions: {
        dateStyle: 'full',
        timeStyle: 'short'
      }
    },

    // 文档页脚
    docFooter: {
      prev: '上一页',
      next: '下一页'
    },

    // 大纲
    outline: {
      level: [2, 3],
      label: '页面导航'
    },

    // 返回顶部
    returnToTopLabel: '返回顶部',

    // 侧边栏菜单标签
    sidebarMenuLabel: '菜单',

    // 深色模式标签
    darkModeSwitchLabel: '主题',
    lightModeSwitchTitle: '切换到浅色模式',
    darkModeSwitchTitle: '切换到深色模式'
  },

  // Markdown 配置
  markdown: {
    lineNumbers: true,
    
    // 代码高亮配置
    theme: {
      light: 'github-light',
      dark: 'github-dark'
    },

    // 支持的语言
    languages: [
      'java',
      'javascript',
      'typescript',
      'vue',
      'html',
      'css',
      'scss',
      'json',
      'yaml',
      'xml',
      'sql',
      'bash',
      'shell',
      'dockerfile',
      'properties',
      'tex',
      'latex',
      'python',
      'go',
      'rust',
      'c',
      'cpp'
    ]
  },

  // 头部配置
  head: [
    ['link', { rel: 'icon', href: '/favicon.ico' }],
    ['meta', { name: 'author', content: 'mumu' }],
    ['meta', { name: 'keywords', content: 'Woodlin, Spring Boot, 多租户, 管理系统, Java, Vue' }],
    // 中文字体配置
    ['link', { 
      rel: 'stylesheet', 
      href: 'https://cdn.jsdelivr.net/npm/lxgw-wenkai-webfont@1.1.0/style.css'
    }],
    ['link', { 
      rel: 'stylesheet', 
      href: 'https://cdn.jsdelivr.net/npm/lxgw-wenkai-screen-webfont@1.1.0/style.css'
    }]
  ],

  // 站点地图
  sitemap: {
    hostname: 'https://woodlin.example.com'
  },

  // 忽略死链检查（开发中）
  ignoreDeadLinks: true
})
