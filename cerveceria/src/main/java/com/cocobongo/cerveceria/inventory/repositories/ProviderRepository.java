package com.cocobongo.cerveceria.inventory.repositories;

import com.cocobongo.cerveceria.inventory.entities.ProviderEntity;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProviderRepository extends JpaRepository<ProviderEntity, Integer> {

    // Todos los proveedores activos
    List<ProviderEntity> findByIsActiveTrue();

    // Proveedor activo por id
    Optional<ProviderEntity> findByIdProviderAndIsActiveTrue(Integer idProvider);

    // Búsqueda por nombre (case-insensitive)
    @Query("SELECT p FROM ProviderEntity p " +
           "WHERE p.isActive = true " +
           "AND (:search IS NULL OR :search = '' " +
           "OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<ProviderEntity> searchByName(@Param("search") String search);

    // Verifica si un proveedor tiene productos activos asociados
    @Query("SELECT COUNT(pr) > 0 FROM ProductEntity pr " +
           "WHERE pr.idProvider = :idProvider " +
           "AND pr.isActive = true")
    boolean hasActiveProducts(@Param("idProvider") Integer idProvider);
}