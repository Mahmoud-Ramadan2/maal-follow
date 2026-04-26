package com.mahmoud.maalflow.modules.installments.purchase.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.exception.ObjectNotFoundException;
import com.mahmoud.maalflow.exception.UserNotFoundException;
import com.mahmoud.maalflow.modules.installments.contract.repo.ContractRepository;
import com.mahmoud.maalflow.modules.installments.purchase.dto.PurchaseFilter;
import com.mahmoud.maalflow.modules.installments.purchase.dto.PurchaseRequest;
import com.mahmoud.maalflow.modules.installments.purchase.dto.PurchaseResponse;
import com.mahmoud.maalflow.modules.installments.purchase.dto.PurchaseStatistics;
import com.mahmoud.maalflow.modules.installments.purchase.entity.Purchase;
import com.mahmoud.maalflow.modules.installments.purchase.mapper.PurchaseMapper;
import com.mahmoud.maalflow.modules.installments.purchase.repo.PurchaseRepository;
import com.mahmoud.maalflow.modules.installments.vendor.entity.Vendor;
import com.mahmoud.maalflow.modules.installments.vendor.repo.VendorRepository;
import com.mahmoud.maalflow.modules.shared.user.repo.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.hibernate.internal.util.collections.ArrayHelper.forEach;

@Slf4j
@AllArgsConstructor
@Service
public class PurchaseService {
    private final PurchaseRepository purchaseRepository;
    private final VendorRepository vendorRepository;
    // TODO: Replace with authenticated user service
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
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

        if (request.getBuyPrice() != null && existingPurchase.getBuyPrice().compareTo(request.getBuyPrice()) != 0) {
            boolean hasContracts = contractRepository.existsContractByPurchaseId(id);
            if (hasContracts) {
                throw new BusinessException("messages.purchase.updatePriceWithContracts");
            }
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
//    public Page<PurchaseResponse> list(int page, int size, String sort, String search) {
//
//        String[] allowedSortFields = {"id", "productName", "buyPrice", "purchaseDate", "vendorName"};
//        String[] allowedSortDirections = {"asc", "desc"};
//        String[] sortParts = sort.split(",");
//
//        Sort.Direction direction = Sort.Direction.fromString("desc"); // Default to descending
//        Sort sorting = Sort.by(direction, "purchaseDate"); // Default sort by purchaseDate
//
//        if (isAllowedSortField(sortParts[1], allowedSortDirections)) {
//             direction = Sort.Direction.fromString(sortParts[1]);
//        } else {
//            log.warn("Invalid sort direction '{}', defaulting to 'desc'", sortParts[1]);
//            //throw new IllegalArgumentException("validation.invalidSortField");
//        }
//        if (isAllowedSortField(sortParts[0], allowedSortFields)) {
//             sorting = Sort.by(direction, sortParts[0]);
//        }else {
//            log.warn("Invalid sort field '{}', defaulting to 'purchaseDate'", sortParts[0]);
////            throw new IllegalArgumentException("validation.invalidSortField");
//        }
//        Pageable pageable = PageRequest.of(page, size, sorting)   ;
//        if (search == null || search.isBlank()) {
//            search = null;
//        }
//            return purchaseRepository.findAllByProductNameContainingIgnoreCase(search, pageable);
//    }

    /**
     * 3/5/2026
     * its new version of list method that accepts pageable and filter object
     * @param pageable
     * @param filter
     * @return page of purchase response
     */
    public Page<PurchaseResponse> list(Pageable pageable, PurchaseFilter filter) {

        //  spring auto generate pageRequest from query params and we can use it directly in repository method
        // Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        String searchTerm = filter.getSearchTerm();
        if (filter.getSearchTerm() == null || filter.getSearchTerm().isBlank()) {
        searchTerm = null;
    }
        return purchaseRepository.searchPurchases(
                filter.getVendorId(),
                filter.getStartDate(),
                filter.getEndDate(),
                searchTerm,
                pageable
        );
}


    @Transactional
    public String softDelete(Long id) {
        Optional<Purchase> result = purchaseRepository.findById(id);
        if (!result.isPresent()) {
            throw new ObjectNotFoundException("messages.purchase.notFound", id);
        } else {
            if (contractRepository.existsContractByPurchaseId(id)) {
                throw new BusinessException("messages.purchase.deleteWithContracts");
            }
            Purchase purchase = result.get();
            String name = purchase.getProductName();
//            purchase.setActive(false);
            purchaseRepository.delete(purchase);
            return messageSource.getMessage("messages.purchase.deleted", new Object[]{name}, LocaleContextHolder.getLocale());
        }
    }

// service/PurchaseService.java

    public PurchaseStatistics getStatistics() {
        long totalCount = purchaseRepository.countAllPurchases();
        BigDecimal totalAmount = purchaseRepository.sumAllBuyPrices();

        BigDecimal avgAmount = totalCount > 0
                ? totalAmount.divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<String, Long> countByVendor = new LinkedHashMap<>();
        purchaseRepository.countGroupedByVendor()
                .forEach(row -> countByVendor.put((String) row[0], (Long) row[1]));

        Map<String, BigDecimal> amountByVendor = new LinkedHashMap<>();
        purchaseRepository.sumGroupedByVendor()
                .forEach(row -> amountByVendor.put((String) row[0], (BigDecimal) row[1]));

        return PurchaseStatistics.builder()
                .totalCount(totalCount)
                .totalAmount(totalAmount)
                .avgAmount(avgAmount)
                .countByVendor(countByVendor)
                .amountByVendor(amountByVendor)
                .build();
    }

}
