package com.cocobongo.cerveceria.inventory.entities;

import com.cocobongo.cerveceria.branches.entities.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "inventory")
@IdClass(IdInventory.class) // Llave compuesta (producto + sucursal)
@Data
public class InventoryEntity {

    @Id
    @Column(name = "id_product", nullable = false)
    private Integer idProduct;

    @Id
    @Column(name = "id_branch", nullable = false)
    private Integer idBranch;

    // Stock actual (no puede ser negativo)
    @NotNull
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    // Stock mínimo para alertas
    @NotNull
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    @Column(name = "min_stock", nullable = false)
    private Integer minStock = 0;

    // Relación con producto (solo lectura)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "id_product",
        insertable = false,
        updatable = false,
        foreignKey = @ForeignKey(name = "fk_inventory_product")
    )
    private ProductEntity product;

    // Relación con sucursal (solo lectura)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "id_branch",
        insertable = false,
        updatable = false,
        foreignKey = @ForeignKey(name = "fk_inventory_branch")
    )
    private BranchEntity branch;
}