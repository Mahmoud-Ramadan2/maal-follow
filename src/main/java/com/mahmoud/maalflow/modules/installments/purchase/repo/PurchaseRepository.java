package com.mahmoud.maalflow.modules.installments.purchase.repo;

import com.mahmoud.maalflow.modules.installments.purchase.dto.PurchaseResponse;
import com.mahmoud.maalflow.modules.installments.purchase.entity.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    @Query("""
                    SELECT new com.mahmoud.maalflow.modules.installments.purchase.dto.PurchaseResponse(
                                  p.productName, p.buyPrice, p.purchaseDate, p.createdAt, p.notes, v.name
                                 )FROM Purchase p LEFT JOIN p.vendor v
            WHERE (:search IS NULL OR
                                       LOWER(p.productName) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<PurchaseResponse> findAllByProductNameContainingIgnoreCase(String search, Pageable pageable);

    @Query("""
            SELECT new com.mahmoud.maalflow.modules.installments.purchase.dto.PurchaseResponse(
                   p.productName, p.buyPrice, p.purchaseDate, p.createdAt, p.notes, v.name)
                   FROM Purchase p JOIN p.vendor v WHERE p.id = :id
            """)
    Optional<PurchaseResponse> findPurchaseResponse(@Param("id") Long id);

}
