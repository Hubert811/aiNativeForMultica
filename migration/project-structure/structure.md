# 项目骨架

## Java SpringBoot 标准结构

```
project/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/app/
│   │   │       ├── controller/     # REST API controllers
│   │   │       ├── service/        # 业务逻辑层
│   │   │       ├── repository/     # 数据访问层
│   │   │       ├── model/          # 实体/DTO
│   │   │       ├── config/         # Spring 配置
│   │   │       └── Application.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-{env}.yml
│   └── test/
│       ├── java/
│       │   └── com/example/app/
│       │       ├── controller/
│       │       │   └── *ApiTest.java      # API 层测试
│       │       ├── service/
│       │       │   └── *Test.java          # 单元测试
│       │       └── integration/
│       │           ├── *SitTest.java       # SIT 集成测试
│       │           └── *UatTest.java       # UAT 验收测试
│       └── resources/
├── docs/                           # 项目文档（内容数据源）
│   ├── design/
│   ├── scrum/
│   └── guides/
├── migration/                      # 迁移参考（历史存档）
└── pom.xml
```

## 与 Multica 的对应关系

| 目录/文件 | 对应 Issue 字段 |
|---|---|
| `docs/scrum/epic/epic-*.md` | Epic Issue description 引用 |
| `docs/scrum/story/story-*.md` | Story Issue description 引用 |
| `docs/design/*.md` | 被 Story Issue 描述中引用 |
| Issue 状态/指派/优先级 | Multica 原生字段，不在文件中存储 |

## 测试命名约定

| 模式 | 用途 |
|---|---|
| `*Test.java` | 单元测试 (JUnit 5) |
| `*ApiTest.java` | API 层测试 (RestAssured/MockMvc) |
| `*SitTest.java` | 集成测试 (TestContainers) |
| `*UatTest.java` | 验收测试 (Cucumber) |
