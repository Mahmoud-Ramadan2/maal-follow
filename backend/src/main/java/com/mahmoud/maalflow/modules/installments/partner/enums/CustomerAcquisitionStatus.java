package com.mahmoud.maalflow.modules.installments.partner.enums;

/**
 * Status of customer acquisition by partners.
 */
public enum CustomerAcquisitionStatus {
    PENDING,     //TODO: Customer acquisition is pending approval

    ACTIVE,      // Partner is actively managing this customer
    INACTIVE,    // Customer relationship is inactive
    TRANSFERRED, // Customer transferred to another partner
    TERMINATED   // Partnership with customer terminated
}
