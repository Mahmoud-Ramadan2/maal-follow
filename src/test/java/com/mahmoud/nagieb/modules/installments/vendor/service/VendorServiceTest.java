package com.mahmoud.nagieb.modules.installments.vendor.service;

import com.mahmoud.nagieb.exception.UserNotFoundException;
import com.mahmoud.nagieb.modules.installments.customer.mapper.CustomerMapper;
import com.mahmoud.nagieb.modules.installments.purchase.entity.Purchase;
import com.mahmoud.nagieb.modules.installments.vendor.dto.VendorResponse;
import com.mahmoud.nagieb.modules.installments.vendor.entity.Vendor;
import com.mahmoud.nagieb.modules.installments.vendor.repo.VendorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VendorServiceTest {

    @Mock
    private VendorRepository vendorRepository;
    @Mock
    private CustomerMapper mapper;
    @Mock
    private MessageSource messageSource;
    @InjectMocks
    private VendorService vendorService;

    @Test
    void testGetById_WithExistingVendor() {
        // Arrange
        Long vendorId = 1L;
        Vendor vendor = new Vendor();
        vendor.setName("Vendor Name");
        Purchase purchase = new Purchase();
        purchase.setProductName("Product 1");
        purchase.setBuyPrice(new BigDecimal("100.0"));
        vendor.setPurchases(List.of(purchase));

        when(vendorRepository.findWithPurchase(any())).thenReturn(Optional.of(vendor));

        // act
        VendorResponse actualResponse = vendorService.getById(vendorId);

        // Assert
        assertEquals("Vendor Name", actualResponse.getName());
        assertEquals(1, actualResponse.getPurchases().size());
        assertEquals("Product 1", actualResponse.getPurchases().get(0).getProductName());
        assertEquals(BigDecimal.valueOf(100.0), actualResponse.getPurchases().get(0).getBuyPrice());
        verify(vendorRepository, times(1)).findWithPurchase(any());

    }

    @Test
    void testGetById_WhenVendorHasNoPurchases() {
        // Arrange
        Long vendorId = 1L;
        Vendor vendor = new Vendor();
        vendor.setName("Vendor Name");
        Purchase purchase = new Purchase();
//        purchase.setProductName("Product 1");
//        purchase.setBuyPrice(new BigDecimal("100.0"));
        vendor.setPurchases(List.of(purchase));

        when(vendorRepository.findWithPurchase(any())).thenReturn(Optional.of(vendor));

        // act
        VendorResponse actualResponse = vendorService.getById(vendorId);

        // Assert & Verify
        assertEquals("Vendor Name", actualResponse.getName());
        assertEquals(1, actualResponse.getPurchases().size());
        assertNull(actualResponse.getPurchases().get(0).getProductName());
        assertNull(actualResponse.getPurchases().get(0).getBuyPrice());
        verify(vendorRepository, times(1)).findWithPurchase(any());
    }

        @Test
    void testGetById_WhenVendorNotFound() {
        // Arrange
            when(vendorRepository.findWithPurchase(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(UserNotFoundException.class, () -> vendorService.getById(1L));
    }

}
