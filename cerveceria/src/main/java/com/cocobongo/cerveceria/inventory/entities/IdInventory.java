package com.cocobongo.cerveceria.inventory.entities;

import java.io.Serializable;
import java.util.Objects;

// Clase que representa la llave compuesta de Inventory (product + branch)
public class IdInventory implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idProduct;
    private Integer idBranch;

    public IdInventory() {}

    public IdInventory(Integer idProduct, Integer idBranch) {
        this.idProduct = idProduct;
        this.idBranch  = idBranch;
    }

    public Integer getIdProduct() { return idProduct; }
    public Integer getIdBranch()  { return idBranch; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IdInventory)) return false;
        IdInventory that = (IdInventory) o;
        return Objects.equals(idProduct, that.idProduct)
            && Objects.equals(idBranch,  that.idBranch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idProduct, idBranch);
    }
}