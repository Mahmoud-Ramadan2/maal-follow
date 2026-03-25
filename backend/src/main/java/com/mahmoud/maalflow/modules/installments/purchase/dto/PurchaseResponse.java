package com.mahmoud.maalflow.modules.installments.purchase.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseResponse {

    private Long id;
    private String productName;
    private BigDecimal buyPrice;
    private LocalDate purchaseDate;
    private LocalDateTime createdAt;
    private String notes;
    private String vendorName;
}
