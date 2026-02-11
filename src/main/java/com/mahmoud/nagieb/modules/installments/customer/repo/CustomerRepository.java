package com.mahmoud.nagieb.modules.installments.customer.repo;

import com.mahmoud.nagieb.modules.installments.customer.entity.Customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Customer entity with comprehensive query methods.
 *
 * @author Mahmoud
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Customer findByNationalId(String nationalId);

    List<Customer> findByPhone(String phone);

    List<Customer> findByPhoneAndActiveTrue(String phone);

    List<Customer> findByAddressContainingIgnoreCaseAndActiveTrue(String address);

    boolean existsByNationalId(String nationalId);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Long id);

    Page<Customer> findByNameContainingIgnoreCaseAndActiveTrue(String search, Pageable pageable);

    Page<Customer> findAllByActiveTrue(Pageable pageable);

    Page<Customer> findAllByActiveFalse(Pageable pageable);

    Page<Customer> findByNameContainingIgnoreCaseAndActiveFalse(String search, Pageable pageable);

    @Modifying
    @Query("UPDATE Customer c SET c.active = false WHERE c.id = :id")
    void unactiveCustomerById(@Param("id") Long id);

    Optional<Customer> findByIdAndActiveTrue(Long id);

    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.contracts WHERE c.id = :id AND c.active = true")
    Optional<Customer> findWithContractsById(@Param("id") Long id);



    @Query("SELECT c FROM Customer c WHERE c.active = true AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.phone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.address) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Customer> searchCustomers(@Param("search") String search, Pageable pageable);

    boolean existsByIdAndActiveTrue(Long id);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.active = true")
    long countActiveCustomers();

    @Query("SELECT COUNT(c) fROM Customer c WHERE c.active = false")
    long countCustomersWithActiveFalse();
}
