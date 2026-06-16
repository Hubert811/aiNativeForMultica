import React, { useEffect, useMemo, useRef, useState } from 'react';
import styles from './ApprovalCountdown.module.css';

export interface ApprovalCountdownProps {
  /** 预占时效（小时） */
  preoccupyDuration: number | null;
  /** 审批开始时间（ISO 8601） */
  approvalStartTime: string | null;
  /** 订单创建时间（ISO 8601） */
  orderCreateTime: string;
  /** 订单状态 */
  orderStatus: string;
  /** 倒计时归零回调 */
  onExpired?: () => void;
}

export const PENDING_APPROVAL_STATUSES = [
  'pending_approval',
  'approval_in_progress',
] as const;

export interface RemainingTime {
  hours: number;
  minutes: number;
  expired: boolean;
}

export function calculateRemaining(
  preoccupyDuration: number,
  approvalStartTime: string | null,
  orderCreateTime: string,
  now: number = Date.now(),
): RemainingTime {
  const durationMs = preoccupyDuration * 3600 * 1000;
  const startTime = approvalStartTime
    ? new Date(approvalStartTime).getTime()
    : new Date(orderCreateTime).getTime();
  const remaining = durationMs - (now - startTime);

  if (remaining <= 0) {
    return { hours: 0, minutes: 0, expired: true };
  }
  return {
    hours: Math.floor(remaining / 3600000),
    minutes: Math.floor((remaining % 3600000) / 60000),
    expired: false,
  };
}

const ApprovalCountdown: React.FC<ApprovalCountdownProps> = ({
  preoccupyDuration,
  approvalStartTime,
  orderCreateTime,
  orderStatus,
  onExpired,
}) => {
  const shouldShow = useMemo(() => {
    return (
      preoccupyDuration != null &&
      preoccupyDuration > 0 &&
      (PENDING_APPROVAL_STATUSES as readonly string[]).includes(orderStatus)
    );
  }, [preoccupyDuration, orderStatus]);

  const [remaining, setRemaining] = useState<RemainingTime>(() => {
    if (!shouldShow || preoccupyDuration == null) {
      return { hours: 0, minutes: 0, expired: false };
    }
    return calculateRemaining(preoccupyDuration, approvalStartTime, orderCreateTime);
  });

  const onExpiredRef = useRef(onExpired);
  useEffect(() => {
    onExpiredRef.current = onExpired;
  }, [onExpired]);

  // 60s 定时刷新
  useEffect(() => {
    if (!shouldShow || preoccupyDuration == null) return;

    const tick = () => {
      const next = calculateRemaining(
        preoccupyDuration,
        approvalStartTime,
        orderCreateTime,
      );
      setRemaining(next);
      if (next.expired) {
        onExpiredRef.current?.();
      }
    };

    const timer = window.setInterval(tick, 60 * 1000);
    return () => window.clearInterval(timer);
  }, [shouldShow, preoccupyDuration, approvalStartTime, orderCreateTime]);

  // Page Visibility：回到前台时立即重新计算
  useEffect(() => {
    if (!shouldShow) return;

    const handleVisibility = () => {
      if (document.visibilityState === 'visible' && preoccupyDuration != null) {
        const next = calculateRemaining(
          preoccupyDuration,
          approvalStartTime,
          orderCreateTime,
        );
        setRemaining(next);
        if (next.expired) {
          onExpiredRef.current?.();
        }
      }
    };

    document.addEventListener('visibilitychange', handleVisibility);
    return () => {
      document.removeEventListener('visibilitychange', handleVisibility);
    };
  }, [shouldShow, preoccupyDuration, approvalStartTime, orderCreateTime]);

  if (!shouldShow) return null;

  const isUrgent = remaining.hours < 1;

  return (
    <div className={styles.countdownContainer} data-testid="approval-countdown">
      <span
        className={`${styles.countdownText} ${isUrgent ? styles.urgent : ''}`}
        data-testid="countdown-text"
      >
        审批剩余 {remaining.hours} 时 {remaining.minutes} 分
      </span>
      <span className={styles.countdownHint}>超期后订单自动取消</span>
    </div>
  );
};

export default ApprovalCountdown;
