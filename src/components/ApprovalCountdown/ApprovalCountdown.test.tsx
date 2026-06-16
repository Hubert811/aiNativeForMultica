/**
 * ApprovalCountdown 单元测试
 *
 * 覆盖验收标准：
 * - mock preoccupyDuration=24 时展示"审批剩余 X 时 Y 分"
 * - 剩余 ≤ 1 小时切换为红色加粗样式（urgent class）
 * - preoccupyDuration=null 或非待审批状态时不渲染
 * - 倒计时归零触发 onExpired 回调
 */
import { act, render, screen } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { ApprovalCountdown } from './ApprovalCountdown';

describe('ApprovalCountdown', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-06-16T12:00:00Z'));
  });
  afterEach(() => {
    vi.useRealTimers();
  });

  const baseProps = {
    orderCreateTime: '2026-06-15T10:00:00Z',
    orderStatus: 'pending_approval',
  };

  it('不渲染：preoccupyDuration 为 null', () => {
    const { container } = render(
      <ApprovalCountdown {...baseProps} preoccupyDuration={null} approvalStartTime={null} />,
    );
    expect(container.firstChild).toBeNull();
  });

  it('不渲染：preoccupyDuration 为 0', () => {
    const { container } = render(
      <ApprovalCountdown {...baseProps} preoccupyDuration={0} approvalStartTime={null} />,
    );
    expect(container.firstChild).toBeNull();
  });

  it('不渲染：非待审批状态', () => {
    const { container } = render(
      <ApprovalCountdown
        {...baseProps}
        orderStatus="shipped"
        preoccupyDuration={24}
        approvalStartTime={null}
      />,
    );
    expect(container.firstChild).toBeNull();
  });

  it('渲染倒计时：preoccupyDuration=24 时显示剩余时间', () => {
    // 当前 12:00，审批开始 10:00 (昨天) -> 实际过了 26 小时？不对，重新算
    // orderCreateTime: 2026-06-15T10:00:00Z，当前 2026-06-16T12:00:00Z -> 过了 26 小时
    // preoccupyDuration=48 -> 剩余 22 小时
    render(
      <ApprovalCountdown
        {...baseProps}
        preoccupyDuration={48}
        approvalStartTime={null}
      />,
    );
    expect(screen.getByTestId('approval-countdown')).toHaveTextContent(
      /审批剩余 22 时 0 分/,
    );
  });

  it('优先使用 approvalStartTime 作为起点', () => {
    // approvalStartTime: 2026-06-16T10:00:00Z，当前 12:00 -> 过了 2 小时
    // preoccupyDuration=24 -> 剩余 22 小时
    render(
      <ApprovalCountdown
        {...baseProps}
        preoccupyDuration={24}
        approvalStartTime="2026-06-16T10:00:00Z"
      />,
    );
    expect(screen.getByTestId('approval-countdown')).toHaveTextContent(
      /审批剩余 22 时 0 分/,
    );
  });

  it('剩余时间 ≤ 1 小时：urgent 样式', () => {
    // approvalStartTime: 2026-06-16T11:30:00Z，当前 12:00 -> 过了 0.5 小时
    // preoccupyDuration=1 -> 剩余 0.5 小时（30 分钟），< 1 小时，urgent
    const { container } = render(
      <ApprovalCountdown
        {...baseProps}
        preoccupyDuration={1}
        approvalStartTime="2026-06-16T11:30:00Z"
      />,
    );
    const text = container.querySelector('.countdownText');
    expect(text?.className).toMatch(/urgent/);
  });

  it('倒计时归零：触发 onExpired', () => {
    const onExpired = vi.fn();
    // approvalStartTime: 2026-06-16T10:00:00Z, preoccupyDuration=2 -> 12:00 时正好归零
    render(
      <ApprovalCountdown
        {...baseProps}
        preoccupyDuration={2}
        approvalStartTime="2026-06-16T10:00:00Z"
        onExpired={onExpired}
      />,
    );
    expect(onExpired).toHaveBeenCalledTimes(1);
  });

  it('每 60 秒自动刷新倒计时', () => {
    // approvalStartTime: 2026-06-16T11:00:00Z, preoccupyDuration=1 -> 剩余 1 小时
    render(
      <ApprovalCountdown
        {...baseProps}
        preoccupyDuration={1}
        approvalStartTime="2026-06-16T11:00:00Z"
      />,
    );
    expect(screen.getByTestId('approval-countdown')).toHaveTextContent(
      /审批剩余 1 时 0 分/,
    );

    act(() => {
      vi.advanceTimersByTime(60_000); // +1 分钟
    });
    expect(screen.getByTestId('approval-countdown')).toHaveTextContent(
      /审批剩余 0 时 59 分/,
    );
  });

  it('Page Visibility：后台恢复时立即重新计算', () => {
    // 进入后台前剩余 59 分，后台过了 10 分钟（setInterval 被节流）
    render(
      <ApprovalCountdown
        {...baseProps}
        preoccupyDuration={1}
        approvalStartTime="2026-06-16T11:00:00Z"
      />,
    );
    // 模拟时间推进 10 分钟（但没触发 interval）
    vi.setSystemTime(new Date('2026-06-16T12:10:00Z'));
    // 触发 visibilitychange
    act(() => {
      Object.defineProperty(document, 'visibilityState', {
        value: 'visible',
        configurable: true,
      });
      document.dispatchEvent(new Event('visibilitychange'));
    });
    // 剩余 = 60 - 70 = 0 -> 已过期
    expect(screen.getByTestId('approval-countdown')).toHaveTextContent(
      /审批剩余 0 时 0 分/,
    );
  });
});
