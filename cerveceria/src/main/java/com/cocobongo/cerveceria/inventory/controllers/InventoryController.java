package com.cocobongo.cerveceria.inventory.controllers;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

import com.cocobongo.cerveceria.inventory.dto.*;
import com.cocobongo.cerveceria.inventory.services.InventoryService;

import jakarta.validation.Valid;

@RestController
public class InventoryController {

    private final InventoryService inventoryService;
 
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // PROVIDERS — /api/v1/providers
    // RF-INV-02 — Solo ADMIN
 
    @GetMapping("/api/v1/providers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProviderResponseDTO>> findAllProviders(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(inventoryService.findAllProviders(search));
    }
 
    @GetMapping("/api/v1/providers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProviderResponseDTO> findProviderById(@PathVariable Integer id) {
        return ResponseEntity.ok(inventoryService.findProviderById(id));
    }
 
    @PostMapping("/api/v1/providers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProviderResponseDTO> createProvider(
            @Valid @RequestBody ProviderRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.createProvider(request));
    }
 
    @PutMapping("/api/v1/providers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProviderResponseDTO> updateProvider(
            @PathVariable Integer id,
            @Valid @RequestBody ProviderRequestDTO request) {
        return ResponseEntity.ok(inventoryService.updateProvider(id, request));
    }
 
    @DeleteMapping("/api/v1/providers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProvider(@PathVariable Integer id) {
        inventoryService.deleteProvider(id);
        return ResponseEntity.noContent().build();
    }

    // PRODUCT - /api/v1/products
    // RF-INV-01, RF-INV-02



    // INVENTORY — /api/v1/inventory
    // RF-INV-05
 
    @GetMapping("/api/v1/inventory")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<InventoryResponseDTO>> findInventoryByBranch(
            @RequestParam Integer idBranch,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(inventoryService.findByBranch(idBranch, search));
    }
 
    @GetMapping("/api/v1/inventory/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InventoryResponseDTO>> findInventoryByProduct(
            @PathVariable Integer idProduct) {
        return ResponseEntity.ok(inventoryService.findByProduct(idProduct));
    }
 
    // INVENTORY MOVEMENT — /api/v1/inventory/entries y /movements
    // RF-INV-04
 
    @PostMapping("/api/v1/inventory/entries")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InventoryMovementResponseDTO> registerEntry(
            @Valid @RequestBody InventoryMovementRequestDTO request,
            @AuthenticationPrincipal Integer idUserLogged) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.registerEntry(request, idUserLogged));
    }
 
    @GetMapping("/api/v1/inventory/movements")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InventoryMovementResponseDTO>> findMovements(
            @RequestParam(required = false) Integer idProduct,
            @RequestParam(required = false) Integer idBranch,
            @RequestParam(required = false) String  type,
            @RequestParam(required = false) String  reason,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(
                inventoryService.findMovements(idProduct, idBranch, type, reason, from, to));
    }

}
