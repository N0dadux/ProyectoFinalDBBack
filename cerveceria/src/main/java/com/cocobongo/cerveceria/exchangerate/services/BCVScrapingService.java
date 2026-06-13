package com.cocobongo.cerveceria.exchangerate.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class BCVScrapingService {

    private final ExchangeRateService exchangeRateService;
    private final ObjectMapper objectMapper;
    
    @Value("${bcv.scraping.enabled:true}")
    private boolean scrapingEnabled;

    @Value("${bcv.scraping.url}")
    private String bcvApiUrl;
    
    /**
     * Se ejecuta todos los días a las 8:00 AM
     * Cron: segundo minuto hora día-del-mes mes día-de-la-semana
     */
    @Scheduled(cron = "${bcv.scraping.cron:0 0 8 * * MON-FRI}")
    public void updateBCVRateAutomatically() {
        if (!scrapingEnabled) {
            log.info("Scraping BCV desactivado por configuración");
            return;
        }
        
        try {
            BigDecimal rate = scrapeBCVRate();
            
            if (rate != null && rate.compareTo(BigDecimal.ZERO) > 0) {
                // Verificar si la tasa es diferente a la actual
                BigDecimal currentRate = exchangeRateService.getCurrentRate();
                
                if (currentRate == null || rate.compareTo(currentRate) != 0) {
                    exchangeRateService.updateRateAutomatically(rate);
                    log.info("✅ Tasa BCV actualizada automáticamente: Bs. {}", rate);
                } else {
                    log.info("📊 Tasa BCV sin cambios: Bs. {}", rate);
                }
            } else {
                log.warn("⚠️ No se pudo obtener tasa BCV válida");
            }
        } catch (Exception e) {
            log.error("❌ Error al hacer scraping BCV: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Método público para scraping manual
     */
    public BigDecimal scrapeBCVRate() {
    try {
        log.debug("Conectando a DolarApi en: {}", bcvApiUrl);

        // Realizamos la petición HTTP GET al JSON
        String jsonResponse = Jsoup.connect(bcvApiUrl)
                .ignoreContentType(true) // OBLIGATORIO para JSON
                .timeout(15000)
                .execute()
                .body();
        
        // Leemos el JSON usando Jackson
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        
        // CAMBIO AQUÍ: Ahora buscamos el campo "promedio"
        if (rootNode.has("promedio") && !rootNode.get("promedio").isNull()) {
            double promedioRate = rootNode.get("promedio").asDouble();
            
            // Opción A: Guardar con 4 decimales (Recomendado para tasas altas como 582.6862)
            return BigDecimal.valueOf(promedioRate).setScale(4, RoundingMode.HALF_UP);
            
            // Opción B: Si tu base de datos o lógica de negocio estrictamente requiere 2 decimales:
            // return BigDecimal.valueOf(promedioRate).setScale(2, RoundingMode.HALF_UP);
        }
        
        log.warn("La respuesta de DolarApi no contiene el campo 'promedio' o es nulo.");
        return null;
        
    } catch (Exception e) {
        log.error("Error al obtener la tasa desde DolarApi: {}", e.getMessage());
        throw new RuntimeException("Error al obtener tasa BCV externa: " + e.getMessage());
    }
}
}