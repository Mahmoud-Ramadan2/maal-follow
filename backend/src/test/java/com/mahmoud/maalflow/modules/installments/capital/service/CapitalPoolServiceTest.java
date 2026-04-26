package com.mahmoud.maalflow.modules.installments.capital.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.modules.installments.capital.dto.CapitalPoolRequest;
import com.mahmoud.maalflow.modules.installments.capital.dto.CapitalPoolResponse;
import com.mahmoud.maalflow.modules.installments.capital.entity.CapitalPool;
import com.mahmoud.maalflow.modules.installments.capital.enums.CapitalTransactionType;
import com.mahmoud.maalflow.modules.installments.capital.mapper.CapitalPoolMapper;
import com.mahmoud.maalflow.modules.installments.capital.repo.CapitalPoolRepository;
import com.mahmoud.maalflow.modules.installments.capital.repo.CapitalTransactionRepository;
import com.mahmoud.maalflow.modules.installments.partner.service.PartnerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapitalPoolServiceTest {

    @Mock
    private CapitalPoolRepository capitalPoolRepository;

    @Mock
    private CapitalTransactionRepository capitalTransactionRepository;

    @Mock
    private CapitalPoolMapper capitalPoolMapper;

    @Mock
    private PartnerService partnerService;

    @InjectMocks
    private CapitalPoolService capitalPoolService;

    @Test
    void updateCapitalPool_updatesAvailableFromTotalMinusLocked_whenManualUpdateAllowed() {
        CapitalPoolRequest request = new CapitalPoolRequest();
        request.setTotalAmount(new BigDecimal("120.00"));
        request.setOwnerContribution(new BigDecimal("50.00"));
        request.setPartnerContributions(new BigDecimal("70.00"));
        request.setDescription("Manual correction");

        CapitalPool currentPool = CapitalPool.builder()
                .id(1L)
                .totalAmount(new BigDecimal("100.00"))
                .availableAmount(new BigDecimal("80.00"))
                .lockedAmount(new BigDecimal("20.00"))
                .returnedAmount(new BigDecimal("10.00"))
                .ownerContribution(new BigDecimal("40.00"))
                .partnerContributions(new BigDecimal("60.00"))
                .build();

        when(capitalPoolRepository.findById(1L)).thenReturn(Optional.of(currentPool));
        when(capitalTransactionRepository.count()).thenReturn(0L);
        when(capitalPoolRepository.save(any(CapitalPool.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(capitalPoolMapper.toResponse(any(CapitalPool.class))).thenReturn(new CapitalPoolResponse());

        capitalPoolService.updateCapitalPool(request);

        ArgumentCaptor<CapitalPool> poolCaptor = ArgumentCaptor.forClass(CapitalPool.class);
        verify(capitalPoolRepository).save(poolCaptor.capture());

        CapitalPool saved = poolCaptor.getValue();
        assertEquals(new BigDecimal("120.00"), saved.getTotalAmount());
        assertEquals(new BigDecimal("50.00"), saved.getOwnerContribution());
        assertEquals(new BigDecimal("70.00"), saved.getPartnerContributions());
        assertEquals(new BigDecimal("100.00"), saved.getAvailableAmount());
        assertEquals(new BigDecimal("20.00"), saved.getLockedAmount());
        assertEquals(new BigDecimal("10.00"), saved.getReturnedAmount());
    }

    @Test
    void updateCapitalPool_throwsWhenTransactionsExist() {
        CapitalPoolRequest request = new CapitalPoolRequest();
        request.setTotalAmount(new BigDecimal("100.00"));
        request.setOwnerContribution(new BigDecimal("40.00"));
        request.setPartnerContributions(new BigDecimal("60.00"));

        CapitalPool currentPool = CapitalPool.builder()
                .id(1L)
                .totalAmount(new BigDecimal("100.00"))
                .availableAmount(new BigDecimal("100.00"))
                .lockedAmount(BigDecimal.ZERO)
                .ownerContribution(new BigDecimal("40.00"))
                .partnerContributions(new BigDecimal("60.00"))
                .build();

        when(capitalPoolRepository.findById(1L)).thenReturn(Optional.of(currentPool));
        when(capitalTransactionRepository.count()).thenReturn(3L);

        assertThrows(BusinessException.class, () -> capitalPoolService.updateCapitalPool(request));
    }

    @Test
    void recalculateCapitalPool_preservesOwnerContribution() {
        CapitalPool currentPool = CapitalPool.builder()
                .id(1L)
                .totalAmount(new BigDecimal("200.00"))
                .availableAmount(new BigDecimal("170.00"))
                .lockedAmount(new BigDecimal("30.00"))
                .returnedAmount(new BigDecimal("5.00"))
                .ownerContribution(new BigDecimal("40.00"))
                .partnerContributions(new BigDecimal("160.00"))
                .build();

        when(capitalPoolRepository.findById(1L)).thenReturn(Optional.of(currentPool));
        when(capitalTransactionRepository.sumAmountByTransactionType(CapitalTransactionType.INVESTMENT))
                .thenReturn(new BigDecimal("100.00"));
        when(capitalTransactionRepository.sumAmountByTransactionType(CapitalTransactionType.WITHDRAWAL))
                .thenReturn(new BigDecimal("20.00"));
        when(capitalTransactionRepository.sumAmountByTransactionType(CapitalTransactionType.ALLOCATION))
                .thenReturn(new BigDecimal("50.00"));
        when(capitalTransactionRepository.sumAmountByTransactionType(CapitalTransactionType.RETURN))
                .thenReturn(new BigDecimal("10.00"));
        when(capitalPoolRepository.save(any(CapitalPool.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(capitalPoolMapper.toResponse(any(CapitalPool.class))).thenReturn(new CapitalPoolResponse());

        capitalPoolService.recalculateCapitalPool();

        ArgumentCaptor<CapitalPool> poolCaptor = ArgumentCaptor.forClass(CapitalPool.class);
        verify(capitalPoolRepository).save(poolCaptor.capture());

        CapitalPool saved = poolCaptor.getValue();
        assertEquals(new BigDecimal("40.00"), saved.getOwnerContribution());
        assertEquals(new BigDecimal("80.00"), saved.getPartnerContributions());
        assertEquals(new BigDecimal("120.00"), saved.getTotalAmount());
        assertEquals(new BigDecimal("40.00"), saved.getLockedAmount());
        assertEquals(new BigDecimal("80.00"), saved.getAvailableAmount());
        assertEquals(new BigDecimal("10.00"), saved.getReturnedAmount());
    }
}

