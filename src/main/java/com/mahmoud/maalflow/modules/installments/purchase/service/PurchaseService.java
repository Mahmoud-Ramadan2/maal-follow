package com.mahmoud.maalflow.modules.installments.purchase.service;

import com.mahmoud.maalflow.exception.ObjectNotFoundException;
import com.mahmoud.maalflow.exception.UserNotFoundException;
import com.mahmoud.maalflow.modules.installments.purchase.dto.PurchaseRequest;
import com.mahmoud.maalflow.modules.installments.purchase.dto.PurchaseResponse;
import com.mahmoud.maalflow.modules.installments.purchase.entity.Purchase;
import com.mahmoud.maalflow.modules.installments.purchase.mapper.PurchaseMapper;
import com.mahmoud.maalflow.modules.installments.purchase.repo.PurchaseRepository;
import com.mahmoud.maalflow.modules.installments.vendor.entity.Vendor;
import com.mahmoud.maalflow.modules.installments.vendor.repo.VendorRepository;
import com.mahmoud.maalflow.modules.shared.user.repo.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@AllArgsConstructor
@Service
public class PurchaseService {
    private final PurchaseRepository purchaseRepository;
    private final VendorRepository vendorRepository;
    // TODO: Replace with authenticated user service
    private final UserRepository userRepository;
    private final MessageSource messageSource;
    private final PurchaseMapper purchaseMapper;


    @Transactional
    public PurchaseResponse create(PurchaseRequest request) {

            Vendor vendor = vendorRepository.findById(request.getVendorId())
                    .orElseThrow(() -> new UserNotFoundException("messages.vendor.notFound", request.getVendorId()));
            Purchase purchase = purchaseMapper.toPurchase(request);
        purchase.setVendor(vendor);
        // TODO: Replace with authenticated user
         // Temporary hardcoded user with ID 1
        purchase.setCreatedBy(userRepository.findById(1L).orElse(null));

            Purchase savedPurchase = purchaseRepository.save(purchase);

            return purchaseMapper.toPurchaseResponse(savedPurchase);
    }

    @Transactional
    public PurchaseResponse update(Long id, PurchaseRequest request) {

        Purchase existingPurchase = purchaseRepository.findById(id)
                .orElseThrow( () -> new ObjectNotFoundException("messages.purchase.notFound", id));
        if (request.getVendorId() != null &&
                (existingPurchase.getVendor() == null ||
                        !existingPurchase.getVendor().getId().equals(request.getVendorId()))) {
            boolean vendorExists = vendorRepository.existsByIdAndActiveTrue(request.getVendorId());
            if (!vendorExists) {
                throw new UserNotFoundException("messages.vendor.notFound", request.getVendorId());
            }
            Vendor newVendor = vendorRepository.findById(request.getVendorId())
                    .orElseThrow(() -> new UserNotFoundException("messages.vendor.notFound", request.getVendorId()));
            existingPurchase.setVendor(newVendor);
        }
        if (request.getProductName() != null) {
            existingPurchase.setProductName(request.getProductName());
        }

        if (request.getBuyPrice() != null) {
            existingPurchase.setBuyPrice(request.getBuyPrice());
        }

        if (request.getPurchaseDate() != null) {
            existingPurchase.setPurchaseDate(request.getPurchaseDate());
        }

        if (request.getNotes() != null) {
            existingPurchase.setNotes(request.getNotes());
        }
            Purchase savedPurchase = purchaseRepository.save(existingPurchase);
            return purchaseMapper.toPurchaseResponse(savedPurchase);

    }

    public PurchaseResponse getById(Long id) {

        // Use findPurchaseResponse to avoid LazInitializationException
        return purchaseRepository.findPurchaseResponse(id)
                .orElseThrow( () -> new ObjectNotFoundException("messages.purchase.notFound", id) );

    }
    public Page<PurchaseResponse> list(int page, int size, String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("productName"));
        if (search == null || search.isBlank()) {
            search = null;
        }
            return purchaseRepository.findAllByProductNameContainingIgnoreCase(search, pageable);
    }

    @Transactional
    public String softDelete(Long id) {
        Optional<Purchase> result = purchaseRepository.findById(id);
        if (!result.isPresent()) {
            throw new ObjectNotFoundException("messages.purchase.notFound", id);
        } else {
            Purchase purchase = result.get();
            String name = purchase.getProductName();
//            purchase.setActive(false);
            purchaseRepository.delete(purchase);
            return messageSource.getMessage("messages.purchase.deleted", new Object[]{name}, LocaleContextHolder.getLocale());
        }
    }
}
