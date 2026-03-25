package com.mahmoud.maalflow.modules.installments.customer.repo;

import com.mahmoud.maalflow.modules.installments.customer.dto.CustomerAccountLinkResponse;
import com.mahmoud.maalflow.modules.installments.customer.entity.CustomerAccountLink;
import com.mahmoud.maalflow.modules.installments.customer.enums.CustomerRelationshipType;
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
    SELECT new com.mahmoud.maalflow.modules.installments.customer.dto.CustomerAccountLinkResponse
                (
                lc.id,
                lc.name,
                lc.phone,
                cal.relationshipType,
                cal.relationshipDescription,
                                lc.active,
                cal.createdAt
                )
         FROM CustomerAccountLink cal
        JOIN cal.customer c
        JOIN  cal.linkedCustomer lc
        JOIN  cal.createdBy u
        WHERE cal.customer.id = :customerId
""")
    List<CustomerAccountLinkResponse> findByCustomerIdAndIsActiveTrue(Long customerId);

    @Query("""
    SELECT new com.mahmoud.maalflow.modules.installments.customer.dto.CustomerAccountLinkResponse
                (
                lc.id,
                lc.name,
                lc.phone,
                cal.relationshipType,
                cal.relationshipDescription,
                                lc.active,
                cal.createdAt
                )
         FROM CustomerAccountLink cal
        JOIN cal.customer c
        JOIN  cal.linkedCustomer lc
        JOIN  cal.createdBy u
        WHERE cal.linkedCustomer.id = :linkedCustomerId
""")
    List<CustomerAccountLinkResponse> findByLinkedCustomerId(Long linkedCustomerId);


@Query("""
    SELECT new com.mahmoud.maalflow.modules.installments.customer.dto.CustomerAccountLinkResponse(
                lc.id,
                lc.name,
                lc.phone,
                cal.relationshipType,
                cal.relationshipDescription,
                                lc.active,
                cal.createdAt
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
    SELECT new com.mahmoud.maalflow.modules.installments.customer.dto.CustomerAccountLinkResponse(
                lc.id,
                lc.name,
                lc.phone,
                cal.relationshipType,
                cal.relationshipDescription,
                lc.active,
                cal.createdAt
                )
         FROM CustomerAccountLink cal
        JOIN cal.linkedCustomer lc
        WHERE (cal.customer.id = :customerId)
        UNION ALL 
            SELECT new com.mahmoud.maalflow.modules.installments.customer.dto.CustomerAccountLinkResponse(
                c.id,
                c.name,
                c.phone,
                cal.relationshipType,
                cal.relationshipDescription,
                c.active,
                cal.createdAt
                )
         FROM CustomerAccountLink cal
        JOIN cal.customer c
        WHERE (cal.linkedCustomer.id = :customerId)
""")
    List<CustomerAccountLinkResponse> findAllLinksForCustomer(@Param("customerId") Long customerId);

    Optional<CustomerAccountLink> findByCustomerIdAndLinkedCustomerId(Long customerId, Long linkedCustomerId);

    boolean existsByCustomerIdAndLinkedCustomerId(Long customerId, Long linkedCustomerId);
}

