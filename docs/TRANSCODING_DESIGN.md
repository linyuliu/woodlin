# 文件转码系统设计文档

## 概述

文件转码系统采用**策略模式**设计，支持多种类型的文件转换，包括文档转PDF、视频转码（降低码率/分辨率）、音频转换、图片格式转换等。

## 架构设计

### 1. 核心接口

#### TranscodingService
转码服务的统一接口，定义了转码操作的标准方法：

```java
public interface TranscodingService {
    // 同步转码
    InputStream transcode(InputStream inputStream, String sourceFormat, TranscodingOptions options);
    
    // 异步转码
    String transcodeAsync(Long fileId, String sourceFormat, TranscodingOptions options);
    
    // 查询任务状态
    TranscodingTaskStatus getTaskStatus(String taskId);
    
    // 获取转码类型
    TranscodingType getTranscodingType();
    
    // 判断是否支持该格式
    boolean supports(String sourceFormat);
}
```

### 2. 策略模式实现

#### TranscodingServiceFactory
工厂类，负责管理和路由不同的转码服务：

- **按类型路由**: 根据 `TranscodingType` 获取对应服务
- **按格式路由**: 根据源文件格式自动选择合适的服务
- **缓存机制**: 提高查找效率

#### AbstractTranscodingService
抽象基类，提供通用实现：

- 任务ID生成
- 异步任务提交模板
- 任务状态查询模板
- 参数验证

### 3. 具体实现

#### DocumentToPdfTranscodingService
文档转PDF服务，支持格式：
- Word: doc, docx
- Excel: xls, xlsx
- PowerPoint: ppt, pptx
- OpenOffice: odt, ods, odp
- Others: rtf, txt

#### VideoTranscodingService
视频转码服务，支持格式：
- mp4, avi, mov, wmv, flv, mkv, webm, m4v, 3gp

支持的转码操作：
- 降低码率（节省带宽）
- 降低分辨率（适配移动端）
- 格式转换
- 编码器切换（h264, h265, vp9等）

## 转码选项配置

### TranscodingOptions
灵活的配置类，支持各种转码场景：

```java
// 通用选项
targetFormat      // 目标格式
quality           // 输出质量（0-100）

// 视频选项
videoCodec        // 视频编码器（h264, h265, vp9）
videoBitrate      // 视频码率（kbps）
videoWidth        // 视频宽度
videoHeight       // 视频高度
frameRate         // 帧率
keepAspectRatio   // 保持宽高比

// 音频选项
audioCodec        // 音频编码器（aac, mp3, opus）
audioBitrate      // 音频码率（kbps）
sampleRate        // 采样率
channels          // 声道数

// 文档选项
pageOrientation   // 页面方向
pageSize          // 页面大小
preserveFormatting // 保留原始格式

// 高级选项
async             // 是否异步处理
callbackUrl       // 回调URL
customParams      // 自定义参数
```

## 实现方案

### 方案一：使用开源工具

#### 文档转PDF
**推荐方案**: LibreOffice Headless

```bash
# 安装
apt-get install libreoffice

# 使用
soffice --headless --convert-to pdf input.docx --outdir /output
```

**优点**: 
- 免费开源
- 支持格式丰富
- 转换质量高

**缺点**:
- 需要安装额外软件
- 启动较慢

**替代方案**:
1. **Apache POI + iText**: 纯Java实现，无需外部依赖
2. **Aspose.Words**: 商业库，功能强大但需要授权
3. **Pandoc**: 支持更多格式，命令行工具

#### 视频转码
**推荐方案**: FFmpeg

```bash
# 安装
apt-get install ffmpeg

# 降低码率和分辨率
ffmpeg -i input.mp4 \
  -c:v libx264 \
  -b:v 1000k \
  -s 1280x720 \
  -r 30 \
  -c:a aac \
  -b:a 128k \
  output.mp4
```

**优点**:
- 功能强大，支持几乎所有格式
- 性能优秀
- 免费开源

**缺点**:
- CPU密集型任务
- 需要外部进程调用

### 方案二：使用云服务

