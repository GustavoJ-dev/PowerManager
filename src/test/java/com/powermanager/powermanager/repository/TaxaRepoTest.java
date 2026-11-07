package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.Taxa;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TaxaRepoTest {

    @Autowired
    private TaxaRepo taxaRepo;

    @Autowired
    private EntityManager entityManager;

    private Taxa criarTaxa(LocalDateTime inicio, LocalDateTime fim) {

        Taxa taxa = new Taxa();

        taxa.setCustoPorUnidade(new BigDecimal("0.85"));
        taxa.setAluguelMedidor(new BigDecimal("12.00"));
        taxa.setTaxaServico(new BigDecimal("5.00"));
        taxa.setImpostoServico(new BigDecimal("3.50"));
        taxa.setCess(new BigDecimal("1.25"));
        taxa.setTaxaFixa(new BigDecimal("2.00"));
        taxa.setInicioVigencia(inicio);
        taxa.setFimVigencia(fim);

        return taxa;
    }

    @Test
    @DisplayName("Deve persistir uma taxa corretamente")
    void devePersistirTaxa() {

        Taxa taxa = criarTaxa(
                LocalDateTime.of(2026, 1, 1, 0, 0),
                null);

        Taxa salva = taxaRepo.saveAndFlush(taxa);

        assertThat(salva.getId()).isNotNull();

        entityManager.clear();

        Taxa encontrada = taxaRepo.findById(salva.getId()).orElseThrow();
        assertThat(encontrada.getCustoPorUnidade()).isEqualByComparingTo("0.85");
        assertThat(encontrada.getAluguelMedidor()).isEqualByComparingTo("12.00");
        assertThat(encontrada.getTaxaServico()).isEqualByComparingTo("5.00");
        assertThat(encontrada.getImpostoServico()).isEqualByComparingTo("3.50");
        assertThat(encontrada.getCess()).isEqualByComparingTo("1.25");
        assertThat(encontrada.getTaxaFixa()).isEqualByComparingTo("2.00");
        assertThat(encontrada.getInicioVigencia())
                .isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
        assertThat(encontrada.getFimVigencia()).isNull();
    }

    @Test
    @DisplayName("Deve buscar uma taxa pelo ID")
    void deveBuscarTaxaPorId() {

        Taxa salva = taxaRepo.saveAndFlush(criarTaxa(
                LocalDateTime.of(2026, 1, 1, 0, 0), null));

        entityManager.clear();

        Optional<Taxa> resultado = taxaRepo.findById(salva.getId());

        assertThat(resultado).isPresent();

        assertThat(resultado.orElseThrow().getCustoPorUnidade())
                .isEqualByComparingTo("0.85");
    }

    @Test
    @DisplayName("Não deve encontrar taxa com ID inexistente")
    void naoDeveEncontrarTaxaPorIdInexistente() {

        Optional<Taxa> resultado = taxaRepo.findById(UUID.randomUUID());

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve listar todas as taxas")
    void deveListarTaxas() {

        taxaRepo.saveAllAndFlush(List.of(
                criarTaxa(
                        LocalDateTime.of(2026, 1, 1, 0, 0),
                        LocalDateTime.of(2026, 2, 1, 0, 0)),
                criarTaxa(
                        LocalDateTime.of(2026, 2, 1, 0, 0),
                        null)));

        entityManager.clear();

        List<Taxa> taxas = taxaRepo.findAll();

        assertThat(taxas).hasSize(2);
    }

    @Test
    @DisplayName("Deve excluir uma taxa pelo ID")
    void deveExcluirTaxa() {

        Taxa salva = taxaRepo.saveAndFlush(criarTaxa(
                LocalDateTime.of(2026, 1, 1, 0, 0), null));

        UUID id = salva.getId();

        taxaRepo.deleteById(id);
        taxaRepo.flush();

        assertThat(taxaRepo.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar a taxa vigente em determinada data")
    void deveBuscarTaxaVigenteEm() {

        Taxa salva = taxaRepo.saveAndFlush(criarTaxa(
                LocalDateTime.of(2026, 1, 1, 0, 0), null));

        Optional<Taxa> resultado = taxaRepo.buscarTaxaVigenteEm(
                LocalDateTime.of(2026, 3, 1, 0, 0));

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getId()).isEqualTo(salva.getId());
    }

    @Test
    @DisplayName("Deve considerar a taxa vigente quando não possui fim de vigência")
    void deveBuscarTaxaSemFimDeVigencia() {

        Taxa salva = taxaRepo.saveAndFlush(criarTaxa(
                LocalDateTime.of(2026, 1, 1, 0, 0), null));

        Optional<Taxa> resultado = taxaRepo.buscarTaxaVigenteEm(
                LocalDateTime.of(2026, 10, 1, 0, 0));

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getId()).isEqualTo(salva.getId());
    }

    @Test
    @DisplayName("Deve considerar o início da vigência como inclusivo")
    void deveBuscarTaxaNoInicioDaVigencia() {

        Taxa salva = taxaRepo.saveAndFlush(criarTaxa(
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 6, 1, 0, 0)));

        Optional<Taxa> resultado = taxaRepo.buscarTaxaVigenteEm(
                LocalDateTime.of(2026, 1, 1, 0, 0));

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getId()).isEqualTo(salva.getId());
    }

    @Test
    @DisplayName("Não deve buscar taxa antes do início da vigência")
    void naoDeveBuscarTaxaAntesDoInicio() {

        taxaRepo.saveAndFlush(criarTaxa(
                LocalDateTime.of(2026, 4, 1, 0, 0), null));

        Optional<Taxa> resultado = taxaRepo.buscarTaxaVigenteEm(
                LocalDateTime.of(2026, 3, 31, 23, 59));

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Não deve considerar o fim da vigência como inclusivo")
    void naoDeveBuscarTaxaNoFimDaVigencia() {

        taxaRepo.saveAndFlush(criarTaxa(
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 6, 1, 0, 0)));

        Optional<Taxa> resultado = taxaRepo.buscarTaxaVigenteEm(
                LocalDateTime.of(2026, 6, 1, 0, 0));

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar taxas com períodos sobrepostos")
    void deveBuscarTaxasComSobreposicao() {

        Taxa salva = taxaRepo.saveAndFlush(criarTaxa(
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 5, 1, 0, 0)));

        List<Taxa> resultado = taxaRepo.buscarTaxasComSobreposicao(
                LocalDateTime.of(2026, 4, 1, 0, 0),
                LocalDateTime.of(2026, 6, 1, 0, 0));

        assertThat(resultado).extracting(Taxa::getId).containsExactly(salva.getId());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver sobreposição")
    void deveRetornarListaVaziaQuandoNaoHouverSobreposicao() {

        taxaRepo.saveAndFlush(criarTaxa(
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 2, 1, 0, 0)));

        List<Taxa> resultado = taxaRepo.buscarTaxasComSobreposicao(
                LocalDateTime.of(2026, 3, 1, 0, 0),
                LocalDateTime.of(2026, 4, 1, 0, 0));

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve encontrar sobreposição quando o fim do intervalo consultado for nulo")
    void deveEncontrarSobreposicaoQuandoFimForNulo() {

        taxaRepo.saveAndFlush(criarTaxa(
                LocalDateTime.of(2026, 1, 1, 0, 0), null));

        List<Taxa> resultado = taxaRepo.buscarTaxasComSobreposicao(
                LocalDateTime.of(2026, 6, 1, 0, 0), null);

        assertThat(resultado).hasSize(1);
    }

    @Test
    @DisplayName("Deve encontrar sobreposição quando o período consultado possui fim")
    void deveEncontrarSobreposicaoQuandoFimEstiverPreenchido() {

        Taxa salva = taxaRepo.saveAndFlush(criarTaxa(
                LocalDateTime.of(2026, 1, 1, 0, 0), null));

        List<Taxa> resultado = taxaRepo.buscarTaxasComSobreposicao(
                LocalDateTime.of(2026, 3, 1, 0, 0),
                LocalDateTime.of(2026, 8, 1, 0, 0));

        assertThat(resultado).extracting(Taxa::getId).containsExactly(salva.getId());
    }

    @Test
    @DisplayName("Não deve encontrar sobreposição quando os períodos apenas se encostam")
    void naoDeveEncontrarSobreposicaoQuandoPeriodosApenasSeEncostarem() {

        taxaRepo.saveAndFlush(criarTaxa(
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 6, 1, 0, 0)));

        List<Taxa> resultado = taxaRepo.buscarTaxasComSobreposicao(
                LocalDateTime.of(2026, 6, 1, 0, 0),
                LocalDateTime.of(2026, 12, 1, 0, 0));

        assertThat(resultado).isEmpty();
    }
}