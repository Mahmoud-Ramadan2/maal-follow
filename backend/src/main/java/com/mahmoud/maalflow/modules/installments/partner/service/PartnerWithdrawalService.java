package com.mahmoud.maalflow.modules.installments.partner.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.exception.ObjectNotFoundException;
import com.mahmoud.maalflow.exception.UserNotFoundException;
import com.mahmoud.maalflow.modules.installments.capital.dto.CapitalTransactionRequest;
import com.mahmoud.maalflow.modules.installments.capital.enums.CapitalTransactionType;
import com.mahmoud.maalflow.modules.installments.capital.service.CapitalTransactionService;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerWithdrawalRequest;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerWithdrawalResponse;
import com.mahmoud.maalflow.modules.installments.partner.entity.Partner;
import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerWithdrawal;
import com.mahmoud.maalflow.modules.installments.partner.enums.WithdrawalStatus;
import com.mahmoud.maalflow.modules.installments.partner.mapper.PartnerWithdrawalMapper;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerRepository;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerWithdrawalRepository;
import com.mahmoud.maalflow.modules.installments.ledger.service.LedgerService;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import com.mahmoud.maalflow.modules.shared.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing partner withdrawal requests.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PartnerWithdrawalService {

    private final PartnerWithdrawalRepository withdrawalRepository;
    private final PartnerRepository partnerRepository;
    private final PartnerWithdrawalMapper withdrawalMapper;
    private final LedgerService ledgerService;
    private final CapitalTransactionService capitalTransactionService;
    private final PartnerShareService partnerShareService;
    private final UserRepository userRepository;
    private final PartnerEffectiveInvestmentService partnerEffectiveInvestmentService;
    @Transactional
    public PartnerWithdrawalResponse createWithdrawalRequest(PartnerWithdrawalRequest request) {
        // Validate partner has sufficient balance
        Partner partner = partnerRepository.findById(request.getPartnerId())
                .orElseThrow(() -> new UserNotFoundException("messages.partner.notFound", request.getPartnerId()));

        // Check if partner has sufficient balance
        if (partner.getCurrentBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException("messages.partner.withdrawal.insufficientBalance");
        }

        PartnerWithdrawal withdrawal = withdrawalMapper.toPartnerWithdrawal(request);
        withdrawal.setPartner(partner);
        withdrawal.setStatus(WithdrawalStatus.PENDING);
        withdrawal.setRequestedAt(LocalDateTime.now());

        log.info("Creating withdrawal request for partner ID {}: amount {}, type {}",
                request.getPartnerId(), request.getAmount(), request.getWithdrawalType());

        return withdrawalMapper.toPartnerWithdrawalResponse(withdrawalRepository.save(withdrawal));
    }

    @Transactional(readOnly = true)
    public List<PartnerWithdrawalResponse> getWithdrawalsByPartnerId(Long partnerId) {
        return withdrawalRepository.findByPartnerId(partnerId)
                .stream().map(withdrawalMapper::toPartnerWithdrawalResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PartnerWithdrawalResponse> getPendingWithdrawals() {
        return withdrawalRepository.findByStatus(WithdrawalStatus.PENDING)
                .stream().map(withdrawalMapper::toPartnerWithdrawalResponse).toList();
    }

    @Transactional
    public PartnerWithdrawalResponse approveWithdrawal(Long id) {

        PartnerWithdrawal withdrawal = withdrawalRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("messages.partner.withdrawal.notFound", id));

        Partner partner = partnerRepository.findByIdForUpdate(withdrawal.getPartner().getId())
                .orElseThrow(() -> new UserNotFoundException("messages.partner.notFound", withdrawal.getPartner().getId()));

        WithdrawalStatus currentStatus = withdrawal.getStatus();
        if (currentStatus != WithdrawalStatus.PENDING) {
            throw new BusinessException("messages.partner.withdrawal.invalidStatusForApproval");
        }

        // Validate balance at approval time as well
        validatePartnerBalance(withdrawal.getPartner().getId(), withdrawal.getAmount());

        checkPendingWithdrawalLimit(withdrawal.getPartner().getId());

        withdrawal.setStatus(WithdrawalStatus.APPROVED);
        withdrawal.setApprovedAt(LocalDateTime.now());
        // TODO: Set approvedBy user
        withdrawal.setApprovedBy(null);
        PartnerWithdrawal saved = withdrawalRepository.save(withdrawal);

        log.info("Approved withdrawal ID {} for partner ID {}", id, withdrawal.getPartner().getId());

        // Update partner balance
        updatePartnerBalanceForWithdrawal(partner, withdrawal.getAmount());

        partnerEffectiveInvestmentService.updatePartnerEffectiveInvestment(partner.getId());

        partnerShareService.recalculateSharePercentages(capitalTransactionService.getPoolForUpdate().getTotalAmount());

        return   withdrawalMapper.toPartnerWithdrawalResponse(saved);
    }


    @Transactional
    public PartnerWithdrawalResponse processWithdrawal(Long id) {

        PartnerWithdrawal withdrawal = getWithdrawalByIdForUpdate(id);

        Partner partner = partnerRepository.findByIdForUpdate(withdrawal.getPartner().getId())
                .orElseThrow(() -> new UserNotFoundException("messages.partner.notFound", withdrawal.getPartner().getId()));

        if (withdrawal.getStatus() != WithdrawalStatus.APPROVED) {
            throw new BusinessException("messages.partner.withdrawal.invalidStatusForProcessing");
        }

        // Validate balance again at processing time
        validatePartnerBalance(partner.getId(), withdrawal.getAmount());

        withdrawal.setStatus(WithdrawalStatus.COMPLETED);
        withdrawal.setProcessedAt(LocalDateTime.now());
        // TODO: Set processedBy user
        withdrawal.setProcessedBy(null);


        // Record capital movement for withdrawal
        recordCapitalWithdrawal(withdrawal);

        // Record ledger expense entry
        recordWithdrawalExpense(withdrawal);

        log.info("Processed withdrawal ID {} for partner ID {} amount {}",
                 id, withdrawal.getPartner().getId(), withdrawal.getAmount());

        PartnerWithdrawal saved = withdrawalRepository.save(withdrawal);

        return withdrawalMapper.toPartnerWithdrawalResponse(saved);
    }

    /**
     * Get withdrawal by ID.
     */
    @Transactional(readOnly = true)
    public PartnerWithdrawalResponse getWithdrawalById(Long id) {
        PartnerWithdrawal withdrawal =  withdrawalRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("messages.partner.withdrawal.notFound", id));

        return withdrawalMapper.toPartnerWithdrawalResponse(withdrawal);
    }

    /**
     * Reject a withdrawal request.
     */
    @Transactional
    public PartnerWithdrawalResponse rejectWithdrawal(Long id, String reason) {
        PartnerWithdrawal withdrawal = withdrawalRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("messages.partner.withdrawal.notFound", id));

        if (withdrawal.getStatus() != WithdrawalStatus.PENDING) {
            throw new BusinessException("messages.partner.withdrawal.invalidStatusForRejection");
        }

        User rejectedBy = userRepository.findById(1L).orElse(null);
        withdrawal.setStatus(WithdrawalStatus.CANCELLED);
        withdrawal.setRejectedAt(LocalDateTime.now());
        withdrawal.setRejectionReason(reason != null ? reason : "No reason provided");
        withdrawal.setRejectedBy(rejectedBy);

        PartnerWithdrawal saved = withdrawalRepository.save(withdrawal);

        log.info("Rejected withdrawal ID {} with reason: {}", id, reason);
        return withdrawalMapper.toPartnerWithdrawalResponse(saved);
    }


    // ============== Helper Methods ==============



    private void updatePartnerBalanceForWithdrawal(Partner partner, BigDecimal withdrawalAmount) {
        // Update totals and current balance
        BigDecimal currentWithdrawals = partner.getTotalWithdrawals() != null
                ? partner.getTotalWithdrawals()
                : BigDecimal.ZERO;
        BigDecimal currentBalance = partner.getCurrentBalance() != null
                ? partner.getCurrentBalance()
                : BigDecimal.ZERO;

        partner.setTotalWithdrawals(currentWithdrawals.add(withdrawalAmount));
        partner.setCurrentBalance(currentBalance.subtract(withdrawalAmount));

        partnerRepository.save(partner);
    }

    private void validatePartnerBalance(Long partnerId, BigDecimal amount) {
        Partner partner = partnerRepository.findByIdForUpdate(partnerId)
                .orElseThrow(() -> new UserNotFoundException("messages.partner.notFound", partnerId));

        if (partner.getCurrentBalance().compareTo(amount) < 0) {
            throw new BusinessException("messages.partner.withdrawal.insufficientBalance");
        }
    }



    private void recordWithdrawalExpense(PartnerWithdrawal withdrawal) {
        try {
            ledgerService.recordPartnerWithdrawalExpense(
                    withdrawal.getPartner().getId(),
                    withdrawal.getAmount(),
                    withdrawal.getId(),
                    "Partner withdrawal - " + withdrawal.getWithdrawalType()
            );
            log.info("Recorded ledger expense for withdrawal ID {}", withdrawal.getId());
        } catch (Exception e) {
            log.error("Failed to record ledger expense for withdrawal ID {}: {}",
                    withdrawal.getId(), e.getMessage());
            throw new BusinessException("messages.partner.withdrawal.ledgerError");
        }
    }

    private void recordCapitalWithdrawal(PartnerWithdrawal withdrawal) {
        CapitalTransactionRequest txRequest = CapitalTransactionRequest.builder()
                .transactionType(CapitalTransactionType.WITHDRAWAL)
                .amount(withdrawal.getAmount())
                .partnerId(withdrawal.getPartner().getId())
                .description("Partner withdrawal processing ID " + withdrawal.getId())
                .build();

        capitalTransactionService.createCapitalTransaction(txRequest);
    }

    private void checkPendingWithdrawalLimit(Long partnerId) {

        Partner partner = partnerRepository.findByIdForUpdate(partnerId)
                .orElseThrow(() -> new UserNotFoundException("messages.partner.notFound", partnerId));

        BigDecimal pendingTotal = withdrawalRepository.sumByPartnerIdAndStatus(partnerId, WithdrawalStatus.PENDING);
        if (pendingTotal == null) {
            pendingTotal = BigDecimal.ZERO;
        }
        BigDecimal newPendingTotal = pendingTotal;
        if (newPendingTotal.compareTo(partner.getCurrentBalance()) > 0) {
            throw new BusinessException("messages.partner.withdrawal.exceedsPendingLimit");
        }
    }

        /**
         *  Get Withdrawalwith
         *  pessimistic lock for update operations.
         */
        @Transactional
        public PartnerWithdrawal getWithdrawalByIdForUpdate(Long id) {

            return withdrawalRepository.findWithdrawalByIdForUpdate(id)
                    .orElseThrow(() -> new ObjectNotFoundException("messages.partner.withdrawal.notFound", id));
        }

}
