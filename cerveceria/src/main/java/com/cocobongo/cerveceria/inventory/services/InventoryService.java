package com.cocobongo.cerveceria.inventory.services;

import com.cocobongo.cerveceria.inventory.entities.*;
import com.cocobongo.cerveceria.inventory.repositories.*;
import com.cocobongo.cerveceria.inventory.dto.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    private static final Set<String> VALID_PRODUCT_TYPES = Set.of("RESALE", "SUPPLY", "MADE");
    private static final Set<String> VALID_ENTRY_REASONS = Set.of("PURCHASE", "ADJUSTMENT");

    private final ProviderRepository          providerRepository;
    private final ProductRepository           productRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryRepository         inventoryRepository;

    public InventoryService(ProviderRepository providerRepository,
                            ProductRepository productRepository,
                            InventoryRepository inventoryRepository,
                            InventoryMovementRepository movementRepository) {
        this.providerRepository          = providerRepository;
        this.productRepository           = productRepository;
        this.inventoryRepository         = inventoryRepository;
        this.inventoryMovementRepository = movementRepository;
    }

    // Obtiene todos los proveedores activos, con opción de búsqueda por nombre
    @Transactional(readOnly = true)
    public List<ProviderResponseDTO> findAllProviders(String search) {
        List<ProviderEntity> list = (search == null || search.isBlank())
                ? providerRepository.findByIsActiveTrue()
                : providerRepository.searchByName(search);
        return list.stream().map(ProviderResponseDTO::new).collect(Collectors.toList());
    }

    // Obtiene un proveedor activo por su ID
    @Transactional(readOnly = true)
    public ProviderResponseDTO findProviderById(Integer id) {
        ProviderEntity p = providerRepository.findByIdProviderAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Provider not found: " + id));
        return new ProviderResponseDTO(p);
    }

    // Crea un nuevo proveedor
    @Transactional
    public ProviderResponseDTO createProvider(ProviderRequestDTO request) {
        ProviderEntity p = new ProviderEntity();
        p.setName(request.getName());
        p.setTelephone(request.getTelephone());
        p.setAddress(request.getAddress());
        p.setEmail(request.getEmail());
        p.setIsActive(true);
        return new ProviderResponseDTO(providerRepository.save(p));
    }

    // Actualiza un proveedor existente
    @Transactional
    public ProviderResponseDTO updateProvider(Integer id, ProviderRequestDTO request) {
        ProviderEntity p = providerRepository.findByIdProviderAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Provider not found: " + id));
        p.setName(request.getName());
        p.setTelephone(request.getTelephone());
        p.setAddress(request.getAddress());
        p.setEmail(request.getEmail());
        return new ProviderResponseDTO(providerRepository.save(p));
    }

    // Realiza un borrado lógico (soft delete) del proveedor
    @Transactional
    public void deleteProvider(Integer id) {
        ProviderEntity p = providerRepository.findByIdProviderAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Provider not found: " + id));
        if (providerRepository.hasActiveProducts(id)) {
            throw new RuntimeException(
                "Cannot deactivate provider " + id + ": has active products associated");
        }
        p.setIsActive(false);
        providerRepository.save(p);
    }

    // Obtiene el inventario de una sucursal con opción de búsqueda por producto
    @Transactional(readOnly = true)
    public List<InventoryResponseDTO> findByBranch(Integer idBranch, String search) {
        return inventoryRepository.findByBranch(idBranch, search)
                .stream().map(this::toInventoryResponse).collect(Collectors.toList());
    }

    // Obtiene el inventario de un producto en todas las sucursales
    @Transactional(readOnly = true)
    public List<InventoryResponseDTO> findByProduct(Integer idProduct) {
        return inventoryRepository.findByProduct(idProduct)
                .stream().map(this::toInventoryResponse).collect(Collectors.toList());
    }

    // Obtiene productos con bajo stock (stock <= minStock)
    @Transactional(readOnly = true)
    public List<InventoryResponseDTO> findLowStock(Integer idBranch) {
        return inventoryRepository.findLowStock(idBranch)
                .stream().map(this::toInventoryResponse).collect(Collectors.toList());
    }

    // Registra una entrada de inventario (IN) y actualiza el stock
    @Transactional
    public InventoryMovementResponseDTO registerEntry(InventoryMovementRequestDTO request,
                                                      Integer idUserLogged) {
        String reason = request.getReason() != null
                ? request.getReason().toUpperCase() : "PURCHASE";

        if (!VALID_ENTRY_REASONS.contains(reason)) {
            throw new RuntimeException(
                "Invalid reason for manual entry: " + reason +
                ". Allowed: PURCHASE, ADJUSTMENT");
        }

        // Busca o crea el registro de inventario
        IdInventory idInventory = new IdInventory(request.getIdProduct(), request.getIdBranch());
        InventoryEntity inventory = inventoryRepository.findById(idInventory)
                .orElseGet(() -> {
                    InventoryEntity newRecord = new InventoryEntity();
                    newRecord.setIdProduct(request.getIdProduct());
                    newRecord.setIdBranch(request.getIdBranch());
                    newRecord.setStock(0);
                    newRecord.setMinStock(0);
                    return newRecord;
                });

        // Actualiza el stock
        inventory.setStock(inventory.getStock() + request.getQuantity());
        inventoryRepository.save(inventory);

        // Registra el movimiento
        InventoryMovementEntity movement = new InventoryMovementEntity();
        movement.setIdProduct(request.getIdProduct());
        movement.setIdBranch(request.getIdBranch());
        movement.setIdUser(idUserLogged);
        movement.setType("IN");
        movement.setReason(reason);
        movement.setQuantity(request.getQuantity());
        movement.setMovementDate(LocalDateTime.now());

        return toMovementResponse(inventoryMovementRepository.save(movement));
    }

    // Obtiene el historial de movimientos de inventario con filtros opcionales
    @Transactional(readOnly = true)
    public List<InventoryMovementResponseDTO> findMovements(Integer idProduct,
                                                            Integer idBranch,
                                                            String type,
                                                            String reason,
                                                            LocalDateTime from,
                                                            LocalDateTime to) {
        return inventoryMovementRepository.findByFilters(idProduct, idBranch, type, reason, from, to)
                .stream().map(this::toMovementResponse).collect(Collectors.toList());
    }

    // Convierte una entidad Inventory a DTO
    private InventoryResponseDTO toInventoryResponse(InventoryEntity i) {
        InventoryResponseDTO r = new InventoryResponseDTO();
        r.setIdProduct(i.getIdProduct());
        r.setIdBranch(i.getIdBranch());
        r.setStock(i.getStock());
        r.setMinStock(i.getMinStock());

        if (i.getProduct() != null) {
            r.setProductName(i.getProduct().getName());
            r.setProductType(i.getProduct().getType());
        }
        return r;
    }

    // Convierte una entidad InventoryMovement a DTO
    private InventoryMovementResponseDTO toMovementResponse(InventoryMovementEntity im) {
        InventoryMovementResponseDTO r = new InventoryMovementResponseDTO();
        r.setIdMovement(im.getIdMovement());
        r.setIdProduct(im.getIdProduct());
        r.setIdBranch(im.getIdBranch());
        r.setIdUser(im.getIdUser());
        r.setType(im.getType());
        r.setReason(im.getReason());
        r.setQuantity(im.getQuantity());
        r.setMovementDate(im.getMovementDate());
        r.setIdReference(im.getIdReference());

        if (im.getProduct() != null) {
            r.setProductName(im.getProduct().getName());
        }

        return r;
    }
}