import { calculateRemaining } from './index';

describe('calculateRemaining', () => {
  const now = new Date('2026-06-16T12:00:00Z').getTime();

  it('returns expired when time is up', () => {
    const result = calculateRemaining(
      2,
      '2026-06-16T09:00:00Z', // 3 hours ago, duration 2h
      '2026-06-16T08:00:00Z',
      now,
    );
    expect(result.expired).toBe(true);
    expect(result.hours).toBe(0);
    expect(result.minutes).toBe(0);
  });

  it('calculates hours and minutes correctly', () => {
    const result = calculateRemaining(
      24,
      '2026-06-16T10:30:00Z', // 1.5h ago
      '2026-06-16T08:00:00Z',
      now,
    );
    // duration 24h - 1.5h elapsed = 22h 30m
    expect(result.expired).toBe(false);
    expect(result.hours).toBe(22);
    expect(result.minutes).toBe(30);
  });

  it('falls back to orderCreateTime when approvalStartTime is null', () => {
    const result = calculateRemaining(
      24,
      null,
      '2026-06-16T11:00:00Z', // 1h ago
      now,
    );
    expect(result.expired).toBe(false);
    expect(result.hours).toBe(23);
    expect(result.minutes).toBe(0);
  });

  it('returns expired when remaining is exactly zero', () => {
    const result = calculateRemaining(
      1,
      '2026-06-16T11:00:00Z',
      '2026-06-16T10:00:00Z',
      now,
    );
    expect(result.expired).toBe(true);
  });

  it('returns urgent-range values when under 1 hour remains', () => {
    const result = calculateRemaining(
      2,
      '2026-06-16T11:15:00Z', // 45 min ago
      '2026-06-16T10:00:00Z',
      now,
    );
    expect(result.expired).toBe(false);
    expect(result.hours).toBe(1);
    expect(result.minutes).toBe(15);
  });
});
