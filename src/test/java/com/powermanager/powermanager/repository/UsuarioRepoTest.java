package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.Usuario;
import com.powermanager.powermanager.entity.enums.TipoUsuario;
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

    private Usuario criarUsuario(String username){

        Usuario usuario = new Usuario();

        usuario.setUsername(username);
        usuario.setNome("Usuário de Teste");
        usuario.setPasswordHash("hash-senha-teste");
        usuario.setTipoUsuario(TipoUsuario.CLIENTE);

        return usuario;
    }

    @Test
    void devePersistirUsuario(){

        Usuario usuario = criarUsuario("usuario.teste");

        Usuario salvo = usuarioRepo.saveAndFlush(usuario);

        assertThat(salvo.getId()).isNotNull();
        assertThat(usuarioRepo.findById(salvo.getId())).isPresent();
    }

    @Test
    void deveBuscarUsuarioPorId(){

        Usuario salvo = usuarioRepo.saveAndFlush(criarUsuario("usuario.busca"));

        Optional<Usuario> resultado = usuarioRepo.findById(salvo.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getUsername()).isEqualTo("usuario.busca");
    }

    @Test
    void deveListarUsuarios(){

        usuarioRepo.saveAndFlush(criarUsuario("usuario.um"));
        usuarioRepo.saveAndFlush(criarUsuario("usuario.dois"));

        List<Usuario> usuarios = usuarioRepo.findAll();

        assertThat(usuarios).hasSize(2);
    }

    @Test
    void deveExcluirUsuario(){

        Usuario salvo = usuarioRepo.saveAndFlush(criarUsuario("usuario.excluir"));

        UUID id = salvo.getId();

        usuarioRepo.deleteById(id);
        usuarioRepo.flush();

        assertThat(usuarioRepo.findById(id)).isEmpty();
    }

    @Test
    void deveBuscarUsuarioPorUsername(){

        Usuario salvo = usuarioRepo.saveAndFlush(criarUsuario("usuario.encontrar"));

        Optional<Usuario> resultado = usuarioRepo.findByUsername("usuario.encontrar");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(salvo.getId());
    }

    @Test
    void deveRetornarVazioQuandoUsernameNaoExistir(){

        Optional<Usuario> resultado = usuarioRepo.findByUsername("usuario.inexistente");

        assertThat(resultado).isEmpty();
    }

    @Test
    void naoDevePermitirUsernameDuplicado(){

        usuarioRepo.saveAndFlush(criarUsuario("usuario.duplicado"));

        assertThatThrownBy(() ->
                usuarioRepo.saveAndFlush(criarUsuario("usuario.duplicado")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

