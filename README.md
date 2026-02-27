# Nexus 后台管理系统

<p align="center">
  <img src="https://img.shields.io/badge/Java-25-orange" alt="Java Version"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.9-green" alt="Spring Boot Version"/>
  <img src="https://img.shields.io/badge/MyBatis--Plus-3.5.15-blue" alt="MyBatis-Plus Version"/>
  <img src="https://img.shields.io/badge/Sa--Token-1.44.0-red" alt="Sa-Token Version"/>
  <img src="https://img.shields.io/badge/License-MIT-yellow" alt="License"/>
</p>

## 📖 项目简介

Nexus 是一个基于 **Spring Boot 3.5** 的现代化后台管理系统，采用模块化架构设计，集成了用户认证、权限管理（RBAC）、系统管理等核心功能。项目使用最新的 Java 25 特性，结合主流的开发框架和工具，旨在提供一个高效、安全、可扩展的企业级后台解决方案。

## ✨ 主要特性

- 🚀 **高性能**: 基于 Spring Boot 3.5.9，支持 Java 25 最新特性
- 🔐 **安全认证**: 集成 Sa-Token 实现无状态 Token 认证与权限控制
- 📊 **权限管理**: 完整的 RBAC（基于角色的访问控制）权限体系
- 🗄️ **多数据源**: 支持动态数据源切换，轻松应对多数据库场景
- 📝 **API 文档**: 集成 Knife4j，自动生成美观的 API 接口文档
- 🔄 **缓存支持**: 支持 Redis + Caffeine 多级缓存
- 🛠️ **模块化设计**: 清晰的模块划分，便于维护和扩展

## 🏗️ 项目架构

```
nexus/
├── nexus-parent          # 父模块，统一管理依赖版本
├── nexus-common          # 公共模块（常量、工具类）
├── nexus-infra           # 基础设施模块（数据库、缓存、Redis）
├── nexus-core            # 核心模块（通用配置、异常处理、基础类）
├── nexus-security        # 安全模块（Sa-Token 认证与授权）
├── nexus-module-rbac     # RBAC 权限管理模块
├── nexus-module-system   # 系统管理模块
└── nexus-boot            # 启动模块（应用入口）
```

### 模块依赖关系

```
                    ┌─────────────────┐
                    │   nexus-boot    │  (启动入口)
                    └────────┬────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
        ▼                    ▼                    ▼
┌───────────────┐  ┌─────────────────┐  ┌─────────────────┐
│ nexus-module- │  │  nexus-module-  │  │  nexus-security │
│     rbac      │  │     system      │  │                 │
└───────┬───────┘  └────────┬────────┘  └────────┬────────┘
        │                   │                    │
        └───────────────────┼────────────────────┘
                            │
                            ▼
                    ┌───────────────┐
                    │  nexus-core   │  (核心功能)
                    └───────┬───────┘
                            │
            ┌───────────────┴───────────────┐
            │                               │
            ▼                               ▼
    ┌───────────────┐               ┌───────────────┐
    │  nexus-infra  │               │ nexus-common  │
    └───────────────┘               └───────────────┘
```

## 📦 模块说明

### nexus-common（公共模块）
- 通用常量定义
- 工具类封装
- 基础依赖：Hutool、Lombok

### nexus-infra（基础设施模块）
- MyBatis-Plus 数据库操作
- 动态数据源配置
- Redis 缓存配置
- Caffeine 本地缓存

### nexus-core（核心模块）
- 统一响应结果封装（Result）
- 全局异常处理
- 通用 Controller/Service/Entity 基类
- 参数校验
- Knife4j API 文档配置

### nexus-security（安全模块）
- Sa-Token 认证配置
- 登录/登出处理
- 权限拦截器
- 安全上下文管理

### nexus-module-rbac（权限管理模块）
- 用户管理（SysUser）
- 角色管理（SysRole）
- 权限管理（SysPermission）
- 用户角色关联（SysUserRole）
- 角色权限关联（SysRolePermission）

### nexus-module-system（系统管理模块）
- 系统配置管理
- 文件上传管理
- 系统工具