#### 阿里云
**文档转换**: 智能媒体管理(IMM)
```java
// 示例代码
ImmClient client = new ImmClient(...);
ConvertOfficeFormatRequest request = new ConvertOfficeFormatRequest()
    .setProject("project-name")
    .setSrcUri("oss://bucket/input.docx")
    .setTgtUri("oss://bucket/output.pdf")
    .setTgtType("pdf");
ConvertOfficeFormatResponse response = client.convertOfficeFormat(request);
```

**视频转码**: 视频点播(VOD)
```java
// 示例代码
DefaultAcsClient client = new DefaultAcsClient(...);
SubmitTranscodeJobsRequest request = new SubmitTranscodeJobsRequest();
request.setVideoId(videoId);
request.setTemplateGroupId(templateGroupId);
SubmitTranscodeJobsResponse response = client.getAcsResponse(request);
```

#### 腾讯云
**文档转换**: 数据万象(CI)
**视频转码**: 云点播(VOD)

#### 华为云
**视频转码**: 视频点播(VOD)

**优点**:
- 无需维护基础设施
- 自动扩容
- 转码速度快
- 支持更多高级功能

**缺点**:
- 需要付费
- 依赖外部服务

### 方案三：自建转码集群

使用消息队列 + Worker集群：

```
[上传文件] -> [消息队列] -> [转码Worker集群] -> [保存结果]
```

**架构组件**:
1. **消息队列**: RabbitMQ / Kafka
2. **Worker**: 独立的转码服务器
3. **任务调度**: XXL-JOB / Quartz
4. **进度跟踪**: Redis

## 集成建议

### 1. 文档转PDF集成步骤

#### 方案A: LibreOffice（推荐开始）

1. **安装LibreOffice**
```bash
apt-get install libreoffice
```

2. **实现转码逻辑**
```java
@Override
public InputStream transcode(InputStream inputStream, String sourceFormat, 
                             TranscodingOptions options) {
    // 保存输入文件
    File inputFile = saveToTempFile(inputStream, sourceFormat);
    File outputFile = createTempFile("pdf");
    
    // 执行转换
    String command = String.format(
        "soffice --headless --convert-to pdf %s --outdir %s",
        inputFile.getAbsolutePath(),
        outputFile.getParent()
    );
    
    Process process = Runtime.getRuntime().exec(command);
    process.waitFor();
    
    // 返回结果
    return new FileInputStream(outputFile);
}
```

#### 方案B: 云服务（推荐生产环境）

1. **添加SDK依赖**
```xml
<dependency>
    <groupId>com.aliyun</groupId>
    <artifactId>aliyun-java-sdk-imm</artifactId>
    <version>1.x.x</version>
</dependency>
```

2. **实现转码逻辑**
```java
@Override
public String transcodeAsync(Long fileId, String sourceFormat, 
                             TranscodingOptions options) {
    // 获取文件信息
    SysFile file = fileService.getById(fileId);
    
    // 提交到云服务
    String taskId = aliyunImmClient.convertDocument(
        file.getFileUrl(),
        options.getTargetFormat()
    );
    
    // 保存任务信息
    saveTaskInfo(taskId, fileId);
    
    return taskId;
}
```

### 2. 视频转码集成步骤

#### 方案A: FFmpeg（推荐开发测试）

1. **安装FFmpeg**
```bash
apt-get install ffmpeg
```

2. **实现转码逻辑**
```java
@Override
public InputStream transcode(InputStream inputStream, String sourceFormat,
                             TranscodingOptions options) {
    File inputFile = saveToTempFile(inputStream, sourceFormat);
    File outputFile = createTempFile(options.getTargetFormat());
    
    // 构建FFmpeg命令
    String command = buildFfmpegCommand(inputFile, outputFile, options);
    
    // 执行转码
    Process process = Runtime.getRuntime().exec(command);
    process.waitFor();
    
    return new FileInputStream(outputFile);
}

private String buildFfmpegCommand(File input, File output, 
                                  TranscodingOptions options) {
    return String.format(
        "ffmpeg -i %s -c:v %s -b:v %dk -s %dx%d -r %d -c:a %s -b:a %dk %s",
        input.getAbsolutePath(),
        options.getVideoCodec(),
        options.getVideoBitrate(),
        options.getVideoWidth(),
        options.getVideoHeight(),
        options.getFrameRate(),
        options.getAudioCodec(),
        options.getAudioBitrate(),
        output.getAbsolutePath()
    );
}
```

#### 方案B: 异步队列处理（推荐生产环境）

