/**
 * useOrderListRefresh 单元测试
 *
 * 覆盖验收标准：
 * - 单条归零：局部更新
 * - 多条同时归零：合并为一次整页刷新
 * - 防抖窗口内多次触发合并为一次请求
 */
import { act, renderHook } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { useOrderListRefresh } from './useOrderListRefresh';
import type { OrderListItem, OrderListResponse } from './types';

const buildResponse = (items: OrderListItem[]): OrderListResponse => ({
  items,
  total: items.length,
  page: 1,
  pageSize: 20,
});

const makeOrder = (id: string): OrderListItem => ({
  orderId: id,
  status: 'pending_approval',
  orderCreateTime: '2026-06-16T10:00:00Z',
  preoccupyDuration: 24,
  approvalStartTime: null,
});

describe('useOrderListRefresh', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });
  afterEach(() => {
    vi.useRealTimers();
  });

  it('单条归零：局部更新，调用 fetchOrderList({ orderId })', async () => {
    const fetchOrderList = vi.fn().mockResolvedValue(buildResponse([makeOrder('ORD-1')]));
    const onOrderUpdated = vi.fn();

    const { result } = renderHook(() =>
      useOrderListRefresh({
        fetchOrderList,
        onOrderUpdated,
        debounceMs: 200,
      }),
    );

    await act(async () => {
      result.current.scheduleRefresh('ORD-1');
      await vi.advanceTimersByTimeAsync(200);
    });

    expect(fetchOrderList).toHaveBeenCalledWith({ orderId: 'ORD-1' });
    expect(onOrderUpdated).toHaveBeenCalledWith(makeOrder('ORD-1'));
  });

  it('多条同时归零（防抖窗口内）：合并为一次整页刷新', async () => {
    const fetchOrderList = vi
      .fn()
      .mockResolvedValue(buildResponse([makeOrder('ORD-1'), makeOrder('ORD-2')]));
    const onListRefreshed = vi.fn();

    const { result } = renderHook(() =>
      useOrderListRefresh({
        fetchOrderList,
        onListRefreshed,
        debounceMs: 200,
      }),
    );

    await act(async () => {
      result.current.scheduleRefresh('ORD-1');
      result.current.scheduleRefresh('ORD-2');
      result.current.scheduleRefresh('ORD-3');
      await vi.advanceTimersByTimeAsync(200);
    });

    // 合并为一次请求（无 orderId 参数 -> 整页刷新）
    expect(fetchOrderList).toHaveBeenCalledTimes(1);
    expect(fetchOrderList).toHaveBeenCalledWith();
    expect(onListRefreshed).toHaveBeenCalledTimes(1);
  });

  it('防抖：200ms 内重复触发同一订单只算一次', async () => {
    const fetchOrderList = vi.fn().mockResolvedValue(buildResponse([makeOrder('ORD-1')]));

    const { result } = renderHook(() =>
      useOrderListRefresh({ fetchOrderList, debounceMs: 200 }),
    );

    await act(async () => {
      result.current.scheduleRefresh('ORD-1');
      result.current.scheduleRefresh('ORD-1');
      result.current.scheduleRefresh('ORD-1');
      await vi.advanceTimersByTimeAsync(200);
    });

    expect(fetchOrderList).toHaveBeenCalledTimes(1);
  });
});
