# OpenTelemetry Micronaut Microservices

这是一个基于Micronaut框架的微服务项目，实现了JWT认证、OpenTelemetry可观测性、熔断器和重试逻辑。

## 服务架构

项目包含三个微服务：

1. **Token Service** (端口 8081) - JWT令牌发行和管理服务
2. **Data-Query-Service** (端口 8080) - RESTful数据查询服务
3. **Client Service** (端口 8082) - HTTP客户端服务，调用Data-Query-Service

## 技术栈

- **框架**: Micronaut 4.7.0
- **JDK**: Java 17
- **认证**: JWT (HS256)
- **可观测性**: OpenTelemetry (Jaeger, Prometheus)
- **API文档**: OpenAPI/Swagger
- **容错**: Circuit Breaker, Retry with Exponential Backoff
- **容器化**: Docker, Docker Compose

## 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| Token Service | 8081 | JWT令牌发行和管理 |
| Data-Query-Service | 8080 | RESTful API服务 |
| Client Service | 8082 | HTTP客户端服务 |
| Jaeger UI | 16686 | 分布式追踪UI |
| Prometheus | 9090 | 指标收集 |

## 快速开始

### 使用Docker Compose启动所有服务

```bash
docker-compose up -d
```

### 单独启动服务

#### Token Service

```bash
cd token-service
mvn clean package
java -jar target/token-service-*.jar
```

#### Data-Query-Service

```bash
cd data-query-service
mvn clean package
java -jar target/data-query-service-*.jar
```

#### Client Service

```bash
cd data-client-service
mvn clean package
java -jar target/data-client-service-*.jar
```

## API端点

### Token Service

#### 获取令牌
```bash
POST http://localhost:8081/oauth/token
Content-Type: application/json

{
  "clientId": "data-client-service",
  "clientSecret": "secret123",
  "grantType": "client_credentials",
  "scope": "read write"
}
```

#### 获取公钥
```bash
GET http://localhost:8081/api/v1/public-key
```

#### 验证令牌
```bash
GET http://localhost:8081/api/v1/validate?token=<your_token>
```

#### 撤销令牌
```bash
POST http://localhost:8081/api/v1/revoke
Content-Type: application/json

{
  "token": "<your_token>"
}
```

### Data-Query-Service

#### 获取所有用户
```bash
GET http://localhost:8080/api/v1/users
Authorization: Bearer <your_token>
```

#### 获取用户详情
```bash
GET http://localhost:8080/api/v1/users/1
Authorization: Bearer <your_token>
```

#### 获取所有产品
```bash
GET http://localhost:8080/api/v1/products?page=0&size=10
Authorization: Bearer <your_token>
```

#### 获取产品详情
```bash
GET http://localhost:8080/api/v1/products/1
Authorization: Bearer <your_token>
```

### Client Service

#### 通过客户端获取所有用户（带熔断器）
```bash
GET http://localhost:8082/api/v1/users?page=0&size=10
```

#### 通过客户端获取用户详情（带熔断器）
```bash
GET http://localhost:8082/api/v1/users/1
```

#### 通过客户端获取所有产品（带熔断器）
```bash
GET http://localhost:8082/api/v1/products?page=0&size=10
```

#### 通过客户端获取产品详情（带熔断器）
```bash
GET http://localhost:8082/api/v1/products/1
```

## OpenAPI文档

- **Token Service**: http://localhost:8081/swagger-ui
- **Data-Query-Service**: http://localhost:8080/swagger-ui
- **Client Service**: http://localhost:8082/swagger-ui

## 可观测性

### Jaeger分布式追踪

访问 Jaeger UI: http://localhost:16686

### Prometheus指标

访问 Prometheus: http://localhost:9090

### 健康检查

- **Token Service**: http://localhost:8081/health
- **Data-Query-Service**: http://localhost:8080/health
- **Client Service**: http://localhost:8082/health

## 功能特性

### Token Service
- JWT令牌发行（HS256算法）
- 回调注册和令牌投递
- 公钥端点
- 令牌撤销/黑名单
- 令牌验证

### Data-Query-Service
- RESTful API
- JWT认证保护
- Mock数据返回
- 分页查询
- 请求验证

### Client Service
- HTTP客户端调用Data-Query-Service
- 自动令牌获取和刷新
- 熔断器模式
- 重试逻辑（指数退避）
- 降级处理

## 配置说明

### 环境变量

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| JWT_SECRET | pleaseChangeThisSecretForASecretKeyForJWTTokenGeneration | JWT签名密钥 |
| JWT_EXPIRATION | 3600 | 令牌过期时间（秒） |
| TOKEN_SERVICE_URL | http://localhost:8081 | Token Service URL |
| DATA_QUERY_SERVICE_URL | http://localhost:8080 | Data-Query-Service URL |
| CLIENT_ID | data-client-service | 客户端ID |
| CLIENT_SECRET | secret123 | 客户端密钥 |

## 项目结构

```
OpenTelemetry/
├── token-service/          # Token Service
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   ├── pom.xml
│   └── Dockerfile
├── data-query-service/     # Data-Query-Service
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   ├── pom.xml
│   └── Dockerfile
├── data-client-service/    # Client Service
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   ├── pom.xml
│   └── Dockerfile
├── docker-compose.yml
├── prometheus.yml
└── README.md
```

## 测试

运行测试：

```bash
# Token Service
cd token-service
mvn test

# Data-Query-Service
cd data-query-service
mvn test

# Client Service
cd data-client-service
mvn test
```

## 构建Docker镜像

```bash
# 构建所有服务
docker-compose build

# 单独构建
docker build -t token-service:latest ./token-service
docker build -t data-query-service:latest ./data-query-service
docker build -t data-client-service:latest ./data-client-service
```

## 停止服务

```bash
docker-compose down
```

## 许可证

Apache 2.0
