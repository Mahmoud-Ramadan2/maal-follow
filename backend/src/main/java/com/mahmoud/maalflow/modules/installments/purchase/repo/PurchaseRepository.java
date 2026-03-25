package com.mahmoud.maalflow.modules.installments.purchase.repo;

import com.mahmoud.maalflow.modules.installments.purchase.dto.PurchaseResponse;
import com.mahmoud.maalflow.modules.installments.purchase.entity.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    @Query("""
                    SELECT new com.mahmoud.maalflow.modules.installments.purchase.dto.PurchaseResponse(
                                  p.id, p.productName, p.buyPrice, p.purchaseDate, p.createdAt, p.notes, v.name
                                 )FROM Purchase p LEFT JOIN p.vendor v
            WHERE (:search IS NULL OR
                                       LOWER(p.productName) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<PurchaseResponse> findAllByProductNameContainingIgnoreCase(String search, Pageable pageable);

    @Query("""
            SELECT new com.mahmoud.maalflow.modules.installments.purchase.dto.PurchaseResponse(
                   p.id, p.productName, p.buyPrice, p.purchaseDate, p.createdAt, p.notes, v.name)
                   FROM Purchase p JOIN p.vendor v
            WHERE (:vendorId IS NULL OR v.id = :vendorId) AND
                  (:startDate IS NULL OR p.purchaseDate >= :startDate) AND
                  (:endDate IS NULL OR p.purchaseDate <= :endDate) AND
                  (:searchTerm IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                              OR
                                  LOWER(p.notes) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                              )
            """)
    Page<PurchaseResponse> searchPurchases(@Param("vendorId") Long vendorId,
            @Param("startDate") LocalDate startDate,
           @Param("endDate") LocalDate endDate,
           @Param("searchTerm") String searchTerm,
            Pageable pageable
    );
    @Query("""
            SELECT new com.mahmoud.maalflow.modules.installments.purchase.dto.PurchaseResponse(
                   p.id, p.productName, p.buyPrice, p.purchaseDate, p.createdAt, p.notes, v.name)
                   FROM Purchase p JOIN p.vendor v WHERE p.id = :id
            """)
    Optional<PurchaseResponse> findPurchaseResponse(@Param("id") Long id);

    // Statistics queries
    @Query("SELECT COUNT(p) FROM Purchase p")
    long countAllPurchases();

    @Query("SELECT COALESCE(SUM(p.buyPrice), 0) FROM Purchase p")
    BigDecimal sumAllBuyPrices();

    @Query("SELECT p.vendor.name, COUNT(p) FROM Purchase p  GROUP BY p.vendor.name")
    List<Object[]> countGroupedByVendor();

    @Query("SELECT p.vendor.name, COALESCE(SUM(p.buyPrice), 0) FROM Purchase p GROUP BY p.vendor.name")
    List<Object[]> sumGroupedByVendor();

}
