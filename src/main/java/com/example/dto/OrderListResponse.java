package com.example.dto;

import java.util.List;

/**
 * 订单列表分页响应。
 */
public class OrderListResponse {

    private List<OrderListItemDto> items;
    private long total;
    private int page;
    private int pageSize;

    public OrderListResponse() {
    }

    public OrderListResponse(List<OrderListItemDto> items, long total, int page, int pageSize) {
        this.items = items;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }

    public List<OrderListItemDto> getItems() {
        return items;
    }

    public void setItems(List<OrderListItemDto> items) {
        this.items = items;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
