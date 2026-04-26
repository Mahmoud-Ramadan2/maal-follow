package com.mahmoud.maalflow.modules.installments.partner.controller;

import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerInvestmentRequest;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerInvestmentResponse;
import com.mahmoud.maalflow.modules.installments.partner.service.PartnerInvestmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartnerInvestmentControllerTest {

    @Mock
    private PartnerInvestmentService investmentService;

    @InjectMocks
    private PartnerInvestmentController controller;

    @Test
    void createInvestment_returnsCorrectLocationHeader() {
        PartnerInvestmentRequest request = PartnerInvestmentRequest.builder()
                .partnerId(5L)
                .amount(new BigDecimal("100.00"))
                .build();

        PartnerInvestmentResponse serviceResponse = new PartnerInvestmentResponse();
        serviceResponse.setId(42L);

        when(investmentService.createInvestment(request)).thenReturn(serviceResponse);

        ResponseEntity<PartnerInvestmentResponse> response = controller.createInvestment(request);

        assertNotNull(response.getHeaders().getLocation());
        assertNotNull(response.getBody());
        assertEquals("/api/v1/partner-investments/42", response.getHeaders().getLocation().toString());
        assertEquals(42L, response.getBody().getId());
    }
}

