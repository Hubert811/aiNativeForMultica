/**
 * useOrderListRefresh — 倒计时归零时的列表刷新 hook
 *
 * 核心策略：
 * 1. 多条订单同时归零时，合并为一次刷新请求（防抖 200ms）
 * 2. 支持单条刷新（orderId 命中时局部更新）与整页刷新两种模式
 * 3. 刷新期间屏蔽重复触发
 *
 * 使用方式：
 *   const { scheduleRefresh, isRefreshing } = useOrderListRefresh({
 *     fetchOrderList,
 *     onOrderUpdated,
 *     onListRefreshed,
 *   });
 *   <OrderCard onExpired={() => scheduleRefresh(order.orderId)} />
 */
import { useCallback, useRef, useState } from 'react';
import type {
  FetchOrderListParams,
  OrderListItem,
  OrderListResponse,
} from './types';

export interface UseOrderListRefreshOptions {
  /** 调用后端订单列表接口 */
  fetchOrderList: (params?: FetchOrderListParams) => Promise<OrderListResponse>;
  /** 单条订单刷新成功后回调（用于局部更新 state） */
  onOrderUpdated?: (updated: OrderListItem) => void;
  /** 整页刷新成功后回调（用于整页替换 state） */
  onListRefreshed?: (list: OrderListResponse) => void;
  /** 刷新失败回调 */
  onError?: (err: unknown) => void;
  /** 防抖窗口（毫秒），默认 200ms */
  debounceMs?: number;
}

export interface UseOrderListRefreshResult {
  /** 注册一个订单的归零事件；多条同时触发会合并为一次请求 */
  scheduleRefresh: (orderId: string) => void;
  /** 当前是否正在刷新 */
  isRefreshing: boolean;
  /** 立即刷新整页（供手动刷新按钮使用） */
  refreshAll: () => Promise<void>;
}

export function useOrderListRefresh({
  fetchOrderList,
  onOrderUpdated,
  onListRefreshed,
  onError,
  debounceMs = 200,
}: UseOrderListRefreshOptions): UseOrderListRefreshResult {
  const [isRefreshing, setIsRefreshing] = useState(false);
  const pendingIdsRef = useRef<Set<string>>(new Set());
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const inflightRef = useRef<Promise<void> | null>(null);

  const flush = useCallback(async () => {
    const ids = Array.from(pendingIdsRef.current);
    pendingIdsRef.current.clear();
    if (ids.length === 0) return;

    // 防止并发刷新
    if (inflightRef.current) {
      // 合并到当前进行中的刷新：把 ids 塞回去，等 inflight 结束后再触发一轮
      ids.forEach((id) => pendingIdsRef.current.add(id));
      return;
    }

    const doRefresh = async () => {
      setIsRefreshing(true);
      try {
        if (ids.length === 1) {
          // 单条刷新：局部更新
          const res = await fetchOrderList({ orderId: ids[0] });
          const updated = res.items.find((it) => it.orderId === ids[0]);
          if (updated) onOrderUpdated?.(updated);
        } else {
          // 多条同时归零：整页刷新（合并请求）
          const res = await fetchOrderList();
          onListRefreshed?.(res);
        }
      } catch (err) {
        onError?.(err);
      } finally {
        setIsRefreshing(false);
        inflightRef.current = null;

        // 在刷新期间新加入的 ids，再跑一轮
        if (pendingIdsRef.current.size > 0) {
          timerRef.current = setTimeout(() => {
            void flush();
          }, 0);
        }
      }
    };

    inflightRef.current = doRefresh();
    await inflightRef.current;
  }, [fetchOrderList, onOrderUpdated, onListRefreshed, onError]);

  const scheduleRefresh = useCallback(
    (orderId: string) => {
      pendingIdsRef.current.add(orderId);
      if (timerRef.current) clearTimeout(timerRef.current);
      timerRef.current = setTimeout(() => {
        timerRef.current = null;
        void flush();
      }, debounceMs);
    },
    [debounceMs, flush],
  );

  const refreshAll = useCallback(async () => {
    if (inflightRef.current) return;
    inflightRef.current = (async () => {
      setIsRefreshing(true);
      try {
        const res = await fetchOrderList();
        onListRefreshed?.(res);
      } catch (err) {
        onError?.(err);
      } finally {
        setIsRefreshing(false);
        inflightRef.current = null;
      }
    })();
    await inflightRef.current;
  }, [fetchOrderList, onListRefreshed, onError]);

  return { scheduleRefresh, isRefreshing, refreshAll };
}

export default useOrderListRefresh;
