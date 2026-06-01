# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Purpose

This is a **prototype project (原型工程)** that demonstrates AI-native project architecture patterns. It is **NOT** a production application.

**Key Characteristics**:
- ✅ Reference template for architecture patterns
- ✅ Framework code (interface definitions, data models)
- ✅ Documentation of best practices
- ❌ No business logic implementations
- ❌ Not runnable as a production service

**Usage**: Copy this project structure as a starting point for new projects, then implement the interfaces defined in the repository layer.

## ⚠️ 技术栈配置

**本项目所有技术栈配置统一在 `archetype-config.yml` 中**。

- 构建/测试命令 → `build_tool.commands.*`
- 测试目录和文件模式 → `test.layers.*`
- 目录映射 → `directories.layers.*`
- 质量工具 → `quality.*`

不要硬编码语言或命令，以 `archetype-config.yml` 为准。

## Development Commands

### 构建和测试（参考 archetype-config.yml）

```bash
# 以下命令为 Java + Maven 示例，实际命令见 archetype-config.yml → build_tool.commands

# 运行单元测试
mvn test

# 格式化代码
mvn spotless:apply

# 运行 lint
mvn checkstyle:check

# 打包
mvn package

# 本地运行
mvn spring-boot:run

# 生成覆盖率报告
mvn jacoco:report
```

### 测试（参考 archetype-config.yml → test.layers）

```bash
# 以下命令为示例，实际命令和目录见 archetype-config.yml

# 运行 API 测试
mvn test -Dtest="*ApiTest"

# 运行 SIT 集成测试
mvn test -Dtest="*SitTest"

# 运行 UAT 验收测试
mvn test -Dtest="*UatTest"

# 生成 HTML 测试报告
mvn surefire-report:report
```

### Docker & Deployment

```bash
# Build Docker image
make docker

# Run local development environment
cd deploy/docker && docker-compose up -d

# Deploy to test environment (K8s)
./deploy/scripts/helm-upgrade.sh test snapshot-mr-30

# Deploy to production (K8s)
./deploy/scripts/helm-upgrade.sh prod V0.1-20260428153000-a1b2c3d
```

## Architecture Overview

### Layered Architecture

**分层映射见 `archetype-config.yml` → `directories.layers`**

通用分层原则：
```
HTTP Request
    ↓
Controller/Handler    — HTTP request/response handling
    ↓
Service/Logic         — Business logic
    ↓
Repository/DAO        — Data access abstraction (interface-based)
    ↓
Entity/Model          — Data models (JPA/GORM entities)
```

**Key Principles**:
- **Interface-based data access layer**: All data access goes through interfaces
- **Dependency injection**: Components receive dependencies through constructors
- **Separation of concerns**: Each layer has distinct responsibilities

### Technology Stack

**见 `archetype-config.yml` → `framework`**

默认参考：
- **Backend**: Java 17+ / Spring Boot / JPA(MyBatis) / PostgreSQL
- **Testing**: JUnit 5 + Mockito (UT), MockMvc/REST Assured (API), SpringBootTest (SIT)
- **DevOps**: Docker / Kubernetes / Helm Charts / GitLab CI

## Directory Structure

**目录映射见 `archetype-config.yml` → `directories`**

通用结构：
```
src/main/java/{base_package}/
├── controller/       # HTTP handlers (REST endpoints)
├── service/          # Business logic
├── repository/       # Data access layer interfaces
├── entity/           # Data models (JPA entities)
├── dto/              # Data transfer objects
├── config/           # Configuration classes
├── filter/           # HTTP middleware
└── util/             # Utility packages

src/test/java/
├── controller/       # API contract tests
├── service/          # Unit tests (with Mockito)
├── sit/              # System integration tests (@SpringBootTest)
└── uat/              # User acceptance tests (Cucumber)

docs/
├── design/           # Architecture design documents
├── guides/           # Usage guides
└── scrum/            # Project management docs

deploy/
├── docker/           # Docker Compose (local development)
└── k8s/              # Kubernetes Helm Charts

src/main/resources/
└── *.yml             # Configuration files (Spring profiles)
```

## Testing Strategy

**配置见 `archetype-config.yml` → `test`**

本项目使用**四层测试金字塔**：

| 层级 | 类型 | 位置（见 YAML `test.layers.*.dir`） | 目的 | 覆盖率目标 |
|------|------|---------|------|-----------|
| UT | 单元测试 | `src/test/java/` | 函数级逻辑正确性 | ≥ 50% |
| API | 契约测试 | `src/test/java/.../api/` | API 接口契约验证 | 100% |
| SIT | 集成测试 | `src/test/java/.../sit/` | 业务流程验证 | ≥ 90% |
| UAT | 验收测试 | `src/test/java/.../uat/` | 端到端用户场景 | ≥ 85% |

**Important**: 单元测试同包结构存放（`src/test/java/com/xxx/service/`），集成/SIT/UAT 测试在对应子目录下。

## Configuration Management

**配置见 `archetype-config.yml` → `directories.config`**

**Runtime Configuration**:
- `src/main/resources/application.yml` — 本地开发
- `src/main/resources/application-test.yml` — 测试环境
- `src/main/resources/application-prod.yml` — 生产环境

**Deployment Configuration**:
- `deploy/docker/docker-compose.yml` — 本地开发
- `deploy/k8s/helm/...` — K8s 部署

**Spring Profile 加载优先级**:
1. 命令行: `--spring.profiles.active=test`
2. 环境变量: `SPRING_PROFILES_ACTIVE=test`
3. 默认: `application.yml`

## Key Design Documents

When implementing features based on this prototype, reference these documents:

- `docs/design/service_layer_architecture_v4.2.md` - Service layer design
- `docs/design/cmdb_design_v4.0.md` - Data layer design
- `docs/design/api_design_v1.3.md` - API design
- `GUIDE.md` - Engineering practices (testing strategy, deployment pipeline)

## When Working with This Codebase

### For Learning Architecture
- Read interface definitions in the repository layer (见 `directories.layers.repository`)
- Study data models in the entity layer (见 `directories.layers.entity`)
- Review design documents in `docs/design/`

### For Creating New Projects
1. Copy this project structure
2. Implement Repository interfaces (见 `directories.layers.repository`)
3. Add controllers in `controller/` (见 `directories.layers.controller`)
4. Add business logic in `service/` (见 `directories.layers.service`)
5. Fill in configuration values in `src/main/resources/` (见 `directories.config`)
6. Write tests following the four-layer strategy (见 `test.layers`)

### Important Constraints

- **DO NOT** add business logic implementations to this prototype project
- **DO NOT** modify framework code without understanding the architecture
- **DO** use this as a reference for understanding AI-native project patterns
- **DO** copy the structure when creating new projects

## Team Skills

The `.claude/skills/` directory contains team skill definitions for various roles:
- `arch/` - Architecture skills
- `commit/` - Git commit and MR creation
- `dev/` - Development workflow
- `devops/` - DevOps operations
- `qa/` - Testing strategy
- `pm/` - Project management

Use these skills with the `/skill` command when working on specific tasks.

## Version History

- **v2.0** (2026-04-28): De-implementation refactor - removed all business logic, kept only framework
- **v1.0** (2026-02-04): Initial version
