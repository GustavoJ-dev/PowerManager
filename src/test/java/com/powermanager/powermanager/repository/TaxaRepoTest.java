package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.Taxa;
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

    private Taxa criarTaxa(LocalDateTime inicio, LocalDateTime fim){

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
    void devePersistirTaxa() {

        Taxa taxa = criarTaxa(LocalDateTime.of(2026, 1, 1, 0, 0), null);

        Taxa salva = taxaRepo.saveAndFlush(taxa);

        assertThat(salva.getId()).isNotNull();
        assertThat(taxaRepo.findById(salva.getId())).isPresent();
    }

    @Test
    void deveBuscarTaxaPorId(){
        Taxa salva = taxaRepo.saveAndFlush(criarTaxa(
                LocalDateTime.of(2026, 1, 1, 0, 0), null));

        Optional<Taxa> resultado = taxaRepo.findById(salva.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getCustoPorUnidade()).isEqualByComparingTo("0.85");
    }

    @Test
    void deveListarTaxas(){

        taxaRepo.saveAndFlush(criarTaxa(
                LocalDateTime.of(2026, 1, 1, 0, 0), null));

        taxaRepo.saveAndFlush(criarTaxa(
                LocalDateTime.of(2026, 2, 1, 0, 0), null));

        List<Taxa> taxas = taxaRepo.findAll();

        assertThat(taxas).hasSize(2);
    }

    @Test
    void deveExcluirTaxa(){

        Taxa salva = taxaRepo.saveAndFlush(criarTaxa(
                LocalDateTime.of(2026, 1, 1, 0, 0), null));

        UUID id = salva.getId();
        taxaRepo.deleteById(id);
        taxaRepo.flush();

        assertThat(taxaRepo.findById(id)).isEmpty();
    }

    @Test
    void deveBuscarTaxaVigenteEm(){

        LocalDateTime inicio = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime dataConsulta = LocalDateTime.of(2026, 3, 1, 0, 0);

        Taxa salva = taxaRepo.saveAndFlush(criarTaxa(inicio, null));

        Optional<Taxa> resultado = taxaRepo.buscarTaxaVigenteEm(dataConsulta);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(salva.getId());
    }

    @Test
    void deveBuscarTaxaSemFimDeVigencia(){

        Taxa salva = taxaRepo.saveAndFlush(criarTaxa(
                LocalDateTime.of(2026, 1, 1, 0, 0),
                null));

        Optional<Taxa> resultado = taxaRepo.buscarTaxaVigenteEm(
                LocalDateTime.of(2026, 10, 1, 0, 0));

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(salva.getId());
    }

    @Test
    void naoDeveBuscarTaxaAntesDoInicio(){

        taxaRepo.saveAndFlush(criarTaxa(
                LocalDateTime.of(2026, 4, 1, 0, 0),
                null));

        Optional<Taxa> resultado = taxaRepo.buscarTaxaVigenteEm(
                LocalDateTime.of(2026, 3, 31, 23, 59));

        assertThat(resultado).isEmpty();
    }

    @Test
    void naoDeveBuscarTaxaNoFimDaVigencia(){

        taxaRepo.saveAndFlush(criarTaxa(
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 6, 1, 0, 0)));

        Optional<Taxa> resultado = taxaRepo.buscarTaxaVigenteEm(
                LocalDateTime.of(2026, 6, 1, 0, 0));

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveBuscarTaxasComSobreposicao(){

        Taxa salva = taxaRepo.saveAndFlush(criarTaxa(
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 5, 1, 0, 0)));

        List<Taxa> resultado = taxaRepo.buscarTaxasComSobreposicao(
                LocalDateTime.of(2026, 4, 1, 0, 0),
                LocalDateTime.of(2026, 6, 1, 0, 0));

        assertThat(resultado)
                .extracting(Taxa::getId)
                .containsExactly(salva.getId());
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverSobreposicao(){

        taxaRepo.saveAndFlush(criarTaxa(
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 2, 1, 0, 0)));

        List<Taxa> resultado = taxaRepo.buscarTaxasComSobreposicao(
                LocalDateTime.of(2026, 3, 1, 0, 0),
                LocalDateTime.of(2026, 4, 1, 0, 0));

        assertThat(resultado).isEmpty();
    }
}