package com.mahmoud.maalflow.modules.installments.partner.service;

import com.mahmoud.maalflow.modules.installments.capital.dto.CapitalTransactionResponse;
import com.mahmoud.maalflow.modules.installments.capital.service.CapitalTransactionService;
import com.mahmoud.maalflow.modules.installments.ledger.dto.LedgerResponse;
import com.mahmoud.maalflow.modules.installments.ledger.enums.LedgerReferenceType;
import com.mahmoud.maalflow.modules.installments.ledger.service.LedgerService;
import com.mahmoud.maalflow.modules.installments.partner.constants.PartnerPayoutReferenceTypes;
import com.mahmoud.maalflow.modules.installments.partner.dto.PayoutReconciliationResponse;
import com.mahmoud.maalflow.modules.installments.partner.entity.Partner;
import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerCommission;
import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerMonthlyProfit;
import com.mahmoud.maalflow.modules.installments.partner.enums.CommissionStatus;
import com.mahmoud.maalflow.modules.installments.partner.enums.ProfitStatus;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerCommissionRepository;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerMonthlyProfitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartnerPayoutReconciliationServiceTest {

    @Mock
    private PartnerCommissionRepository partnerCommissionRepository;

    @Mock
    private PartnerMonthlyProfitRepository partnerMonthlyProfitRepository;

    @Mock
    private LedgerService ledgerService;

    @Mock
    private CapitalTransactionService capitalTransactionService;

    @InjectMocks
    private PartnerPayoutReconciliationService reconciliationService;

    @Test
    void reconcileCommissionPayout_matchingLedgerAndCapital_isFullyReconciled() {
        Partner partner = new Partner();
        partner.setId(5L);

        PartnerCommission commission = PartnerCommission.builder()
                .id(42L)
                .partner(partner)
                .commissionAmount(new BigDecimal("150.00"))
                .status(CommissionStatus.PAID)
                .build();

        LedgerResponse ledger = LedgerResponse.builder()
                .id(101L)
                .idempotencyKey("COMMISSION_42")
                .partnerId(5L)
                .amount(new BigDecimal("150.00"))
                .referenceType(LedgerReferenceType.PROFIT_DISTRIBUTION)
                .referenceId(42L)
                .description("Partner commission payout - SALES_COMMISSION")
                .build();

        CapitalTransactionResponse capital = CapitalTransactionResponse.builder()
                .id(301L)
                .partnerId(5L)
                .amount(new BigDecimal("150.00"))
                .referenceType(PartnerPayoutReferenceTypes.PARTNER_COMMISSION_PAYOUT)
                .referenceId(42L)
                .description("Partner commission payout ID 42")
                .build();

        when(partnerCommissionRepository.findById(42L)).thenReturn(Optional.of(commission));
        when(ledgerService.getByIdempotencyKey("COMMISSION_42")).thenReturn(Optional.of(ledger));
        when(capitalTransactionService.getCapitalTransactionsByReference(
                PartnerPayoutReferenceTypes.PARTNER_COMMISSION_PAYOUT, 42L))
                .thenReturn(Collections.singletonList(capital));

        PayoutReconciliationResponse result = reconciliationService.reconcileCommissionPayout(42L);

        assertTrue(result.isLedgerMatched());
        assertTrue(result.isCapitalMatched());
        assertTrue(result.isFullyReconciled());
        assertTrue(result.getIssues().isEmpty());
        assertEquals(101L, result.getLedgerEntryId());
        assertEquals(301L, result.getCapitalTransactionId());
    }

    @Test
    void reconcileMonthlyProfitPayout_missingCapital_isNotFullyReconciled() {
        Partner partner = new Partner();
        partner.setId(7L);

        PartnerMonthlyProfit monthlyProfit = new PartnerMonthlyProfit();
        monthlyProfit.setId(99L);
        monthlyProfit.setPartner(partner);
        monthlyProfit.setCalculatedProfit(new BigDecimal("220.00"));
        monthlyProfit.setStatus(ProfitStatus.PAID);

        LedgerResponse ledger = LedgerResponse.builder()
                .id(401L)
                .idempotencyKey("PROFIT_DISTRIBUTION_99")
                .partnerId(7L)
                .amount(new BigDecimal("220.00"))
                .referenceType(LedgerReferenceType.PROFIT_DISTRIBUTION)
                .referenceId(99L)
                .description("Partner monthly profit payout for 2026-04")
                .build();

        when(partnerMonthlyProfitRepository.findById(99L)).thenReturn(Optional.of(monthlyProfit));
        when(ledgerService.getByIdempotencyKey("PROFIT_DISTRIBUTION_99")).thenReturn(Optional.of(ledger));
        when(capitalTransactionService.getCapitalTransactionsByReference(
                PartnerPayoutReferenceTypes.PARTNER_MONTHLY_PROFIT_PAYOUT, 99L))
                .thenReturn(Collections.emptyList());

        PayoutReconciliationResponse result = reconciliationService.reconcileMonthlyProfitPayout(99L);

        assertTrue(result.isLedgerMatched());
        assertFalse(result.isCapitalMatched());
        assertFalse(result.isFullyReconciled());
        assertTrue(result.getIssues().contains("Missing capital settlement transaction"));
    }
}

