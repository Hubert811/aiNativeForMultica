1|---
2|name: "dev"
3|description: "开发工作流程指导 - 编码、测试、代码质量、MR/PR 创建和 CI/CD。用于开发任务、编码、功能实现、Bug 修复、单元测试、代码覆盖率、代码审查、CI/CD 流水线和 Git 操作。"
4|version: "5.1"
5|---
6|
7|# Development Workflow
8|
9|## Story-Driven Development（铁律）
10|
11|**没有 Story，不写代码。**
12|
13|Developer 三不做：
14|1. **不见 Story 不开工**：必须存在 `{project_docs}/scrum/story/story-*.md`，状态为 TODO/IN_PROGRESS
15|2. **不见 AC 不实现**：Story 必须有验收标准，否则退回 Scrum Master
16|3. **不被指派不接受**：不接受自我指派
17|
18|例外（无需 Story）：用户直接指令、紧急生产修复、探索性调查
19|
20|**说明**:
21|- `{project_docs}`: 项目文档目录（通常为 docs/）
22|
23|---
24|
25|## 核心职责
26|
27|1. **代码风格**：遵循 KISS、DRY、SOLID 原则；接口与实现分离，逻辑与配置分离
28|2. **功能开发**：实现新功能、修复 Bug、优化性能
29|3. **单元测试**：编写和维护单元测试，确保**方法/分支覆盖率** ≥ `{coverage_threshold}`
30|4. **代码质量**：遵循代码规范，进行代码审查
31|5. **集成测试**：使用项目 SIT 测试框架验证功能
32|6. **文档维护**：反馈设计文档、API 文档、运维文档
33|
34|**说明**:
35|- `{coverage_threshold}`: **方法/分支覆盖率**阈值（默认 70%）
36|- **覆盖率指标说明**：
37|  - **方法覆盖率**：确保每个方法都被调用
38|  - **分支覆盖率**：确保每个 if/switch 分支（true/false）都被执行
39|  - 选择原因：行覆盖率太简单，条件覆盖率太苛刻，方法/分支覆盖率是适度指标
40|
41|---
42|
43|## 代码风格
44|
45|### 命名规范（⚠️ 重要）
46|
**强制标准：参见 `archetype-config.yml` → `language.naming`**

项目命名规范由 `archetype-config.yml` 统一配置：
- 文件后缀: `language.naming.file_suffix`
- 标识符: `language.naming.identifier_case`（Java: camelCase, Go: snake_case）
- 类名: `language.naming.class_case`（PascalCase）
- 常量: `language.naming.constant_case`（UPPER_SNAKE_CASE）
- 包名: `language.naming.package_case`（lowercase）

