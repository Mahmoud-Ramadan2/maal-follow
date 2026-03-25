package com.mahmoud.maalflow.modules.installments.partner.repo;

import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerProfitCalculationConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PartnerProfitCalculationConfigRepository extends JpaRepository<PartnerProfitCalculationConfig, Long> {

    Optional<PartnerProfitCalculationConfig> findFirstByIsActiveTrueOrderByCreatedAtDesc();

    Optional<PartnerProfitCalculationConfig> findByIsActiveTrue();
}
