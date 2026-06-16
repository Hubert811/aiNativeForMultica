package com.example.service;

import com.example.dto.OrderListItemDto;
import com.example.dto.OrderListResponse;
import com.example.entity.ApprovalRecordEntity;
import com.example.entity.OrderEntity;
import com.example.enums.OrderStatus;
import com.example.repository.ApprovalRecordRepository;
import com.example.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 订单列表查询服务。
 * <p>
 * 在返回前端之前，为每条订单补充审批倒计时所需字段：
 * <ul>
 *   <li>{@code preoccupyDuration} - 从 Webshop 获取（本地缓存）</li>
 *   <li>{@code approvalStartTime} - 从审批记录表获取，缺失时前端降级用 orderCreateTime</li>
 *   <li>{@code orderCreateTime} - 订单创建时间</li>
 * </ul>
 */
@Service
public class OrderListService {

    private static final Logger log = LoggerFactory.getLogger(OrderListService.class);

    private final OrderRepository orderRepository;
    private final ApprovalRecordRepository approvalRecordRepository;
    private final PreoccupyDurationCacheService preoccupyDurationCacheService;

    public OrderListService(OrderRepository orderRepository,
                            ApprovalRecordRepository approvalRecordRepository,
                            PreoccupyDurationCacheService preoccupyDurationCacheService) {
        this.orderRepository = orderRepository;
        this.approvalRecordRepository = approvalRecordRepository;
        this.preoccupyDurationCacheService = preoccupyDurationCacheService;
    }

    /**
     * 分页查询订单列表并填充倒计时字段。
     */
    public OrderListResponse listOrders(int page, int pageSize) {
        Page<OrderEntity> pageResult = orderRepository.findAll(PageRequest.of(page, pageSize));
        List<OrderEntity> orders = pageResult.getContent();

        if (orders.isEmpty()) {
            return new OrderListResponse(Collections.emptyList(), pageResult.getTotalElements(), page, pageSize);
        }

        // 1. 批量查询预占时效（缓存）
        List<String> headerIds = orders.stream()
                .map(OrderEntity::getDockingHeaderId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<String, Integer> durationMap = preoccupyDurationCacheService.batchGet(headerIds);

        // 2. 批量查询审批开始时间
        List<String> orderIds = orders.stream()
                .map(OrderEntity::getOrderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Map<String, Instant> approvalStartMap = loadApprovalStartTimes(orderIds);

        // 3. 组装 DTO
        List<OrderListItemDto> items = orders.stream()
                .map(order -> toDto(order, durationMap, approvalStartMap))
                .collect(Collectors.toList());

        return new OrderListResponse(items, pageResult.getTotalElements(), page, pageSize);
    }

    private Map<String, Instant> loadApprovalStartTimes(List<String> orderIds) {
        if (orderIds.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            List<ApprovalRecordEntity> records = approvalRecordRepository.findByOrderIdIn(orderIds);
            // 每个 orderId 取最新的一条 submitTime
            return records.stream()
                    .collect(Collectors.toMap(
                            ApprovalRecordEntity::getOrderId,
                            ApprovalRecordEntity::getSubmitTime,
                            (a, b) -> a.isAfter(b) ? a : b));
        } catch (Exception e) {
            log.warn("Failed to load approval records, degrading to orderCreateTime", e);
            return Collections.emptyMap();
        }
    }

    private OrderListItemDto toDto(OrderEntity order,
                                   Map<String, Integer> durationMap,
                                   Map<String, Instant> approvalStartMap) {
        OrderListItemDto dto = new OrderListItemDto();
        dto.setOrderId(order.getOrderId());
        dto.setStatus(order.getStatus() == null ? null : order.getStatus().getValue());
        dto.setDockingHeaderId(order.getDockingHeaderId());
        dto.setOrderCreateTime(order.getOrderCreateTime() == null
                ? null
                : order.getOrderCreateTime().toString());

        // 仅"待审批"类状态才填充 preoccupyDuration（其它状态前端不会展示倒计时）
        String statusValue = dto.getStatus();
        if (OrderStatus.isPendingApprovalStatus(statusValue) && order.getDockingHeaderId() != null) {
            dto.setPreoccupyDuration(durationMap.get(order.getDockingHeaderId()));
        }

        Instant approvalStart = approvalStartMap.get(order.getOrderId());
        if (approvalStart != null) {
            dto.setApprovalStartTime(approvalStart.toString());
        }
        // approvalStartTime 为 null 时，前端降级使用 orderCreateTime，后端不再冗余填充

        return dto;
    }
}
