package com.mahmoud.maalflow.modules.installments.schedule.controller;

import com.mahmoud.maalflow.modules.installments.schedule.entity.CollectionRoute;
import com.mahmoud.maalflow.modules.installments.schedule.entity.CollectionRouteItem;
import com.mahmoud.maalflow.modules.installments.schedule.enums.RouteType;
import com.mahmoud.maalflow.modules.installments.schedule.service.CollectionRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Collection Route REST Controller (Requirement #6).
 * Copy to: src/main/java/com/mahmoud/maalflow/modules/installments/schedule/controller/
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

    @PutMapping("/{routeId}/deactivate")
    public ResponseEntity<Void> deactivateRoute(@PathVariable Long routeId) {
        service.deactivateRoute(routeId);
        return ResponseEntity.ok().build();
    }
}

