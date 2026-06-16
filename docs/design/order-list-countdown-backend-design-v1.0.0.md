---
version: "1.0.0"
created: "2026-06-16"
updated: "2026-06-16"
story_ref: "TES-37"
parent_design: "TES-36 架构设计文档"
---

# 后端设计：订单列表接口新增预占时效与审批开始时间字段

## 1. 概述

为支持 Punch 商城订单列表页的审批倒计时功能，后端需要在订单列表接口的响应 DTO 中新增三个字段，并从 Webshop 读取对接抬头的预占时效配置。

## 2. 接口变更

### 2.1 订单列表响应新增字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `preoccupyDuration` | Number \| null | 预占时效（小时），未配置时为 null |
| `approvalStartTime` | String \| null | 审批开始时间（ISO 8601），为空时前端降级用 orderCreateTime |
| `orderCreateTime` | String | 订单创建时间（ISO 8601） |

### 2.2 响应示例

```json
{
  "orderId": "ORD-20260616-001",
  "status": "pending_approval",
  "orderCreateTime": "2026-06-15T10:00:00Z",
  "preoccupyDuration": 24,
  "approvalStartTime": "2026-06-15T10:05:00Z"
}
```

## 3. 核心实现

### 3.1 模块清单

| 模块 | 文件 | 职责 |
|------|------|------|
| DTO | `OrderListItemDto` | 响应 DTO，包含新增字段 |
| 枚举 | `OrderStatus` | 订单状态枚举，`isPendingApprovalStatus()` 判断待审批状态 |
| 实体 | `OrderEntity`, `ApprovalRecordEntity` | 订单表、审批记录表 JPA 映射 |
| Repository | `OrderRepository`, `ApprovalRecordRepository` | 数据访问层 |
| Client | `WebshopClient` | Webshop 调用，批量获取预占时效 |
| Service | `PreoccupyDurationCacheService` | 预占时效本地缓存 + Webshop 降级 |
| Service | `OrderListService` | 订单列表查询与字段组装 |
| Controller | `OrderController` | REST 接口 |
| Config | `CacheConfig` | Caffeine 缓存管理器 |

### 3.2 缓存策略

- **缓存键**：`preoccupy_duration:{对接抬头ID}`
- **TTL**：30 分钟（配置项 `preoccupy.cache.ttl-minutes`）
- **最大条目数**：1000（配置项 `preoccupy.cache.max-size`）
- **刷新机制**：被动刷新（过期后首次请求触发）
- **降级**：Webshop 不可达时返回 null，不阻断列表查询
- **取值校验**：1 ~ 720 小时，非法值过滤

### 3.3 审批开始时间获取

- 优先从 `t_approval_record` 表获取最新审批提交时间
- 若无审批记录，DTO 中 `approvalStartTime` 为 null，前端降级使用 `orderCreateTime`

### 3.4 待审批状态

```java
OrderStatus.isPendingApprovalStatus(orderStatus)
// 接受值：pending_approval, approval_in_progress
```

仅当订单状态为待审批时，才查询预占时效并填充 `preoccupyDuration` 字段。

## 4. 验收标准

- [x] 订单列表接口响应包含 `preoccupyDuration`、`approvalStartTime`、`orderCreateTime`
- [x] 缓存命中时不触发 Webshop API 调用（单元测试验证）
- [x] Webshop 不可达时返回 null，不阻断列表查询（单元测试验证）
- [x] `approvalStartTime` 为空时前端降级使用 `orderCreateTime`（DTO 提供 `resolveCountdownStartTime()`）
- [x] 预占时效取值校验（1~720 小时）

## 5. 待确认事项（来自父 design）

| 事项 | 当前方案 | 备注 |
|------|----------|------|
| 审批开始时间来源 | 审批记录表 `submit_time` 字段 | 已实现 |
| `orderCreateTime` 是否已返回 | 已作为新增字段返回 | 已实现 |
| 待审批状态枚举 | `pending_approval`, `approval_in_progress` | 已实现 |
| 预占时效取值范围 | 1~720 小时 | 已实现校验 |
