package com.mahmoud.maalflow.modules.installments.customer.service;

import com.mahmoud.maalflow.exception.AccessDeniedException;
import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.exception.DuplicateNationalIdException;
import com.mahmoud.maalflow.exception.UserNotFoundException;
import com.mahmoud.maalflow.modules.installments.contract.dto.ContractResponse;
import com.mahmoud.maalflow.modules.installments.contract.mapper.ContractMapper;
import com.mahmoud.maalflow.modules.installments.customer.dto.*;
import com.mahmoud.maalflow.modules.installments.customer.entity.Customer;
import com.mahmoud.maalflow.modules.installments.customer.entity.CustomerAccountLink;
import com.mahmoud.maalflow.modules.installments.customer.enums.CustomerRelationshipType;
import com.mahmoud.maalflow.modules.installments.customer.mapper.CustomerAccountLinkMapper;
import com.mahmoud.maalflow.modules.installments.customer.mapper.CustomerMapper;
import com.mahmoud.maalflow.modules.installments.customer.repo.CustomerAccountLinkRepository;
import com.mahmoud.maalflow.modules.installments.customer.repo.CustomerRepository;
import com.mahmoud.maalflow.modules.shared.user.repo.UserRepository;
import com.mahmoud.maalflow.modules.shared.utils.Utiles;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing customers with comprehensive business validation.
 *
 * @author Mahmoud
 */
@Service
@AllArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerAccountLinkRepository customerAccountLinkRepository;
    private  final UserRepository userRepository;
    private final CustomerMapper mapper;
    private final MessageSource messageSource;
    private final ContractMapper contractMapper;
    private final CustomerAccountLinkMapper customerAccountLinkMapper;
    private final static String ACTIVE= "active";
    private final static String NON_ACTIVE= "inactive";
    private final static String ALL= "all";


    /**
     * Creates a new customer with business validation.
     */
    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        log.info("Creating customer with name: {} and national ID: {}", request.getName(), request.getNationalId());

        // Business Rule 1: Validate national ID uniqueness
        if (request.getNationalId() != null && !request.getNationalId().isBlank()) {
            if (customerRepository.existsByNationalId(request.getNationalId())) {
                log.error("Duplicate national ID: {}", request.getNationalId());
                throw new DuplicateNationalIdException("messages.customer.nationalId.exists", request.getNationalId());
            }
        }

        // Business Rule 2: Validate phone uniqueness (warning if duplicate)
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            List<Customer> existingCustomers = customerRepository.findByPhoneAndActiveTrue(request.getPhone());
            if (!existingCustomers.isEmpty()) {
                log.warn("Phone number {} already exists for customer(s): {}",
                        request.getPhone(),
                        existingCustomers.stream().map(Customer::getName).collect(Collectors.joining(", ")));
                // Note: Not throwing exception as multiple customers can share phone numbers
                // but logging for audit purposes
            }
        }

        // Business Rule 3: Name must be at least 3 characters
        if (request.getName() != null && request.getName().trim().length() < 3) {
            throw new BusinessException("validation.name.size");
        }

        Customer customer = mapper.toCustomer(request);

        // TODO: Set createdBy from security context when authentication is implemented

        customer.setCreatedBy(userRepository.findById(1L).orElse(null));

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Successfully created customer with ID: {} and name: {}",
                savedCustomer.getId(), savedCustomer.getName());

        return mapper.toCustomerResponse(savedCustomer);
    }

    /**
     * Updates an existing customer with validation.
     */
    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        log.info("Updating customer with ID: {}", id);

        Customer existingCustomer = customerRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> {
                    log.error("Customer not found with id: {}", id);
                    return new UserNotFoundException("messages.customer.notFound", id);
                });

        // Business Rule: Validate national ID uniqueness if changed
        if (request.getNationalId() != null && !request.getNationalId().equals(existingCustomer.getNationalId())) {
            if (customerRepository.existsByNationalId(request.getNationalId())) {
                throw new DuplicateNationalIdException("messages.customer.nationalId.exists", request.getNationalId());
            }
        }

        // Update fields
        if (request.getName() != null) {
            if (request.getName().trim().length() < 3) {
                throw new BusinessException("validation.name.size");
            }
            existingCustomer.setName(request.getName());
        }
        if (request.getPhone() != null) {
            existingCustomer.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            existingCustomer.setAddress(request.getAddress());
        }
        if (request.getNationalId() != null) {
            existingCustomer.setNationalId(request.getNationalId());
        }
        if (request.getNotes() != null) {
            existingCustomer.setNotes(request.getNotes());
        }

        Customer updatedCustomer = customerRepository.save(existingCustomer);
        log.info("Successfully updated customer with ID: {}", id);

        return mapper.toCustomerResponse(updatedCustomer);
    }

    /**
     * Gets customer by ID.
     */
    @Transactional(readOnly = true)
    public CustomerResponse getById(Long id) {
        boolean isAdmin = true; // TODO: Get from security context
        boolean isActive = customerRepository.existsByIdAndActiveTrue(id);
         if (isAdmin){
            log.info("Admin access: Fetching customer with ID: {} regardless of active status", id);
            Customer customer = customerRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("messages.customer.notFound", id));
            return mapper.toCustomerResponse(customer);
        }
         else if (!isActive && !isAdmin) {
             log.error("Customer with ID: {} is inactive and user is not admin", id);
             throw new AccessDeniedException("messages.access.denied");
         }
        Customer customer = customerRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new UserNotFoundException("messages.customer.notFound", id));
        return mapper.toCustomerResponse(customer);
    }

    /**
     * Gets customer with all contracts.
     */
    @Transactional(readOnly = true)
    public CustomerWithContarctsResponse getCustomerWithContracts(Long id) {
            log.info("Fetching customer with contracts for ID: {}", id);

        Customer customer = customerRepository.findWithContractsById(id)
                .orElseThrow(() -> new UserNotFoundException("messages.customer.notFound", id));

        CustomerWithContarctsResponse response = new CustomerWithContarctsResponse();
        response.setId(customer.getId());
        response.setName(customer.getName());
        response.setPhone(customer.getPhone());
        response.setAddress(customer.getAddress());
        response.setNationalId(customer.getNationalId());
        response.setNotes(customer.getNotes());
        response.setActive(customer.isActive());
        response.setCreatedAt(customer.getCreatedAt());
        response.setUpdatedAt(customer.getUpdatedAt());
        response.setCreatedBy(customer.getCreatedBy() != null ? customer.getCreatedBy().getName() : null);

        if (customer.getContracts() != null) {
            List<ContractResponse> contracts = customer.getContracts().stream()
                    .map(contractMapper::toContractResponse)
                    .collect(Collectors.toList());

            response.setContracts(contracts);
        }

        return response;
    }

    /**
     * Lists customers with pagination and search.
     */
