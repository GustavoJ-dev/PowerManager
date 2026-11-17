
package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.RegraTributaria;
import com.powermanager.powermanager.enums.AbrangenciaTributaria;
import com.powermanager.powermanager.enums.TipoCalculoTributario;
import com.powermanager.powermanager.enums.TipoTributo;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RegraTributariaRepoTest {

    @Autowired
    private RegraTributariaRepo regraTributariaRepo;

    @Autowired
    private EntityManager entityManager;

    private RegraTributaria criarRegra(TipoTributo tipoTributo, AbrangenciaTributaria abrangencia,
            String estado, String municipio, String codigoIbge, LocalDate inicio, LocalDate fim) {

        RegraTributaria regra = new RegraTributaria();

        regra.setTipoTributo(tipoTributo);
        regra.setAbrangencia(abrangencia);
        regra.setTipoCalculo(TipoCalculoTributario.PERCENTUAL);
        regra.setEstado(estado);
        regra.setMunicipio(municipio);
        regra.setCodigoIbge(codigoIbge);
        regra.setAliquota(new BigDecimal("18.000000"));
        regra.setValorFixo(null);
        regra.setInicioVigencia(inicio);
        regra.setFimVigencia(fim);

        return regra;
    }

    @Test
    @DisplayName("Deve persistir uma regra tributária corretamente")
    void devePersistirRegraTributaria() {

        RegraTributaria salva = regraTributariaRepo.saveAndFlush(
                criarRegra(
                        TipoTributo.ICMS,
                        AbrangenciaTributaria.ESTADUAL,
                        "BA",
                        null,
                        null,
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );

        assertThat(salva.getId()).isNotNull();

        entityManager.clear();

        RegraTributaria encontrada =
                regraTributariaRepo.findById(salva.getId()).orElseThrow();

        assertThat(encontrada.getTipoTributo()).isEqualTo(TipoTributo.ICMS);
        assertThat(encontrada.getAbrangencia())
                .isEqualTo(AbrangenciaTributaria.ESTADUAL);
        assertThat(encontrada.getEstado()).isEqualTo("BA");
        assertThat(encontrada.getAliquota())
                .isEqualByComparingTo("18.000000");
        assertThat(encontrada.getFimVigencia()).isNull();
    }

    @Test
    @DisplayName("Deve buscar uma regra pelo ID")
    void deveBuscarRegraPorId() {

        RegraTributaria salva = regraTributariaRepo.saveAndFlush(
                criarRegra(
                        TipoTributo.ICMS,
                        AbrangenciaTributaria.ESTADUAL,
                        "BA",
                        null,
                        null,
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );

        entityManager.clear();

        Optional<RegraTributaria> resultado =
                regraTributariaRepo.findById(salva.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getTipoTributo())
                .isEqualTo(TipoTributo.ICMS);
    }

    @Test
    @DisplayName("Não deve encontrar regra com ID inexistente")
    void naoDeveEncontrarRegraPorIdInexistente() {

        assertThat(regraTributariaRepo.findById(UUID.randomUUID()))
                .isEmpty();
    }

    @Test
    @DisplayName("Deve listar todas as regras tributárias")
    void deveListarRegrasTributarias() {

        regraTributariaRepo.saveAllAndFlush(List.of(
                criarRegra(
                        TipoTributo.ICMS,
                        AbrangenciaTributaria.ESTADUAL,
                        "BA",
                        null,
                        null,
                        LocalDate.of(2026, 1, 1),
                        null
                ),
                criarRegra(
                        TipoTributo.PIS,
                        AbrangenciaTributaria.FEDERAL,
                        null,
                        null,
                        null,
                        LocalDate.of(2026, 1, 1),
                        null
                )
        ));

        entityManager.clear();

        assertThat(regraTributariaRepo.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("Deve excluir uma regra pelo ID")
    void deveExcluirRegra() {

        RegraTributaria salva = regraTributariaRepo.saveAndFlush(
                criarRegra(
                        TipoTributo.ICMS,
                        AbrangenciaTributaria.ESTADUAL,
                        "BA",
                        null,
                        null,
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );

        UUID id = salva.getId();

        regraTributariaRepo.deleteById(id);
        regraTributariaRepo.flush();

        assertThat(regraTributariaRepo.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar regra estadual vigente")
    void deveBuscarRegraEstadualVigente() {

        RegraTributaria salva = regraTributariaRepo.saveAndFlush(
                criarRegra(
                        TipoTributo.ICMS,
                        AbrangenciaTributaria.ESTADUAL,
                        "BA",
                        null,
                        null,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 7, 1)
                )
        );

        Optional<RegraTributaria> resultado =
                regraTributariaRepo.buscarRegraVigenteEm(
                        TipoTributo.ICMS,
                        AbrangenciaTributaria.ESTADUAL,
                        "BA",
                        null,
                        null,
                        LocalDate.of(2026, 3, 1)
                );

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getId()).isEqualTo(salva.getId());
    }

    @Test
    @DisplayName("Deve buscar regra federal com localização nula")
    void deveBuscarRegraFederalComLocalizacaoNula() {

        RegraTributaria salva = regraTributariaRepo.saveAndFlush(
                criarRegra(
                        TipoTributo.PIS,
                        AbrangenciaTributaria.FEDERAL,
                        null,
                        null,
                        null,
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );

        Optional<RegraTributaria> resultado =
                regraTributariaRepo.buscarRegraVigenteEm(
                        TipoTributo.PIS,
                        AbrangenciaTributaria.FEDERAL,
                        null,
                        null,
                        null,
                        LocalDate.of(2026, 5, 1)
                );

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getId()).isEqualTo(salva.getId());
    }

    @Test
    @DisplayName("Deve buscar regra municipal pela localização")
    void deveBuscarRegraMunicipalPelaLocalizacao() {

        RegraTributaria salva = regraTributariaRepo.saveAndFlush(
                criarRegra(
                        TipoTributo.CIP_COSIP,
                        AbrangenciaTributaria.MUNICIPAL,
                        "BA",
                        "Paramirim",
                        "2923600",
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );

        Optional<RegraTributaria> resultado =
                regraTributariaRepo.buscarRegraVigenteEm(
                        TipoTributo.CIP_COSIP,
                        AbrangenciaTributaria.MUNICIPAL,
                        "BA",
                        "Paramirim",
                        "2923600",
                        LocalDate.of(2026, 5, 1)
                );

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getId()).isEqualTo(salva.getId());
    }

    @Test
    @DisplayName("Não deve buscar regra antes do início da vigência")
    void naoDeveBuscarRegraAntesDoInicio() {

        regraTributariaRepo.saveAndFlush(
                criarRegra(
                        TipoTributo.ICMS,
                        AbrangenciaTributaria.ESTADUAL,
                        "BA",
                        null,
                        null,
                        LocalDate.of(2026, 4, 1),
                        null
                )
        );

        assertThat(regraTributariaRepo.buscarRegraVigenteEm(
                TipoTributo.ICMS,
                AbrangenciaTributaria.ESTADUAL,
                "BA",
                null,
                null,
                LocalDate.of(2026, 3, 31)
        )).isEmpty();
    }

    @Test
    @DisplayName("Não deve considerar o fim da vigência como inclusivo")
    void naoDeveBuscarRegraNoFimDaVigencia() {

        regraTributariaRepo.saveAndFlush(
                criarRegra(
                        TipoTributo.ICMS,
                        AbrangenciaTributaria.ESTADUAL,
                        "BA",
                        null,
                        null,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 6, 1)
                )
        );

        assertThat(regraTributariaRepo.buscarRegraVigenteEm(
                TipoTributo.ICMS,
                AbrangenciaTributaria.ESTADUAL,
                "BA",
                null,
                null,
                LocalDate.of(2026, 6, 1)
        )).isEmpty();
    }

    @Test
    @DisplayName("Não deve buscar regra de outro tributo")
    void naoDeveBuscarRegraDeOutroTributo() {

        regraTributariaRepo.saveAndFlush(
                criarRegra(
                        TipoTributo.ICMS,
                        AbrangenciaTributaria.ESTADUAL,
                        "BA",
                        null,
                        null,
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );

        assertThat(regraTributariaRepo.buscarRegraVigenteEm(
                TipoTributo.PIS,
                AbrangenciaTributaria.ESTADUAL,
                "BA",
                null,
                null,
                LocalDate.of(2026, 3, 1)
        )).isEmpty();
    }

    @Test
    @DisplayName("Não deve buscar regra de outra localização")
    void naoDeveBuscarRegraDeOutraLocalizacao() {

        regraTributariaRepo.saveAndFlush(
                criarRegra(
                        TipoTributo.ICMS,
                        AbrangenciaTributaria.ESTADUAL,
                        "BA",
                        null,
                        null,
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );

        assertThat(regraTributariaRepo.buscarRegraVigenteEm(
                TipoTributo.ICMS,
                AbrangenciaTributaria.ESTADUAL,
                "SP",
                null,
                null,
                LocalDate.of(2026, 3, 1)
        )).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar regras com vigências sobrepostas")
    void deveBuscarRegrasComSobreposicao() {

        RegraTributaria salva = regraTributariaRepo.saveAndFlush(
                criarRegra(
                        TipoTributo.ICMS,
                        AbrangenciaTributaria.ESTADUAL,
                        "BA",
                        null,
                        null,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 5, 1)
                )
        );

        List<RegraTributaria> resultado =
                regraTributariaRepo.buscarRegrasComSobreposicao(
                        TipoTributo.ICMS,
                        AbrangenciaTributaria.ESTADUAL,
                        "BA",
                        null,
                        null,
                        LocalDate.of(2026, 4, 1),
                        LocalDate.of(2026, 6, 1)
                );

        assertThat(resultado)
                .extracting(RegraTributaria::getId)
                .containsExactly(salva.getId());
    }

    @Test
    @DisplayName("Não deve encontrar sobreposição entre localizações diferentes")
    void naoDeveEncontrarSobreposicaoEntreLocalizacoesDiferentes() {

        regraTributariaRepo.saveAndFlush(
                criarRegra(
                        TipoTributo.ICMS,
                        AbrangenciaTributaria.ESTADUAL,
                        "BA",
                        null,
                        null,
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );

        List<RegraTributaria> resultado =
                regraTributariaRepo.buscarRegrasComSobreposicao(
                        TipoTributo.ICMS,
                        AbrangenciaTributaria.ESTADUAL,
                        "SP",
                        null,
                        null,
                        LocalDate.of(2026, 3, 1),
                        null
                );

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve encontrar sobreposição quando o fim consultado for nulo")
    void deveEncontrarSobreposicaoQuandoFimForNulo() {

        regraTributariaRepo.saveAndFlush(
                criarRegra(
                        TipoTributo.PIS,
                        AbrangenciaTributaria.FEDERAL,
                        null,
                        null,
                        null,
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );

        List<RegraTributaria> resultado =
                regraTributariaRepo.buscarRegrasComSobreposicao(
                        TipoTributo.PIS,
                        AbrangenciaTributaria.FEDERAL,
                        null,
                        null,
                        null,
                        LocalDate.of(2026, 6, 1),
                        null
                );

        assertThat(resultado).hasSize(1);
    }

    @Test
    @DisplayName("Não deve encontrar sobreposição quando os períodos apenas se encostam")
    void naoDeveEncontrarSobreposicaoQuandoPeriodosApenasSeEncostam() {

        regraTributariaRepo.saveAndFlush(
                criarRegra(
                        TipoTributo.ICMS,
                        AbrangenciaTributaria.ESTADUAL,
                        "BA",
                        null,
                        null,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 6, 1)
                )
        );

        List<RegraTributaria> resultado =
                regraTributariaRepo.buscarRegrasComSobreposicao(
                        TipoTributo.ICMS,
                        AbrangenciaTributaria.ESTADUAL,
                        "BA",
                        null,
                        null,
                        LocalDate.of(2026, 6, 1),
                        LocalDate.of(2026, 12, 1)
                );

        assertThat(resultado).isEmpty();
    }
}

