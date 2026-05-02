package com.cocobongo.cerveceria.inventory.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Entity
@Table(name = "provider")
@Data 
public class ProviderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_provider")
    private Integer idProvider;

    // Nombre obligatorio
    @NotNull(message = "Name is required")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // Teléfono opcional con validación
    @Column(name = "telephone", length = 20)
    @Pattern(
        regexp = "^[0-9\\s\\+\\-\\(\\)]{7,20}$",
        message = "Invalid telephone format"
    )
    private String telephone;

    // Dirección opcional
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    // Email opcional pero válido si viene
    @Column(name = "email", length = 100)
    @Email(
        regexp = "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$",
        message = "Invalid email format"
    )
    private String email;

    // Soft delete
    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}