package com.mahmoud.nagieb.modules.installments.customer.repo;

import com.mahmoud.nagieb.modules.installments.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Mahmoud
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Customer findByNationalId(Long nationalId);

    List<Customer> findByPhone(String phone);

    boolean existsByNationalId(Long nationalId);

    Page<Customer> findByNameContainingIgnoreCaseAndActiveTrue(String search, Pageable pageable);

    Page<Customer> findAllByActiveTrue(Pageable pageable);

    Page<Customer> findAllByActiveFalse(Pageable pageable);

    Page<Customer> findByNameContainingIgnoreCaseAndActiveFalse(String search, Pageable pageable);

    @Modifying
    @Query("update Customer set active = false where id = :id")
    void unactiveCustomerById(Long id);

    Optional<Customer> findByIdAndActiveTrue(Long id);

}
