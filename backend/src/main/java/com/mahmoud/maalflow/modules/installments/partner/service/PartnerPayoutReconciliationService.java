package com.mahmoud.maalflow.modules.installments.partner.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.modules.installments.capital.dto.CapitalTransactionResponse;
import com.mahmoud.maalflow.modules.installments.capital.service.CapitalTransactionService;
import com.mahmoud.maalflow.modules.installments.ledger.dto.LedgerResponse;
import com.mahmoud.maalflow.modules.installments.ledger.service.LedgerService;
import com.mahmoud.maalflow.modules.installments.partner.constants.PartnerPayoutReferenceTypes;
import com.mahmoud.maalflow.modules.installments.partner.dto.PayoutReconciliationResponse;
import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerCommission;
import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerMonthlyProfit;
import com.mahmoud.maalflow.modules.installments.partner.enums.CommissionStatus;
import com.mahmoud.maalflow.modules.installments.partner.enums.ProfitStatus;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerCommissionRepository;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerMonthlyProfitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PartnerPayoutReconciliationService {

    private final PartnerCommissionRepository partnerCommissionRepository;
    private final PartnerMonthlyProfitRepository partnerMonthlyProfitRepository;
    private final LedgerService ledgerService;
    private final CapitalTransactionService capitalTransactionService;

    @Transactional(readOnly = true)
    public PayoutReconciliationResponse reconcileCommissionPayout(Long commissionId) {
        PartnerCommission commission = partnerCommissionRepository.findById(commissionId)
                .orElseThrow(() -> new BusinessException("messages.commission.notFound"));

        Optional<LedgerResponse> ledger = ledgerService.getByIdempotencyKey("COMMISSION_" + commissionId);
        CapitalTransactionResponse capital = firstOrNull(capitalTransactionService.getCapitalTransactionsByReference(
                PartnerPayoutReferenceTypes.PARTNER_COMMISSION_PAYOUT,
                commissionId));

        return buildResponse(
                commission.getId(),
                "COMMISSION",
                commission.getStatus().name(),
                commission.getPartner().getId(),
                commission.getCommissionAmount(),
                ledger,
                capital,
                commission.getStatus() == CommissionStatus.PAID
        );
    }

    @Transactional(readOnly = true)
    public PayoutReconciliationResponse reconcileMonthlyProfitPayout(Long monthlyProfitId) {
        PartnerMonthlyProfit monthlyProfit = partnerMonthlyProfitRepository.findById(monthlyProfitId)
                .orElseThrow(() -> new BusinessException("messages.partner.monthlyProfit.notFound"));

        Optional<LedgerResponse> ledger = ledgerService.getByIdempotencyKey("PROFIT_DISTRIBUTION_" + monthlyProfitId);
//        CapitalTransactionResponse capital = firstOrNull(capitalTransactionService.getCapitalTransactionsByReference(
//                PartnerPayoutReferenceTypes.PARTNER_MONTHLY_PROFIT_PAYOUT,
//                monthlyProfitId));

        return buildResponse(
                monthlyProfit.getId(),
                "MONTHLY_PROFIT",
                monthlyProfit.getStatus().name(),
                monthlyProfit.getPartner().getId(),
                monthlyProfit.getCalculatedProfit(),
                ledger,
                null,
                monthlyProfit.getStatus() == ProfitStatus.PAID
        );
    }

    private PayoutReconciliationResponse buildResponse(Long payoutId,
                                                       String payoutType,
                                                       String payoutStatus,
                                                       Long partnerId,
                                                       BigDecimal payoutAmount,
                                                       Optional<LedgerResponse> ledger,
                                                       CapitalTransactionResponse capital,
                                                       boolean payoutMustBeSettled) {

        List<String> issues = new ArrayList<>();
        LedgerResponse ledgerResponse = ledger.orElse(null);

        boolean ledgerMatched = ledgerResponse != null
                && ledgerResponse.getPartnerId() != null
                && ledgerResponse.getPartnerId().equals(partnerId)
                && ledgerResponse.getAmount() != null
                && ledgerResponse.getAmount().compareTo(payoutAmount) == 0;

        boolean capitalMatched = capital != null
                && capital.getPartnerId() != null
                && capital.getPartnerId().equals(partnerId)
                && capital.getAmount() != null
                && capital.getAmount().compareTo(payoutAmount) == 0;

        if (payoutMustBeSettled && ledgerResponse == null) {
            issues.add("Missing ledger settlement entry");
        }
//        if (payoutMustBeSettled && capital == null) {
//            issues.add("Missing capital settlement transaction");
//        }
        if (ledgerResponse != null && !ledgerMatched) {
            issues.add("Ledger settlement does not match payout amount/partner");
        }
//        if (capital != null && !capitalMatched) {
//            issues.add("Capital settlement does not match payout amount/partner");
//        }

        boolean fullyReconciled = issues.isEmpty();

        return PayoutReconciliationResponse.builder()
                .payoutId(payoutId)
                .payoutType(payoutType)
                .payoutStatus(payoutStatus)
                .partnerId(partnerId)
                .payoutAmount(payoutAmount)
                .ledgerEntryId(ledgerResponse != null ? ledgerResponse.getId() : null)
                .ledgerIdempotencyKey(ledgerResponse != null ? ledgerResponse.getIdempotencyKey() : null)
                .ledgerAmount(ledgerResponse != null ? ledgerResponse.getAmount() : null)
                .ledgerReferenceType(ledgerResponse != null && ledgerResponse.getReferenceType() != null
                        ? ledgerResponse.getReferenceType().name() : null)
                .ledgerReferenceId(ledgerResponse != null ? ledgerResponse.getReferenceId() : null)
                .ledgerDescription(ledgerResponse != null ? ledgerResponse.getDescription() : null)
                .capitalTransactionId(capital != null ? capital.getId() : null)
                .capitalAmount(capital != null ? capital.getAmount() : null)
                .capitalReferenceType(capital != null ? capital.getReferenceType() : null)
                .capitalReferenceId(capital != null ? capital.getReferenceId() : null)
                .capitalDescription(capital != null ? capital.getDescription() : null)
                .ledgerMatched(ledgerMatched)
                .capitalMatched(capitalMatched)
                .fullyReconciled(fullyReconciled)
                .issues(issues)
                .build();
    }

    private CapitalTransactionResponse firstOrNull(List<CapitalTransactionResponse> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return null;
        }
        return transactions.getFirst();
    }
}

