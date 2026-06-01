# 剩余文件处理指南

## 数据源原则

**内容是单源（Markdown），流程是单源（Issue），各自唯一就不存在不一致。**

- Markdown 文件存储设计、Epic、Story 的详细内容
- Issue 存储状态、指派、讨论、metadata
- Issue description 中通过文件路径引用对应的 Markdown 文档
- 不需要任何脚本去检查文件和 Issue 之间的一致性——Issue API 本身是流程权威，git 仓库是内容权威

## 迁移后的目录结构

```
project/
├── migration/                    # 迁移参考材料（历史存档）
│   ├── README.md
│   ├── agents/                   # Agent system prompt
│   ├── skills/                   # Skill 定义
│   └── project-structure/        # 项目骨架
├── docs/
│   ├── design/                   # 设计文档
│   │   ├── README.md             # 设计文档规范
│   │   └── archive/              # 旧版归档
│   ├── scrum/
│   │   ├── epic/                 # Epic 规范
│   │   └── story/                # Story 规范
│   └── guides/                   # 开发指南
└── src/                          # Java SpringBoot 源码
```

## 旧版文件清理

迁移完成后，以下旧版文件/目录应删除（如果存在于工作区中）：

| 文件/目录 | 原因 |
|---|---|
| `docs/guides/ai-native-guide.md` | 旧版操作手册，内容已过时 |
| `migration/skills/pm/scripts/` | 脚本已不再需要 |
| `migration/skills/pm/templates/` | 模板已由 Multica 替代 |

## 新增文档

对于尚未创建的文档（实际 Epic/Story/设计文档），按以下流程创建：

1. **Architect** 创建 `docs/design/` 下的设计文档
2. **PM** 在 Multica 中创建 Epic Issue，description 引用 Epic markdown 文件
3. **PM** 在 Multica 中创建 Story Issue，description 引用 Story markdown 文件
4. 每个 Issue 的 description 保持简短，仅存概要+文档引用

## 每周检查

不再需要文件审计脚本。每周检查应通过：

```bash
multica issue list --project <project-id> --output json
```

查询 Issue 状态，确认父子 Issue 关系和 metadata 完整性。
