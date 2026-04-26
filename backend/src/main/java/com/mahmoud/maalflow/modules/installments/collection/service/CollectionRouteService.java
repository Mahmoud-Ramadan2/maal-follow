package com.mahmoud.maalflow.modules.installments.collection.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.exception.ObjectNotFoundException;
import com.mahmoud.maalflow.modules.installments.collection.dto.CollectionItemStatusUpdateRequest;
import com.mahmoud.maalflow.modules.installments.collection.dto.CollectionRouteReorderRequest;
import com.mahmoud.maalflow.modules.installments.collection.dto.CollectionRouteUpdateRequest;
import com.mahmoud.maalflow.modules.installments.collection.dto.CollectionSearchRequest;
import com.mahmoud.maalflow.modules.installments.collection.dto.CustomerUnpaidSummaryResponse;
import com.mahmoud.maalflow.modules.installments.collection.entity.CollectionRoute;
import com.mahmoud.maalflow.modules.installments.collection.entity.CollectionRouteItem;
import com.mahmoud.maalflow.modules.installments.collection.enums.CollectionItemStatus;
import com.mahmoud.maalflow.modules.installments.collection.enums.RouteType;
import com.mahmoud.maalflow.modules.installments.collection.repo.CollectionRouteRepository;
import com.mahmoud.maalflow.modules.installments.collection.repo.CollectionRouteItemRepository;
import com.mahmoud.maalflow.modules.installments.customer.entity.Customer;
import com.mahmoud.maalflow.modules.installments.customer.repo.CustomerRepository;
import com.mahmoud.maalflow.modules.shared.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service for managing collection routes (Requirement #6).
 * Groups customers by address or date for optimized payment collection.
 *
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CollectionRouteService {

    private final CollectionRouteRepository routeRepo;
    private final CollectionRouteItemRepository itemRepo;
    private final CustomerRepository customerRepo;
    private final UserRepository userRepository;
    // TODO: Replace with SecurityUtils after security impl

    @Transactional
    public CollectionRoute createRoute(String name, String description, RouteType routeType) {
        CollectionRoute route = new CollectionRoute();
        route.setName(name);
        route.setDescription(description);
        route.setRouteType(routeType);
        route.setIsActive(true);
        route.setCreatedBy(userRepository.findById(1L).orElse(null)); // TODO: securityUtils.getCurrentUser()

        CollectionRoute saved = routeRepo.save(route);
        saved.setRouteItems(new ArrayList<>());
        log.info("Created collection route: {} ({})", saved.getName(), saved.getRouteType());
        return saved;
    }

    @Transactional(readOnly = true)
    public CollectionRoute getRouteById(Long routeId) {
        CollectionRoute route = routeRepo.findById(routeId)
                .orElseThrow(() -> new ObjectNotFoundException("Route not found", routeId));
        route.setRouteItems(itemRepo.findByCollectionRouteIdAndIsActiveTrueOrderBySequenceOrderAsc(routeId));
        return route;
    }

    @Transactional
    public CollectionRoute updateRoute(Long routeId, CollectionRouteUpdateRequest request) {
        CollectionRoute route = routeRepo.findById(routeId)
                .orElseThrow(() -> new ObjectNotFoundException("Route not found", routeId));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            route.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            route.setDescription(request.getDescription().trim());
        }
        if (request.getRouteType() != null) {
            route.setRouteType(request.getRouteType());
        }

        CollectionRoute saved = routeRepo.save(route);
        saved.setRouteItems(itemRepo.findByCollectionRouteIdAndIsActiveTrueOrderBySequenceOrderAsc(routeId));
        return saved;
    }

    @Transactional
    public CollectionRouteItem addCustomerToRoute(Long routeId, Long customerId, Integer sequenceOrder, String notes) {
        CollectionRoute route = routeRepo.findById(routeId)
                .orElseThrow(() -> new ObjectNotFoundException("Route not found", routeId));
        if (!Boolean.TRUE.equals(route.getIsActive())) {
            throw new BusinessException("collection.route.inactive");
        }

        if (itemRepo.existsByCollectionRouteIdAndCustomerIdAndIsActiveTrue(routeId, customerId)) {
            throw new BusinessException("collection.route.customer.duplicate");
        }

        Customer customer = customerRepo.findByIdAndActiveTrue(customerId)
                .orElseThrow(() -> new ObjectNotFoundException("Customer not found", customerId));

        CollectionRouteItem item = new CollectionRouteItem();
        item.setCollectionRoute(route);
        item.setCustomer(customer);
        item.setSequenceOrder(sequenceOrder != null ? sequenceOrder : getNextSequence(routeId));
        item.setNotes(notes);
        item.setIsActive(true);
        item.setCollectionStatus(CollectionItemStatus.PENDING);

        CollectionRouteItem saved = itemRepo.save(item);
        log.info("Added customer {} to route {} at position {}", customerId, routeId, saved.getSequenceOrder());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<CollectionRoute> getActiveRoutes() {
        List<CollectionRoute> routes = routeRepo.findByIsActiveTrueOrderByNameAsc();
        routes.forEach(route -> route.setRouteItems(
                itemRepo.findByCollectionRouteIdAndIsActiveTrueOrderBySequenceOrderAsc(route.getId())
        ));
        return routes;
    }

    @Transactional(readOnly = true)
    public List<CollectionRouteItem> getRouteItems(Long routeId) {
        return itemRepo.findByCollectionRouteIdAndIsActiveTrueOrderBySequenceOrderAsc(routeId);
    }

    @Transactional
    public void removeCustomerFromRoute(Long itemId) {
        CollectionRouteItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new ObjectNotFoundException("Route item not found", itemId));
        item.setIsActive(false);
        itemRepo.save(item);
        log.info("Removed item {} from route", itemId);
    }

    @Transactional
    public void deactivateRoute(Long routeId) {
        CollectionRoute route = routeRepo.findById(routeId)
                .orElseThrow(() -> new ObjectNotFoundException("Route not found", routeId));
        route.setIsActive(false);
        routeRepo.save(route);

        List<CollectionRouteItem> items = itemRepo.findByCollectionRouteIdAndIsActiveTrueOrderBySequenceOrderAsc(routeId);
        for (CollectionRouteItem item : items) {
            item.setIsActive(false);
        }
        itemRepo.saveAll(items);

        log.info("Deactivated route {}", routeId);
    }

    @Transactional
    public CollectionRoute reorderRouteItems(Long routeId, CollectionRouteReorderRequest request) {
        if (request.getItemIds() == null || request.getItemIds().isEmpty()) {
            throw new BusinessException("collection.route.reorder.empty");
        }

        CollectionRoute route = routeRepo.findById(routeId)
                .orElseThrow(() -> new ObjectNotFoundException("Route not found", routeId));

        List<CollectionRouteItem> activeItems =
                itemRepo.findByCollectionRouteIdAndIsActiveTrueOrderBySequenceOrderAsc(routeId);

        if (activeItems.size() != request.getItemIds().size()) {
            throw new BusinessException("collection.route.reorder.mismatch");
        }

        Set<Long> expectedIds = new HashSet<>();
        for (CollectionRouteItem item : activeItems) {
            expectedIds.add(item.getId());
        }
        Set<Long> requestedIds = new HashSet<>(request.getItemIds());
        if (!expectedIds.equals(requestedIds)) {
            throw new BusinessException("collection.route.reorder.invalidItems");
        }

        List<CollectionRouteItem> orderedItems = itemRepo.findByCollectionRouteIdAndIdIn(routeId, request.getItemIds());
        for (CollectionRouteItem item : orderedItems) {
            int nextIndex = request.getItemIds().indexOf(item.getId());
            item.setSequenceOrder(nextIndex + 1);
        }
        itemRepo.saveAll(orderedItems);

        route.setRouteItems(itemRepo.findByCollectionRouteIdAndIsActiveTrueOrderBySequenceOrderAsc(routeId));
        return route;
    }

    @Transactional
    public CollectionRouteItem updateItemStatus(Long itemId, CollectionItemStatusUpdateRequest request) {
        CollectionRouteItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new ObjectNotFoundException("Route item not found", itemId));

        CollectionItemStatus newStatus = request.getStatus() == null
                ? CollectionItemStatus.PENDING
                : request.getStatus();

        item.setCollectionStatus(newStatus);
        item.setNotes(request.getNotes());
        if (newStatus == CollectionItemStatus.COLLECTED) {
            item.setCollectedAmount(request.getCollectedAmount() == null ? BigDecimal.ZERO : request.getCollectedAmount());
        } else {
            item.setCollectedAmount(request.getCollectedAmount());
        }

        return itemRepo.save(item);
    }

    @Transactional(readOnly = true)
    public Page<CustomerUnpaidSummaryResponse> searchUncollectedCustomers(CollectionSearchRequest request) {
        int page = request.getPage() == null ? 0 : Math.max(request.getPage(), 0);
        int size = request.getSize() == null ? 20 : Math.min(Math.max(request.getSize(), 1), 200);

        Page<Customer> resultPage = customerRepo.searchEligibleForCollectionRoute(
                PageRequest.of(page, size),
                request.getSearchTerm(),
                request.getAddress()
        );

        List<CustomerUnpaidSummaryResponse> content = resultPage.getContent().stream().map(customer ->
                CustomerUnpaidSummaryResponse.builder()
                        .id(customer.getId())
                        .name(customer.getName())
                        .phone(customer.getPhone())
                        .address(customer.getAddress())
                        .outstandingAmount(BigDecimal.ZERO)
                        .build()
        ).toList();

        return new PageImpl<>(content, resultPage.getPageable(), resultPage.getTotalElements());
    }

    private Integer getNextSequence(Long routeId) {
        Integer max = itemRepo.findMaxSequenceOrderByRouteId(routeId);
        return max == null ? 1 : max + 1;
    }
}

