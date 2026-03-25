# Maal Flow - Associations Module (الجمعيات)

## What is This Module?

A "Gam3eya" (جمعية) is a rotating savings and credit association (ROSCA).
A group of people each contribute a fixed amount monthly. Each month, one member
receives the entire pool. This repeats until everyone has received once.

## Database Schema

### Migration: V25__create_associations_module.sql

```sql
-- Copy to: src/main/resources/db/migration/V25__create_associations_module.sql

CREATE TABLE association (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    monthly_amount DECIMAL(12,2) NOT NULL,
    total_members INT NOT NULL,
    duration_months INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    status ENUM('ACTIVE','COMPLETED','CANCELLED') DEFAULT 'ACTIVE',
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_association_created_by FOREIGN KEY (created_by) REFERENCES user(id)
);

CREATE TABLE association_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    association_id BIGINT NOT NULL,
    member_name VARCHAR(200) NOT NULL,
    phone VARCHAR(20),
    turn_order INT NOT NULL,
    has_received BOOLEAN DEFAULT FALSE,
    received_date DATE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_member_association FOREIGN KEY (association_id) REFERENCES association(id)
);

CREATE TABLE association_payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    association_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    payment_month VARCHAR(7) NOT NULL,
    payment_date DATE NOT NULL,
    status ENUM('PAID','PENDING','LATE') DEFAULT 'PENDING',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_apayment_association FOREIGN KEY (association_id) REFERENCES association(id),
    CONSTRAINT fk_apayment_member FOREIGN KEY (member_id) REFERENCES association_member(id)
);

CREATE INDEX idx_association_status ON association(status);
CREATE INDEX idx_member_association ON association_member(association_id);
CREATE INDEX idx_apayment_month ON association_payment(payment_month);
```

## Package Structure

```
modules/associations/
  controller/
    AssociationController.java
    AssociationMemberController.java
  dto/
    AssociationRequest.java
    AssociationResponse.java
    AssociationMemberRequest.java
    AssociationMemberResponse.java
    AssociationPaymentRequest.java
    AssociationPaymentResponse.java
  entity/
    Association.java
    AssociationMember.java
    AssociationPayment.java
  enums/
    AssociationStatus.java
    MemberPaymentStatus.java
  mapper/
    AssociationMapper.java
  repo/
    AssociationRepository.java
    AssociationMemberRepository.java
    AssociationPaymentRepository.java
  service/
    AssociationService.java
```

## Ready-to-Paste Code Files

See files in `files/code/associations/` directory.

