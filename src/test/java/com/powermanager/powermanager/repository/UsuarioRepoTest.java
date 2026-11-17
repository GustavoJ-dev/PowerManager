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

    private Usuario criarUsuario(
            String username,
            TipoUsuario tipoUsuario
    ) {
        Usuario usuario = new Usuario();

        usuario.setUsername(username);
        usuario.setNome("Usuário de Teste");
        usuario.setPasswordHash("hash-senha-teste");
        usuario.setTipoUsuario(tipoUsuario);

        return usuario;
    }

    private Cliente criarCliente(String email) {
        Cliente cliente = new Cliente();

        cliente.setNome("Cliente de Teste");
        cliente.setEmail(email);
        cliente.setTelefone("71999999999");

        return cliente;
    }

    @Test
    @DisplayName("Deve persistir um administrador corretamente")
    void devePersistirAdministrador() {

        Usuario salvo = usuarioRepo.saveAndFlush(
                criarUsuario("admin.teste", TipoUsuario.ADMINISTRADOR)
        );

        assertThat(salvo.getId()).isNotNull();

        entityManager.clear();

        Usuario encontrado = usuarioRepo.findById(salvo.getId())
                .orElseThrow();

        assertThat(encontrado.getUsername()).isEqualTo("admin.teste");
        assertThat(encontrado.getNome()).isEqualTo("Usuário de Teste");
        assertThat(encontrado.getPasswordHash())
                .isEqualTo("hash-senha-teste");
        assertThat(encontrado.getTipoUsuario())
                .isEqualTo(TipoUsuario.ADMINISTRADOR);
        assertThat(encontrado.getCliente()).isNull();
    }

    @Test
    @DisplayName("Deve buscar usuário pelo ID")
    void deveBuscarUsuarioPorId() {

        Usuario salvo = usuarioRepo.saveAndFlush(
                criarUsuario("usuario.busca", TipoUsuario.ADMINISTRADOR)
        );

        entityManager.clear();

        Optional<Usuario> resultado = usuarioRepo.findById(salvo.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getUsername())
                .isEqualTo("usuario.busca");
    }

    @Test
    @DisplayName("Não deve encontrar usuário com ID inexistente")
    void naoDeveEncontrarUsuarioPorIdInexistente() {

        Optional<Usuario> resultado =
                usuarioRepo.findById(UUID.randomUUID());

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve listar todos os usuários")
    void deveListarUsuarios() {

        usuarioRepo.saveAllAndFlush(List.of(
                criarUsuario("admin.um", TipoUsuario.ADMINISTRADOR),
                criarUsuario("admin.dois", TipoUsuario.ADMINISTRADOR)
        ));

        entityManager.clear();

        List<Usuario> resultado = usuarioRepo.findAll();

        assertThat(resultado)
                .hasSize(2)
                .extracting(Usuario::getUsername)
                .containsExactlyInAnyOrder("admin.um", "admin.dois");
    }

    @Test
    @DisplayName("Deve excluir usuário pelo ID")
    void deveExcluirUsuario() {

        Usuario salvo = usuarioRepo.saveAndFlush(
                criarUsuario("usuario.excluir", TipoUsuario.ADMINISTRADOR)
        );

        UUID id = salvo.getId();

        usuarioRepo.deleteById(id);
        usuarioRepo.flush();

        assertThat(usuarioRepo.findById(id)).isEmpty();
        assertThat(usuarioRepo.existsById(id)).isFalse();
    }

    @Test
    @DisplayName("Deve buscar usuário pelo username")
    void deveBuscarUsuarioPorUsername() {

        Usuario salvo = usuarioRepo.saveAndFlush(
                criarUsuario("usuario.encontrar", TipoUsuario.ADMINISTRADOR)
        );

        entityManager.clear();

        Optional<Usuario> resultado =
                usuarioRepo.findByUsername("usuario.encontrar");

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getId()).isEqualTo(salvo.getId());
    }

    @Test
    @DisplayName("Não deve encontrar usuário com username inexistente")
    void naoDeveEncontrarUsuarioPorUsernameInexistente() {

        Optional<Usuario> resultado =
                usuarioRepo.findByUsername("usuario.inexistente");

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve verificar se username já existe")
    void deveVerificarExistenciaDeUsername() {

        usuarioRepo.saveAndFlush(
                criarUsuario("usuario.existe", TipoUsuario.ADMINISTRADOR)
        );

        assertThat(usuarioRepo.existsByUsername("usuario.existe")).isTrue();
        assertThat(usuarioRepo.existsByUsername("outro.usuario")).isFalse();
    }

    @Test
    @DisplayName("Não deve permitir usernames duplicados")
    void naoDevePermitirUsernameDuplicado() {

        usuarioRepo.saveAndFlush(
                criarUsuario("usuario.duplicado", TipoUsuario.ADMINISTRADOR)
        );

        assertThatThrownBy(() ->
                usuarioRepo.saveAndFlush(
                        criarUsuario(
                                "usuario.duplicado",
                                TipoUsuario.ADMINISTRADOR
                        )
                )
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Deve persistir relacionamento entre usuário e cliente")
    void devePersistirRelacionamentoComCliente() {

        Cliente cliente = clienteRepo.saveAndFlush(
                criarCliente("cliente@email.com")
        );

        Usuario usuario = criarUsuario(
                "usuario.cliente",
                TipoUsuario.CLIENTE
        );

        usuario.setCliente(cliente);

        Usuario salvo = usuarioRepo.saveAndFlush(usuario);

        entityManager.clear();

        Usuario encontrado = usuarioRepo.findById(salvo.getId())
                .orElseThrow();

        assertThat(encontrado.getCliente()).isNotNull();
        assertThat(encontrado.getCliente().getId())
                .isEqualTo(cliente.getId());
    }

    @Test
    @DisplayName("Deve buscar usuário pelo ID do cliente")
    void deveBuscarUsuarioPorClienteId() {

        Cliente cliente = clienteRepo.saveAndFlush(
                criarCliente("cliente.busca@email.com")
        );

        Usuario usuario = criarUsuario(
                "usuario.por.cliente",
                TipoUsuario.CLIENTE
        );

        usuario.setCliente(cliente);

        Usuario salvo = usuarioRepo.saveAndFlush(usuario);

        entityManager.clear();

        Optional<Usuario> resultado =
                usuarioRepo.findByCliente_Id(cliente.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getId()).isEqualTo(salvo.getId());
    }

    @Test
    @DisplayName("Deve verificar se cliente já possui usuário")
    void deveVerificarExistenciaDeUsuarioPorCliente() {

        Cliente cliente = clienteRepo.saveAndFlush(
                criarCliente("cliente.existe@email.com")
        );

        Usuario usuario = criarUsuario(
                "usuario.vinculado",
                TipoUsuario.CLIENTE
        );

        usuario.setCliente(cliente);

        usuarioRepo.saveAndFlush(usuario);

        assertThat(usuarioRepo.existsByCliente_Id(cliente.getId())).isTrue();

        Cliente outroCliente = clienteRepo.saveAndFlush(
                criarCliente("outro.cliente@email.com")
        );

        assertThat(usuarioRepo.existsByCliente_Id(outroCliente.getId()))
                .isFalse();
    }

    @Test
    @DisplayName("Não deve permitir associar o mesmo cliente a dois usuários")
    void naoDevePermitirClienteAssociadoADoisUsuarios() {

        Cliente cliente = clienteRepo.saveAndFlush(
                criarCliente("cliente.duplicado@email.com")
        );

        Usuario primeiro = criarUsuario(
                "usuario.primeiro",
                TipoUsuario.CLIENTE
        );

        primeiro.setCliente(cliente);

        usuarioRepo.saveAndFlush(primeiro);

        Usuario segundo = criarUsuario(
                "usuario.segundo",
                TipoUsuario.CLIENTE
        );

        segundo.setCliente(cliente);

        assertThatThrownBy(() -> usuarioRepo.saveAndFlush(segundo))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
