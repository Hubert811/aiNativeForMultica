# Story 状态更新工作流

## 双层状态模型

- **`status`**（骨架）：Multica Issue 原生状态，表达宏观进度
  ```
  todo → in_progress → in_review → done
  ```
- **`phase`**（血肉）：metadata 细粒度流程阶段
  ```
  writing → reviewing → ac_review → testing
  ```

### 状态映射

| Scrum 阶段 | `status` | `metadata.phase` |
|------------|----------|-------------------|
| 开发 Story | `in_progress` | `"writing"` |
| 提交代码审查 | `in_review` | `"reviewing"` |
| AC 验收中 | `in_review` | `"ac_review"` |
| 测试中 | `in_review` | `"testing"` |
| Story 完成 | `done` | *(删除)* |

> 原则：中间 phase 变化不碰 status，只有 phase 走到终态时才同步更新 status。

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
multica issue metadata set <story-id> --key phase --value writing
multica issue update <story-id> --assignee "developer-name"
```

### 提交审查

```bash
multica issue status <story-id> in_review
multica issue metadata set <story-id> --key phase --value reviewing
multica issue metadata set <story-id> --key verification_evidence --type string --value "commit: abc1234"
```

### AC 验收通过，进入测试

```bash
multica issue metadata set <story-id> --key phase --value testing
```

### 完成

```bash
multica issue status <story-id> done
multica issue metadata delete <story-id> --key phase
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
multica issue metadata set <story-id> --key phase --value writing
```

在 Issue comment 中说明退回原因。
