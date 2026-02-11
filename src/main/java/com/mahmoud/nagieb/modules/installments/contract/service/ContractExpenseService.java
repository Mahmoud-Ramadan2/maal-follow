package com.mahmoud.nagieb.modules.installments.contract.service;

import com.mahmoud.nagieb.exception.BusinessException;
import com.mahmoud.nagieb.exception.ObjectNotFoundException;
import com.mahmoud.nagieb.exception.UserNotFoundException;
import com.mahmoud.nagieb.modules.installments.contract.dto.ContractExpenseRequest;
import com.mahmoud.nagieb.modules.installments.contract.dto.ContractExpenseResponse;
import com.mahmoud.nagieb.modules.installments.contract.entity.Contract;
import com.mahmoud.nagieb.modules.installments.contract.entity.ContractExpense;
import com.mahmoud.nagieb.modules.installments.contract.entity.InstallmentSchedule;
import com.mahmoud.nagieb.modules.installments.contract.mapper.ContractExpenseMapper;
import com.mahmoud.nagieb.modules.installments.contract.repo.ContractExpenseRepository;
import com.mahmoud.nagieb.modules.installments.contract.repo.ContractRepository;
import com.mahmoud.nagieb.modules.installments.partner.entity.Partner;
import com.mahmoud.nagieb.modules.installments.partner.repo.PartnerRepository;
import com.mahmoud.nagieb.modules.shared.enums.ExpenseType;
import com.mahmoud.nagieb.modules.installments.contract.enums.PaidBy;
import com.mahmoud.nagieb.modules.shared.user.entity.User;
import com.mahmoud.nagieb.modules.shared.user.repo.UserRepository;
import com.mahmoud.nagieb.modules.installments.ledger.entity.DailyLedger;
import com.mahmoud.nagieb.modules.installments.ledger.enums.LedgerReferenceType;
import com.mahmoud.nagieb.modules.installments.ledger.enums.LedgerSource;
import com.mahmoud.nagieb.modules.installments.ledger.enums.LedgerType;
import com.mahmoud.nagieb.modules.installments.ledger.repo.DailyLedgerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractExpenseService {

    private final ContractExpenseRepository expenseRepository;
    private final ContractRepository contractRepository;
    private final PartnerRepository partnerRepository;
    private final UserRepository userRepository;
    private final ContractExpenseMapper expenseMapper;
    private final DailyLedgerRepository dailyLedgerRepository;

    /**
     * Create a new contract expense
     * The database triggers will automatically update the contract's total_expenses and net_profit
     * Requirement #15: Track additional costs and expenses
     */
    @Transactional
    public ContractExpenseResponse createExpense(ContractExpenseRequest request) {

        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new ObjectNotFoundException("messages.contract.notFound", request.getContractId()));


        // TODO: Replace with actual authenticated user ID
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new UserNotFoundException("messages.user.notFound", 1L));

        ContractExpense expense = expenseMapper.toEntity(request);
        expense.setContract(contract);
        expense.setCreatedBy(user);
        if (expense.getPartner() == null && PaidBy.PARTNER.equals(expense.getPaidBy())) {
            throw new BusinessException("messages.expense.partner.required");
        }
        if (request.getPartnerId() != null) {
            Partner partner = partnerRepository.findById(request.getPartnerId())
                    .orElseThrow(() -> new ObjectNotFoundException("messages.partner.notFound", request.getPartnerId()));
            expense.setPartner(partner);
        }

        if(request.getScheduleId() != null){
            // Validate installment schedule belongs to contract
            InstallmentSchedule schedule = contractRepository.findInstallmentScheduleByIdAndContractId(request.getScheduleId(), contract.getId()).orElse(null);
                expense.setInstallmentSchedule(schedule);
        }

        ContractExpense saved = expenseRepository.save(expense);
        log.info("Created expense {} for contract {}", saved.getId(), contract.getContractNumber());

        // Record expense in daily ledger
        recordExpenseInDailyLedger(saved, user);

        // Note: Database triggers automatically update contract totals

        return expenseMapper.toResponse(saved);
    }

    @Transactional
    public ContractExpenseResponse updateExpense(Long id, ContractExpenseRequest request) {
        ContractExpense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("messages.expense.notFound", id));

        if ( PaidBy.PARTNER.equals(expense.getPaidBy()) && expense.getPartner() == null) {
            throw new BusinessException("messages.expense.partner.required");
        }

        if(request.getPaidBy() != null) {
            expense.setPaidBy(request.getPaidBy());
        }

        if (request.getAmount() != null) {
            expense.setAmount(request.getAmount());
        }
        if (request.getExpenseType() != null) {
            expense.setExpenseType(request.getExpenseType());
        }
        if (request.getDescription() != null) {
            expense.setDescription(request.getDescription());
        }
        if (request.getExpenseDate() != null) {
            expense.setExpenseDate(request.getExpenseDate());
        }
        if (request.getPaidBy() != null) {
            expense.setPaidBy(request.getPaidBy());
        }
        if (request.getReceiptNumber() != null) {
            expense.setReceiptNumber(request.getReceiptNumber());
        }
        if (request.getNotes() != null) {
            expense.setNotes(request.getNotes());
        }
        // TODO: Handle Updated By from Security Context
