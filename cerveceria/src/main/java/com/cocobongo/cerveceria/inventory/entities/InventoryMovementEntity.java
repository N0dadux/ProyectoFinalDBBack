package com.cocobongo.cerveceria.inventory.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.cocobongo.cerveceria.branches.entities.*;

@Entity
@Table(name = "inventory_movement")
@Data
public class InventoryMovementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movement")
    private Integer idMovement;

    // Identificadores
    @NotNull
    @Column(name = "id_product", nullable = false)
    private Integer idProduct;

    @NotNull
    @Column(name = "id_branch", nullable = false)
    private Integer idBranch;

    @NotNull
    @Column(name = "id_user", nullable = false)
    private Integer idUser;

    // Tipo de movimiento: IN o OUT
    @NotNull
    @Pattern(regexp = "IN|OUT", message = "Type must be IN or OUT")
    @Column(name = "type", nullable = false)
    private String type;

    // Motivo del movimiento
    @NotNull
    @Pattern(
        regexp = "PURCHASE|SALE|PRODUCTION|TRANSFER|ADJUSTMENT|RETURN",
        message = "Invalid reason"
    )
    @Column(name = "reason", nullable = false)
    private String reason;

    // Cantidad (> 0)
    @NotNull
    @Positive(message = "Quantity must be greater than 0")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // Fecha automática
    @Column(name = "movement_date", nullable = false, updatable = false)
    private LocalDateTime movementDate = LocalDateTime.now();

    // Referencia opcional
    @Column(name = "id_reference")
    private Integer idReference;

    // Relación con producto (solo lectura)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "id_product",
        insertable = false,
        updatable = false
    )
    private ProductEntity product;

    // 🔥 IMPORTANTE: faltaba esta relación
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "id_branch",
        insertable = false,
        updatable = false
    )
    private BranchEntity branch;
}