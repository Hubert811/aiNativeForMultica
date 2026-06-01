1|---
2|name: spec-xchecker
3|description: "四路交叉验证工具 - 检查 Design Spec ↔ Scrum ↔ Code ↔ Tests 对齐一致性。当用户需要验证代码实现是否符合设计文档、Story AC 是否完整、测试覆盖率是否达标、Story 开发完成检查、MR 前验证、代码审查一致性验证时，必须使用此技能。适用于开发阶段的质量保证，防止设计文档与实现不一致的事故。⚠️ 实验性质技能，建议结合人工审查使用。"
4|version: "3.0"
5|status: "experimental"
6|---
7|
8|# Spec-XChecker - 四路交叉验证工具
9|
10|> **⚠️ 实验性质技能（Experimental）**
11|>
12|> 此技能仍在积极迭代优化中，可能存在误报和漏报。建议作为开发辅助工具，结合人工审查使用。
13|
14|---
15|
16|## 快速开始
17|
18|### 基本用法
19|
20|```bash
21|# 检查指定 Story（Medium Mode，推荐）
22|/spec-xchecker --story 15-23 --mode medium
23|
24|# 自动模式（自动检测当前 Story）
25|/spec-xchecker --auto-mode --mode medium
26|```
27|
28|### 三种检查模式
29|
30|| 模式 | 执行时间 | 检查范围 | 适用场景 |
31||------|----------|----------|----------|
32|| **Quick** | <1 秒 | DS 层（设计依据） | Hook 自动触发、简单修改 |
33|| **Medium** | 5-10 秒 | DS + SC 层 | Story 开发完成、MR 前检查 |
34|| **Deep** | 30-60 秒 | DS + SC + CT + ST 层 | 大型重构、关键 Story 验证 |
35|
36|---
37|
38|## 核心职责
39|
40|Spec-XChecker 是一个自动化审查工具，用于检查项目文档与代码实现的一致性，防止设计文档与实现不一致的事故。
41|
42|**四路交叉验证**：
43|1. **DS 层**：Design Spec ↔ Scrum 文档（验证详细设计是否有概要设计依据）
44|2. **SC 层**：Scrum AC ↔ 代码实现（验证代码是否实现详细设计）
45|3. **CT 层**：代码函数 ↔ 单元/API 测试（验证代码是否有测试覆盖）
46|4. **ST 层**：Scrum AC ↔ SIT/UAT 测试（验证测试是否覆盖详细设计）
47|
48|**核心原则**：
49|- **设计域 = SSOT**（唯一真实来源）：概要设计（`docs/design/`）+ 详细设计（`docs/scrum/`）
50|- **检查顺序铁律**：DS → SC → CT → ST（逐层验证，确保每一步都有依据）
51|- **静态检查 ≠ 运行时保证**：工具只做静态语义分析，不保证运行时正确性
52|
53|---
54|
55|## 检查清单
56|
57|**📌 完整的 21 项检查清单**：参见 [references/checklist.md](references/checklist.md)
58|
59|**检查顺序铁律**：DS → SC → CT → ST（不可改变）
60|
61|### DS 层：Design Spec ↔ Scrum（4 项）
62|
63|- **DS-01**: Story 是否引用 Design Spec（P1）
64|- **DS-02**: AC 是否与 Design Spec 一致（P0）
65|- **DS-03**: Design Spec 引用是否正确（P2）
66|- **DS-04**: Epic 规划是否与 Design Spec 一致（P2）
67|
68|### SC 层：Scrum ↔ Code（6 项）
69|
70|- **SC-01**: 每个 AC 是否有对应代码实现（P1）
71|- **SC-02**: 代码逻辑是否满足 AC 描述（P0）
72|- **SC-03**: 新增代码是否引用了正确的表/字段（P1）
73|- **SC-04**: 错误处理是否覆盖异常场景（P2）
74|- **SC-05**: 日志输出是否符合规范（P2）
75|- **SC-06**: Commit Message 是否包含 Story ID（P2）
76|
77|### CT 层：Code ↔ Tests (UT/API)（5 项）
78|
79|- **CT-01**: UT 函数是否有对应 test_ 函数（P0）
80|- **CT-02**: API 接口是否有对应测试（P0）
81|- **CT-03**: UT 测试覆盖率是否达标（P1）
82|- **CT-04**: API 测试覆盖率是否达标（P1）
83|- **CT-05**: Mock 使用是否合理（P2）
84|
85|### ST 层：Scrum ↔ Tests (SIT/UAT)（6 项）
86|
87|- **ST-01**: 每个 AC 是否有对应 SIT 用例（P0）
88|- **ST-02**: SIT 测试是否检查正确对象（P0）
89|- **ST-03**: SIT 测试是否覆盖异常路径（P1）
90|- **ST-04**: UAT 测试是否覆盖用户场景（P2）
91|- **ST-05**: 测试数据质量评分是否达标（P2）
92|- **ST-06**: SIT 覆盖率是否满足要求（P1）
93|
94|**优先级说明**：
95|- **P0**：关键检查，必须通过
96|- **P1**：重要检查，建议通过
97|- **P2**：次要检查，可选通过
98|
99|---
100|
101|## 智能策略引擎（v3.0）
102|
103|**📌 详细的 AC 类型映射和策略说明**：参见 [references/strategy_engine.md](references/strategy_engine.md)
104|
105|### 核心特性
106|
107|**AC 驱动的动态检查策略**：
108|- 基于 AC 内容的本地启发式规则（不依赖外部 LLM API）
109|- 自动识别 Story 类型（Database/Testing/Feature/Hotfix 等）
110|- 动态生成检查策略（哪些检查做，哪些跳过）
111|
112|**三级智能策略引擎**：
113|1. **Level 1**：扩展 AC 类型映射表（10 种类型）
114|2. **Level 2**：负向规则引擎（positive_checks + negative_checks 机制）
115|3. **Level 3**：动态置信度计算（单一类型 95%，混合类型 80%，无法分类 60%）
116|
117|**效果提升**：
118|- STORY-15-22 检查项减少 43%（21→12）
119|- 通过率提升 76%（33%→58%）
120|
121|---
122|
123|## 高级用法
124|
125|**📌 详细使用指南**：参见 [references/usage_guide.md](references/usage_guide.md)
126|
127|### 指定检查范围
128|
129|```bash
130|# 只检查 Code + Tests 层
131|/spec-xchecker --story 15-23 --mode medium --scope code,test
132|
133|# 只检查 Design + Scrum 层
134|/spec-xchecker --story 15-23 --mode quick --scope ds,sc
135|```
136|
137|### 指定输出格式
138|
139|```bash
140|# Markdown 报告（默认）
141|/spec-xchecker --story 15-23 --mode medium
142|
143|# JSON 报告
144|/spec-xchecker --story 15-23 --mode medium --format json
145|```
146|
147|---
148|
149|## 与其他工具的区别
150|
151|| 维度 | `/review` | `/spec-xchecker` |
152||------|-----------|-----------------|
153|| **核心目标** | 代码质量（Code is good?） | 对齐一致性（Code matches Spec?） |
154|| **检查依据** | 通用编程规范、项目规范 | Design Spec、Story AC、测试用例 |
155|| **检查范围** | 代码逻辑、风格、性能、安全 | 代码与文档的一致性 |
156|| **典型场景** | 日常 Code Review | Story 开发、MR 前检查 |
157|
158|**协作关系**：
159|- 代码质量检查 → `/review` SKILL
160|- 对齐一致性检查 → `/spec-xchecker` SKILL
161|- 大型重构检查 → `/spec-xchecker --mode deep`
162|
163|---
164|
165|## 工具能力边界
166|
167|**Spec-XChecker 能做什么**（静态检查）：
168|1. **文档结构检查**：文件是否存在、引用是否正确
169|2. **文本语义分析**：AC 关键词提取、代码函数名匹配
170|3. **覆盖率统计**：测试函数数量、表引用检查
171|4. **模式匹配**：Commit Message 格式、日志关键字
172|
173|**Spec-XChecker 不能做什么**（运行时保证）：
174|1. ❌ **动态行为验证**：代码在运行时是否正确
175|2. ❌ **集成问题**：多个模块协同工作是否正常
176|3. ❌ **性能问题**：代码执行效率、资源占用
177|4. ❌ **安全漏洞**：SQL 注入、XSS 等运行时安全问题
178|
179|**工程实践建议**：
180|- ✅ Spec-XChecker 用于**开发阶段**（MR 前检查、Story 完成验证）
181|- ✅ 运行时保证依赖**SIT/UAT 测试**（集成测试、用户验收测试）
182|- ✅ 生产环境依赖**监控告警**（日志、Metrics、Tracing）
183|
184|---
185|
186|## 🤝 Agent Team 协作
187|
188|Spec-XChecker 与其他角色密切协作：
189|
190|- **架构设计** → [arch SKILL](../arch/SKILL.md)
191|  - Design Spec 版本管理
192|  - 文档归档规范
193|  - 参考：`docs/design/` 中的设计文档
194|
195|- **开发工作流** → [dev SKILL](../dev/SKILL.md)
196|  - 代码实现验证
197|  - 单元测试覆盖
198|  - Commit Message 规范
199|
200|- **测试验证** → [qa SKILL](../qa/SKILL.md)
201|  - SIT/UAT 测试覆盖
202|  - 测试数据质量
203|  - 参考：`test_reports/` 中的测试报告
204|
205|- **项目管理** → [pm SKILL](../pm/SKILL.md)
206|  - Story AC 验证
207|  - Epic 规划一致性
208|  - 参考：`docs/scrum/story/` 中的 Story 文档
209|
210|---
211|
212|## 目录结构
213|
214|```
215|.claude/skills/spec-xchecker/
216|├── SKILL.md                     # 本文件（主入口）
217|├── scripts/                     # 所有可执行代码（官方标准）
218|│   ├── spec-xchecker.py         # 主入口脚本
219|│   ├── report_generator.py      # 报告生成器
220|│   ├── strategy_generator.py    # 智能策略引擎（v2.5）
221|│   ├── trigger_check.sh         # Stop Hook 触发脚本
222|│   ├── notify_pending_reports.sh # SessionStart Hook 通知脚本
223|│   ├── lib/                     # Python 模块
224|│   │   ├── story_resolver.py    # Story ID + Design Spec 加载
225|│   │   ├── classify.py          # 文件分类
226|│   │   └── get_memory_dir.sh    # Memory 目录计算
227|│   └── checkers/                # 检查器模块
228|│       ├── checker_design.py    # DS 层检查
229|│       ├── checker_code.py      # SC 层检查
230|│       ├── checker_test.py      # CT 层检查
231|│       ├── checker_scenario.py  # ST 层检查
232|│       └── strategy_generator.py# 智能策略引擎
233|├── references/                  # 参考文档（官方标准，按需加载）
234|│   ├── usage_guide.md           # 详细使用指南
235|│   ├── checklist.md             # 完整的 21 项检查清单
236|│   ├── strategy_engine.md       # 智能策略引擎详细说明
237|│   └── troubleshooting.md       # 故障排查指南
238|└── assets/                     # 静态资源（官方标准）
239|    └── config/
240|        └── config.yaml          # 配置文件
241|```
242|
243|**目录结构说明**（符合官方最佳实践）：
244|- ✅ **scripts/** - 所有可执行代码（Python 脚本、Shell 脚本、模块）
245|- ✅ **references/** - 参考文档（按需加载，支持渐进式披露）
246|- ✅ **assets/** - 静态资源文件（配置文件、模板等）
247|- ✅ **SKILL.md** - 主入口（<500 行，包含核心内容和导航）
248|
249|---
250|
251|## 常见问题
252|
253|**Q: 如何选择检查模式？**
254|- Quick：Hook 自动触发、简单修改
255|- Medium：Story 开发完成、MR 前检查（推荐）
256|- Deep：大型重构、关键 Story 验证
257|
258|**Q: 检查通过但实际有问题？**
259|- 使用 `--mode deep` 进行更深入的检查
260|- 检查报告详情，查看具体是哪一项检查失败
261|
262|**Q: 找不到 Story 文档？**
263|- 检查 `docs/scrum/story/story-13-{story_id}-*.md` 是否存在
264|- 确认 Story ID 格式正确（例如：15-23）
265|
266|**Q: 没有 Design Spec？**
267|- 在 Story 文档中添加 `Design Spec: ../design/xxx.md`
268|- 创建对应的 Design Spec 文档
269|
270|**Q: 遇到其他问题？**
271|- 参见 [references/troubleshooting.md](references/troubleshooting.md) 获取详细故障排查指南
272|
273|---
274|
275|## 详细参考
276|
277|以下参考文档会在需要时按需加载：
278|
279|- **[详细使用指南](references/usage_guide.md)** - 命令行参数、Hook 配置、报告位置
280|- **[完整检查清单](references/checklist.md)** - 21 项检查的详细说明
281|- **[智能策略引擎](references/strategy_engine.md)** - AC 类型映射、负向规则、置信度计算
282|- **[故障排查指南](references/troubleshooting.md)** - 常见问题和解决方案
283|
284|---
285|
286|**版本**: v4.0 | **最后更新**: 2026-04-29 | **维护者**: chenhuazhong@minieye.cc
287|
288|**更新日志**:
289|- v4.0 (2026-04-29): 🎯 **官方标准重构**：完全符合 Claude Code 官方文档
290|  - **目录结构规范化**：调整为官方标准目录
291|    - ✅ `scripts/` - 所有可执行代码（包括 lib/ 和 checkers/ 子目录）
292|    - ✅ `references/` - 参考文档（按需加载，支持渐进式披露）
293|    - ✅ `assets/` - 静态资源（配置文件）
294|    - ❌ 移除非标准的 `lib/` 和 `checkers/` 顶级目录
295|  - **导入路径更新**：所有 Python 导入路径更新为 `scripts.lib.*` 和 `scripts.checkers.*`
296|  - **SKILL.md 目录结构章节**：更新为新的官方标准结构
297|  - **参考来源**：[Extend Claude with skills - Claude Code Docs](https://code.claude.com/docs/en/skills)
298|- v3.0 (2026-04-29): 🎯 **重大重构**：符合 Claude Code Skill 最佳实践
299|  - 更新 frontmatter（skill → name）
300|  - 优化 description，更"pushy"的触发描述
301|  - 精简主 SKILL.md（483 行 → ~300 行，减少 38%）
302|  - 提取详细内容到 references/ 目录
303|  - 添加 Agent Team 协作章节
304|  - 添加清晰的导航指引
305|- v2.5 (2026-04-25): 智能策略引擎（三级智能、负向规则、动态置信度）
306|- v2.4 (2026-04-20): AC 驱动的动态检查策略（POC）
307|- v2.3 (2026-04-15): 三域一致性模型、检查顺序铁律
308|