不要硬编码命名规范，以 YAML 为准。
50|
51|#### 文件命名规则
#### 文件命名规则
| 类型 | ✅ 正确 | ❌ 错误 |
|------|---------|---------|
| **源代码** | 参见 `archetype-config.yml` → `language.naming` | — |
| **配置文件** | `config_dev.yaml`, `database_config.yaml` | `config-dev.yaml`, `database-config.yaml` |
| **Shell 脚本** | `start_dev.sh`, `init_db.sh` | `start-dev.sh`, `init-db.sh` |
| **目录命名** | 参见 `archetype-config.yml` → `directories.layers` | — |
| **API 定义** | `{PROJECT_NAME}_api` | `{PROJECT_NAME}-api` |
| **构建产物** | 参见 `archetype-config.yml` → `build_tool.artifact` | — |
#### 例外情况
62|#### 例外情况
63|
64|| 类型 | 允许格式 | 理由 |
65||------|----------|------|
66|| **Go Modules** | `{PROJECT_NAME}` | Go 社区标准，所有 Go 官方和第三方模块都使用连字符 |
67|| **Go 包名** | 单个单词（`main`, `config`, `handler`） | Go 语言规范要求包名使用简短的单个小写单词 |
68|| **通用配置文件** | `Makefile`, `Dockerfile`, `.gitlab-ci.yml`, `docker-compose.yml`, `go.mod` | 开发工具的标准命名，修改会导致工具无法识别 |
69|| **文档文件** | `epic-1-scaffolding.md`, `story-1-01-design.md` | 文档文件使用连字符更便于阅读 |
70|
71|#### 代码检查清单
72|
73|在创建或重命名文件时，请按照以下清单检查：
74|- [ ] 文件名使用小写字母
75|- [ ] 多个单词使用下划线 `_` 连接
76|- [ ] 不使用连字符 `-`（除非属于例外情况）
77|- [ ] 不使用驼峰命名 `CamelCase` 或 `PascalCase`
78|- [ ] 不使用空格或特殊字符
79|
80|#### 重命名操作指南
81|
82|当发现不符合规范的文件时，按以下步骤重命名：
83|
84|**1. 使用 git mv 重命名（保留历史）**
85|```bash
86|# 示例：重命名配置文件
87|git mv etc/config-dev.yaml etc/config_dev.yaml
88|
89|# 示例：重命名 Go 源文件
90|git mv {PROJECT_NAME}.go {PROJECT_NAME}.go
91|
92|# 示例：重命名目录
93|git mv sandbox/dev-pod-informer sandbox/dev_pod_informer
94|```
95|
96|**2. 更新所有引用**
97|重命名文件后，必须更新所有引用该文件的地方：
98|- **Makefile**: 更新文件路径引用
99|- **配置文件**: 更新 include 引用
100|- **Go 代码**: 更新 import 路径
101|- **Shell 脚本**: 更新文件路径
102|- **文档**: 更新文件路径说明
103|
104|**3. 验证编译和测试**
105|```bash
106|# 重新编译
107|make build
108|
109|# 运行测试
110|make test
111|```
112|
113|### 其他代码风格原则
114|
115|- **KISS / DRY / SOLID**：追求简单、可维护、可扩展的实现
116|- **接口与实现分离**：保持模块职责单一、接口清晰，实现与调用解耦
117|- **模块化**：函数职责单一，避免冗长或重复代码
118|- **注释原则**：注释放在核心业务和复杂逻辑处，保持必要但不过度，注重代码自解释性
119|- **分层处理**：业务流程和外部依赖分层（如控制器、服务、存储分离）
120|- **格式统一**：使用项目配置的 lint / formatter 工具
121|
122|---
123|
124|## 容器化开发规范（⚠️ 关键）
125|
126|**黄金规则**：每次更新代码后，启动服务前必须重新构建容器镜像
127|
128|容器使用的是编译产物（二进制/字节码等），**不重新构建则代码更改不会生效**。
129|
130|```bash
131|# ❌ 错误：只重启服务
132|docker compose restart {service_name}
133|
134|# ✅ 正确：重新构建并启动
135|docker compose up -d --build {service_name}
136|```
137|
138|**验证方法**：检查容器内编译产物的时间戳是否为最近时间。
139|
140|---
141|
142|## 测试规范
143|
144|### 单元测试（UT）
145|
#### 测试命令

**优先使用 `archetype-config.yml` 中的配置：**

| 配置路径 | 用途 |
|----------|------|
| `build_tool.commands.test` | 运行单元测试 |
| `build_tool.commands.coverage` | 生成覆盖率报告 |
| `test.layers.ut.run_command` | UT 层专用命令 |
| `test.coverage_targets.ut` | UT 覆盖率目标 % |
| `test.coverage_targets.logic_layer` | Service/Logic 层最低覆盖率 |

