package com.mahmoud.maalflow.modules.installments.contract.dto;


import com.mahmoud.maalflow.modules.installments.contract.enums.ContractStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ContractResponse {

    // Contract Identification
    private Long id;
    private String contractNumber;
    private ContractStatus status;

    // Customer Information
    private Long customerId;
    private String customerName;
    private Long responsibleUserId;
    private String responsibleUserName;

    // Product/Supplier Information
    private Long purchaseId;
    private String productName;
    private String vendorName;

    // Partner Information
    private Long partnerId;
    private String partnerName;

    // Financial Details - Base Pricing
    private BigDecimal originalPrice;
    private BigDecimal additionalCosts;
    private BigDecimal finalPrice;
    private BigDecimal downPayment;
    private BigDecimal remainingAmount;

    // Financial Details - Discounts
    private BigDecimal cashDiscountRate;
    private BigDecimal earlyPaymentDiscountRate;

    // Payment Schedule
    private Integer months;
    private BigDecimal monthlyAmount;
    private Integer agreedPaymentDay;

    // Profit Calculation
    private BigDecimal profitAmount;
    private BigDecimal totalExpenses;
    // Net Profit = Profit Amount - Total Expenses
    private BigDecimal netProfit;

    // Capital tracking
    private BigDecimal capitalAllocated;
    private BigDecimal capitalReturned;

    // Dates
    private LocalDate startDate;
    private LocalDate completionDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String notes;




}