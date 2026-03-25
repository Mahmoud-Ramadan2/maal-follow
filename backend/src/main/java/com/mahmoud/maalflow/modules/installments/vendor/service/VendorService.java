package com.mahmoud.maalflow.modules.installments.vendor.service;

import com.mahmoud.maalflow.exception.*;
import com.mahmoud.maalflow.modules.installments.vendor.dto.PurchaseDTO;
import com.mahmoud.maalflow.modules.installments.vendor.dto.VendorRequest;
import com.mahmoud.maalflow.modules.installments.vendor.dto.VendorResponse;
import com.mahmoud.maalflow.modules.installments.vendor.dto.VendorSummary;
import com.mahmoud.maalflow.modules.installments.vendor.entity.Vendor;
import com.mahmoud.maalflow.modules.installments.vendor.mapper.VendorMapper;
import com.mahmoud.maalflow.modules.installments.vendor.repo.VendorRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@AllArgsConstructor
public class VendorService {

    private final VendorRepository vendorRepository;
    private final VendorMapper VendorMapper;
    private final MessageSource messageSource;

    @Transactional
    public  VendorSummary create( VendorRequest request) {
        if(request.getName() != null && vendorRepository.existsByName(request.getName())) {
            throw new DuplicateVendorNameException("validation.name.exists", request.getName());
        }
        if(request.getPhone() != null && vendorRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateVendorPhoneException("validation.phone.exists", request.getPhone());
        }
        Vendor vendor = VendorMapper.toVendor(request);
        vendorRepository.save(vendor);
        return VendorMapper.toVendorSummary(vendor);
    }

    @Transactional
    public  VendorSummary update(Long id, VendorRequest request) {
        Optional<Vendor>  result = vendorRepository.findById(id);
        if (!result.isPresent()) {
            throw new UserNotFoundException("messages.vendor.notFound", id);
        } else {
            Vendor vendor = result.get();
            if (request.getName() != null) {
                vendor.setName(request.getName());
            }
            if (request.getPhone() != null) {
                vendor.setPhone(request.getPhone());
            }
            if (request.getAddress() != null) {
                vendor.setAddress(request.getAddress());
            }
            if (request.getNotes() != null) {
                vendor.setNotes(request.getNotes());
            }
            vendorRepository.save(vendor);
            return VendorMapper.toVendorSummary(vendor);
        }
    }

    public  VendorResponse getById(Long id) {
        Vendor vendor = vendorRepository.findWithPurchase(id)
                .orElseThrow( () -> new UserNotFoundException("messages.vendor.notFound", id));

            List<PurchaseDTO> purchases = vendor.getPurchases().stream()
                    .map(p -> new PurchaseDTO(p.getId(), p.getProductName(), p.getBuyPrice()))
                    .toList();

            //VendorResponse vendorResponse = VendorMapper.toVendorResponse(vendor);
        return new VendorResponse(
                vendor.getId(),
                vendor.getName(),
                vendor.getPhone(),
                vendor.getAddress(),
                vendor.getNotes(),
                vendor.getActive(),
                purchases
        );
    }

    public Page<VendorSummary> list(Pageable pageable, String search, String status) {

         pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("name").descending());
        String searchTerm =  null;
         if (search != null && !search.isBlank()) {
             searchTerm = search.trim();
         }
         if ("all".equalsIgnoreCase(status)) {
             // TODO chack admin
             return vendorRepository.searchVendor(pageable, searchTerm, null)
                     .map(VendorMapper::toVendorSummary);
         } else if ("inactive".equalsIgnoreCase(status)) {
             // TODO chack admin
             return vendorRepository.searchVendor(pageable, searchTerm, false)
                     .map(VendorMapper::toVendorSummary);
         } else {
             Page<Vendor> vendorPage = vendorRepository.searchVendor(pageable, searchTerm, true);
             return  vendorPage.map(VendorMapper::toVendorSummary);

    }
    }
//    public Page<VendorResponse> listWithPurchase(int page, int size, String search) {
//
//        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
//        if (search == null || search.isBlank()) {
//          search = null;
//        }
//            return vendorRepository.findAllByActiveTrueWithPurchase(search, pageable);
//
//    }

    @Transactional
    public String softDelete(Long id) {
        Optional<Vendor> result = vendorRepository.findById(id);
        if (!result.isPresent()) {
            throw new UserNotFoundException("messages.vendor.notFound", id);
        } else {
            Vendor vendor = result.get();
            vendor.setActive(false);
            vendorRepository.save(vendor);
            return messageSource.getMessage("messages.vendor.deleted", new Object[]{vendor.getName()}, LocaleContextHolder.getLocale());
        }
    }
}
