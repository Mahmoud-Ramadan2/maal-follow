package com.mahmoud.maalflow.modules.installments.partner.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.exception.UserNotFoundException;
import com.mahmoud.maalflow.modules.installments.capital.dto.CapitalTransactionRequest;
import com.mahmoud.maalflow.modules.installments.capital.enums.CapitalTransactionType;
import com.mahmoud.maalflow.modules.installments.capital.service.CapitalPoolService;
import com.mahmoud.maalflow.modules.installments.capital.service.CapitalTransactionService;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerInvestmentRequest;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerInvestmentResponse;
import com.mahmoud.maalflow.modules.installments.partner.entity.Partner;
import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerInvestment;
import com.mahmoud.maalflow.modules.installments.partner.enums.InvestmentStatus;
import com.mahmoud.maalflow.modules.installments.partner.enums.InvestmentType;
import com.mahmoud.maalflow.modules.installments.partner.enums.PartnerStatus;
import com.mahmoud.maalflow.modules.installments.partner.mapper.PartnerInvestmentMapper;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerInvestmentRepository;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerRepository;
import com.mahmoud.maalflow.modules.installments.ledger.service.LedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.mahmoud.maalflow.modules.shared.constants.AppConstants.MONTH_FORMAT;
import static com.mahmoud.maalflow.modules.shared.constants.AppConstants.NEW_INVESTMENT_DELAY_MONTHS;

