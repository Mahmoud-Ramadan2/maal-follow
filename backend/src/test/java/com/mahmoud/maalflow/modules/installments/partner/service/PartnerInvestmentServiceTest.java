package com.mahmoud.maalflow.modules.installments.partner.service;

import com.mahmoud.maalflow.modules.installments.capital.dto.CapitalTransactionRequest;
import com.mahmoud.maalflow.modules.installments.capital.entity.CapitalPool;
import com.mahmoud.maalflow.modules.installments.capital.enums.CapitalTransactionType;
import com.mahmoud.maalflow.modules.installments.capital.service.CapitalTransactionService;
import com.mahmoud.maalflow.modules.installments.ledger.service.LedgerService;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerInvestmentResponse;
import com.mahmoud.maalflow.modules.installments.partner.entity.Partner;
import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerInvestment;
import com.mahmoud.maalflow.modules.installments.partner.enums.InvestmentStatus;
import com.mahmoud.maalflow.modules.installments.partner.mapper.PartnerInvestmentMapper;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerInvestmentRepository;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartnerInvestmentServiceTest {

    @Mock
    private PartnerInvestmentRepository investmentRepository;

    @Mock
    private PartnerRepository partnerRepository;

    @Mock
    private PartnerInvestmentMapper investmentMapper;

    @Mock
    private LedgerService ledgerService;

    @Mock
    private CapitalTransactionService capitalTransactionService;

    @Mock
    private PartnerService partnerService;

    @InjectMocks
    private PartnerInvestmentService partnerInvestmentService;

    @Test
    void confirmInvestment_recordsCapitalTransaction_and_updatesCurrentBalance() {
        Partner partner = new Partner();
        partner.setId(7L);
        partner.setTotalWithdrawals(new BigDecimal("10.00"));

        PartnerInvestment investment = new PartnerInvestment();
        investment.setId(20L);
        investment.setPartner(partner);
        investment.setAmount(new BigDecimal("100.00"));
        investment.setStatus(InvestmentStatus.PENDING);

        when(investmentRepository.findById(20L)).thenReturn(Optional.of(investment));
        when(partnerRepository.existsById(7L)).thenReturn(true);
        when(investmentRepository.save(any(PartnerInvestment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(partnerRepository.findById(7L)).thenReturn(Optional.of(partner));
        when(investmentRepository.sumByPartnerIdAndStatus(7L, InvestmentStatus.CONFIRMED))
                .thenReturn(new BigDecimal("110.00"));
        when(investmentMapper.toPartnerInvestmentResponse(any(PartnerInvestment.class)))
                .thenReturn(new PartnerInvestmentResponse());
        CapitalPool pool = new CapitalPool();
        pool.setTotalAmount(new BigDecimal("500.00"));
        when(capitalTransactionService.getPoolForUpdate()).thenReturn(pool);

        partnerInvestmentService.confirmInvestment(20L);

        ArgumentCaptor<Partner> partnerCaptor = ArgumentCaptor.forClass(Partner.class);
        verify(partnerRepository).save(partnerCaptor.capture());
        assertEquals(new BigDecimal("110.00"), partnerCaptor.getValue().getTotalInvestment());
        assertEquals(new BigDecimal("100.00"), partnerCaptor.getValue().getCurrentBalance());

        ArgumentCaptor<CapitalTransactionRequest> txCaptor = ArgumentCaptor.forClass(CapitalTransactionRequest.class);
        verify(capitalTransactionService).createCapitalTransaction(txCaptor.capture());
        assertEquals(CapitalTransactionType.INVESTMENT, txCaptor.getValue().getTransactionType());
        assertEquals(new BigDecimal("100.00"), txCaptor.getValue().getAmount());
        assertEquals(7L, txCaptor.getValue().getPartnerId());
    }
}

