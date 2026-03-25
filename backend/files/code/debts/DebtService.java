package com.mahmoud.maalflow.modules.debts.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.exception.ObjectNotFoundException;
import com.mahmoud.maalflow.modules.debts.dto.*;
import com.mahmoud.maalflow.modules.debts.entity.*;
import com.mahmoud.maalflow.modules.debts.enums.*;
import com.mahmoud.maalflow.modules.debts.repo.*;
import com.mahmoud.maalflow.modules.shared.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing Debts (الديون).
 * Copy to: src/main/java/com/mahmoud/maalflow/modules/debts/service/
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DebtService {

    private final DebtRepository debtRepo;
    private final DebtPaymentRepository paymentRepo;
    private final UserRepository userRepository;
    // TODO: Replace with SecurityUtils after security impl
    // private final SecurityUtils securityUtils;

    @Transactional
    public DebtResponse createDebt(DebtRequest request) {
        log.info("Creating {} debt for: {}", request.getDebtType(), request.getPersonName());

        Debt debt = Debt.builder()
                .personName(request.getPersonName())
                .phone(request.getPhone())
                .debtType(request.getDebtType())
                .originalAmount(request.getOriginalAmount())
                .remainingAmount(request.getOriginalAmount())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .status(DebtStatus.ACTIVE)
                .createdBy(userRepository.findById(1L).orElse(null)) // TODO: securityUtils.getCurrentUser()
                .build();

        Debt saved = debtRepo.save(debt);
        log.info("Created debt ID: {} type: {} amount: {}", saved.getId(), saved.getDebtType(), saved.getOriginalAmount());
        return toResponse(saved);
    }

    @Transactional
    public DebtResponse recordPayment(DebtPaymentRequest request) {
        Debt debt = debtRepo.findById(request.getDebtId())
                .orElseThrow(() -> new ObjectNotFoundException("Debt not found", request.getDebtId()));

        if (debt.getStatus() == DebtStatus.SETTLED) {
            throw new BusinessException("Debt is already settled");
        }
        if (request.getAmount().compareTo(debt.getRemainingAmount()) > 0) {
            throw new BusinessException("Payment amount exceeds remaining debt: " + debt.getRemainingAmount());
        }

        DebtPayment payment = DebtPayment.builder()
                .debt(debt)
                .amount(request.getAmount())
                .paymentDate(request.getPaymentDate())
                .paymentMethod(request.getPaymentMethod())
                .notes(request.getNotes())
                .build();
        paymentRepo.save(payment);

        // Update remaining
        debt.setRemainingAmount(debt.getRemainingAmount().subtract(request.getAmount()));
        if (debt.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            debt.setRemainingAmount(BigDecimal.ZERO);
            debt.setStatus(DebtStatus.SETTLED);
            log.info("Debt {} fully settled", debt.getId());
        }
        debtRepo.save(debt);

        log.info("Recorded payment of {} for debt {}. Remaining: {}", request.getAmount(), debt.getId(), debt.getRemainingAmount());
        return toResponse(debt);
    }

    @Transactional(readOnly = true)
    public DebtResponse getById(Long id) {
        Debt debt = debtRepo.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Debt not found", id));
        return toResponse(debt);
    }

    @Transactional(readOnly = true)
    public Page<DebtResponse> list(DebtType type, DebtStatus status, String search, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Debt> debts;
        if (search != null && !search.isBlank()) {
            debts = debtRepo.search(search.trim(), pageable);
        } else if (type != null && status != null) {
            debts = debtRepo.findByDebtTypeAndStatus(type, status, pageable);
        } else if (type != null) {
            debts = debtRepo.findByDebtType(type, pageable);
        } else if (status != null) {
            debts = debtRepo.findByStatus(status, pageable);
        } else {
            debts = debtRepo.findAll(pageable);
        }
        return debts.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<DebtPayment> getPayments(Long debtId) {
        return paymentRepo.findByDebtIdOrderByPaymentDateDesc(debtId);
    }

    /** Get total active receivables (people owe me) */
    @Transactional(readOnly = true)
    public BigDecimal getTotalReceivables() {
        return debtRepo.getTotalActiveByType(DebtType.RECEIVABLE);
    }

    /** Get total active payables (I owe people) */
    @Transactional(readOnly = true)
    public BigDecimal getTotalPayables() {
        return debtRepo.getTotalActiveByType(DebtType.PAYABLE);
    }

    /** Check for overdue debts and update their status */
    @Transactional
    public void updateOverdueDebts() {
        List<Debt> overdue = debtRepo.findByDueDateBeforeAndStatus(LocalDate.now(), DebtStatus.ACTIVE);
        for (Debt debt : overdue) {
            debt.setStatus(DebtStatus.OVERDUE);
        }
        debtRepo.saveAll(overdue);
        log.info("Updated {} debts to OVERDUE status", overdue.size());
    }

    @Transactional
    public void cancelDebt(Long id) {
        Debt debt = debtRepo.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Debt not found", id));
        debt.setStatus(DebtStatus.CANCELLED);
        debtRepo.save(debt);
        log.info("Cancelled debt ID: {}", id);
    }

    private DebtResponse toResponse(Debt d) {
        BigDecimal totalPaid = paymentRepo.getTotalPaidByDebtId(d.getId());
        int paymentCount = paymentRepo.findByDebtIdOrderByPaymentDateDesc(d.getId()).size();
        return DebtResponse.builder()
                .id(d.getId())
                .personName(d.getPersonName())
                .phone(d.getPhone())
                .debtType(d.getDebtType())
                .originalAmount(d.getOriginalAmount())
                .remainingAmount(d.getRemainingAmount())
                .totalPaid(totalPaid)
                .description(d.getDescription())
                .dueDate(d.getDueDate())
                .status(d.getStatus())
                .createdAt(d.getCreatedAt())
                .paymentCount(paymentCount)
                .build();
    }
}