/**
 * Service for managing partner investments.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PartnerInvestmentService {

    private final PartnerInvestmentRepository investmentRepository;
    private final PartnerRepository partnerRepository;
    private final CapitalPoolService capitalPoolService;
    private final PartnerInvestmentMapper investmentMapper;
    private final LedgerService ledgerService;
    private final CapitalTransactionService capitalTransactionService;
    private final PartnerShareService partnerShareService;
    private final PartnerEffectiveInvestmentService partnerEffectiveInvestmentService;


    @Transactional
    public PartnerInvestmentResponse createInvestment(PartnerInvestmentRequest request) {

        Partner partner = partnerRepository.findById(request.getPartnerId())
                .orElseThrow(() -> new UserNotFoundException("messages.partner.notFound", request.getPartnerId()));

        PartnerInvestment investment = investmentMapper.toPartnerInvestment(request);

        investment.setStatus(InvestmentStatus.PENDING);

        if (investment.getAmount() == null || investment.getAmount().compareTo(BigDecimal.valueOf(100)) < 0) {
            throw new BusinessException("messages.partner.investmentAmount.invalid");
        }

        // INITIAL for first investment, ADDITIONAL otherwise
        boolean exist = investmentRepository.existsByPartnerId(partner.getId());
        if (!exist) {
            investment.setInvestmentType(InvestmentType.INITIAL);
        } else {
            investment.setInvestmentType(InvestmentType.ADDITIONAL);
        }

        investment.setPartner(partner);
        investment.setInvestedAt(java.time.LocalDateTime.now());

        PartnerInvestment saved = investmentRepository.save(investment);

        log.info("Created new investment with id: {} for partner id: {} with status: {}", saved.getId(), partner.getId(), saved.getStatus());
        return investmentMapper.toPartnerInvestmentResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PartnerInvestmentResponse> getInvestmentsByPartnerId(Long partnerId) {
        List<PartnerInvestment> investments = investmentRepository.findByPartnerId(partnerId);
        return investments.stream()
                .map(investmentMapper::toPartnerInvestmentResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PartnerInvestment getInvestmentById(Long id) {
        return investmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Investment not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public PartnerInvestment getInvestmentByIdForUpdate(Long id) {
        return investmentRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RuntimeException("Investment not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public PartnerInvestmentResponse getInvestmentResponseById(Long id) {
        PartnerInvestment inv = getInvestmentById(id);
        return investmentMapper.toPartnerInvestmentResponse(inv);
    }

    @Transactional
    public PartnerInvestmentResponse confirmInvestment(Long id) {

        PartnerInvestment investment = getInvestmentByIdForUpdate(id);
        if (investment.getStatus() == InvestmentStatus.CONFIRMED) {
            return investmentMapper.toPartnerInvestmentResponse(investment);
        }

        Long partnerId = investment.getPartner().getId();

//        boolean exists = partnerRepository.existsById(partnerId);
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new UserNotFoundException("messages.partner.notFound", partnerId));


        investment.setStatus(InvestmentStatus.CONFIRMED);
        investment.setInvestedAt(LocalDateTime.now());
        PartnerInvestment updated = investmentRepository.save(investment);

        // Update partner's total investment and balance,
        // and set investment start date and profit calculation start month if this is the initial investment.
        boolean isInitialInvestment = updated.getInvestmentType().equals(InvestmentType.INITIAL);
        updatePartnerInvestmentBalanceAndDates(partnerId, isInitialInvestment);
        recordCapitalInvestment(updated);

        partnerEffectiveInvestmentService.updatePartnerEffectiveInvestment(partnerId);
        partnerShareService.recalculateSharePercentages(capitalPoolService.getPoolOrThrowForUpdate().getTotalAmount());

        // Record ledger income entry for confirmed investment

        recordInvestmentIncome(updated);

        log.info("Confirmed investment with id: {} for partner id: {}", updated.getId(), updated.getPartner().getId());

        return investmentMapper.toPartnerInvestmentResponse(updated);
    }


    // ============== Helper Methods ==============

    /**
     * Update the total investment balance of a partner.
     */
    @Transactional
    public void updatePartnerInvestmentBalanceAndDates(Long partnerId, boolean isInitialInvestment) {

        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new UserNotFoundException("messages.partner.notFound", partnerId));

        if (isInitialInvestment) {
            partner.setStatus(PartnerStatus.ACTIVE);
            LocalDate currentDate = LocalDate.now();
            partner.setInvestmentStartDate(currentDate);
            partner.setProfitCalculationStartMonth(currentDate.plusMonths(NEW_INVESTMENT_DELAY_MONTHS).format(MONTH_FORMAT));
        }

        BigDecimal totalInvestment = investmentRepository.sumByPartnerIdAndStatus(
                partnerId, InvestmentStatus.CONFIRMED);

        BigDecimal safeTotalInvestment = totalInvestment != null ? totalInvestment : BigDecimal.ZERO;
        BigDecimal safeTotalWithdrawals = partner.getTotalWithdrawals() != null
                ? partner.getTotalWithdrawals()
                : BigDecimal.ZERO;

        partner.setTotalInvestment(safeTotalInvestment);
        partner.setCurrentBalance(safeTotalInvestment.subtract(safeTotalWithdrawals));

        log.info("Updated total investment for partner id: {} to {}", partnerId, partner.getTotalInvestment());
        partnerRepository.save(partner);
    }

    /**
     * Record capital transaction for confirmed investment.
     *  recordCapitalInvestment is called after confirming the investment to ensure that only confirmed investments are recorded in the capital transactions. This method creates a new capital transaction of type INVESTMENT with the amount of the investment and links it to the partner. This allows us to track all capital movements related to partner investments in a consistent way.
     */
    private void recordCapitalInvestment(PartnerInvestment investment) {
        CapitalTransactionRequest txRequest = CapitalTransactionRequest.builder()
                .transactionType(CapitalTransactionType.INVESTMENT)
                .amount(investment.getAmount())
                .partnerId(investment.getPartner().getId())
                .description("استثمار جديد برقم  " + investment.getId())
                .build();

        capitalTransactionService.createCapitalTransaction(txRequest);
    }

    /**
     * Record investment income in the ledger when investment is confirmed.
     */
    private void recordInvestmentIncome(PartnerInvestment investment) {
        try {
            ledgerService.recordPartnerInvestmentIncome(
                    investment.getPartner().getId(),
                    investment.getAmount(),
                    investment.getId(),
                    "استثمار - " + investment.getInvestmentType()
            );
            log.info("Recorded ledger income for investment ID {}", investment.getId());
        } catch (Exception e) {
            log.error("Failed to record ledger income for investment ID {}: {}",
                    investment.getId(), e.getMessage());
            throw new BusinessException("messages.partner.investment.ledgerError");
        }
    }


}
