package com.mahmoud.maalflow.modules.installments.payment.repo;

import com.mahmoud.maalflow.modules.installments.payment.entity.PaymentDiscountConfig;
import com.mahmoud.maalflow.modules.installments.payment.enums.DiscountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PaymentDiscountConfig entity.
 */
@Repository
public interface PaymentDiscountConfigRepository extends JpaRepository<PaymentDiscountConfig, Long> {

    Optional<PaymentDiscountConfig> findByDiscountTypeAndIsActiveTrue(DiscountType discountType);

    List<PaymentDiscountConfig> findAllByIsActiveTrue();

    void deleteByDiscountTypeAndIsActiveTrue(DiscountType discountType);
}
