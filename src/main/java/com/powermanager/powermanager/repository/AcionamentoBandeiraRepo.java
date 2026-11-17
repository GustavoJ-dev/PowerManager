
package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.AcionamentoBandeira;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AcionamentoBandeiraRepo
        extends JpaRepository<AcionamentoBandeira, UUID> {

    /**
     * Busca o acionamento oficial vigente em determinada data.
     */
    @Query("""
            SELECT a FROM AcionamentoBandeira a
            WHERE a.inicioVigencia <= :data
              AND (a.fimVigencia IS NULL OR a.fimVigencia > :data)
            """)
    Optional<AcionamentoBandeira> buscarAcionamentoVigenteEm(
            @Param("data") LocalDate data
    );

    /**
     * Busca acionamentos cujos períodos se sobrepõem
     * ao intervalo informado.
     */
    @Query("""
            SELECT a FROM AcionamentoBandeira a
            WHERE (:fim IS NULL OR a.inicioVigencia < :fim)
              AND (a.fimVigencia IS NULL OR a.fimVigencia > :inicio)
            """)
    List<AcionamentoBandeira> buscarAcionamentosComSobreposicao(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );
}

