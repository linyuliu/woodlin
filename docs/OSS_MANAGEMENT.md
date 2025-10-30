# OSS资源管理系统文档

## 概述

Woodlin OSS资源管理系统是一个功能完善的对象存储管理解决方案，支持多种存储平台，提供安全的文件上传、下载、管理功能。

## 核心特性

### 1. 多平台支持

系统支持以下存储平台：

- **本地存储（Local）**: 文件存储在服务器本地磁盘
- **MinIO**: 开源的对象存储服务，支持私有化部署
- **AWS S3**: 亚马逊云存储服务，也兼容S3协议的其他存储
- **阿里云OSS**: 阿里云对象存储服务
- **腾讯云COS**: 腾讯云对象存储服务
- **华为云OBS**: 华为云对象存储服务

### 2. 安全上传机制

- **令牌验证**: 所有上传必须使用令牌和签名，防止未授权访问
- **文件类型检测**: 使用Apache Tika检测文件真实类型，防止文件伪装
- **大小限制**: 支持配置文件大小限制
- **扩展名白名单**: 支持配置允许的文件扩展名
- **MIME类型验证**: 支持配置允许的MIME类型

### 3. 上传策略

系统支持灵活的上传策略配置：

- 是否检测文件真实类型（Apache Tika）
- 是否检查文件大小
- 是否校验MD5
- 是否允许重复上传
- 是否生成缩略图（图片）
- 文件路径模式（支持时间、用户等占位符）
- 文件名模式（支持时间戳、随机字符串、UUID等）
- 签名有效期

### 4. 智能文件管理

- **秒传功能**: 通过MD5查重，已上传的文件无需重复上传
- **图片处理**: 自动识别图片并获取尺寸信息
- **访问统计**: 记录文件访问次数和最后访问时间
- **预签名URL**: 支持生成临时访问链接
- **文件过期**: 支持设置文件过期时间

## 数据库设计

### 1. 存储配置表 (sys_storage_config)

存储各个存储平台的配置信息：

```sql
- config_id: 配置ID
- config_name: 配置名称
- storage_type: 存储类型（local/minio/s3/oss/cos/obs）
- access_key: 访问密钥
- secret_key: 密钥（加密存储）
- endpoint: 终端节点地址
- bucket_name: 存储桶名称
- region: 区域
- base_path: 基础路径
- domain: 自定义域名
- is_default: 是否为默认配置
- is_public: 是否公开访问
- status: 状态
- max_file_size: 最大文件大小
- allowed_extensions: 允许的文件扩展名
```

### 2. 上传策略表 (sys_upload_policy)

定义文件上传的策略和规则：

```sql
- policy_id: 策略ID
- policy_name: 策略名称
- policy_code: 策略编码
- storage_config_id: 关联的存储配置ID
- detect_file_type: 是否检测文件真实类型
- check_file_size: 是否检查文件大小
- max_file_size: 最大文件大小
- allowed_extensions: 允许的文件扩展名
- allowed_mime_types: 允许的MIME类型
- check_md5: 是否校验MD5
- allow_duplicate: 是否允许重复上传
- generate_thumbnail: 是否生成缩略图
- path_pattern: 文件路径模式
- file_name_pattern: 文件名模式
- signature_expires: 签名有效期
```

### 3. 文件信息表 (sys_file)

存储文件的元数据信息：

```sql
- file_id: 文件ID
- file_name: 文件名称
- original_name: 原始文件名
- file_path: 文件路径
- file_url: 文件URL
- file_size: 文件大小
- file_extension: 文件扩展名
- mime_type: MIME类型
- detected_mime_type: 检测到的真实MIME类型
- file_md5: 文件MD5
- file_sha256: 文件SHA256
- storage_type: 存储类型
- storage_config_id: 存储配置ID
- upload_policy_id: 上传策略ID
- is_image: 是否为图片
- image_width: 图片宽度
- image_height: 图片高度
- thumbnail_path: 缩略图路径
- is_public: 是否公开
- access_count: 访问次数
```

### 4. 上传令牌表 (sys_upload_token)

用于签名验证和安全控制：

```sql
- token_id: 令牌ID
- token: 上传令牌
- policy_id: 上传策略ID
- signature: 签名
- max_file_size: 最大文件大小
- expire_time: 过期时间
- is_used: 是否已使用
```

## API接口

### 1. 文件上传接口

#### 1.1 获取上传令牌

```http
POST /file/upload/token
Content-Type: application/json

{
  "policyCode": "default",
  "fileName": "example.jpg",
  "fileSize": 1024000
}
```

响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "abc123...",
    "signature": "def456...",
    "credential": "{...}",
    "expireTime": "2025-01-30T15:00:00",
    "maxFileSize": 104857600,
    "allowedExtensions": "jpg,png,pdf"
  }
}
```

#### 1.2 上传文件

```http
POST /file/upload
Content-Type: multipart/form-data

file: [文件]
token: abc123...
signature: def456...
```

#### 1.3 快速上传

```http
POST /file/upload/quick
Content-Type: multipart/form-data

