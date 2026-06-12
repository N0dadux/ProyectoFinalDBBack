package com.cocobongo.cerveceria.exchangerate.services;
 
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cocobongo.cerveceria.common.exception.BusinessException;
import com.cocobongo.cerveceria.exchangerate.dto.ExchangeRateRequest;
import com.cocobongo.cerveceria.exchangerate.dto.ExchangeRateResponse;
import com.cocobongo.cerveceria.exchangerate.entities.ExchangeRateEntity;
import com.cocobongo.cerveceria.exchangerate.repositories.ExchangeRateRepository;
import com.cocobongo.cerveceria.users.entities.UserEntity; 
import com.cocobongo.cerveceria.users.services.UserService;

import lombok.RequiredArgsConstructor;
 
@Service
@RequiredArgsConstructor
public class ExchangeRateService {
 
    private final ExchangeRateRepository exchangeRateRepository;
    private final UserService userService;
 
    // ── Consulta: tasa vigente ─────────────────────────────────────────────────
 
    @Transactional(readOnly = true)
    public ExchangeRateResponse getCurrent() {
        ExchangeRateEntity entity = getCurrentEntityOrThrow();
        return toResponse(entity);
    }
 
    /**
     * Retorna la entidad directamente — usado internamente por SalesService
     * y OutgoingService para asociar la tasa a la venta/gasto al momento
     * de registrarlos.
     */
    @Transactional(readOnly = true)
    public ExchangeRateEntity getCurrentEntity() {
        return getCurrentEntityOrThrow();
    }
 
    /**
     * Retorna solo el valor numérico de la tasa — usado por CurrencyConverter.
     */
    @Transactional(readOnly = true)
    public BigDecimal getCurrentRate() {
        return getCurrentEntityOrThrow().getRate();
    }
 
    // ── Actualización: cualquier usuario autenticado puede actualizarla ────────
 
    @Transactional
    public ExchangeRateResponse updateRate(ExchangeRateRequest request,
                                           UserEntity currentUser) {
        ExchangeRateEntity newRate = ExchangeRateEntity.builder()
                .rate(request.getRate())
                .registeredBy(currentUser)
                .build();
 
        ExchangeRateEntity saved = exchangeRateRepository.save(newRate);
        return toResponse(saved);
    }

    /**
     * Actualización automática por scraping (sin usuario autenticado)
     */
    @Transactional
    public void updateRateAutomatically(BigDecimal rate) {
        // Buscar usuario sistema o crear uno por defecto
        UserEntity systemUser = userService.findByEmail("cocobongo@gmail.com");
                
        ExchangeRateEntity entity = new ExchangeRateEntity();
        entity.setRate(rate);
        entity.setRegisteredBy(systemUser);
        entity.setRegisteredAt(LocalDateTime.now());
        entity.setAutomatic(true); // Campo opcional para identificar actualizaciones automáticas
        
        exchangeRateRepository.save(entity);
    }
 
    // ── Interno ────────────────────────────────────────────────────────────────
 
    private ExchangeRateEntity getCurrentEntityOrThrow() {
        return exchangeRateRepository.findLatest()
                .orElseThrow(() -> new BusinessException(
                        "No hay una tasa de cambio registrada. " +
                        "Por favor registre la tasa BCV del día antes de continuar."));
    }
 
    private ExchangeRateResponse toResponse(ExchangeRateEntity entity) {
        ExchangeRateResponse response = new ExchangeRateResponse();
        response.setIdRate(entity.getIdRate());
        response.setRate(entity.getRate());
        response.setRegisteredAt(entity.getRegisteredAt());
 
        if (entity.getRegisteredBy() != null) {
            response.setRegisteredBy(entity.getRegisteredBy().getName());
        }
 
        response.setOneDollarInVes(entity.getRate());
        response.setHundredVesInUsd(
                BigDecimal.valueOf(100)
                        .divide(entity.getRate(), 6, RoundingMode.HALF_UP));
 
        return response;
    }
}
