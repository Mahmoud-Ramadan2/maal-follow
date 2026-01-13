package com.mahmoud.nagieb.modules.installments.customer.service;

import com.mahmoud.nagieb.exception.DuplicateNationalIdException;
import com.mahmoud.nagieb.exception.UserNotFoundException;
import com.mahmoud.nagieb.modules.installments.customer.dto.CustomerRequest;
import com.mahmoud.nagieb.modules.installments.customer.dto.CustomerResponse;
import com.mahmoud.nagieb.modules.installments.customer.entity.Customer;
import com.mahmoud.nagieb.modules.installments.customer.mapper.CustomerMapper;
import com.mahmoud.nagieb.modules.installments.customer.repo.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;

import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper mapper;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private CustomerService customerService;

//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        customerService = new CustomerService(customerRepository, mapper, messageSource);
//    }

    @Test
    void testCreate_CustomerCreatedSuccessfully() {
        // Arrange
        String name = "احمد محمد";
        String phone = "01145675347";
        String address = "123 Street";
        String nationalId = "12345678901234";
        String notes = "اختبار ملاحظات";

        CustomerRequest request = new CustomerRequest(name, phone, address, nationalId, notes);
        Customer customer = new Customer();
        when(mapper.toCustomer(request)).thenReturn(customer);
        when(customerRepository.save(customer)).thenReturn(customer);
        CustomerResponse response = new CustomerResponse();
        when(mapper.toCustomerResponse(customer)).thenReturn(response);

        // Act
        CustomerResponse actualResponse = customerService.create(request);

        // Assert
        assertEquals(response, actualResponse);
    }

    @Test
    void testCreate_NationalIdAlreadyExists_ThrowsDuplicateNationalIdException() {
        // Arrange
        String name = "احمد محمد";
        String phone = "01145675347";
        String address = "123 Street";
        String nationalId = "12345678901234";
        String notes = "اختبار ملاحظات";

        CustomerRequest request = new CustomerRequest(name, phone, address, nationalId, notes);
        when(customerRepository.existsByNationalId(nationalId)).thenReturn(true);
        // Act & Assert
        assertThrows(DuplicateNationalIdException.class, () -> customerService.create(request));
        verify(customerRepository, never()).save(any());
    }

    // success soft delete test
    @Test
    void testDelete_CustomerDeletedSuccessfully() {
        // Arrange
        Long customerId = 1L;
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setName("Ahmed");
        when(customerRepository.findByIdAndActiveTrue(customerId)).thenReturn(Optional.of(customer));
        when(messageSource.getMessage(
                eq("messages.customer.deleted") , any(),
                any())).thenReturn("Customer Ahmed deleted");
        // Act
        String result = customerService.softDelete(customerId);
        // Assert
        assertEquals("Customer Ahmed deleted", result);
        verify(customerRepository, times(1)).unactiveCustomerById(customerId);

    }

    @Test
    void testDelete_CustomerNotFound_ThrowsException() {
        // Arrange
        Long customerId = 1L;
        when(customerRepository.findByIdAndActiveTrue(customerId)).thenReturn(Optional.empty());

        // act & assert
        assertThrows(UserNotFoundException.class, ()-> customerService.softDelete(customerId));
        verify(customerRepository, never()).unactiveCustomerById(any());

    }
}