file: [文件]
policyCode: default
```

#### 1.4 秒传检查

```http
GET /file/upload/check/{md5}
```

### 2. 文件管理接口

#### 2.1 分页查询文件

```http
GET /file/manage/page?current=1&size=20
```

#### 2.2 获取文件详情

```http
GET /file/manage/{fileId}
```

#### 2.3 获取文件访问URL

```http
GET /file/manage/{fileId}/url?expirationTime=3600
```

#### 2.4 下载文件

```http
GET /file/manage/{fileId}/download
```

#### 2.5 删除文件

```http
DELETE /file/manage/{fileId}
```

#### 2.6 批量删除文件

```http
DELETE /file/manage/batch
Content-Type: application/json

[123, 456, 789]
```

#### 2.7 搜索文件

```http
GET /file/manage/search?keyword=example&current=1&size=20
```

## 配置说明

### application.yml 配置示例

```yaml
woodlin:
  file:
    # 本地存储配置
    local:
      enabled: true
      base-path: ./uploads/
      domain: http://localhost:8080
    
    # MinIO配置
    minio:
      enabled: false
      endpoint: http://localhost:9000
      access-key: minioadmin
      secret-key: minioadmin
      bucket-name: woodlin
      domain: http://localhost:9000
    
    # AWS S3配置
    s3:
      enabled: false
      endpoint: https://s3.amazonaws.com
      access-key: your-access-key
      secret-key: your-secret-key
      region: us-east-1
      bucket-name: your-bucket
    
    # 阿里云OSS配置
    oss:
      enabled: false
      endpoint: https://oss-cn-hangzhou.aliyuncs.com
      access-key: your-access-key
      secret-key: your-secret-key
      bucket-name: your-bucket
    
    # 腾讯云COS配置
    cos:
      enabled: false
      access-key: your-access-key
      secret-key: your-secret-key
      region: ap-guangzhou
      bucket-name: your-bucket
    
    # 华为云OBS配置
    obs:
      enabled: false
      endpoint: https://obs.cn-north-4.myhuaweicloud.com
      access-key: your-access-key
      secret-key: your-secret-key
      bucket-name: your-bucket
```

## 使用示例

### 前端上传文件（两步法）

```javascript
// 1. 获取上传令牌
async function getUploadToken() {
  const response = await fetch('/file/upload/token', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      policyCode: 'default',
      fileName: 'example.jpg'
    })
  });
  return await response.json();
}

// 2. 上传文件
async function uploadFile(file, token, signature) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('token', token);
  formData.append('signature', signature);
  
  const response = await fetch('/file/upload', {
    method: 'POST',
    body: formData
  });
  return await response.json();
}

// 使用示例
const tokenData = await getUploadToken();
const result = await uploadFile(file, tokenData.data.token, tokenData.data.signature);
console.log('上传成功:', result);
```

### 前端快速上传（简化）

```javascript
async function quickUpload(file) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('policyCode', 'default');
  
  const response = await fetch('/file/upload/quick', {
    method: 'POST',
    body: formData
  });
  return await response.json();
}
```

### 秒传功能

```javascript
// 1. 计算文件MD5
async function calculateMD5(file) {
  // 使用 spark-md5 或其他MD5库
  // ...
  return md5;
}

// 2. 检查文件是否存在
async function checkFileExists(md5) {
  const response = await fetch(`/file/upload/check/${md5}`);
  const result = await response.json();
  return result.data; // 如果文件已存在，返回文件信息
}

// 3. 秒传逻辑
const md5 = await calculateMD5(file);
const existingFile = await checkFileExists(md5);
if (existingFile) {
  console.log('文件已存在，秒传成功');
  return existingFile;
} else {
  // 执行正常上传
  return await quickUpload(file);
}
```

## 安全建议

1. **密钥管理**: 
   - 存储配置中的密钥应加密存储
   - 使用环境变量或密钥管理服务
   
2. **访问控制**:
   - 上传令牌有时效性
   - 私有文件使用预签名URL
   
3. **文件验证**:
   - 启用文件类型检测
   - 设置合理的文件大小限制
   - 配置扩展名和MIME类型白名单
   
4. **审计日志**:
   - 记录文件上传、下载、删除操作
   - 记录上传者IP和User-Agent

## 性能优化

1. **大文件上传**:
   - 使用分片上传
   - 前端直传到OSS
   
2. **缓存策略**:
   - 公开文件启用CDN
   - 使用浏览器缓存
   
3. **数据库优化**:
   - 为MD5、SHA256字段建立索引
   - 定期清理过期令牌

## 故障排查

### 常见问题

1. **上传失败**: 检查存储配置是否正确，密钥是否有效
2. **文件类型检测失败**: 确认Apache Tika依赖已正确引入
3. **预签名URL无法访问**: 检查存储配置中的endpoint和domain设置
4. **令牌过期**: 调整signature_expires配置

## 扩展开发

### 添加新的存储平台

1. 实现 `StorageService` 接口
2. 在 `StorageType` 枚举中添加新类型
3. 创建配置类
4. 注册为Spring Bean

### 自定义上传策略

1. 在数据库中添加新策略
2. 配置策略参数
3. 通过API使用新策略

## 版本历史

- v1.0.0 (2025-01-30): 初始版本，支持6种存储平台
