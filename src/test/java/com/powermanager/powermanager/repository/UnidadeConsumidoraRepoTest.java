package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.Cliente;
import com.powermanager.powermanager.entity.UnidadeConsumidora;
import com.powermanager.powermanager.enums.ClasseConsumo;
import com.powermanager.powermanager.enums.ModalidadeTarifaria;
import com.powermanager.powermanager.enums.SubgrupoTarifario;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class UnidadeConsumidoraRepoTest {

    @Autowired
    private UnidadeConsumidoraRepo unidadeConsumidoraRepo;

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

    private UnidadeConsumidora criarUnidadeConsumidora(Cliente cliente, String codigo, String distribuidora) {

        UnidadeConsumidora unidade = new UnidadeConsumidora();

        unidade.setCodigo(codigo);
        unidade.setEndereco("Rua das Flores, 123");
        unidade.setCidade("Salvador");
        unidade.setEstado("BA");
        unidade.setCep("40000-000");
        unidade.setDistribuidora(distribuidora);
        unidade.setClasseConsumo(ClasseConsumo.RESIDENCIAL);
        unidade.setSubgrupoTarifario(SubgrupoTarifario.B1);
        unidade.setModalidadeTarifaria(ModalidadeTarifaria.CONVENCIONAL);
        unidade.setAtiva(true);
        unidade.setCliente(cliente);

        return unidade;
    }

    @Test
    @DisplayName("Deve persistir uma unidade consumidora e gerar seu UUID")
    void devePersistirUnidadeConsumidora() {

        Cliente cliente = criarCliente("joao@email.com");

        UnidadeConsumidora unidade = criarUnidadeConsumidora(
                cliente,
                "UC-001",
                "Distribuidora A");

        UnidadeConsumidora salva =
                unidadeConsumidoraRepo.saveAndFlush(unidade);

        assertThat(salva.getId()).isNotNull();

        entityManager.clear();

        UnidadeConsumidora encontrada =
                unidadeConsumidoraRepo.findById(salva.getId()).orElseThrow();

        assertThat(encontrada.getCodigo()).isEqualTo("UC-001");
        assertThat(encontrada.getEndereco()).isEqualTo("Rua das Flores, 123");
        assertThat(encontrada.getCidade()).isEqualTo("Salvador");
        assertThat(encontrada.getEstado()).isEqualTo("BA");
        assertThat(encontrada.getCep()).isEqualTo("40000-000");
        assertThat(encontrada.getDistribuidora()).isEqualTo("Distribuidora A");
        assertThat(encontrada.getClasseConsumo()).isEqualTo(ClasseConsumo.RESIDENCIAL);
        assertThat(encontrada.getSubgrupoTarifario()).isEqualTo(SubgrupoTarifario.B1);
        assertThat(encontrada.getModalidadeTarifaria())
                .isEqualTo(ModalidadeTarifaria.CONVENCIONAL);
        assertThat(encontrada.getAtiva()).isTrue();
        assertThat(encontrada.getCliente().getId()).isEqualTo(cliente.getId());
    }

    @Test
    @DisplayName("Deve buscar uma unidade consumidora pelo ID")
    void deveBuscarUnidadeConsumidoraPorId() {

        Cliente cliente = criarCliente("joao@email.com");

        UnidadeConsumidora salva = unidadeConsumidoraRepo.saveAndFlush(
                criarUnidadeConsumidora(cliente, "UC-002", "Distribuidora A"));

        entityManager.clear();

        Optional<UnidadeConsumidora> resultado = unidadeConsumidoraRepo.findById(salva.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getCodigo()).isEqualTo("UC-002");
    }

    @Test
    @DisplayName("Não deve encontrar unidade consumidora com ID inexistente")
    void naoDeveEncontrarUnidadeConsumidoraPorIdInexistente() {

        Optional<UnidadeConsumidora> resultado = unidadeConsumidoraRepo.findById(UUID.randomUUID());

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve listar as unidades consumidoras persistidas")
    void deveListarUnidadesConsumidoras() {

        Cliente cliente = criarCliente("joao@email.com");

        unidadeConsumidoraRepo.saveAllAndFlush(List.of(
                criarUnidadeConsumidora(cliente, "UC-003", "Distribuidora A"),
                criarUnidadeConsumidora(cliente, "UC-004", "Distribuidora A")
        ));

        entityManager.clear();

        List<UnidadeConsumidora> resultado = unidadeConsumidoraRepo.findAll();

        assertThat(resultado)
                .hasSize(2)
                .extracting(UnidadeConsumidora::getCodigo)
                .containsExactlyInAnyOrder("UC-003", "UC-004");
    }

    @Test
    @DisplayName("Deve buscar unidades consumidoras pelo ID do cliente")
    void deveBuscarUnidadesConsumidorasPorCliente() {

        Cliente primeiroCliente = criarCliente("joao@email.com");
        Cliente segundoCliente = criarCliente("maria@email.com");

        unidadeConsumidoraRepo.saveAllAndFlush(List.of(
                criarUnidadeConsumidora(
                        primeiroCliente, "UC-005", "Distribuidora A"
                ),
                criarUnidadeConsumidora(
                        primeiroCliente, "UC-006", "Distribuidora A"
                ),
                criarUnidadeConsumidora(
                        segundoCliente, "UC-007", "Distribuidora A"
                )));

        entityManager.clear();

        List<UnidadeConsumidora> resultado = unidadeConsumidoraRepo.findByCliente_Id(primeiroCliente.getId());

        assertThat(resultado)
                .hasSize(2)
                .allSatisfy(unidade ->
                        assertThat(unidade.getCliente().getId())
                                .isEqualTo(primeiroCliente.getId())
                )
                .extracting(UnidadeConsumidora::getCodigo)
                .containsExactlyInAnyOrder("UC-005", "UC-006");
    }

    @Test
    @DisplayName("Deve buscar unidade pela distribuidora e pelo código")
    void deveBuscarPorDistribuidoraECodigo() {

        Cliente cliente = criarCliente("joao@email.com");

        UnidadeConsumidora salva = unidadeConsumidoraRepo.saveAndFlush(
                criarUnidadeConsumidora(
                        cliente, "UC-008", "Distribuidora A"));

        entityManager.clear();

        Optional<UnidadeConsumidora> resultado =
                unidadeConsumidoraRepo.findByDistribuidoraAndCodigo(
                        "Distribuidora A",
                        "UC-008");

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getId()).isEqualTo(salva.getId());
    }

    @Test
    @DisplayName("Não deve encontrar unidade com distribuidora e código inexistentes")
    void naoDeveEncontrarPorDistribuidoraECodigoInexistentes() {

        Optional<UnidadeConsumidora> resultado =
                unidadeConsumidoraRepo.findByDistribuidoraAndCodigo(
                        "Distribuidora Inexistente",
                        "UC-999"
                );

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve verificar a existência pela distribuidora e pelo código")
    void deveVerificarExistenciaPorDistribuidoraECodigo() {

        Cliente cliente = criarCliente("joao@email.com");

        unidadeConsumidoraRepo.saveAndFlush(
                criarUnidadeConsumidora(
                        cliente, "UC-009", "Distribuidora A"
                ));

        assertThat(
                unidadeConsumidoraRepo.existsByDistribuidoraAndCodigo(
                        "Distribuidora A",
                        "UC-009"
                )).isTrue();

        assertThat(
                unidadeConsumidoraRepo.existsByDistribuidoraAndCodigo(
                        "Distribuidora A",
                        "UC-999")).isFalse();
    }

    @Test
    @DisplayName("Não deve permitir códigos duplicados na mesma distribuidora")
    void naoDevePermitirCodigoDuplicadoNaMesmaDistribuidora() {

        Cliente cliente = criarCliente("joao@email.com");

        unidadeConsumidoraRepo.saveAndFlush(
                criarUnidadeConsumidora(
                        cliente, "UC-010", "Distribuidora A"));

        UnidadeConsumidora duplicada = criarUnidadeConsumidora(
                cliente, "UC-010", "Distribuidora A");

        assertThatThrownBy(
                () -> unidadeConsumidoraRepo.saveAndFlush(duplicada))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Deve excluir uma unidade consumidora pelo ID")
    void deveExcluirUnidadeConsumidoraPorId() {

        Cliente cliente = criarCliente("joao@email.com");

        UnidadeConsumidora salva = unidadeConsumidoraRepo.saveAndFlush(
                criarUnidadeConsumidora(
                        cliente, "UC-011", "Distribuidora A"
                ));

        UUID id = salva.getId();

        unidadeConsumidoraRepo.deleteById(id);
        unidadeConsumidoraRepo.flush();

        assertThat(unidadeConsumidoraRepo.findById(id)).isEmpty();
        assertThat(unidadeConsumidoraRepo.existsById(id)).isFalse();
    }
}