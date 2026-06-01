
1|---
2|name: refactor
3|description: 安全重构代码技能。当用户明确提到"重构"、"代码异味"或需要在TDD循环的Refactor阶段改进代码内部结构时使用。
4|
5|**仅在以下场景触发**：
6|- 用户明确说"重构这段代码"
7|- 提到"代码异味"并要求改进
8|- TDD循环中的Refactor阶段
9|- 代码审查中需要改进内部结构但不改变功能
10|
11|**注意**：如果用户只是说"优化"、"改进"或"提升性能"，不应该触发此技能，除非明确提到"重构"。
12|---
13|
14|# 安全重构
15|
16|## 核心原则
17|
18|**重构不改变外部行为，只改进内部结构。测试必须始终通过。**
19|
20|重构是改善既有代码设计的过程，通过一系列小的、保持行为的变换来提高代码质量。每个变换都称为一个"重构"。
21|
22|## 何时重构
23|
24|### 在 TDD 循环中
25|- 每个 Red-Green 循环后都应检查是否需要重构
26|- 不要等到代码"完全烂掉"才重构
27|- 小步重构，频繁重构
28|
29|### 重构信号（Code Smells）
30|
31|**快速识别代码需要重构的信号**：
32|
33|#### 1. 重复代码
34|相同的代码片段出现在多个地方 → 提取公共函数
35|
36|#### 2. 函数过长
37|函数超过 20-30 行，包含多个抽象层次 → 提取小函数
38|
39|#### 3. 魔法数字
40|代码中出现未命名的数字 → 使用命名常量
41|
42|#### 4. 参数过多
43|函数参数超过 3-4 个 → 引入参数对象
44|
45|#### 5. 深层嵌套
46|条件嵌套超过 3 层 → 使用卫语句（Guard Clauses）
47|
48|**💡 详细说明**：参见 [references/code-smells.md](references/code-smells.md)
49|
50|## 重构技术速查
51|
52|### 1. 提取函数（Extract Function）
53|
54|**何时使用**：函数过长，代码片段可以被独立命名
55|
56|```go
57|// 重构前
58|func PrintOwing(invoice *Invoice) {
59|    printBanner()
60|    outstanding := 0.0
61|    for _, order := range invoice.Orders {
62|        outstanding += order.Amount
63|    }
64|    fmt.Printf("未付金额: %.2f\n", outstanding)
65|}
66|
67|// 重构后
68|func PrintOwing(invoice *Invoice) {
69|    printBanner()
70|    outstanding := calculateOutstanding(invoice)
71|    fmt.Printf("未付金额: %.2f\n", outstanding)
72|}
73|```
74|
75|### 2. 内联函数（Inline Function）
76|
77|**何时使用**：函数体和函数名一样清晰
78|
79|```go
80|// 重构前
81|func getRating(driver *Driver) int {
82|    return moreThanFiveLateDeliveries(driver) ? 2 : 1
83|}
84|
85|// 重构后
86|func getRating(driver *Driver) int {
87|    return driver.NumberOfLateDeliveries > 5 ? 2 : 1
88|}
89|```
90|
91|### 3. 提取变量（Extract Variable）
92|
93|**何时使用**：表达式难以理解
94|
95|```go
96|// 重构前
97|func Price(order *Order) float64 {
98|    return order.Quantity*order.ItemPrice -
99|        max(0, order.Quantity-500)*order.ItemPrice*0.05
100|}
101|
102|// 重构后
103|func Price(order *Order) float64 {
104|    basePrice := order.Quantity * order.ItemPrice
105|    quantityDiscount := max(0, order.Quantity-500) * order.ItemPrice * 0.05
106|    return basePrice - quantityDiscount
107|}
108|```
109|
110|### 4. 重命名（Rename）
111|
112|**何时使用**：命名不能准确表达意图
113|
114|```go
115|// 重构前
116|func calc(u *User) float64 { ... }
117|
118|// 重构后
119|func calculateTotalOrderAmount(user *User) float64 { ... }
120|```
121|
122|### 5. 卫语句（Guard Clauses）
123|
124|**何时使用**：深层嵌套的条件
125|
126|```go
127|// 重构前：嵌套条件
128|func GetPayAmount(employee *Employee) float64 {
129|    var result float64
130|    if employee.IsSeparated {
131|        result = 0
132|    } else {
133|        if employee.IsRetired {
134|            result = 0
135|        } else {
136|            result = employee.Salary
137|        }
138|    }
139|    return result
140|}
141|
142|// 重构后：卫语句
143|func GetPayAmount(employee *Employee) float64 {
144|    if employee.IsSeparated {
145|        return 0
146|    }
147|    if employee.IsRetired {
148|        return 0
149|    }
150|    return employee.Salary
151|}
152|```
153|
154|**💡 详细技术**：参见 [references/techniques.md](references/techniques.md)
155|
156|## 标准重构流程
157|
158|### 步骤 1：确保测试通过
159|
160|```bash
161|go test ./... -v
162|```
163|
164|所有测试必须是绿色的才能开始重构
165|
166|### 步骤 2：进行小的重构
167|
168|- 一次只做一个小改动
169|- 例如：只重命名一个变量，或只提取一个函数
170|
171|### 步骤 3：运行测试
172|
173|```bash
174|go test ./... -v
175|```
176|
177|确保重构没有破坏功能
178|
179|### 步骤 4：提交（可选）
180|
181|```bash
182|git add .
183|git commit -m "refactor: 提取 validateEmail 函数"
184|```
185|
186|### 步骤 5：重复步骤 2-4
187|
188|继续下一个小的重构
189|
190|## 重构检查清单
191|
192|每次重构后检查：
193|
194|- [ ] 所有测试都通过
195|- [ ] 代码更易读
196|- [ ] 没有引入新的复杂性
197|- [ ] 没有改变外部行为
198|- [ ] 函数/变量命名更清晰
199|- [ ] 消除了重复代码
200|- [ ] 降低了耦合度
201|
202|## 重构原则
203|
204|### DO（应该做）
205|
206|✓ **小步重构** - 每次只改一个地方
207|✓ **频繁测试** - 每次改动后都运行测试
208|✓ **保持绿灯** - 重构过程中测试必须始终通过
209|✓ **改善命名** - 好的命名是最好的文档
210|✓ **消除重复** - DRY (Don't Repeat Yourself)
211|✓ **简化逻辑** - 能用简单方法就不用复杂方法
212|
213|### DON'T（不应该做）
214|
215|✗ **不要同时重构和添加功能** - 一次只做一件事
216|✗ **不要在红灯时重构** - 测试失败时先让测试通过
217|✗ **不要大规模重构** - 避免一次改动太多代码
218|✗ **不要盲目重构** - 确保重构有明确目的
219|✗ **不要过度设计** - 不要为未来可能不会发生的需求重构
220|
221|## 输出格式
222|
223|重构时输出：
224|
225|```
226|♻️  重构：[重构内容简述]
227|   原因：[为什么需要重构]
228|   技术：[使用的重构技术]
229|
230|✓ 运行测试
231|   结果：PASS（X个测试，耗时 Yms）
232|
233|✓ 重构完成
234|   改进：[具体改进说明]
235|```
236|
237|## 示例输出
238|
239|```
240|♻️  重构：提取邮箱验证逻辑
241|   原因：RegisterUser 和 UpdateUserEmail 中存在重复的验证代码
242|   技术：Extract Function
243|
244|   提取前：2 处重复，共 8 行代码
245|   提取后：1 个函数 validateEmail，被 2 处调用
246|
247|✓ 运行测试
248|   命令：go test ./internal/user -v
249|   结果：PASS
250|   覆盖率：85.2%
251|
252|✓ 重构完成
253|   改进：
254|   - 消除了 8 行重复代码
255|   - 提高了可维护性（邮箱验证逻辑集中在一处）
256|   - 测试覆盖率保持不变
257|```
258|
259|## 何时停止重构
260|
261|满足以下条件即可停止当前重构：
262|
263|1. ✓ 代码清晰易读，意图明确
264|2. ✓ 没有明显的代码异味
265|3. ✓ 函数职责单一，长度适中（< 30 行）
266|4. ✓ 没有重复代码
267|5. ✓ 命名准确描述了意图
268|6. ✓ 所有测试通过
269|
270|**记住**：重构是持续的过程，不要追求一次性完美。每个 TDD 循环做一点改进即可。
271|
272|## 更多资源
273|
274|### 📚 完整示例
275|
276|真实场景的重构案例：
277|- [examples.md](examples.md) - 端到端重构示例
278|
279|### 📖 详细参考
280|
281|深入理解重构：
282|- [references/code-smells.md](references/code-smells.md) - 代码异味详解
283|- [references/techniques.md](references/techniques.md) - 重构技术详解
284|
285|### 📖 参考资料
286|
287|- 《重构：改善既有代码的设计》- Martin Fowler
288|- TDD 循环 (tdd-cycle skill)
289|- 测试优先 (test-first skill)
290|- SOLID 原则
291|