/**
 * OrderCard — 订单卡片（集成 ApprovalCountdown）
 *
 * 结构：
 *   OrderCard
 *   ├── OrderStatusTag (现有)
 *   ├── ApprovalCountdown (新增 - 条件渲染)
 *   └── ...其他现有内容
 */
import React, { memo } from 'react';
import { ApprovalCountdown } from '../../components/ApprovalCountdown';
import type { OrderListItem } from './types';

export interface OrderCardProps {
  order: OrderListItem;
  /** 倒计时归零回调，由父级 useOrderListRefresh 提供 */
  onExpired: (orderId: string) => void;
}

/**
 * 现有的 OrderStatusTag 组件 — 占位引用。
 * 实际项目中应从对应的组件库导入，这里仅示意。
 */
// import { OrderStatusTag } from '@/components/OrderStatusTag';

const OrderStatusTag: React.FC<{ status: string }> = ({ status }) => (
  <span className="order-status-tag" data-status={status}>
    {status}
  </span>
);

export const OrderCard: React.FC<OrderCardProps> = memo(({ order, onExpired }) => {
  const handleExpired = React.useCallback(() => {
    onExpired(order.orderId);
  }, [onExpired, order.orderId]);

  return (
    <div className="order-card" data-order-id={order.orderId}>
      {/* 现有字段 */}
      <OrderStatusTag status={order.status} />

      {/* 新增：审批倒计时（组件内部做条件渲染，无匹配时返回 null） */}
      <ApprovalCountdown
        preoccupyDuration={order.preoccupyDuration}
        approvalStartTime={order.approvalStartTime}
        orderCreateTime={order.orderCreateTime}
        orderStatus={order.status}
        onExpired={handleExpired}
      />

      {/* ...其他现有内容 */}
      <div className="order-card__body">
        {/* 订单号、商品、金额等现有字段 */}
        <span>{order.orderId}</span>
      </div>
    </div>
  );
});

OrderCard.displayName = 'OrderCard';

export default OrderCard;
