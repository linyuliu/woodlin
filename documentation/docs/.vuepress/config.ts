/**
 * VuePress Theme Hope 配置文件
 * 
 * @author mumu
 * @description Woodlin 项目文档配置
 * @see https://theme-hope.vuejs.press/zh/
 */

import { defineUserConfig } from "vuepress"
import { viteBundler } from "@vuepress/bundler-vite"
import { hopeTheme } from "vuepress-theme-hope"

export default defineUserConfig({
  // 使用 Vite 打包器
  bundler: viteBundler(),
  // 站点配置
  lang: "zh-CN",
  title: "Woodlin 文档",
  description: "Woodlin 多租户中后台管理系统完整文档",

  // 部署站点的基础路径
  base: "/",

  // 头部配置
  head: [
    ["link", { rel: "icon", href: "/favicon.ico" }],
    ["meta", { name: "author", content: "mumu" }],
    ["meta", { name: "keywords", content: "Woodlin, Spring Boot, 多租户, 管理系统, Java, Vue" }],
    // DNS 预连接优化
    ["link", { rel: "preconnect", href: "https://cdn.jsdelivr.net" }],
    ["link", { rel: "dns-prefetch", href: "https://cdn.jsdelivr.net" }],
    // 中文字体配置 - 使用 preload 优化加载
    [
      "link",
      {
        rel: "stylesheet",
        href: "https://cdn.jsdelivr.net/npm/lxgw-wenkai-webfont@1.1.0/style.css",
        media: "all",
      },
    ],
    [
      "link",
      {
        rel: "stylesheet",
        href: "https://cdn.jsdelivr.net/npm/lxgw-wenkai-screen-webfont@1.1.0/style.css",
        media: "all",
      },
    ],
    // 添加中文字体的 fallback 保证
    [
      "style",
      {},
      `
      @font-face {
        font-family: 'Fallback Chinese';
        src: local('PingFang SC'), local('Microsoft YaHei'), local('SimSun'), local('STHeiti'), local('WenQuanYi Micro Hei');
        font-weight: normal;
        font-style: normal;
        font-display: swap;
      }
    `,
    ],
  ],

  // 主题配置
  theme: hopeTheme({
    // 主题基本选项
    hostname: "https://woodlin.example.com",

    // 作者信息
    author: {
      name: "mumu",
      url: "https://github.com/linyuliu/woodlin",
    },

    // 导航栏
    navbar: [
      { text: "首页", link: "/" },
      { text: "快速开始", link: "/guide/getting-started.md" },
      { text: "模块文档", link: "/modules/overview.md" },
      { text: "开发指南", link: "/development/code-style.md" },
      { text: "部署指南", link: "/deployment/overview.md" },
      { text: "API 文档", link: "/api/overview.md" },
    ],

    // 侧边栏
    sidebar: {
      "/guide/": [
        {
          text: "指南",
          children: [
            "introduction.md",
            "getting-started.md",
            "architecture.md",
            "directory-structure.md",
            "configuration.md",
          ],
        },
      ],
      "/modules/": [
        {
          text: "模块概览",
          children: ["overview.md"],
        },
        {
          text: "基础模块",
          children: [
            "dependencies.md",
            "common.md",
            "security.md",
          ],
        },
        {
          text: "核心模块",
          children: [
            "system.md",
            "tenant.md",
            "file.md",
            "task.md",
            "generator.md",
            "sql2api.md",
          ],
        },
        {
          text: "应用模块",
          children: [
            "admin.md",
            "web.md",
          ],
        },
      ],
      "/development/": [
        {
          text: "开发指南",
          children: [
            "code-style.md",
            "environment-setup.md",
            "debugging.md",
            "testing.md",
            "commit-convention.md",
            "contributing.md",
          ],
        },
        {
          text: "进阶开发",
          children: [
            "custom-module.md",
            "extension.md",
            "performance.md",
          ],
        },
      ],
      "/deployment/": [
        {
          text: "部署指南",
          children: [
            "overview.md",
            "local.md",
            "docker.md",
            "kubernetes.md",
            "production.md",
            "monitoring.md",
          ],
        },
      ],
      "/api/": [
        {
          text: "API 文档",
          children: [
            "overview.md",
            "authentication.md",
            "system.md",
            "user.md",
            "role.md",
            "tenant.md",
            "file.md",
            "task.md",
            "sql2api.md",
          ],
        },
      ],
    },

    // 仓库配置
    repo: "linyuliu/woodlin",
    repoLabel: "GitHub",
    repoDisplay: true,

    // 文档仓库配置
    docsRepo: "linyuliu/woodlin",
    docsDir: "documentation/docs",
    docsBranch: "main",

    // 编辑链接
    editLink: true,
    editLinkText: "在 GitHub 上编辑此页面",

    // 最后更新时间
    lastUpdated: true,
    lastUpdatedText: "最后更新于",

    // 贡献者
    contributors: true,
    contributorsText: "贡献者",

    // 页脚
    footer: "基于 MIT 许可发布 | Copyright © 2024-present mumu",
    displayFooter: true,

    // 加密配置（如果需要）
    // encrypt: {
    //   config: {},
    // },

    // 插件配置
    plugins: {
      // 代码高亮
      prismjs: {
        lineNumbers: true,
      },

      // Markdown 增强
      mdEnhance: {
        // 启用任务列表
        tasklist: true,
        // 启用对齐
        align: true,
        // 启用属性
        attrs: true,
        // 启用上下角标
        sub: true,
        sup: true,
        // 启用标记
        mark: true,
        // 启用脚注
        footnote: true,
        // GFM 警告
        gfm: true,
        // 启用代码演示
        demo: true,
        // 启用 Mermaid 流程图
        mermaid: true,
        // 启用组件
        component: true,
        // 启用包含文件
        include: true,
        // 启用样式化
        stylize: [
          {
            matcher: "Recommended",
            replacer: ({ tag }) => {
              if (tag === "em")
                return {
                  tag: "Badge",
                  attrs: { type: "tip" },
                  content: "Recommended",
                };
            },
          },
        ],
      },

      // 数学公式支持 (使用 markdownMath 替代 katex)
      markdownMath: {
        type: "katex",
      },

      // Markdown 标签页 (使用 markdownTab 替代 codetabs)
      markdownTab: {
        tabs: true,
        codeTabs: true,
      },

      // Markdown 图片增强
      markdownImage: {
        figure: true,
        lazyload: true,
        mark: true,
        size: true,
      },

      // 代码复制
      copyCode: {
        showInMobile: true,
      },

      // 版权信息
      copyright: {
        author: "mumu",
        license: "MIT",
        triggerLength: 100,
        maxLength: 700,
        canonical: "https://woodlin.example.com",
        global: false,
      },

      // Git 信息
      git: true,

      // 自动目录
      catalog: true,

      // PWA（渐进式网络应用）
      pwa: false,

      // 评论（可选）
      // comment: {
      //   provider: "Giscus",
      //   repo: "linyuliu/woodlin",
      //   repoId: "your-repo-id",
      //   category: "Announcements",
      //   categoryId: "your-category-id",
      // },

      // 组件
      components: {
        components: ["Badge", "VPCard"],
      },
    },

    // 多语言（如果需要）
    locales: {
      "/": {
        // 导航栏
        navbar: [
          { text: "首页", link: "/" },
          { text: "快速开始", link: "/guide/getting-started.md" },
          { text: "模块文档", link: "/modules/overview.md" },
          { text: "开发指南", link: "/development/code-style.md" },
          { text: "部署指南", link: "/deployment/overview.md" },
          { text: "API 文档", link: "/api/overview.md" },
        ],

        // 侧边栏
        sidebar: "structure",

        // 页脚
        footer: "基于 MIT 许可发布 | Copyright © 2024-present mumu",
        displayFooter: true,

        // 博客配置（如果需要）
        blog: false,
      },
    },
  }),

  // 开发服务器配置
  // 端口配置
  port: 5173,

  // 开发服务器主机名
  host: "0.0.0.0",

  // 是否在开发服务器启动后打开浏览器
  open: false,

})
