# Story 状态更新工作流

## 状态流转

使用 Multica Issue 原生状态：

```
todo → in_progress → in_review → done
```

## 代码验证（状态更新前）

在更新 Issue 状态之前，通过 git 验证代码变更是否与 Story 匹配：

```bash
# 查看最近与 Story 相关的提交
git log --oneline --all --grep="STORY-001" -10

# 查看具体提交的变更内容
git show <commit-sha> --stat
git show <commit-sha> -- <path/to/file>

# 确认修改范围是否与 Story 文档描述一致
git diff <base-branch>...<feature-branch> --stat
```

代码验证的目的是确认实现完整性，不用于流程状态判定。流程权威仍在 Issue。

## 操作步骤

### 开始开发

```bash
multica issue status <story-id> in_progress
multica issue update <story-id> --assignee "developer-name"
```

### 提交审查

```bash
multica issue status <story-id> in_review
multica issue metadata set <story-id> --key verification_evidence --type string --value "commit: abc1234"
```

### 测试阶段（通过 metadata 标记）

```bash
multica issue metadata set <story-id> --key testing --type bool --value true
```

### 完成

```bash
multica issue status <story-id> done
multica issue metadata set <story-id> --key qa_status --type string --value "green"
```

## 证据链

完整的 Story 完成证据：

1. Issue status = `done`
2. metadata 包含 `verification_evidence`（commit SHA + PR URL）
3. metadata 包含 `qa_status = "green"`

代码验证通过 `git log` / `git show` 确认实现完整性，但流程状态以 Issue API 为准。

## 退回流程

审查不通过或测试失败：

```bash
multica issue status <story-id> in_progress
```

在 Issue comment 中说明退回原因。
