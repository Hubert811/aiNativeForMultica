# Story 编号规则

## 格式

`story-{epic_number}-{story_sequence}-{slug}.md`

## 示例

| 文件 | 含义 |
|---|---|
| `story-1-01-user-registration.md` | Epic 1 的第 1 个 Story |
| `story-1-02-user-login.md` | Epic 1 的第 2 个 Story |
| `story-2-01-product-list.md` | Epic 2 的第 1 个 Story |

## 规则

1. `epic_number` 从 1 开始递增
2. `story_sequence` 在每个 Epic 内从 01 开始递增
3. `slug` 用 kebab-case 描述 Story 内容
4. 编号一旦分配不可重用

## Issue 对应

每个 Story 文件对应一个 Story Issue，通过 description 引用文件路径。
