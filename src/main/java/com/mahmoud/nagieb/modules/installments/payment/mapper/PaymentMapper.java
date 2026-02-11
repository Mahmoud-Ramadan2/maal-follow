package com.mahmoud.nagieb.modules.installments.payment.mapper;

import com.mahmoud.nagieb.modules.installments.payment.dto.PaymentRequest;
import com.mahmoud.nagieb.modules.installments.payment.dto.PaymentResponse;
import com.mahmoud.nagieb.modules.installments.payment.dto.PaymentSummary;
import com.mahmoud.nagieb.modules.installments.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Payment entity and DTOs.
 *
 * @author Mahmoud
 */
@Mapper(componentModel = "spring")
public interface PaymentMapper {

    /**
     * Convert PaymentRequest to Payment entity.
     */
    @Mapping(target = "netAmount",ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "paymentDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "receivedBy", ignore = true)
    @Mapping(target = "collector", ignore = true)
    @Mapping(target = "installmentSchedule", ignore = true)
    Payment toPayment(PaymentRequest request);

    /**
     * Convert Payment entity to PaymentResponse.
     */
    @Mapping(target = "receivedByName", source = "receivedBy.name")
    @Mapping(target = "receivedById", source = "receivedBy.id")
    @Mapping(target = "collectorName", source = "collector.name")
    @Mapping(target = "collectorId", source = "collector.id")
    @Mapping(target = "installmentScheduleId", source = "installmentSchedule.id")
    PaymentResponse toPaymentResponse(Payment payment);

    /**
     * Convert Payment entity to PaymentSummary.
     */

    PaymentSummary toPaymentSummary(Payment payment);
}