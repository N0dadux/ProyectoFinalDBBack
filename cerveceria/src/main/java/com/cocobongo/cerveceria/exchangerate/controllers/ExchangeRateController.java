package com.cocobongo.cerveceria.exchangerate.controllers;

import com.cocobongo.cerveceria.common.dto.ApiResponse;
import com.cocobongo.cerveceria.exchangerate.dto.ExchangeRateRequest;
import com.cocobongo.cerveceria.exchangerate.dto.ExchangeRateResponse;
import com.cocobongo.cerveceria.exchangerate.services.ExchangeRateService;
import com.cocobongo.cerveceria.users.entities.UserEntity;
import com.cocobongo.cerveceria.exchangerate.services.BCVScrapingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/exchange-rate")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;
    private final BCVScrapingService bcvScrapingService;

    /**
     * GET /api/v1/exchange-rate/current
     * Tasa BCV vigente — usada por el dashboard y por el frontend
     * para calcular precios en bolívares en tiempo real antes de
     * registrar una venta.
     */
    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<ExchangeRateResponse>> getCurrent() {
        return ResponseEntity.ok(ApiResponse.ok(exchangeRateService.getCurrent()));
    }

    /**
     * PATCH /api/v1/exchange-rate
     * Actualiza la tasa BCV del día.
     * Accesible por ADMIN y EMPLOYEE — el dueño puede delegar esta tarea.
     * Cada actualización crea un nuevo registro (historial silencioso).
     */
    @PatchMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<ExchangeRateResponse>> updateRate(
            @Valid @RequestBody ExchangeRateRequest request,
            @AuthenticationPrincipal UserEntity currentUser) {

        ExchangeRateResponse response = exchangeRateService.updateRate(request, currentUser);
        return ResponseEntity.ok(
                ApiResponse.ok("Tasa BCV actualizada correctamente", response));
    }

    @PostMapping("/scrape")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<ExchangeRateResponse>> scrapeNow() {
        try {
            BigDecimal rate = bcvScrapingService.scrapeBCVRate();
            if (rate != null) {
                ExchangeRateRequest request = new ExchangeRateRequest();
                request.setRate(rate);
                // Aquí necesitas obtener el usuario actual del contexto
                return ResponseEntity.ok(
                        ApiResponse.ok("Tasa BCV actualizada desde el BCV: Bs. " + rate, null));
            }
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("No se pudo obtener la tasa del BCV"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/v1/exchange-rate/test-scrape
     * Endpoint de diagnóstico (solo desarrollo)
     *
    @GetMapping("/test-scrape")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> testScraping() {
        String html = bcvScrapingService.testScraping();
        return ResponseEntity.ok(ApiResponse.ok("Diagnóstico BCV", html));
    }
    */
}