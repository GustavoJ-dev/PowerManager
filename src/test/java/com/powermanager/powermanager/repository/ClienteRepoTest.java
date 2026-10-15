package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.Cliente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ClienteRepoTest {

    @Autowired
    private ClienteRepo clienteRepo;

    private Cliente criarCliente(){

        Cliente cliente = new Cliente();
        cliente.setNome("João da Silva");
        cliente.setEndereco("Rua das Flores, 123");
        cliente.setCidade("Salvador");
        cliente.setEstado("Bahia");
        cliente.setEmail("joao@email.com");
        cliente.setTelefone("71999999999");

        return cliente;
    }

    @Test
    @DisplayName("Deve persistir um cliente e gerar seu UUID")
    void devePersistirCliente(){

        Cliente cliente = criarCliente();

        Cliente salvo = clienteRepo.saveAndFlush(cliente);

        assertThat(salvo.getId()).isNotNull();
        assertThat(clienteRepo.findById(salvo.getId())).isPresent();
    }

    @Test
    @DisplayName("Deve Buscar Um Cliente pelo ID")
    void deveBuscarClientePorId(){

        Cliente salvo = clienteRepo.saveAndFlush(criarCliente());

        Optional<Cliente> resultado = clienteRepo.findById(salvo.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNome()).isEqualTo("João da Silva");
    }

    @Test
    @DisplayName("Não deve encontrar Cliente com iD inexistente")
    void naoDeveEncontrarClientePorIdInexistente(){

        UUID idInexistente = UUID.randomUUID();

        Optional<Cliente> resultado = clienteRepo.findById(idInexistente);

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve Listar Clientes persistidos")
    void deveListarClientesPersistidos(){

        clienteRepo.saveAndFlush(criarCliente());

        Cliente segundo = criarCliente();

        segundo.setEmail("maria@email.com");
        segundo.setNome("Maria Oliveira");

        clienteRepo.saveAndFlush(segundo);

        List<Cliente> clientes = clienteRepo.findAll();

        assertThat(clientes)
                .hasSize(2)
                .extracting(Cliente::getNome)
                .containsExactly("João da Silva", "Maria Oliveira");
    }

    @Test
    @DisplayName("Deve verificar a existencia de um cliente")
    void deveVerificarExistenciaCliente(){

        Cliente salvo = clienteRepo.saveAndFlush(criarCliente());

        assertThat(clienteRepo.existsById(salvo.getId())).isTrue();
        assertThat(clienteRepo.existsById(UUID.randomUUID())).isFalse();
    }

    @Test
    @DisplayName("Deve contar Clientes persistidos")
    void deveContarClientes(){

        clienteRepo.saveAndFlush(criarCliente());

        assertThat(clienteRepo.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve Excluir cliente pelo ID")
    void deveExcluirPeloId(){

        Cliente salvo = clienteRepo.saveAndFlush(criarCliente());

        UUID id = salvo.getId();

        clienteRepo.deleteById(id);
        clienteRepo.flush();

        assertThat(clienteRepo.findById(id)).isEmpty();
        assertThat(clienteRepo.existsById(id)).isFalse();
    }
}
