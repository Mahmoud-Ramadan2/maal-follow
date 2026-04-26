package com.mahmoud.maalflow.modules.installments.collection.controller;

import com.mahmoud.maalflow.modules.installments.collection.dto.CollectionItemStatusUpdateRequest;
import com.mahmoud.maalflow.modules.installments.collection.dto.CollectionRouteReorderRequest;
import com.mahmoud.maalflow.modules.installments.collection.dto.CollectionRouteUpdateRequest;
import com.mahmoud.maalflow.modules.installments.collection.dto.CollectionSearchRequest;
import com.mahmoud.maalflow.modules.installments.collection.dto.CustomerUnpaidSummaryResponse;
import com.mahmoud.maalflow.modules.installments.collection.entity.CollectionRoute;
import com.mahmoud.maalflow.modules.installments.collection.entity.CollectionRouteItem;
import com.mahmoud.maalflow.modules.installments.collection.enums.RouteType;
import com.mahmoud.maalflow.modules.installments.collection.service.CollectionRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Collection Route REST Controller (Requirement #6).
 */
@RestController
@RequestMapping("/api/v1/collection-routes")
@RequiredArgsConstructor
public class CollectionRouteController {

    private final CollectionRouteService service;

    @PostMapping
    public ResponseEntity<CollectionRoute> createRoute(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam RouteType routeType) {
        return ResponseEntity.status(201).body(service.createRoute(name, description, routeType));
    }

    @GetMapping
    public ResponseEntity<List<CollectionRoute>> getActiveRoutes() {
        return ResponseEntity.ok(service.getActiveRoutes());
    }

    @GetMapping("/{routeId}")
    public ResponseEntity<CollectionRoute> getRouteById(@PathVariable Long routeId) {
        return ResponseEntity.ok(service.getRouteById(routeId));
    }

    @PutMapping("/{routeId}")
    public ResponseEntity<CollectionRoute> updateRoute(
            @PathVariable Long routeId,
            @RequestBody CollectionRouteUpdateRequest request) {
        return ResponseEntity.ok(service.updateRoute(routeId, request));
    }

    @GetMapping("/{routeId}/items")
    public ResponseEntity<List<CollectionRouteItem>> getRouteItems(@PathVariable Long routeId) {
        return ResponseEntity.ok(service.getRouteItems(routeId));
    }

    @PostMapping("/{routeId}/customers/{customerId}")
    public ResponseEntity<CollectionRouteItem> addCustomer(
            @PathVariable Long routeId,
            @PathVariable Long customerId,
            @RequestParam(required = false) Integer sequenceOrder,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.status(201).body(service.addCustomerToRoute(routeId, customerId, sequenceOrder, notes));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long itemId) {
        service.removeCustomerFromRoute(itemId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{routeId}/items/reorder")
    public ResponseEntity<CollectionRoute> reorderItems(
            @PathVariable Long routeId,
            @RequestBody CollectionRouteReorderRequest request) {
        return ResponseEntity.ok(service.reorderRouteItems(routeId, request));
    }

    @PatchMapping("/items/{itemId}/status")
    public ResponseEntity<CollectionRouteItem> updateItemStatus(
            @PathVariable Long itemId,
            @RequestBody CollectionItemStatusUpdateRequest request) {
        return ResponseEntity.ok(service.updateItemStatus(itemId, request));
    }

    @PostMapping("/search-uncollected")
    public ResponseEntity<Page<CustomerUnpaidSummaryResponse>> searchUncollected(
            @RequestBody(required = false) CollectionSearchRequest request) {
        CollectionSearchRequest safeRequest = request == null ? new CollectionSearchRequest() : request;
        return ResponseEntity.ok(service.searchUncollectedCustomers(safeRequest));
    }

    @PutMapping("/{routeId}/deactivate")
    public ResponseEntity<Void> deactivateRoute(@PathVariable Long routeId) {
        service.deactivateRoute(routeId);
        return ResponseEntity.ok().build();
    }
}

