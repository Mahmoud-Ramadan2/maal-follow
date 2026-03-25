package com.mahmoud.maalflow.modules.installments.customer.enums;

/**
 * Types of relationships between linked customer accounts.
 */
public enum CustomerRelationshipType {
    SAME_PERSON,        // Same person with multiple accounts
    FAMILY_MEMBER,      // Family member
    BUSINESS_PARTNER,   // Business partner
    GUARANTOR,          // Guarantor for the customer
    OTHER               // Other relationship
}

