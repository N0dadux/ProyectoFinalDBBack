package com.cocobongo.cerveceria.inventory.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class InventoryMovementResponseDTO {

    // ID único del movimiento de inventario
    private Integer idMovement;

    // Identificadores del producto y sucursal involucrados
    private Integer idProduct;
    private Integer idBranch;

    // Usuario que realizó el movimiento
    private Integer idUser;

    // Tipo de movimiento: IN (entrada) o OUT (salida)
    private String type;

    // Motivo del movimiento (PURCHASE, SALE, ADJUSTMENT, etc.)
    private String reason;

    // Cantidad de unidades movidas
    private Integer quantity;

    // Fecha y hora en que se registró el movimiento
    private LocalDateTime movementDate;

    // Referencia opcional (ej: id de venta, transferencia, etc.)
    private Integer idReference;

    // Nombre del producto (dato adicional para mostrar en respuestas)
    private String productName;

    public InventoryMovementResponseDTO() {}
}