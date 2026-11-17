
package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.AcionamentoBandeira;
import com.powermanager.powermanager.enums.TipoBandeira;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AcionamentoBandeiraRepoTest {

    @Autowired
    private AcionamentoBandeiraRepo acionamentoBandeiraRepo;

    @Autowired
    private EntityManager entityManager;

    private AcionamentoBandeira criarAcionamento(TipoBandeira tipo, LocalDate inicio, LocalDate fim) {

        AcionamentoBandeira acionamento = new AcionamentoBandeira();

        acionamento.setTipoBandeira(tipo);
        acionamento.setInicioVigencia(inicio);
        acionamento.setFimVigencia(fim);
        acionamento.setDataDivulgacao(LocalDate.of(2025, 12, 15));
        acionamento.setReferenciaOficial("Publicação oficial de teste");

        return acionamento;
    }

    @Test
    @DisplayName("Deve persistir um acionamento de bandeira corretamente")
    void devePersistirAcionamento() {

        AcionamentoBandeira salvo = acionamentoBandeiraRepo.saveAndFlush(
                criarAcionamento(
                        TipoBandeira.AMARELA,
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );

        assertThat(salvo.getId()).isNotNull();

        entityManager.clear();

        AcionamentoBandeira encontrado =
                acionamentoBandeiraRepo.findById(salvo.getId()).orElseThrow();

        assertThat(encontrado.getTipoBandeira())
                .isEqualTo(TipoBandeira.AMARELA);

        assertThat(encontrado.getInicioVigencia())
                .isEqualTo(LocalDate.of(2026, 1, 1));

        assertThat(encontrado.getFimVigencia()).isNull();

        assertThat(encontrado.getDataDivulgacao())
                .isEqualTo(LocalDate.of(2025, 12, 15));

        assertThat(encontrado.getReferenciaOficial())
                .isEqualTo("Publicação oficial de teste");
    }

    @Test
    @DisplayName("Deve buscar um acionamento pelo ID")
    void deveBuscarAcionamentoPorId() {

        AcionamentoBandeira salvo = acionamentoBandeiraRepo.saveAndFlush(
                criarAcionamento(
                        TipoBandeira.VERDE,
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );

        entityManager.clear();

        Optional<AcionamentoBandeira> resultado =
                acionamentoBandeiraRepo.findById(salvo.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getTipoBandeira())
                .isEqualTo(TipoBandeira.VERDE);
    }

    @Test
    @DisplayName("Não deve encontrar acionamento com ID inexistente")
    void naoDeveEncontrarAcionamentoPorIdInexistente() {

        assertThat(acionamentoBandeiraRepo.findById(UUID.randomUUID()))
                .isEmpty();
    }

    @Test
    @DisplayName("Deve listar todos os acionamentos")
    void deveListarAcionamentos() {

        acionamentoBandeiraRepo.saveAllAndFlush(List.of(
                criarAcionamento(
                        TipoBandeira.VERDE,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 2, 1)
                ),
                criarAcionamento(
                        TipoBandeira.AMARELA,
                        LocalDate.of(2026, 2, 1),
                        null
                )
        ));

        entityManager.clear();

        assertThat(acionamentoBandeiraRepo.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("Deve excluir um acionamento pelo ID")
    void deveExcluirAcionamento() {

        AcionamentoBandeira salvo = acionamentoBandeiraRepo.saveAndFlush(
                criarAcionamento(
                        TipoBandeira.VERDE,
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );

        UUID id = salvo.getId();

        acionamentoBandeiraRepo.deleteById(id);
        acionamentoBandeiraRepo.flush();

        assertThat(acionamentoBandeiraRepo.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar o acionamento vigente em determinada data")
    void deveBuscarAcionamentoVigenteEm() {

        AcionamentoBandeira salvo = acionamentoBandeiraRepo.saveAndFlush(
                criarAcionamento(
                        TipoBandeira.AMARELA,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 4, 1)
                )
        );

        Optional<AcionamentoBandeira> resultado =
                acionamentoBandeiraRepo.buscarAcionamentoVigenteEm(
                        LocalDate.of(2026, 2, 15)
                );

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getId()).isEqualTo(salvo.getId());
    }

    @Test
    @DisplayName("Deve buscar acionamento com fim de vigência nulo")
    void deveBuscarAcionamentoSemFimDeVigencia() {

        AcionamentoBandeira salvo = acionamentoBandeiraRepo.saveAndFlush(
                criarAcionamento(
                        TipoBandeira.VERDE,
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );

        Optional<AcionamentoBandeira> resultado =
                acionamentoBandeiraRepo.buscarAcionamentoVigenteEm(
                        LocalDate.of(2026, 10, 1)
                );

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getId()).isEqualTo(salvo.getId());
    }

    @Test
    @DisplayName("Deve considerar o início da vigência como inclusivo")
    void deveBuscarAcionamentoNoInicioDaVigencia() {

        acionamentoBandeiraRepo.saveAndFlush(
                criarAcionamento(
                        TipoBandeira.AMARELA,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 4, 1)
                )
        );

        assertThat(acionamentoBandeiraRepo.buscarAcionamentoVigenteEm(
                LocalDate.of(2026, 1, 1)
        )).isPresent();
    }

    @Test
    @DisplayName("Não deve buscar acionamento antes do início da vigência")
    void naoDeveBuscarAcionamentoAntesDoInicio() {

        acionamentoBandeiraRepo.saveAndFlush(
                criarAcionamento(
                        TipoBandeira.AMARELA,
                        LocalDate.of(2026, 2, 1),
                        null
                )
        );

        assertThat(acionamentoBandeiraRepo.buscarAcionamentoVigenteEm(
                LocalDate.of(2026, 1, 31)
        )).isEmpty();
    }

    @Test
    @DisplayName("Não deve considerar o fim da vigência como inclusivo")
    void naoDeveBuscarAcionamentoNoFimDaVigencia() {

        acionamentoBandeiraRepo.saveAndFlush(
                criarAcionamento(
                        TipoBandeira.AMARELA,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 4, 1)
                )
        );

        assertThat(acionamentoBandeiraRepo.buscarAcionamentoVigenteEm(
                LocalDate.of(2026, 4, 1)
        )).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar acionamentos com períodos sobrepostos")
    void deveBuscarAcionamentosComSobreposicao() {

        AcionamentoBandeira salvo = acionamentoBandeiraRepo.saveAndFlush(
                criarAcionamento(
                        TipoBandeira.AMARELA,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 5, 1)
                )
        );

        List<AcionamentoBandeira> resultado =
                acionamentoBandeiraRepo.buscarAcionamentosComSobreposicao(
                        LocalDate.of(2026, 4, 1),
                        LocalDate.of(2026, 6, 1)
                );

        assertThat(resultado)
                .extracting(AcionamentoBandeira::getId)
                .containsExactly(salvo.getId());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver sobreposição")
    void deveRetornarListaVaziaQuandoNaoHouverSobreposicao() {

        acionamentoBandeiraRepo.saveAndFlush(
                criarAcionamento(
                        TipoBandeira.VERDE,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 2, 1)
                )
        );

        List<AcionamentoBandeira> resultado =
                acionamentoBandeiraRepo.buscarAcionamentosComSobreposicao(
                        LocalDate.of(2026, 3, 1),
                        LocalDate.of(2026, 4, 1)
                );

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve encontrar sobreposição quando o fim consultado for nulo")
    void deveEncontrarSobreposicaoQuandoFimForNulo() {

        acionamentoBandeiraRepo.saveAndFlush(
                criarAcionamento(
                        TipoBandeira.VERDE,
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );

        List<AcionamentoBandeira> resultado =
                acionamentoBandeiraRepo.buscarAcionamentosComSobreposicao(
                        LocalDate.of(2026, 6, 1),
                        null
                );

        assertThat(resultado).hasSize(1);
    }

    @Test
    @DisplayName("Não deve encontrar sobreposição quando os períodos apenas se encostam")
    void naoDeveEncontrarSobreposicaoQuandoPeriodosApenasSeEncostarem() {

        acionamentoBandeiraRepo.saveAndFlush(
                criarAcionamento(
                        TipoBandeira.AMARELA,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 6, 1)
                )
        );

        List<AcionamentoBandeira> resultado =
                acionamentoBandeiraRepo.buscarAcionamentosComSobreposicao(
                        LocalDate.of(2026, 6, 1),
                        LocalDate.of(2026, 12, 1)
                );

        assertThat(resultado).isEmpty();
    }
}

