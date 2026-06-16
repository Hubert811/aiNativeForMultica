package com.example.service;

import com.example.dto.OrderListResponse;
import com.example.entity.ApprovalRecordEntity;
import com.example.entity.OrderEntity;
import com.example.enums.OrderStatus;
import com.example.repository.ApprovalRecordRepository;
import com.example.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

class OrderListServiceTest {

    private OrderRepository orderRepository;
    private ApprovalRecordRepository approvalRecordRepository;
    private PreoccupyDurationCacheService cacheService;
    private OrderListService service;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        approvalRecordRepository = mock(ApprovalRecordRepository.class);
        cacheService = mock(PreoccupyDurationCacheService.class);
        service = new OrderListService(orderRepository, approvalRecordRepository, cacheService);
    }

    @Test
    void listOrders_emptyPage() {
        when(orderRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        OrderListResponse response = service.listOrders(0, 20);

        assertEquals(0, response.getItems().size());
        assertEquals(0, response.getTotal());
        verifyNoInteractions(cacheService);
        verifyNoInteractions(approvalRecordRepository);
    }

    @Test
    void listOrders_pendingApproval_enrichesDto() {
        OrderEntity order = new OrderEntity();
        order.setOrderId("ORD-001");
        order.setStatus(OrderStatus.PENDING_APPROVAL);
        order.setDockingHeaderId("HDR-1");
        order.setOrderCreateTime(Instant.parse("2026-06-15T10:00:00Z"));

        when(orderRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(order)));
        when(cacheService.batchGet(anyCollection())).thenReturn(Map.of("HDR-1", 24));

        Instant approvalTime = Instant.parse("2026-06-15T10:05:00Z");
        ApprovalRecordEntity record = new ApprovalRecordEntity();
        record.setOrderId("ORD-001");
        record.setSubmitTime(approvalTime);
        when(approvalRecordRepository.findByOrderIdIn(anyCollection())).thenReturn(List.of(record));

        OrderListResponse response = service.listOrders(0, 20);

        assertEquals(1, response.getItems().size());
        var dto = response.getItems().get(0);
        assertEquals("ORD-001", dto.getOrderId());
        assertEquals("pending_approval", dto.getStatus());
        assertEquals(24, dto.getPreoccupyDuration());
        assertEquals("2026-06-15T10:05:00Z", dto.getApprovalStartTime());
        assertEquals("2026-06-15T10:00:00Z", dto.getOrderCreateTime());
    }

    @Test
    void listOrders_nonPendingStatus_noPreoccupyDuration() {
        OrderEntity order = new OrderEntity();
        order.setOrderId("ORD-002");
        order.setStatus(OrderStatus.SHIPPED);
        order.setDockingHeaderId("HDR-2");
        order.setOrderCreateTime(Instant.parse("2026-06-15T10:00:00Z"));

        when(orderRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(order)));
        when(approvalRecordRepository.findByOrderIdIn(anyCollection())).thenReturn(Collections.emptyList());

        OrderListResponse response = service.listOrders(0, 20);
        var dto = response.getItems().get(0);

        assertNull(dto.getPreoccupyDuration());
        assertNull(dto.getApprovalStartTime());
        verifyNoInteractions(cacheService); // 非待审批订单不查询预占时效
    }

    @Test
    void listOrders_noApprovalRecord_approvalStartTimeFallsBackToNull() {
        // approvalStartTime 为 null 时，前端降级使用 orderCreateTime
        OrderEntity order = new OrderEntity();
        order.setOrderId("ORD-003");
        order.setStatus(OrderStatus.PENDING_APPROVAL);
        order.setDockingHeaderId("HDR-3");
        order.setOrderCreateTime(Instant.parse("2026-06-15T10:00:00Z"));

        when(orderRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(order)));
        when(cacheService.batchGet(anyCollection())).thenReturn(Map.of("HDR-3", 12));
        when(approvalRecordRepository.findByOrderIdIn(anyCollection())).thenReturn(Collections.emptyList());

        OrderListResponse response = service.listOrders(0, 20);
        var dto = response.getItems().get(0);

        assertEquals(12, dto.getPreoccupyDuration());
        assertNull(dto.getApprovalStartTime()); // 后端不填充，前端降级
        assertEquals("2026-06-15T10:00:00Z", dto.getOrderCreateTime());
    }
}
