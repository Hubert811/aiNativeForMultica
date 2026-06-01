# Story 规范

## 概述

Story 是可在一个迭代内完成的最小可交付工作单元。

## 文件位置

Story 规范文档存储在 `docs/scrum/story/story-*.md`。

## Issue 对应关系

每个 Story markdown 文件对应一个 Multica Story Issue：
- Story Issue 的 description 放概要+文件引用
- Story 的详细内容（AC 全文、测试策略）在 markdown 文件中
- Story 的状态、指派、优先级由 Issue 字段管理

## Markdown 文件结构

```markdown
---
story_id: "STORY-001"
title: "用户注册"
epic: "EPIC-001"
---

# STORY-001: 用户注册

## 描述
[用户故事描述：作为一个 XX 角色，我想要 XX，以便 XX]

## 验收标准 (AC)

### AC-1: 正常注册
- [ ] 输入合法邮箱、密码、用户名后，注册成功
- [ ] 返回 JWT token
- [ ] 数据持久化到数据库

### AC-2: 重复邮箱
- [ ] 返回 409 Conflict
- [ ] 错误信息明确提示邮箱已存在

### AC-3: 非法输入
- [ ] 邮箱格式错误返回 400
- [ ] 密码长度不足返回 400

## 测试策略

| 测试类型 | 框架 | 覆盖 AC |
|---|---|---|
| 单元测试 | JUnit 5 + Mockito | AC 逻辑分支 |
| API 测试 | RestAssured | AC-1, AC-2, AC-3 |
| 集成测试 | TestContainers | AC-1 端到端 |

## 技术实现要点
- 使用 Spring Validation 做入参校验
- BCrypt 加密密码
- JWT 生成 token
```

## 状态流转

Story 状态采用**双层状态模型**：

- **Layer 1（骨架）**：Multica Issue 原生 `status`，表达宏观进度
  ```
  todo → in_progress → in_review → done
  ```
- **Layer 2（血肉）**：Issue metadata `phase`，表达 Scrum 流程细粒度阶段
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
| Story 完成 | `done` | *(删除或清空)* |

### 状态流转条件

| 从状态 | 到状态 | 条件 |
|---|---|---|
| todo | in_progress | 开发者开始工作，设置 `phase = "writing"` |
| in_progress | in_review | 代码已提交，设置 `phase = "reviewing"` |
| in_review | in_review (phase 流转) | AC 验收通过，设置 `phase = "ac_review"` → `"testing"` |
| in_review | done | AC 全部通过 + testing 通过，删除 `phase` |
| in_review | in_progress | review 不通过或测试失败，退回 `phase = "writing"` |

### 操作方法

```bash
# 开始开发
multica issue status <story-id> in_progress
multica issue metadata set <story-id> --key phase --value writing

# 提交审查
multica issue status <story-id> in_review
multica issue metadata set <story-id> --key phase --value reviewing

# AC 验收通过，进入测试
multica issue metadata set <story-id> --key phase --value testing

# Story 完成
multica issue status <story-id> done
multica issue metadata delete <story-id> --key phase
```

> **原则**：中间 phase 变化不碰 status，只有 phase 走到终态时才同步更新 status。

### Verification Evidence

完成时，在 Issue metadata 中记录证据：

```bash
multica issue metadata set <story-id> --key verification_evidence --type string --value "commit: abc1234, PR: https://..."
```

## AC 签字率

概念保留：所有 AC 通过后才能标记 done。实现方式改为通过 Issue metadata + autopilot 自动验证 AC 完成情况，不再用文件标记。

## 编号规则

`story-{epic_number}-{story_sequence}-{slug}.md`

例如：`story-1-01-user-registration.md` 表示 Epic 1 的第 1 个 Story。
