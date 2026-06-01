
> ⚠️ **技术栈配置统一在 `archetype-config.yml` 中**
>
> 本文档中涉及的所有命令、路径、工具选择以 `archetype-config.yml` 为准：
> - 构建/测试命令: `build_tool.commands.*`
> - 测试目录和文件模式: `test.layers.*`
> - 质量工具: `quality.*`
> - 目录映射: `directories.layers.*`
> - 代码解析规则: `spec_xchecker.code_parsing.*`
>
> **不要硬编码具体语言或命令**，换语言时只改 YAML 即可。

1|---
2|name: commit
3|description: "代码提交与 MR 创建技能 - 自动生成语义化 commit message、创建符合规范的 GitLab Merge Request、验证飞书工作项关联。当用户提到 Git 提交、commit、push、推送代码、创建 MR、创建 PR、合并请求、或需要提交代码、推送代码、创建 MR/PR 时，必须使用此技能。支持交互式（对话）和非交互式（参数）两种模式。"
4|version: "2.1"
5|author: DevTools Team <devtools@minieye.com>
6|license: Proprietary
7|tags:
8|  - devops
9|  - git
10|  - gitlab
11|  - feishu
12|---
13|
14|# Commit Skill
15|
16|企业级 Git 提交和 MR 创建技能，支持自动生成语义化 commit message、创建符合规范的 MR、验证飞书工作项关联。
17|
18|## 核心职责
19|
20|1. **代码提交**：自动生成语义化 commit message，基于代码变更分析
21|2. **推送代码**：推送到远端仓库，支持自动创建远端分支
22|3. **MR 创建**：创建符合 Conventional Commits 规范的 GitLab MR
23|4. **工作项验证**：验证飞书项目工作项关联，确保可追溯性
24|5. **内审检查**：提交前检查敏感信息、测试覆盖、文档更新
25|
26|---
27|
28|## 🚨 铁律：MR 创建前必须同步目标分支（强制执行）
29|
30|**⚠️ 创建 MR 之前，必须先同步目标分支的代码！！！没有 conflict 才能创建 MR！**
31|
32|**标准工作流程**（强制执行）：
33|
34|```bash
35|# Step 1: 同步目标分支（必须！）
36|git fetch origin <target-branch>
37|
38|# Step 2: Rebase 到最新目标分支（必须！）
39|git rebase origin/<target-branch>
40|
41|# Step 3: 检查是否有冲突
42|git status
43|
44|# Step 4: 只有在没有冲突的情况下，才能：
45|#    - 推送代码
46|#    - 创建 MR
47|```
48|
49|**检查清单**（强制执行）：
50|- [ ] 目标分支已同步（`git fetch origin <target-branch>`）
51|- [ ] 已 rebase 到最新目标分支（`git rebase origin/<target-branch>`）
52|- [ ] 无冲突（`git status` 显示 clean）
53|- [ ] 变更列表正确（`git diff --name-only origin/<target-branch>...HEAD`）
54|- [ ] 提交历史正确（`git log --oneline origin/<target-branch>..HEAD`）
55|
56|**只有以上全部满足，才能创建 MR！**
57|
58|**Why**: 避免重复提交、错误的 diff、不必要的冲突
59|
60|**典型后果**:
61|- MR 包含已合并到目标分支的变更
62|- 需要强制 rebase 修复，增加工作量
63|- 影响代码审查效率和 CI/CD 流程
64|
65|---
66|
67|## ⚠️ 环境配置管理
68|
69|**设计原则**:
70|- ✅ **SKILL.md**: 存储可移植的代码提交流程和最佳实践
71|- ✅ **.env.skill**: 存储项目特定凭证信息（GitLab PAT、飞书 auth_key 等）
72|- ❌ **禁止**: 将具体凭证值硬编码在 SKILL.md 中
73|
74|**凭证信息配置**:
75|
76|项目根目录的 `.env.skill` 文件会保留此技能需要的凭证信息：
77|
78|```bash
79|# GitLab Personal Access Token
80|GITLAB_PAT=glpat-xxxxxxxxxxxxx
81|
82|# 飞书认证密钥
83|FEISHU_AUTH_KEY=cli_xxxxxxxxxxxxxx
84|
85|# Git Host 实例地址（可选，默认从 git remote 自动检测）
86|# 支持 GitLab、GitHub、Gitea 等平台
87|# GITLAB_URL=https://gitlab.example.com
88|# GITHUB_TOKEN=***
89|
90|# 飞书项目 ID（可选，用于工作项验证）
91|FEISHU_PROJECT_ID=xxxxxxxxxxxxx
92|```
93|
94|**部署前验证**:
95|1. 读取项目根目录 `.env.skill` 文件获取凭证信息
96|2. 验证 GitLab PAT 是否有效
97|3. 验证飞书 auth_key 是否可用
98|4. 确认凭证有足够的权限（git write、feishu project read）
99|
100|**注意**: `.env.skill` 已在 `.gitignore` 中，不应提交到 Git 仓库
101|
102|---
103|
104|## 核心功能
105|
106|### 1. Commit 自动生成
107|
108|基于代码变更自动生成语义化 commit message：
109|
110|- 分析文件列表 + code diff + 工作上下文
111|- 输出 ≤1/2 屏幕的精简描述
112|- **不强制 Conventional Commits 格式**（避免 semantic_release 版本爆炸）
113|- 支持动态上下文注入（加载项目最近 commits 匹配风格）
114|
115|### 2. Push 远端
116|
117|支持推送到远端仓库：
118|
119|- 默认使用 `origin/<branch>` 命名
120|- 远端分支不存在时自动创建
121|- 自动检测默认目标分支（master/main）
122|
123|### 3. MR 创建
124|
125|创建符合规范的 GitLab MR：
126|
127|- **MR Title**：遵循 Conventional Commits 格式 `type(scope): summary<30字符`
128|- **MR Description**：包含飞书工作项元数据（YAML front matter 格式）
129|- 支持交互式（可选对话）和非交互式（一次性参数）两种模式
130|
131|### 4. 飞书工作项验证
132|
133|验证飞书项目工作项关联：
134|
135|- 检查 YAML front matter 格式
136|- 使用 lark-cli 验证工作项 ID 是否存在
137|- **⚠️ 强制要求：必须要求用户提供关联的"飞书工作项 uid"**
138|- 非交互模式：验证失败直接报错退出
139|- 交互模式：提示补正，阻止创建
140|
141|**重要说明**：
142|- 飞书工作项 uid 是飞书项目管理系统中的唯一标识符
143|- 创建 MR 时必须关联有效的飞书工作项 uid（数字格式，如 6934756567）
144|- 不得使用占位符、默认值或跳过此验证步骤
145|- 如果用户无法提供有效的 uid，应阻止创建 MR 并说明原因
146|
147|### 5. 内审规则检查
148|
149|执行代码提交前的内审检查：
150|
151|- 敏感信息检查（密钥、密码、IP 地址等）
152|- 测试覆盖检查（测试文件是否存在或变更）
153|- 文档更新检查
154|
155|---
156|
157|## Commit 规则
158|
159|**📌 详细提交约定**：参见 [references/commit-conventions.md](references/commit-conventions.md)
160|
161|**自由格式**：不强制 Conventional Commits，commit message 可以自由编写。
162|
163|**自动生成**：基于代码变更自动生成语义化 message，参考项目最近的 commits 风格。
164|
165|---
166|
167|## MR 规范
168|
169|**📌 MR 模板**：
170|- [Feature MR 模板](references/mr-templates/feature.md)
171|- [Bugfix MR 模板](references/mr-templates/bugfix.md)
172|- [Hotfix MR 模板](references/mr-templates/hotfix.md)
173|- [Refactoring MR 模板](references/mr-templates/refactoring.md)
174|- [Documentation MR 模板](references/mr-templates/docs.md)
175|- [CI/CD MR 模板](references/mr-templates/ci-cd.md)
176|
177|### MR Title 格式
178|
179|```
180|type(scope): summary
181|```
182|
183|- **type**: feat, fix, docs, style, refactor, perf, test, build, ci, chore, revert
184|- **scope**: 影响范围（可选）
185|- **summary**: 简短描述（<30 字符）
186|
187|### MR Description 格式
188|
189|```yaml
190|---
191|feishu.task: 6723548458
192|---
193|
194|## 功能说明
195|（功能描述）
196|
197|## 变更内容
198|- 变更点 1
199|- 变更点 2
200|
201|## 测试
202|- [ ] 单元测试通过
203|- [ ] SIT 测试通过
204|```
205|
206|**重要规则**：
207|1. YAML front matter 必须在描述开头
208|2. 字段名固定为 `feishu.task`
209|3. 冒号后必须有空格
210|4. 工作项 ID 是数字格式（如 6723548458）
211|
212|---
213|
214|## 工作模式
215|
216|**📌 详细使用示例**：
217|- 交互式模式：参见 [examples/interactive-usage.md](examples/interactive-usage.md)
218|- 非交互式模式：参见 [examples/non-interactive-usage.md](examples/non-interactive-usage.md)
219|
220|### 交互式模式
221|
222|交互式模式提供对话式流程，每个步骤都有默认值，用户可以确认或修改：
223|
224|1. 确认目标分支（自动检测 master/main）
225|2. 检查 PAT 配置
226|3. **输入飞书工作项 uid（⚠️ 强制要求）**
227|4. **验证飞书工作项 uid 有效性**
228|5. 生成/确认 MR Title
229|6. 确认创建 MR
230|
231|**⚠️ 重要：飞书工作项 uid 是必填项**
232|- 不得使用占位符、默认值或跳过此步骤
233|- 如果用户无法提供有效的 uid，应阻止创建 MR 并说明原因
234|- 必须使用 lark-cli 或手动方式验证 uid 存在性
235|
236|### 非交互式模式
237|
238|非交互式模式通过命令行参数一次性提供所有信息，适合自动化脚本：
239|
240|```bash
241|# 自动生成 commit
242|git add <files>
243|commit.sh --auto-generate --non-interactive
244|
245|# 推送代码
246|push.sh --branch feature/login --remote origin --create-if-not-exists --non-interactive
247|
248|# 创建 MR
249|mr.sh create \
250|  --source-branch feat/login \
251|  --target-branch master \
252|  --mr-title "feat(auth): implement login" \
253|  --feishu-task 6723548458 \
254|  --non-interactive
255|
256|# ⚠️ 注意：--feishu-task 参数是必填项
257|# 如果用户未提供，必须明确提示并提供有效的飞书工作项 uid
258|```
259|
260|---
261|
262|## 错误处理
263|
264|### 退出码系统
265|
266|使用 3 位数字退出码（000-999）：
267|
268|- **0xx**: 成功/信息
269|- **2xx**: 依赖/配置错误
270|- **3xx**: Git 操作错误
271|- **4xx**: GitLab API 错误
272|- **5xx**: 飞书集成错误
273|- **6xx**: 内审检查错误
274|- **9xx**: 未知错误
275|
276|### 交互式 vs 非交互式错误处理
277|
278|| 场景 | 交互式 | 非交互式 |
279||------|--------|----------|
280|| PAT 未配置 | 提示配置指导，等待输入 | 输出错误，退出码 203 |
281|| 工作项验证失败 | 提示补正，阻止创建 | 输出错误，退出码 500 |
282|| GitLab API 错误 | 显示错误，询问重试 | 输出错误，退出码 402 |
283|| 内审检查失败 | 警告，询问是否继续 | 输出警告，继续创建 |
284|
285|---
286|
287|## 配置
288|
289|**📌 配置文件示例**：参见 [config/code-committer.yaml](config/code-committer.yaml)
290|
291|### PAT 配置优先级
292|
293|1. 环境变量 `GITLAB_PAT`
294|2. 配置文件 `~/.config/commit/config.yaml`
295|3. 项目配置 `.claude/commit.yaml`
296|4. 运行时交互输入（仅交互式模式）
297|
298|### 配置文件示例
299|
300|```yaml
301|gitlab:
302|  pat: ""  # 留空表示使用环境变量
303|  auto_detect_url: true
304|
305|feishu:
306|  require_validation: true
307|  auth_identity: "user"
308|
309|audit:
310|  check_sensitive_data: true
311|  check_test_coverage: false
312|  check_documentation: true
313|
314|commit:
315|  max_length_lines: 12
316|  include_file_summary: true
317|  load_recent_commits: true
318|  check_claude_md: true
319|```
320|
321|---
322|
323|## 依赖要求
324|
325|**📌 可用脚本**：
326|- `scripts/commit-generator.sh`：自动生成 commit message
327|- `scripts/feishu-validator.sh`：验证飞书工作项
328|- `scripts/audit-checker.sh`：执行审计检查
329|- `scripts/error-codes.sh`：错误码定义
330|- `scripts/gitlab-api.sh`：GitLab API 操作
331|- `scripts/remote-handler.sh`：远端仓库操作
332|- 其他辅助脚本位于 `scripts/` 目录
333|
334|**系统依赖**：
335|- **git**: 版本控制（必需）
336|- **lark-cli**: 飞书 CLI（飞书功能必需）
337|  - 安装: `npm install -g lark-cli`
338|  - GitHub: https://github.com/larksuite/cli
339|
340|---
341|
342|## 使用示例
343|
344|### 场景 1: 自动生成 commit 并推送
345|
346|```bash
347|# 暂存变更
348|git add src/login.py tests/test_login.py
349|
350|# 自动生成 commit message
351|commit.sh --auto-generate
352|
353|# 推送到远端
354|push.sh --create-if-not-exists
355|```
356|
357|### 场景 2: 创建关联飞书工作项的 MR
358|
359|```bash
360|# 非交互式
361|mr.sh create \
362|  --source-branch feat/login \
363|  --target-branch master \
364|  --mr-title "feat(auth): implement JWT authentication" \
365|  --feishu-task 6723548458 \
366|  --non-interactive
367|
368|# 交互式
369|mr.sh create
370|# (按提示输入)
371|```
372|
373|### 场景 3: 一键提交+推送+创建 MR
374|
375|```bash
376|push-and-mr.sh \
377|  --feishu-task 6723548458 \
378|  --non-interactive
379|```
380|
381|---
382|
383|## 内审检查
384|
385|**📌 详细审计检查清单**：参见 [references/audit-checklist.md](references/audit-checklist.md)
386|
387|### 敏感信息检查
388|
389|检测以下模式并提供警告：
390|
391|- `password.*=`
392|- `api[_-]?key.*=`
393|- `secret.*=`
394|- IP 地址格式
395|
396|### 测试覆盖检查
397|
398|检查变更中是否包含测试文件：
399|
400|- 检查文件名是否包含 `test` 或 `spec`
401|- 不实际运行测试，仅检查文件是否存在或变更
402|
403|### 文档更新检查
404|
405|检查相关文档是否需要更新：
406|
407|- README.md
408|- API 文档
409|- 变更日志
410|
411|---
412|
413|## 最佳实践
414|
415|**📌 详细故障排除指南**: 参见 [references/mr-creation-troubleshooting.md](references/mr-creation-troubleshooting.md)
416|
417|### MR 创建成功的关键技巧
418|
419|**核心技巧**：
420|1. **⚠️ 同步目标分支**（铁律）：`git fetch origin <target-branch>` && `git rebase origin/<target-branch>`
421|2. **PAT 验证**: 先验证 PAT 权限，避免 401 错误
422|3. **HTTP 状态码捕获**: 使用 `curl -w` 捕获状态码，判断创建结果
423|4. **MR 信息查询**: 通过 API 查询 MR 的 iid 和 web_url
424|5. **飞书任务 ID 验证**: 检查 YAML front matter 格式（`feishu.task: <uid>`）
425|6. **完整工作流程**: 同步目标分支 → PAT 验证 → 创建 MR → 查询信息 → 验证结果
426|
427|### 常见错误
428|
429|**🚨 MR 包含重复的文件变更**（铁律违反）:
430|- **原因**: 创建 MR 前未同步目标分支，导致包含已合并的变更
431|- **解决**: `git fetch origin <target-branch>` && `git rebase origin/<target-branch>` --force-with-lease
432|- **预防**: 遵循铁律，创建 MR 前必须先同步并 rebase 目标分支
433|
434|**HTTP 401 Unauthorized**:
435|- 原因: PAT 无效或权限不足
436|- 解决: 验证 PAT 是否有 `api` 权限
437|
438|**glab 命令无返回结果**:
439|- 原因: PAT 未配置或版本过旧
440|- 解决: 使用 `glab --web` 或切换到 curl API
441|
442|**飞书任务 ID 验证失败**:
443|- 原因: YAML 格式错误或 ID 不存在
444|- 解决: 检查 `feishu.task: <uid>` 格式，冒号后有空格
445|
446|---
447|
448|## 🤝 Agent Team 协作
449|
450|Commit 工作需要与其他角色密切协作。本 SKILL 专注于代码提交和 MR 创建技术实践，相关职责请参考：
451|
452|- **开发工作流** → [dev SKILL](../dev/SKILL.md)
453|  - 代码开发、测试规范
454|  - Commit message 格式要求
455|  - 参考：`docs/scrum/story/` 中的 Story 文档
456|
457|- **测试验证** → [qa SKILL](../qa/SKILL.md)
458|  - 测试覆盖要求
459|  - 提交前测试验证
460|  - SIT/UAT 测试标准
461|
462|- **项目管理** → [pm SKILL](../pm/SKILL.md)
463|  - 飞书工作项管理
464|  - Story/Epic 状态跟踪
465|  - 参考：飞书项目管理系统
466|
467|- **DevOps 流程** → [devops SKILL](../devops/SKILL.md)
468|  - CI/CD Pipeline 集成
469|  - MR/Merge Request 审批流程
470|  - 部署环境配置
471|
472|---
473|
474|## 参考
475|
476|- [设计文档](/docs/design/skills/commit-v1.0.0.md)
477|- [飞书文档：GitLab MR - 飞书工作项关联功能使用指南](https://minieye.feishu.cn/wiki/StSfwrGDoibiIOklf8ccOFumnHd)
478|- [Conventional Commits](https://www.conventionalcommits.org/)
479|- [GitLab API Documentation: Merge Requests](https://docs.gitlab.com/ee/api/merge_requests.html)
480|- [GitLab API Documentation: Users](https://docs.gitlab.com/ee/api/users.html)
481|
482|---
483|
484|**版本**: v2.1
485|**维护者**: DevTools Team
486|