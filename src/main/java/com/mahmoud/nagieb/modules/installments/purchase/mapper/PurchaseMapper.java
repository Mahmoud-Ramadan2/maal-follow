package com.mahmoud.nagieb.modules.installments.purchase.mapper;

import com.mahmoud.nagieb.modules.installments.purchase.dto.PurchaseRequest;
import com.mahmoud.nagieb.modules.installments.purchase.dto.PurchaseResponse;
import com.mahmoud.nagieb.modules.installments.purchase.entity.Purchase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PurchaseMapper {

    @Mapping(target = "vendor", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Purchase toPurchase(PurchaseRequest request);
    // TODO: Check LazyException
    @Mapping(target = "vendorName",  expression = "java(purchase.getVendor().getName())")
    PurchaseResponse toPurchaseResponse(Purchase purchase);
}
