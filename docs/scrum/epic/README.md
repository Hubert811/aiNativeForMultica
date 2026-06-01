# Epic 规范

## 概述

Epic 是项目的顶层工作单元，包含多个 Story。

## 文件位置

Epic 规范文档存储在 `docs/scrum/epic/epic-*.md`。

## Issue 对应关系

每个 Epic markdown 文件对应一个 Multica Epic Issue：
- Epic Issue 的 description 放概要+文件引用
- Epic 的详细内容（描述、Story 列表、依赖关系）在 markdown 文件中
- Epic 的状态、优先级、指派由 Issue 字段管理

## Markdown 文件结构

```markdown
---
epic_id: "EPIC-001"
title: "用户认证系统"
priority: high
---

# EPIC-001: 用户认证系统

## 描述
[详细描述 Epic 的目标和范围]

## Stories

- [ ] [STORY-001](story-1-01-xxx.md) - 用户注册
- [ ] [STORY-002](story-1-02-xxx.md) - 用户登录
- [ ] [STORY-003](story-1-03-xxx.md) - 密码重置

## 依赖关系
[与其他 Epic 的依赖]

## 验收标准
[Epic 级别的验收标准]
```

## Story 数量管理

Story 数量由 Multica 父子 Issue 关系保证——每个 Story Issue 的 `parent_issue_id` 指向 Epic Issue。不需要用脚本去检查文件中的 stories 数组与实际文件数量是否一致。

## 状态管理

Epic 的状态（todo/in_progress/in_review/done）由 Issue 字段管理，不在 markdown 文件中维护状态标记。
