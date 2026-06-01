
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

# PM Skill

## 角色

PM 的日常工作流程 Skill。

## 主战场

**Multica Issue 是主战场**，Markdown 文档是内容数据源。PM 通过 `multica issue` CLI 管理流程，详细内容在文档中查阅。

## 6-Step 工作流

### Step 1: 需求分析

- 阅读设计文档（`docs/design/`）
- 确认 Epic 范围和 Story 拆分
- 在 `docs/scrum/epic/` 中创建 Epic markdown 文件

### Step 2: 创建 Epic Issue

```bash
multica issue create \
  --title "EPIC-001: Epic 标题" \
  --description "## 概要
一句话概括

## 详细文档
→ docs/scrum/epic/epic-1-xxx.md

## 进度
待分派 Story" \
  --project <project-id> \
  --priority high
```

记录 Epic Issue ID 到 metadata：

```bash
multica issue metadata set <epic-id> --key epic_doc --type string --value "docs/scrum/epic/epic-1-xxx.md"
```

### Step 3: 创建 Story Issues

为每个 Story 创建 Issue，`--parent` 指向 Epic Issue：

```bash
multica issue create \
  --title "STORY-001: Story 标题" \
  --description "## 概要
一句话概括

## 详细文档
→ docs/scrum/story/story-1-01-xxx.md

## 进度
待开发" \
  --parent <epic-id> \
  --project <project-id>
```

记录 Story 文档路径：

```bash
multica issue metadata set <story-id> --key story_doc --type string --value "docs/scrum/story/story-1-01-xxx.md"
```

### Step 4: 状态更新

开发者完成工作后，通过 CLI 更新状态：

```bash
multica issue status <story-id> in_review
```

不要用 `sed` 修改 Markdown 文件中的状态。

Testing 阶段通过 metadata 标记：

```bash
multica issue metadata set <story-id> --key testing --type bool --value true
```

### Step 5: 验收验证

QA 验证 AC 通过后：

```bash
multica issue status <story-id> done
multica issue metadata set <story-id> --key verification_evidence --type string --value "commit: abc1234"
multica issue metadata set <story-id> --key qa_status --type string --value "green"
```

Epic 的完成度通过查询子 Issue 状态计算：

```bash
multica issue list --project <project-id> --parent <epic-id> --output json
```

统计 `done` 状态的 Story 数量占比。

### Step 6: 进度展示

使用 **Multica Project View** 展示项目进度：
- 按状态分组查看 Issue
- 查看 Epic 完成度
- 筛选特定 Story/开发者

不再使用 DASHBOARD.md 或 KANBAN.md 文件。

## 每周检查

通过 Issue API 检查项目健康度：

```bash
# 查看所有 Issue 状态
multica issue list --project <project-id> --output json

# 检查未分派的 Story
multica issue list --project <project-id> --status todo --output json

# 检查阻塞项
multica issue list --project <project-id> --status blocked --output json
```

不再需要 `audit_and_render.sh` 或 `grep` 检查文件一致性。

## 数据原则

- **内容唯一**：Markdown 文件是内容的唯一数据源
- **流程唯一**：Issue 是流程的唯一载体
- **不重复**：同一信息只存储在一处
- **引用**：Issue description 通过文件路径引用文档
