package com.mahmoud.maalflow.modules.installments.partner.service;


import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.modules.installments.capital.entity.CapitalPool;
import com.mahmoud.maalflow.modules.installments.partner.entity.Partner;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartnerShareService {

    private final PartnerRepository partnerRepository;

    @Transactional
    public void recalculateSharePercentages(BigDecimal totalCapital) {


        BigDecimal safeTotalCapital = nz(totalCapital);

        if (safeTotalCapital.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Total capital is zero or negative ({}). Setting all partner shares to 0%.", safeTotalCapital);
            throw new BusinessException("messages.capitalPool.totalAmount.positive");
        }
        List<Partner> partners = partnerRepository.findAllActivePartners();
        if (partners.isEmpty()) {
            return;
        }

        for (Partner partner : partners) {
            BigDecimal effectiveInvestment = nz(partner.getEffectiveInvestment());
            BigDecimal share = BigDecimal.ZERO;

            if (effectiveInvestment.compareTo(BigDecimal.ZERO) > 0) {
                share = effectiveInvestment
                        .multiply(BigDecimal.valueOf(100))
                        .divide(safeTotalCapital, 2, RoundingMode.HALF_UP);
            }
            partner.setSharePercentage(share);
        }

        partnerRepository.saveAll(partners);
    }

    private BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}