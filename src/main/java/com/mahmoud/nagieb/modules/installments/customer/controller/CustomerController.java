package com.mahmoud.nagieb.modules.installments.customer.controller;

import com.mahmoud.nagieb.modules.installments.contract.dto.ContractResponse;
import com.mahmoud.nagieb.modules.installments.contract.service.ContractService;
import com.mahmoud.nagieb.modules.installments.customer.dto.*;
import com.mahmoud.nagieb.modules.installments.customer.entity.CustomerAccountLink;
import com.mahmoud.nagieb.modules.installments.customer.enums.CustomerRelationshipType;
import com.mahmoud.nagieb.modules.installments.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for Customer management.
 *
 * @author Mahmoud
 */
@RestController
@RequestMapping("/api/v1/customers")
@AllArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final ContractService contractService;
    private final MessageSource  messageSource;
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {

        return ResponseEntity.ok(customerService.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<CustomerSummary>> list(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size,
                                      @RequestParam(required = false) String search) {

        Page<CustomerSummary> customerPage  = customerService.list(page, size, search);
        return ResponseEntity.ok(customerPage);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        String message = customerService.softDelete(id);
        return ResponseEntity.ok(message);
    }

    /**
     * Retrieves customer with all their contracts.
     */
    @GetMapping("/{id}/with-contracts")
    public ResponseEntity<CustomerWithContarctsResponse> getCustomerWithContracts(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerWithContracts(id));
    }

    /**
     * Retrieves paginated contracts for a customer.
     */
    @GetMapping("/{id}/contracts")
    public ResponseEntity<Page<ContractResponse>> getCustomerContracts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(contractService.getCustomerWithContracts(id, page, size));
    }

    /**
     * Links two customer accounts together.
     */
    @PostMapping("/{customerId}/link/{linkedCustomerId}")
    public ResponseEntity<Void> linkCustomerAccounts(
            @PathVariable Long customerId,
            @PathVariable Long linkedCustomerId,
            @RequestParam CustomerRelationshipType relationshipType,
            @RequestParam(required = false) String description) {

        customerService.linkCustomerAccounts(customerId, linkedCustomerId, relationshipType, description);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Gets all linked accounts for a customer.
     */
    @GetMapping("/{customerId}/linked-accounts")
    public ResponseEntity<List<CustomerAccountLinkResponse>> getLinkedAccounts(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.getLinkedAccounts(customerId));
    }

    @GetMapping("/linked-accounts/by-relation-type")
    public ResponseEntity<List<CustomerAccountLinkResponse>> getLinkedAccountsByRelationType(
            @RequestParam CustomerRelationshipType relationshipType) {
        return ResponseEntity.ok(customerService.getLinkedAccountsByRelationType(relationshipType));
    }

    /**
     * Gets statistics about active customers.
     */
    @GetMapping("/stats/count")
    public ResponseEntity<Map<String, Long>> getCustomerCount() {

        long countActive = customerService.getActiveCustomerCount();
        long countInactive = customerService.getInactiveCustomerCount();

        String activeMessage = messageSource.getMessage("customer.count.active", null, null);
        String inactiveMessage = messageSource.getMessage("customer.count.inactive", null, null);

        return ResponseEntity.ok(Map.of(
                activeMessage, countActive,
                inactiveMessage, countInactive
        ));
    }
}