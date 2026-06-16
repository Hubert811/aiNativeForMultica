package com.example.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * 审批记录实体。
 * <p>
 * 用于获取审批开始时间（即审批提交时间）。若订单无审批记录，
 * 后端降级使用 orderCreateTime 作为倒计时起始时间。
 */
@Entity
@Table(name = "t_approval_record")
public class ApprovalRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    /** 审批提交时间，即倒计时起始时间 */
    @Column(name = "submit_time", nullable = false)
    private Instant submitTime;

    // ----- getters / setters -----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public Instant getSubmitTime() { return submitTime; }
    public void setSubmitTime(Instant submitTime) { this.submitTime = submitTime; }
}
