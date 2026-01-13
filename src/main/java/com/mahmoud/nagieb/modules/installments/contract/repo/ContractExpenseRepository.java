package com.mahmoud.nagieb.modules.installments.contract.repo;

import com.mahmoud.nagieb.modules.installments.contract.entity.ContractExpense;
import com.mahmoud.nagieb.modules.shared.enums.ExpenseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContractExpenseRepository extends JpaRepository<ContractExpense, Long> {

    // Find all expenses for a specific contract
    List<ContractExpense> findByContractIdOrderByExpenseDateDesc(Long contractId);

    // Find expenses by type
    List<ContractExpense> findByExpenseType(ExpenseType expenseType);

    // Find expenses in date range
    @Query("""
        SELECT e FROM ContractExpense e 
        WHERE e.expenseDate BETWEEN :startDate AND :endDate
        ORDER BY e.expenseDate DESC
    """)
    List<ContractExpense> findExpensesBetweenDates(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Get total expenses for a contract
    @Query("""
        SELECT COALESCE(SUM(e.amount), 0) FROM ContractExpense e
        WHERE e.contract.id = :contractId
   """)
    BigDecimal getTotalExpensesByContractId(@Param("contractId") Long contractId);

    // Get total expenses by partner
    @Query("""
        SELECT COALESCE(SUM(e.amount), 0) FROM ContractExpense e
        WHERE e.partner.id = :partnerId
   """)
    BigDecimal getTotalExpensesByPartnerId(@Param("partnerId") Long partnerId);

    // Find expenses by contract and type
    List<ContractExpense> findByContractIdAndExpenseType(Long contractId, ExpenseType expenseType);
}