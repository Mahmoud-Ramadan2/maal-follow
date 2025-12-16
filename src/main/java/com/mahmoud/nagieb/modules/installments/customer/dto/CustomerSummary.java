package com.mahmoud.nagieb.modules.installments.customer.dto;

import lombok.Data;
import lombok.Value;

/**
 * @author Mahmoud
 */
@Value
public class CustomerSummary {

     String name;
     String phone;
     String address;
     Long nationalId;
     String notes;
}
