# NeusoftAI - 东软智能运维咨询系统

一个基于 AI 的 IT 运维故障诊断与咨询平台，集成设备管理、知识库、公告系统等功能，帮助运维团队高效处理故障和积累运维经验。

## 技术栈

### 前端
- **Vue 3** + Composition API
- **Vite 6** 构建工具
- **Vue Router 4** 路由管理
- **Pinia** 状态管理
- **Axios** HTTP 请求

### 后端
- **Spring Boot 3.5** + Java 21
- **MyBatis-Plus** ORM 框架
- **MySQL** 数据库
- **JWT** 用户认证
- **Ollama** 本地 AI 模型（gemma4:e2b）

## 功能模块

| 模块 | 说明 |
|------|------|
| 故障咨询 | 向 AI 描述故障现象，获取智能诊断方案，支持多轮对话 |
| 咨询记录 | 查看历史咨询记录，支持点赞和方案优化 |
| 设备管理 | 管理 IT 设备资产（服务器、打印机、网络设备等） |
| 运维知识库 | 积累和查阅运维知识文章（Markdown 格式） |
| 系统公告 | 发布和查看运维通知、维护公告 |
| 用户系统 | 注册/登录，JWT 鉴权，角色权限（管理员/普通用户） |

## 项目结构

```
NeosoftAI/
├── NeusoftAI_frontend/          # 前端 Vue 项目
│   ├── src/
│   │   ├── api/                 # API 请求封装
│   │   ├── assets/              # 静态资源
│   │   ├── router/              # 路由配置
│   │   ├── stores/              # Pinia 状态管理
│   │   ├── utils/               # 工具函数
│   │   └── views/               # 页面组件
│   ├── package.json
│   └── vite.config.js
├── NeusoftAI_spring/            # 后端 Spring Boot 项目
│   ├── src/main/java/com/eric/  # Java 源码
│   ├── src/main/resources/      # 配置文件
│   ├── sql/                     # 数据库初始化脚本
│   └── pom.xml
└── README.md
```

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.8+
- MySQL 8.0+
- Node.js 18+
- [Ollama](https://ollama.com/)（本地 AI 推理）

### 1. 克隆项目

```bash
git clone https://github.com/<你的用户名>/NeusoftAI.git
cd NeusoftAI
```

### 2. 数据库初始化

```bash
mysql -u root -p < NeusoftAI_spring/sql/init.sql
```

### 3. 后端配置

复制配置模板并填写你的数据库密码：

```bash
cp NeusoftAI_spring/src/main/resources/application.yml.example NeusoftAI_spring/src/main/resources/application.yml
```

编辑 `application.yml`，修改以下配置：

```yaml
spring:
  datasource:
    username: root
    password: 你的数据库密码

ollama:
  base-url: http://localhost:11434
  model: gemma4:e2b
```

### 4. 启动后端

```bash
cd NeusoftAI_spring
mvn spring-boot:run
```

后端运行在 `http://localhost:8080`

### 5. 启动前端

```bash
cd NeusoftAI_frontend
npm install
npm run dev
```

前端运行在 `http://localhost:5173`

### 6. 启动 Ollama AI 服务

```bash
ollama pull gemma4:e2b
ollama serve
```

### 7. 测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 管理员 |
| geekeric | 123456 | 普通用户 |

## License

MIT
