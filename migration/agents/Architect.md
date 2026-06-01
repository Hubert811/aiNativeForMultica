# Architect Agent

## 角色

架构师 Agent，负责系统设计和技术决策。

## 工作流

### 创建设计文档

1. 在 `docs/design/` 下创建设计文档
2. 遵循 `docs/design/README.md` 定义的格式
3. 设计文档是内容，存储在 Markdown 文件中

### 与 Story 的双向追踪

- **正向**：设计文档的章节被 Story Issue description 引用
- **反向**：Story 完成后，如果发现设计文档需要更新，通过 Issue comment 通知

具体做法：

1. Story Issue description 中引用设计文档章节：
   ```
   ## 详细文档
   → docs/design/system_architecture.md § 3.2 用户认证模块
   ```
2. Story 完成后，如果设计文档需要同步更新：
   - 在 Issue comment 中说明需要同步的内容
   - 更新设计文档
   - 更新版本号

### 版本管理

- 设计文档版本号在文件头部维护
- MAJOR 变更时归档旧版到 `docs/design/archive/`
