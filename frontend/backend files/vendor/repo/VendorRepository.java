package com.mahmoud.maalflow.modules.installments.vendor.repo;

import com.mahmoud.maalflow.modules.installments.vendor.entity.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {

    boolean existsByName(String name);

    boolean existsByPhone(String phone);

    boolean existsByIdAndActiveTrue(Long id);

    Page<Vendor> findAllByActiveTrue(Pageable pageable);

   Page<Vendor> findByNameContainingIgnoreCaseAndActiveTrue(String search, Pageable pageable);

    Page<Vendor> findAllByActiveFalse(Pageable pageable);

    Page<Vendor> findByNameContainingIgnoreCaseAndActiveFalse(String search, Pageable pageable);

    @Query("SELECT v FROM Vendor v LEFT JOIN FETCH v.purchases WHERE v.id = :id AND v.active = true")
    Optional<Vendor> findWithPurchase(@Param("id") Long id);

    // worng Pagination approach
    @Query("""
            SELECT v FROM Vendor v LEFT JOIN FETCH v.purchases WHERE
                    v.active = true AND
                    (:search IS NULL OR
                    LOWER(v.name) LIKE LOWER(CONCAT( '%', :search, '%')) OR LOWER(v.phone) LIKE LOWER(CONCAT('%', :search, '%')))
""")
    Page<Vendor> findAllByActiveTrueWithPurchase(String search, Pageable pageable);
}