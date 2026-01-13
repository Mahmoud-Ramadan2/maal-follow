package com.mahmoud.nagieb.modules.installments.customer.repo;

import com.mahmoud.nagieb.modules.installments.customer.entity.CustomerAccountLink;
import com.mahmoud.nagieb.modules.installments.customer.enums.CustomerRelationshipType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CustomerAccountLink entity.
 */
@Repository
public interface CustomerAccountLinkRepository extends JpaRepository<CustomerAccountLink, Long> {

    List<CustomerAccountLink> findByCustomerIdAndIsActiveTrue(Long customerId);

    List<CustomerAccountLink> findByLinkedCustomerIdAndIsActiveTrue(Long linkedCustomerId);

    List<CustomerAccountLink> findByRelationshipTypeAndIsActiveTrue(CustomerRelationshipType relationshipType);

    @Query("SELECT cal FROM CustomerAccountLink cal WHERE " +
           "(cal.customer.id = :customerId OR cal.linkedCustomer.id = :customerId) " +
           "AND cal.isActive = true")
    List<CustomerAccountLink> findAllLinksForCustomer(Long customerId);

    Optional<CustomerAccountLink> findByCustomerIdAndLinkedCustomerId(Long customerId, Long linkedCustomerId);

    boolean existsByCustomerIdAndLinkedCustomerId(Long customerId, Long linkedCustomerId);
}

