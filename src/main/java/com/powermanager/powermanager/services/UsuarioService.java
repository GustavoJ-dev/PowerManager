package com.powermanager.powermanager.services;

import com.powermanager.powermanager.entity.Cliente;
import com.powermanager.powermanager.entity.Usuario;
import com.powermanager.powermanager.entity.enums.TipoUsuario;
import com.powermanager.powermanager.repository.UsuarioRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepo usuarioRepo;
    private final ClienteService clienteService;

    //cadastra novo usuario
    @Transactional
    public Usuario cadastrarUsuario(Usuario usuario) {

        validarUsuario(usuario);

        verificarUsernameDisponivel(usuario.getUsername());

        //usuario CLIENTE precisa estar associado a um cliente
        //ADMNISTRADOR não precisa estar associado a cliente

        if (usuario.getTipoUsuario() == null || usuario.getCliente().getId() == null){

            throw new IllegalArgumentException("Usuario CLIENTE deve estar associado a um cliente");
        }

        Cliente cliente = clienteService.buscarPorId(usuario.getCliente().getId());

        usuario.setCliente(cliente);

        if (usuario.getTipoUsuario() == TipoUsuario.ADMINISTRADOR) {

            usuario.setCliente(null);
        }
        return usuarioRepo.save(usuario);
    }

    //lista todos os usuarios
    @Transactional(readOnly = true)
    public List<Usuario> listarUsuarios(){

        return usuarioRepo.findAll();
    }

    //buscar pelo id
    @Transactional(readOnly = true)
    public Usuario buscarPorId(UUID id){

        return usuarioRepo.findById(id).orElseThrow(() ->
                new RuntimeException("Nenhum Usuário com esse ID:" + id));
    }

    //busca um usuario pelo username
    @Transactional(readOnly = true)
    public Usuario buscaPorUsername(String username){

        return usuarioRepo.findByUsername(username).orElseThrow(() ->
                new RuntimeException("Usuário: " + username + " não encontrado."));
    }

    @Transactional
    public Usuario atualizarUsuario(UUID id, Usuario usuarioAtualizado) {

        Usuario usuario = buscarPorId(id);

        if (usuarioAtualizado.getUsername() == null || usuarioAtualizado.getUsername().isBlank()){

            throw new IllegalArgumentException("O username é obrigatório");
        }

        /*
         * Caso o username esteja sendo alterado,
         * verifica se o novo username já pertence
         * a outro usuário.
         */

        if (!usuario.getUsername().equals(usuarioAtualizado.getUsername())){

            verificarUsernameDisponivel(usuarioAtualizado.getUsername());
        }

        usuario.setUsername(usuarioAtualizado.getUsername());

        usuario.setNome(usuarioAtualizado.getNome());

        return usuarioRepo.save(usuario);

    }

    /*
     * Autentica um usuário.
     *
     * A verificação real da senha será feita posteriormente
     * com PasswordEncoder/Spring Security.
     */
    @Transactional(readOnly = true)
    public Usuario autenticar(String username, String passwordHash) {

        Usuario usuario = usuarioRepo.findByUsername(username).orElseThrow(() ->
                        new RuntimeException("Usuário ou senha inválidos."));

        if (!usuario.getPasswordHash().equals(passwordHash)){

            throw new RuntimeException("Usuário ou senha inválidos.");
        }

        return usuario;
    }


    private void validarUsuario(Usuario usuario) {

        if (usuario == null) {

            throw new IllegalArgumentException("Usuário não pode ser nulo.");
        }

        if (usuario.getUsername() == null || usuario.getUsername().isBlank()) {

            throw new IllegalArgumentException("O username é obrigatório.");
        }

        if (usuario.getNome() == null || usuario.getNome().isBlank()) {

            throw new IllegalArgumentException("O nome é obrigatório.");
        }

        if (usuario.getPasswordHash() == null || usuario.getPasswordHash().isBlank()) {

            throw new IllegalArgumentException("A senha é obrigatória.");
        }

        if (usuario.getTipoUsuario() == null) {

            throw new IllegalArgumentException("O tipo do usuário é obrigatório.");
        }
    }

    private void verificarUsernameDisponivel(String username){

        if (usuarioRepo.findByUsername(username).isPresent()) {

            throw new RuntimeException("Username já está sendo utilizado: " + username);
        }
    }

}
