package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.Cliente;
import com.powermanager.powermanager.entity.Medidor;
import com.powermanager.powermanager.entity.UnidadeConsumidora;
import com.powermanager.powermanager.enums.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class MedidorRepoTest {

    @Autowired
    private MedidorRepo medidorRepo;

    @Autowired
    private ClienteRepo clienteRepo;

    @Autowired
    private EntityManager entityManager;

    private Cliente criarCliente(String email) {

        Cliente cliente = new Cliente();

        cliente.setNome("João da Silva");
        cliente.setEmail(email);
        cliente.setTelefone("71999999999");

        return clienteRepo.saveAndFlush(cliente);
    }

    private UnidadeConsumidora criarUnidadeConsumidora(Cliente cliente) {

        UnidadeConsumidora unidade = new UnidadeConsumidora();

        unidade.setCodigo("UC-001");
        unidade.setEndereco("Rua das Flores, 123");
        unidade.setCidade("Salvador");
        unidade.setEstado("BA");
        unidade.setCep("40000-000");
        unidade.setDistribuidora("Distribuidora A");
        unidade.setClasseConsumo(ClasseConsumo.RESIDENCIAL);
        unidade.setSubgrupoTarifario(SubgrupoTarifario.B1);
        unidade.setModalidadeTarifaria(ModalidadeTarifaria.CONVENCIONAL);
        unidade.setAtiva(true);
        unidade.setCliente(cliente);

        entityManager.persist(unidade);
        entityManager.flush();

        return unidade;
    }

    private Medidor criarMedidor(UnidadeConsumidora unidade, String numero, boolean ativo) {

        Medidor medidor = new Medidor();

        medidor.setNumero(numero);
        medidor.setTipoMedidor(TipoMedidor.ELETRONICO);
        medidor.setTipoLigacao(TipoLigacao.BIFASICO);
        medidor.setAtivo(ativo);
        medidor.setDataInstalacao(LocalDate.of(2026, 1, 10));
        medidor.setUnidadeConsumidora(unidade);

        if (!ativo) {
            medidor.setDataRetirada(LocalDate.of(2026, 6, 10));
        }

        return medidor;
    }

    @Test
    @DisplayName("Deve persistir um medidor e gerar seu UUID")
    void devePersistirMedidor() {

        Cliente cliente = criarCliente("joao@email.com");

        UnidadeConsumidora unidade = criarUnidadeConsumidora(cliente);

        Medidor salvo = medidorRepo.saveAndFlush(
                criarMedidor(unidade, "MED-001", true));

        assertThat(salvo.getId()).isNotNull();

        entityManager.clear();

        Medidor encontrado = medidorRepo.findById(salvo.getId()).orElseThrow();

        assertThat(encontrado.getNumero()).isEqualTo("MED-001");
        assertThat(encontrado.getTipoMedidor())
                .isEqualTo(TipoMedidor.ELETRONICO);
        assertThat(encontrado.getTipoLigacao())
                .isEqualTo(TipoLigacao.BIFASICO);
        assertThat(encontrado.getAtivo()).isTrue();
        assertThat(encontrado.getDataInstalacao())
                .isEqualTo(LocalDate.of(2026, 1, 10));
        assertThat(encontrado.getDataRetirada()).isNull();
        assertThat(encontrado.getUnidadeConsumidora().getId())
                .isEqualTo(unidade.getId());
    }

    @Test
    @DisplayName("Deve buscar um medidor pelo ID")
    void deveBuscarMedidorPorId() {

        UnidadeConsumidora unidade =
                criarUnidadeConsumidora(
                        criarCliente("joao@email.com"));

        Medidor salvo = medidorRepo.saveAndFlush(
                criarMedidor(unidade, "MED-002", true));

        entityManager.clear();

        Optional<Medidor> resultado =
                medidorRepo.findById(salvo.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getNumero())
                .isEqualTo("MED-002");
    }

    @Test
    @DisplayName("Não deve encontrar medidor com ID inexistente")
    void naoDeveEncontrarMedidorPorIdInexistente() {

        Optional<Medidor> resultado =
                medidorRepo.findById(UUID.randomUUID());

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar um medidor pelo número")
    void deveBuscarMedidorPorNumero() {

        UnidadeConsumidora unidade =
                criarUnidadeConsumidora(
                        criarCliente("joao@email.com"));

        medidorRepo.saveAndFlush(
                criarMedidor(unidade, "MED-003", true));

        entityManager.clear();

        Optional<Medidor> resultado =
                medidorRepo.findByNumero("MED-003");

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getNumero())
                .isEqualTo("MED-003");
    }

    @Test
    @DisplayName("Não deve encontrar medidor com número inexistente")
    void naoDeveEncontrarMedidorPorNumeroInexistente() {

        Optional<Medidor> resultado =
                medidorRepo.findByNumero("MED-INEXISTENTE");

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve listar todos os medidores persistidos")
    void deveListarMedidores() {

        UnidadeConsumidora unidade =
                criarUnidadeConsumidora(
                        criarCliente("joao@email.com"));

        medidorRepo.saveAllAndFlush(List.of(
                criarMedidor(unidade, "MED-004", true),
                criarMedidor(unidade, "MED-005", false)));

        entityManager.clear();

        List<Medidor> resultado = medidorRepo.findAll();

        assertThat(resultado)
                .hasSize(2)
                .extracting(Medidor::getNumero)
                .containsExactlyInAnyOrder("MED-004", "MED-005");
    }

    @Test
    @DisplayName("Deve verificar a existência de um medidor pelo ID")
    void deveVerificarExistenciaPorId() {

        UnidadeConsumidora unidade =
                criarUnidadeConsumidora(
                        criarCliente("joao@email.com"));

        Medidor salvo = medidorRepo.saveAndFlush(
                criarMedidor(unidade, "MED-006", true));

        assertThat(medidorRepo.existsById(salvo.getId())).isTrue();
        assertThat(medidorRepo.existsById(UUID.randomUUID())).isFalse();
    }

    @Test
    @DisplayName("Deve contar os medidores persistidos")
    void deveContarMedidores() {

        UnidadeConsumidora unidade =
                criarUnidadeConsumidora(
                        criarCliente("joao@email.com"));

        medidorRepo.saveAllAndFlush(List.of(
                criarMedidor(unidade, "MED-007", true),
                criarMedidor(unidade, "MED-008", false)));

        assertThat(medidorRepo.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve excluir um medidor pelo ID")
    void deveExcluirMedidorPorId() {

        UnidadeConsumidora unidade =
                criarUnidadeConsumidora(
                        criarCliente("joao@email.com"));

        Medidor salvo = medidorRepo.saveAndFlush(
                criarMedidor(unidade, "MED-009", true));

        UUID id = salvo.getId();

        medidorRepo.deleteById(id);
        medidorRepo.flush();

        assertThat(medidorRepo.findById(id)).isEmpty();
        assertThat(medidorRepo.existsById(id)).isFalse();
    }

    @Test
    @DisplayName("Não deve permitir números de medidor duplicados")
    void naoDevePermitirNumeroDuplicado() {

        UnidadeConsumidora unidade =
                criarUnidadeConsumidora(
                        criarCliente("joao@email.com"));

        medidorRepo.saveAndFlush(
                criarMedidor(unidade, "MED-010", true));

        Medidor duplicado =
                criarMedidor(unidade, "MED-010", true);

        assertThatThrownBy(
                () -> medidorRepo.saveAndFlush(duplicado)
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Deve recuperar o histórico de medidores de uma unidade consumidora")
    void deveBuscarHistoricoDeMedidoresPorUnidadeConsumidora() {

        UnidadeConsumidora unidade =
                criarUnidadeConsumidora(
                        criarCliente("joao@email.com"));

        medidorRepo.saveAllAndFlush(List.of(
                criarMedidor(unidade, "MED-011", false),
                criarMedidor(unidade, "MED-012", true)));

        entityManager.clear();

        List<Medidor> resultado =
                medidorRepo.findByUnidadeConsumidora_Id(unidade.getId());

        assertThat(resultado)
                .hasSize(2)
                .extracting(Medidor::getNumero)
                .containsExactlyInAnyOrder("MED-011", "MED-012");
    }

    @Test
    @DisplayName("Deve buscar somente o medidor ativo de uma unidade consumidora")
    void deveBuscarMedidorAtivoPorUnidadeConsumidora() {

        UnidadeConsumidora unidade =
                criarUnidadeConsumidora(
                        criarCliente("joao@email.com"));

        medidorRepo.saveAllAndFlush(List.of(
                criarMedidor(unidade, "MED-013", false),
                criarMedidor(unidade, "MED-014", true)));

        entityManager.clear();

        Optional<Medidor> resultado =
                medidorRepo.findByUnidadeConsumidora_IdAndAtivoTrue(
                        unidade.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getNumero())
                .isEqualTo("MED-014");
        assertThat(resultado.orElseThrow().getAtivo()).isTrue();
    }
}
