package com.mahmoud.maalflow.modules.installments.payment.service;

import com.mahmoud.maalflow.modules.installments.payment.entity.Payment;
import com.mahmoud.maalflow.modules.installments.payment.mapper.PaymentMapper;
import com.mahmoud.maalflow.modules.installments.payment.repo.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentQueryServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentQueryService paymentQueryService;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void search_withoutFilters_passesNonNullSpecificationToRepository() {
        when(paymentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty(PageRequest.of(0, 10)));

        Page<?> result = paymentQueryService.search(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                0,
                10
        );

        ArgumentCaptor<Specification<Payment>> specCaptor = ArgumentCaptor.forClass((Class) Specification.class);
        verify(paymentRepository).findAll(specCaptor.capture(), any(Pageable.class));
        assertNotNull(specCaptor.getValue());
        assertFalse(result.hasContent());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void findAllByFilters_withoutFilters_passesNonNullSpecificationToRepository() {
        when(paymentRepository.findAll(any(Specification.class))).thenReturn(List.of());

        List<Payment> result = paymentQueryService.findAllByFilters(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        ArgumentCaptor<Specification<Payment>> specCaptor = ArgumentCaptor.forClass((Class) Specification.class);
        verify(paymentRepository).findAll(specCaptor.capture());
        assertNotNull(specCaptor.getValue());
        assertNotNull(result);
    }
}



