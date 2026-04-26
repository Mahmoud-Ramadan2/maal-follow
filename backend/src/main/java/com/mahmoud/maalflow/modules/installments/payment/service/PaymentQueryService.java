package com.mahmoud.maalflow.modules.installments.payment.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.modules.installments.capital.entity.CapitalTransaction;
import com.mahmoud.maalflow.modules.installments.capital.repo.CapitalTransactionRepository;
import com.mahmoud.maalflow.modules.installments.ledger.entity.DailyLedger;
import com.mahmoud.maalflow.modules.installments.ledger.enums.LedgerReferenceType;
import com.mahmoud.maalflow.modules.installments.ledger.repo.DailyLedgerRepository;
import com.mahmoud.maalflow.modules.installments.payment.dto.PaymentResponse;
import com.mahmoud.maalflow.modules.installments.payment.dto.PaymentReconciliationResponse;
import com.mahmoud.maalflow.modules.installments.payment.dto.PaymentSummary;
import com.mahmoud.maalflow.modules.installments.payment.entity.Payment;
import com.mahmoud.maalflow.modules.installments.payment.enums.PaymentMethod;
import com.mahmoud.maalflow.modules.installments.payment.enums.PaymentProcessingStatus;
import com.mahmoud.maalflow.modules.installments.payment.mapper.PaymentMapper;
import com.mahmoud.maalflow.modules.installments.payment.repo.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.mahmoud.maalflow.modules.shared.constants.AppConstants.MONTH_FORMAT;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final CapitalTransactionRepository capitalTransactionRepository;
    private final DailyLedgerRepository dailyLedgerRepository;


    public PaymentResponse getById(Long id) {
        return paymentRepository.findById(id)
                .map(paymentMapper::toPaymentResponse)
                .orElseThrow(() -> new com.mahmoud.maalflow.exception.ObjectNotFoundException("messages.payment.notFound", id));
    }

    public PaymentReconciliationResponse getReconciliation(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new com.mahmoud.maalflow.exception.ObjectNotFoundException("messages.payment.notFound", id));

        var schedule = payment.getInstallmentSchedule();
        var contract = schedule != null ? schedule.getContract() : null;

        List<CapitalTransaction> capitalTransactions = capitalTransactionRepository
                .findByReferenceTypeAndReferenceIdOrderByTransactionDateDesc("PAYMENT", payment.getId());
        List<DailyLedger> ledgerEntries = dailyLedgerRepository
                .findByReferenceTypeAndReferenceIdOrderByCreatedAtDesc(LedgerReferenceType.PAYMENT, payment.getId());

        BigDecimal capitalReturned = capitalTransactions.stream()
                .map(CapitalTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal ledgerAmount = ledgerEntries.stream()
                .map(DailyLedger::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return PaymentReconciliationResponse.builder()
                .paymentId(payment.getId())
                .idempotencyKey(payment.getIdempotencyKey())
                .status(payment.getStatus())
                .paymentDate(payment.getPaymentDate())
                .actualPaymentDate(payment.getActualPaymentDate())
                .paymentMethod(payment.getPaymentMethod())
                .amount(payment.getAmount())
                .discountAmount(payment.getDiscountAmount())
                .netAmount(payment.getNetAmount())
                .isEarlyPayment(payment.getIsEarlyPayment())
                .scheduleId(schedule != null ? schedule.getId() : null)
                .scheduleSequenceNumber(schedule != null ? schedule.getSequenceNumber() : null)
                .scheduleStatus(schedule != null ? schedule.getStatus() : null)
                .contractId(contract != null ? contract.getId() : null)
                .contractNumber(contract != null ? contract.getContractNumber() : null)
                .receivedById(payment.getReceivedBy() != null ? payment.getReceivedBy().getId() : null)
                .receivedByName(payment.getReceivedBy() != null ? payment.getReceivedBy().getName() : null)
                .collectorId(payment.getCollector() != null ? payment.getCollector().getId() : null)
                .collectorName(payment.getCollector() != null ? payment.getCollector().getName() : null)
                .capitalReturned(capitalReturned)
                .capitalTransactionCount(capitalTransactions.size())
                .capitalTransactionIds(capitalTransactions.stream().map(CapitalTransaction::getId).toList())
                .ledgerAmount(ledgerAmount)
                .ledgerEntryCount(ledgerEntries.size())
                .ledgerEntryIds(ledgerEntries.stream().map(DailyLedger::getId).toList())
                .nonCapitalPortion(payment.getNetAmount() != null ? payment.getNetAmount().subtract(capitalReturned) : BigDecimal.ZERO)
                .build();
    }

    public Optional<PaymentResponse> getByIdempotencyKey(String idempotencyKey) {
        return paymentRepository.findByIdempotencyKey(idempotencyKey).map(paymentMapper::toPaymentResponse);
    }

    public Page<PaymentSummary> list(int page, int size) {
        return search(null, null, null, null, null, null, null, null, null, null, page, size);
    }

    public Page<PaymentSummary> listByMonth(String month, int page, int size) {
        validatePaymentMonth(month);
        return search(month, null, null, null, null, null, null, null, null, null, page, size);
    }

    public Page<PaymentSummary> listByDateRange(LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("messages.payment.invalidDateRange");
        }
        return search(null, startDate.toLocalDate(), endDate.toLocalDate(), null, null, null, null, null, null, null, page, size);
    }

    public Page<PaymentSummary> listEarlyPayments(int page, int size) {
        return search(null, null, null, true, null, null, null, null, null, null, page, size);
    }

    public Page<PaymentSummary> search(
            String month,
            LocalDate startDate,
            LocalDate endDate,
            Boolean isEarlyPayment,
            PaymentProcessingStatus status,
            PaymentMethod paymentMethod,
            Long collectorId,
            Long contractId,
            String customerName,
            BigDecimal minNetAmount,
            int page,
            int size
    ) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException("messages.payment.invalidDateRange");
        }
        if (month != null && !month.isBlank()) {
            validatePaymentMonth(month);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentDate"));
        Specification<Payment> spec = buildSearchSpecification(
                month,
                startDate,
                endDate,
                isEarlyPayment,
                status,
                paymentMethod,
                collectorId,
                contractId,
                customerName,
                minNetAmount
        );

        return paymentRepository.findAll(spec, pageable).map(paymentMapper::toPaymentSummary);
    }

    public List<Payment> findAllByFilters(
            String month,
            LocalDate startDate,
            LocalDate endDate,
            Boolean isEarlyPayment,
            PaymentProcessingStatus status,
            PaymentMethod paymentMethod,
            Long collectorId,
            Long contractId,
            String customerName,
            BigDecimal minNetAmount
    ) {
        Specification<Payment> spec = buildSearchSpecification(
                month,
                startDate,
                endDate,
                isEarlyPayment,
                status,
                paymentMethod,
                collectorId,
                contractId,
                customerName,
                minNetAmount
        );
        return paymentRepository.findAll(spec);
    }

    private Specification<Payment> buildSearchSpecification(
            String month,
            LocalDate startDate,
            LocalDate endDate,
            Boolean isEarlyPayment,
            PaymentProcessingStatus status,
            PaymentMethod paymentMethod,
            Long collectorId,
            Long contractId,
            String customerName,
            BigDecimal minNetAmount
    ) {
        List<Specification<Payment>> specs = new ArrayList<>();

        if (month != null && !month.isBlank()) {
            specs.add((root, query, cb) -> cb.equal(root.get("agreedPaymentMonth"), month));
        }
        if (startDate != null) {
            specs.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("actualPaymentDate"), startDate));
        }
        if (endDate != null) {
            specs.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("actualPaymentDate"), endDate));
        }
        if (isEarlyPayment != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("isEarlyPayment"), isEarlyPayment));
        }
        if (status != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (paymentMethod != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("paymentMethod"), paymentMethod));
        }
        if (collectorId != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("collector").get("id"), collectorId));
        }
        if (contractId != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("installmentSchedule").get("contract").get("id"), contractId));
        }
        if (customerName != null && !customerName.isBlank()) {
            specs.add((root, query, cb) -> cb.like(
                    cb.lower(root.get("installmentSchedule").get("contract").get("customer").get("name")),
                    "%" + customerName.trim().toLowerCase() + "%"
            ));
        }
        if (minNetAmount != null) {
            specs.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("netAmount"), minNetAmount));
        }

        // Keep repository calls safe when no filters are provided.
        Specification<Payment> result = (root, query, cb) -> cb.conjunction();
        for (Specification<Payment> spec : specs) {
            result = result.and(spec);
        }
        return result;
    }

    private void validatePaymentMonth(String month) {
        if (month == null || !month.matches("^\\d{4}-\\d{2}$")) {
            throw new BusinessException("messages.payment.invalidMonthFormat");
        }
        try {
            YearMonth.parse(month, MONTH_FORMAT);
        } catch (Exception e) {
            throw new BusinessException("messages.payment.invalidMonthFormat");
        }
    }
}

