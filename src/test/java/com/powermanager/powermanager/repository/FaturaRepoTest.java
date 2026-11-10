package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.Cliente;
import com.powermanager.powermanager.entity.Fatura;
import com.powermanager.powermanager.entity.Medidor;
import com.powermanager.powermanager.enums.ClasseConsumo;
import com.powermanager.powermanager.enums.StatusFatura;
import com.powermanager.powermanager.enums.TipoLigacao;
import com.powermanager.powermanager.enums.TipoMedidor;

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
    private MedidorRepo medidorRepo;

    @Autowired
    private ClienteRepo clienteRepo;

    @Autowired
    private EntityManager entityManager;

    private Cliente criarCliente(String email) {

        Cliente cliente = new Cliente();

        cliente.setNome("João da Silva");
        cliente.setEndereco("Rua das Flores, 123");
        cliente.setCidade("Salvador");
        cliente.setEstado("Bahia");
        cliente.setEmail(email);
        cliente.setTelefone("71999999999");

        return clienteRepo.saveAndFlush(cliente);
    }

    private Medidor criarMedidor(String numero, String email) {

        Medidor medidor = new Medidor();

        medidor.setNumero(numero);
        medidor.setLocalizacao("Residencial");
        medidor.setTipoMedidor(TipoMedidor.ELETRONICO);
        medidor.setTipoLigacao(TipoLigacao.BIFASICO);
        medidor.setClasseConsumo(ClasseConsumo.RESIDENCIAL);
        medidor.setDias(30);
        medidor.setCliente(criarCliente(email));

        return medidorRepo.saveAndFlush(medidor);
    }

    private Fatura criarFatura(Medidor medidor, LocalDate competencia) {

        Fatura fatura = new Fatura();

        fatura.setCompetencia(competencia);
        fatura.setUnidadesConsumidas(new BigDecimal("100.00"));

        fatura.setCustoPorUnidadeAplicado(new BigDecimal("0.80"));
        fatura.setAluguelMedidorAplicado(new BigDecimal("10.00"));
        fatura.setTaxaServicoAplicada(new BigDecimal("5.00"));
        fatura.setImpostoServicoAplicado(new BigDecimal("2.00"));
        fatura.setCessAplicado(new BigDecimal("1.00"));
        fatura.setTaxaFixaAplicada(new BigDecimal("3.00"));

        fatura.setValorTotal(new BigDecimal("101.00"));
        fatura.setStatusFatura(StatusFatura.PENDENTE);
        fatura.setMedidor(medidor);

        return fatura;
    }

    @Test
    @DisplayName("Deve persistir uma fatura corretamente")
    void devePersistirFatura() {

        Medidor medidor = criarMedidor("MED-001", "joao@email.com");

        Fatura salva = faturaRepo.saveAndFlush(criarFatura(medidor, LocalDate.of(2026, 1, 1)));

        assertThat(salva.getId()).isNotNull();

        entityManager.clear();

        Fatura encontrada = faturaRepo.findById(salva.getId()).orElseThrow();

        assertThat(encontrada.getCompetencia()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(encontrada.getUnidadesConsumidas()).isEqualByComparingTo("100.00");
        assertThat(encontrada.getCustoPorUnidadeAplicado()).isEqualByComparingTo("0.80");
        assertThat(encontrada.getAluguelMedidorAplicado()).isEqualByComparingTo("10.00");
        assertThat(encontrada.getTaxaServicoAplicada()).isEqualByComparingTo("5.00");
        assertThat(encontrada.getImpostoServicoAplicado()).isEqualByComparingTo("2.00");
        assertThat(encontrada.getCessAplicado()).isEqualByComparingTo("1.00");
        assertThat(encontrada.getTaxaFixaAplicada()).isEqualByComparingTo("3.00");
        assertThat(encontrada.getValorTotal()).isEqualByComparingTo("101.00");
        assertThat(encontrada.getStatusFatura()).isEqualTo(StatusFatura.PENDENTE);
        assertThat(encontrada.getMedidor().getId()).isEqualTo(medidor.getId());
    }

    @Test
    @DisplayName("Deve buscar uma fatura pelo ID")
    void deveBuscarFaturaPorId() {

        Medidor medidor = criarMedidor("MED-002", "maria@email.com");

        Fatura salva = faturaRepo.saveAndFlush(criarFatura(medidor, LocalDate.of(2026, 2, 1)));

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
    @DisplayName("Deve listar todas as faturas de um medidor")
    void deveBuscarFaturasPorMedidor() {

        Medidor medidor = criarMedidor("MED-003", "cliente3@email.com");

        faturaRepo.saveAllAndFlush(List.of(
                criarFatura(medidor, LocalDate.of(2026, 1, 1)),
                criarFatura(medidor, LocalDate.of(2026, 2, 1))));

        Medidor outroMedidor = criarMedidor("MED-004", "cliente4@email.com");

        faturaRepo.saveAndFlush(
                criarFatura(outroMedidor, LocalDate.of(2026, 1, 1)));

        entityManager.clear();

        List<Fatura> resultado = faturaRepo.findByMedidor(medidor);

        assertThat(resultado)
                .hasSize(2)
                .allSatisfy(fatura -> assertThat(fatura.getMedidor().getId())
                                .isEqualTo(medidor.getId()));
    }

    @Test
    @DisplayName("Deve ordenar faturas por competência decrescente")
    void deveOrdenarFaturasPorCompetenciaDecrescente() {

        Medidor medidor = criarMedidor("MED-005", "cliente5@email.com");

        faturaRepo.saveAllAndFlush(List.of(
                criarFatura(medidor, LocalDate.of(2026, 1, 1)),
                criarFatura(medidor, LocalDate.of(2026, 3, 1)),
                criarFatura(medidor, LocalDate.of(2026, 2, 1))));

        entityManager.clear();

        List<Fatura> resultado = faturaRepo.findByMedidorOrderByCompetenciaDesc(medidor);

        assertThat(resultado)
                .extracting(Fatura::getCompetencia)
                .containsExactly(
                        LocalDate.of(2026, 3, 1),
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 1, 1));
    }

    @Test
    @DisplayName("Deve buscar fatura por medidor e competência")
    void deveBuscarFaturaPorMedidorECompetencia() {

        Medidor medidor = criarMedidor("MED-006", "cliente6@email.com");

        LocalDate competencia = LocalDate.of(2026, 4, 1);

        Fatura salva = faturaRepo.saveAndFlush(criarFatura(medidor, competencia));

        entityManager.clear();

        Optional<Fatura> resultado = faturaRepo.findByMedidorAndCompetencia(medidor, competencia);

        assertThat(resultado).isPresent();

        assertThat(resultado.orElseThrow().getId()).isEqualTo(salva.getId());
    }

    @Test
    @DisplayName("Não deve encontrar fatura para competência inexistente")
    void naoDeveEncontrarFaturaPorCompetenciaInexistente() {

        Medidor medidor = criarMedidor("MED-007", "cliente7@email.com");

        Optional<Fatura> resultado = faturaRepo.findByMedidorAndCompetencia(medidor,
                LocalDate.of(2026, 5, 1));

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve contar as faturas persistidas")
    void deveContarFaturas() {

        Medidor medidor = criarMedidor("MED-008", "cliente8@email.com");

        faturaRepo.saveAllAndFlush(List.of(
                criarFatura(medidor, LocalDate.of(2026, 1, 1)),
                criarFatura(medidor, LocalDate.of(2026, 2, 1))));

        assertThat(faturaRepo.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve excluir uma fatura pelo ID")
    void deveExcluirFaturaPorId() {

        Medidor medidor = criarMedidor("MED-009", "cliente9@email.com");

        Fatura salva = faturaRepo.saveAndFlush(criarFatura(medidor, LocalDate.of(2026, 1, 1)));

        UUID id = salva.getId();

        faturaRepo.deleteById(id);
        faturaRepo.flush();

        assertThat(faturaRepo.findById(id)).isEmpty();
        assertThat(faturaRepo.existsById(id)).isFalse();
    }

    @Test
    @DisplayName("Não deve permitir duas faturas do mesmo medidor na mesma competência")
    void naoDevePermitirFaturasDuplicadasNaMesmaCompetencia() {

        Medidor medidor = criarMedidor("MED-010", "cliente10@email.com");

        LocalDate competencia = LocalDate.of(2026, 1, 1);

        faturaRepo.saveAndFlush(criarFatura(medidor, competencia));

        Fatura duplicada = criarFatura(medidor, competencia);

        assertThatThrownBy(() -> faturaRepo.saveAndFlush(duplicada))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Deve permitir faturas de medidores diferentes na mesma competência")
    void devePermitirFaturasDeMedidoresDiferentesNaMesmaCompetencia() {

        Medidor primeiroMedidor = criarMedidor("MED-011", "cliente11@email.com");

        Medidor segundoMedidor = criarMedidor("MED-012", "cliente12@email.com");

        LocalDate competencia = LocalDate.of(2026, 1, 1);

        faturaRepo.saveAllAndFlush(List.of(criarFatura(primeiroMedidor, competencia),
                criarFatura(segundoMedidor, competencia)));

        assertThat(faturaRepo.count()).isEqualTo(2);
    }
}