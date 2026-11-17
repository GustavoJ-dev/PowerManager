
package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.RegraTributaria;
import com.powermanager.powermanager.enums.AbrangenciaTributaria;
import com.powermanager.powermanager.enums.TipoTributo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegraTributariaRepo
        extends JpaRepository<RegraTributaria, UUID> {

    /**
     * Busca uma regra tributária vigente para determinada
     * combinação de tributo, abrangência e localização.
     */
    @Query("""
            SELECT r FROM RegraTributaria r
            WHERE r.tipoTributo = :tipoTributo
              AND r.abrangencia = :abrangencia
              AND ((:estado IS NULL AND r.estado IS NULL) OR r.estado = :estado)
              AND ((:municipio IS NULL AND r.municipio IS NULL) OR r.municipio = :municipio)
              AND ((:codigoIbge IS NULL AND r.codigoIbge IS NULL) OR r.codigoIbge = :codigoIbge)
              AND r.inicioVigencia <= :data
              AND (r.fimVigencia IS NULL OR r.fimVigencia > :data)
            """)
    Optional<RegraTributaria> buscarRegraVigenteEm(
            @Param("tipoTributo") TipoTributo tipoTributo,
            @Param("abrangencia") AbrangenciaTributaria abrangencia,
            @Param("estado") String estado,
            @Param("municipio") String municipio,
            @Param("codigoIbge") String codigoIbge,
            @Param("data") LocalDate data
    );

    /**
     * Busca regras tributárias da mesma combinação
     * tributária e territorial com vigências sobrepostas.
     */
    @Query("""
            SELECT r FROM RegraTributaria r
            WHERE r.tipoTributo = :tipoTributo
              AND r.abrangencia = :abrangencia
              AND ((:estado IS NULL AND r.estado IS NULL) OR r.estado = :estado)
              AND ((:municipio IS NULL AND r.municipio IS NULL) OR r.municipio = :municipio)
              AND ((:codigoIbge IS NULL AND r.codigoIbge IS NULL) OR r.codigoIbge = :codigoIbge)
              AND (:fim IS NULL OR r.inicioVigencia < :fim)
              AND (r.fimVigencia IS NULL OR r.fimVigencia > :inicio)
            """)
    List<RegraTributaria> buscarRegrasComSobreposicao(
            @Param("tipoTributo") TipoTributo tipoTributo,
            @Param("abrangencia") AbrangenciaTributaria abrangencia,
            @Param("estado") String estado,
            @Param("municipio") String municipio,
            @Param("codigoIbge") String codigoIbge,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );
}

