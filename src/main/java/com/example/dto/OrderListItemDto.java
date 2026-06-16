package com.example.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * 订单列表项响应 DTO。
 * <p>
 * 为支持前端审批倒计时功能，新增以下字段：
 * <ul>
 *   <li>{@code preoccupyDuration} - 预占时效（小时）</li>
 *   <li>{@code approvalStartTime} - 审批开始时间（ISO 8601）</li>
 *   <li>{@code orderCreateTime} - 订单创建时间（ISO 8601）</li>
 * </ul>
 * 前端根据 {@code preoccupyDuration} 与 {@code approvalStartTime}（缺失时降级为
 * {@code orderCreateTime}）计算倒计时。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderListItemDto {

    /** 订单 ID */
    private String orderId;

    /** 订单状态（后端统一定义，前端依赖此值判断是否展示倒计时） */
    private String status;

    /** 对接抬头 ID，用于查询预占时效配置 */
    private String dockingHeaderId;

    /**
     * 预占时效（小时）。
     * <p>
     * 未配置或 Webshop 不可达时为 null，前端不展示倒计时。
     * 取值范围：1 ~ 720（1 分钟 ~ 30 天）。
     */
    private Integer preoccupyDuration;

    /**
     * 审批开始时间（ISO 8601）。
     * <p>
     * 优先从审批记录表获取审批提交时间；若无审批记录则为 null，
     * 前端降级使用 {@link #orderCreateTime} 计算倒计时。
     */
    private String approvalStartTime;

    /**
     * 订单创建时间（ISO 8601）。
     * <p>
     * 始终非空，作为 approvalStartTime 的降级值。
     */
    private String orderCreateTime;

    // ----- getters / setters -----

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDockingHeaderId() {
        return dockingHeaderId;
    }

    public void setDockingHeaderId(String dockingHeaderId) {
        this.dockingHeaderId = dockingHeaderId;
    }

    public Integer getPreoccupyDuration() {
        return preoccupyDuration;
    }

    public void setPreoccupyDuration(Integer preoccupyDuration) {
        this.preoccupyDuration = preoccupyDuration;
    }

    public String getApprovalStartTime() {
        return approvalStartTime;
    }

    public void setApprovalStartTime(String approvalStartTime) {
        this.approvalStartTime = approvalStartTime;
    }

    public String getOrderCreateTime() {
        return orderCreateTime;
    }

    public void setOrderCreateTime(String orderCreateTime) {
        this.orderCreateTime = orderCreateTime;
    }

    /**
     * 返回倒计时起始时间：优先 approvalStartTime，缺失时降级为 orderCreateTime。
     */
    public Instant resolveCountdownStartTime() {
        String raw = (approvalStartTime != null && !approvalStartTime.isBlank())
                ? approvalStartTime
                : orderCreateTime;
        return raw == null ? null : Instant.parse(raw);
    }
}
