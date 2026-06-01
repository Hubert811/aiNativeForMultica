1|---
2|skill: "devops"
3|description: "DevOps 工作技能 - CI/CD 流程、容器化构建、Kubernetes 部署、基础设施即代码、监控告警。当用户提到部署、容器化、K8s、Helm、ArgoCD、CI/CD、监控、日志、或需要执行部署、排查线上问题时，必须使用此技能。"
4|version: "2.0"
5|---
6|
7|# DevOps 工作技能
8|
9|## 核心职责
10|
11|1. **CI/CD 流程管理**：设计、维护持续集成与持续部署流水线
12|2. **容器化构建**：Docker 镜像构建、优化与发布管理
13|3. **Kubernetes 部署**：应用容器编排、配置管理、滚动更新
14|4. **基础设施即代码**：使用 Terraform/Helm 管理基础设施
15|5. **监控与告警**：配置监控体系、日志聚合、健康检查
16|6. **安全最佳实践**：镜像扫描、Secret 管理、RBAC 权限控制
17|
18|---
19|
20|## ⚠️ 行为准则（铁律）
21|
22|### 准则 1：配置即代码，必须版本化管理
23|
24|- ✅ 所有配置文件纳入 Git 版本控制
25|- ✅ 环境特定配置使用独立的 values 文件
26|- ❌ 禁止在生产环境手动修改配置
27|- ❌ 禁止在配置文件中硬编码密码
28|
29|### 准则 2：镜像标签必须可追溯
30|
31|- ✅ 生产环境使用语义化版本标签（v1.0.0）
32|- ✅ CI/CD 使用 Git Commit SHA 作为标签
33|- ❌ 禁止在生产环境使用 latest 标签
34|- ❌ 禁止使用无意义的标签（如 test、temp）
35|
36|### 准则 3：部署必须可回滚
37|
38|- ✅ Helm 部署保留历史版本（`--history-max`）
39|- ✅ 配置文件变更前必须备份
40|- ✅ 数据库变更必须有回滚脚本
41|- ❌ 禁止执行无法回滚的变更
42|
43|### 准则 4：生产环境部署必须有人工确认
44|
45|- ✅ 生产环境部署使用手动触发（when: manual）
46|- ✅ 部署前通知相关人员
47|- ✅ 部署时必须有人值守监控
48|- ❌ 禁止自动部署到生产环境
49|
50|### 准则 5：监控与告警必须覆盖关键指标
51|
52|- ✅ 配置健康检查（liveness/readiness probe）
53|- ✅ 监控核心资源（CPU/Memory/磁盘）
54|- ✅ 配置告警规则（Pod Crash/内存泄漏/API 错误率）
55|- ✅ 日志集中收集（ELK/Loki）
56|
57|### 准则 6：敏感信息必须加密存储
58|
59|- ✅ 密码使用 Secret/External Secrets/Vault
60|- ✅ Git 仓库中禁止明文密码
61|- ✅ 生产环境密钥定期轮换
62|- ❌ 禁止在配置文件中明文存储密码
63|
64|---
65|
66|## ⚠️ 常见错误（铁律）
67|
68|### ❌ 错误 1：直接修改生产环境配置
69|
70|```bash
71|# ❌ 错误：直接在线上修改配置
72|kubectl edit configmap myapp -n production
73|
74|# ✅ 正确：修改配置文件，通过 CI/CD 部署
75|vim values-prod.yaml
76|git add values-prod.yaml
77|git commit -m "feat: update config"
78|```
79|
80|### ❌ 错误 2：使用 latest 标签部署生产环境
81|
82|```bash
83|# ❌ 错误：无法追溯版本
84|docker pull registry.example.com/app:latest
85|
86|# ✅ 正确：使用语义化版本
87|docker pull registry.example.com/app:v1.0.0
88|```
89|
90|### ❌ 错误 3：配置文件中硬编码密码
91|
92|```yaml
93|# ❌ 错误：明文密码
94|database:
95|  password: "my-secret-password"
96|
97|# ✅ 正确：引用 Secret
98|database:
99|  password: {{ .Values.db.password | quote }}
100|```
101|
102|### ❌ 错误 4：不制定执行计划直接操作
103|
104|```bash
105|# ❌ 错误：直接执行 helm upgrade
106|helm upgrade --install app ./chart
107|
108|# ✅ 正确：先制定计划，确认后执行
109|helm get values myapp
110|helm diff upgrade myapp ./chart
111|helm upgrade --install app ./chart
112|```
113|
114|---
115|
116|## 工作流程（通用模板）
117|
118|### 部署前检查清单
119|
120|- [ ] 阅读项目 CLAUDE.md 理解部署架构
121|- [ ] 定位并分析 CI/CD 配置文件
122|- [ ] 制定执行计划（环境信息、风险识别、执行步骤）
123|- [ ] 备份当前配置（Helm values/K8s manifests）
124|- [ ] 在测试环境验证变更
125|- [ ] 确认回滚策略可用
126|
127|### 部署执行流程
128|
129|1. **环境准备**
130|   ```bash
131|   kubectl cluster-info
132|   helm version
133|   ```
134|
135|2. **配置更新**
136|   ```bash
137|   vim values-prod.yaml
138|   git add values-prod.yaml
139|   git commit -m "feat: update config"
140|   ```
141|
142|3. **构建镜像**
143|   ```bash
144|   # CI/CD 自动构建并推送镜像
145|   # 或手动构建（开发环境）
146|   docker build -t app:v1.0.0 .
147|   ```
148|
149|4. **执行部署**
150|   ```bash
151|   # Helm 部署
152|   helm upgrade --install app ./chart -f values-prod.yaml
153|   # 或 ArgoCD 自动同步（GitOps 模式）
154|   ```
155|
156|5. **验证部署**
157|   ```bash
158|   kubectl get pods -n production
159|   kubectl logs -f deployment/app -n production
160|   ```
161|
162|### 故障排查流程
163|
164|1. **问题定位**
165|   ```bash
166|   kubectl get pods -n <namespace>
167|   kubectl describe pod <pod-name>
168|   kubectl logs <pod-name>
169|   ```
170|
171|2. **常见问题**
172|   - **ImagePullBackOff**：镜像不存在或权限不足
173|   - **CrashLoopBackOff**：应用启动失败（查看日志）
174|   - **OOMKilled**：内存限制过小（调整 resources.limits）
175|   - **Pod 无法启动**：配置错误或依赖服务不可用
176|
177|3. **回滚操作**
178|   ```bash
179|   # Helm 回滚
180|   helm rollback myapp 2
181|   # K8s 回滚
182|   kubectl rollout undo deployment/app
183|   ```
184|
185|> **💡 详细工作流程**：参见 [reference/deployment_workflows.md](reference/deployment_workflows.md)
186|
187|---
188|
189|## ⚠️ 项目理解方法论（铁律）
190|
191|### 核心原则：理解项目 → 适配工具 → 制定计划
192|
193|**黄金法则**：
194|- ✅ **第一优先**：理解项目的 DevOps 技术选型
195|- ✅ **第二优先**：阅读项目 CLAUDE.md 理解部署架构
196|- ✅ **第三优先**：定位并分析项目配置文件
197|- ❌ **绝对禁止**：不经理解直接套用其他项目的配置
198|
199|### 快速识别项目技术栈
200|
201|| 工具类型 | 典型文件名 | 识别方式 |
202||---------|-----------|---------|
203|| **CI 平台** | `.gitlab-ci.yml` / `.github/workflows/*.yml` / `Jenkinsfile` | Glob 搜索 |
204|| **容器化** | `Dockerfile` / `docker-compose.yml` | Glob 搜索 |
205|| **K8s 部署** | `**/helm/**/*.yaml` / `**/argocd/*.yaml` | Glob 搜索 |
206|| **IaC 工具** | `terraform/**` / `**/*.tf` | Glob 搜索 |
207|
208|### 关键配置分析
209|
210|**CI/CD 配置**：
211|- 识别流水线阶段（test → build → deploy）
212|- 分析 Job 依赖关系（needs/dependsOn）
213|- 检查环境变量安全（避免硬编码密码）
214|
215|**Dockerfile 分析**：
216|- 是否使用多阶段构建（减小镜像体积）
217|- 基础镜像安全性（官方镜像、版本固定）
218|- 层缓存利用（频繁变化的指令放后面）
219|
220|**Helm Chart 分析**：
221|- values.yaml 是否按环境分离
222|- 资源限制是否合理（防止资源耗尽）
223|- 健康检查是否配置（liveness/readiness probe）
224|
225|> **💡 详细方法论**：参见 [reference/project_understanding.md](reference/project_understanding.md)
226|
227|---
228|
229|## 环境配置管理
230|
231|**设计原则**:
232|- ✅ **SKILL.md**: 存储可移植的 DevOps 行为准则和最佳实践
233|- ✅ **.env.skill**: 存储项目特定环境配置（namespace, API Server, kubeconfig）
234|- ❌ **禁止**: 将具体环境配置值硬编码在 SKILL.md 中
235|
236|**部署前验证**:
237|1. 读取项目根目录 `.env.skill` 文件获取环境配置
238|2. 验证 Kubeconfig、API Server、Namespace 匹配
239|3. 参考本文档的 "部署前检查清单" 执行验证
240|
241|**注意**: `.env.skill` 已在 `.gitignore` 中，不应提交到 Git 仓库
242|
243|---
244|
245|## 最佳实践
246|
247|### 1. 配置管理
248|
249|- 使用 Helm Chart 管理 K8s 资源
250|- values.yaml 按环境分离（dev/staging/prod）
251|- 敏感信息使用 Secret/External Secrets
252|- 配置文件纳入 Git 版本控制
253|
254|### 2. 镜像构建
255|
256|- 使用多阶段构建减小镜像体积
257|- 利用层缓存加速构建
258|- 使用语义化版本标签
259|- 定期扫描镜像漏洞
260|
261|### 3. CI/CD 流水线
262|
263|- 阶段划分清晰（test → build → deploy）
264|- 生产环境部署使用手动触发
265|- 配置自动回滚机制
266|- 保留部署历史记录
267|
268|### 4. 监控告警
269|
270|- 配置健康检查（liveness/readiness probe）
271|- 监控核心指标（CPU/Memory/QPS/延迟）
272|- 配置告警规则（多渠道通知）
273|- 日志集中收集（ELK/Loki）
274|
275|### 5. 安全加固
276|
277|- 定期扫描镜像漏洞（Trivy/Snyk）
278|- 使用非 root 用户运行容器
279|- 配置 RBAC 最小权限原则
280|- 敏感信息使用 Vault/External Secrets
281|
282|---
283|
284|## 🤝 Agent Team 协作
285|
286|DevOps 工作需要与其他角色密切协作。本 SKILL 专注于 DevOps 技术实践，相关职责请参考：
287|
288|- **架构设计** → [arch SKILL](../arch/SKILL.md)
289|  - 系统架构设计、部署架构
290|  - 网络拓扑、安全设计
291|  - 参考：`docs/design/system_architecture_v*.md`
292|
293|- **开发工作流** → [dev SKILL](../dev/SKILL.md)
294|  - 构建、测试、代码质量
295|  - CI/CD Pipeline 集成
296|  - 参考：`docs/scrum/story/` 中的 Story 文档
297|
298|- **测试验证** → [qa SKILL](../qa/SKILL.md)
299|  - 测试分层架构、UT/SIT/UAT
300|  - API 测试、性能测试
301|  - 部署后验证标准
302|
303|- **项目管理** → [pm SKILL](../pm/SKILL.md)
304|  - 部署排期、发布计划
305|  - 技术债务管理
306|  - 参考：`docs/scrum/prd/` 中的 Epic 文档
307|
308|---
309|
310|## 关键资源
311|
312|### Reference 文档
313|
314|- [项目理解方法论（详细）](reference/project_understanding.md) - 项目理解完整方法论
315|- [部署工作流程（详细）](reference/deployment_workflows.md) - 部署和故障排查详细流程
316|
317|### 官方文档
318|
319|- Docker: https://docs.docker.com/
320|- Kubernetes: https://kubernetes.io/docs/
321|- Helm: https://helm.sh/docs/
322|- ArgoCD: https://argoproj.github.io/argo-cd/
323|- Prometheus: https://prometheus.io/docs/
324|- Trivy: https://aquasecurity.github.io/trivy/
325|
326|### SKILL 文档
327|
328|- `.claude/skills/arch/SKILL.md` - 架构设计技能
329|- `.claude/skills/dev/SKILL.md` - 开发工作技能
330|- `.claude/skills/qa/SKILL.md` - 测试工作技能
331|- `.claude/skills/pm/SKILL.md` - 项目管理技能
332|
333|### 项目文档
334|
335|- `CLAUDE.md` - 项目概览和部署架构
336|- `docs/design/system_architecture_v*.md` - 系统架构设计
337|
338|---
339|
340|**版本**: v2.0
341|**创建日期**: 2026-03-25
342|**更新日期**: 2026-04-29
343|**维护者**: DevOps Team
344|
345|**更新日志**:
346|- v2.0 (2026-04-29): 🎯 **重大更新**：渐进式披露优化
347|  - 调整章节顺序，高价值内容前置（行为准则、常见错误）
348|  - 精简主文档（449 行 → 283 行，减少 37%）
349|  - 提取详细内容到 reference/ 目录
350|  - 新增 Agent Team 协作章节
351|  - 更新 description，更"pushy"的触发描述
352|- v1.0 (2026-03-25): 初始版本
353|