package com.cocobongo.cerveceria.auth.repositories;
 
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cocobongo.cerveceria.auth.entities.AuditEntity;
 
public interface AuditRepository extends JpaRepository<AuditEntity, Integer> {
 
    /*
     * Ejemplo de query JPQL con filtros dinámicos.
     * Este es el patrón que sustituye a JDBC para consultas con múltiples
     * parámetros opcionales: se usan IS NULL como cortocircuito —
     * si el parámetro llega null, esa condición siempre es true y se ignora.
     */
    @Query("""
        SELECT a FROM AuditEntity a
        WHERE (:userId   IS NULL OR a.user.idUser = :userId)
          AND (:action   IS NULL OR a.action      = :action)
          AND (CAST(:from AS java.time.LocalDateTime) IS NULL OR a.createdAt >= :from)
          AND (CAST(:to   AS java.time.LocalDateTime) IS NULL OR a.createdAt <= :to)
        ORDER BY a.createdAt DESC
    """)
    Page<AuditEntity> findByFilters(
            @Param("userId") Integer userId,
            @Param("action") String  action,
            @Param("from")   LocalDateTime from,
            @Param("to")     LocalDateTime to,
            Pageable pageable
    );
}