# 迁移索引

本文档是从 Go archetype 项目迁移到 Multica + Java SpringBoot 架构的总纲。

## 核心原则

**Markdown 文件是内容的唯一数据源，Issue 是流程的唯一载体，两者不重复存储任何内容。**

| 放 Markdown 文档 | 放 Multica Issue |
|---|---|
| 设计文档正文、Epic/Story 详情、AC 全文 | 状态流转 (todo/in_progress/in_review/done) |
| 测试策略、验收标准详情 | 指派、优先级、标签 |
| 版本号、设计决策记录 | 评论/讨论记录 |
| 规范/模板定义 | verification metadata（commit SHA、完成时间） |

核心判断标准：**是内容还是流程？** 内容放文档，流程放 Issue。

## Issue description 规范

每个 Issue（Epic/Story）的 description 只保留三件事：

```
## 概要
一句话概括

## 详细文档
→ docs/scrum/story/story-X-XX-xxx.md

## 进度
当前阶段 / 阻塞项
```

## 迁移清单

### 已迁移
| 组件 | 状态 |
|---|---|
| 代码骨架 | `migration/project-structure/structure.md` |
| Agent 定义 | `migration/agents/` |
| PM Skill | `migration/skills/pm/SKILL.md` |

### 不再迁移（由 Multica 原生替代）
| 组件 | 替代方案 |
|---|---|
| DASHBOARD.md | Multica Project View |
| KANBAN.md | Multica Project View |
| audit_and_render.sh | 不再需要，进度由 Issue API 获取 |
| kanban_renderer.py | 不再需要 |

## 后续

迁移完成后，本文档保留为历史参考。新的工作流遵循 `docs/scrum/` 和 `docs/design/` 中定义的规范。
