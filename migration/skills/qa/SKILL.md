1|---
2|skill: "qa"
3|description: "QA 工作技能 - 测试分层架构、UT 单元测试、SIT 系统集成测试、UAT 用户验收测试、API 接口测试、测试报告管理。当用户提到测试、QA、质量保证、单元测试、集成测试、验收测试、测试用例设计、pytest、go test、测试覆盖率、测试报告、回归测试、TDD、测试框架、问题排查、测试环境、测试数据、测试幂等性、或需要设计/执行测试时，必须使用此技能。确保所有测试活动遵循分层架构和幂等性原则。"
4|version: "6.0"
5|---
6|
7|# QA 工作技能
8|
9|## 📋 文档概述
10|
11|本文档描述了 QA 工作的核心技能和流程，包括测试分层架构、UT 单元测试、SIT 系统集成测试、UAT 用户验收测试、API 接口测试、测试报告管理等。本文档适用于任何项目的 QA 工作，使用占位符表示项目特定内容。
12|
13|**章节优先级**：本文档按照重要性和使用频率组织章节，**测试分层架构**和**TDD 验收标准**是最核心的高价值章节，放在最前面。
14|
15|---
16|
17|## 核心职责
18|
19|1. **UT 回归测试**：收集单元测试结果，统计测试覆盖率（Statement Coverage）
20|2. **SIT 系统集成测试**：实现测试框架、用例开发、执行测试、问题排查
21|3. **UAT 用户验收测试**：实现测试框架、用例开发、端到端测试
22|4. **API 接口测试**：根据 API 元数据文件和设计文档设计测试用例
23|5. **测试报告管理**：生成测试报告，反馈给架构师和开发专家
24|6. **问题排查与修复**：诊断测试失败原因，指导问题修复
25|
26|---
27|
28|## 🏗️ 测试分层架构（⭐ 核心高价值）
29|
30|### 测试金字塔模型
31|
32|```
33|                /\
34|               /  \
35|              / UAT \  ← 端到端业务场景（少量）
36|             /------\
37|            /  SIT   \  ← 系统集成测试（适中）
38|           /----------\
39|          /   API      \  ← 接口契约验证（较多）
40|         /--------------\
41|        /     UT          \  ← 单元测试（最多）
42|       /------------------\
43|```
44|
45|### 测试层级定义
46|
47|| 测试层级 | 测试目标 | 测试对象 | 执行频率 | 用例数 | 价值 |
48||---------|---------|----------|----------|--------|------|
49|| **UT** | 函数/方法正确性 | 单元代码 | 每次提交 | 最多 | ⭐⭐⭐ |
50|| **API** | 接口契约验证 | RESTful API | 每次提交 | 较多 | ⭐⭐⭐ |
51|| **SIT** | 系统集成正确性 | K8s + DB + Service | 每次发布前 | 适中 | ⭐⭐ |
52|| **UAT** | 端到端业务场景 | 完整用户流程 | 每次发布前 | 较少 | ⭐ |
53|
54|### 核心规则（强制执行）
55|
**规则 1：目录对应关系**
- ✅ **API 测试** → 参见 `archetype-config.yml` → `test.layers.api`
- ✅ **SIT 测试** → 参见 `archetype-config.yml` → `test.layers.sit`
- ✅ **UAT 测试** → 参见 `archetype-config.yml` → `test.layers.uat`
- ❌ **禁止**：将测试放在子目录中

**规则 2：回归测试必须完整执行**
```bash
# ✅ 正确：运行所有测试（命令见 archetype-config.yml）
# UT:    build_tool.commands.test
# API:   test.layers.api.run_command 或 test.layers.api.pytest_fallback.run_command
# SIT:   test.layers.sit.run_command 或 test.layers.sit.pytest_fallback.run_command
# UAT:   test.layers.uat.run_command 或 test.layers.uat.pytest_fallback.run_command

# ❌ 错误：只运行部分测试
{test_runner} tests/{test_layer}/test_single.py -v
```

