package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.Cliente;
import com.powermanager.powermanager.entity.Fatura;
import com.powermanager.powermanager.entity.Medidor;
import com.powermanager.powermanager.entity.enums.StatusFatura;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
public class FaturaRepoTest {

    @Autowired
    private FaturaRepo faturaRepo;

    @Autowired
    private MedidorRepo medidorRepo;

    @Autowired
    private ClienteRepo clienteRepo;

    private Cliente criarCliente(String email){

        Cliente cliente = new Cliente();

        cliente.setNome("João da Silva");
        cliente.setEndereco("Rua das Flores, 123");
        cliente.setCidade("Salvador");
        cliente.setEstado("Bahia");
        cliente.setEmail(email);
        cliente.setTelefone("71999999999");

        return clienteRepo.saveAndFlush(cliente);
    }

    private Medidor criarMedidor(String numero, String email){

        Medidor medidor = new Medidor();

        medidor.setNumero(numero);
        medidor.setLocalizacao("Residencial");
        medidor.setTipo("Digital");
        medidor.setCodigoFase("BIFASICO");
        medidor.setTipoFatura("RESIDENCIAL");
        medidor.setDias(30);
        medidor.setCliente(criarCliente(email));

        return medidorRepo.saveAndFlush(medidor);
    }

    private Fatura criarFatura(Medidor medidor, LocalDate competencia){

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
    @DisplayName("Deve persistir uma fatura e gerar UUID")
    void devePersistirFatura(){

        Medidor medidor = criarMedidor(
                "MED-001", "joao@email.com");

        Fatura salva = faturaRepo.saveAndFlush(
                criarFatura(medidor, LocalDate.of(2026, 1, 1)));

        assertThat(salva.getId()).isNotNull();
        assertThat(faturaRepo.findById(salva.getId())).isPresent();
    }

    @Test
    @DisplayName("Deve buscar uma fatura por ID")
    void deveBuscarFaturaPorId(){

        Medidor medidor = criarMedidor("MED-002", "maria@email.com");

        Fatura salva = faturaRepo.saveAndFlush(
                criarFatura(medidor, LocalDate.of(2026, 2, 1)));

        Optional<Fatura> resultado = faturaRepo.findById(salva.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getCompetencia())
                .isEqualTo(LocalDate.of(2026, 2, 1));
    }

    @Test
    @DisplayName("Não deve encontrar fatura por ID inexistente")
    void naoDeveEncontrarFaturaPorIdInexistente() {
        assertThat(faturaRepo.findById(UUID.randomUUID()))
                .isEmpty();
    }

    @Test
    @DisplayName("Deve listar as faturas de um medidor")
    void deveBuscarFaturasPorMedidor(){

        Medidor medidor = criarMedidor("MED-003", "cliente3@email.com");

        faturaRepo.saveAndFlush(
                criarFatura(medidor, LocalDate.of(2026, 1, 1)));

        faturaRepo.saveAndFlush(
                criarFatura(medidor, LocalDate.of(2026, 2, 1)));

        Medidor outroMedidor = criarMedidor("MED-004", "cliente4@email.com");

        faturaRepo.saveAndFlush(
                criarFatura(outroMedidor, LocalDate.of(2026, 1, 1)));

        assertThat(faturaRepo.findByMedidor(medidor))
                .hasSize(2)
                .allSatisfy(fatura -> assertThat(fatura.getMedidor().getId())
                                .isEqualTo(medidor.getId()));
    }

    @Test
    @DisplayName("Deve ordenar faturas por competência decrescente")
    void deveOrdenarFaturasPorCompetenciaDecrescente(){

        Medidor medidor = criarMedidor("MED-005", "cliente5@email.com");

        faturaRepo.saveAndFlush(
                criarFatura(medidor, LocalDate.of(2026, 1, 1)));

        faturaRepo.saveAndFlush(
                criarFatura(medidor, LocalDate.of(2026, 3, 1)));

        faturaRepo.saveAndFlush(
                criarFatura(medidor, LocalDate.of(2026, 2, 1)));

        assertThat(
                faturaRepo.findByMedidorOrderByCompetenciaDesc(medidor))
                .extracting(Fatura::getCompetencia)
                .containsExactly(
                        LocalDate.of(2026, 3, 1),
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 1, 1));
    }

    @Test
    @DisplayName("Deve buscar fatura por medidor e competência")
    void deveBuscarFaturaPorMedidorECompetencia(){

        Medidor medidor = criarMedidor("MED-006", "cliente6@email.com");

        LocalDate competencia = LocalDate.of(2026, 4, 1);

        faturaRepo.saveAndFlush(criarFatura(medidor, competencia));

        Optional<Fatura> resultado = faturaRepo.findByMedidorAndCompetencia(medidor, competencia);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getCompetencia()).isEqualTo(competencia);
    }

    @Test
    @DisplayName("Não deve encontrar fatura para competência inexistente")
    void naoDeveEncontrarFaturaPorCompetenciaInexistente(){

        Medidor medidor = criarMedidor("MED-007", "cliente7@email.com");

        assertThat(faturaRepo.findByMedidorAndCompetencia(medidor,
                LocalDate.of(2026, 5, 1))).isEmpty();
    }

    @Test
    @DisplayName("Deve contar as faturas persistidas")
    void deveContarFaturas(){

        Medidor medidor = criarMedidor("MED-008", "cliente8@email.com");

        faturaRepo.saveAndFlush(
                criarFatura(medidor, LocalDate.of(2026, 1, 1)));

        faturaRepo.saveAndFlush(
                criarFatura(medidor, LocalDate.of(2026, 2, 1)));

        assertThat(faturaRepo.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve excluir uma fatura por ID")
    void deveExcluirFaturaPorId(){

        Medidor medidor = criarMedidor("MED-009", "cliente9@email.com");

        Fatura salva = faturaRepo.saveAndFlush(
                criarFatura(medidor, LocalDate.of(2026, 1, 1)));

        UUID id = salva.getId();

        faturaRepo.deleteById(id);
        faturaRepo.flush();

        assertThat(faturaRepo.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("Não deve permitir duas faturas do mesmo medidor na mesma competência")
    void naoDevePermitirFaturasDuplicadasNaMesmaCompetencia(){

        Medidor medidor = criarMedidor("MED-010", "cliente10@email.com");

        LocalDate competencia = LocalDate.of(2026, 1, 1);

        faturaRepo.saveAndFlush(criarFatura(medidor, competencia));

        Fatura duplicada = criarFatura(medidor, competencia);

        assertThatThrownBy(() -> faturaRepo.saveAndFlush(duplicada))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
