/**
 * OrderListPage — 订单列表页（集成示例）
 *
 * 展示如何将 ApprovalCountdown 接入现有订单列表：
 * 1. 维护订单列表 state
 * 2. 接入 useOrderListRefresh 处理倒计时归零的刷新
 * 3. 将 scheduleRefresh 透传给每个 OrderCard 的 onExpired
 */
import React, { useCallback, useEffect, useState } from 'react';
import { OrderCard } from './OrderCard';
import { useOrderListRefresh } from './useOrderListRefresh';
import type { OrderListItem, OrderListResponse } from './types';

/**
 * 实际的 fetchOrderList 应调用现有的订单列表接口：
 *   GET /api/orders?page=&pageSize=&status=&orderId=
 *
 * 这里用 mock 演示类型契约。
 */
const fetchOrderList = async (): Promise<OrderListResponse> => {
  // TODO: 替换为真实接口调用
  // const res = await request.get<OrderListResponse>('/api/orders', { params });
  // return res.data;
  return { items: [], total: 0, page: 1, pageSize: 20 };
};

export const OrderListPage: React.FC = () => {
  const [orders, setOrders] = useState<OrderListItem[]>([]);
  const [loading, setLoading] = useState(false);

  const loadOrders = useCallback(async () => {
    setLoading(true);
    try {
      const res = await fetchOrderList();
      setOrders(res.items);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadOrders();
  }, [loadOrders]);

  // 单条刷新后，局部替换 state 中对应订单
  const handleOrderUpdated = useCallback((updated: OrderListItem) => {
    setOrders((prev) =>
      prev.map((o) => (o.orderId === updated.orderId ? updated : o)),
    );
  }, []);

  // 整页刷新后，整批替换
  const handleListRefreshed = useCallback((res: OrderListResponse) => {
    setOrders(res.items);
  }, []);

  const { scheduleRefresh, isRefreshing, refreshAll } = useOrderListRefresh({
    fetchOrderList,
    onOrderUpdated: handleOrderUpdated,
    onListRefreshed: handleListRefreshed,
    onError: (err) => console.error('[OrderList] refresh failed:', err),
  });

  return (
    <div className="order-list-page">
      <header className="order-list-page__header">
        <h2>订单列表</h2>
        <button onClick={refreshAll} disabled={loading || isRefreshing}>
          {isRefreshing ? '刷新中…' : '刷新'}
        </button>
      </header>

      <div className="order-list-page__body">
        {orders.length === 0 && !loading ? (
          <div className="order-list-page__empty">暂无订单</div>
        ) : (
          orders.map((order) => (
            <OrderCard
              key={order.orderId}
              order={order}
              onExpired={scheduleRefresh}
            />
          ))
        )}
      </div>
    </div>
  );
};

export default OrderListPage;
