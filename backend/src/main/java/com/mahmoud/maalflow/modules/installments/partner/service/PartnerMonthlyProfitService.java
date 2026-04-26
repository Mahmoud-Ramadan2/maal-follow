package com.mahmoud.maalflow.modules.installments.partner.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.exception.ObjectNotFoundException;
import com.mahmoud.maalflow.modules.installments.capital.service.CapitalTransactionService;
import com.mahmoud.maalflow.modules.installments.ledger.dto.LedgerResponse;
import com.mahmoud.maalflow.modules.installments.ledger.service.LedgerService;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerInvestmentRequest;
import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerMonthlyProfit;
import com.mahmoud.maalflow.modules.installments.partner.enums.ProfitStatus;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerMonthlyProfitRepository;
import com.mahmoud.maalflow.modules.installments.payment.enums.PaymentMethod;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import com.mahmoud.maalflow.modules.shared.user.repo.UserRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartnerMonthlyProfitService {

    private final PartnerMonthlyProfitRepository partnerMonthlyProfitRepository;
    private final UserRepository userRepository;
    private final LedgerService ledgerService;
    private final PartnerInvestmentService partnerInvestmentService;

    @Transactional(readOnly = true)
    public List<PartnerMonthlyProfit> getByPartnerId(Long partnerId) {
        return partnerMonthlyProfitRepository.findByPartnerId(partnerId);
    }

    @Transactional(readOnly = true)
    public PartnerMonthlyProfit getById(Long id) {
        return partnerMonthlyProfitRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("messages.partner.monthlyProfit.notFound", id));
    }

    @Transactional(readOnly = true)
    public List<PartnerMonthlyProfit> getByDistributionId(Long distributionId) {
        return partnerMonthlyProfitRepository.findByProfitDistributionId(distributionId);
    }

    @Transactional
    public PartnerMonthlyProfit payMonthlyProfit(Long monthlyProfitId,
                                                 Long paidByUserId,
                                                 PaymentMethod paymentMethod,
                                                 LocalDate paymentDate,
                                                 String notes) {

        PartnerMonthlyProfit monthlyProfit = partnerMonthlyProfitRepository.findById(monthlyProfitId)
                .orElseThrow(() -> new ObjectNotFoundException("messages.partner.monthlyProfit.notFound", monthlyProfitId));

        if (monthlyProfit.getStatus() != ProfitStatus.CALCULATED) {
            throw new BusinessException("messages.partner.monthlyProfit.invalidStatusForPayment");
        }

        User paidBy = userRepository.findById(paidByUserId)
                .orElseThrow(() -> new BusinessException("messages.user.notFound"));

        monthlyProfit.setStatus(ProfitStatus.PAID);
        monthlyProfit.setPaidBy(paidBy);
        monthlyProfit.setPaymentMethod(paymentMethod != null ? paymentMethod : PaymentMethod.CASH);
        monthlyProfit.setPaymentDate(paymentDate != null ? paymentDate : LocalDate.now());
        if (notes != null && !notes.isBlank()) {
            monthlyProfit.setNotes(notes);
        }

        PartnerMonthlyProfit saved = partnerMonthlyProfitRepository.save(monthlyProfit);

        LedgerResponse ledgerResponse = ledgerService.recordPartnerProfitDistributionExpense(
                saved.getPartner().getId(),
                saved.getCalculatedProfit(),
                saved.getId(),
                "Partner monthly profit payout for " + saved.getProfitDistribution().getMonthYear()
        );

        // TODO
//        CapitalTransactionRequest txRequest = CapitalTransactionRequest.builder()
//                .transactionType(CapitalTransactionType.WITHDRAWAL)
//                .amount(saved.getCalculatedProfit())
//                .partnerId(saved.getPartner().getId())
//                .referenceType(PartnerPayoutReferenceTypes.PARTNER_MONTHLY_PROFIT_PAYOUT)
//                .referenceId(saved.getId())
//                .description("Partner monthly profit payout ID " + saved.getId())
//                .build();
//        CapitalTransactionResponse capitalResponse = capitalTransactionService.createCapitalTransaction(txRequest);

        log.info("Partner monthly profit payout settlement trace: monthlyProfitId={}, partnerId={}, amount={}, ledgerId={}, ledgerKey={}",
                saved.getId(),
                saved.getPartner().getId(),
                saved.getCalculatedProfit(),
                ledgerResponse.getId(),
                ledgerResponse.getIdempotencyKey()
               );

        return saved;
    }

    @Transactional
    public PartnerMonthlyProfit reinvestMonthlyProfit(Long monthlyProfitId,
                                                 Long userId,
                                                 PaymentMethod paymentMethod,
                                                 LocalDate paymentDate,
                                                 String notes) {

        PartnerMonthlyProfit monthlyProfit = partnerMonthlyProfitRepository.findById(monthlyProfitId)
                .orElseThrow(() -> new ObjectNotFoundException("messages.partner.monthlyProfit.notFound", monthlyProfitId));

        if (monthlyProfit.getStatus() == ProfitStatus.REINVESTED) {

            throw new BusinessException("messages.partner.monthlyProfit.invalidStatusForReinvestment");

        }else if (monthlyProfit.getStatus() != ProfitStatus.CALCULATED) {
            throw new BusinessException("messages.partner.monthlyProfit.invalidStatusForPayment");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("messages.user.notFound"));

        monthlyProfit.setStatus(ProfitStatus.REINVESTED);
        monthlyProfit.setPaidBy(user);
        if (notes != null && !notes.isBlank()) {
            monthlyProfit.setNotes(notes);
        }

        // record as invetstment in the partner's capital pool instead of payment
        // record investment in partner investment service with pending status
        String investmentNote =String.format( "إضافة ربح شهر %s الخاص بشريك رقم%d "
                , monthlyProfit.getProfitDistribution().getMonthYear(), monthlyProfit.getPartner().getId());

        recordProfitInvestment(monthlyProfit.getCalculatedProfit(), monthlyProfit.getPartner().getId(), investmentNote);

        PartnerMonthlyProfit saved = partnerMonthlyProfitRepository.save(monthlyProfit);

//        LedgerResponse ledgerResponse = ledgerService.recordPartnerProfitDistributionExpense(
//                saved.getPartner().getId(),
//                saved.getCalculatedProfit(),
//                saved.getId(),
//                "Partner monthly profit payout for " + saved.getProfitDistribution().getMonthYear()
//        );

        log.info("Partner monthly profit reinvestment trace: monthlyProfitId={}, partnerId={}, amount={}, userId={}",
                saved.getId(),
                saved.getPartner().getId(),
                saved.getCalculatedProfit(),
                user.getId()
        );

        return saved;
    }

    private void recordProfitInvestment(BigDecimal calculatedProfit, Long id, String investmentNote)
    {
    PartnerInvestmentRequest investmentRequest = PartnerInvestmentRequest.builder()
            .partnerId(id)
            .amount(calculatedProfit)
            .notes(investmentNote)
            .build();
        partnerInvestmentService.createInvestment(investmentRequest);
}
}


