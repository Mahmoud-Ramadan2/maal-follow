package com.mahmoud.maalflow.modules.installments.payment.service;

import com.mahmoud.maalflow.modules.installments.payment.dto.PaymentRequest;
import com.mahmoud.maalflow.modules.installments.payment.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentCommandService {

    private final PaymentService paymentService;

    public PaymentResponse processPayment(PaymentRequest request) {
        return paymentService.processPayment(request);
    }

    public PaymentResponse cancelPayment(Long id, String reason) {
        return paymentService.cancelPayment(id, reason);
    }

    public PaymentResponse refundPayment(Long id, String reason) {
        return paymentService.refundPayment(id, reason);
    }
}

