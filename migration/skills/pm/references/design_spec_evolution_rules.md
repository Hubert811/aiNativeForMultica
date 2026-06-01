# 设计文档演进规则

## 版本更新

| 变更类型 | 版本变化 | 操作 |
|---|---|---|
| 架构级变更 | MAJOR +1 | 更新版本号，归档旧版 |
| 模块级变更 | MINOR +1 | 更新版本号 |
| 文字修正 | PATCH +1 | 更新版本号 |

## 归档流程

MAJOR 版本更新时：

1. 将旧版文件移至 `docs/design/archive/`
2. 命名格式：`{原文件名}_v{旧版本号}.md`
3. 创建新版文件在原位置

## Story 与设计文档的关联

Story Issue description 中引用设计文档章节：

```
## 详细文档
→ docs/design/system_architecture.md § 3.2 用户认证模块
```

Story 完成后，如果设计文档需要同步更新：

1. 更新设计文档
2. 更新版本号
3. 在 Story Issue comment 中说明同步内容

不需要单独的状态同步步骤。
