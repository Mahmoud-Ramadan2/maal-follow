package com.mahmoud.maalflow.modules.installments.profit.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerMonthlyProfitRepository;
import com.mahmoud.maalflow.modules.installments.partner.service.PartnerProfitCalculationService;
import com.mahmoud.maalflow.modules.installments.profit.entity.MonthlyProfitDistribution;
import com.mahmoud.maalflow.modules.installments.profit.enums.ProfitDistributionStatus;
import com.mahmoud.maalflow.modules.installments.profit.repo.MonthlyProfitDistributionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthlyProfitDistributionServiceTest {

    @Mock
    private MonthlyProfitDistributionRepository repository;

    @Mock
    private PartnerProfitCalculationService partnerProfitCalculationService;

    @Mock
    private PartnerMonthlyProfitRepository partnerMonthlyProfitRepository;

    @InjectMocks
    private MonthlyProfitDistributionService service;

//    @Test
//    void calculateProfit_pendingDistribution_movesToCalculated() {
//        MonthlyProfitDistribution distribution = newDistribution(ProfitDistributionStatus.PENDING);
//        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(distribution));
//        when(repository.save(any(MonthlyProfitDistribution.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        MonthlyProfitDistribution result = service.calculateProfit(1L);
//
//        assertEquals(ProfitDistributionStatus.CALCULATED, result.getStatus());
//        assertEquals(new BigDecimal("80.00"), result.getDistributableProfit());
//        verify(repository).save(distribution);
//    }

//    @Test
//    void calculateProfit_distributedDistribution_rejected() {
//        MonthlyProfitDistribution distribution = newDistribution(ProfitDistributionStatus.DISTRIBUTED);
//        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(distribution));
//
//        BusinessException ex = assertThrows(BusinessException.class, () -> service.calculateProfit(1L));
//
//        assertEquals("messages.profit.distribution.recalculateAfterDistribution.notAllowed", ex.getMessageKey());
//    }

    @Test
    void distributeProfit_calculatedDistribution_generatesPartnerAllocationsAndMovesState() {
        MonthlyProfitDistribution distribution = newDistribution(ProfitDistributionStatus.CALCULATED);
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(distribution));
        when(partnerMonthlyProfitRepository.findByProfitDistributionId(1L)).thenReturn(Collections.emptyList());
        when(repository.save(any(MonthlyProfitDistribution.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MonthlyProfitDistribution result = service.distributeProfit(1L);

        assertEquals(ProfitDistributionStatus.DISTRIBUTED, result.getStatus());
        verify(partnerProfitCalculationService).calculateAndDistributeMonthlyProfits(distribution);
        verify(repository).save(distribution);
    }

    @Test
    void distributeProfit_existingPartnerRows_rejected() {
        MonthlyProfitDistribution distribution = newDistribution(ProfitDistributionStatus.CALCULATED);
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(distribution));
        when(partnerMonthlyProfitRepository.findByProfitDistributionId(1L))
                .thenReturn(Collections.singletonList(new com.mahmoud.maalflow.modules.installments.partner.entity.PartnerMonthlyProfit()));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.distributeProfit(1L));

        assertEquals("messages.profit.distribution.alreadyGenerated", ex.getMessageKey());
    }

    @Test
    void lockDistribution_distributedDistribution_movesToLocked() {
        MonthlyProfitDistribution distribution = newDistribution(ProfitDistributionStatus.DISTRIBUTED);
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(distribution));
        when(repository.save(any(MonthlyProfitDistribution.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MonthlyProfitDistribution result = service.lockDistribution(1L);

        assertEquals(ProfitDistributionStatus.LOCKED, result.getStatus());
        verify(repository).save(distribution);
    }

    @Test
    void lockDistribution_nonDistributed_rejected() {
        MonthlyProfitDistribution distribution = newDistribution(ProfitDistributionStatus.CALCULATED);
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(distribution));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.lockDistribution(1L));

        assertEquals("messages.profit.distribution.notDistributed", ex.getMessageKey());
    }

    private MonthlyProfitDistribution newDistribution(ProfitDistributionStatus status) {
        MonthlyProfitDistribution distribution = new MonthlyProfitDistribution();
        distribution.setId(1L);
        distribution.setMonthYear("2026-04");
        distribution.setTotalProfit(new BigDecimal("100.00"));
        distribution.setManagementFeePercentage(new BigDecimal("10.00"));
        distribution.setZakatPercentage(new BigDecimal("10.00"));
        distribution.setStatus(status);
        return distribution;
    }
}

