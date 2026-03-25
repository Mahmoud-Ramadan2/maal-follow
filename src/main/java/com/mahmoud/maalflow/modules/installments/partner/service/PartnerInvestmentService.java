package com.mahmoud.maalflow.modules.installments.partner.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.exception.UserNotFoundException;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerInvestmentRequest;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerInvestmentResponse;
import com.mahmoud.maalflow.modules.installments.partner.entity.Partner;
import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerInvestment;
import com.mahmoud.maalflow.modules.installments.partner.enums.InvestmentStatus;
import com.mahmoud.maalflow.modules.installments.partner.enums.InvestmentType;
import com.mahmoud.maalflow.modules.installments.partner.mapper.PartnerInvestmentMapper;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerInvestmentRepository;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerRepository;
import com.mahmoud.maalflow.modules.installments.ledger.service.LedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing partner investments.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PartnerInvestmentService {

    private final PartnerInvestmentRepository investmentRepository;
    private final PartnerRepository partnerRepository;
    private final PartnerInvestmentMapper investmentMapper;
    private final LedgerService ledgerService;

    @Transactional
    public PartnerInvestmentResponse createInvestment(PartnerInvestmentRequest request) {

        Partner partner = partnerRepository.findById(request.getPartnerId())
                .orElseThrow(() -> new UserNotFoundException("messages.partner.notFound", request.getPartnerId()));

        PartnerInvestment investment = investmentMapper.toPartnerInvestment(request);

        // Default to PENDING when status is not provided, otherwise use provided status
        if (request.getStatus() == null) {
            investment.setStatus(InvestmentStatus.PENDING);
        } else {
            investment.setStatus(request.getStatus());
        }

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
    public PartnerInvestmentResponse getInvestmentResponseById(Long id) {
        PartnerInvestment inv = getInvestmentById(id);
        return investmentMapper.toPartnerInvestmentResponse(inv);
    }

    @Transactional
    public PartnerInvestmentResponse confirmInvestment(Long id) {

        PartnerInvestment investment = getInvestmentById(id);
        if (investment.getStatus() == InvestmentStatus.CONFIRMED) {
            return investmentMapper.toPartnerInvestmentResponse(investment);
        }

        Long partnerId = investment.getPartner().getId();

        boolean exists = partnerRepository.existsById(partnerId);

        if (!exists) {
            throw new UserNotFoundException("messages.partner.notFound", partnerId);
        }

        investment.setStatus(InvestmentStatus.CONFIRMED);

        PartnerInvestment updated = investmentRepository.save(investment);
        updatePartnerInvestmentBalance(partnerId);

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
    public void updatePartnerInvestmentBalance(Long partnerId) {

        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new UserNotFoundException("messages.partner.notFound", partnerId));

        BigDecimal totalInvestment = investmentRepository.sumByPartnerIdAndStatus(
                partnerId, InvestmentStatus.CONFIRMED);

        partner.setTotalInvestment(totalInvestment != null ? totalInvestment : BigDecimal.ZERO);

        log.info("Updated total investment for partner id: {} to {}", partnerId, partner.getTotalInvestment());
        partnerRepository.save(partner);
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
                    "Partner investment - " + investment.getInvestmentType()
            );
            log.info("Recorded ledger income for investment ID {}", investment.getId());
        } catch (Exception e) {
            log.error("Failed to record ledger income for investment ID {}: {}",
                    investment.getId(), e.getMessage());
            throw new BusinessException("messages.partner.investment.ledgerError");
        }
    }

}
