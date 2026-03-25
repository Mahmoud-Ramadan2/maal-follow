package com.mahmoud.maalflow.modules.installments.schedule.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.exception.ObjectNotFoundException;
import com.mahmoud.maalflow.modules.installments.schedule.entity.CollectionRoute;
import com.mahmoud.maalflow.modules.installments.schedule.entity.CollectionRouteItem;
import com.mahmoud.maalflow.modules.installments.schedule.repo.CollectionRouteRepository;
import com.mahmoud.maalflow.modules.installments.schedule.repo.CollectionRouteItemRepository;
import com.mahmoud.maalflow.modules.installments.customer.entity.Customer;
import com.mahmoud.maalflow.modules.installments.customer.repo.CustomerRepository;
import com.mahmoud.maalflow.modules.shared.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing collection routes (Requirement #6).
 * Groups customers by address or date for optimized payment collection.
 *
 * NOTE: Requires CollectionRouteRepository and CollectionRouteItemRepository
 * to exist in schedule/repo/ (they should already be there based on entities).
 *
 * Copy to: src/main/java/com/mahmoud/maalflow/modules/installments/schedule/service/
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
    public CollectionRoute createRoute(String name, String description,
                                       com.mahmoud.maalflow.modules.installments.schedule.enums.RouteType routeType) {
        CollectionRoute route = new CollectionRoute();
        route.setName(name);
        route.setDescription(description);
        route.setRouteType(routeType);
        route.setIsActive(true);
        route.setCreatedBy(userRepository.findById(1L).orElse(null)); // TODO: securityUtils.getCurrentUser()

        CollectionRoute saved = routeRepo.save(route);
        log.info("Created collection route: {} ({})", saved.getName(), saved.getRouteType());
        return saved;
    }

    @Transactional
    public CollectionRouteItem addCustomerToRoute(Long routeId, Long customerId, Integer sequenceOrder, String notes) {
        CollectionRoute route = routeRepo.findById(routeId)
                .orElseThrow(() -> new ObjectNotFoundException("Route not found", routeId));
        Customer customer = customerRepo.findByIdAndActiveTrue(customerId)
                .orElseThrow(() -> new ObjectNotFoundException("Customer not found", customerId));

        CollectionRouteItem item = new CollectionRouteItem();
        item.setCollectionRoute(route);
        item.setCustomer(customer);
        item.setSequenceOrder(sequenceOrder != null ? sequenceOrder : getNextSequence(routeId));
        item.setNotes(notes);
        item.setIsActive(true);

        CollectionRouteItem saved = itemRepo.save(item);
        log.info("Added customer {} to route {} at position {}", customerId, routeId, saved.getSequenceOrder());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<CollectionRoute> getActiveRoutes() {
        return routeRepo.findByIsActiveTrueOrderByNameAsc();
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
        log.info("Deactivated route {}", routeId);
    }

    private Integer getNextSequence(Long routeId) {
        List<CollectionRouteItem> items = itemRepo.findByCollectionRouteIdAndIsActiveTrueOrderBySequenceOrderAsc(routeId);
        return items.isEmpty() ? 1 : items.get(items.size() - 1).getSequenceOrder() + 1;
    }
}

