package com.example.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * 订单实体。
 */
@Entity
@Table(name = "t_order")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private com.example.enums.OrderStatus status;

    @Column(name = "docking_header_id")
    private String dockingHeaderId;

    @Column(name = "order_create_time", nullable = false)
    private Instant orderCreateTime;

    // ----- getters / setters -----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public com.example.enums.OrderStatus getStatus() { return status; }
    public void setStatus(com.example.enums.OrderStatus status) { this.status = status; }

    public String getDockingHeaderId() { return dockingHeaderId; }
    public void setDockingHeaderId(String dockingHeaderId) { this.dockingHeaderId = dockingHeaderId; }

    public Instant getOrderCreateTime() { return orderCreateTime; }
    public void setOrderCreateTime(Instant orderCreateTime) { this.orderCreateTime = orderCreateTime; }
}
