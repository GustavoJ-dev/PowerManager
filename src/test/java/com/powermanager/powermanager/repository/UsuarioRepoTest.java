package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.Cliente;
import com.powermanager.powermanager.entity.Usuario;
import com.powermanager.powermanager.enums.TipoUsuario;

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
class UsuarioRepoTest {

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private ClienteRepo clienteRepo;

    @Autowired
    private EntityManager entityManager;

    private Usuario criarUsuario(String username) {

        Usuario usuario = new Usuario();

        usuario.setUsername(username);
        usuario.setNome("Usuário de Teste");
        usuario.setPasswordHash("hash-senha-teste");
        usuario.setTipoUsuario(TipoUsuario.CLIENTE);

        return usuario;
    }

    private Cliente criarCliente() {

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
    @DisplayName("Deve persistir um usuário corretamente")
    void devePersistirUsuario() {

        Usuario salvo = usuarioRepo.saveAndFlush(criarUsuario("usuario.teste"));

        assertThat(salvo.getId()).isNotNull();

        entityManager.clear();

        Usuario encontrado = usuarioRepo.findById(salvo.getId()).orElseThrow();

        assertThat(encontrado.getUsername()).isEqualTo("usuario.teste");
        assertThat(encontrado.getNome()).isEqualTo("Usuário de Teste");
        assertThat(encontrado.getPasswordHash()).isEqualTo("hash-senha-teste");
        assertThat(encontrado.getTipoUsuario()).isEqualTo(TipoUsuario.CLIENTE);
        assertThat(encontrado.getCliente()).isNull();
    }

    @Test
    @DisplayName("Deve buscar um usuário pelo ID")
    void deveBuscarUsuarioPorId() {

        Usuario salvo = usuarioRepo.saveAndFlush(criarUsuario("usuario.busca"));

        entityManager.clear();

        Optional<Usuario> resultado = usuarioRepo.findById(salvo.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getUsername()).isEqualTo("usuario.busca");
    }

    @Test
    @DisplayName("Não deve encontrar usuário com ID inexistente")
    void naoDeveEncontrarUsuarioPorIdInexistente() {

        Optional<Usuario> resultado = usuarioRepo.findById(UUID.randomUUID());

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve listar todos os usuários")
    void deveListarUsuarios() {

        usuarioRepo.saveAllAndFlush(List.of(
                criarUsuario("usuario.um"),
                criarUsuario("usuario.dois")));

        entityManager.clear();

        List<Usuario> usuarios = usuarioRepo.findAll();

        assertThat(usuarios)
                .hasSize(2)
                .extracting(Usuario::getUsername)
                .containsExactlyInAnyOrder("usuario.um", "usuario.dois");
    }

    @Test
    @DisplayName("Deve excluir um usuário pelo ID")
    void deveExcluirUsuario() {

        Usuario salvo = usuarioRepo.saveAndFlush(criarUsuario("usuario.excluir"));

        UUID id = salvo.getId();

        usuarioRepo.deleteById(id);
        usuarioRepo.flush();

        assertThat(usuarioRepo.findById(id)).isEmpty();
        assertThat(usuarioRepo.existsById(id)).isFalse();
    }

    @Test
    @DisplayName("Deve buscar um usuário pelo username")
    void deveBuscarUsuarioPorUsername() {

        Usuario salvo = usuarioRepo.saveAndFlush(criarUsuario("usuario.encontrar"));

        entityManager.clear();

        Optional<Usuario> resultado = usuarioRepo.findByUsername("usuario.encontrar");

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getId()).isEqualTo(salvo.getId());
    }

    @Test
    @DisplayName("Não deve encontrar usuário com username inexistente")
    void naoDeveEncontrarUsuarioPorUsernameInexistente() {

        Optional<Usuario> resultado = usuarioRepo.findByUsername("usuario.inexistente");

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Não deve permitir usernames duplicados")
    void naoDevePermitirUsernameDuplicado() {

        usuarioRepo.saveAndFlush(criarUsuario("usuario.duplicado"));

        assertThatThrownBy(() ->
                usuarioRepo.saveAndFlush(criarUsuario("usuario.duplicado")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Deve persistir o relacionamento entre usuário e cliente")
    void devePersistirRelacionamentoComCliente() {

        Cliente cliente = clienteRepo.saveAndFlush(criarCliente());

        Usuario usuario = criarUsuario("usuario.cliente");
        usuario.setCliente(cliente);

        Usuario salvo = usuarioRepo.saveAndFlush(usuario);

        entityManager.clear();

        Usuario encontrado = usuarioRepo.findById(salvo.getId()).orElseThrow();

        assertThat(encontrado.getCliente()).isNotNull();
        assertThat(encontrado.getCliente().getId()).isEqualTo(cliente.getId());
    }
}