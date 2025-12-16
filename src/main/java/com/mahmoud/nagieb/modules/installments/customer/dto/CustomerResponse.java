package com.mahmoud.nagieb.modules.installments.customer.dto;

import lombok.Data;

/**
 * @author Mahmoud
 */
@Data
public class CustomerResponse {

    private String name;
    private String phone;
    private String address;
    private Long nationalId;
    private String notes;
}
