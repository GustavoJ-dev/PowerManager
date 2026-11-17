
package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.BandeiraTarifaria;
import com.powermanager.powermanager.enums.TipoBandeira;
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
class BandeiraTarifariaRepoTest {

    @Autowired
    private BandeiraTarifariaRepo bandeiraTarifariaRepo;

    @Autowired
    private EntityManager entityManager;

    private BandeiraTarifaria criarBandeira(TipoBandeira tipo, String valor, LocalDate inicio, LocalDate fim) {

        BandeiraTarifaria bandeira = new BandeiraTarifaria();

        bandeira.setTipoBandeira(tipo);
        bandeira.setValorAdicional(new BigDecimal(valor));
        bandeira.setInicioVigencia(inicio);
        bandeira.setFimVigencia(fim);

        return bandeira;
    }

    @Test
    @DisplayName("Deve persistir uma bandeira tarifária corretamente")
    void devePersistirBandeiraTarifaria() {

        BandeiraTarifaria salva = bandeiraTarifariaRepo.saveAndFlush(
                criarBandeira(
                        TipoBandeira.AMARELA,
                        "0.018850",
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );

        assertThat(salva.getId()).isNotNull();

        entityManager.clear();

        BandeiraTarifaria encontrada =
                bandeiraTarifariaRepo.findById(salva.getId()).orElseThrow();

        assertThat(encontrada.getTipoBandeira())
                .isEqualTo(TipoBandeira.AMARELA);

        assertThat(encontrada.getValorAdicional())
                .isEqualByComparingTo("0.018850");

        assertThat(encontrada.getInicioVigencia())
                .isEqualTo(LocalDate.of(2026, 1, 1));

        assertThat(encontrada.getFimVigencia()).isNull();
    }

    @Test
    @DisplayName("Deve buscar uma bandeira pelo ID")
    void deveBuscarBandeiraPorId() {

        BandeiraTarifaria salva = bandeiraTarifariaRepo.saveAndFlush(
                criarBandeira(
                        TipoBandeira.VERDE,
                        "0.000000",
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );

        entityManager.clear();

        Optional<BandeiraTarifaria> resultado =
                bandeiraTarifariaRepo.findById(salva.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getTipoBandeira())
                .isEqualTo(TipoBandeira.VERDE);
    }

    @Test
    @DisplayName("Não deve encontrar bandeira com ID inexistente")
    void naoDeveEncontrarBandeiraPorIdInexistente() {

        assertThat(bandeiraTarifariaRepo.findById(UUID.randomUUID()))
                .isEmpty();
    }

    @Test
    @DisplayName("Deve listar todas as bandeiras cadastradas")
    void deveListarBandeiras() {

        bandeiraTarifariaRepo.saveAllAndFlush(List.of(
                criarBandeira(
                        TipoBandeira.VERDE,
                        "0.000000",
                        LocalDate.of(2026, 1, 1),
                        null
                ),
                criarBandeira(
                        TipoBandeira.AMARELA,
                        "0.018850",
                        LocalDate.of(2026, 1, 1),
                        null
                )
        ));

        entityManager.clear();

        assertThat(bandeiraTarifariaRepo.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("Deve excluir uma bandeira pelo ID")
    void deveExcluirBandeira() {

        BandeiraTarifaria salva = bandeiraTarifariaRepo.saveAndFlush(
                criarBandeira(
                        TipoBandeira.VERDE,
                        "0.000000",
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );

        UUID id = salva.getId();

        bandeiraTarifariaRepo.deleteById(id);
        bandeiraTarifariaRepo.flush();

        assertThat(bandeiraTarifariaRepo.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar a bandeira vigente em determinada data")
    void deveBuscarBandeiraVigenteEm() {

        BandeiraTarifaria salva = bandeiraTarifariaRepo.saveAndFlush(
                criarBandeira(
                        TipoBandeira.AMARELA,
                        "0.018850",
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 6, 1)
                )
        );

        Optional<BandeiraTarifaria> resultado =
                bandeiraTarifariaRepo.buscarBandeiraVigenteEm(
                        TipoBandeira.AMARELA,
                        LocalDate.of(2026, 3, 1)
                );

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getId()).isEqualTo(salva.getId());
    }

    @Test
    @DisplayName("Deve considerar a bandeira vigente quando o fim for nulo")
    void deveBuscarBandeiraSemFimDeVigencia() {

        BandeiraTarifaria salva = bandeiraTarifariaRepo.saveAndFlush(
                criarBandeira(
                        TipoBandeira.VERDE,
                        "0.000000",
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );

        Optional<BandeiraTarifaria> resultado =
                bandeiraTarifariaRepo.buscarBandeiraVigenteEm(
                        TipoBandeira.VERDE,
                        LocalDate.of(2026, 10, 1)
                );

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getId()).isEqualTo(salva.getId());
    }

    @Test
    @DisplayName("Deve considerar o início da vigência como inclusivo")
    void deveBuscarBandeiraNoInicioDaVigencia() {

        bandeiraTarifariaRepo.saveAndFlush(
                criarBandeira(
                        TipoBandeira.AMARELA,
                        "0.018850",
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 6, 1)
                )
        );

        assertThat(bandeiraTarifariaRepo.buscarBandeiraVigenteEm(
                TipoBandeira.AMARELA,
                LocalDate.of(2026, 1, 1)
        )).isPresent();
    }

    @Test
    @DisplayName("Não deve buscar bandeira antes do início da vigência")
    void naoDeveBuscarBandeiraAntesDoInicio() {

        bandeiraTarifariaRepo.saveAndFlush(
                criarBandeira(
                        TipoBandeira.AMARELA,
                        "0.018850",
                        LocalDate.of(2026, 4, 1),
                        null
                )
        );

        assertThat(bandeiraTarifariaRepo.buscarBandeiraVigenteEm(
                TipoBandeira.AMARELA,
                LocalDate.of(2026, 3, 31)
        )).isEmpty();
    }

    @Test
    @DisplayName("Não deve considerar o fim da vigência como inclusivo")
    void naoDeveBuscarBandeiraNoFimDaVigencia() {

        bandeiraTarifariaRepo.saveAndFlush(
                criarBandeira(
                        TipoBandeira.AMARELA,
                        "0.018850",
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 6, 1)
                )
        );

        assertThat(bandeiraTarifariaRepo.buscarBandeiraVigenteEm(
                TipoBandeira.AMARELA,
                LocalDate.of(2026, 6, 1)
        )).isEmpty();
    }

    @Test
    @DisplayName("Não deve encontrar bandeira de outro tipo")
    void naoDeveBuscarBandeiraDeOutroTipo() {

        bandeiraTarifariaRepo.saveAndFlush(
                criarBandeira(
                        TipoBandeira.AMARELA,
                        "0.018850",
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );

        assertThat(bandeiraTarifariaRepo.buscarBandeiraVigenteEm(
                TipoBandeira.VERDE,
                LocalDate.of(2026, 3, 1)
        )).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar bandeiras com períodos sobrepostos")
    void deveBuscarBandeirasComSobreposicao() {

        BandeiraTarifaria salva = bandeiraTarifariaRepo.saveAndFlush(
                criarBandeira(
                        TipoBandeira.AMARELA,
                        "0.018850",
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 5, 1)
                )
        );

        List<BandeiraTarifaria> resultado =
                bandeiraTarifariaRepo.buscarBandeirasComSobreposicao(
                        TipoBandeira.AMARELA,
                        LocalDate.of(2026, 4, 1),
                        LocalDate.of(2026, 6, 1)
                );

        assertThat(resultado)
                .extracting(BandeiraTarifaria::getId)
                .containsExactly(salva.getId());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver sobreposição")
    void deveRetornarListaVaziaQuandoNaoHouverSobreposicao() {

        bandeiraTarifariaRepo.saveAndFlush(
                criarBandeira(
                        TipoBandeira.AMARELA,
                        "0.018850",
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 2, 1)
                )
        );

        List<BandeiraTarifaria> resultado =
                bandeiraTarifariaRepo.buscarBandeirasComSobreposicao(
                        TipoBandeira.AMARELA,
                        LocalDate.of(2026, 3, 1),
                        LocalDate.of(2026, 4, 1)
                );

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve encontrar sobreposição quando o fim consultado for nulo")
    void deveEncontrarSobreposicaoQuandoFimForNulo() {

        bandeiraTarifariaRepo.saveAndFlush(
                criarBandeira(
                        TipoBandeira.VERDE,
                        "0.000000",
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );

        List<BandeiraTarifaria> resultado =
                bandeiraTarifariaRepo.buscarBandeirasComSobreposicao(
                        TipoBandeira.VERDE,
                        LocalDate.of(2026, 6, 1),
                        null
                );

        assertThat(resultado).hasSize(1);
    }

    @Test
    @DisplayName("Não deve encontrar sobreposição quando os períodos apenas se encostam")
    void naoDeveEncontrarSobreposicaoQuandoPeriodosApenasSeEncostarem() {

        bandeiraTarifariaRepo.saveAndFlush(
                criarBandeira(
                        TipoBandeira.AMARELA,
                        "0.018850",
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 6, 1)
                )
        );

        List<BandeiraTarifaria> resultado =
                bandeiraTarifariaRepo.buscarBandeirasComSobreposicao(
                        TipoBandeira.AMARELA,
                        LocalDate.of(2026, 6, 1),
                        LocalDate.of(2026, 12, 1)
                );

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Não deve considerar sobreposição entre tipos diferentes")
    void naoDeveConsiderarSobreposicaoEntreTiposDiferentes() {

        bandeiraTarifariaRepo.saveAndFlush(
                criarBandeira(
                        TipoBandeira.AMARELA,
                        "0.018850",
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );

        List<BandeiraTarifaria> resultado =
                bandeiraTarifariaRepo.buscarBandeirasComSobreposicao(
                        TipoBandeira.VERDE,
                        LocalDate.of(2026, 3, 1),
                        null
                );

        assertThat(resultado).isEmpty();
    }
}
