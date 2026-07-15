# TimeKeeper ⏱️

一个简洁的时间管理应用，支持正计时、倒计时和番茄钟三种模式，帮助记录专注时长、管理分类、追踪目标进度。

## ✨ 功能特性

- **三种计时模式** — 正计时 / 倒计时 / 番茄钟（工作+休息循环）
- **圆形 SVG 表盘** — 实时显示进度，三种模式视觉区分
- **分类管理** — 自定义分类名称和颜色，统计各分类时长占比
- **历史记录** — 按日 / 周 / 月查看计时记录
- **数据统计** — 每日趋势图、分类占比饼图
- **日历视图** — 月度概览，每天专注时长热力显示
- **目标管理** — 设置每日 / 每周目标，实时进度追踪
- **偏好设置** — 番茄钟工作/休息时长、倒计时默认值自定义
- **响应式 UI** — 桌面端侧边栏 + 移动端底部导航
- **JWT 认证** — 无状态 token 认证，支持多用户

## 🏗️ 项目结构

```
timekeeper/
├── timekeeper/              # 前端（Vue 3 + Pinia + Element Plus）
└── timekeeper-server/       # 后端（Spring Boot + JPA + MySQL + JWT）
```

## 🛠️ 技术栈

**前端**
- Vue 3 + Vite
- Pinia 状态管理
- Vue Router
- Element Plus + Element Plus Icons
- ECharts 图表

**后端**
- Spring Boot 3.4
- Spring Data JPA + Hibernate
- Spring Security + JWT (jjwt)
- MySQL 8.4
- Knife4j (OpenAPI 文档)

## 🚀 快速开始

### 1. 启动后端

```bash
# 创建数据库
CREATE DATABASE timekeeper DEFAULT CHARACTER SET utf8mb4;

# 修改 timekeeper-server/src/main/resources/application.yml
# 配置数据库地址、用户名、密码

cd timekeeper-server
./mvnw spring-boot:run
```

后端启动在 http://localhost:9090，API 文档在 http://localhost:9090/doc.html

### 2. 启动前端

```bash
cd timekeeper
npm install
npm run dev
```

访问 http://localhost:5173，注册账号后即可使用。

## 📂 核心目录说明

```
timekeeper/src/
├── api/           # HTTP 接口封装（按模块拆分）
├── stores/        # Pinia 状态管理
├── views/         # 页面组件
├── components/    # 通用组件（TimerCircle 等）
├── layouts/       # 布局组件（MainLayout）
└── router/        # 路由配置

timekeeper-server/src/main/java/com/timekeeper/
├── controller/    # REST 控制器
├── service/       # 业务逻辑
├── entity/        # JPA 实体
├── repository/    # Spring Data JPA 仓库
├── dto/           # 请求/响应 DTO
├── config/        # Security / CORS / OpenAPI 配置
└── security/      # JWT 认证过滤器
```

## 📄 License

MIT
