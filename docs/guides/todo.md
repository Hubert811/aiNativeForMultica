# TODO: Phase 3/4/5 基础设施落地

> 本文档列出 AI-Native 开发流程中 Phase 3/4/5 缺失的基础设施文件。
> archetype-config.yml 中的 `deploy.stages` 段已定义了配置，但缺少对应的可执行文件。
> 技术栈: Java 17 / Spring Boot 3.2 / Maven

---

## Phase 3: 测试环境

### 3.1 Dockerfile

**文件**: `deploy/docker/Dockerfile`

**作用**: 打包 Java/Spring Boot 应用为 Docker 镜像

**参考配置**（来自 archetype-config.yml）:
- 基础镜像: `eclipse-temurin:17-jre`
- 构建产物: `target/*.jar`
- 构建命令: `mvn package`

---

### 3.2 docker-compose.yml

**文件**: `deploy/docker/docker-compose.yml`

**作用**: 本地 SIT 集成环境（应用 + 数据库 + Redis）

**参考配置**（来自 archetype-config.yml）:
- Spring Boot 3.2
- JPA + 数据库（PostgreSQL/MySQL，按项目实际选择）
- Redis 缓存

---

### 3.3 .gitlab-ci.yml（CI/CD 流水线）

**文件**: `.gitlab-ci.yml`（项目根目录）

**作用**: 定义 CI/CD 流水线，覆盖以下阶段：
- `test` — 运行 UT / API / SIT 测试
- `build` — 构建 Docker 镜像（snapshot）
- `deploy_test` — Helm 部署到测试环境
- `qa_test` — QA 回归测试触发

**需要定义的关键 jobs**:

| Job | 阶段 | 触发条件 |
|-----|------|---------|
| `unit_test` | test | 每次 push |
| `api_contract_test` | test | 每次 push |
| `sit_test` | test | 每次 push |
| `build_snapshot` | build | MR 创建或更新 |
| `deploy_test_env` | deploy_test | build_snapshot 通过 |
| `qa_regression` | qa_test | deploy_test_env 通过，手动触发 |

---

## Phase 4: 生产构建

### 4.1 CI 生产构建 Job

**文件**: `.gitlab-ci.yml`（追加）

**作用**: MR 合并后自动构建生产镜像

**需要定义的关键 jobs**:

| Job | 阶段 | 触发条件 |
|-----|------|---------|
| `build_production` | build_prod | main 分支 merge |
| `push_registry` | build_prod | build_production 通过 |

**镜像标签策略**（来自 archetype-config.yml）:
```yaml
snapshot: "snapshot-mr-{mr_iid}"
release: "{sem_ver}-{timestamp}-{sha}"
```

---

## Phase 5: 生产发布

### 5.1 Helm Chart

**目录**: `deploy/k8s/helm/`

**需要创建的文件**:

| 文件 | 作用 |
|------|------|
| `Chart.yaml` | Chart 元数据（name、version） |
| `values.yaml` | 默认配置值 |
| `values-test.yaml` | 测试环境配置 |
| `values-prod.yaml` | 生产环境配置 |
| `templates/deployment.yaml` | K8s Deployment 定义 |
| `templates/service.yaml` | K8s Service 定义 |
| `templates/ingress.yaml` | K8s Ingress 定义（可选） |
| `templates/configmap.yaml` | K8s ConfigMap（应用配置） |
| `templates/hpa.yaml` | 水平自动扩缩（可选） |

**参考配置**（来自 archetype-config.yml）:
```yaml
k8s:
  helm_dir: "deploy/k8s/helm"
  chart_name: "project-template"
  values_test: "values-test.yaml"
  values_prod: "values-prod.yaml"
```

### 5.2 CI 生产发布 Jobs

**文件**: `.gitlab-ci.yml`（追加）

**需要定义的关键 jobs**:

| Job | 阶段 | 触发条件 |
|-----|------|---------|
| `deploy_prod_env` | deploy_prod | 手动触发，需 Human Review 确认 |
| `prod_validation` | validate | deploy_prod_env 通过 |

---

## 实现建议

### 优先级

1. **高**: `.gitlab-ci.yml` — 没有 CI 流水线，其他都无法触发
2. **高**: `Dockerfile` — CI 构建镜像的前置条件
3. **中**: `deploy/docker/docker-compose.yml` — 本地 SIT 需要
4. **中**: `deploy/k8s/helm/` — 测试/生产环境部署需要
5. **低**: CI 中的生产发布 jobs — 测试环境跑通后再加

### 实现后验证

每个文件创建完成后，需要验证：
- [ ] archetype-config.yml 中的配置能在 .gitlab-ci.yml / Dockerfile / Helm Charts 中正确引用
- [ ] 替换 archetype-config.yml 中的语言/构建工具后，CI 流水线仍能工作（换语言只改 yml，不改流水线）
- [ ] 本地 `docker-compose up` 能启动完整集成环境
- [ ] `helm install --dry-run` 能通过验证