### nexus-boot（启动模块）
- Spring Boot 应用入口
- 配置文件管理
- 模块集成

## 🛠️ 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 25 | 开发语言 |
| Spring Boot | 3.5.9 | 应用框架 |
| MyBatis-Plus | 3.5.15 | ORM 框架 |
| Dynamic Datasource | 4.3.1 | 动态数据源 |
| Sa-Token | 1.44.0 | 权限认证框架 |
| Knife4j | 4.5.0 | API 文档工具 |
| Hutool | 5.8.43 | Java 工具类库 |
| Caffeine | 3.2.3 | 本地缓存 |
| Redis | - | 分布式缓存 |
| MySQL | 8.x | 关系型数据库 |
| Lombok | - | 简化代码 |

## 🚀 快速开始

### 环境要求

- **JDK**: 25+
- **Maven**: 3.8+
- **MySQL**: 8.0+
- **Redis**: 6.0+

### 1. 克隆项目

```bash
git clone https://github.com/your-username/nexus.git
cd nexus
```

### 2. 配置数据库

创建数据库：

```sql
CREATE DATABASE nexus DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

### 3. 修改配置文件

编辑 `nexus-boot/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    dynamic:
      datasource:
        local-mysql:
          url: jdbc:mysql://127.0.0.1:3306/nexus?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
          username: your_username
          password: your_password
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
```

### 4. 编译打包

```bash
# Windows
mvnw.cmd clean install

# Linux / macOS
./mvnw clean install
```

### 5. 启动应用

```bash
# Windows
mvnw.cmd spring-boot:run -pl nexus-boot

# 或直接运行 jar 包
java -jar nexus-boot/target/nexus-boot-1.0.0-SNAPSHOT.jar
```

### 6. 访问应用

- **应用地址**: http://localhost:9981
- **API 文档**: http://localhost:9981/doc.html
- **Swagger UI**: http://localhost:9981/swagger-ui.html

## 📋 配置说明

### 应用配置

```yaml
server:
  port: 9981                    # 服务端口

nexus:
  file-upload-path: D:/upload/  # 文件上传路径
  file-access-url: http://localhost:8380/  # 文件访问地址
  aes-key: xxx                  # AES 加密密钥
```

### Sa-Token 配置

```yaml
sa-token:
  token-name: Authorization     # Token 名称
  timeout: 86400               # Token 有效期（秒）
  is-concurrent: true          # 是否允许同一账号并发登录
  is-share: true               # 多人登录共享一个 Token
  token-style: random-64       # Token 风格
  token-prefix: Bearer         # Token 前缀
  is-read-cookie: false        # 是否从 Cookie 读取 Token
```

### MyBatis-Plus 配置

```yaml
mybatis-plus:
  mapper-locations: classpath*:mapper/**/*.xml  # Mapper XML 位置
  configuration:
    map-underscore-to-camel-case: true          # 驼峰命名转换
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # SQL 日志输出
  global-config:
    db-config:
      id-type: ASSIGN_ID       # ID 生成策略（雪花算法）
```

## 📚 API 文档

项目集成了 Knife4j，启动后访问 http://localhost:9981/doc.html 即可查看完整的 API 文档。

API 文档分组：
- **NEXUS-BOOT**: 主应用接口
- **RBAC**: 权限管理接口
- **SYSTEM**: 系统管理接口

## 🔐 权限说明

系统采用 RBAC（基于角色的访问控制）模型：

```
用户 (User) ──M:N── 角色 (Role) ──M:N── 权限 (Permission)
```

### 权限校验方式

```java
// 1. 注解方式
@SaCheckLogin                    // 登录校验
@SaCheckRole("admin")           // 角色校验
@SaCheckPermission("user:add")  // 权限校验

