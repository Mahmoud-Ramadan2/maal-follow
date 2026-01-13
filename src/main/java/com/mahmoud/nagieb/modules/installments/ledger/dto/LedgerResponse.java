package com.mahmoud.nagieb.modules.installments.ledger.dto;

import com.mahmoud.nagieb.modules.installments.ledger.enums.LedgerReferenceType;
import com.mahmoud.nagieb.modules.installments.ledger.enums.LedgerSource;
import com.mahmoud.nagieb.modules.installments.ledger.enums.LedgerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for ledger entry details.
 *
 * @author Mahmoud
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerResponse {

    private Long id;
    private String idempotencyKey;
    private LedgerType type;
    private BigDecimal amount;
    private LedgerSource source;
    private LedgerReferenceType referenceType;
    private Long referenceId;
    private String description;
    private LocalDate date;
    private LocalDateTime createdAt;
    private String userName;
    private Long userId;
    private String partnerName;
    private Long partnerId;
}

