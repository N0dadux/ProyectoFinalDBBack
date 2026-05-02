package com.cocobongo.cerveceria.inventory.repositories;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import com.cocobongo.cerveceria.inventory.entities.*;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, IdInventory> {

    // Inventario de una sucursal con búsqueda por nombre de producto
    @Query("SELECT i FROM InventoryEntity i JOIN FETCH i.product p " +
           "WHERE i.idBranch = :branchId " +
           "AND p.isActive = true " +
           "AND (:search IS NULL OR :search = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<InventoryEntity> findByBranch(
            @Param("branchId") Integer branchId,
            @Param("search")   String  search);

    // Inventario de un producto en todas sus sucursales
    @Query("SELECT i FROM InventoryEntity i WHERE i.idProduct = :productId")
    List<InventoryEntity> findByProduct(@Param("productId") Integer productId);

    // Stock de un producto en una sucursal específica
    @Query("SELECT i FROM InventoryEntity i " +
           "WHERE i.idProduct = :productId AND i.idBranch = :branchId")
    Optional<InventoryEntity> findByProductAndBranch(
            @Param("productId") Integer productId,
            @Param("branchId")  Integer branchId);

    // Productos con stock <= minStock (alertas)
    @Query("SELECT i FROM InventoryEntity i JOIN FETCH i.product p " +
           "WHERE i.stock <= i.minStock " +
           "AND p.isActive = true " +
           "AND (:branchId IS NULL OR i.idBranch = :branchId)")
    List<InventoryEntity> findLowStock(@Param("branchId") Integer branchId);
}