
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
2|skill: "arch"
3|description: "架构师工作技能 - 架构设计、文档管理、语义化版本控制、设计审查。当用户提到架构、设计文档、版本管理、技术选型、系统设计、数据模型、API设计、文档审查、设计规范、架构决策、或需要创建/更新设计文档时，必须使用此技能。确保所有设计文档遵循语义化版本规范和命名约定。"
4|version: "2.1"
5|---
6|
7|# Architect 工作技能
8|
9|## 核心职责
10|
11|1. **架构设计**：设计系统架构、数据模型、服务层架构、应用层架构
12|2. **文档管理**：维护设计文档，遵循语义化版本管理规范
13|3. **技术决策**：制定技术选型、设计模式、最佳实践
14|4. **跨层协调**：协调系统层、数据层、服务层、应用层的设计一致性
15|5. **设计审查**：Review 设计文档，确保架构合理性和可实施性
16|
17|---
18|
19|## 📋 设计文档管理规范（铁律）
20|
21|### ⚠️ 核心原则：语义化版本 + 层次分类 + 归档管理
22|
23|**设计文档必须遵循以下规则**：
24|
25|1. **按架构层次分类命名**：使用 `{layer}_design_{sem_ver}.md` 格式
26|2. **语义化版本号**：变更粒度直接体现在文件名版本部分
27|3. **归档管理**：旧版本归档到 `{project_docs}/design/archive/`
28|4. **禁止描述性命名**：不使用 plan、update、design 等临时文件名
29|
30|**说明**:
31|- `{layer}`: 架构层次（如 system, service, data_layer, api）
32|- `{sem_ver}`: 语义化版本号（如 v1.0, v2.3.1）
33|- `{project_docs}`: 项目文档目录（通常为 docs/）
34|
35|---
36|
37|### 📏 文档长度控制原则
38|
39|**核心要求**：
40|- **主文档**（遵循 `{layer}_design_{sem_ver}.md` 命名）：建议保持在 1000-2000 行
41|- **子文档**（遵循 `{layer}/{topic}_guide_{sem_ver}.md` 命名）：建议 200-500 行
42|- **避免过度示例化**：减少代码示例和详细实现指南
43|
44|**拆分策略**：
45|1. **主文档保留核心设计**：架构概述、职责分离、数据模型概览、查询策略
46|2. **子文档聚焦专题实现**：特定技术实现、配置详解、部署方案等
47|3. **跨文档引用**：主文档提供概述和引用，子文档提供详细实现
48|
49|**文档结构示例**（使用占位符）：
50|```
51|{project_docs}/design/
52|├── {layer}_design_v{major}.{minor}.{patch}.md    # 主文档
53|├── {layer}/                                       # 子文档目录（可选）
54|│   ├── {topic}_guide_v{major}.{minor}.{patch}.md
55|│   └── {implementation}_v{major}.{minor}.{patch}.md
56|└── archive/                                       # 归档目录
57|    └── {layer}_design_v{major}.{minor}.{patch}_{date}.md
58|```
59|
60|**说明**:
61|- `{project_docs}`: 项目文档目录（如 docs/）
62|- `{layer}`: 架构层次（如 service, data_layer, api）
63|- `{major}.{minor}.{patch}`: 版本号（如 4.1.2）
64|- `{topic}`: 专题名称（如 fk_constraint, data_sync）
65|- `{implementation}`: 实现名称（如 ttl_cleanup）
66|- `{date}`: 归档日期（YYYYMMDD，如 20260422）
67|
68|**引用格式**（主文档 → 子文档）：
69|```markdown
70|### {章节编号} {专题名称}
71|
72|> **详细设计文档**：[{专题名称} v{version}]({layer}/{topic}_guide_v{version}.md)
73|
74|**核心原则**：
75|1. {原则 1}
76|2. {原则 2}
77|3. {原则 3}
78|```
79|
80|**判断标准**（何时拆分子文档）：
81|| 场景 | 处理方式 | 行数建议 |
82||------|---------|---------|
83|| 核心架构设计 | 保留在主文档 | 100-200 行 |
84|| 详细实现指南 | 拆分子文档 | 200-500 行 |
85|| 完整实施方案 | 拆分子文档 | 300-500 行 |
86|| 大型时序图/流程图 | 拆分子文档 | > 50 行 |
87|
88|**禁止事项**：
89|- ❌ 主文档超过 2000 行（必须拆分）
90|- ❌ 子文档超过 500 行（进一步拆分或精简）
91|- ❌ 代码示例占比超过 30%（移到子文档或代码仓库）
92|- ❌ 过度示例化（保留关键示例，删除重复说明）
93|
94|---
95|
96|### 🔗 跨文档引用准确性原则
97|
98|**引用路径规范**（使用相对路径）：
99|1. **主文档 → 子文档**：使用相对路径 `{layer}/`（同层目录）
100|2. **子文档 → 主文档**：使用相对路径 `../{layer}_design_v{version}.md`
101|3. **子文档 → 子文档**：使用相对路径 `./{filename}.md`
102|4. **所有文档 → Story/报告**：使用相对路径 `../../{dir}/{subdir}/`
103|
104|**说明**:
105|- `{layer}`: 架构层次（如 service, data_layer）
106|- `{version}`: 版本号（如 v1.0, v2.1）
107|- `{dir}`: 目录名称（如 scrum, test_reports）
108|- `{subdir}`: 子目录名称（如 story, reports）
109|
110|**引用格式**：
111|```markdown
112|# 主文档中的引用
113|> **详细设计文档**：[{专题名称} v{version}]({layer}/{topic}_guide_v{version}.md)
114|
115|# 子文档中的引用
116|- **[主文档：{layer}_design_v{version}.md - {章节编号}节](../{layer}_design_v{version}.md#{section-id})**
117|- **[{story-id}]({project_docs}/scrum/story/{story-file}.md)**
118|```
119|
120|**验证清单**：
121|- [ ] 所有引用路径使用相对路径（不使用绝对路径）
122|- [ ] 锚点链接（`#section-id`）准确有效
123|- [ ] 跨文档引用在不同目录层级下正常工作
124|- [ ] 子文档移动后更新主文档引用路径
125|
126|---
127|
128|## 文档命名规范
129|
130|### 架构层次分类
131|
132|| 架构层次 | 文档命名模式 | 占位符说明 |
133||---------|-------------|-----------|
134|| **系统架构** | `system_architecture_{sem_ver}.md` | {sem_ver} = v{major}.{minor}.{patch} |
135|| **数据层/数据存储** | `data_layer_design_{sem_ver}.md` | {sem_ver} = v{major}.{minor}.{patch} |
136|| **服务层架构** | `service_layer_architecture_{sem_ver}.md` | {sem_ver} = v{major}.{minor}.{patch} |
137|| **API/应用层** | `api_design_{sem_ver}.md` | {sem_ver} = v{major}.{minor}.{patch} |
138|| **FAQ 文档** | `{name}_faq_{sem_ver}.md` | {name} = 主题名称，{sem_ver} = 版本号 |
139|
140|### 语义化版本规则
141|
142|**版本格式**：`v{MAJOR}.{MINOR}.{PATCH}`
143|
144|| 版本类型 | 版本号示例 | 变更类型 | 判断标准 |
145||---------|-----------|---------|---------|
146|| **MAJOR** | v2.0 → v3.0 | 重大功能新增、架构变更 | 新增完整章节、数据模型变更、接口重定义 |
147|| **MINOR** | v2.0 → v2.1 | 功能新增、向后兼容 | 新增小功能、配置项、优化项 |
148|| **PATCH** | v2.0 → v2.0.1 | Bug 修复、小改动 | 修正错误、补充说明、格式调整 |
149|
150|### ❌ 禁止的命名方式
151|
152|**以下命名方式严格禁止**：
153|
154|1. ❌ 描述性语言命名的临时文件：
155|   - `{feature}_design_updates.md`
156|   - `plan_{date}.md`
157|   - `design_update_phase1.md`
158|   - 任何带有 `plan`、`update`、`design`、`proposal` 等前缀的文件名
159|
160|2. ❌ 不带版本号的文档：
161|   - `{layer}_design.md`（缺少版本号）
162|   - `faq.md`（缺少版本号）
163|
164|3. ❌ 使用日期作为版本号：
165|   - `{layer}_design_{date}.md`（如 20260204）
166|
167|**✅ 正确的命名方式**：
168|
169|- ✅ `{layer}_design_v{major}.{minor}.{patch}.md`
170|- ✅ `{layer}_design_v{major}.{minor}.md`
171|- ✅ `{name}_faq_v{major}.{minor}.{patch}.md`
172|
173|---
174|
175|## 文档归档规范
176|
177|### 归档时机
178|
179|**每次 MAJOR 或 MINOR 版本更新时**，必须归档旧版本：
180|
181|```bash
182|# 归档旧版本（示例）
183|mv {project_docs}/design/{layer}_design_v{major}.{minor}.md \
184|   {project_docs}/design/archive/{layer}_design_v{major}.{minor}_$(date +%Y%m%d).md
185|
186|# 说明:
187|# - {project_docs}: 项目文档目录（如 docs/）
188|# - {layer}: 架构层次（如 service_layer, data_layer）
189|# - {major}.{minor}: 版本号
190|```
191|
192|### 归档文件命名
193|
194|**格式**：`{filename}_v{version}_{date}.{ext}`
195|
196|| 组成部分 | 说明 | 示例 |
197||---------|------|------|
198|| `{filename}` | 原文件名（不含版本号） | `{layer}_design` |
199|| `{version}` | 语义化版本号 | `v2.0` |
200|| `{date}` | 归档日期（YYYYMMDD） | `20260204` |
201|| `{ext}` | 文件扩展名 | `.md` |
202|
203|**完整示例**：
204|- `{layer}_design_v{version}_{date}.md`（如 `service_layer_architecture_v2.0_20260204.md`）
205|
206|### 默认保留位置
207|
208|**`{project_docs}/design/` 目录只保留最新版本**：
209|
210|```bash
211|# 正确的目录结构（示例）
212|{project_docs}/design/
213|├── {layer1}_design_v{version}.md      # 最新版本
214|├── {layer2}_design_v{version}.md      # 最新版本
215|└── {layer3}_design_v{version}.md      # 最新版本
216|
217|{project_docs}/design/archive/
218|├── {layer1}_design_v{version}_{date}.md   # 历史版本
219|├── {layer2}_design_v{version}_{date}.md   # 历史版本
220|└── {layer3}_design_v{version}_{date}.md   # 历史版本
221|
222|{project_docs}/design/analysis/       # 分析文档目录（可选）
223|├── informer_data_analysis.md          # Informer 实际数据分析
224|├── database_schema_analysis.md        # 数据库 schema 分析
225|└── technical_research.md              # 技术方案调研
226|```
227|
228|**说明**：
229|- `archive/` 目录存放归档的历史版本（带日期戳）
230|- `analysis/` 目录存放分析文档和调研报告（不受版本管理约束）
231|
232|---
233|
234|## 文档更新流程
235|
236|### Step 1: 确定变更类型
237|
238|**判断版本号增量**：
239|
240|| 变更内容 | 版本类型 | 文件名变化 |
241||---------|---------|-----------|
242|| 新增完整章节 | MAJOR | v2.0 → v3.0 |
243|| 数据模型变更（DDL 脚本） | MAJOR | v2.3 → v3.0 |
244|| 接口重定义 | MAJOR | v1.0 → v2.0 |
245|| 新增配置项 | MINOR | v2.0 → v2.1 |
246|| 新增小功能 | MINOR | v2.0 → v2.1 |
247|| 修正错误 | PATCH | v2.0 → v2.0.1 |
248|| 补充说明 | PATCH | v2.0 → v2.0.1 |
249|
250|### Step 2: 归档旧版本（如需要）
251|
252|**MAJOR 或 MINOR 版本更新时**：
253|
254|```bash
255|# 进入项目目录
256|cd {project_root}
257|
258|# 归档旧版本
259|mv {project_docs}/design/{layer}_design_v{old_version}.md \
260|   {project_docs}/design/archive/{layer}_design_v{old_version}_$(date +%Y%m%d).md
261|
262|# 说明:
263|# - {project_root}: 项目根目录
264|# - {project_docs}: 项目文档目录（如 docs/）
265|# - {layer}: 架构层次（如 service, data_layer）
266|# - {old_version}: 旧版本号（如 v2.0）
267|```
268|
269|**PATCH 版本更新时**：
270|- 不归档（直接覆盖文件）
271|
272|### Step 3: 创建新版本
273|
274|**方式 1：复制旧版本（推荐）**
275|
276|```bash
277|# 复制旧版本作为基础
278|cp {project_docs}/design/archive/{layer}_design_v{version}_{date}.md \
279|   {project_docs}/design/{layer}_design_v{new_version}.md
280|
281|# 编辑新版本，更新内容
282|vim {project_docs}/design/{layer}_design_v{new_version}.md
283|```
284|
285|**方式 2：直接创建新版本**
286|
287|```bash
288|# 直接创建新版本
289|vim {project_docs}/design/{layer}_design_v{new_version}.md
290|```
291|
292|### Step 4: 更新版本历史表
293|
294|**在每个文档的开头更新版本历史表**：
295|
296|```markdown
297|## 📋 版本历史
298|
299|| 版本 | 日期 | 变更说明 | 作者 |
300||------|------|---------|------|
301|| v1.0 | YYYY-MM-DD | 初始版本 | {Author} |
302|| v2.0 | YYYY-MM-DD | {变更说明} | {Author} |
303|| v3.0 | YYYY-MM-DD | {变更说明} | {Author} |
304|
305|**v3.0 主要变更**：
306|- ✅ {变更 1}
307|- ✅ {变更 2}
308|- ✅ {变更 3}
309|- ⚠️ 向后兼容：{兼容性说明}
310|```
311|
312|### Step 5: 更新相关文档
313|
314|**如果更新涉及多个文档，保持版本号一致**：
315|
316|| 主文档版本 | FAQ 文档版本 | 示例场景 |
317||-----------|------------|---------|
318|| v3.0 | v3.0 | 主文档 MAJOR 更新，FAQ 同步更新 |
319|| v2.1 | v2.1 | 主文档 MINOR 更新，FAQ 同步更新 |
320|| v2.0.1 | 不变 | 主文档 PATCH 更新，FAQ 不变 |
321|
322|---
323|
324|## 文档结构规范
325|
326|### 必需章节
327|
328|**每个设计文档必须包含以下章节**：
329|
330|1. **文档元数据**
331|   ```markdown
332|   # {文档标题} v{version}
333|
334|   **版本**: vX.Y (简短说明)
335|   **创建日期**: YYYY-MM-DD
336|   **状态**: ✅ 设计完成 | 🚧 实施中 | ⚠️ 已废弃
337|   **替代版本**: vX.Y-1 (已归档至 `archive/文件名_vX.Y-1_YYYYMMDD.md`)
338|
339|   **版本说明**:
340|   - vX.Y 新增功能/修正内容 1
341|   - vX.Y 新增功能/修正内容 2
342|
343|   **关键修正**（如果有）:
344|   1. ❌ vX.Y-1 错误假设
345|      ✅ vX.Y 修正内容
346|   ```
347|
348|2. **版本历史表**
349|   ```markdown
350|   ## 📋 版本历史
351|
352|   | 版本 | 日期 | 变更说明 | 作者 |
353|   |------|------|---------|------|
354|   | v1.0 | YYYY-MM-DD | 初始版本 | {Author} |
355|   | v2.0 | YYYY-MM-DD | {变更说明} | {Author} |
356|   | v3.0 | YYYY-MM-DD | {变更说明} | {Author} |
357|
358|   **v3.0 主要变更**：
359|   - ✅ {变更 1}
360|   - ✅ {变更 2}
361|   - ✅ {变更 3}
362|   - ⚠️ 向后兼容：{兼容性说明}
363|   ```
364|
365|3. **文档概述**
366|   ```markdown
367|   ## 📋 文档概述
368|
369|   本文档描述了...
370|   ```
371|
372|4. **核心内容章节**
373|   - 根据文档类型组织（架构设计、数据模型、API 设计等）
374|
375|5. **变更日志（可选）**
376|   - 对于 PATCH 更新，可以添加变更日志
377|
378|### 推荐章节结构（示例）
379|
380|**说明**: 以下为通用示例，具体章节根据实际项目调整
381|
382|**system_architecture_{sem_ver}.md**：
383|```markdown
384|1. 架构概述
385|2. 技术栈
386|3. 部署架构
387|4. 网络拓扑
388|5. 安全设计
389|6. 监控告警
390|```
391|
392|**data_layer_design_{sem_ver}.md**：
393|```markdown
394|1. 设计概述
395|2. 数据模型
396|3. 表结构设计
397|4. 索引设计
398|5. 数据字典
399|6. 迁移脚本
400|```
401|
402|**service_layer_architecture_{sem_ver}.md**（示例）：
403|```markdown
404|1. 架构概述
405|2. 监听机制（如 K8s Informer、消息队列）
406|3. 生命周期处理流程
407|4. 状态机设计
408|5. 业务逻辑计算
409|6. 资源实时计算
410|7. 事件日志输出
411|8. {新增章节}
412|```
413|
414|**{name}_faq_{sem_ver}.md**：
415|```markdown
416|FAQ-1: {问题 1}
417|FAQ-2: {问题 2}
418|...
419|FAQ-N: {问题 N}
420|```
421|
422|---
423|
424|## 设计文档与 Story 的关系
425|
426|### 双向追踪
427|
428|**设计文档 → Story**：
429|- 每个设计文档可以对应多个 Story
430|- Story 引用设计文档的章节号
431|
432|**Story → 设计文档**：
433|- Story 的实现会更新设计文档
434|- 设计文档的版本号要体现 Story 的变更
435|
436|### 示例（通用化）
437|
438|```yaml
439|# Story 文档（示例格式）
440|story_id: "{story-id}"
441|title: "{story-title}"
442|dependencies:
443|  - "{parent-story-id}"
444|design_docs:
445|  - "{project_docs}/design/{layer}_design_v{version}.md#{chapter}"
446|  - "{project_docs}/design/{layer}_design_v{version}.md#{section}"
447|
448|# 实施完成后
449|design_updates:
450|  - "{project_docs}/design/{layer}_design_v{new_version}.md"  # v{old} → v{new}
451|  - "{project_docs}/design/{name}_faq_v{new_version}.md"      # v{old} → v{new}
452|```
453|
454|**说明**:
455|- `{story-id}`: Story 标识符（如 STORY-123）
456|- `{chapter}`: 章节名称（如 "历史记录存储优化"）
457|- `{section}`: 小节名称（如 "数据模型概述"）
458|
459|---
460|
461|## 常见错误
462|
463|### ❌ 错误 1：使用描述性文件名
464|
465|```bash
466|# ❌ 错误：创建临时规划文件
467|vim {project_docs}/design/plan_{date}.md
468|vim {project_docs}/design/{feature}_design_updates.md
469|vim {project_docs}/design/proposal.md
470|
471|# ✅ 正确：直接更新正式文档
472|vim {project_docs}/design/{layer}_design_v{version}.md
473|```
474|
475|### ❌ 错误 2：不归档旧版本
476|
477|```bash
478|# ❌ 错误：保留多个版本在 design/ 目录
479|{project_docs}/design/
480|├── {layer}_design_v1.0.md
481|├── {layer}_design_v2.0.md
482|└── {layer}_design_v3.0.md
483|
484|# ✅ 正确：只保留最新版本
485|{project_docs}/design/
486|└── {layer}_design_v3.0.md
487|
488|{project_docs}/design/archive/
489|├── {layer}_design_v1.0_{date1}.md
490|└── {layer}_design_v2.0_{date2}.md
491|```
492|
493|### ❌ 错误 3：版本号判断错误
494|
495|```bash
496|# ❌ 错误：小改动使用 MAJOR 版本
497|修正错别字 → v2.0 → v3.0  # 过度升级
498|
499|# ✅ 正确：小改动使用 PATCH 版本
500|修正错别字 → v2.0 → v2.0.1
501|