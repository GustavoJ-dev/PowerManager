package com.powermanager.powermanager.services;

import com.powermanager.powermanager.dto.usuario.UsuarioAtualizacaoDTO;
import com.powermanager.powermanager.dto.usuario.UsuarioCadastroDTO;
import com.powermanager.powermanager.dto.usuario.UsuarioResponseDTO;
import com.powermanager.powermanager.entity.Cliente;
import com.powermanager.powermanager.entity.Usuario;
import com.powermanager.powermanager.entity.enums.TipoUsuario;
import com.powermanager.powermanager.exception.ClienteNotFoundException;
import com.powermanager.powermanager.exception.UsernameAlreadyExistsException;
import com.powermanager.powermanager.exception.UsuarioNotFoundException;
import com.powermanager.powermanager.exception.UsuarioValidationException;
import com.powermanager.powermanager.mapper.UsuarioMapper;
import com.powermanager.powermanager.repository.ClienteRepo;
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
    private final ClienteRepo clienteRepo;
    private final UsuarioMapper usuarioMapper;

    //cadastra novo usuario
    @Transactional
    public UsuarioResponseDTO cadastrarUsuario(UsuarioCadastroDTO dto) {

        if (dto == null){

            throw new UsuarioValidationException("Os dados do usuário são obrigatórios.");
        }

        verificarUsernameDisponivel(dto.username());

        Usuario usuario = usuarioMapper.paraEntidade(dto);

        validarUsuario(usuario);

        //Cliente precisa estar associado a um cliente existente
        if (dto.tipoUsuario() == TipoUsuario.CLIENTE){

            if (dto.clienteId() == null){

                throw new UsuarioValidationException("Usuário CLIENTE deve estar associado a um cliente");
            }

            Cliente cliente = clienteRepo.findById(dto.clienteId())
                    .orElseThrow(
                            ()-> new ClienteNotFoundException("Cliente não encontrado."));

            usuario.setCliente(cliente);
        }

        //administrador não possui cliente associado
        if (dto.tipoUsuario() == TipoUsuario.ADMINISTRADOR){

            usuario.setCliente(null);
        }

        //manter até a etapa security
        usuario.setPasswordHash(dto.senha());

        Usuario usuarioSalvo = usuarioRepo.save(usuario);

        return usuarioMapper.paraResponseDTO(usuarioSalvo);

    }

    //lista todos os usuarios
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarUsuarios(){

        return usuarioRepo.findAll()
                .stream()
                .map(usuarioMapper::paraResponseDTO)
                .toList();
    }

    //buscar pelo id
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(UUID id){

        Usuario usuario = usuarioRepo.findById(id).orElseThrow(() ->
                new UsuarioNotFoundException("Nenhum Usuário com esse ID:" + id));

        return usuarioMapper.paraResponseDTO(usuario);
    }

    //busca um usuario pelo username
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscaPorUsername(String username){

        Usuario usuario =  usuarioRepo.findByUsername(username).orElseThrow(() ->
                new UsuarioNotFoundException("Usuário: " + username + " não encontrado."));

        return usuarioMapper.paraResponseDTO(usuario);
    }

    // Atualiza os campos permitidos do usuário
    @Transactional
    public UsuarioResponseDTO atualizarUsuario(UUID id, UsuarioAtualizacaoDTO usuarioAttDTO) {

        if (usuarioAttDTO == null) {

            throw new UsuarioValidationException("Os dados do usuário para atualização não podem ser nulos.");
        }

        Usuario usuario = usuarioRepo.findById(id)
                .orElseThrow(
                        ()-> new UsuarioNotFoundException(
                                "Nenhum Usuário encontrado com esse ID: " + id));


        if (usuarioAttDTO.username() == null || usuarioAttDTO.username().isBlank()) {

            throw new UsuarioValidationException("O username é obrigatório");
        }

        if (usuarioAttDTO.nome() == null || usuarioAttDTO.nome().isBlank()) {

            throw new UsuarioValidationException("O nome é obrigatório.");
        }

        if (!usuario.getUsername().equals(usuarioAttDTO.username())) {

            verificarUsernameDisponivel(usuarioAttDTO.username());
        }

        usuarioMapper.atualizarEntidade(usuarioAttDTO, usuario);

        Usuario usuarioSalvo = usuarioRepo.save(usuario);

        return usuarioMapper.paraResponseDTO(usuarioSalvo);

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
                        new UsuarioValidationException("Usuário ou senha inválidos."));

        if (!usuario.getPasswordHash().equals(passwordHash)){

            throw new UsuarioValidationException("Usuário ou senha inválidos.");
        }

        return usuario;
    }


    private void validarUsuario(Usuario usuario) {

        if (usuario == null) {

            throw new UsuarioValidationException("Usuário não pode ser nulo.");
        }

        if (usuario.getUsername() == null || usuario.getUsername().isBlank()) {

            throw new UsuarioValidationException("O username é obrigatório.");
        }

        if (usuario.getNome() == null || usuario.getNome().isBlank()) {

            throw new UsuarioValidationException("O nome é obrigatório.");
        }

        if (usuario.getPasswordHash() == null || usuario.getPasswordHash().isBlank()) {

            throw new UsuarioValidationException("A senha é obrigatória.");
        }

        if (usuario.getTipoUsuario() == null) {

            throw new UsuarioValidationException("O tipo do usuário é obrigatório.");
        }
    }

    private void verificarUsernameDisponivel(String username){

        if (usuarioRepo.findByUsername(username).isPresent()) {

            throw new UsernameAlreadyExistsException("Username já está sendo utilizado: " + username);
        }
    }
}