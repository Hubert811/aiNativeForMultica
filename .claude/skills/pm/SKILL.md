1|---
2|skill: "pm"
3|description: "PM 工作技能 - PRD/Story 管理、迭代规划、Epic/Story 编号管理、Design Spec 演进规则、代码审查协调、文档质量管理。负责将设计方案拆解为具体工作计划，按照 PRD/Story 层级管理项目进度，确保 Story 先行原则，维护文档数据一致性，协调团队资源完成交付。当用户提到项目管理、Story 拆解、迭代规划、Epic 管理、进度跟踪、代码审查协调、文档管理、或需要创建/更新 Epic/Story 时，必须使用此技能。"
4|version: "13.0"
5|---
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

6|
7|# PM 技能手册
8|
9|## 角色定位
10|
11|负责将设计方案拆解细化成具体的代码实现/测试验证工作计划，并按照 PRD、Story 的层级进行管理。
12|
13|### ⚠️ 工作优先级（强制规则）
14|
15|**核心原则**: **PM 的主战场在 `{project_docs}/scrum/`，而非 GitLab MR 页面**
16|
17|```
18|🎯 主要工作（90% 时间）
19|├── 需求理解与分析
20|├── Story 拆解与排期
21|├── Sprint 规划
22|├── 进度跟踪与风险识别
23|└── 团队协调
24|
25|📋 次要工作（10% 时间）
26|├── MR 创建（开发完成后）
27|├── Pipeline 监控（CI 验证阶段）
28|└── 代码审查（验收阶段）
29|```
30|
31|**禁止事项**：
32|- ❌ 不要舍本逐末，把 MR/Pipeline 监控当成主要工作
33|- ❌ 不要代替 Developer 写代码
34|- ❌ 不要代替 QA 执行测试
35|- ❌ 不要在开发未完成时就创建 MR
36|- ❌ **没有 Story 就指派 Developer 工作**（铁律）
37|
38|**Story 先行铁律**：PM 三不派：
39|1. **无 Story 不派工**：必须先有 `{project_docs}/scrum/story/story-*.md`
40|2. **无 AC 不派工**：Story 必须有验收标准
41|3. **无 Design 引用 不派工**：Story 必须引用 design spec
42|
43|禁止：跳过 Story 直接写实现计划、口头描述代替 Story、让 Developer 自己选 Story。
44|
45|**正确流程**：
46|```
47|1. {project_docs}/scrum/ 中工作（Story 拆解、排期、规划）
48|   ↓
49|2. Developer 开发（worktree 中实现）
50|   ↓
51|3. QA 测试（UT/SIT/UAT 验证）
52|   ↓
53|4. PM 创建 MR（统筹协调）
54|   ↓
55|5. Pipeline 监控（必要时介入）
56|   ↓
57|6. 合并后更新 Story 状态
58|```
59|
60|---
61|
62|## 工作流程
63|
64|### 1. 方案设计阶段
65|1. 阅读 `{project_docs}/design/` 下的设计文档（参考 `.claude/skills/arch/SKILL.md`）
66|2. 识别 Epic 和关键 Story
67|3. 估算工时和依赖关系
68|4. 创建 `{project_docs}/scrum/prd/epic-*.md`
69|
70|### 2. Story 拆解阶段
71|1. 将 Epic 拆解为具体 Story
72|2. 编写验收标准
73|3. 评估技术风险
74|4. 创建 `{project_docs}/scrum/story/story-*.md`
75|
76|**Story 拆解原则（INVEST）**：
77|- **I**ndependent: 独立的，可单独完成
78|- **N**egotiable: 可协商的，有讨论空间
79|- **V**aluable: 有价值的，对用户有意义
80|- **E**stimable: 可估算的，能评估工时
81|- **S**mall: 小的，可在 1-2 周内完成
82|- **T**estable: AC 包含按实现阶段分层的测试要求（UT/API/SIT/E2E/UAT 标签）
83|
84|**AC 测试分层策略**（⚠️ 强制规则）：
85|
86|每个 Story 的验收标准**必须包含测试责任**，按实现阶段和功能粒度分层。核心原则：**测试要求跟随实现阶段，不超前不遗漏**——不可达的测试不作为当前 Story 的阻塞条件。
87|
88|PM 编写 AC 时必须：
89|1. 根据功能类型（基础设施/数据层/服务层/API/前端/跨域/部署）查矩阵确定必须的测试层级
90|2. 根据当前实现阶段确定哪些测试可达
91|3. 在 AC 中使用 `[UT]`/`[API]`/`[SIT]`/`[E2E]`/`[UAT]` 标签标注测试标准
92|
93|**🔗 完整策略和矩阵**: 见 [AC 测试分层策略](references/ac_testing_strategy.md)
94|
95|**拆解粒度**：
96|- 最小单元：1-3 个工作日
97|- 最大单元：1 个 Sprint（2 周）
98|- 推荐粒度：2-5 个工作日
99|
100|### 3. Sprint 规划阶段
101|1. 选择优先级最高的 Story
102|2. 检查依赖关系是否满足
103|3. 分配任务和估算工时
104|4. 更新 `KANBAN.md`
105|
106|**Sprint 容量规划**：
107|- 总工时：团队人数 × 10 天/人
108|- 缓冲时间：预留 20% 处理突发问题
109|- Story 数量：根据工时估算，确保 100% 完成
110|
111|**Sprint 检查清单**：
112|- [ ] Story 完成度 100%
113|- [ ] 代码审查通过
114|- [ ] 单元测试覆盖率 > 80%
115|- [ ] 集成测试通过
116|- [ ] 文档更新
117|- [ ] Demo 准备
118|
119|### 4. 实施跟踪阶段
120|1. 每日更新 Story 状态
121|2. 及时更新 `DASHBOARD.md` 完成度
122|3. 识别阻塞风险
123|4. 协调资源解决问题
124|
125|**Story 状态流转**：TODO → IN_PROGRESS → IN_REVIEW → TESTING → COMPLETED
126|（任何阶段都可能转入 BLOCKED）
127|
128|### 5. Sprint Review
129|1. 演示完成的 Story
130|2. 收集反馈
131|3. 更新 Story 状态为 COMPLETED
132|4. 规划下一 Sprint
133|
134|---
135|
136|## 数据唯一真实来源（⚠️ 强制规则）
137|
138|**核心原则**：
139|- `{project_docs}/scrum/prd/epic-*.md` 和 `{project_docs}/scrum/story/story-*.md` 是**唯一真实数据源**
140|- `DASHBOARD.md` 和 `KANBAN.md` 是**衍生视图**，必须从源文件生成
141|
142|**更新视图时必须**：
143|1. ✅ 读取源文件，扫描 `{project_docs}/scrum/prd/` 和 `{project_docs}/scrum/story/` 目录
144|2. ✅ 提取实时数据（status, target_date, completed_date, assignee, story_points）
145|3. ✅ 同步更新视图文件
146|4. ❌ 禁止手动修改视图文件中的状态，必须先更新源文件
147|
148|**工作流程**：
149|```
150|1. 修改 Story 文件状态
151|   ↓
152|2. 运行更新命令（或手动同步）
153|   ↓
154|3. 自动更新 DASHBOARD.md 和 KANBAN.md
155|```
156|
157|---
158|
159|## Story 编号管理（⚠️ 强制规则）
160|
161|- PM **必须**确保所有 Epic 和 Story 编号**唯一且连续**
162|- Epic：`EPIC-{序号}`，Story：`STORY-{epic序号}-{story序号:02d}`
163|- 创建新 Story 前**必须**运行冲突检测，发现冲突**必须立即修复**
164|- 文件名、front matter id、标题三处编号必须一致
165|
166|**🔗 详细命令、创建流程和冲突修复**: 见 [Story 编号管理规则](references/story_numbering_rules.md)
167|
168|---
169|
170|## Epic 文件管理规范（⚠️ 强制规则）
171|
172|- Epic 命名：`epic-{序号}-{简短描述}.md`，序号唯一且连续，kebab-case 描述
173|- Epic metadata 包含 11 个必需字段（id/title/description/status/priority/layer/owner/start_date/target_date/stories/dependencies）
174|- `stories` 数组**必须**与实际 Story 文件数量**完全一致**
175|- `layer` 分类：INFRA / DATA_LAYER / SERVICE_LAYER / APP_LAYER / CROSS_LAYER
176|
177|**Epic-Story 一致性检查清单**（创建/更新 Epic 时强制执行）：
178|- [ ] stories 数组包含所有 Story ID，数量 = 实际 Story 文件数量
179|- [ ] 每个 Story ID 都能在 stories 列表中找到
180|- [ ] Epic status 与 Story 完成比例一致
181|- [ ] Epic body checkbox 与 Story 实际状态同步（`- [x]` 对应 COMPLETED）
182|- [ ] 无重复 Epic 编号，metadata 包含所有必需字段
183|
184|**🔗 YAML 模板和验证命令**: 见 [Epic 模板](templates/epic_template.md)
185|
186|---
187|
188|## Story 状态更新流程（⚠️ 证据驱动）
189|
190|**核心原则**: 🔴 **一切基于证据，一切经过验证，一切严谨规范**
191|
192|**证据链完整性**:
193|```
194|Git Commit Evidence → Code Verification → Production Verification → Story Status Update → Epic Checkbox Sync → DASHBOARD/KANBAN Sync
195|```
196|
197|### AC 签字铁律（强制执行）
198|
199|**核心原则**：状态流转 = AC 签字率达标，不达标不流转。
200|
201|| 目标状态 | AC 签字率 | Task 签字率 | 前置条件 |
202||---------|----------|------------|---------|
203|| IN_PROGRESS | ≥ 0% | ≥ 0% | 至少 1 条 Task 已勾选（开发启动标志） |
204|| IN_REVIEW | ≥ 80% | ≥ 50% | 所有功能标准 AC 已勾选 |
205|| TESTING | 100% | ≥ 80% | 全部 AC 已勾选，测试标准 AC 已验证 |
206|| COMPLETED | 100% | 100% | 全部 AC + Task 已勾选，QA 验证通过 |
207|
208|**禁止事项**：
209|- ❌ AC 签字率 < 100% 就标记 COMPLETED
210|- ❌ 批量修改状态（必须逐 Story 验证后修改）
211|- ❌ 不留证据就勾选（每条勾选对应 git commit）
212|
213|**签字证据**：
214|- 功能类 AC：代码实现 commit 即为证据
215|- 测试类 AC：测试通过 commit 即为证据
216|- 证据格式：在 Story frontmatter 的 `verification_evidence` 字段记录 commit short SHA（7 位，如 `["d45bb35"]`）
217|
218|### 6-Step 流程概要
219|
220|**Step 1: Git Log Timeline 回溯分析**
221|- 时机: 每次更新 Story 状态前、每周五下午项目审计
222|- 命令: `git log --since="30 days ago"` 或 `git log --all --grep="STORY-X-XX"`
223|- 验证: 查找 Git 提交证据，确认代码真实存在
224|
225|**Step 2: 代码验证（Code Verification）**
226|- 验证清单: 检查 Commit 修改文件 → 阅读代码实现 → 运行测试
227|- 命令: `git show <commit-hash> --stat`, `git show <commit-hash> <file-path>`
228|- 标准: 代码真实存在 + 逻辑符合验收标准 + 测试通过
229|
230|**Step 3: 生产环境验证（Production Verification）⚠️ 条件触发**
231|- 适用场景: 数据库优化/数据清理/性能调优，或**任何关于生产环境的论断**
232|- 验证清单: 连接生产数据库 → 查询实际数据 → 基于实际数据生成结论
233|- 命令示例: `PGPASSWORD="password" psql -h <prod-host> -p <port> -U <user> -d <database>`
234|- 标准: ✅ 必须连接生产查询，❌ 不接受"Research 文档说..."或"应该是..."
235|
236|**Step 4: Story 状态修正**
237|- 修正原则: 只有 Git 证据 + 代码验证 + 生产环境验证（如适用）都通过后，才能更新状态
238|- 批量命令: `sed -i 's/^status: "TODO"/status: "COMPLETED"/' "$file"`
239|- 日期更新: `sed -i "/^---/a completed_date: \"$(date +%Y-%m-%d)\"" "$file"`
240|
241|**Step 5: Epic Body Checkbox 同步（⚠️ 强制执行）**
242|- 时机: Story 状态修正后、DASHBOARD/KANBAN 同步前
243|- 原因: Epic body 中的 Story 列表是团队进度的直观展示，checkbox 不同步会导致进度误判
244|
245|**同步操作**：
246|1. **更新 Story checkbox**：根据 Story 最终状态，将 Epic body 中对应行的 `- [ ]` 改为 `- [x]`（COMPLETED）或保持 `- [ ]`（TODO/IN_PROGRESS 等）
247|2. **补充缺失条目**：如果 Epic frontmatter 的 `stories` 列表中有 Story ID 但 body 中没有对应行，必须补充条目
248|3. **更新 Epic AC checkbox**：根据 Epic 内 Story 完成度，将验收标准中已满足的条目打钩（`- [ ]` → `- [x]`）。当 Epic 内所有 Story 均 COMPLETED 时，AC 应全部打钩
249|4. **验证一致性**：确保 body 的 Story 列表行数 = frontmatter `stories` 数组长度
250|
251|```bash
252|# 验证每个 Epic 的 checkbox 与 Story 状态一致
253|for epic_file in {project_docs}/scrum/prd/epic-*.md; do
254|  echo "=== $(basename $epic_file) ==="
255|  grep "\- \[[ x]\] STORY" "$epic_file"
256|done
257|```
258|
259|**禁止事项**：
260|- ❌ 更新 Story status 后不更新 Epic checkbox
261|- ❌ Epic body 缺少新增 Story 的条目
262|- ❌ checkbox 状态与 Story 实际状态不一致
263|
264|**Step 6: DASHBOARD/KANBAN 同步**
265|- 时机: Story 状态修正后立即同步
266|- 同步清单: DASHBOARD.md（Epic 进度、Story 统计） + KANBAN.md（看板列数据、分布统计）
267|- 验证: `grep -c 'status: "COMPLETED"' {project_docs}/scrum/story/*.md`
268|
269|### 禁止事项（Prohibitions）
270|
271|1. **禁止凭空更新 Story 状态**
272|   - ❌ "应该完成了" → ✅ 必须有 Git Commit 证据
273|   - ❌ "代码应该在那里" → ✅ 必须验证文件真实存在
274|
275|2. **禁止凭空推测生产环境状态** ⚠️ 新增强制规则
276|   - ❌ "Research 文档说生产环境有 X 条记录" → ✅ **必须连接数据库验证**
277|   - ❌ "生产环境应该是..." → ✅ **必须查询实际数据**
278|   - 🔴 严重后果: 立即更正，公开承认错误
279|
280|3. **禁止优先级设置不确认**
281|   - P0/P1 优先级必须与用户确认
282|   - 例：Node Informer 设置为 P2（应该是 P0）
283|
284|4. **禁止冗余文件堆积**
285|   - 不创建多版本文件（如 sprint-5-plan-final.md）
286|   - 定期清理 test_reports/ 临时文件
287|
288|**🔴 严重后果**: 违反"禁止凭空推测生产环境状态" → 立即更正，公开承认错误；重复违反 → 重新培训，暂停 PM 权限
289|
290|### 每周项目审计（Weekly Project Audit）
291|
292|**时间**: 每周五下午
293|
294|**审计清单**:
295|1. [ ] 运行 `git log --since="7 days ago"` 分析本周 Commit
296|2. [ ] 验证所有 IN_PROGRESS → COMPLETED 的 Story 代码实现
297|3. [ ] 确认没有"虚假完成"的 Story
298|4. [ ] **验证 Epic-Story 一致性**（⚠️ 新增强制检查）
299|   - [ ] 检查所有 Epic 的 stories 数组是否完整
300|   - [ ] 验证 stories 数量 = 实际 Story 文件数量
301|   - [ ] 确认每个 Story 都在对应 Epic 的 stories 列表中
302|   - [ ] 检查 Epic 编号是否唯一且连续
303|   - [ ] 验证 Epic metadata 包含所有必需字段
304|   - [ ] **验证 Epic body checkbox 与 Story 实际状态一致**（`- [x]` 对应 COMPLETED，`- [ ]` 对应非 COMPLETED）
305|5. [ ] 更新 DASHBOARD/KANBAN 反映真实进度
306|6. [ ] 清理冗余文件（test_reports/, 临时分析报告）
307|
308|**🔗 详细操作流程**: 见 `{skill_path}/references/story_status_update_workflow.md`
309|
310|---
311|
312|## Design Spec 演进规则（⚠️ 强制规则）
313|
314|Design Spec 是唯一真实来源，Epic/Story 是实现手段。版本更新时的核心规则：
315|
316|1. **新版本默认生效**：新 Design Spec → 新 Epic/Story
317|2. **取消旧 Story 并记录追溯**：`cancel_reason` / `replaced_by` / `cancel_date`
318|3. **已完成工作保留**：COMPLETED 状态不可篡改
319|4. **版本引用更新**：IN_PROGRESS Story 遇验收标准冲突 → 立即 BLOCKED；仅版本描述不一致 → 更新描述继续
320|
321|**🔗 完整规则和 Story 引用更新矩阵**: 见 [Design Spec 演进规则](references/design_spec_evolution_rules.md)
322|
323|---
324|
325|## 文档质量管理
326|
327|DASHBOARD.md 和 KANBAN.md 是衍生视图，禁止直接修改。数据源是 `{project_docs}/scrum/prd/` 和 `{project_docs}/scrum/story/`。
328|
329|更新流程：修改源文件 → 运行 `{skill_path}/scripts/audit_and_render.sh` → 验证格式 → 分离提交。
330|
331|**🔗 完整的渲染工具、验证命令和检查清单**: 见 [文档质量管理指南](references/document_quality_guide.md)
332|
333|---
334|
335|## 代码审查与检查清单
336|
337|Commit 必须包含 Story ID。审查覆盖代码质量、文档完整性、测试验证、Story 同步、编号一致性五个维度。
338|
339|**🔗 Commit 格式、示例和完整检查清单**: 见 [代码审查指南](references/code_review_guide.md)
340|
341|---
342|
343|## 附加资源
344|
345|**详细参考文档**（按需加载）：
346|- [Design Spec 演进规则](references/design_spec_evolution_rules.md) - 版本更新和 Story 管理规则
347|- [Story 编号管理规则](references/story_numbering_rules.md) - 编号冲突检测和修复流程
348|- [Story 状态更新工作流](references/story_status_update_workflow.md) - 6-Step 证据驱动的状态更新流程
349|- [AC 测试分层策略](references/ac_testing_strategy.md) - 测试层级矩阵和 Story 状态对应关系
350|- [文档质量管理指南](references/document_quality_guide.md) - 渲染工具、更新流程、检查清单
351|- [代码审查指南](references/code_review_guide.md) - Commit 格式、审查检查清单
352|
353|**辅助脚本**：`scripts/audit_and_render.sh` / `audit_metadata.py` / `kanban_renderer.py` / `render_views.py`
354|
355|**模板文件**：`templates/` 目录（story / epic / dashboard / kanban / sprint_plan / sprint_retro / todo）
356|
357|**协作 SKILL**：`arch` / `dev` / `qa` / `devops`
358|
359|**占位符**：`{project_docs}` = `docs/`，`{skill_path}` = `.claude/skills/pm/`
360|
361|---
362|
363|**版本**: v13.0
364|**更新日期**: 2026-05-18
365|
366|**更新日志**：
367|- v13.0 (2026-05-18): 精简主文档 750→~500 行，详细内容迁移至 references/
368|  - Story 编号管理、Epic 文件管理：删除内联命令，保留规则摘要+链接
369|  - Design Spec 演进规则：压缩为 4 条核心规则+链接
370|  - 文档质量管理 → 新建 references/document_quality_guide.md
371|  - 代码审查与检查清单 → 新建 references/code_review_guide.md
372|- v12.1 (2026-05-18): 新增 Epic Body Checkbox 同步步骤
373|- v12.0 (2026-04-29): 重大重组，渐进式披露优化
374|