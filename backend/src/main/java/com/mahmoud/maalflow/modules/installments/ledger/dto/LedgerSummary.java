package com.mahmoud.maalflow.modules.installments.ledger.dto;

import com.mahmoud.maalflow.modules.installments.ledger.enums.LedgerSource;
import com.mahmoud.maalflow.modules.installments.ledger.enums.LedgerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Summary DTO for ledger entry listing.
 *
 * @author Mahmoud
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerSummary {

    private Long id;
    private LedgerType type;
    private BigDecimal amount;
    private LedgerSource source;
    private String description;
    private LocalDate date;
}

