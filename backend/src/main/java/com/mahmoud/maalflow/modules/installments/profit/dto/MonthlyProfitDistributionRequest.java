package com.mahmoud.maalflow.modules.installments.profit.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MonthlyProfitDistributionRequest {

	@NotBlank(message = "{validation.monthYear.required}")
	@Pattern(regexp = "^\\d{4}-\\d{2}$", message = "{validation.monthYear.pattern}")
	private String monthYear;

	@NotNull(message = "{validation.totalProfit.required}")
	@DecimalMin(value = "0.00", message = "{validation.totalProfit.min}")
	@Digits(integer = 12, fraction = 2, message = "{validation.totalProfit.format}")
	private BigDecimal totalProfit;

	@NotNull(message = "{validation.managementFeePercentage.required}")
	@DecimalMin(value = "0.00", message = "{validation.managementFeePercentage.range}")
	@DecimalMax(value = "100.00", message = "{validation.managementFeePercentage.range}")
	@Digits(integer = 5, fraction = 2, message = "{validation.managementFeePercentage.format}")
	private BigDecimal managementFeePercentage;

	@NotNull(message = "{validation.zakatPercentage.required}")
	@DecimalMin(value = "0.00", message = "{validation.zakatPercentage.range}")
	@DecimalMax(value = "100.00", message = "{validation.zakatPercentage.range}")
	@Digits(integer = 5, fraction = 2, message = "{validation.zakatPercentage.format}")
	private BigDecimal zakatPercentage;

	@Size(max = 2000, message = "{validation.notes.size}")
	private String calculationNotes;
}