**规则 3：禁止使用 --maxfail 提前终止**
76|- ❌ `{test_runner} tests/sit/ --maxfail=5` - 只跑 5 个测试就停止
77|- ✅ `{test_runner} tests/sit/ -v` - 完整执行所有测试用例
78|
79|### 为什么分层测试如此重要？
80|
81|**1. 快速失败，快速修复**
82|- UT 层：问题在开发阶段立即发现，修复成本最低
83|- API 层：接口问题在集成前发现
84|- SIT/UAT 层：系统性问题在发布前发现
85|
86|**2. 测试金字塔的经济效益**
87|```
88|        成本
89|         ↑
90|      UAT |    ▓▓▓▓ (高成本，少量)
91|         |   ▓▓▓
92|      SIT |  ▓▓▓▓▓▓ (中成本，适中)
93|         | ▓▓▓▓
94|     API  |▓▓▓▓▓▓▓▓ (低成本，较多)
95|         |▓▓▓
96|      UT  |▓▓▓▓▓▓▓▓▓▓ (极低成本，最多)
97|         +---------------------→ 时间
98|```
99|
100|**3. 各层级的不可替代性**
101|- UT 无法替代 SIT：Mock 无法发现集成问题
102|- SIT 无法替代 UAT：测试环境无法模拟真实用户场景
103|- UAT 无法替代 UT：端到端测试无法定位具体函数错误
104|
105|---
106|
107|## TDD 验收标准（⭐ 核心高价值）
108|
109|### 🟢 绿灯（通过）
110|- ✅ 所有 SIT 测试通过
111|- ✅ 所有 UAT 核心测试通过
112|- ✅ 核心指标计算误差 < 1%
113|- ✅ 无 P0/P1 级 Bug
114|
115|**可以发布**：产品质量良好，用户体验符合预期。
116|
117|### 🟡 黄灯（有条件通过）
118|- ⚠️ SIT 测试通过率 70-89%
119|- ⚠️ 核心功能可用，但存在问题
120|- ⚠️ 存在 P1 级 Bug（不影响核心功能）
121|
122|**可以发布，但需要**：
123|- 明确已知问题清单
124|- 制定修复计划
125|- 监控生产环境表现
126|
127|### 🔴 红灯（不通过）
128|- ❌ SIT 测试失败（无法连接基础服务）
129|- ❌ 核心指标计算错误（误差 > 5%）
130|- ❌ 存在 P0 级 Bug（数据丢失、事件丢失）
131|- ❌ 通过率 < 70%
132|
133|**禁止发布**：必须修复所有 P0 级问题后重新测试。
134|
135|### TDD 的核心价值
136|
137|**1. 驱动设计，而不仅是验证**
138|```python
139|# TDD 流程：红 → 绿 → 重构
140|
141|# Step 1: 写测试（红灯）
142|def test_user_login():
143|    result = login("user", "pass")
144|    assert result.success == True
145|    assert result.token is not None
146|
147|# Step 2: 实现功能（绿灯）
148|def login(username, password):
149|    # 最简实现，让测试通过
150|    return LoginResult(success=True, token="abc123")
151|
152|# Step 3: 重构优化
153|def login(username, password):
154|    # 真实实现，但测试仍然通过
155|    if authenticate(username, password):
156|        token = generate_token(username)
157|        return LoginResult(success=True, token=token)
158|    return LoginResult(success=False, token=None)
159|```
160|
161|**2. 文档即测试**
162|- 测试用例 = 活的文档
163|- 展示 API 的预期行为
164|- 新人可以通过测试理解系统
165|
166|**3. 重构的安全网**
167|- 有测试保护的重构 = 安全
168|- 无测试的重构 = 赌博
169|
170|---
171|
172|## 📋 测试用例设计原则（⭐ 高价值）
173|
174|### ⚠️ 核心原则：测试用例必须体现设计文档意图
175|
176|**设计文档是测试用例的唯一真实来源**：
177|
178|1. **API 测试用例**必须基于 API 元数据文件
179|   - ✅ 每个端点都有测试用例
180|   - ✅ 每个参数都测试（required、optional、default）
181|   - ✅ 每个响应字段都验证
182|   - ❌ 禁止凭空想象测试场景
183|
184|**说明**:
185|- API 元数据文件：`.api`、OpenAPI/Swagger、GraphQL schema、protobuf 等
186|
187|2. **SIT 测试用例**必须基于设计文档
188|   - ✅ 覆盖设计文档中的业务流程
189|   - ✅ 验收标准符合设计文档的功能要求
190|   - ❌ 禁止遗漏关键功能
191|
192|3. **UAT 测试用例**必须基于 PRD 文档
193|   - ✅ 端到端场景覆盖用户故事
194|   - ✅ 验收标准符合用户需求
195|   - ❌ 禁止偏离验收标准
196|
197|### 测试用例过期的判断标准
198|
199|**如果出现以下情况，说明测试用例已过期**：
200|1. ❌ API 元数据文件定义了新端点，但测试缺少对应测试
201|2. ❌ 设计文档添加了新功能，但测试未覆盖
202|3. ❌ API 响应结构变化，但测试仍验证旧字段
203|4. ❌ 新增了可选参数，但测试未验证其行为
204|
205|### 测试用例设计方法论
206|
207|**边界值分析**：
208|```python
209|# ❌ 错误：只测试正常值
210|def test_age_validation():
211|    assert validate_age(25) == True
212|
213|# ✅ 正确：测试边界值
214|def test_age_validation():
215|    assert validate_age(-1) == False    # 最小值外
216|    assert validate_age(0) == True     # 最小值
217|    assert validate_age(18) == True    # 正常值
218|    assert validate_age(150) == True   # 最大值
219|    assert validate_age(151) == False  # 最大值外
220|```
221|
222|**等价类划分**：
223|```python
224|# ✅ 每个等价类至少一个测试用例
225|def test_username_validation():
226|    # 有效等价类
227|    assert validate_username("abc") == True
228|    assert validate_username("user123") == True
229|    
230|    # 无效等价类
231|    assert validate_username("") == False      # 空字符串
232|    assert validate_username("a") == False     # 太短
233|    assert validate_username("a"*100) == False # 太长
234|    assert validate_username("123") == False   # 纯数字
235|```
236|
237|---
238|
239|## 🧹 测试幂等性策略（⭐ 高价值）
240|
241|### ⚠️ 核心原则：测试必须可重复执行，结果一致
242|
243|**幂等性定义**：测试可以重复执行任意次数，每次结果相同。
244|
245|### 测试策略四阶段
246|
247|**1. 测试开始前：全局清理**
248|```python
249|@pytest.fixture(scope="session", autouse=True)
250|def global_cleanup(k8s_client, db_connection):
251|    """测试会话级别的全局清理（幂等操作）"""
252|    print("🧹 全局清理：清理所有 test-* 数据")
253|    clean_k8s_resources(k8s_client, "test-")
254|    clean_db_data(db_connection, "test-%")
255|    yield
256|    clean_k8s_resources(k8s_client, "test-")
257|    clean_db_data(db_connection, "test-%")
258|```
259|
260|**2. 数据准备阶段：独立命名**
261|```python
262|@pytest.mark.sit
263|def test_sit_002_pod_add_event():
264|    pod_name = "test-sit-002-pod-add"  # ✅ 独立命名，避免冲突
265|```
266|
267|**命名规范**：
268|- 格式：`test-{layer}-{编号}-{用途描述}`
269|- 示例：`test-sit-002-pod-add`, `test-uat-001-lifecycle`
270|
271|**3. 测试执行阶段：数据隔离**
272|```python
273|# ✅ 正确：每个测试用例使用独立名称
274|def test_case_001():
275|    name = "test-case-001-action"
276|
277|def test_case_002():
278|    name = "test-case-002-action"
279|
280|# ❌ 错误：多个测试用例共享名称
281|def test_case_001(shared_name):  # 共享 fixture → 冲突！
282|```
283|
284|**4. 测试结束后：自动清理**
285|```python
286|@pytest.fixture(scope="session", autouse=True)
287|def global_cleanup(k8s_client, db_connection):
288|    yield
289|    # ✅ 自动清理所有 test-* 数据（无论测试成功/失败）
290|    clean_k8s_resources(k8s_client, "test-")
291|    clean_db_data(db_connection, "test-%")
292|```
293|
294|### 幂等性保障机制
295|
296|| 保护机制 | 作用范围 | 实现方式 |
297||---------|---------|----------|
298|| **全局清理** | 测试会话级别 | `global_cleanup` fixture（scope="session"） |
299|| **K8s 清理** | 集群资源 | 清理所有 test-* 资源 |
300|| **数据库清理** | 持久化数据 | 清理所有 test-% 记录 |
301|| **独立命名** | 测试用例级别 | 每个用例使用独立名称 |
302|
303|### 为什么幂等性如此重要？
304|
305|**1. CI/CD 的可靠性**
306|- 非幂等测试：偶尔失败，难以排查
307|- 幂等测试：要么总是通过，要么总是失败
308|
309|**2. 开发效率**
310|```bash
311|# ❌ 非幂等：每次运行前需要手动清理
312|rm -rf test_data/*
313|pytest tests/sit/
314|
315|# ✅ 幂等：直接运行，自动清理
316|pytest tests/sit/
317|```
318|
319|**3. 并行测试的基础**
320|- 幂等测试可以并行执行
321|- 非幂等测试必须串行执行
322|
323|---
324|
325|## 🔐 数据准备幂等性原则（⭐ 高价值）
326|
327|### ⚠️ 核心原则：测试数据清理必须完整且幂等
328|
329|**数据准备阶段必须保证**：
330|1. **完整性**：清理所有相关表的数据，避免残留影响测试结果
331|2. **幂等性**：测试可以重复执行，每次都从干净状态开始
332|3. **原子性**：使用事务保证多表操作的原子性
333|
334|### 正确实现
335|
336|**✅ 完整清理（所有相关表）**：
337|```python
338|def _cleanup_test_data(db_connection, pattern):
339|    """清理测试数据（使用事务）"""
340|    with db_connection.cursor() as cur:
341|        try:
342|            cur.execute("BEGIN")
343|            # 删除顺序：子表 → 主表（考虑外键依赖）
344|            cur.execute("DELETE FROM {child_table} WHERE name LIKE %s", (pattern,))
345|            cur.execute("DELETE FROM {main_table} WHERE name LIKE %s", (pattern,))
346|            db_connection.commit()
347|        except Exception as e:
348|            db_connection.rollback()
349|            raise e
350|```
351|
352|**说明**:
353|- `{child_table}`: 子表名称
354|- `{main_table}`: 主表名称
355|
356|**✅ 幂等操作（测试前后都清理）**：
357|```python
358|@pytest.fixture(autouse=True)
359|def cleanup_test_data(db_connection, test_pattern):
360|    # 测试前清理：确保环境干净（关键！）
361|    _cleanup_test_data(db_connection, test_pattern)
362|    yield
363|    # 测试后清理：避免数据残留
364|    _cleanup_test_data(db_connection, test_pattern)
365|```
366|
367|### 关键检查清单
368|
369|- [ ] **测试前清理**：fixture 在 yield 之前执行
370|- [ ] **测试后清理**：fixture 在 yield 之后执行
371|- [ ] **完整清理**：清理所有相关表
372|- [ ] **使用事务**：多表操作使用 BEGIN/COMMIT/ROLLBACK
373|- [ ] **考虑外键**：按照依赖顺序删除（子表 → 主表）
374|
375|---
376|
377|## ⚡ pytest xfail/xpassed 实践策略
378|
379|### 核心概念
380|
381|**xfail（expected failure）**：标记预期失败的测试，用于未实现功能、已知 Bug、环境限制等场景
382|
383|**xpassed（unexpectedly passed）**：标记为 xfail 但实际通过，表明功能已实现或环境已修复
384|
385|### TDD 实践策略
386|
387|**何时使用 xfail**：
388|- 测试先行：先写测试标记 xfail，再实现功能
389|- 依赖阻塞：等待外部依赖修复
390|- 环境限制：当前环境无法支持
391|
392|**xpassed 处理流程**：
393|1. 识别 xpassed 测试：`pytest -v | grep XPASS`
394|2. 验证功能正确性
395|3. 移除 xfail 标记
396|4. 更新测试基线
397|
398|### 最佳实践
399|
400|**DO**：
401|- ✅ reason 清晰：说明预期失败的根本原因
402|- ✅ 及时清理：xpassed 后立即移除标记
403|- ✅ 定期审查：避免 xfail 过期失去跟踪价值
404|
405|**DON'T**：
406|- ❌ 滥用掩盖：不要用 xfail 掩盖应该修复的问题
407|- ❌ 模糊描述：避免 "todo"、"待修复" 等无意义 reason
408|- ❌ 长期遗留：定期处理 xpassed，保持测试准确性
409|
410|### 统计规则
411|
412|**通过率计算**：`pass_rate = (passed + xpassed) / total_tests`
413|
414|**质量指标**：
415|- xpassed 数量反映功能实现进度
416|- 及时处理 xpassed 保持测试有效性
417|
418|---
419|
420|## 🔄 重构测试标准化流程（铁律）
421|
422|### ⚠️ 核心原则：基线对比法
423|
424|**重构测试必须遵循"基线对比法"**：
425|1. 先验证环境可用
426|2. 建立测试基线
427|3. 执行重构
428|4. 回归测试对比
429|5. 判断问题来源（环境 vs 重构）
430|
431|### 标准流程（四阶段）
432|
433|**阶段 1: 重构前准备** ✅
434|```bash
435|# 验证环境 + 建立测试基线
436|cd {deploy_path} && {compose_command} up -d --build
437|cd ../..
438|{test_command} > logs/baseline.log 2>&1
439|{test_runner} tests/ -v --html=test_reports/baseline.html
440|```
441|
442|**阶段 2: 执行重构** ✅
443|```bash
444|# 使用 git mv 保留历史
445|git mv old_path new_path
446|# 更新 import 路径...
447|git commit -m "refactor: xxx"
448|```
449|
450|**阶段 3: 回归测试** ✅
451|```bash
452|# 重新构建环境
453|cd {deploy_path} && {compose_command} up -d --build && cd ../..
454|# 回归测试
455|{test_command} > logs/refactor.log 2>&1
456|{test_runner} tests/ -v --html=test_reports/refactor.html
457|# 对比基线 vs 重构后的通过率
458|```
459|
460|**阶段 4: 问题诊断** ✅
461|```bash
462|# 判断问题来源的决策树
463|if [ 重构后测试失败 ]; then
464|    if [ 基线测试也失败 ]; then
465|        echo "❌ 环境问题：测试前基线就有问题"
466|    else
467|        echo "❌ 重构问题：基线通过，重构后失败"
468|    fi
469|fi
470|```
471|
472|### 关键检查清单
473|
474|- [ ] **基线建立**：重构前先运行测试并记录结果
475|- [ ] **环境验证**：确认容器运行最新代码（`--build`）
476|- [ ] **数据库清理**：测试前清理数据库
477|- [ ] **完整测试**：不使用 `--maxfail`，执行所有测试用例
478|- [ ] **结果对比**：基线 vs 重构后，通过率差异 ≤5%
479|- [ ] **问题归因**：明确区分"环境问题"和"重构问题"
480|
481|---
482|
483|## 🐛 问题排查方法论（铁律）
484|
485|### ⚠️ 核心原则：代码优先原则
486|
487|**"代码是唯一的真实来源"**：
488|1. 遇到问题先阅读代码，理解逻辑
489|2. 再检查配置，是否符合代码预期
490|3. 最后修改配置，而不是"试错"
491|
492|### 问题排查三步法
493|
494|**Step 1: 阅读代码，理解逻辑** ✅
495|```bash
496|# 示例：配置问题排查
497|vim {source_code_path}
498|# 理解配置优先级、处理逻辑
499|```
500|
501|