//    @Transactional(readOnly = true)
//    public Page<CustomerSummary> list(Pageable pageable, String search) {
//        //  spring auto generate pageRequest from query params and we can use it directly in repository method
////        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
//
//        if (search == null || search.isBlank()) {
//            return customerRepository.findAllByActiveTrue(pageable)
//                    .map(mapper::toCustomerSummary);
//        } else {
//            return customerRepository.searchCustomers(search, pageable)
//                    .map(mapper::toCustomerSummary);
//        }
//    }

    // v2 of list that return all, active , non active debending on filters
    @Transactional(readOnly = true)
    public Page<CustomerSummary> list(Pageable pageable, String search, String status) {
        //  spring auto generate pageRequest from query params and we can use it directly in repository method
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("updatedAt").descending());
        String searchTerm =  null;
        if (search != null && !search.isBlank()) {
             searchTerm = search.toLowerCase();
        }
        Page<CustomerSummary> customers;
            if(status.equalsIgnoreCase(ALL)){
                //TODO check is admin
                customers = customerRepository.searchCustomers(pageable, searchTerm)
                    .map(mapper::toCustomerSummary);
            } else if(status.equalsIgnoreCase(NON_ACTIVE)){
                //TODO check is admin
                customers = customerRepository.searchCustomers(pageable, searchTerm, false)
                    .map(mapper::toCustomerSummary);
            } else {
            customers = customerRepository.searchCustomers(pageable, searchTerm, true)
                .map(mapper::toCustomerSummary);
        }

            return customers;

    }

    /**
     * Lists deleted customers (admin only).
     */
    @Transactional(readOnly = true)
    public Page<CustomerSummary> listDeleted(Pageable pageable, String search, boolean isAdmin) {
        if (!isAdmin) {
            throw new AccessDeniedException("messages.access.denied");
        }


        if (search == null || search.isBlank()) {
            return customerRepository.findAllByActiveFalse(pageable)
                    .map(mapper::toCustomerSummary);
        } else {
            return customerRepository.findByNameContainingIgnoreCaseAndActiveFalse(search, pageable)
                    .map(mapper::toCustomerSummary);
        }
    }

    /**
     * Soft deletes a customer.
     */
    @Transactional
    public String softDelete(Long id) {
        log.info("Soft deleting customer with ID: {}", id);

        Customer customer = customerRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new UserNotFoundException("messages.customer.notFound", id));

        // Business Rule: Cannot delete customer with active contracts
        if (customer.getContracts() != null && !customer.getContracts().isEmpty()) {
            long activeContracts = customer.getContracts().stream()
                    .filter(c -> "ACTIVE".equals(c.getStatus().name()) || "LATE".equals(c.getStatus().name()))
                    .count();

            if (activeContracts > 0) {
                log.error("Cannot delete customer with active contracts. Customer ID: {}, Active contracts: {}",
                         id, activeContracts);
                throw new BusinessException("messages.customer.delete.hasActiveContracts");
            }
        }

        customerRepository.unactiveCustomerById(id);
        log.info("Successfully soft deleted customer with ID: {}", id);

        return messageSource.getMessage("messages.customer.deleted",
                new Object[]{customer.getName()},
                LocaleContextHolder.getLocale());
    }

    /**
     * Links two customer accounts together.
     */
    @Transactional
    public void linkCustomerAccounts(Long customerId, Long linkedCustomerId,
                                     CustomerRelationshipType relationshipType,
                                     String description) {
        log.info("Linking customer {} with customer {}, relationship: {}",
                customerId, linkedCustomerId, relationshipType);

        // Validate both customers exist
        Customer customer = customerRepository.findByIdAndActiveTrue(customerId)
                .orElseThrow(() -> new UserNotFoundException("messages.customer.notFound", customerId));

        Customer linkedCustomer = customerRepository.findByIdAndActiveTrue(linkedCustomerId)
                .orElseThrow(() -> new UserNotFoundException("messages.customer.notFound", linkedCustomerId));

        // Business Rule: Cannot link customer to itself
        if (customerId.equals(linkedCustomerId)) {
            throw new BusinessException("messages.customer.link.cannotLinkToSelf");
        }

        // Business Rule: Check if link already exists
        if (customerAccountLinkRepository.existsByCustomerIdAndLinkedCustomerId(customerId, linkedCustomerId)) {
            throw new BusinessException("messages.customer.link.alreadyExists");
        }

        // Validate relationship type
        if (!Utiles.isValidEnumValue(CustomerRelationshipType.class, relationshipType.name())) {
            relationshipType = CustomerRelationshipType.OTHER;
        }

        CustomerAccountLink link = new CustomerAccountLink();
        link.setCustomer(customer);
        link.setLinkedCustomer(linkedCustomer);
        link.setRelationshipType(relationshipType);
        link.setRelationshipDescription(description);
        link.setActive(true);

        // TODO: Set createdBy from security context
        link.setCreatedBy(userRepository.findById(1L).orElse(null));

        customerAccountLinkRepository.save(link);
        log.info("Successfully linked customers {} and {}", customerId, linkedCustomerId);
    }

    /**
     * Gets all linked accounts for a customer.
     */
    @Transactional(readOnly = true)
    public List<CustomerAccountLinkResponse> getLinkedAccounts(Long customerId) {
        List<CustomerAccountLinkResponse> links = customerAccountLinkRepository.findAllLinksForCustomer(customerId);
        return links;
//        return links.stream()
//                .map(customerAccountLinkMapper::toCustomerAccountLinkResponse)
//                .collect(Collectors.toList());
    }

    /**
     * Gets count of active customers.
     */
    @Transactional(readOnly = true)
    public long getActiveCustomerCount() {
        return customerRepository.countActiveCustomers();
    }

    /**
     * Gets count of inactive customers.
     * @return
     */
    @Transactional(readOnly = true)
    public long getInactiveCustomerCount() {
        return customerRepository.countCustomersWithActiveFalse();

    }

    /**
     * Gets linked accounts by relationship type.
     */
    @Transactional(readOnly = true)
    public List<CustomerAccountLinkResponse> getLinkedAccountsByRelationType(CustomerRelationshipType relationshipType) {
        return customerAccountLinkRepository.findByRelationshipTypeAndIsActiveTrue(relationshipType);
    }
}
