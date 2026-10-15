package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.Cliente;
import com.powermanager.powermanager.entity.Medidor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
public class MedidorRepoTest {

    @Autowired
    private MedidorRepo medidorRepo;

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

        return clienteRepo.saveAndFlush(cliente);
    }

    private Medidor criarMedidor(Cliente cliente) {

        Medidor medidor = new Medidor();

        medidor.setNumero("MED-001");
        medidor.setLocalizacao("Residencial");
        medidor.setTipo("Digital");
        medidor.setCodigoFase("BIFASICO");
        medidor.setTipoFatura("RESIDENCIAL");
        medidor.setDias(30);
        medidor.setCliente(cliente);

        return medidor;
    }

    @Test
    @DisplayName("Deve persistir medidor e gerar UUID")
    void devePersistirMedidor() {

        Cliente cliente = criarCliente();
        Medidor medidor = criarMedidor(cliente);

        Medidor salvo = medidorRepo.saveAndFlush(medidor);

        assertThat(salvo.getId()).isNotNull();
        assertThat(medidorRepo.findById(salvo.getId())).isPresent();
    }

    @Test
    @DisplayName("Deve buscar medidor por ID")
    void deveBuscarMedidorPorId(){

        Cliente cliente = criarCliente();

        Medidor salvo = medidorRepo.saveAndFlush(criarMedidor(cliente));

        Optional<Medidor> resultado = medidorRepo.findById(salvo.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNumero()).isEqualTo("MED-001");
    }

    @Test
    @DisplayName("Não deve encontrar medidor por ID inexistente")
    void naoDeveEncontrarMedidorPorIdInexistente(){

        Optional<Medidor> resultado = medidorRepo.findById(UUID.randomUUID());

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar medidor pelo número")
    void deveBuscarMedidorPorNumero(){

        Cliente cliente = criarCliente();

        medidorRepo.saveAndFlush(criarMedidor(cliente));

        Optional<Medidor> resultado = medidorRepo.findByNumero("MED-001");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getTipo()).isEqualTo("Digital");
    }

    @Test
    @DisplayName("Não deve encontrar medidor por número inexistente")
    void naoDeveEncontrarMedidorPorNumeroInexistente(){

        Optional<Medidor> resultado = medidorRepo.findByNumero("MED-INEXISTENTE");

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve listar os medidores persistidos")
    void deveListarMedidores(){

        Cliente primeiroCliente = criarCliente();

        Medidor primeiro = criarMedidor(primeiroCliente);
        medidorRepo.saveAndFlush(primeiro);

        Cliente segundoCliente = criarCliente();
        Medidor segundo = criarMedidor(segundoCliente);
        segundo.setNumero("MED-002");

        medidorRepo.saveAndFlush(segundo);

        assertThat(medidorRepo.findAll())
                .hasSize(2)
                .extracting(Medidor::getNumero)
                .containsExactlyInAnyOrder("MED-001", "MED-002");
    }

    @Test
    @DisplayName("Deve verificar a existência do medidor por ID")
    void deveVerificarExistenciaPorId(){

        Cliente cliente = criarCliente();

        Medidor salvo = medidorRepo.saveAndFlush(criarMedidor(cliente));

        assertThat(medidorRepo.existsById(salvo.getId())).isTrue();
        assertThat(medidorRepo.existsById(UUID.randomUUID())).isFalse();
    }

    @Test
    @DisplayName("Deve contar os medidores persistidos")
    void deveContarMedidores(){

        Cliente cliente = criarCliente();

        medidorRepo.saveAndFlush(criarMedidor(cliente));

        assertThat(medidorRepo.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve excluir medidor pelo ID")
    void deveExcluirMedidorPorId(){

        Cliente cliente = criarCliente();

        Medidor salvo = medidorRepo.saveAndFlush(criarMedidor(cliente));

        UUID id = salvo.getId();

        medidorRepo.deleteById(id);
        medidorRepo.flush();

        assertThat(medidorRepo.findById(id)).isEmpty();
        assertThat(medidorRepo.existsById(id)).isFalse();
    }

    @Test
    @DisplayName("Não deve permitir números de medidor duplicados")
    void naoDevePermitirNumeroDuplicado(){

        Cliente primeiroCliente = criarCliente();

        medidorRepo.saveAndFlush(criarMedidor(primeiroCliente));

        Cliente segundoCliente = criarCliente();

        Medidor duplicado = criarMedidor(segundoCliente);

        assertThatThrownBy(() ->
                medidorRepo.saveAndFlush(duplicado))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Não deve associar dois medidores ao mesmo cliente")
    void naoDevePermitirDoisMedidoresParaOMesmoCliente() {

        Cliente cliente = criarCliente();

        medidorRepo.saveAndFlush(criarMedidor(cliente));

        Medidor segundoMedidor = criarMedidor(cliente);
        segundoMedidor.setNumero("MED-002");

        assertThatThrownBy(() ->
                medidorRepo.saveAndFlush(segundoMedidor))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
