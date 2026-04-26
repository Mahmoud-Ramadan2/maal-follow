package com.mahmoud.maalflow.modules.installments.profit.service;

import com.mahmoud.maalflow.modules.installments.contract.entity.Contract;
import com.mahmoud.maalflow.modules.installments.contract.entity.Deduction;
import com.mahmoud.maalflow.modules.installments.schedule.entity.InstallmentSchedule;
import com.mahmoud.maalflow.modules.installments.contract.enums.DeductionType;
import com.mahmoud.maalflow.modules.installments.contract.repo.DeductionRepository;
import com.mahmoud.maalflow.modules.installments.contract.repo.ContractExpenseRepository;
import com.mahmoud.maalflow.modules.installments.ledger.entity.DailyLedger;
import com.mahmoud.maalflow.modules.installments.ledger.enums.LedgerReferenceType;
import com.mahmoud.maalflow.modules.installments.ledger.enums.LedgerSource;
import com.mahmoud.maalflow.modules.installments.ledger.enums.LedgerType;
import com.mahmoud.maalflow.modules.installments.ledger.repo.DailyLedgerRepository;
import com.mahmoud.maalflow.modules.installments.profit.entity.MonthlyProfitDistribution;
import com.mahmoud.maalflow.modules.installments.profit.enums.ProfitDistributionStatus;
import com.mahmoud.maalflow.modules.installments.profit.repo.MonthlyProfitDistributionRepository;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.mahmoud.maalflow.modules.shared.constants.AppConstants.DEFAULT_MANAGEMENT_FEE_PERCENTAGE;
import static com.mahmoud.maalflow.modules.shared.constants.AppConstants.DEFAULT_ZAKAT_FEE_PERCENTAGE;

