package com.mahmoud.maalflow.modules.installments.purchase.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * @author Mahmoud
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseFilter {
    private Long vendorId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String searchTerm;
}