// 2. 代码方式
StpUtil.checkLogin();           // 登录校验
StpUtil.checkRole("admin");     // 角色校验
StpUtil.checkPermission("user:add");  // 权限校验
```

## 📁 目录结构

```
nexus/
├── mvnw                          # Maven Wrapper (Linux)
├── mvnw.cmd                      # Maven Wrapper (Windows)
├── pom.xml                       # 父 POM 文件
│
├── nexus-common/                 # 公共模块
│   └── src/main/java/cn/dong/nexus/common/
│       ├── constants/            # 常量定义
│       └── util/                 # 工具类
│
├── nexus-infra/                  # 基础设施模块
│   └── src/main/java/cn/dong/nexus/infra/
│       └── config/               # 基础设施配置
│
├── nexus-core/                   # 核心模块
│   └── src/main/java/cn/dong/nexus/core/
│       ├── annotations/          # 自定义注解
│       ├── api/                  # API 响应封装
│       ├── base/                 # 基类
│       ├── config/               # 配置类
│       ├── exception/            # 异常定义
│       ├── handler/              # 处理器
│       ├── resmapping/           # 资源映射
│       ├── security/             # 安全相关
│       ├── util/                 # 工具类
│       └── valid/                # 参数校验
│
├── nexus-security/               # 安全模块
│   └── src/main/java/cn/dong/nexus/security/
│       ├── config/               # 安全配置
│       ├── context/              # 安全上下文
│       ├── exception/            # 安全异常
│       └── service/              # 安全服务
│
├── nexus-module-rbac/            # RBAC 模块
│   └── src/main/java/cn/dong/nexus/modules/rbac/
│       ├── constant/             # 常量
│       ├── controller/           # 控制器
│       ├── domain/               # 实体/DTO/VO
│       ├── mapper/               # Mapper 接口
│       └── service/              # 服务层
│
├── nexus-module-system/          # 系统管理模块
│   └── src/main/java/cn/dong/nexus/modules/system/
│       ├── controller/           # 控制器
│       ├── domain/               # 实体/DTO/VO
│       ├── mapper/               # Mapper 接口
│       ├── service/              # 服务层
│       └── util/                 # 工具类
│
└── nexus-boot/                   # 启动模块
    └── src/main/
        ├── java/cn/dong/nexus/
        │   ├── config/           # 应用配置
        │   ├── controller/       # 控制器
        │   └── NexusApplication.java  # 启动类
        └── resources/
            ├── application.yml   # 配置文件
            ├── logback-spring.xml # 日志配置
            └── static/           # 静态资源
```

## 🔧 开发指南

### 新增模块

1. 在根目录创建新模块目录
2. 添加 `pom.xml` 并配置父模块依赖
3. 在父 `pom.xml` 的 `<modules>` 中添加新模块
4. 在 `nexus-boot` 的 `pom.xml` 中添加依赖

### 代码规范

- 遵循阿里巴巴 Java 开发手册
- 使用 Lombok 简化代码
- 所有 API 接口添加 Swagger 注解
- 统一使用 `Result<T>` 封装响应结果

### 日志规范

```java
@Slf4j
public class ExampleService {
    public void doSomething() {
        log.info("操作描述: {}", param);
        log.error("错误信息: ", exception);
    }
}
```

## 🐛 常见问题

### 1. Maven 编译失败

```bash
# 清理并重新编译
mvnw.cmd clean install -DskipTests
```

### 2. Lombok 不生效

- 确保 IDE 安装了 Lombok 插件
- 开启 Annotation Processing

### 3. 找不到 Mapper XML

检查 `application.yml` 中的配置：
```yaml
mybatis-plus:
  mapper-locations: classpath*:mapper/**/*.xml
```

### 4. Redis 连接失败

确保 Redis 服务已启动，并检查配置：
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

## 📝 更新日志

### v1.0.0-SNAPSHOT (开发中)

- 🎉 初始化项目架构
- ✅ 完成 RBAC 权限管理模块
- ✅ 集成 Sa-Token 认证框架
- ✅ 集成 Knife4j API 文档
- ✅ 集成 MyBatis-Plus
- ✅ 支持动态数据源
- ✅ 支持 Redis 缓存

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 📄 开源协议

本项目基于 [MIT License](LICENSE) 开源。

## 📧 联系方式

如有问题或建议，请提交 [Issue](https://github.com/your-username/nexus/issues)。

---

<p align="center">
  ⭐ 如果这个项目对你有帮助，请给一个 Star 支持一下！
</p>
