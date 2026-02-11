package com.mahmoud.nagieb.modules.installments.customer.repo;

import com.mahmoud.nagieb.modules.installments.customer.dto.CustomerAccountLinkResponse;
import com.mahmoud.nagieb.modules.installments.customer.entity.CustomerAccountLink;
import com.mahmoud.nagieb.modules.installments.customer.enums.CustomerRelationshipType;
import jakarta.persistence.Entity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CustomerAccountLink entity.
 */
@Repository
public interface CustomerAccountLinkRepository extends JpaRepository<CustomerAccountLink, Long> {

    @Query("""
    SELECT new com.mahmoud.nagieb.modules.installments.customer.dto.CustomerAccountLinkResponse
                (
                c.name,
                lc.name,
                cal.relationshipType,
                cal.relationshipDescription,
                cal.isActive,
                u.name,
                cal.createdAt,
                cal.updatedAt
                )
         FROM CustomerAccountLink cal
        JOIN cal.customer c
        JOIN  cal.linkedCustomer lc
        JOIN  cal.createdBy u
        WHERE cal.customer.id = :customerId
        AND cal.isActive = true
""")
    List<CustomerAccountLinkResponse> findByCustomerIdAndIsActiveTrue(Long customerId);

    @Query("""
    SELECT new com.mahmoud.nagieb.modules.installments.customer.dto.CustomerAccountLinkResponse
                (
                c.name,
                lc.name,
                cal.relationshipType,
                cal.relationshipDescription,
                cal.isActive,
                u.name,
                cal.createdAt,
                cal.updatedAt
                )
         FROM CustomerAccountLink cal
        JOIN cal.customer c
        JOIN  cal.linkedCustomer lc
        JOIN  cal.createdBy u
        WHERE cal.linkedCustomer.id = :linkedCustomerId
        AND cal.isActive = true
""")
    List<CustomerAccountLinkResponse> findByLinkedCustomerIdAndIsActiveTrue(Long linkedCustomerId);


@Query("""
    SELECT new com.mahmoud.nagieb.modules.installments.customer.dto.CustomerAccountLinkResponse(
                c.name,
                lc.name,
                cal.relationshipType,
                cal.relationshipDescription,
                cal.isActive,
                u.name,
                cal.createdAt,
                cal.updatedAt
                )
         FROM CustomerAccountLink cal
        JOIN cal.customer c
        JOIN  cal.linkedCustomer lc
        JOIN  cal.createdBy u
        WHERE cal.relationshipType = :relationshipType
        AND cal.isActive = true
""")
    List<CustomerAccountLinkResponse> findByRelationshipTypeAndIsActiveTrue(CustomerRelationshipType relationshipType);



    @Query(""" 
    SELECT new com.mahmoud.nagieb.modules.installments.customer.dto.CustomerAccountLinkResponse(
                c.name,
                lc.name,
                cal.relationshipType,
                cal.relationshipDescription,
                cal.isActive,
                u.name,
                cal.createdAt,
                cal.updatedAt
                )
         FROM CustomerAccountLink cal
        JOIN cal.customer c
        JOIN  cal.linkedCustomer lc
        JOIN  cal.createdBy u
        WHERE (c.id = :customerId OR lc.id = :customerId)
        AND cal.isActive = true
""")
    List<CustomerAccountLinkResponse> findAllLinksForCustomer(@Param("customerId") Long customerId);

    Optional<CustomerAccountLink> findByCustomerIdAndLinkedCustomerId(Long customerId, Long linkedCustomerId);

    boolean existsByCustomerIdAndLinkedCustomerId(Long customerId, Long linkedCustomerId);
}

