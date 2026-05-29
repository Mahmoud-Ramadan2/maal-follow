package com.mahmoud.maalflow.modules.installments.schedule.dto;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleMetadataUpdateRequest {
    private LocalDate dueDate;
    @Size(max = 500, message = "{validation.notes.size}")
    private String notes;
    @Positive(message = "{messages.contract.collectorId.invalid}")
    private Long collectorId;
    private Boolean clearCollector;
}