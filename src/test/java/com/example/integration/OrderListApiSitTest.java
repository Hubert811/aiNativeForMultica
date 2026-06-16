package com.example.integration;

import com.example.Application;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SIT 集成测试：验证订单列表接口返回结构包含倒计时所需字段。
 * <p>
 * 需 TestContainers 提供数据库，此处仅做冒烟验证（真实环境由 CI 触发）。
 */
@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OrderListApiSitTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void listOrders_returnsOkAndExpectedFields() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/orders", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // 响应结构包含 items / total / page / pageSize
        assertTrue(response.getBody().contains("\"items\""));
        assertTrue(response.getBody().contains("\"total\""));
    }
}