1. **添加消息队列依赖**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

2. **实现异步处理**
```java
@Override
public String transcodeAsync(Long fileId, String sourceFormat,
                             TranscodingOptions options) {
    String taskId = generateTaskId();
    
    // 创建任务记录
    TranscodeTask task = new TranscodeTask()
        .setTaskId(taskId)
        .setFileId(fileId)
        .setOptions(options)
        .setStatus("pending");
    saveTask(task);
    
    // 发送到消息队列
    rabbitTemplate.convertAndSend(
        "video.transcode.queue",
        task
    );
    
    return taskId;
}

@RabbitListener(queues = "video.transcode.queue")
public void processTranscodeTask(TranscodeTask task) {
    try {
        updateTaskStatus(task.getTaskId(), "processing");
        
        // 执行转码
        File result = executeFFmpeg(task);
        
        // 上传结果文件
        Long outputFileId = uploadResultFile(result);
        
        updateTaskStatus(task.getTaskId(), "completed", outputFileId);
    } catch (Exception e) {
        updateTaskStatus(task.getTaskId(), "failed", e.getMessage());
    }
}
```

## 数据库设计

### 转码任务表
```sql
CREATE TABLE `sys_transcoding_task` (
    `task_id` varchar(64) NOT NULL COMMENT '任务ID',
    `file_id` bigint(20) NOT NULL COMMENT '源文件ID',
    `transcoding_type` varchar(50) NOT NULL COMMENT '转码类型',
    `source_format` varchar(20) NOT NULL COMMENT '源格式',
    `target_format` varchar(20) NOT NULL COMMENT '目标格式',
    `options` text COMMENT '转码选项（JSON）',
    `status` varchar(20) NOT NULL COMMENT '状态',
    `progress` int(11) DEFAULT 0 COMMENT '进度',
    `output_file_id` bigint(20) DEFAULT NULL COMMENT '输出文件ID',
    `error_message` varchar(500) DEFAULT NULL COMMENT '错误信息',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `start_time` datetime DEFAULT NULL,
    `complete_time` datetime DEFAULT NULL,
    PRIMARY KEY (`task_id`),
    KEY `idx_file_id` (`file_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='转码任务表';
```

## API接口示例

### 1. 提交转码任务
```http
POST /file/transcoding/submit

{
  "fileId": 123456,
  "transcodingType": "video_transcode",
  "options": {
    "targetFormat": "mp4",
    "videoCodec": "h264",
    "videoBitrate": 1000,
    "videoWidth": 1280,
    "videoHeight": 720,
    "frameRate": 30,
    "audioCodec": "aac",
    "audioBitrate": 128
  }
}

Response:
{
  "code": 200,
  "data": {
    "taskId": "abc123def456"
  }
}
```

### 2. 查询任务状态
```http
GET /file/transcoding/status/{taskId}

Response:
{
  "code": 200,
  "data": {
    "taskId": "abc123def456",
    "status": "processing",
    "progress": 65,
    "createTime": "2025-01-31T10:00:00"
  }
}
```

### 3. 获取转码结果
```http
GET /file/transcoding/result/{taskId}

Response:
{
  "code": 200,
  "data": {
    "taskId": "abc123def456",
    "status": "completed",
    "outputFileId": 123457,
    "outputFileUrl": "https://cdn.example.com/files/output.mp4"
  }
}
```

## 性能优化建议

### 1. 资源隔离
- 转码worker独立部署
- 使用容器化（Docker）
- 资源限制（CPU、内存）

### 2. 并发控制
- 限制同时转码任务数
- 任务优先级队列
- 超时控制

### 3. 缓存策略
- 相同参数的转码结果缓存
- 使用CDN加速分发

### 4. 监控告警
- 任务队列长度监控
- 转码失败率监控
- 资源使用率监控

## 总结

转码系统采用灵活的策略模式设计，支持：

1. **多种转码类型**: 文档、视频、音频、图片
2. **多种实现方案**: 开源工具、云服务、自建集群
3. **同步/异步处理**: 适应不同场景需求
4. **易于扩展**: 新增转码类型只需实现接口

**建议使用路径**:
- 开发阶段: LibreOffice + FFmpeg
- 生产环境: 云服务（阿里云/腾讯云）+ 自建异步队列
