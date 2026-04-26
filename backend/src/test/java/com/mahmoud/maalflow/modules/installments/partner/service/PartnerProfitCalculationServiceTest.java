package com.mahmoud.maalflow.modules.installments.partner.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.modules.installments.partner.entity.Partner;
import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerMonthlyProfit;
import com.mahmoud.maalflow.modules.installments.partner.enums.PartnerStatus;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerInvestmentRepository;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerMonthlyProfitRepository;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerRepository;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerWithdrawalRepository;
import com.mahmoud.maalflow.modules.installments.profit.entity.MonthlyProfitDistribution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartnerProfitCalculationServiceTest {

    @Mock
    private PartnerRepository partnerRepository;

    @Mock
    private PartnerMonthlyProfitRepository partnerMonthlyProfitRepository;

    @Mock
    private PartnerInvestmentRepository partnerInvestmentRepository;

    @Mock
    private PartnerWithdrawalRepository partnerWithdrawalRepository;

    @InjectMocks
    private PartnerProfitCalculationService service;

    @Test
    void calculateAndDistributeMonthlyProfits_usesMonthEndCutoffAndPersistsFrozenSnapshots() {
        MonthlyProfitDistribution distribution = new MonthlyProfitDistribution();
        distribution.setMonthYear("2026-07");
        distribution.setDistributableProfit(new BigDecimal("1000.00"));

        Partner p2 = partner(2L, PartnerStatus.ACTIVE, null);
        Partner p1 = partner(1L, PartnerStatus.ACTIVE, null);
        when(partnerRepository.findByProfitSharingActive(true)).thenReturn(List.of(p2, p1));

        LocalDateTime monthEnd = YearMonth.parse("2026-07").atEndOfMonth().atTime(LocalTime.MAX);
        LocalDateTime investmentCutoff = monthEnd.minusMonths(2);

        when(partnerInvestmentRepository.sumConfirmedInvestmentsByPartnerBeforeDate(1L, investmentCutoff))
                .thenReturn(new BigDecimal("3000.00"));
        when(partnerWithdrawalRepository.sumApprovedOrCompletedWithdrawalsByPartnerUpToDate(1L, monthEnd))
                .thenReturn(new BigDecimal("500.00"));

        when(partnerInvestmentRepository.sumConfirmedInvestmentsByPartnerBeforeDate(2L, investmentCutoff))
                .thenReturn(new BigDecimal("1000.00"));
        when(partnerWithdrawalRepository.sumApprovedOrCompletedWithdrawalsByPartnerUpToDate(2L, monthEnd))
                .thenReturn(BigDecimal.ZERO);

        when(partnerMonthlyProfitRepository.save(any(PartnerMonthlyProfit.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.calculateAndDistributeMonthlyProfits(distribution);

        verify(partnerInvestmentRepository).sumConfirmedInvestmentsByPartnerBeforeDate(1L, investmentCutoff);
        verify(partnerInvestmentRepository).sumConfirmedInvestmentsByPartnerBeforeDate(2L, investmentCutoff);
        verify(partnerWithdrawalRepository).sumApprovedOrCompletedWithdrawalsByPartnerUpToDate(1L, monthEnd);
        verify(partnerWithdrawalRepository).sumApprovedOrCompletedWithdrawalsByPartnerUpToDate(2L, monthEnd);

        ArgumentCaptor<PartnerMonthlyProfit> captor = ArgumentCaptor.forClass(PartnerMonthlyProfit.class);
        verify(partnerMonthlyProfitRepository, times(2)).save(captor.capture());
        List<PartnerMonthlyProfit> rows = captor.getAllValues();

        PartnerMonthlyProfit first = rows.get(0);
        PartnerMonthlyProfit second = rows.get(1);

        assertEquals(1L, first.getPartner().getId());
        assertEquals(new BigDecimal("2500.00"), first.getInvestmentAmount());
        assertEquals(new BigDecimal("71.43"), first.getSharePercentage());
        assertEquals(new BigDecimal("714.28"), first.getCalculatedProfit());

        assertEquals(2L, second.getPartner().getId());
        assertEquals(new BigDecimal("1000.00"), second.getInvestmentAmount());
        assertEquals(new BigDecimal("28.57"), second.getSharePercentage());
        assertEquals(new BigDecimal("285.72"), second.getCalculatedProfit());
    }

    @Test
    void calculateAndDistributeMonthlyProfits_excludesPartnerInactivatedWithinMonthAndAddsAuditNote() {
        MonthlyProfitDistribution distribution = new MonthlyProfitDistribution();
        distribution.setMonthYear("2026-07");
        distribution.setDistributableProfit(new BigDecimal("500.00"));

        Partner active = partner(1L, PartnerStatus.ACTIVE, null);
        Partner inactiveMidMonth = partner(2L, PartnerStatus.INACTIVE, LocalDateTime.of(2026, 7, 10, 12, 0));

        when(partnerRepository.findByProfitSharingActive(true)).thenReturn(List.of(active, inactiveMidMonth));
        when(partnerInvestmentRepository.sumConfirmedInvestmentsByPartnerBeforeDate(eq(1L), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("1000.00"));
        when(partnerWithdrawalRepository.sumApprovedOrCompletedWithdrawalsByPartnerUpToDate(eq(1L), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);
        when(partnerMonthlyProfitRepository.save(any(PartnerMonthlyProfit.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.calculateAndDistributeMonthlyProfits(distribution);

        verify(partnerInvestmentRepository, never())
                .sumConfirmedInvestmentsByPartnerBeforeDate(eq(2L), any(LocalDateTime.class));
        verify(partnerMonthlyProfitRepository, times(1)).save(any(PartnerMonthlyProfit.class));
        assertTrue(distribution.getCalculationNotes().contains("Excluded partner 2"));
    }

    @Test
    void calculateAndDistributeMonthlyProfits_noEligiblePartners_throwsBusinessException() {
        MonthlyProfitDistribution distribution = new MonthlyProfitDistribution();
        distribution.setMonthYear("2026-07");
        distribution.setDistributableProfit(new BigDecimal("100.00"));

        Partner inactiveMidMonth = partner(2L, PartnerStatus.INACTIVE, LocalDateTime.of(2026, 7, 5, 9, 0));
        when(partnerRepository.findByProfitSharingActive(true)).thenReturn(List.of(inactiveMidMonth));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.calculateAndDistributeMonthlyProfits(distribution));

        assertEquals("messages.partner.monthlyProfit.eligiblePartner.empty", ex.getMessageKey());
        assertTrue(distribution.getCalculationNotes().contains("Excluded partner 2"));
    }

    private Partner partner(Long id, PartnerStatus status, LocalDateTime updatedAt) {
        Partner partner = new Partner();
        partner.setId(id);
        partner.setStatus(status);
        partner.setProfitSharingActive(true);
        partner.setUpdatedAt(updatedAt);
        return partner;
    }
}

