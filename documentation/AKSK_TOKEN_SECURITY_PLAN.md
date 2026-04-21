# AK/SK、Token、网关与加密策略完善方案

## 1. 当前仓库扫描结论

### 1.1 已有能力

- 已有统一的 `Token` 登录与鉴权链路，基于 Sa-Token。
- 已有系统配置页、系统配置表、字典管理页、动态字典表。
- 已有 API 加密配置页面，可配置 `AES`、`RSA`、`SM4`。
- 已有文件上传场景的“令牌 + 签名”能力，但它只适用于上传，不是通用开放 API 的 AK/SK 模型。

### 1.2 明确缺口

- 没有真正的通用 `AK/SK` 凭证模型，也没有应用/客户端表。
- 没有请求签名规范：缺少 `timestamp`、`nonce`、规范化 canonical request、签名串规则、重放防护。
- 没有“仅 Token / 仅 AKSK / 同时开启”三种模式的统一后端执行链。
- 没有独立网关实现，仓库文档中网关仍是“可选层”。
- 前后端加密链路不一致：前端仍有 XOR 占位实现，后端是 AES/RSA/SM4。
- 国密链路不完整：SM4 代码依赖 BC provider，但仓库未提供对应依赖和初始化。
- SQL2API 里虽然有 `authType` / `encryptAlgorithm` 字段，但当前仅是数据结构与页面选项，未形成运行时 enforcement。

## 2. 目标方案

### 2.1 统一安全模式

新增统一安全模式 `api_security_mode`：

- `TOKEN`
- `AKSK`
- `TOKEN_AND_AKSK`
- `NONE`

运行时原则：

- 管理后台默认 `TOKEN`
- 对外开放 API 默认 `AKSK`
- 高敏接口默认 `TOKEN_AND_AKSK`

### 2.2 AK/SK 数据模型

建议新增：

- `sys_open_app`
    - 应用标识、应用名称、状态、租户、负责人、IP 白名单、备注
- `sys_open_app_credential`
    - `access_key`
    - `secret_key_hash`
    - `signature_algorithm`
    - `encrypt_algorithm`
    - `security_mode`
    - 生效时间、失效时间、轮换时间、状态
- `sys_open_api_policy`
    - 路径匹配
    - 请求方法
    - 是否要求 Token
    - 是否要求 AK/SK
    - 签名算法
    - 加密算法
    - 时间窗
    - nonce 防重放开关

`SK` 不落库明文，只存密文或摘要，并通过密钥管理机制做二次保护。

## 3. 鉴权链路

### 3.1 请求头规范

建议统一为：

- `Authorization: Bearer <token>`
- `X-Access-Key: <ak>`
- `X-Signature-Algorithm: HMAC_SHA256`
- `X-Timestamp: <unix_seconds>`
- `X-Nonce: <random>`
- `X-Signature: <base64_signature>`
- `X-Encrypt-Algorithm: AES_GCM`
- `X-Request-Id: <trace_id>`

### 3.2 签名串规范

建议 canonical string：

1. HTTP Method
2. URI Path
3. 排序后的 Query String
4. 请求体摘要 `SHA-256(body)`
5. `timestamp`
6. `nonce`
7. `tenantId`
8. `accessKey`

### 3.3 三种模式执行逻辑

- `TOKEN`
    - 校验登录态
- `AKSK`
    - 校验 AK
    - 校验时间窗
    - 校验 nonce 未重复
    - 校验签名
- `TOKEN_AND_AKSK`
    - 两者都必须成功

## 4. 网关与应用一致性

建议后续新增网关模块时，统一以下配置键，并与应用侧共用字典和配置模型：

- `api.security.mode`
- `api.signature.algorithm`
- `api.signature.timestamp-window-seconds`
- `api.signature.nonce-enabled`
- `api.encryption.algorithm`
- `api.encryption.required`

网关负责：

- 时间窗校验
- nonce 防重放
- AK/SK 签名验签
- 基础限流
- 透传鉴权上下文到业务服务

应用服务负责：

- Token 登录态
- 细粒度权限
- 业务审计
- 租户隔离

## 5. 加密策略建议

### 5.1 国际通用算法

推荐优先级：

1. `AES_GCM`
2. `HMAC_SHA256`
3. `RSA_OAEP_SHA256`
4. `RSA_SHA256`
5. `AES_CBC` 仅兼容保留

### 5.2 国密算法

推荐提供：

- `SM2_SM3` 用于签名
- `SM4_CBC` 用于对称加密
- `SM4_GCM` 若底层库和合规要求允许，可作为增强选项

### 5.3 默认组合

- 国际默认：
    - 签名 `HMAC_SHA256`
    - 加密 `AES_GCM`
- 国密默认：
    - 签名 `SM2_SM3`
    - 加密 `SM4_CBC`

## 6. 前端与系统管理

建议把完整配置放在“系统管理”下的新页面或增强后的“系统设置”页中，至少包含：

- 安全模式选择
- Token / AKSK / 双因子模式切换
- 签名算法
- 加密算法
- 时间窗
- nonce 开关
- 密钥轮换策略
- 客户端应用管理
- 凭证吊销 / 重发

当前仓库已具备：

- `系统设置`
- `配置管理`
- `字典管理`

因此可以直接把该能力落在系统管理目录中，无需新建一级菜单。

## 7. 动态字典建议

本次已补充初始化字典：

- `api_security_mode`
- `api_signature_algorithm`
- `api_encryption_algorithm`

后续还建议补：

- `api_signature_location`
- `api_key_status`
- `api_client_type`
- `api_nonce_strategy`

## 8. 分阶段实施建议

### Phase 1

- 补齐 AK/SK 数据模型
- 落地统一签名规范
- 完成 `AKSK` 与 `TOKEN_AND_AKSK` 后端校验链
- 统一网关/应用配置键

### Phase 2

- 前端系统管理页面接入应用管理、凭证管理、策略管理
- 动态字典完全替代硬编码枚举
- 密钥轮换、吊销、审计日志

### Phase 3

- 国际算法与国密算法双栈
- 网关统一验签 + 应用内细粒度鉴权
- SDK / 示例代码 / 文档 / Postman 集成

## 9. 风险提示

- 不能继续使用前端 XOR 占位加密。
- 不能把上传签名逻辑当作通用 AK/SK 实现。
- 不能只做页面和配置，不做统一验签规则。
- 国密能力必须补齐密码库依赖、Provider 初始化、联调测试和兼容性验证。
