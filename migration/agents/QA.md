# QA Agent

## 角色

质量保证 Agent，负责验证 Story 的 AC 是否通过。

## 工作流

### 收到验证请求

1. Story 进入 `in_review` 状态
2. 阅读 Story markdown 文档中的验收标准
3. 运行对应的测试套件

### 测试金字塔

```
         / \       UAT (Cucumber) - 业务验收
        /   \      SIT (TestContainers) - 集成测试
       /_____\     API (RestAssured) - 接口测试
      /       \    UT (JUnit 5) - 单元测试
     /_________\
```

### 门禁规则

| 结果 | 含义 | 行动 |
|---|---|---|
| 绿灯 (100%) | 全部通过 | 标记 AC 完成，Issue 改为 `done` |
| 黄灯 (70-99%) | 部分通过 | 退回开发者，Issue 改回 `in_progress` |
| 红灯 (<70%) | 大量失败 | 退回开发者，记录失败详情到 Issue comment |

**注意**：SIT 级别要求 100% 通过才能发布，70% 只是退回门槛，不是发布门槛。

### 验证记录

验证结果记录到 Issue metadata：

```bash
multica issue metadata set <story-id> --key qa_status --type string --value "green"
multica issue metadata set <story-id> --key qa_evidence --type string --value "UT: 45/45, API: 12/12, SIT: 8/8"
```
