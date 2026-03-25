package com.mahmoud.maalflow.modules.associations.dto;

import lombok.*;
import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AssociationMemberResponse {
    private Long id;
    private String memberName;
    private String phone;
    private Integer turnOrder;
    private Boolean hasReceived;
    private LocalDate receivedDate;
    private String notes;
    private Integer paymentsMade;
    private Integer paymentsExpected;
}

