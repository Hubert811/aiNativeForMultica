package com.example.repository;

import com.example.entity.ApprovalRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalRecordRepository extends JpaRepository<ApprovalRecordEntity, Long> {

    /**
     * 查询每个 orderId 最新的一条审批记录（按 submitTime 倒序取第一条）。
     * 若某订单无审批记录，则不出现在结果中，由 Service 层降级为 orderCreateTime。
     */
    List<ApprovalRecordEntity> findByOrderIdIn(Collection<String> orderIds);

    Optional<ApprovalRecordEntity> findFirstByOrderIdOrderBySubmitTimeDesc(String orderId);
}
