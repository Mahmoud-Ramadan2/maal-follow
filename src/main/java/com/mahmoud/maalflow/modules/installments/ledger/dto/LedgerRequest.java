package com.mahmoud.maalflow.modules.installments.ledger.dto;

import com.mahmoud.maalflow.modules.installments.ledger.enums.LedgerReferenceType;
import com.mahmoud.maalflow.modules.installments.ledger.enums.LedgerSource;
import com.mahmoud.maalflow.modules.installments.ledger.enums.LedgerType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating/updating ledger entries.
 * Includes idempotency key for duplicate prevention.
 *
 * @author Mahmoud
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerRequest {

    /**
     * Idempotency key to prevent duplicate ledger entries.
     */
    @NotBlank(message = "{validation.ledger.idempotencyKey.required}")
    @Size(max = 100, message = "{validation.ledger.idempotencyKey.size}")
    private String idempotencyKey;

    @NotNull(message = "{validation.ledger.type.required}")
    private LedgerType type;

    @NotNull(message = "{validation.ledger.amount.required}")
    @DecimalMin(value = "0.01", message = "{validation.ledger.amount.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.ledger.amount.format}")
    private BigDecimal amount;

    @NotNull(message = "{validation.ledger.source.required}")
    private LedgerSource source;

    private LedgerReferenceType referenceType;

    private Long referenceId;

    @Size(max = 2000, message = "{validation.ledger.description.size}")
    private String description;

    @NotNull(message = "{validation.ledger.date.required}")
    private LocalDate date;

    /**
     * Partner ID if this entry is related to a partner.
     */
    private Long partnerId;
}

