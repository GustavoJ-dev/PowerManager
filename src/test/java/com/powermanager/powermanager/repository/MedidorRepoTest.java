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
import org.springframework.dao.DataIntegrityViolationException;

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
        cliente.setEndereco("Rua das Flores, 123");
        cliente.setCidade("Salvador");
        cliente.setEstado("Bahia");
        cliente.setEmail(email);
        cliente.setTelefone("71999999999");

        return clienteRepo.saveAndFlush(cliente);
    }

    private Medidor criarMedidor(Cliente cliente, String numero) {

        Medidor medidor = new Medidor();

        medidor.setNumero(numero);
        medidor.setLocalizacao("Residencial");
        medidor.setTipoMedidor(TipoMedidor.ELETRONICO);
        medidor.setTipoLigacao(TipoLigacao.BIFASICO);
        medidor.setClasseConsumo(ClasseConsumo.RESIDENCIAL);
        medidor.setDias(30);
        medidor.setCliente(cliente);

        return medidor;
    }

    @Test
    @DisplayName("Deve persistir um medidor corretamente")
    void devePersistirMedidor() {

        Cliente cliente = criarCliente("joao@email.com");

        Medidor salvo = medidorRepo.saveAndFlush(criarMedidor(cliente, "MED-001"));

        assertThat(salvo.getId()).isNotNull();

        entityManager.clear();

        Medidor encontrado = medidorRepo.findById(salvo.getId()).orElseThrow();

        assertThat(encontrado.getNumero()).isEqualTo("MED-001");
        assertThat(encontrado.getLocalizacao()).isEqualTo("Residencial");
        assertThat(encontrado.getTipoMedidor()).isEqualTo(TipoMedidor.ELETRONICO);
        assertThat(encontrado.getTipoLigacao()).isEqualTo(TipoLigacao.BIFASICO);
        assertThat(encontrado.getClasseConsumo()).isEqualTo(ClasseConsumo.RESIDENCIAL);
        assertThat(encontrado.getDias()).isEqualTo(30);
        assertThat(encontrado.getCliente().getId()).isEqualTo(cliente.getId());
    }

    @Test
    @DisplayName("Deve buscar um medidor pelo ID")
    void deveBuscarMedidorPorId() {

        Cliente cliente = criarCliente("joao@email.com");

        Medidor salvo = medidorRepo.saveAndFlush(criarMedidor(cliente, "MED-002"));

        entityManager.clear();

        Optional<Medidor> resultado = medidorRepo.findById(salvo.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getNumero())
                .isEqualTo("MED-002");
    }

    @Test
    @DisplayName("Não deve encontrar medidor com ID inexistente")
    void naoDeveEncontrarMedidorPorIdInexistente() {

        Optional<Medidor> resultado = medidorRepo.findById(UUID.randomUUID());

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar um medidor pelo número")
    void deveBuscarMedidorPorNumero() {

        Cliente cliente = criarCliente("joao@email.com");

        medidorRepo.saveAndFlush(criarMedidor(cliente, "MED-003"));

        entityManager.clear();

        Optional<Medidor> resultado = medidorRepo.findByNumero("MED-003");

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getTipoMedidor())
                .isEqualTo(TipoMedidor.ELETRONICO);
    }

    @Test
    @DisplayName("Não deve encontrar medidor com número inexistente")
    void naoDeveEncontrarMedidorPorNumeroInexistente() {

        Optional<Medidor> resultado = medidorRepo.findByNumero("MED-INEXISTENTE");

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve listar todos os medidores persistidos")
    void deveListarMedidores() {

        Cliente primeiroCliente = criarCliente("primeiro@email.com");
        Cliente segundoCliente = criarCliente("segundo@email.com");

        medidorRepo.saveAllAndFlush(List.of(
                criarMedidor(primeiroCliente, "MED-004"),
                criarMedidor(segundoCliente, "MED-005")));

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

        Cliente cliente = criarCliente("joao@email.com");

        Medidor salvo = medidorRepo.saveAndFlush(criarMedidor(cliente, "MED-006"));

        assertThat(medidorRepo.existsById(salvo.getId())).isTrue();
        assertThat(medidorRepo.existsById(UUID.randomUUID())).isFalse();
    }

    @Test
    @DisplayName("Deve contar os medidores persistidos")
    void deveContarMedidores() {

        Cliente primeiroCliente = criarCliente("primeiro@email.com");
        Cliente segundoCliente = criarCliente("segundo@email.com");

        medidorRepo.saveAllAndFlush(List.of(
                criarMedidor(primeiroCliente, "MED-007"),
                criarMedidor(segundoCliente, "MED-008")));

        assertThat(medidorRepo.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve excluir um medidor pelo ID")
    void deveExcluirMedidorPorId() {

        Cliente cliente = criarCliente("joao@email.com");

        Medidor salvo = medidorRepo.saveAndFlush(criarMedidor(cliente, "MED-009"));

        UUID id = salvo.getId();

        medidorRepo.deleteById(id);
        medidorRepo.flush();

        assertThat(medidorRepo.findById(id)).isEmpty();
        assertThat(medidorRepo.existsById(id)).isFalse();
    }

    @Test
    @DisplayName("Não deve permitir números de medidor duplicados")
    void naoDevePermitirNumeroDuplicado() {

        Cliente primeiroCliente = criarCliente("primeiro@email.com");
        Cliente segundoCliente = criarCliente("segundo@email.com");

        medidorRepo.saveAndFlush(criarMedidor(primeiroCliente, "MED-010"));

        Medidor duplicado = criarMedidor(segundoCliente, "MED-010");

        assertThatThrownBy(() -> medidorRepo.saveAndFlush(duplicado))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Deve permitir múltiplos medidores para o mesmo cliente")
    void devePermitirMultiplosMedidoresParaOMesmoCliente() {

        Cliente cliente = criarCliente("joao@email.com");

        medidorRepo.saveAllAndFlush(List.of(criarMedidor(cliente, "MED-011"), criarMedidor(cliente, "MED-012")));

        entityManager.clear();

        Cliente encontrado = clienteRepo.findById(cliente.getId()).orElseThrow();

        assertThat(encontrado.getMedidores())
                .hasSize(2)
                .extracting(Medidor::getNumero)
                .containsExactlyInAnyOrder("MED-011", "MED-012");
    }
}