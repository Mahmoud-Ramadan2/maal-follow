package com.mahmoud.nagieb.modules.installments.customer.controller;

import com.mahmoud.nagieb.modules.installments.customer.dto.CustomerRequest;
import com.mahmoud.nagieb.modules.installments.customer.dto.CustomerResponse;
import com.mahmoud.nagieb.modules.installments.customer.dto.CustomerSummary;
import com.mahmoud.nagieb.modules.installments.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author Mahmoud
 */
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {

        return ResponseEntity.status(201).body(customerService.create(request));
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
        return new ResponseEntity<>(customerPage , HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public  ResponseEntity<String> delete(@PathVariable Long id) {
        String message =customerService.softDelete(id);
        return ResponseEntity.ok(message);
    }
}