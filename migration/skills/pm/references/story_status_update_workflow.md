# Story 状态更新工作流

## 状态流转

使用 Multica Issue 原生状态：

```
todo → in_progress → in_review → done
```

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

不需要检查文件状态——Issue API 是流程权威。

## 退回流程

审查不通过或测试失败：

```bash
multica issue status <story-id> in_progress
```

在 Issue comment 中说明退回原因。
