package com.mahmoud.maalflow.modules.installments.customer.dto;

import lombok.Value;

/**
 * @author Mahmoud
 */
@Value
public class CustomerSummary {

     Long id;
     String name;
     String phone;
     String address;
     String nationalId;
     Boolean active;
    String notes;
     String createdAt;
}
