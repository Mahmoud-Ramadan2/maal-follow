package com.mahmoud.maalflow.modules.installments.purchase.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PurchaseRequest {

    @NotNull(message = "{messages.vendorId.required}")
    @Positive
    private Long vendorId;
    
    @NotBlank(message = "{messages.productName.required}")
    private String productName;
    @NotNull(message = "{messages.buyPrice.required}")
    @DecimalMin(value = "1", message = "{messages.buyPrice.invalid}")
    @Digits(integer = 10, fraction = 2, message = "{messages.buyPrice.format}")
    private BigDecimal buyPrice;
    @NotNull(message = "{messages.purchaseDate.required}")
//    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "{messages.purchaseDate.invalid}")
    @PastOrPresent(message = "{messages.purchaseDate.invalid}")
    private LocalDate purchaseDate;
    @Size(max = 500, message = "{validation.notes.size}")
    private String notes;


}
