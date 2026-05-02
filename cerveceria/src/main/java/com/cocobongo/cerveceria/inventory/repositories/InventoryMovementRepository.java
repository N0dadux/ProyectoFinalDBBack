package com.cocobongo.cerveceria.inventory.repositories;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import com.cocobongo.cerveceria.inventory.entities.InventoryMovementEntity;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovementEntity, Integer> {

    @Query("SELECT im FROM InventoryMovementEntity im " +
           "WHERE (:idProduct IS NULL OR im.idProduct = :idProduct) " +
           "AND   (:idBranch  IS NULL OR im.idBranch  = :idBranch)  " +
           "AND   (:type      IS NULL OR UPPER(im.type)   = UPPER(:type)) " +
           "AND   (:reason    IS NULL OR UPPER(im.reason) = UPPER(:reason)) " +
           "AND   (:dateFrom  IS NULL OR im.movementDate >= :dateFrom) " +
           "AND   (:dateTo    IS NULL OR im.movementDate <= :dateTo) " +
           "ORDER BY im.movementDate DESC")
    List<InventoryMovementEntity> findByFilters(
            @Param("idProduct") Integer       idProduct,
            @Param("idBranch")  Integer       idBranch,
            @Param("type")      String        type,
            @Param("reason")    String        reason,
            @Param("dateFrom")  LocalDateTime dateFrom,
            @Param("dateTo")    LocalDateTime dateTo);
}