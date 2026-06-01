# AC 测试策略矩阵

## Java 测试工具链映射

| 测试层级 | Java 工具 | 用途 | Maven 命令 |
|---------|----------|------|-----------|
| **UT** | JUnit 5 + Mockito | 函数/方法正确性，Mock 外部依赖 | `mvn test` |
| **API** | RestAssured / MockMvc | RESTful 接口契约验证 | `mvn test -Dtest=**/api/**` |
| **SIT** | TestContainers + @SpringBootTest | K8s + DB + Service 集成验证 | `mvn test -Dtest=**/sit/**` |
| **UAT** | Cucumber + Selenium | 端到端业务场景验收 | `mvn test -Dtest=**/uat/**` |

## 各层详细说明

### UT（单元测试）

- **框架**：JUnit 5（`@Test`, `@BeforeEach`, `@DisplayName`）
- **Mock**：Mockito（`@Mock`, `@InjectMocks`, `when().thenReturn()`）
- **断言**：AssertJ（`assertThat().isEqualTo()`）或 JUnit 5 `Assertions`
- **覆盖率**：JaCoCo（Statement Coverage ≥ 80%）
- **目录**：`src/test/java/.../unit/`
- **原则**：不依赖外部资源，每个测试独立可重复

### API（接口测试）

- **框架**：RestAssured（外部 HTTP 调用）或 MockMvc（Spring 内部模拟）
- **验证**：HTTP 状态码、响应体 Schema、Header、错误码
- **目录**：`src/test/java/.../api/`
- **原则**：基于 OpenAPI/Swagger 元数据设计用例，覆盖所有端点和参数组合

### SIT（集成测试）

- **框架**：TestContainers（真实 DB/Redis/MQ 容器）+ `@SpringBootTest`
- **验证**：多服务交互、数据库事务、消息队列消费
- **目录**：`src/test/java/.../sit/`
- **原则**：使用真实依赖容器，不 Mock 外部服务；测试数据用 `test-*` 前缀命名

### UAT（用户验收测试）

- **框架**：Cucumber（Gherkin 语法编写业务场景）+ Selenium（浏览器自动化）
- **验证**：端到端用户流程，业务验收标准
- **目录**：`src/test/java/.../uat/`
- **原则**：基于 PRD 用户故事编写 .feature 文件，非技术人员可读

## TDD 门禁规则

| 结果 | SIT 通过率 | 行动 |
|---|---|---|
| 🟢 绿灯 | 100% | 标记 AC 通过，Issue 改为 `done` |
| 🟡 黄灯 | 70-99% | 退回开发者，Issue 改回 `in_progress`，记录失败详情 |
| 🔴 红灯 | <70% | 退回开发者，Issue 改回 `in_progress`，记录失败详情到 Issue comment |

**发布要求**：SIT 必须 100% 通过才能发布。黄灯只是退回门槛，不是发布门槛。

## 测试命令参考（Maven）

```bash
# 运行所有 UT
mvn test

# 运行指定层的测试
mvn test -Dtest="**/api/**"       # API 测试
mvn test -Dtest="**/sit/**"       # SIT 测试
mvn test -Dtest="**/uat/**"       # UAT 测试

# 运行指定测试类
mvn test -Dtest=UserServiceTest

# 生成覆盖率报告
mvn test jacoco:report

# 跳过测试（仅构建）
mvn package -DskipTests
```
