package com.mahmoud.maalflow.modules.debts.repo;

import com.mahmoud.maalflow.modules.debts.entity.Debt;
import com.mahmoud.maalflow.modules.debts.enums.DebtStatus;
import com.mahmoud.maalflow.modules.debts.enums.DebtType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Copy to: src/main/java/com/mahmoud/maalflow/modules/debts/repo/
 */
@Repository
public interface DebtRepository extends JpaRepository<Debt, Long> {
    Page<Debt> findByDebtType(DebtType type, Pageable pageable);
    Page<Debt> findByStatus(DebtStatus status, Pageable pageable);
    Page<Debt> findByDebtTypeAndStatus(DebtType type, DebtStatus status, Pageable pageable);
    List<Debt> findByDueDateBeforeAndStatus(LocalDate date, DebtStatus status);

    @Query("SELECT COALESCE(SUM(d.remainingAmount), 0) FROM Debt d WHERE d.debtType = :type AND d.status = 'ACTIVE'")
    BigDecimal getTotalActiveByType(DebtType type);

    @Query("SELECT d FROM Debt d WHERE d.personName LIKE %:search% OR d.phone LIKE %:search%")
    Page<Debt> search(String search, Pageable pageable);
}

