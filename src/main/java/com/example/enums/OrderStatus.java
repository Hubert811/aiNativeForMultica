package com.example.enums;

import java.util.Set;

/**
 * 订单状态枚举。
 * <p>
 * 统一管理"待审批"类状态，前端依赖后端返回的 status 值判断是否展示倒计时。
 */
public enum OrderStatus {

    /** 待审批 */
    PENDING_APPROVAL("pending_approval"),

    /** 审批中 */
    APPROVAL_IN_PROGRESS("approval_in_progress"),

    /** 已取消 */
    CANCELLED("cancelled"),

    /** 已完成 */
    COMPLETED("completed"),

    /** 已发货 */
    SHIPPED("shipped");

    private static final Set<String> PENDING_APPROVAL_STATUSES = Set.of(
            PENDING_APPROVAL.value,
            APPROVAL_IN_PROGRESS.value
    );

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * 判断给定状态字符串是否属于"待审批"类状态。
     * <p>
     * 只有待审批类状态的订单才展示审批倒计时。
     *
     * @param orderStatus 订单状态字符串（原始值）
     * @return 如果属于待审批类状态返回 true，否则返回 false
     */
    public static boolean isPendingApprovalStatus(String orderStatus) {
        return orderStatus != null && PENDING_APPROVAL_STATUSES.contains(orderStatus);
    }
}
