package com.mahmoud.maalflow.modules.associations.dto;

import com.mahmoud.maalflow.modules.associations.enums.AssociationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AssociationResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal monthlyAmount;
    private Integer totalMembers;
    private Integer durationMonths;
    private LocalDate startDate;
    private LocalDate endDate;
    private AssociationStatus status;
    private BigDecimal totalPoolAmount;
    private Integer currentRound;
    private LocalDateTime createdAt;
    private List<AssociationMemberResponse> members;
}

