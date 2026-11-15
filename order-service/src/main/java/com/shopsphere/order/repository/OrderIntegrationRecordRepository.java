package com.shopsphere.order.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shopsphere.order.entity.OrderIntegrationRecord;

@Repository
public interface OrderIntegrationRecordRepository extends JpaRepository<OrderIntegrationRecord, Long> {

    Optional<OrderIntegrationRecord> findByIdempotencyKey(String idempotencyKey);

    List<OrderIntegrationRecord> findByOrderId(Long orderId);
}
