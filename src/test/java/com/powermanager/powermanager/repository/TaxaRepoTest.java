package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.Taxa;
import com.powermanager.powermanager.enums.ClasseConsumo;
import com.powermanager.powermanager.enums.ModalidadeTarifaria;
import com.powermanager.powermanager.enums.SubgrupoTarifario;
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
class TaxaRepoTest {

    @Autowired
    private TaxaRepo taxaRepo;

    @Autowired
    private EntityManager entityManager;

    private Taxa criarTaxa(

            String distribuidora,
            SubgrupoTarifario subgrupo,
            ClasseConsumo classe,
            ModalidadeTarifaria modalidade,
            LocalDate inicio,
            LocalDate fim
    ) {
        Taxa taxa = new Taxa();

        taxa.setDistribuidora(distribuidora);
        taxa.setSubgrupoTarifario(subgrupo);
        taxa.setClasseConsumo(classe);
        taxa.setModalidadeTarifaria(modalidade);

        taxa.setTarifaEnergia(new BigDecimal("0.300000"));
        taxa.setTarifaUsoDistribuicao(new BigDecimal("0.500000"));

        taxa.setInicioVigencia(inicio);
        taxa.setFimVigencia(fim);

        return taxa;
    }

    private Taxa criarTaxaResidencial(LocalDate inicio, LocalDate fim) {

        return criarTaxa(
                "Neoenergia Coelba",
                SubgrupoTarifario.B1,
                ClasseConsumo.RESIDENCIAL,
                ModalidadeTarifaria.CONVENCIONAL,
                inicio,
                fim
        );
    }

    @Test
    @DisplayName("Deve persistir uma taxa corretamente")
    void devePersistirTaxa() {

        Taxa salva = taxaRepo.saveAndFlush(
                criarTaxaResidencial(
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );

        assertThat(salva.getId()).isNotNull();

        entityManager.clear();

        Taxa encontrada = taxaRepo.findById(salva.getId()).orElseThrow();

        assertThat(encontrada.getDistribuidora())
                .isEqualTo("Neoenergia Coelba");

        assertThat(encontrada.getSubgrupoTarifario())
                .isEqualTo(SubgrupoTarifario.B1);

        assertThat(encontrada.getClasseConsumo())
                .isEqualTo(ClasseConsumo.RESIDENCIAL);

        assertThat(encontrada.getModalidadeTarifaria())
                .isEqualTo(ModalidadeTarifaria.CONVENCIONAL);

        assertThat(encontrada.getTarifaEnergia())
                .isEqualByComparingTo("0.300000");

        assertThat(encontrada.getTarifaUsoDistribuicao())
                .isEqualByComparingTo("0.500000");

        assertThat(encontrada.getInicioVigencia())
                .isEqualTo(LocalDate.of(2026, 1, 1));

        assertThat(encontrada.getFimVigencia()).isNull();
    }

    @Test
    @DisplayName("Deve buscar uma taxa pelo ID")
    void deveBuscarTaxaPorId() {

        Taxa salva = taxaRepo.saveAndFlush(
                criarTaxaResidencial(
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );

        entityManager.clear();

        Optional<Taxa> resultado = taxaRepo.findById(salva.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getId()).isEqualTo(salva.getId());
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
                criarTaxaResidencial(
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 2, 1)
                ),
                criarTaxaResidencial(
                        LocalDate.of(2026, 2, 1),
                        null
                )
        ));

        entityManager.clear();

        List<Taxa> resultado = taxaRepo.findAll();

