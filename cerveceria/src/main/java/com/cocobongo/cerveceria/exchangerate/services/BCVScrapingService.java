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

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class BCVScrapingService {

    private final ExchangeRateService exchangeRateService;
    
    @Value("${bcv.scraping.enabled:true}")
    private boolean scrapingEnabled;
    
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
            // URL oficial del BCV
            String url = "https://www.bcv.org.ve";
            
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "es-VE,es;q=0.9,en;q=0.8")
                    .timeout(15000)
                    .get();
            
            // Selector para la tasa del dólar (puede necesitar ajustes si BCV cambia su HTML)
            // Estructura típica del BCV 2024:
            Element dolarDiv = doc.select("#dolar").first();
            
            if (dolarDiv != null) {
                // Buscar el valor dentro del div del dólar
                Element rateElement = dolarDiv.select("strong").first();
                
                if (rateElement != null) {
                    String rateText = rateElement.text()
                            .trim()
                            .replace(".", "")  // Eliminar separadores de miles
                            .replace(",", "."); // Convertir coma decimal a punto
                    
                    return new BigDecimal(rateText).setScale(2, RoundingMode.HALF_UP);
                }
            }
            
            // Selector alternativo (por si cambian la estructura)
            Elements posiblesTasas = doc.select(".recuadrotsmc_center strong");
            for (Element el : posiblesTasas) {
                String text = el.text().trim();
                if (text.matches("[\\d.,]+")) {
                    String rateText = text.replace(".", "").replace(",", ".");
                    return new BigDecimal(rateText).setScale(2, RoundingMode.HALF_UP);
                }
            }
            
            log.warn("No se encontró la tasa en la estructura esperada del BCV");
            return null;
            
        } catch (Exception e) {
            log.error("Error en scraping BCV: {}", e.getMessage());
            throw new RuntimeException("Error al obtener tasa BCV: " + e.getMessage());
        }
    }
}