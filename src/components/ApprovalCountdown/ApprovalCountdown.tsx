/**
 * ApprovalCountdown - 审批倒计时组件
 *
 * 在待审批订单卡片中展示审批剩余时间，倒计时归零时触发刷新回调。
 *
 * 依赖：
 * - TES-38 (前端组件开发) 的交付物
 * - TES-37 (后端 DTO 字段) 联调后的 preoccupyDuration / approvalStartTime 字段
 */
import { useEffect, useMemo, useState } from 'react';
import clsx from 'clsx';
import styles from './ApprovalCountdown.module.css';

export interface ApprovalCountdownProps {
  /** 预占时效（小时），未配置时为 null 或 0 */
  preoccupyDuration: number | null;
  /** 审批开始时间（ISO 8601），为空时使用 orderCreateTime */
  approvalStartTime: string | null;
  /** 订单创建时间（ISO 8601） */
  orderCreateTime: string;
  /** 订单状态 */
  orderStatus: string;
  /** 倒计时归零回调，用于触发列表刷新 */
  onExpired?: () => void;
}

/** 待审批状态枚举（与后端保持一致） */
const PENDING_APPROVAL_STATUSES = new Set([
  'pending_approval',
  'approval_in_progress',
]);

function isPendingApprovalStatus(status: string): boolean {
  return PENDING_APPROVAL_STATUSES.has(status);
}

interface RemainingTime {
  hours: number;
  minutes: number;
  expired: boolean;
}

function calculateRemaining(
  preoccupyDuration: number,
  approvalStartTime: string | null,
  orderCreateTime: string,
): RemainingTime {
  const durationMs = preoccupyDuration * 3600 * 1000;
  const startTime = approvalStartTime
    ? new Date(approvalStartTime).getTime()
    : new Date(orderCreateTime).getTime();

  if (Number.isNaN(startTime)) {
    return { hours: 0, minutes: 0, expired: true };
  }

  const remaining = durationMs - (Date.now() - startTime);
  if (remaining <= 0) {
    return { hours: 0, minutes: 0, expired: true };
  }

  return {
    hours: Math.floor(remaining / 3600000),
    minutes: Math.floor((remaining % 3600000) / 60000),
    expired: false,
  };
}

/** 刷新间隔：60 秒 */
const TICK_INTERVAL_MS = 60_000;

export const ApprovalCountdown: React.FC<ApprovalCountdownProps> = ({
  preoccupyDuration,
  approvalStartTime,
  orderCreateTime,
  orderStatus,
  onExpired,
}) => {
  const shouldShow =
    preoccupyDuration != null &&
    preoccupyDuration > 0 &&
    isPendingApprovalStatus(orderStatus);

  const [remaining, setRemaining] = useState<RemainingTime>(() => {
    if (!shouldShow) {
      return { hours: 0, minutes: 0, expired: true };
    }
    return calculateRemaining(preoccupyDuration!, approvalStartTime, orderCreateTime);
  });

  // 输入变化时重新计算
  useEffect(() => {
    if (!shouldShow) return;
    setRemaining(calculateRemaining(preoccupyDuration!, approvalStartTime, orderCreateTime));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [preoccupyDuration, approvalStartTime, orderCreateTime, orderStatus]);

  // 定时刷新 + 过期处理
  useEffect(() => {
    if (!shouldShow || remaining.expired) {
      if (remaining.expired && shouldShow) {
        onExpired?.();
      }
      return;
    }

    const timer = setInterval(() => {
      const next = calculateRemaining(preoccupyDuration!, approvalStartTime, orderCreateTime);
      setRemaining(next);
      if (next.expired) {
        onExpired?.();
        clearInterval(timer);
      }
    }, TICK_INTERVAL_MS);

    return () => clearInterval(timer);
  }, [shouldShow, remaining.expired, preoccupyDuration, approvalStartTime, orderCreateTime, onExpired]);

  // Page Visibility 优化：浏览器后台标签页 setInterval 可能被节流
  useEffect(() => {
    if (!shouldShow) return;
    const handleVisibility = () => {
      if (document.visibilityState === 'visible') {
        const next = calculateRemaining(preoccupyDuration!, approvalStartTime, orderCreateTime);
        setRemaining(next);
        if (next.expired) onExpired?.();
      }
    };
    document.addEventListener('visibilitychange', handleVisibility);
    return () => document.removeEventListener('visibilitychange', handleVisibility);
  }, [shouldShow, preoccupyDuration, approvalStartTime, orderCreateTime, onExpired]);

  if (!shouldShow) return null;

  const isUrgent = remaining.hours < 1;

  return (
    <div className={styles.countdownContainer} data-testid="approval-countdown">
      <span
        className={clsx(styles.countdownText, {
          [styles.urgent]: isUrgent,
        })}
      >
        审批剩余 {remaining.hours} 时 {remaining.minutes} 分
      </span>
      <span
        className={clsx(styles.countdownHint, {
          [styles.urgent]: isUrgent,
        })}
      >
        超期后订单自动取消
      </span>
    </div>
  );
};

export default ApprovalCountdown;