> 历史多语言参考表（保留供查阅，但实际以 YAML 为准）：
> - Go: `go test ./...` | `go test -coverprofile=coverage.out` | mockery
> - Python: `pytest` | `pytest --cov=. --cov-branch` | unittest.mock
> - Java: `mvn test` | `mvn test jacoco:report` | Mockito
> - JS/TS: `npm test` | `npm run test:coverage` | jest.mock
> - Rust: `cargo test` | `cargo tarpaulin --branch` | mockito
163|
164|#### 执行步骤
165|
166|```bash
167|# 1. 识别项目语言（查看 go.mod, package.json, pom.xml, Cargo.toml 等）
168|# 2. 从上表选择对应命令
169|# 3. 运行测试
170|{test_cmd}
171|
172|# 生成覆盖率报告
173|{coverage_cmd}
174|```
175|
176|**说明**:
177|- 如果项目有 Makefile 或 package.json 中定义的 test 脚本，**优先使用项目自定义命令**
178|- 覆盖率报告通常输出到 `coverage.out`、`.coverage`、`htmlcov/` 等目录
179|
180|**覆盖率要求**：
181|- 核心组件：**方法/分支覆盖率** ≥ 80%
182|- 整体代码：**方法/分支覆盖率** ≥ `{coverage_threshold}`（默认 70%）
183|
184|### 集成测试（SIT）
185|
186|**黄金规则**：使用项目 SIT 测试框架，禁止自己编写测试脚本
187|
188|#### 常见 SIT 框架
189|
190|| 框架 | `{sit_run_cmd}` | 报告位置 |
191||------|-----------------|----------|
192|| **自定义脚本** | `./tests/sit/run_sit_tests.sh --auto` | `test_reports/sit_report-*.md` |
193|| **pytest集成** | `pytest tests/integration/` | `test_reports/integration_report.html` |
194|| **Maven Verify** | `mvn verify` | `target/site/jacoco/index.html` |
195|
196|```bash
197|# 使用项目 SIT 框架验证
198|{sit_run_cmd}
199|```
200|
201|**说明**:
202|- 测试报告输出到 `test_reports/`
203|
204|### Mock 测试策略
205|
206|**核心原则：Spec → Scenario → Mock Data 三层对齐**
207|
208|基于设计规范（Spec）定义测试场景（Scenario），构建对齐的 Mock 数据，确保单元测试准确体现设计意图。
209|
210|**关键要求**：
211|- **Spec → Scenario 对齐**：每个 scenario 必须回溯到具体的 spec 条款
212|- **Scenario → Mock Data 对齐**：mock data 必须完全满足 scenario 的前置条件
213|- **可追溯性**：测试代码注释标注来源 spec 的章节号
214|
215|**测试代码注释规范**（使用 AAA 模式）：
216|```go
217|// Spec: {design_doc} §{section}
218|// Scenario: {scenario_description}
219|func TestXXX(t *testing.T) {
220|    // Arrange: 基于 spec 构建场景
221|    // Act: 执行被测试的逻辑
222|    // Assert: 验证 spec 要求
223|}
224|```
225|
226|**接口抽象原则**：
227|- 将具体类型改为接口，使用依赖注入传入 mock 对象
228|- 避免在业务逻辑中硬依赖具体实现
229|
230|**说明**:
231|- `{design_doc}`: 设计文档名称
232|- `{section}`: 章节编号
233|- Mock 工具根据项目语言选择（见上表）
234|
235|---
236|
237|## 代码质量
238|
### 代码质量工具

**优先使用 `archetype-config.yml` 中的配置：**

| 配置路径 | 用途 |
|----------|------|
| `quality.formatter.command` | 格式化命令 |
| `quality.linter[].command` | lint 命令 |
| `quality.gates.format_pass` | MR 前格式化门禁 |
| `quality.gates.lint_pass` | MR 前 lint 门禁 |

