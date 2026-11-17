package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.Cliente;
import com.powermanager.powermanager.entity.Fatura;
import com.powermanager.powermanager.entity.UnidadeConsumidora;
import com.powermanager.powermanager.enums.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class FaturaRepoTest {

    @Autowired
    private FaturaRepo faturaRepo;

    @Autowired
    private ClienteRepo clienteRepo;

    @Autowired
    private UnidadeConsumidoraRepo unidadeConsumidoraRepo;

    @Autowired
    private EntityManager entityManager;

    private UnidadeConsumidora criarUnidadeConsumidora(String email, String codigo) {

        Cliente cliente = new Cliente();
        cliente.setNome("Cliente Teste");
        cliente.setEmail(email);
        cliente.setTelefone("71999999999");

        cliente = clienteRepo.saveAndFlush(cliente);

        UnidadeConsumidora unidade = new UnidadeConsumidora();
        unidade.setCodigo(codigo);
        unidade.setEndereco("Rua das Flores, 123");
        unidade.setCidade("Salvador");
        unidade.setEstado("BA");
        unidade.setCep("40000000");
        unidade.setDistribuidora("Neoenergia Coelba");
        unidade.setClasseConsumo(ClasseConsumo.RESIDENCIAL);
        unidade.setSubgrupoTarifario(SubgrupoTarifario.B1);
        unidade.setModalidadeTarifaria(ModalidadeTarifaria.CONVENCIONAL);
        unidade.setAtiva(true);
        unidade.setCliente(cliente);

        return unidadeConsumidoraRepo.saveAndFlush(unidade);
    }

    private Fatura criarFatura(UnidadeConsumidora unidade, LocalDate competencia) {

        Fatura fatura = new Fatura();

        fatura.setUnidadeConsumidora(unidade);
        fatura.setCompetencia(competencia);

        fatura.setInicioPeriodo(competencia);
        fatura.setFimPeriodo(competencia.plusMonths(1).minusDays(1));

        fatura.setConsumoKwh(new BigDecimal("100.000"));
        fatura.setOrigemConsumo(OrigemConsumo.MANUAL);

        fatura.setTarifaEnergiaTE(new BigDecimal("0.300000"));
        fatura.setTarifaUsoDistribuicaoTUSD(new BigDecimal("0.500000"));

        fatura.setValorTE(new BigDecimal("30.00"));
        fatura.setValorTUSD(new BigDecimal("50.00"));

        fatura.setValorBandeira(new BigDecimal("5.00"));
        fatura.setValorSubtotal(new BigDecimal("85.00"));
        fatura.setValorTotal(new BigDecimal("85.00"));

        fatura.setDataEmissao(competencia.plusMonths(1));
        fatura.setDataVencimento(competencia.plusMonths(1).plusDays(10));

        fatura.setStatus(StatusFatura.PENDENTE);

        return fatura;
    }

    @Test
    @DisplayName("Deve persistir uma fatura corretamente")
    void devePersistirFatura() {

        UnidadeConsumidora unidade = criarUnidadeConsumidora(
                "cliente1@email.com",
                "UC-001"
        );

        Fatura salva = faturaRepo.saveAndFlush(
                criarFatura(unidade, LocalDate.of(2026, 1, 1))
        );

        assertThat(salva.getId()).isNotNull();

        entityManager.clear();

        Fatura encontrada = faturaRepo.findById(salva.getId()).orElseThrow();

        assertThat(encontrada.getCompetencia())
                .isEqualTo(LocalDate.of(2026, 1, 1));

        assertThat(encontrada.getConsumoKwh())
                .isEqualByComparingTo("100.000");

        assertThat(encontrada.getValorTotal())
                .isEqualByComparingTo("85.00");

        assertThat(encontrada.getOrigemConsumo())
                .isEqualTo(OrigemConsumo.MANUAL);

        assertThat(encontrada.getStatus())
                .isEqualTo(StatusFatura.PENDENTE);

        assertThat(encontrada.getUnidadeConsumidora().getId())
                .isEqualTo(unidade.getId());
    }

    @Test
    @DisplayName("Deve buscar uma fatura pelo ID")
    void deveBuscarFaturaPorId() {

        UnidadeConsumidora unidade = criarUnidadeConsumidora(
                "cliente2@email.com",
                "UC-002"
        );

        Fatura salva = faturaRepo.saveAndFlush(
                criarFatura(unidade, LocalDate.of(2026, 2, 1))
        );

        entityManager.clear();

        Optional<Fatura> resultado = faturaRepo.findById(salva.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getCompetencia())
                .isEqualTo(LocalDate.of(2026, 2, 1));
    }

    @Test
    @DisplayName("Não deve encontrar fatura com ID inexistente")
    void naoDeveEncontrarFaturaPorIdInexistente() {

        Optional<Fatura> resultado = faturaRepo.findById(UUID.randomUUID());

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve listar as faturas de uma unidade consumidora")
    void deveBuscarFaturasPorUnidadeConsumidora() {

        UnidadeConsumidora unidade = criarUnidadeConsumidora(
                "cliente3@email.com",
                "UC-003"
        );

        faturaRepo.saveAllAndFlush(List.of(
                criarFatura(unidade, LocalDate.of(2026, 1, 1)),
                criarFatura(unidade, LocalDate.of(2026, 2, 1))
        ));

        UnidadeConsumidora outraUnidade = criarUnidadeConsumidora(
                "cliente4@email.com",
                "UC-004"
        );

        faturaRepo.saveAndFlush(
                criarFatura(outraUnidade, LocalDate.of(2026, 1, 1))
        );

        entityManager.clear();

        List<Fatura> resultado =
                faturaRepo.findByUnidadeConsumidora_Id(unidade.getId());

        assertThat(resultado)
                .hasSize(2)
                .allSatisfy(fatura ->
                        assertThat(fatura.getUnidadeConsumidora().getId())
                                .isEqualTo(unidade.getId())
                );
    }

    @Test
    @DisplayName("Deve ordenar as faturas por competência decrescente")
    void deveOrdenarFaturasPorCompetenciaDecrescente() {

        UnidadeConsumidora unidade = criarUnidadeConsumidora(
                "cliente5@email.com",
                "UC-005"
        );

        faturaRepo.saveAllAndFlush(List.of(
                criarFatura(unidade, LocalDate.of(2026, 1, 1)),
                criarFatura(unidade, LocalDate.of(2026, 3, 1)),
                criarFatura(unidade, LocalDate.of(2026, 2, 1))
        ));

        entityManager.clear();

        List<Fatura> resultado =
                faturaRepo.findByUnidadeConsumidora_IdOrderByCompetenciaDesc(
                        unidade.getId()
                );

        assertThat(resultado)
                .extracting(Fatura::getCompetencia)
                .containsExactly(
                        LocalDate.of(2026, 3, 1),
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 1, 1)
                );
    }

    @Test
    @DisplayName("Deve buscar fatura por unidade consumidora e competência")
    void deveBuscarFaturaPorUnidadeECompetencia() {

        UnidadeConsumidora unidade = criarUnidadeConsumidora(
                "cliente6@email.com",
                "UC-006"
        );

        LocalDate competencia = LocalDate.of(2026, 4, 1);

        Fatura salva = faturaRepo.saveAndFlush(
                criarFatura(unidade, competencia)
        );

        entityManager.clear();

        Optional<Fatura> resultado =
                faturaRepo.findByUnidadeConsumidora_IdAndCompetencia(
                        unidade.getId(),
                        competencia
                );

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getId()).isEqualTo(salva.getId());
    }

    @Test
    @DisplayName("Não deve encontrar fatura para competência inexistente")
    void naoDeveEncontrarFaturaPorCompetenciaInexistente() {

        UnidadeConsumidora unidade = criarUnidadeConsumidora(
                "cliente7@email.com",
                "UC-007"
        );

        Optional<Fatura> resultado =
                faturaRepo.findByUnidadeConsumidora_IdAndCompetencia(
                        unidade.getId(),
                        LocalDate.of(2026, 5, 1)
                );

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar faturas pelo status")
    void deveBuscarFaturasPorStatus() {

        UnidadeConsumidora unidade = criarUnidadeConsumidora(
                "cliente8@email.com",
                "UC-008"
        );

        Fatura pendente = criarFatura(
                unidade,
                LocalDate.of(2026, 1, 1)
        );

        Fatura paga = criarFatura(
                unidade,
                LocalDate.of(2026, 2, 1)
        );

        paga.setStatus(StatusFatura.PAGO);
        paga.setDataPagamento(LocalDate.of(2026, 3, 5));

        faturaRepo.saveAllAndFlush(List.of(pendente, paga));

        entityManager.clear();

        List<Fatura> resultado =
                faturaRepo.findByStatus(StatusFatura.PENDENTE);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getStatus())
                .isEqualTo(StatusFatura.PENDENTE);
    }

    @Test
    @DisplayName("Deve contar as faturas persistidas")
    void deveContarFaturas() {

        UnidadeConsumidora unidade = criarUnidadeConsumidora(
                "cliente9@email.com",
                "UC-009"
        );

        faturaRepo.saveAllAndFlush(List.of(
                criarFatura(unidade, LocalDate.of(2026, 1, 1)),
                criarFatura(unidade, LocalDate.of(2026, 2, 1))
        ));

        assertThat(faturaRepo.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve excluir uma fatura pelo ID")
    void deveExcluirFaturaPorId() {

        UnidadeConsumidora unidade = criarUnidadeConsumidora(
                "cliente10@email.com",
                "UC-010"
        );

        Fatura salva = faturaRepo.saveAndFlush(
                criarFatura(unidade, LocalDate.of(2026, 1, 1))
        );

        UUID id = salva.getId();

        faturaRepo.deleteById(id);
        faturaRepo.flush();

        assertThat(faturaRepo.findById(id)).isEmpty();
        assertThat(faturaRepo.existsById(id)).isFalse();
    }

    @Test
    @DisplayName("Não deve permitir duas faturas da mesma unidade na mesma competência")
    void naoDevePermitirFaturasDuplicadasNaMesmaCompetencia() {

        UnidadeConsumidora unidade = criarUnidadeConsumidora(
                "cliente11@email.com",
                "UC-011"
        );

        LocalDate competencia = LocalDate.of(2026, 1, 1);

        faturaRepo.saveAndFlush(criarFatura(unidade, competencia));

        Fatura duplicada = criarFatura(unidade, competencia);

        assertThatThrownBy(() -> faturaRepo.saveAndFlush(duplicada))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Deve permitir faturas de unidades diferentes na mesma competência")
    void devePermitirFaturasDeUnidadesDiferentesNaMesmaCompetencia() {

        UnidadeConsumidora primeiraUnidade = criarUnidadeConsumidora(
                "cliente12@email.com",
                "UC-012"
        );

        UnidadeConsumidora segundaUnidade = criarUnidadeConsumidora(
                "cliente13@email.com",
                "UC-013"
        );

        LocalDate competencia = LocalDate.of(2026, 1, 1);

        faturaRepo.saveAllAndFlush(List.of(
                criarFatura(primeiraUnidade, competencia),
                criarFatura(segundaUnidade, competencia)
        ));

        assertThat(faturaRepo.count()).isEqualTo(2);
    }
}