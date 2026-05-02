package com.cocobongo.cerveceria.inventory.dto;

import com.cocobongo.cerveceria.inventory.entities.ProviderEntity;
import lombok.Data;

@Data
public class ProviderResponseDTO {

    // Identificador único del proveedor
    private Integer idProvider;

    // Datos básicos del proveedor
    private String name;
    private String telephone;
    private String address;
    private String email;

    // Estado lógico (soft delete)
    private Boolean isActive;

    public ProviderResponseDTO() {}

    // Constructor que transforma la entidad en DTO
    public ProviderResponseDTO(ProviderEntity p) {
        if (p != null) {
            this.idProvider = p.getIdProvider();
            this.name       = p.getName();
            this.telephone  = p.getTelephone();
            this.address    = p.getAddress();
            this.email      = p.getEmail();
            this.isActive   = p.getIsActive();
        }
    }
}