> 历史参考表（实际以 YAML 为准）：
> - Go: `gofmt` | `golangci-lint` | `go build`
> - Python: `black` | `flake8`/`pylint` | `python -m build`
> - Java: `google-java-format` | `checkstyle` | `mvn package`
> - JS: `prettier` | `eslint` | `npm run build`
> - Rust: `cargo fmt` | `cargo clippy` | `cargo build`
248|
249|### 提交前检查清单
250|
251|- [ ] 代码已格式化（`{fmt_cmd}`）
252|- [ ] 通过静态分析（`{lint_cmd}`）
253|- [ ] 单元测试通过（`{test_cmd}`）
254|- [ ] **方法/分支覆盖率**达标（≥ `{coverage_threshold}`）
255|- [ ] SIT 测试验证通过（`{sit_run_cmd}`）
256|- [ ] 代码已审查（如果有 PR/MR）
257|- [ ] 文档已更新（如果需要）
258|
259|**说明**:
260|- 从上表选择对应命令
261|- 如果项目有 Makefile 或 package.json 中定义了 fmt/lint 脚本，**优先使用项目自定义命令**
262|
263|---
264|
265|## 代码提交规范
266|
267|**Commit Message 格式**：
268|```
269|<type>(<scope>): <subject>
270|
271|<body>
272|
273|<footer>
274|```
275|
276|**类型**：`feat`, `fix`, `refactor`, `docs`, `test`, `chore`
277|
278|---
279|
280|## Git 推送规则（⚠️ 重要）
281|
282|**默认行为**：
283|- ✅ **只做**：`git add` + `git commit`（提交到本地分支）
284|- ❌ **不做**：`git push`（不推送远程）
285|
286|**`{main_branch}` 分支保护规则**：
287|- **方式 1**：Review 授权流程（修改代码 → commit → 等待 review → 用户手动 push）
288|- **方式 2**：MR/PR 流程（创建 feature 分支 → push → 创建 MR/PR → 合并）
289|
290|**绝对禁止**：未经明确授权直接 `git push origin {main_branch}`
291|
292|**YOLO 模式例外条件**（必须同时满足）：
293|1. 用户**明确授权**（明确说"YOLO模式"或"可以推送"）
294|2. 只能推送 **`{main_branch}` 之外的分支**（feature/*, dev, bugfix/* 等）
295|3. 推送前必须显示 commit 信息供审查
296|4. **永远不要自动推送 `{main_branch}`**（即使 YOLO 模式也不行）
297|
298|**说明**:
299|- `{main_branch}`: 主分支名（通常为 master 或 main）
300|
301|---
302|
303|## Git Worktree 工作流
304|
305|**核心概念**：主干开发模式，支持并行开发多个任务
306|
307|> **💡 辅助脚本**：本 SKILL 附带了 Git Workflow 辅助脚本 `git-workflow.skill.sh`，简化 worktree 操作。
308|>
309|> **安装**（一次性）：
310|> ```bash
311|> # 添加到 ~/.bashrc 或 ~/.zshrc
312|> echo 'source $(git rev-parse --show-toplevel)/.claude/skills/dev/scripts/git-workflow.skill.sh' >> ~/.bashrc
313|> source ~/.bashrc
314|> ```
315|>
316|> **快速使用**：
317|> ```bash
318|> # 创建功能分支 worktree
319|> git-workflow.feature.start story-06-01 "pod handler"
320|>
321|> # 在 worktree 中同步上游 master/main
322|> git-workflow.feature.sync
323|>
324|> # 推送并创建 MR
325|> git-workflow.feature.submit
326|>
327|> # MR 合并后清理 worktree
328|> git-workflow.feature.finish story-06-01
329|>
330|> # 列出所有 worktree 及其状态
331|> git-workflow.worktree.list
332|> ```
333|>
334|> **优势**：1 条命令替代 6-8 步 Git 操作，自动检测项目配置，彩色输出提示
335|
336|### 分支命名规范
337|
338|- 功能分支：`feat/{task_id}-{summary}`
339|- Bug 修复：`fix/{bug_id}-{description}`
340|- 紧急修复：`hotfix/{short_description}`
341|- 重构：`refactor/{short_description}`
342|
343|### 标准流程
344|
345|```bash
346|# 1. 创建 worktree
347|cd {project_root}
348|git worktree add ../{worktree_dir}/{branch_name} -b {branch_name}
349|
350|# 2. 在 worktree 中开发
351|cd ../{worktree_dir}/{branch_name}
352|# ... 开发代码 ...
353|git add .
354|git commit -m "<type>(<scope>): <subject>"
355|
356|# 3. 推送到远程（非 {main_branch} 分支）
357|git push origin {branch_name}
358|
359|# 4. 创建 MR/PR（根据项目使用的平台）
360|
361|# 5. MR/PR 合并后，清理 worktree
362|cd {project_root}
363|git worktree remove ../{worktree_dir}/{branch_name}
364|git branch -d {branch_name}
365|```
366|
367|**说明**:
368|- `{project_root}`: 项目根目录
369|- `{worktree_dir}`: worktree 存放目录（常见值：`../{project_name}-worktrees`, `../worktrees`）
370|
371|### Worktree 管理
372|
373|- 同时维护 2-3 个工作树，不超过 5 个
374|- 定期执行 `git worktree prune` 清理孤立 worktree
375|
376|---
377|
378|## 开发工作流
379|
380|### Bug 修复流程
381|
382|1. **问题定位**：阅读测试报告，查看代码，确定根因
383|2. **修复实现**：编写修复代码，添加单元测试
384|3. **单元测试**：运行 `{test_cmd}`
385|4. **SIT 验证**：运行 `{sit_run_cmd}`
386|5. **提交代码**：撰写清晰的 Commit Message
387|6. **生成报告**：如果修改影响核心功能，运行完整测试
388|
389|### 新功能开发流程
390|
391|1. **需求分析**：阅读 PRD 和 Story 文档
392|2. **设计实现**：参考设计文档，设计数据结构、接口、流程
393|3. **编码实现**：遵循代码规范，编写单元测试
394|4. **单元测试**：运行 `{test_cmd}`，确保覆盖率达标
395|5. **SIT 验证**：运行 `{sit_run_cmd}`
396|6. **文档更新**：更新设计文档、API 文档、CLAUDE.md
397|7. **代码审查**：提交 MR/PR，根据反馈修改
398|8. **合并发布**：合并到主分支，生成测试报告
399|
400|---
401|
402|## MR/PR 创建与 CI 调试
403|
404|### 核心工作模式
405|
406|```
407|MR/PR 创建 → Pipeline 监听 → 状态判断 → 成功/失败处理
408|                    ↓                      ↓
409|               持续监听               Yolo Mode 修复
410|                                       ↓
411|                                  重新推送 → 继续监听
412|                                           ↓
413|                                       闭环验证
414|```
415|
416|### 阶段一：MR/PR 创建
417|
418|#### 前置检查
419|
420|- [ ] 功能完整性：所有 AC 已满足
421|- [ ] 代码质量：单元测试**方法/分支覆盖率**达标
422|- [ ] 测试验证：UT / SIT / UAT 全部通过
423|- [ ] 代码规范：符合项目编码规范
424|- [ ] 文档更新：设计文档、API 文档已更新
425|- [ ] Git 规范：Commit Message 包含任务 ID
426|- [ ] 分支状态：feature 分支提交已整理
427|
428|#### MR/PR Description 模板（40-100 行）
429|
430|```markdown
431|## 功能说明
432|[简述功能目标 + 核心改进点 3-5 条]
433|
434|## 变更说明
435|### 新增文件 (N 个)
436|- `path/file` (X lines) - 简短说明
437|
438|### 修改文件 (M 个)
439|- `path/file` - 修改说明
440|
441|### 代码统计
442|- 新增: +X lines
443|- 删除: -Y lines
444|- 测试覆盖率: XX%
445|
446|## 测试验证结果
447|| 场景 | 预期 | 实际 | 状态 |
448||------|------|------|------|
449|| 场景1 | XXX | XXX | ✅ |
450|
451|## 相关文档
452|- Story: `{project_docs}/scrum/story/story-*.md`
453|- Design: `{project_docs}/design/{layer}_design_v{version}.md`
454|
455|## 验收标准
456|- [x] AC1
457|- [x] AC2
458|```
459|
460|#### 创建步骤
461|
462|**使用 CLI 工具（推荐）**：
463|```bash
464|# GitLab
465|glab mr create --title "<type>(<scope>): <subject>" \
466|  --target-branch {main_branch} --source-branch {branch_name}
467|
468|# GitHub
469|gh pr create --title "<type>(<scope>): <subject>" \
470|  --base {main_branch} --head {branch_name}
471|```
472|
473|**使用 REST API（备选）**：
474|- 使用项目对应平台的 REST API 创建 MR/PR
475|- 注意 Shell 变量作用域：变量设置和使用必须在同一命令行
476|
477|### 阶段二：Pipeline 监控
478|
479|**状态值**：`pending` → `running` → `success` / `failed` / `canceled` / `skipped`
480|
481|**监控方式**：
482|- 使用平台 CLI（`glab`, `gh`）或 REST API 查询 Pipeline 状态
483|- 获取失败 Job 列表和日志
484|- 日志过大时保存到文件分析
485|
486|**CI 失败类型与处理**：
487|
488|| 失败类型 | 负责方 | 典型场景 |
489||---------|--------|----------|
490|| 编译错误 | Dev | 语法错误、类型不匹配、依赖缺失 |
491|| 测试失败 | Dev/QA | 断言失败、集成测试失败 |
492|| 超时 | Dev | 死锁、网络延迟、性能问题 |
493|| Runner 异常 | Ops | Runner 不可用、资源不足 |
494|
495|### 阶段三：Yolo Mode 快速修复
496|
497|**触发条件**：Pipeline 失败
498|
499|**5 步修复流程**：
500|1. **同步主分支**：`git fetch origin {main_branch}` + `git rebase origin/{main_branch}`
501|