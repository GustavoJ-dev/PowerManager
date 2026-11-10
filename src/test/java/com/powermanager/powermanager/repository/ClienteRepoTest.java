package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.Cliente;
import com.powermanager.powermanager.entity.Medidor;
import com.powermanager.powermanager.enums.ClasseConsumo;
import com.powermanager.powermanager.enums.TipoLigacao;
import com.powermanager.powermanager.enums.TipoMedidor;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
public class ClienteRepoTest {

    @Autowired
    private ClienteRepo clienteRepo;

    @Autowired
    private EntityManager entityManager;

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

    private Medidor criarMedidor(Cliente cliente, String numero){

        Medidor medidor = new Medidor();

        medidor.setNumero(numero);
        medidor.setCliente(cliente);
        medidor.setLocalizacao("Residência");
        medidor.setTipoMedidor(TipoMedidor.ELETRONICO);
        medidor.setTipoLigacao(TipoLigacao.MONOFASICO);
        medidor.setClasseConsumo(ClasseConsumo.RESIDENCIAL);
        medidor.setDias(30);

        return medidor;
    }

    @Test
    @DisplayName("Deve persistir um cliente e gerar seu UUID")
    void devePersistirCliente(){

        Cliente cliente = criarCliente();

        Cliente salvo = clienteRepo.saveAndFlush(cliente);

        assertThat(salvo.getId()).isNotNull();

        entityManager.clear();

        Optional<Cliente> resultado = clienteRepo.findById(salvo.getId());

        assertThat(resultado).isPresent();

        Cliente encontrado = resultado.orElseThrow();

        assertThat(encontrado.getNome()).isEqualTo("João da Silva");
        assertThat(encontrado.getEndereco()).isEqualTo("Rua das Flores, 123");
        assertThat(encontrado.getCidade()).isEqualTo("Salvador");
        assertThat(encontrado.getEstado()).isEqualTo("Bahia");
        assertThat(encontrado.getEmail()).isEqualTo("joao@email.com");
        assertThat(encontrado.getTelefone()).isEqualTo("71999999999");
    }

    @Test
    @DisplayName("Deve Buscar Um Cliente pelo ID")
    void deveBuscarClientePorId(){

        Cliente salvo = clienteRepo.saveAndFlush(criarCliente());

        entityManager.clear();

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

        Cliente primeiro = criarCliente();
        Cliente segundo = criarCliente();

        segundo.setEmail("maria@email.com");
        segundo.setNome("Maria Oliveira");

        clienteRepo.saveAllAndFlush(List.of(primeiro, segundo));

        entityManager.clear();

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

       Cliente primeiro = criarCliente();

       Cliente segundo = criarCliente();
       segundo.setEmail("maria@email.com");

       clienteRepo.saveAllAndFlush(List.of(primeiro, segundo));

       assertThat(clienteRepo.count()).isEqualTo(2);


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

    @Test
    @DisplayName("Deve permitir que um cliente possua múltiplos medidores")
    void devePermitirQueUmClienteTenhaMultiplosMedidores(){

        Cliente cliente = clienteRepo.saveAndFlush(criarCliente());

        Medidor primeiro = criarMedidor(cliente, "MED-001");
        Medidor segundo = criarMedidor(cliente, "MED-002");

        entityManager.persist(primeiro);
        entityManager.persist(segundo);

        entityManager.flush();
        entityManager.clear();

        Cliente encontrado = clienteRepo.findById(cliente.getId()).orElseThrow();

        assertThat(encontrado.getMedidores())
                .hasSize(2)
                .extracting(Medidor::getNumero)
                .containsExactly("MED-001", "MED-002");
    }
}
