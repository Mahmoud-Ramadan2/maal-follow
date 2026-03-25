package com.mahmoud.maalflow.modules.debts.repo;

import com.mahmoud.maalflow.modules.debts.entity.DebtPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DebtPaymentRepository extends JpaRepository<DebtPayment, Long> {
    List<DebtPayment> findByDebtIdOrderByPaymentDateDesc(Long debtId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM DebtPayment p WHERE p.debt.id = :debtId")
    BigDecimal getTotalPaidByDebtId(Long debtId);
}

