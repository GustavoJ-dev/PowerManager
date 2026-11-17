
package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.BandeiraTarifaria;
import com.powermanager.powermanager.enums.TipoBandeira;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BandeiraTarifariaRepo extends JpaRepository<BandeiraTarifaria, UUID> {

    /**
     * Busca o valor adicional de uma bandeira vigente
     * em determinada data.
     */
    @Query("""
            SELECT b FROM BandeiraTarifaria b
            WHERE b.tipoBandeira = :tipo
              AND b.inicioVigencia <= :data
              AND (b.fimVigencia IS NULL OR b.fimVigencia > :data)
            """)
    Optional<BandeiraTarifaria> buscarBandeiraVigenteEm(
            @Param("tipo") TipoBandeira tipo,
            @Param("data") LocalDate data
    );

    /**
     * Busca cadastros do mesmo tipo cuja vigência
     * esteja sobreposta ao intervalo informado.
     */
    @Query("""
            SELECT b FROM BandeiraTarifaria b
            WHERE b.tipoBandeira = :tipo
              AND (:fim IS NULL OR b.inicioVigencia < :fim)
              AND (b.fimVigencia IS NULL OR b.fimVigencia > :inicio)
            """)
    List<BandeiraTarifaria> buscarBandeirasComSobreposicao(
            @Param("tipo") TipoBandeira tipo,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );
}

