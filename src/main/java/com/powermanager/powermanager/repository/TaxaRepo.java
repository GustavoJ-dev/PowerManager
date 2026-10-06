package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.Taxa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaxaRepo extends JpaRepository<Taxa, UUID> {

    /**
     * Busca a taxa vigente em uma determinada data.
     *
     * Regra:
     * inicioVigencia <= data
     * e
     * fimVigencia > data
     *
     * Quando fimVigencia for null, a taxa permanece vigente.
     */
    @Query("""
            Select t FROM taxa t
            WHERE t.inicioVigencia <= :data
                AND(t.fimVigencia IS NULL OR t.fimVigencia > :data)
            """)
    Optional<Taxa> buscarTaxaVigenteEm(@Param("data")LocalDateTime data);


    /**
     * Verifica se existe alguma taxa cuja vigência esteja
     * sobreposta ao intervalo informado.
     */
    @Query("""
            SELECT t FROM taxa t
            WHERE t.inicioVigencia < :fim
                AND (t.fimVigencia IS NULL OR t.fimVigencia > :inicio)
            """)
    List<Taxa> buscarTaxasComSobreposicao(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);
}
