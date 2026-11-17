package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.Taxa;
import com.powermanager.powermanager.enums.ClasseConsumo;
import com.powermanager.powermanager.enums.ModalidadeTarifaria;
import com.powermanager.powermanager.enums.SubgrupoTarifario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaxaRepo extends JpaRepository<Taxa, UUID> {

    @Query("""
            SELECT t
            FROM Taxa t
            WHERE t.distribuidora = :distribuidora
              AND t.subgrupoTarifario = :subgrupo
              AND t.classeConsumo = :classe
              AND t.modalidadeTarifaria = :modalidade
              AND t.inicioVigencia <= :data
              AND (t.fimVigencia IS NULL OR t.fimVigencia > :data)
            """)
    Optional<Taxa> buscarTaxaVigenteEm(
            @Param("distribuidora") String distribuidora,
            @Param("subgrupo") SubgrupoTarifario subgrupo,
            @Param("classe") ClasseConsumo classe,
            @Param("modalidade") ModalidadeTarifaria modalidade,
            @Param("data") LocalDate data
    );

    @Query("""
            SELECT t
            FROM Taxa t
            WHERE t.distribuidora = :distribuidora
              AND t.subgrupoTarifario = :subgrupo
              AND t.classeConsumo = :classe
              AND t.modalidadeTarifaria = :modalidade
              AND (:fim IS NULL OR t.inicioVigencia < :fim)
              AND (t.fimVigencia IS NULL OR t.fimVigencia > :inicio)
            """)
    List<Taxa> buscarTaxasComSobreposicao(
            @Param("distribuidora") String distribuidora,
            @Param("subgrupo") SubgrupoTarifario subgrupo,
            @Param("classe") ClasseConsumo classe,
            @Param("modalidade") ModalidadeTarifaria modalidade,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );
}
