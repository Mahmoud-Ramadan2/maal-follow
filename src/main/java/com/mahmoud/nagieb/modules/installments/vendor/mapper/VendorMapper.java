package com.mahmoud.nagieb.modules.installments.vendor.mapper;

import com.mahmoud.nagieb.modules.installments.vendor.dto.VendorRequest;
import com.mahmoud.nagieb.modules.installments.vendor.dto.VendorResponse;
import com.mahmoud.nagieb.modules.installments.vendor.dto.VendorSummary;
import com.mahmoud.nagieb.modules.installments.vendor.entity.Vendor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VendorMapper {

    @Mapping(target = "purchases", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Vendor toVendor(VendorRequest request);

    VendorResponse toVendorResponse(Vendor vendor);

    VendorSummary toVendorSummary(Vendor vendor);
}
