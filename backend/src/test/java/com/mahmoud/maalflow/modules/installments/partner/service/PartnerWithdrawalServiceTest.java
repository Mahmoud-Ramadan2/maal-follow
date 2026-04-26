package com.mahmoud.maalflow.modules.installments.partner.service;

import com.mahmoud.maalflow.modules.installments.capital.dto.CapitalTransactionRequest;
import com.mahmoud.maalflow.modules.installments.capital.entity.CapitalPool;
import com.mahmoud.maalflow.modules.installments.capital.enums.CapitalTransactionType;
import com.mahmoud.maalflow.modules.installments.capital.service.CapitalTransactionService;
import com.mahmoud.maalflow.modules.installments.ledger.service.LedgerService;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerWithdrawalResponse;
import com.mahmoud.maalflow.modules.installments.partner.entity.Partner;
import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerWithdrawal;
import com.mahmoud.maalflow.modules.installments.partner.enums.WithdrawalStatus;
import com.mahmoud.maalflow.modules.installments.partner.mapper.PartnerWithdrawalMapper;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerRepository;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerWithdrawalRepository;
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
class PartnerWithdrawalServiceTest {

    @Mock
    private PartnerWithdrawalRepository withdrawalRepository;

    @Mock
    private PartnerRepository partnerRepository;

    @Mock
    private PartnerWithdrawalMapper withdrawalMapper;

    @Mock
    private LedgerService ledgerService;

    @Mock
    private CapitalTransactionService capitalTransactionService;

    @Mock
    private PartnerService partnerService;

    @InjectMocks
    private PartnerWithdrawalService partnerWithdrawalService;

    @Test
    void processWithdrawal_recordsCapitalTransaction_and_updatesPartnerTotals() {
        Partner partner = new Partner();
        partner.setId(9L);
        partner.setCurrentBalance(new BigDecimal("200.00"));
        partner.setTotalWithdrawals(new BigDecimal("20.00"));

        PartnerWithdrawal withdrawal = new PartnerWithdrawal();
        withdrawal.setId(15L);
        withdrawal.setPartner(partner);
        withdrawal.setAmount(new BigDecimal("50.00"));
        withdrawal.setStatus(WithdrawalStatus.APPROVED);

        when(withdrawalRepository.findById(15L)).thenReturn(Optional.of(withdrawal));
        when(partnerRepository.findById(9L)).thenReturn(Optional.of(partner));
        when(withdrawalRepository.save(any(PartnerWithdrawal.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(withdrawalMapper.toPartnerWithdrawalResponse(any(PartnerWithdrawal.class)))
                .thenReturn(new PartnerWithdrawalResponse());
        CapitalPool pool = new CapitalPool();
        pool.setTotalAmount(new BigDecimal("600.00"));
        when(capitalTransactionService.getPoolForUpdate()).thenReturn(pool);

        partnerWithdrawalService.processWithdrawal(15L);

        ArgumentCaptor<Partner> partnerCaptor = ArgumentCaptor.forClass(Partner.class);
        verify(partnerRepository).save(partnerCaptor.capture());
        assertEquals(new BigDecimal("70.00"), partnerCaptor.getValue().getTotalWithdrawals());
        assertEquals(new BigDecimal("150.00"), partnerCaptor.getValue().getCurrentBalance());

        ArgumentCaptor<CapitalTransactionRequest> txCaptor = ArgumentCaptor.forClass(CapitalTransactionRequest.class);
        verify(capitalTransactionService).createCapitalTransaction(txCaptor.capture());
        assertEquals(CapitalTransactionType.WITHDRAWAL, txCaptor.getValue().getTransactionType());
        assertEquals(new BigDecimal("50.00"), txCaptor.getValue().getAmount());
        assertEquals(9L, txCaptor.getValue().getPartnerId());
    }
}

