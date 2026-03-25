package com.mahmoud.maalflow.modules.installments.contract.repo;

import com.mahmoud.maalflow.modules.installments.contract.dto.ContractResponse;
import com.mahmoud.maalflow.modules.installments.contract.entity.Contract;
import com.mahmoud.maalflow.modules.installments.contract.entity.InstallmentSchedule;
import com.mahmoud.maalflow.modules.installments.contract.enums.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {


    boolean existsByPurchaseIdAndStatusAndCustomerId(Long purchaseId, ContractStatus contractStatus, Long customerId);


    @Query("""
SELECT new com.mahmoud.maalflow.modules.installments.contract.dto.ContractResponse(
        c.id,
        c.contractNumber,
        c.status,
        c.customer.name,
        c.responsibleUser.name,
        c.purchase.productName,
        c.purchase.vendor.name,
        c.partner.name,
        c.originalPrice,
        c.additionalCosts,
        c.finalPrice,
        c.downPayment,
        c.remainingAmount,
        c.cashDiscountRate,
        c.earlyPaymentDiscountRate,
        c.months,
        c.monthlyAmount,
        c.agreedPaymentDay,
        c.profitAmount,
        c.totalExpenses,
        c.netProfit,
        c.startDate,
        c.completionDate,
           c.createdAt,
           c.updatedAt,
        c.notes
            ) 
 FROM Contract c WHERE c.customer.id = :customerId
    """)
    Page<ContractResponse> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    // Find contracts by status
    Page<Contract> findByStatus(ContractStatus status, Pageable pageable);

    // Find active contracts by partner
    @Query("""
        SELECT c FROM Contract c 
        WHERE c.partner.id = :partnerId 
        AND c.status = 'ACTIVE'
        ORDER BY c.startDate DESC
    """)
    List<Contract> findActiveContractsByPartnerId(@Param("partnerId") Long partnerId);

    // Find contracts by customer with multiple accounts (for requirement #18)
    @Query("""
        SELECT c FROM Contract c
        WHERE c.customer.id IN (
            SELECT DISTINCT cal.linkedCustomer.id
            FROM CustomerAccountLink cal 
                WHERE cal.customer.id = :customerId AND cal.isActive = true
            UNION
            SELECT :customerId
        )
        ORDER BY c.startDate DESC
    """)
    List<Contract> findAllContractsByLinkedCustomers(@Param("customerId") Long customerId);

    // Find contracts by contract number
    Optional<Contract> findByContractNumber(String contractNumber);

    // Find contracts completing in date range
    @Query("""
        SELECT c FROM Contract c 
        WHERE c.completionDate BETWEEN :startDate AND :endDate
        ORDER BY c.completionDate
    """)
    List<Contract> findContractsCompletingBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Get total monthly expected installments
    @Query("""
        SELECT COALESCE(SUM(c.monthlyAmount), 0)
        FROM Contract c
        WHERE c.status = 'ACTIVE'
   """)
    BigDecimal getTotalMonthlyExpectedAmount();

    // Count active contracts
    Long countByStatus(ContractStatus status);

    // Get total profit for active contracts
    @Query("""
        SELECT COALESCE(SUM(c.netProfit), 0) 
        FROM Contract c 
        WHERE c.status IN ('ACTIVE', 'COMPLETED')
    """)
    BigDecimal getTotalNetProfit();

    // Find contracts by responsible user
    List<Contract> findByResponsibleUserId(Long userId);

    // Find contracts by customer address (for collection routes - requirement #6)
    @Query("""
        SELECT c FROM Contract c 
        WHERE c.customer.address LIKE %:address% 
        AND c.status = 'ACTIVE'
        ORDER BY c.customer.address
    """)
    List<Contract> findActiveContractsByCustomerAddress(@Param("address") String address);

    // Find contracts by agreed payment day (requirement #4)
    @Query("""
        SELECT c FROM Contract c 
        WHERE c.agreedPaymentDay = :paymentDay 
        AND c.status = 'ACTIVE'
        ORDER BY c.customer.name
    """)
    List<Contract> findActiveContractsByAgreedPaymentDay(@Param("paymentDay") Integer paymentDay);

    @Query("""
        SELECT is
        FROM InstallmentSchedule is
        WHERE is.id = :scheduleId
        AND is.contract.id = :contractId
    """)
    Optional<InstallmentSchedule> findInstallmentScheduleByIdAndContractId(Long scheduleId, Long contractId);
}