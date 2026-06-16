/**
 * 订单列表 DTO 类型定义
 *
 * 映射后端订单列表接口响应，包含倒计时计算所需的三个字段：
 * - preoccupyDuration: 预占时效（小时）
 * - approvalStartTime: 审批开始时间（ISO 8601）
 * - orderCreateTime: 订单创建时间（ISO 8601）
 *
 * 对应后端 story TES-37 的接口变更。
 */

export interface OrderListItem {
  /** 订单号 */
  orderId: string;
  /** 订单状态（后端枚举值） */
  status: string;
  /** 订单创建时间（ISO 8601） */
  orderCreateTime: string;
  /** 预占时效（小时）。未配置时为 null 或 0 */
  preoccupyDuration: number | null;
  /** 审批开始时间（ISO 8601）。为空时使用 orderCreateTime 作为倒计时起点 */
  approvalStartTime: string | null;
  /** 对接抬头 ID（用于批量刷新时的分组，可选） */
  dockingHeaderId?: string;
  /** 其他现有字段（按需扩展） */
  [key: string]: unknown;
}

export interface OrderListResponse {
  items: OrderListItem[];
  total: number;
  page: number;
  pageSize: number;
}

export interface FetchOrderListParams {
  page?: number;
  pageSize?: number;
  status?: string;
  /** 单条刷新时传入 orderId，仅刷新该条；不传则刷新整页 */
  orderId?: string;
}
