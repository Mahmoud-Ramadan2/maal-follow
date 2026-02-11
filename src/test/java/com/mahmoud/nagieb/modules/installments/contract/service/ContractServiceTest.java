package com.mahmoud.nagieb.modules.installments.contract.service;

import com.mahmoud.nagieb.exception.BusinessException;
import com.mahmoud.nagieb.exception.ObjectNotFoundException;
import com.mahmoud.nagieb.exception.UserNotFoundException;
import com.mahmoud.nagieb.modules.installments.contract.dto.ContractRequest;
import com.mahmoud.nagieb.modules.installments.contract.dto.ContractResponse;
import com.mahmoud.nagieb.modules.installments.contract.entity.Contract;
import com.mahmoud.nagieb.modules.installments.contract.mapper.ContractMapper;
import com.mahmoud.nagieb.modules.installments.contract.repo.ContractRepository;
import com.mahmoud.nagieb.modules.installments.customer.entity.Customer;
import com.mahmoud.nagieb.modules.installments.customer.repo.CustomerRepository;
import com.mahmoud.nagieb.modules.installments.purchase.entity.Purchase;
import com.mahmoud.nagieb.modules.installments.purchase.repo.PurchaseRepository;
import com.mahmoud.nagieb.modules.installments.contract.enums.ContractStatus;
import com.mahmoud.nagieb.modules.shared.user.entity.User;
import com.mahmoud.nagieb.modules.shared.user.repo.UserRepository;
import org.hibernate.mapping.Any;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PurchaseRepository purchaseRepository;
    @Mock
    private ContractRepository contractRepository;
    @Mock
    private ContractMapper contractMapper;

    @InjectMocks
    private ContractService contractService;

    @Captor
    private ArgumentCaptor<Contract> contractCaptor;

    private ContractRequest validRequest;
    private Customer existingCustomer;
    private User existingUser;
    private Purchase existingPurchase;

    @BeforeEach
    void setUp() {
        validRequest = new ContractRequest();
        validRequest.setFinalPrice(BigDecimal.valueOf(3000.00));
        validRequest.setDownPayment(BigDecimal.valueOf(500.00));
        validRequest.setMonths(12);
        validRequest.setStartDate(LocalDate.now().minusDays(1));
        validRequest.setCustomerId(1L);
        validRequest.setPurchaseId(2L);
        validRequest.setAdditionalCosts(BigDecimal.valueOf(100.0));
        validRequest.setResponsibleUserId(1L);
        validRequest.setNotes("Initial");

        existingCustomer = new Customer();
        existingCustomer.setId(1L);

        existingUser = new User();
        existingUser.setId(1L);
        existingPurchase = new Purchase();
        existingPurchase.setId(2L);
        existingPurchase.setBuyPrice(BigDecimal.valueOf(2000.00));
    }

    @Test
    void create_successful() {

        when(contractRepository.existsByPurchaseIdAndStatusAndCustomerId(validRequest.getPurchaseId(), ContractStatus.ACTIVE, validRequest.getCustomerId()))
                .thenReturn(false);
        when(customerRepository.findByIdAndActiveTrue(validRequest.getCustomerId()))
                .thenReturn(Optional.of(existingCustomer));
        when(purchaseRepository.findById(validRequest.getPurchaseId()))
                .thenReturn(Optional.of(existingPurchase));
        when(userRepository.findById(existingUser.getId()))
                .thenReturn(Optional.of(existingUser));

        Contract saved = new Contract();

        when(contractMapper.toContract(validRequest)).thenReturn(saved);

        saved.setId(10L);
        saved.setFinalPrice(validRequest.getFinalPrice());
        saved.setDownPayment(validRequest.getDownPayment());
        saved.setMonths(validRequest.getMonths());
        saved.setAdditionalCosts(validRequest.getAdditionalCosts());

//        when(contractRepository.save(any(Contract.class))).thenReturn(saved);
        when(contractRepository.save(contractCaptor.capture())).thenReturn(saved);

        ContractResponse resp = new ContractResponse();
        resp.setStatus(ContractStatus.ACTIVE);
        when(contractMapper.toContractResponse(saved)).thenReturn(resp);

        ContractResponse result = contractService.create(validRequest);

        assertNotNull(result);
        assertEquals(ContractStatus.ACTIVE, result.getStatus());
        verify(contractRepository).save(contractCaptor.capture());
        Contract captured = contractCaptor.getValue();
        assertEquals(validRequest.getFinalPrice(), captured.getFinalPrice());
        assertEquals(validRequest.getDownPayment(), captured.getDownPayment());
        assertEquals(BigDecimal.valueOf(2500.00).setScale(2), captured.getRemainingAmount().setScale(2));
        // monthly = remaining / months
        assertEquals(0, captured.getMonthlyAmount().compareTo(
                BigDecimal.valueOf(2500.00).divide(BigDecimal.valueOf(12), 2, java.math.RoundingMode.HALF_UP)));
        verify(contractMapper, times(1)).toContract(validRequest);
        verify(contractMapper, times(1)).toContractResponse(saved);
    }

    @Test
    void create_duplicateActiveContract_throwsBusinessException() {

        when(contractRepository.existsByPurchaseIdAndStatusAndCustomerId(validRequest.getPurchaseId(), ContractStatus.ACTIVE, validRequest.getCustomerId()))
                .thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> contractService.create(validRequest));

        assertNotNull(ex);
        verifyNoInteractions(contractMapper);
        verify(customerRepository, never()).save(any());
    }

    @Test
    void create_customerNotFound_throwsUserNotFoundException() {
        when(contractRepository.existsByPurchaseIdAndStatusAndCustomerId(validRequest.getPurchaseId(), ContractStatus.ACTIVE, validRequest.getCustomerId()))
                .thenReturn(false);
        when(customerRepository.findByIdAndActiveTrue(validRequest.getCustomerId()))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> contractService.create(validRequest));
        verifyNoInteractions(purchaseRepository);
    }

    @Test
    void create_purchaseNotFound_throwsObjectNotFoundException() {
        when(contractRepository.existsByPurchaseIdAndStatusAndCustomerId(validRequest.getPurchaseId(), ContractStatus.ACTIVE, validRequest.getCustomerId()))
                .thenReturn(false);
        when(customerRepository.findByIdAndActiveTrue(validRequest.getCustomerId()))
                .thenReturn(Optional.of(existingCustomer));
        when(purchaseRepository.findById(validRequest.getPurchaseId()))
                .thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> contractService.create(validRequest));
    }


    @Test
    void update_successful_changesAndRecalculates() {
        Long contractId = 5L;
        Contract existing = new Contract();
        existing.setId(contractId);
        existing.setCustomer(existingCustomer);
        existing.setPurchase(existingPurchase);
        existing.setFinalPrice(BigDecimal.valueOf(2000));
        existing.setDownPayment(BigDecimal.valueOf(200));
        existing.setMonths(10);
        existing.setRemainingAmount(BigDecimal.valueOf(1800));
        existing.setMonthlyAmount(BigDecimal.valueOf(180));

        when(contractRepository.findById(contractId)).thenReturn(Optional.of(existing));

        ContractRequest updateReq = new ContractRequest();
        updateReq.setCustomerId(11L);
        updateReq.setPurchaseId(22L);
        updateReq.setMonths(12);
        updateReq.setFinalPrice(BigDecimal.valueOf(2400));
        updateReq.setDownPayment(BigDecimal.valueOf(400));
        updateReq.setStartDate(LocalDate.now());
        updateReq.setAdditionalCosts(BigDecimal.valueOf(200));
        updateReq.setAgreedPaymentDay(Integer.valueOf(5));
        updateReq.setNotes("updated");

        Customer newCustomer = new Customer();
        newCustomer.setId(11L);
        Purchase newPurchase = new Purchase();
        newPurchase.setId(22L);
        newPurchase.setBuyPrice(BigDecimal.valueOf(200));

        when(customerRepository.findByIdAndActiveTrue(11L)).thenReturn(Optional.of(newCustomer));
        when(purchaseRepository.findById(22L)).thenReturn(Optional.of(newPurchase));
        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));

        Contract saved = new Contract();
        saved.setId(contractId);
        ContractResponse resp = new ContractResponse();

        when(contractRepository.save(contractCaptor.capture())).thenReturn(saved);
        when(contractMapper.toContractResponse(saved)).thenReturn(any());
        ContractResponse result = contractService.update(contractId, updateReq);

        verify(contractRepository).save(contractCaptor.capture());
        Contract captured = contractCaptor.getValue();
        assertEquals(newCustomer.getId(), captured.getCustomer().getId());
        assertEquals(newPurchase.getId(), captured.getPurchase().getId());
        // remaining = 2400 - 400  = 2000
        assertEquals(0, captured.getRemainingAmount().compareTo(BigDecimal.valueOf(2000)));
        // monthly = 2000 / 12 rounded to 2 decimals
        assertEquals(0, captured.getMonthlyAmount().compareTo(
                BigDecimal.valueOf(2000).divide(BigDecimal.valueOf(12), 2, java.math.RoundingMode.HALF_UP)));
    }

    @Test
    void update_notFound_throwsObjectNotFoundException() {
        when(contractRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ObjectNotFoundException.class, () -> contractService.update(99L, new ContractRequest()));
        verify(contractRepository,  never()).save(any());
    }

    @Test
    void getCustomerWithContracts_successful() {
        Long customerId = 1L;
        when(customerRepository.existsByIdAndActiveTrue(customerId)).thenReturn(true);

        ContractResponse cr1 = new ContractResponse();
        ContractResponse cr2 = new ContractResponse();
        Page<ContractResponse> page = new PageImpl<>(List.of(cr1, cr2));
        when(contractRepository.findByCustomerId(eq(customerId), any(Pageable.class))).thenReturn(page);

        Page<ContractResponse> result = contractService.getCustomerWithContracts(customerId, 0, 10);
        assertEquals(2, result.getTotalElements());
        verify(contractRepository).findByCustomerId(eq(customerId), any(Pageable.class));
    }

    @Test
    void getCustomerWithContracts_customerNotFound_throwsUserNotFoundException() {
        Long customerId = 1L;
        when(customerRepository.existsByIdAndActiveTrue(customerId)).thenReturn(false);
        assertThrows(UserNotFoundException.class, () -> contractService.getCustomerWithContracts(customerId, 0, 10));
        verifyNoInteractions(contractRepository);
    }
}
