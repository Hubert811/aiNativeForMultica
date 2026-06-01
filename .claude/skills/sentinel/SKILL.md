
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
2|name: sentinel
3|description: 线上服务哨兵 - 定期巡检、功能回归测试、数据质量验证和根因分析（RCA）。当用户明确提到"巡检"、"回归测试"、"健康检查"、"线上分析"、"RCA"或需要验证生产环境服务健康时使用。
4|
5|**仅在以下场景触发**：
6|- 用户明确说"执行巡检"、"运行回归测试"
7|- 提到"健康检查"、"服务可用性验证"
8|- 需要进行线上数据质量分析
9|- 生产环境问题诊断和根因分析
10|- 定期监控和预防性检查
11|
12|**注意**：如果用户只是说"测试"、"检查"等泛化词汇，不应该触发此技能，除非明确提到巡检、回归或健康检查。
13|---
14|
15|# Sentinel - 线上服务哨兵
16|
17|## 核心职责
18|
19|Sentinel 是线上服务的守护者，负责：
20|- 🛡️ **定期巡检** - Smoke/Sanity/Full 三级巡检
21|- ✅ **功能回归测试** - API 测试和数据质量验证
22|- 🔍 **健康分析** - 服务可用性和数据完整性检查
23|- 🎯 **根因分析** - 问题诊断和 RCA（Root Cause Analysis）
24|
25|## 快速开始
26|
27|### 一键执行（推荐）
28|
29|```bash
30|# 健全性测试生产环境
31|./.claude/skills/sentinel/scripts/inspect sanity prod
32|
33|# 冒烟测试测试环境
34|./.claude/skills/sentinel/scripts/inspect smoke test
35|
36|# 完整测试生产数据
37|./.claude/skills/sentinel/scripts/inspect full prod
38|```
39|
40|**参数说明**：
41|- `LEVEL`：巡检级别（smoke / sanity / full）
42|- `ENV`：环境（dev / test / prod）
43|- **默认值**：`sanity test`
44|
45|**中文支持**：
46|```bash
47|./.claude/skills/sentinel/scripts/inspect 健全 生产
48|./.claude/skills/sentinel/scripts/inspect 冒烟 测试环境
49|./.claude/skills/sentinel/scripts/inspect 完整 生产
50|```
51|
52|## 巡检级别
53|
54|> 回归测试覆盖度：**smoke < sanity < full**
55|
56|### Smoke（每小时）
57|- **执行时间**：< 30 秒
58|- **覆盖范围**：
59|  - API 健康检查（/healthz）
60|  - 资源查询（空结果、带条件、分页）
61|  - 错误场景处理（无效时间范围、无效 Pod 名称）
62|
63|### Sanity（每天）
64|- **执行时间**：< 5 分钟
65|- **覆盖范围**：
66|  - 所有 Smoke 测试
67|  - 时区一致性验证
68|  - 元数据完整性验证
69|  - 数据质量评分（>= 70%）
70|  - 索引完整性和数据一致性
71|
72|### Full（每周）
73|- **执行时间**：< 10 分钟
74|- **覆盖范围**：
75|  - 所有 Sanity 测试
76|  - 计算准确性验证（误差 < 1%）
77|  - 多次启停周期验证
78|  - 维度覆盖率验证
79|
80|## 巡检报告
81|
82|**存储位置**：`test_reports/inspection/`
83|
84|**文件命名**：`inspection_smoke_dev_YYYYMMDD_HHMMSS.md`
85|
86|**报告格式**：纯 Markdown（易于阅读、分享和版本管理）
87|
88|**报告内容**：
89|- 📊 测试结果摘要（总数、通过、失败、错误、跳过、通过率）
90|- 🔧 环境信息（API URL、数据库连接）
91|- 📋 测试套件详情
92|- ❌ 失败测试详情（失败原因和详细错误）
93|- ✅ 通过测试列表
94|
95|## 验收标准
96|
97|### Smoke 巡检
98|- [ ] 所有 API Smoke Tests 通过
99|- [ ] API 响应时间 < 2 秒
100|
101|### Sanity 巡检
102|- [ ] 数据质量评分 >= 70%
103|- [ ] 无严重 Bug（时区错误、GPU 负值）
104|- [ ] 索引完整
105|
106|### Full 巡检
107|- [ ] GPU 计算误差 < 1%
108|- [ ] 维度覆盖率 >= 95%
109|
110|## 故障排查
111|
112|### 数据库连接失败
113|```bash
114|# 检查环境变量
115|echo $DB_HOST
116|echo $DB_PORT
117|
118|# 手动测试连接
119|psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME
120|```
121|
122|### API 请求失败
123|```bash
124|# 检查 API URL
125|echo $RESOURCE_METER_API_URL
126|
127|# 手动测试 API
128|curl $RESOURCE_METER_API_URL/healthz
129|```
130|
131|## GitLab CI 集成
132|
133|本技能可以通过 GitLab CI Scheduled Pipeline 定期执行。
134|
135|**定时任务**：
136|- **冒烟测试**：每小时（Cron: `0 * * * *`）
137|- **健全性测试**：每天凌晨 2 点（Cron: `0 2 * * *`）
138|- **完整测试**：每周日凌晨 2 点（Cron: `0 2 * * 0`）
139|
140|## 配置管理
141|
142|### 使用 .env.skill（推荐 ✨）
143|
144|**配置文件**：项目根目录的 `.env.skill`
145|
146|**优势**：
147|- ✅ 最安全：敏感信息与代码分离
148|- ✅ 已在 `.gitignore` 中，不会被提交到 Git
149|- ✅ 统一管理：所有环境变量集中存储
150|- ✅ 项目级配置：所有技能共享
151|
152|**首次配置**：
153|```bash
154|# 在项目根目录创建 .env.skill
155|cat > .env.skill << 'EOF'
156|# Sentinel 配置 - 测试环境
157|export TEST_API_URL="http://192.168.25.200:30882/api/v1"
158|export TEST_DB_HOST="192.168.25.200"
159|export TEST_DB_PORT="32432"
160|export TEST_DB_NAME="event_db"
161|export TEST_DB_USER="postgres"
162|export TEST_DB_PASSWORD="your-password"
163|export TEST_KUBECONFIG="/path/to/kubeconfig"
164|
165|# Sentinel 配置 - 生产环境
166|export PROD_API_URL="http://192.168.25.23:31676/api/v1"
167|export PROD_DB_HOST="192.168.25.200"
168|export PROD_DB_PORT="32432"
169|export PROD_DB_NAME="event_db"
170|export PROD_DB_USER="postgres"
171|export PROD_DB_PASSWORD="your-password"
172|export PROD_KUBECONFIG="/path/to/kubeconfig"
173|EOF
174|
175|# 设置权限（仅当前用户可读写）
176|chmod 600 .env.skill
177|```
178|
179|**验证配置**：
180|```bash
181|# 检查环境变量是否正确加载
182|source .env.skill
183|echo $TEST_API_URL
184|echo $TEST_DB_HOST
185|
186|# 执行巡检
187|./.claude/skills/sentinel/scripts/inspect sanity test
188|```
189|
190|### KUBECONFIG 控制
191|
192|```bash
193|# 默认：从 .env.skill 读取
194|./.claude/skills/sentinel/scripts/inspect sanity test
195|
196|# 禁用 KUBECONFIG（跳过所有需要 K8s 的测试）
197|KUBECONFIG="DISABLED" ./.claude/skills/sentinel/scripts/inspect sanity prod
198|
199|# 自定义 KUBECONFIG 路径
200|KUBECONFIG="/custom/path/kubeconfig" ./.claude/skills/sentinel/scripts/inspect sanity test
201|```
202|
203|### 配置参考
204|
205|配置模板位于：`.claude/skills/sentinel/config/inspection_config.yaml.template`
206|
207|如需查看完整的配置结构和说明，请参考模板文件。
208|
209|## 架构说明
210|
211|Resource Meter 采用**服务和数据库分离部署**架构：
212|- **服务部署**：在 prod K8s 集群，通过 NodePort Service 暴露
213|- **数据库部署**：在 test K8s 集群，测试环境和生产环境共享同一个数据库实例
214|
215|## 更多资源
216|
217|### 脚本说明
218|- `scripts/inspect` - 主执行脚本（简化命令）
219|- `scripts/run_inspection.sh` - 完整巡检脚本
220|- `scripts/generate_md_report.py` - Markdown 报告生成器
221|- `scripts/load_config.sh` - 配置加载脚本（从 .env.skill）
222|
223|### 配置文件
224|- `config/inspection_config.yaml.template` - 配置模板（参考文档）
225|- `.env.skill` - 实际配置文件（项目根目录，需本地创建）
226|