package com.cocobongo.cerveceria.inventory.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProviderRequestDTO {

    // Nombre obligatorio (máx 100 caracteres)
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    // Teléfono opcional (mismo patrón que en la BD)
    @Size(max = 20, message = "Telephone must be at most 20 characters")
    @Pattern(
        regexp = "^[0-9\\s\\+\\-\\(\\)]{7,20}$",
        message = "Invalid telephone format"
    )
    private String telephone;

    // Dirección opcional
    private String address;

    // Email válido (alineado con BD)
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must be at most 100 characters")
    private String email;
}