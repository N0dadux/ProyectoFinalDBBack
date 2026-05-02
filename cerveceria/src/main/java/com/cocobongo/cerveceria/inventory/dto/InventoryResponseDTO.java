package com.cocobongo.cerveceria.inventory.dto;

import com.cocobongo.cerveceria.inventory.entities.InventoryEntity;
import lombok.Data;

@Data
public class InventoryResponseDTO {

    // Identificadores del producto y sucursal
    private Integer idProduct;
    private Integer idBranch;

    // Stock actual y stock mínimo configurado
    private Integer stock;
    private Integer minStock;

    // Datos adicionales del producto (para respuesta)
    private String productName;
    private String productType;

    public InventoryResponseDTO() {}

    // Constructor que transforma la entidad en DTO
    public InventoryResponseDTO(InventoryEntity i) {
        this.idProduct = i.getIdProduct();
        this.idBranch  = i.getIdBranch();
        this.stock     = i.getStock();
        this.minStock  = i.getMinStock();

        // evitar NullPointerException
        if (i.getProduct() != null) {
            this.productName = i.getProduct().getName();
            this.productType = i.getProduct().getType();
        }
    }
}