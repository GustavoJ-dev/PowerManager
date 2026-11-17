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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ClienteRepoTest {

    @Autowired
    private ClienteRepo clienteRepo;

    @Autowired
    private EntityManager entityManager;

    private Cliente criarCliente(String nome, String email) {

        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        cliente.setEmail(email);
        cliente.setTelefone("71999999999");
        return cliente;
    }

    private UnidadeConsumidora criarUnidadeConsumidora(Cliente cliente, String codigo) {

        UnidadeConsumidora unidade = new UnidadeConsumidora();
        unidade.setCodigo(codigo);
        unidade.setEndereco("Rua das Flores, 123");
        unidade.setCidade("Salvador");
        unidade.setEstado("BA");
        unidade.setCep("40000-000");
        unidade.setDistribuidora("Distribuidora Exemplo");
        unidade.setClasseConsumo(ClasseConsumo.RESIDENCIAL);
        unidade.setSubgrupoTarifario(SubgrupoTarifario.B1);
        unidade.setModalidadeTarifaria(ModalidadeTarifaria.CONVENCIONAL);
        unidade.setAtiva(true);
        unidade.setCliente(cliente);
        return unidade;
    }

    @Test
    @DisplayName("Deve persistir um cliente e gerar seu UUID")
    void devePersistirClienteEGerarUUID() {

        Cliente cliente = criarCliente("João da Silva", "joao@email.com");

        Cliente salvo = clienteRepo.saveAndFlush(cliente);

        assertThat(salvo.getId()).isNotNull();

        entityManager.clear();

        Cliente encontrado = clienteRepo.findById(salvo.getId()).orElseThrow();
        assertThat(encontrado.getNome()).isEqualTo("João da Silva");
        assertThat(encontrado.getEmail()).isEqualTo("joao@email.com");
        assertThat(encontrado.getTelefone()).isEqualTo("71999999999");
    }

    @Test
    @DisplayName("Deve buscar um cliente pelo ID")
    void deveBuscarClientePorId() {

        Cliente salvo = clienteRepo.saveAndFlush(criarCliente("João da Silva", "joao@email.com"));

        entityManager.clear();

        Optional<Cliente> resultado = clienteRepo.findById(salvo.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getNome()).isEqualTo("João da Silva");
    }

    @Test
    @DisplayName("Não deve encontrar cliente com ID inexistente")
    void naoDeveEncontrarClientePorIdInexistente() {

        assertThat(clienteRepo.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    @DisplayName("Deve listar os clientes persistidos")
    void deveListarClientesPersistidos() {

        Cliente primeiro = criarCliente("João da Silva", "joao@email.com");
        Cliente segundo = criarCliente("Maria Oliveira", "maria@email.com");

        clienteRepo.saveAllAndFlush(List.of(primeiro, segundo));
        entityManager.clear();

        assertThat(clienteRepo.findAll())
                .hasSize(2)
                .extracting(Cliente::getNome)
                .containsExactlyInAnyOrder("João da Silva", "Maria Oliveira");
    }

    @Test
    @DisplayName("Deve verificar a existência de um cliente pelo ID")
    void deveVerificarExistenciaCliente() {

        Cliente salvo = clienteRepo.saveAndFlush(criarCliente("João da Silva", "joao@email.com"));

        assertThat(clienteRepo.existsById(salvo.getId())).isTrue();
        assertThat(clienteRepo.existsById(UUID.randomUUID())).isFalse();
    }

    @Test
    @DisplayName("Deve contar os clientes persistidos")
    void deveContarClientesPersistidos() {

        clienteRepo.saveAllAndFlush(List.of(
                criarCliente("João da Silva", "joao@email.com"),
                criarCliente("Maria Oliveira", "maria@email.com")));

        assertThat(clienteRepo.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve excluir um cliente pelo ID")
    void deveExcluirClientePeloId() {

        Cliente salvo = clienteRepo.saveAndFlush(criarCliente("João da Silva", "joao@email.com"));
        UUID id = salvo.getId();

        clienteRepo.deleteById(id);
        clienteRepo.flush();

        assertThat(clienteRepo.findById(id)).isEmpty();
        assertThat(clienteRepo.existsById(id)).isFalse();
    }

    @Test
    @DisplayName("Deve permitir que um cliente possua múltiplas unidades consumidoras")
    void devePermitirMultiplasUnidadesConsumidoras() {

        Cliente cliente = clienteRepo.saveAndFlush(criarCliente("João da Silva", "joao@email.com"));

        entityManager.persist(criarUnidadeConsumidora(cliente, "UC-001"));
        entityManager.persist(criarUnidadeConsumidora(cliente, "UC-002"));
        entityManager.flush();
        entityManager.clear();

        Cliente encontrado = clienteRepo.findById(cliente.getId()).orElseThrow();

        assertThat(encontrado.getUnidadeConsumidoras())
                .hasSize(2)
                .extracting(UnidadeConsumidora::getCodigo)
                .containsExactlyInAnyOrder("UC-001", "UC-002");
    }
}
