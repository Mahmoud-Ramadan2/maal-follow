package com.mahmoud.nagieb.modules.installments.customer.service;

import com.mahmoud.nagieb.exception.AccessDeniedException;
import com.mahmoud.nagieb.exception.DuplicateNationalIdException;
import com.mahmoud.nagieb.exception.UserNotFoundException;
import com.mahmoud.nagieb.modules.installments.customer.dto.CustomerRequest;
import com.mahmoud.nagieb.modules.installments.customer.dto.CustomerResponse;
import com.mahmoud.nagieb.modules.installments.customer.dto.CustomerSummary;
import com.mahmoud.nagieb.modules.installments.customer.entity.Customer;
import com.mahmoud.nagieb.modules.installments.customer.repo.CustomerRepository;
import com.mahmoud.nagieb.modules.installments.customer.mapper.CustomerMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author Mahmoud
 */
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper mapper;
    private final MessageSource messageSource;


    public CustomerService(CustomerRepository customerRepository, CustomerMapper mapper, MessageSource messageSource) {
        this.customerRepository = customerRepository;
        this.mapper = mapper;
        this.messageSource = messageSource;
    }

    public CustomerResponse create(CustomerRequest request) {
        if (request.getNationalId() != null && customerRepository.existsByNationalId(request.getNationalId())) {
            throw new DuplicateNationalIdException("messages.customer.nationalId.exists", request.getNationalId());
        }
        Customer customer = mapper.toCustomer(request);
        customerRepository.save(customer);
        return mapper.toCustomerResponse(customer);
    }


    public CustomerResponse update(Long id, CustomerRequest request) {
        Optional<Customer> result = customerRepository.findById(id);
        if (result.isPresent()) {
            Customer existingCustomer = result.get();
            Customer updatedCustomer = mapper.toCustomer(request);
            updatedCustomer.setId(existingCustomer.getId());
            updatedCustomer.setCreatedAt(existingCustomer.getCreatedAt());
            customerRepository.save(updatedCustomer);
            return mapper.toCustomerResponse(updatedCustomer);
        } else {
            throw new UserNotFoundException("messages.customer.notFound", id);
        }
    }

    public CustomerResponse getById(Long id) {
        Optional<Customer> result = customerRepository.findByIdAndActiveTrue(id);
        if (result.isPresent()) {
            return mapper.toCustomerResponse(result.get());
        } else {
            throw new UserNotFoundException("messages.customer.notFound", id);
        }
    }


    public Page<CustomerSummary> list(int page, int size, String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        if (search == null || search.isBlank()) {
            return customerRepository.findAllByActiveTrue(pageable)
                    .map(customer -> mapper.toCustomerSummary(customer));
        } else {
            return customerRepository.findByNameContainingIgnoreCaseAndActiveTrue(search, pageable)
                    .map(mapper::toCustomerSummary);
        }
    }

    public Page<CustomerSummary> listDeleted(int page, int size, String search, boolean isAdmin) {
        if (!isAdmin) {
            throw new AccessDeniedException("messages.access.denied");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        if (search == null || search.isBlank()) {
            return customerRepository.findAllByActiveFalse(pageable)
                    .map(mapper::toCustomerSummary);
        } else {
            return customerRepository.findByNameContainingIgnoreCaseAndActiveFalse(search, pageable)
                    .map(mapper::toCustomerSummary);
        }
    }



    // softly delete
    @Transactional
    public String softDelete(Long id) {
        Customer customer = customerRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new UserNotFoundException("messages.customer.notFound", id));

        customerRepository.unactiveCustomerById(id);
        return messageSource.getMessage("messages.customer.deleted", new Object[]{customer.getName()}, LocaleContextHolder.getLocale());
    }
}