//        expense.setUpdatedBy();

        return expenseMapper.toResponse(expenseRepository.save(expense));
    }

    public List<ContractExpenseResponse> getExpensesByContractId(Long contractId) {
        List<ContractExpense> expenses = expenseRepository.findByContractIdOrderByExpenseDateDesc(contractId);
        return expenses.stream()
                .map(expenseMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<ContractExpenseResponse> getExpensesByType(ExpenseType type) {
        List<ContractExpense> expenses = expenseRepository.findByExpenseType(type);
        return expenses.stream()
                .map(expenseMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<ContractExpenseResponse> getExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        List<ContractExpense> expenses = expenseRepository.findExpensesBetweenDates(startDate, endDate);
        return expenses.stream()
                .map(expenseMapper::toResponse)
                .collect(Collectors.toList());
    }

    public BigDecimal getTotalExpensesForContract(Long contractId) {
        return expenseRepository.getTotalExpensesByContractId(contractId);
    }

    public BigDecimal getTotalExpensesForPartner(Long partnerId) {
        return expenseRepository.getTotalExpensesByPartnerId(partnerId);
    }

    @Transactional
    public void deleteExpense(Long id) {
        expenseRepository.deleteById(id);
        // Note: Database triggers will automatically recalculate contract totals
    }

    public ContractExpenseResponse getExpenseById(Long id) {
        ContractExpense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("messages.expense.notFound", id));
        return expenseMapper.toResponse(expense);
    }

    private void recomputeContractTotals(Contract contract) {

        // DB triggers handle (totalExpenses,netProfit)
        BigDecimal totalExpenses = expenseRepository.getTotalExpensesByContractId(contract.getId());
        if (totalExpenses == null) {
            totalExpenses = BigDecimal.ZERO;
        }
        contract.setTotalExpenses(totalExpenses);

        BigDecimal netProfit = contract.getProfitAmount().subtract(totalExpenses).setScale(2, RoundingMode.HALF_UP);
        contract.setNetProfit(netProfit);



        contractRepository.save(contract);
    }

    /**
     * Record contract expense in daily ledger for proper financial tracking
     */
    private void recordExpenseInDailyLedger(ContractExpense expense, User user) {
        String idempotencyKey = "LEDGER-EXP-" + expense.getId();
        String description = expense.getInstallmentSchedule() != null ?
                "مصاريف اضافية خاصة بالقسط " + expense.getInstallmentSchedule().getId() :
                "مصاريف اضافية عامة للعقد " + expense.getContract().getId();
        DailyLedger ledgerEntry = DailyLedger.builder()
                .idempotencyKey(idempotencyKey)
                .type(LedgerType.EXPENSE)
                .amount(expense.getAmount())
                .source(LedgerSource.OPERATING_EXPENSE)
                .referenceType(expense.getInstallmentSchedule() != null ? LedgerReferenceType.INSTALLMENT_SCHEDULE : LedgerReferenceType.CONTRACT_EXPENSE)
                .referenceId(expense.getId())
                .description(description)
                .date(expense.getExpenseDate())
                .user(user)
                .partner(expense.getPartner())
                .build();

        dailyLedgerRepository.save(ledgerEntry);
        log.debug("Recorded expense {} in daily ledger", expense.getId());
    }
}