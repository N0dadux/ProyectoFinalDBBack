package com.cocobongo.cerveceria.exchangerate.entities;
 
import com.cocobongo.cerveceria.users.entities.UserEntity;
import jakarta.persistence.*;
import lombok.*;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
 
/**
 * Tasa de cambio BCV (Bolívares por 1 USD).
 *
 * Se guarda historial aunque el usuario solo vea/edite la tasa actual.
 * El historial es necesario internamente para que los reportes muestren
 * los valores en bolívares con la tasa correcta al día de cada venta.
 *
 * El usuario nunca accede al historial directamente — solo al valor actual
 * via GET /api/v1/exchange-rate/current.
 */
@Entity
@Table(name = "exchange_rate")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRateEntity {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rate")
    private Integer idRate;
 
    // Bolívares por 1 USD. Ej: 567.68 significa 1 USD = 567.68 VES
    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal rate;
 
    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    @Column(name = "is_automatic", nullable = false)
    private boolean automatic = false;
 
    // Quién registró esta tasa (ADMIN o EMPLOYEE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private UserEntity registeredBy;
 
    @PrePersist
    protected void onCreate() {
        this.registeredAt = LocalDateTime.now();
    }
}