/**
 * Service for processing profit from installment payments.
 * Handles:
 * - Calculating management fees and zakat deductions
 * - Recording deductions in the ledger
 * - Accumulating monthly profit for distribution
 *
 * @author Mahmoud
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProfitProcessingService {

    private final DeductionRepository deductionRepository;
    private final DailyLedgerRepository ledgerRepository;
    private final MonthlyProfitDistributionRepository profitDistributionRepository;
    private final ContractExpenseRepository contractExpenseRepository;

    // TODO: Zakat calculation needs more requirements - commented out for now
    // private static final BigDecimal DEFAULT_ZAKAT_PERCENTAGE = BigDecimal.valueOf(0.025); // 2.5%

    /**
     * Process proportional profit for partial payments.
     * Calculates ONLY the profit portion of the payment and processes accordingly.
     *
     * @param schedule The installment schedule
     * @param paidAmount The amount paid (partial or full)
     * @param paymentDate The date of payment
     * @param user The user who processed the payment
     */
    @Transactional
    public void processProportionalInstallmentProfit(
            InstallmentSchedule schedule,
            BigDecimal paidAmount,
            LocalDate paymentDate,
            User user) {

        BigDecimal  expectedTotalAmount = schedule.getAmount();
        BigDecimal expectedTotalPrincipal = schedule.getPrincipalAmount();
        BigDecimal expectedTotalProfit = schedule.getProfitAmount();

        if (expectedTotalProfit == null || expectedTotalProfit.compareTo(BigDecimal.ZERO) <= 0) {
            log.debug("No profit to process for schedule {}", schedule.getId());
            return;
        }

        if (expectedTotalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid total schedule amount for schedule {}", schedule.getId());
            return;
        }

        // Calculate payment ratio based on payment amount
        BigDecimal paymentRatio = paidAmount.divide(expectedTotalAmount, 10, RoundingMode.HALF_UP);

        // Calculate how much of this payment is PROFIT (not principal)
        BigDecimal profitPortionOfPayment = expectedTotalProfit.multiply(paymentRatio).setScale(2, RoundingMode.HALF_UP);
        BigDecimal principalPortionOfPayment = expectedTotalPrincipal.multiply(paymentRatio).setScale(2, RoundingMode.HALF_UP);
        System.out.println(principalPortionOfPayment.compareTo(paidAmount.subtract(principalPortionOfPayment)) == 0);


        BigDecimal ratio = paymentRatio.multiply(BigDecimal.valueOf(100)).setScale(1);
        log.info("Processing proportional profit for schedule {}: payment={}, ratio={}%, principal={}, profit={}",
                schedule.getId(), paidAmount, ratio,
                principalPortionOfPayment, profitPortionOfPayment);

        String description = "النسبة المئوية مما يحب دفعه (" +
                ratio + "%)";
        // Process ONLY the profit portion (not the entire payment amount)
        if (profitPortionOfPayment.compareTo(BigDecimal.ZERO) > 0) {
            processProfit(schedule, profitPortionOfPayment, paidAmount, paymentDate, user, description);
        }
     }

    /**
     * Core profit processing method
     */
    private void processProfit(
            InstallmentSchedule schedule,
            BigDecimal grossProfit,
            BigDecimal paidAmount,
            LocalDate paymentDate,
            User user,
            String description) {

        Contract contract = schedule.getContract();

        // 1. First Subtract contract expenses related to this schedule/contract

        // calculate contract expenses
        // (1. expenses recorded for this schedule in today,
        // 2. any remaining expenses for this contract that should be allocated to this schedule)
        BigDecimal scheduleExpensesToday = contractExpenseRepository.getTotalExpensesByScheduleIdAndDateToday(schedule.getId(), LocalDate.now());
//        BigDecimal contractExpenses = getContractExpensesForPeriod(schedule) ;

        BigDecimal netProfitAfterExpenses = grossProfit.subtract(scheduleExpensesToday);

        // 2. Calculate management fee  (30% of gross profit)
        BigDecimal managementFee = calculateManagementFee(netProfitAfterExpenses);

        // 3. Calculate net profit AFTER management fee deduction
        BigDecimal netProfitAfterManagement = netProfitAfterExpenses.subtract(managementFee);


        // TODO: Zakat calculation needs more requirements - commented out
        // BigDecimal zakat = calculateZakat(netProfitAfterExpenses);
        // BigDecimal netProfit = netProfitAfterExpenses.subtract(zakat);
        BigDecimal netProfit = netProfitAfterManagement;

        // 3. Record management fee deduction
//        if (managementFee.compareTo(BigDecimal.ZERO) > 0) {
//
//            recordDeduction(contract, schedule, DeductionType.MANAGEMENT_FEE,
//                    managementFee, paymentDate, user, description);
//        }

        // TODO: Record zakat
        // if (zakat.compareTo(BigDecimal.ZERO) > 0) {
        //     recordDeduction(contract, schedule, DeductionType.ZAKAT, zakat, paymentDate, user, description);
        // }

        // 4. Accumulate net profit for partner distribution (AFTER management fee and expenses)
        if (netProfit.compareTo(BigDecimal.ZERO) > 0) {
            accumulateMonthlyProfit(schedule, netProfit, grossProfit, managementFee, BigDecimal.ZERO, scheduleExpensesToday);
        }

        log.info("Processed {} for schedule {}: gross={}, mgmtFee={}, expenses={}, net={}, payment={}",
                description, schedule.getId(), grossProfit, managementFee, scheduleExpensesToday, netProfit, paidAmount);
    }

    /**
     * Get management fee percentage for contract.
     * Could be from contract settings, partner settings, or system default.
     */
    private BigDecimal getManagementFeePercentage(Contract contract) {
        // TODO: Get from contract or partner settings if available
        // For now, use system default
        return DEFAULT_MANAGEMENT_FEE_PERCENTAGE;
    }

    /**
     * Calculate management fee from profit (30% default)
     */
    private BigDecimal calculateManagementFee(BigDecimal profit) {
        return profit.multiply(DEFAULT_MANAGEMENT_FEE_PERCENTAGE)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    // TODO: Zakat calculation commented out until requirements are finalized
    // /**
    //  * Get zakat percentage for contract.
    //  */
    // private BigDecimal getZakatPercentage(Contract contract) {
    //     return DEFAULT_ZAKAT_PERCENTAGE;
    // }
    //
    // /**
    //  * Calculate zakat from profit (2.5% default)
    //  */
    // private BigDecimal calculateZakat(BigDecimal profit) {
    //     return profit.multiply(DEFAULT_ZAKAT_PERCENTAGE).setScale(2, RoundingMode.HALF_UP);
    // }

    /**
     * Record deduction (management fee or zakat) in database and ledger
     */
    private void recordDeduction(
            Contract contract,
            InstallmentSchedule schedule,
            DeductionType type,
            BigDecimal amount,
            LocalDate date,
            User user,
            String description) {

        // Create deduction record
        Deduction deduction = Deduction.builder()
                .contract(contract)
                .installmentSchedule(schedule)
                .deductionType(type)
                .amount(amount)
                .deductionDate(date)
                .month(schedule.getProfitMonth())
                .notes(description)
                .build();

        Deduction saved = deductionRepository.save(deduction);

        // Record in ledger
        recordDeductionInLedger(saved, contract, schedule, type, amount, date, user);

        log.debug("Recorded {} deduction {} for schedule {} ({})", type, amount, schedule.getId(), description);
    }

    /**
     * Record deduction in daily ledger
     */
    private void recordDeductionInLedger(
            Deduction deduction,
            Contract contract,
            InstallmentSchedule schedule,
            DeductionType type,
            BigDecimal amount,
            LocalDate date,
            User user) {

        LedgerSource source = (type == DeductionType.MANAGEMENT_FEE)
                ? LedgerSource.MANAGEMENT_FEE : LedgerSource.ZAKAT;

        // Management fee is income for business, zakat is expense
        LedgerType ledgerType = (type == DeductionType.MANAGEMENT_FEE)
                ? LedgerType.INCOME : LedgerType.EXPENSE;

        String idempotencyKey = "LEDGER-DED-" + deduction.getId();

        DailyLedger ledger = DailyLedger.builder()
                .idempotencyKey(idempotencyKey)
                .type(ledgerType)
                .amount(amount)
                .source(source)
                .referenceType(LedgerReferenceType.DEDUCTION)
                .referenceId(deduction.getId())
                .description(type.name() + " من عقد رقم " + contract.getId() +
                        " قسط رقم #" + schedule.getSequenceNumber())
                .date(date)
                .user(user)
                .build();

        ledgerRepository.save(ledger);
    }

    /**
     * Accumulate profit for monthly distribution to partners
     * Note: netProfit is AFTER management fee and expenses deduction
     */
    private void accumulateMonthlyProfit(
            InstallmentSchedule schedule,
            BigDecimal netProfit,
            BigDecimal grossProfit,
            BigDecimal managementFee,
            BigDecimal zakat,
            BigDecimal expenses) {

        // Find or create monthly profit distribution record
        String monthYear = schedule.getProfitMonth();
        String finalMonthYear = monthYear;
        MonthlyProfitDistribution distribution = profitDistributionRepository
                .findByMonthYear(monthYear)
                .orElseGet(() -> createNewMonthlyDistribution(finalMonthYear));

        // check if currunt distribtion is distributed or locked
        // so it will be added to next month with notfifctinon
        boolean rolledOver = false;
        MonthlyProfitDistribution originalDistribution = null;
        while (distribution.getStatus().equals(ProfitDistributionStatus.DISTRIBUTED) || distribution.getStatus().equals(ProfitDistributionStatus.LOCKED)) {

            rolledOver = true;
            originalDistribution = distribution;

            log.info("Current distribution for month {} is already {}, creating new distribution for next month",
                    monthYear, distribution.getStatus());

            // Create new distribution for next month
            LocalDate currentMonth = LocalDate.parse(monthYear + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String nextMonthYear = currentMonth.plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
            distribution = profitDistributionRepository
                    .findByMonthYear(nextMonthYear)
                    .orElseGet(() -> createNewMonthlyDistribution(nextMonthYear));

            monthYear = nextMonthYear;

        }

        if (rolledOver) {
            String note = String.format("تم إضافة ربح القسط رقم %d للعقد %d لشهر %s إلى توزيعات شهر %s بسبب حالة توزيع الشهر الحالي: %s",
                    schedule.getSequenceNumber(),
                    schedule.getContract().getId(),
                    schedule.getProfitMonth(),
                    monthYear,
                    originalDistribution.getStatus());

            if (distribution.getCalculationNotes() == null || distribution.getCalculationNotes().isBlank()) {
                distribution.setCalculationNotes(note);
            } else {
                distribution.setCalculationNotes(distribution.getCalculationNotes() + " | " + note);
            }
        }

            // Accumulate values - note that netProfit is what goes to partners
        distribution.setTotalProfit(
                safeAdd(distribution.getTotalProfit(), grossProfit));
        distribution.setManagementFeeAmount(
                safeAdd(distribution.getManagementFeeAmount(), managementFee));
        distribution.setZakatAmount(
                safeAdd(distribution.getZakatAmount(), zakat));
        distribution.setContractExpensesAmount(
                safeAdd(distribution.getContractExpensesAmount(), expenses));
        // This is the amount that will be distributed to partners (after management fee and expenses)
        distribution.setDistributableProfit(
                safeAdd(distribution.getDistributableProfit(), netProfit));

        distribution.setPartnersTotalProfit(
                safeAdd(distribution.getPartnersTotalProfit(), netProfit));

        distribution.setOwnerProfit(
                safeAdd(distribution.getOwnerProfit(), netProfit));

        profitDistributionRepository.save(distribution);
        log.debug("Accumulated profit for month {}: gross={}, mgmtFee={}, expenses={}, distributable={}",
                monthYear, grossProfit, managementFee, expenses, netProfit);
    }

    /**
     * Create new monthly profit distribution record
     */
    private MonthlyProfitDistribution createNewMonthlyDistribution(String monthYear) {
        MonthlyProfitDistribution newDist = new MonthlyProfitDistribution();
        newDist.setMonthYear(monthYear);
        newDist.setTotalProfit(BigDecimal.ZERO);
        newDist.setManagementFeeAmount(BigDecimal.ZERO);
        newDist.setZakatAmount(BigDecimal.ZERO);
        newDist.setContractExpensesAmount(BigDecimal.ZERO);
        newDist.setDistributableProfit(BigDecimal.ZERO);
        newDist.setManagementFeePercentage(DEFAULT_MANAGEMENT_FEE_PERCENTAGE);
        newDist.setZakatPercentage(DEFAULT_ZAKAT_FEE_PERCENTAGE);
        newDist.setStatus(ProfitDistributionStatus.PENDING);
        return profitDistributionRepository.save(newDist);
    }

    /**
     * Safely add two BigDecimal values (handles null)
     */
    private BigDecimal safeAdd(BigDecimal a, BigDecimal b) {
        BigDecimal safeA = (a != null) ? a : BigDecimal.ZERO;
        BigDecimal safeB = (b != null) ? b : BigDecimal.ZERO;
        return safeA.add(safeB);
    }

    /**
     * Reverse profit processing (for payment cancellation/reversal)
     */
    @Transactional
    public void reverseInstallmentProfit(InstallmentSchedule schedule, User user) {
        // TODO: Implement profit reversal logic
        // - Find and reverse deductions
        // - Update monthly profit distribution
        // - Create reversal ledger entries
        log.warn("Profit reversal not yet implemented for schedule {}", schedule.getId());
    }

    /**
     * Calculate contract expenses related to this installment schedule or contract
     * for the same month to properly account for expenses in profit calculation
     */
    private BigDecimal getContractExpensesForPeriod(InstallmentSchedule schedule) {
        // Get expenses for the contract in the same month as the installment
        String profitMonth = schedule.getProfitMonth();
        LocalDate startDate = LocalDate.parse(profitMonth + "-01");
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        Long contractId = schedule.getContract().getId();

        // Calculate expenses for this contract in the same month
        BigDecimal expenses = contractExpenseRepository.getTotalExpensesByContractIdAndMonth(contractId, startDate, endDate);

            if (expenses == null || expenses.compareTo(BigDecimal.ZERO) < 0) {
            expenses = BigDecimal.ZERO;
        }

        log.debug("Contract {} expenses for month {}: {}", contractId, profitMonth, expenses);
        return expenses;
    }
}

