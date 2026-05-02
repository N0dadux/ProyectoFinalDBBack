package com.cocobongo.cerveceria.inventory.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class InventoryMovementRequestDTO {

    // ID del producto sobre el cual se realizará el movimiento de inventario
    @NotNull(message = "Product id is required")
    private Integer idProduct;

    // ID de la sucursal donde ocurre el movimiento
    @NotNull(message = "Branch id is required")
    private Integer idBranch;

    // Cantidad de unidades a ingresar (debe ser mayor a 0)
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private Integer quantity;

    // Motivo del movimiento (solo PURCHASE o ADJUSTMENT)
    // Se valida aquí para evitar valores inválidos desde la entrada
    @Pattern(
        regexp = "PURCHASE|ADJUSTMENT",
        message = "Reason must be either PURCHASE or ADJUSTMENT"
    )
    private String reason = "PURCHASE";

    // Observaciones opcionales del movimiento
    private String notes;
}