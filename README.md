# AI Demo

一个基于 **Vue 3 + Spring Cloud** 的全栈示例项目。

---

## 技术栈

### 前端
- Vue 3 + Vite + TypeScript
- Vue Router（路由管理）
- Pinia（状态管理）
- Axios（HTTP 请求）

### 后端
- Spring Boot 2.7.18
- Spring Cloud 2021.0.8
- Spring Cloud Gateway（网关）
- Maven（多模块构建）

---

## 项目结构

```
ai-demo/
├── frontend/                          # 前端 Vue 项目
│   ├── package.json                   # 依赖配置
│   ├── vite.config.ts                 # Vite 配置（含 API 代理到 8080）
│   ├── tsconfig.json
│   ├── index.html
│   └── src/
│       ├── main.ts                    # 入口文件
│       ├── App.vue                    # 根组件（含导航栏）
│       ├── router/index.ts            # 路由配置（首页 / 关于）
│       ├── stores/counter.ts          # Pinia 状态管理示例
│       ├── views/
│       │   ├── Home.vue               # 首页（含后端接口测试按钮）
│       │   └── About.vue              # 关于页
│       └── components/
│           └── HelloWorld.vue
│
└── backend/                           # 后端 Spring Cloud 项目
    ├── pom.xml                        # 父 POM
    ├── gateway/                       # 网关模块（端口 8080）
    │   ├── pom.xml
    │   └── src/main/
    │       ├── java/com/example/gateway/GatewayApplication.java
    │       └── resources/application.yml   # 路由：/api/** → user-service:8081
    └── user-service/                  # 业务服务模块（端口 8081）
        ├── pom.xml
        └── src/main/
            ├── java/com/example/userservice/
            │   ├── UserServiceApplication.java
            │   └── controller/HelloController.java  # /hello, /hello/time
            └── resources/application.yml
```

---

## 运行状态

| 服务 | 端口 | 说明 |
|------|------|------|
| 前端（Vite） | 5173 | Vue 3 开发服务器 |
| 网关（Gateway） | 8080 | Spring Cloud Gateway，统一入口 |
| 用户服务（User Service） | 8081 | 业务服务，提供 /hello 接口 |

---

## 启动命令

### 前端

```powershell
cd frontend
npm run dev
```

前端启动后访问：http://localhost:5173

### 后端 - 用户服务

```powershell
cd backend/user-service
mvn spring-boot:run
```

### 后端 - 网关

```powershell
cd backend/gateway
mvn spring-boot:run
```

---

## 快速开始

1. **启动用户服务**（端口 8081）
2. **启动网关**（端口 8080）
3. **启动前端**（端口 5173）
4. 浏览器访问 http://localhost:5173
5. 在首页点击【获取后端消息】按钮测试前后端联调

---

## 接口说明

| 接口 | 说明 |
|------|------|
| `GET http://localhost:8081/hello` | 用户服务直接访问 |
| `GET http://localhost:8080/api/hello` | 通过网关代理访问 |
| `GET http://localhost:8080/api/hello/time` | 获取当前时间 |

前端通过 Vite 代理将 `/api/**` 请求转发到 Gateway（8080），Gateway 剥离 `/api` 前缀后转发到 User Service（8081）。

---

## 环境要求

- Node.js >= 16
- Java 11
- Maven >= 3.6
