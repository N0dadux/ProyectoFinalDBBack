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
     * Actualización automática programada
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
                BigDecimal currentRate = exchangeRateService.getCurrentRate();
                
                if (currentRate == null || rate.compareTo(currentRate) != 0) {
                    exchangeRateService.updateRateAutomatically(rate);
                    log.info("✅ Tasa BCV actualizada automáticamente: Bs. {}", rate);
                } else {
                    log.info("📊 Tasa BCV sin cambios: Bs. {}", rate);
                }
            }
        } catch (Exception e) {
            log.error("❌ Error en scraping automático BCV: {}", e.getMessage());
        }
    }

    /**
     * Método principal de scraping
     */
    public BigDecimal scrapeBCVRate() {
        try {
            String url = "https://www.bcv.org.ve/";
            
            // 1. Conectar a la página
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(15000)
                    .get();
            
            // 2. Buscar la tasa en diferentes ubicaciones posibles
            
            // Opción A: Buscar dentro del div #dolar (estructura principal)
            Element dolarSection = doc.select("#dolar").first();
            if (dolarSection != null) {
                // Buscar el strong que contiene la tasa
                Element rateElement = dolarSection.select("strong").first();
                if (rateElement != null) {
                    String rateStr = rateElement.text().trim();
                    return parseRate(rateStr);
                }
            }
            
            // Opción B: Buscar por clase recuadrotsmc_center
            Element rateElement = doc.select(".recuadrotsmc_center strong").first();
            if (rateElement != null) {
                String rateStr = rateElement.text().trim();
                return parseRate(rateStr);
            }
            
            // Opción C: Buscar cualquier strong que tenga formato de número
            Elements strongElements = doc.select("strong");
            for (Element strong : strongElements) {
                String text = strong.text().trim();
                // Verificar si tiene formato de número con coma
                if (text.matches("\\d{1,3}(?:\\.\\d{3})*,\\d+")) {
                    return parseRate(text);
                }
            }
            
            log.warn("No se encontró la tasa BCV en la página");
            return null;
            
        } catch (Exception e) {
            log.error("Error al conectar con BCV: {}", e.getMessage());
            throw new RuntimeException("No se pudo obtener la tasa del BCV", e);
        }
    }
    
    /**
     * Parsea el texto de la tasa a BigDecimal
     * Ejemplo: "582,68620000" -> 582.68620000
     */
    private BigDecimal parseRate(String rateText) {
        try {
            // Limpiar el texto
            String cleanRate = rateText
                    .trim()
                    .replaceAll("\\s+", "")     // Eliminar espacios
                    .replace(".", "")            // Eliminar separadores de miles (582.686 -> 582686)
                    .replace(",", ".");          // Convertir coma decimal a punto (582,68 -> 582.68)
            
            log.debug("Tasa parseada: {} -> {}", rateText, cleanRate);
            
            return new BigDecimal(cleanRate).setScale(2, RoundingMode.HALF_UP);
            
        } catch (NumberFormatException e) {
            log.error("Error al parsear tasa: '{}'", rateText, e);
            return null;
        }
    }
    
    /**
     * Prueba de scraping (método público para debug)
     */
    public String testScraping() {
        try {
            String url = "https://www.bcv.org.ve/";
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(15000)
                    .get();
            
            // Mostrar estructura para debug
            StringBuilder sb = new StringBuilder();
            sb.append("=== ESTRUCTURA BCV ===\n");
            
            Element dolar = doc.select("#dolar").first();
            if (dolar != null) {
                sb.append("Div #dolar encontrado:\n");
                sb.append(dolar.html().substring(0, Math.min(500, dolar.html().length())));
                sb.append("\n...\n\n");
                
                Elements strongs = dolar.select("strong");
                sb.append("Elementos strong encontrados: ").append(strongs.size()).append("\n");
                for (Element s : strongs) {
                    sb.append("  - ").append(s.text()).append("\n");
                }
            } else {
                sb.append("Div #dolar NO encontrado\n");
            }
            
            return sb.toString();
            
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}