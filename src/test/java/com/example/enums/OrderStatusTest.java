package com.example.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderStatusTest {

    @Test
    void isPendingApprovalStatus_recognizesExpectedValues() {
        assertTrue(OrderStatus.isPendingApprovalStatus("pending_approval"));
        assertTrue(OrderStatus.isPendingApprovalStatus("approval_in_progress"));
    }

    @Test
    void isPendingApprovalStatus_rejectsNonPendingValues() {
        assertFalse(OrderStatus.isPendingApprovalStatus("shipped"));
        assertFalse(OrderStatus.isPendingApprovalStatus("completed"));
        assertFalse(OrderStatus.isPendingApprovalStatus("cancelled"));
        assertFalse(OrderStatus.isPendingApprovalStatus(null));
        assertFalse(OrderStatus.isPendingApprovalStatus(""));
    }

    @Test
    void values_matchExpected() {
        assertEquals("pending_approval", OrderStatus.PENDING_APPROVAL.getValue());
        assertEquals("approval_in_progress", OrderStatus.APPROVAL_IN_PROGRESS.getValue());
    }
}
