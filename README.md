# Nutriscan-Server (食品营养智能体服务端)

这是一个基于 Spring Boot 3 构建的食品营养分析智能体后端服务。该项目结合了 **Coze AI 工作流** 进行食品识别与营养分析，并提供用户饮食管理、社区交流、营养健康数据统计等核心功能。

## 🌟 核心功能

- **用户管理**: 用户注册、登录、个人信息维护、体重变化记录 (Weight Log)。
- **AI 营养分析**: 
  - 基于 Coze API 进行食品图片识别与深度营养成分分析。
  - 用户每日卡路里摄入记录 (Calorie Log) 与健康数据统计 (User Statistics)。
- **健康社区**: 
  - 用户可以发布健康饮食动态 (Post)、参与评论 (Comment)。
  - 动态标签分类管理 (Tag) 及用户浏览历史记录 (ViewHistory)。
- **语音播报 (TTS)**: 集成火山引擎 (Volcengine) TTS，支持将营养分析结果转换为语音播报。
- **文件与对象存储**: 基于 MinIO 实现统一的图片和文件上传管理。
- **反馈系统**: 收集用户在使用过程中的意见与反馈。

## 🛠️ 技术栈

- **后端核心**: Java 17, Spring Boot 3.2.2
- **数据访问与持久层**: 
  - 关系型数据: MySQL 8.x, Spring Data JPA, HikariCP 连接池
  - 文档型数据: MongoDB, Spring Data MongoDB
- **对象存储**: MinIO (文件与图片上传)
- **第三方集成 (API)**:
  - 扣子 (Coze) AI Workflow API
  - 火山引擎 (Volcengine) TTS API
- **其他工具**: Maven, Lombok, Apache HttpClient5, Python (用于辅助 AI 测试脚本)

## 📋 环境要求

- JDK 17 及以上版本
- Maven 3.6+
- MySQL 8.0+
- MongoDB 4.4+
- MinIO Server
- （可选）Python 3.8+ 及 `cozepy` 依赖，用于运行 `src/main/python/` 下的独立测试脚本。

## 🚀 快速开始

### 1. 准备环境与数据库
- 创建 MySQL 数据库，命名为 `nutriscan`。
- 启动 MongoDB 实例，并确保已创建对应用户和数据库。
- 启动 MinIO 服务，创建一个名为 `nutriscan` 的 Bucket，并记录 Access Key 和 Secret Key。

### 2. 配置项目
编辑 `src/main/resources/application.properties` 文件，替换以下占位符为你本地或服务器的真实配置：

```properties
# MySQL 配置
spring.datasource.url=jdbc:mysql://<你的IP>:3306/nutriscan?...
spring.datasource.username=<你的MySQL用户名>
spring.datasource.password=<你的MySQL密码>

# MongoDB 配置
spring.data.mongodb.host=<你的MongoDB IP>
spring.data.mongodb.username=<用户名>
spring.data.mongodb.password=<密码>

# MinIO 配置
minio.endpoint=http://<你的MinIO IP>:9000
minio.access-key=<你的AccessKey>
minio.secret-key=<你的SecretKey>

# Coze AI API 配置
coze.api.key=<你的Coze API Key>
coze.workflow.id=<你的Coze Workflow ID>

# 火山引擎 TTS 配置
volcengine.tts.app-id=<你的App ID>
volcengine.tts.token=<你的Token>
```

### 3. 编译与运行
在项目根目录下执行以下命令来编译和启动项目：

```bash
# 编译项目 (跳过测试)
mvn clean install -DskipTests

# 运行 Spring Boot 应用
mvn spring-boot:run
```
服务默认运行在 `http://localhost:8080`。

## 📁 目录结构

```text
Nutriscan-Server/
├── src/main/java/com/luminous/nutriscan/
│   ├── config/       # 配置文件 (MinIO 配置, 应用配置等)
│   ├── controller/   # RESTful API 控制器 (用户, 社区, 营养分析等)
│   ├── dto/          # 数据传输对象 (请求和响应封装)
│   ├── model/        # 实体类 (JPA & MongoDB Entities)
│   ├── repository/   # 数据访问层接口 (Spring Data)
│   └── service/      # 核心业务逻辑实现
├── src/main/python/  # Python 辅助脚本 (Coze 工作流 CLI 测试工具)
├── src/main/resources/
│   └── application.properties # Spring Boot 核心配置文件
└── pom.xml           # Maven 项目依赖管理文件
```

## 📄 许可证 (License)
本项目遵循库中 `LICENSE` 文件声明的开源协议。