        assertThat(resultado).hasSize(2);
    }

    @Test
    @DisplayName("Deve excluir uma taxa pelo ID")
    void deveExcluirTaxa() {

        Taxa salva = taxaRepo.saveAndFlush(
                criarTaxaResidencial(
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );

        UUID id = salva.getId();

        taxaRepo.deleteById(id);
        taxaRepo.flush();

        assertThat(taxaRepo.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar a taxa vigente na data informada")
    void deveBuscarTaxaVigenteEm() {

        Taxa salva = taxaRepo.saveAndFlush(
                criarTaxaResidencial(
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 7, 1)
                )
        );

        Optional<Taxa> resultado = taxaRepo.buscarTaxaVigenteEm(
                "Neoenergia Coelba",
                SubgrupoTarifario.B1,
                ClasseConsumo.RESIDENCIAL,
                ModalidadeTarifaria.CONVENCIONAL,
                LocalDate.of(2026, 3, 1)
        );

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getId()).isEqualTo(salva.getId());
    }

    @Test
    @DisplayName("Deve considerar vigente a taxa sem fim de vigência")
    void deveBuscarTaxaSemFimDeVigencia() {

        Taxa salva = taxaRepo.saveAndFlush(
                criarTaxaResidencial(
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );

        Optional<Taxa> resultado = taxaRepo.buscarTaxaVigenteEm(
                "Neoenergia Coelba",
                SubgrupoTarifario.B1,
                ClasseConsumo.RESIDENCIAL,
                ModalidadeTarifaria.CONVENCIONAL,
                LocalDate.of(2026, 10, 1)
        );

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getId()).isEqualTo(salva.getId());
    }

    @Test
    @DisplayName("Deve considerar o início da vigência como inclusivo")
    void deveBuscarTaxaNoInicioDaVigencia() {

        Taxa salva = taxaRepo.saveAndFlush(
                criarTaxaResidencial(
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 6, 1)
                )
        );

        Optional<Taxa> resultado = taxaRepo.buscarTaxaVigenteEm(
                "Neoenergia Coelba",
                SubgrupoTarifario.B1,
                ClasseConsumo.RESIDENCIAL,
                ModalidadeTarifaria.CONVENCIONAL,
                LocalDate.of(2026, 1, 1)
        );

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getId()).isEqualTo(salva.getId());
    }

    @Test
    @DisplayName("Não deve considerar vigente a taxa no dia do fim da vigência")
    void naoDeveBuscarTaxaNoFimDaVigencia() {

        taxaRepo.saveAndFlush(
                criarTaxaResidencial(
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 6, 1)
                )
        );

        Optional<Taxa> resultado = taxaRepo.buscarTaxaVigenteEm(
                "Neoenergia Coelba",
                SubgrupoTarifario.B1,
                ClasseConsumo.RESIDENCIAL,
                ModalidadeTarifaria.CONVENCIONAL,
                LocalDate.of(2026, 6, 1)
        );

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Não deve buscar taxa antes do início da vigência")
    void naoDeveBuscarTaxaAntesDoInicio() {

        taxaRepo.saveAndFlush(
                criarTaxaResidencial(
                        LocalDate.of(2026, 4, 1),
                        null
                )
        );

        Optional<Taxa> resultado = taxaRepo.buscarTaxaVigenteEm(
                "Neoenergia Coelba",
                SubgrupoTarifario.B1,
                ClasseConsumo.RESIDENCIAL,
                ModalidadeTarifaria.CONVENCIONAL,
                LocalDate.of(2026, 3, 31)
        );

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Não deve encontrar taxa de outra distribuidora")
    void naoDeveEncontrarTaxaDeOutraDistribuidora() {

        taxaRepo.saveAndFlush(criarTaxaResidencial(
                LocalDate.of(2026, 1, 1),
                null
        ));

        Optional<Taxa> resultado = taxaRepo.buscarTaxaVigenteEm(
                "Outra Distribuidora",
                SubgrupoTarifario.B1,
                ClasseConsumo.RESIDENCIAL,
                ModalidadeTarifaria.CONVENCIONAL,
                LocalDate.of(2026, 3, 1)
        );

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Não deve encontrar taxa de outra combinação tarifária")
    void naoDeveEncontrarTaxaDeOutraCombinacaoTarifaria() {

        taxaRepo.saveAndFlush(criarTaxaResidencial(
                LocalDate.of(2026, 1, 1),
                null
        ));

        Optional<Taxa> resultado = taxaRepo.buscarTaxaVigenteEm(
                "Neoenergia Coelba",
                SubgrupoTarifario.B1,
                ClasseConsumo.COMERCIAL_SERVICOS,
                ModalidadeTarifaria.CONVENCIONAL,
                LocalDate.of(2026, 3, 1)
        );

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve encontrar taxa com período sobreposto")
    void deveBuscarTaxasComSobreposicao() {

        Taxa salva = taxaRepo.saveAndFlush(
                criarTaxaResidencial(
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 5, 1)
                )
        );

        List<Taxa> resultado = taxaRepo.buscarTaxasComSobreposicao(
                "Neoenergia Coelba",
                SubgrupoTarifario.B1,
                ClasseConsumo.RESIDENCIAL,
                ModalidadeTarifaria.CONVENCIONAL,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 6, 1)
        );

        assertThat(resultado)
                .extracting(Taxa::getId)
                .containsExactly(salva.getId());
    }

    @Test
    @DisplayName("Deve encontrar sobreposição quando o fim do intervalo consultado for nulo")
    void deveEncontrarSobreposicaoQuandoFimForNulo() {

        Taxa salva = taxaRepo.saveAndFlush(
                criarTaxaResidencial(
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );

        List<Taxa> resultado = taxaRepo.buscarTaxasComSobreposicao(
                "Neoenergia Coelba",
                SubgrupoTarifario.B1,
                ClasseConsumo.RESIDENCIAL,
                ModalidadeTarifaria.CONVENCIONAL,
                LocalDate.of(2026, 6, 1),
                null
        );

        assertThat(resultado)
                .extracting(Taxa::getId)
                .containsExactly(salva.getId());
    }

    @Test
    @DisplayName("Não deve encontrar sobreposição quando os períodos apenas se encostam")
    void naoDeveEncontrarSobreposicaoQuandoPeriodosApenasSeEncostarem() {

        taxaRepo.saveAndFlush(
                criarTaxaResidencial(
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 6, 1)
                )
        );

        List<Taxa> resultado = taxaRepo.buscarTaxasComSobreposicao(
                "Neoenergia Coelba",
                SubgrupoTarifario.B1,
                ClasseConsumo.RESIDENCIAL,
                ModalidadeTarifaria.CONVENCIONAL,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 12, 1)
        );

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Não deve encontrar sobreposição para outra combinação tarifária")
    void naoDeveEncontrarSobreposicaoDeOutraCombinacao() {

        taxaRepo.saveAndFlush(
                criarTaxaResidencial(
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 5, 1)
                )
        );

        List<Taxa> resultado = taxaRepo.buscarTaxasComSobreposicao(
                "Neoenergia Coelba",
                SubgrupoTarifario.B1,
                ClasseConsumo.COMERCIAL_SERVICOS,
                ModalidadeTarifaria.CONVENCIONAL,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 6, 1)
        );

        assertThat(resultado).isEmpty();
    }
}
