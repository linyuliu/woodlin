# woodlin-module-security - 安全认证模块

## 模块概述

安全认证模块当前的实际 Maven artifactId 为 `woodlin-module-security`，位于 `woodlin-modules/woodlin-module-security`。  
它基于 Sa-Token 1.44.0 提供统一的登录认证、权限校验、会话管理、加密解密和密码策略能力。

## Maven 坐标

```xml
<dependency>
    <groupId>com.mumu</groupId>
    <artifactId>woodlin-module-security</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 核心能力

- 登录认证与 Token 管理
- RBAC 权限校验
- 基于 Sa-Token 的会话能力
- AES / RSA / SM4 等加密能力
- 密码策略与安全配置

## 当前定位

`woodlin-module-security` 是主业务模块之一，由 `woodlin-apps/woodlin-admin` 装配使用。  
它不再是一个独立的根级目录模块，也不再依赖主应用的全局扫包，而是通过模块自己的自动装配进行注册。

## 相关文档

- [模块总览](./overview)
- [技术架构](/guide/architecture)
- [配置说明](/guide/configuration)
