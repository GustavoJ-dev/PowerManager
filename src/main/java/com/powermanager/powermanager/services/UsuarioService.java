package com.powermanager.powermanager.services;

import com.powermanager.powermanager.dto.usuario.UsuarioAtualizacaoDTO;
import com.powermanager.powermanager.dto.usuario.UsuarioCadastroDTO;
import com.powermanager.powermanager.dto.usuario.UsuarioResponseDTO;
import com.powermanager.powermanager.entity.Cliente;
import com.powermanager.powermanager.entity.Usuario;
import com.powermanager.powermanager.enums.TipoUsuario;
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
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepo usuarioRepo;
    private final ClienteRepo clienteRepo;
    private final UsuarioMapper usuarioMapper;

    /*
     * Cadastra um novo usuário.
     */
    @Transactional
    public UsuarioResponseDTO cadastrarUsuario(UsuarioCadastroDTO dto) {

        if (dto == null) {

            throw new UsuarioValidationException("Os dados do usuário são obrigatórios.");
        }

        String username = normalizarUsername(dto.username());

        verificarUsernameDisponivel(username);

        Usuario usuario = usuarioMapper.paraEntidade(dto);

        usuario.setUsername(username);
        usuario.setPasswordHash(dto.senha());

        validarUsuario(usuario);

        associarCliente(usuario, dto.tipoUsuario(), dto.clienteId());

        Usuario usuarioSalvo = usuarioRepo.save(usuario);

        return usuarioMapper.paraResponseDTO(usuarioSalvo);
    }

    /*
     * Lista todos os usuários.
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarUsuarios() {

        return usuarioRepo.findAll()
                .stream()
                .map(usuarioMapper::paraResponseDTO)
                .toList();
    }

    /*
     * Busca um usuário pelo ID.
     */
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(UUID id) {

        if (id == null) {

            throw new UsuarioValidationException("O ID do usuário é obrigatório.");
        }

        Usuario usuario = usuarioRepo.findById(id).orElseThrow(
                () -> new UsuarioNotFoundException("Nenhum usuário encontrado com esse ID: " + id));

        return usuarioMapper.paraResponseDTO(usuario);
    }

    /*
     * Busca um usuário pelo username.
     */
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscaPorUsername(String username) {

        String usernameNormalizado = normalizarUsername(username);

        Usuario usuario = usuarioRepo.findByUsername(usernameNormalizado).orElseThrow(
                () -> new UsuarioNotFoundException("Usuário: " + usernameNormalizado +
                        " não encontrado."));

        return usuarioMapper.paraResponseDTO(usuario);
    }

    /*
     * Atualiza os campos permitidos do usuário.
     */
    @Transactional
    public UsuarioResponseDTO atualizarUsuario(UUID id, UsuarioAtualizacaoDTO usuarioAttDTO) {

        if (id == null) {

            throw new UsuarioValidationException("O ID do usuário é obrigatório.");
        }

        if (usuarioAttDTO == null) {

            throw new UsuarioValidationException("Os dados do usuário para atualização não podem ser nulos.");
        }

        Usuario usuario = usuarioRepo.findById(id).orElseThrow(
                        () -> new UsuarioNotFoundException("Nenhum usuário encontrado com esse ID: "
                                + id));

        String usernameNormalizado = normalizarUsername(usuarioAttDTO.username());

        if (usuarioAttDTO.nome() == null || usuarioAttDTO.nome().isBlank()) {

            throw new UsuarioValidationException("O nome é obrigatório.");
        }

        if (!usuario.getUsername().equals(usernameNormalizado)) {

            verificarUsernameDisponivel(usernameNormalizado);
        }

        usuarioMapper.atualizarEntidade(usuarioAttDTO, usuario);

        // Garante que o username seja armazenado normalizado.
        usuario.setUsername(usernameNormalizado);

        Usuario usuarioSalvo = usuarioRepo.save(usuario);

        return usuarioMapper.paraResponseDTO(usuarioSalvo);
    }

    /*
     * Autentica um usuário.
     *
     * Implementação temporária.
     * A validação segura será realizada posteriormente
     * com PasswordEncoder e Spring Security.
     */
    @Transactional(readOnly = true)
    public Usuario autenticar(String username, String passwordHash) {

        String usernameNormalizado = normalizarUsername(username);

        if (passwordHash == null || passwordHash.isBlank()) {

            throw new UsuarioValidationException("Usuário ou senha inválidos.");
        }

        Usuario usuario = usuarioRepo.findByUsername(usernameNormalizado).orElseThrow(
                () -> new UsuarioValidationException("Usuário ou senha inválidos."));

        if (!usuario.getPasswordHash().equals(passwordHash)) {

            throw new UsuarioValidationException("Usuário ou senha inválidos.");
        }

        return usuario;
    }

    /*
     * Associa o usuário ao cliente correspondente.
     */
    private void associarCliente(Usuario usuario, TipoUsuario tipoUsuario, UUID clienteId) {

        if (tipoUsuario == TipoUsuario.CLIENTE) {

            if (clienteId == null) {

                throw new UsuarioValidationException("Usuário CLIENTE deve estar associado a um cliente.");
            }

            Cliente cliente = clienteRepo.findById(clienteId).orElseThrow(
                    () -> new ClienteNotFoundException("Cliente não encontrado."));

            usuario.setCliente(cliente);
            return;
        }

        if (tipoUsuario == TipoUsuario.ADMINISTRADOR) {

            if (clienteId != null) {

                throw new UsuarioValidationException("Usuário ADMINISTRADOR não pode possuir cliente associado.");
            }

            usuario.setCliente(null);
        }
    }

    /*
     * Valida os dados básicos do usuário.
     */
    private void validarUsuario(Usuario usuario) {

        if (usuario == null) {

            throw new UsuarioValidationException("Usuário não pode ser nulo.");
        }

        if (usuario.getUsername() == null || usuario.getUsername().isBlank()) {

            throw new UsuarioValidationException("O username é obrigatório.");
        }

        if (usuario.getUsername().length() > 100) {

            throw new UsuarioValidationException("O username deve ter no máximo 100 caracteres.");
        }

        if (usuario.getNome() == null || usuario.getNome().isBlank()) {

            throw new UsuarioValidationException("O nome é obrigatório.");
        }

        if (usuario.getNome().length() > 100) {

            throw new UsuarioValidationException("O nome deve ter no máximo 100 caracteres.");
        }

        if (usuario.getPasswordHash() == null || usuario.getPasswordHash().isBlank()) {

            throw new UsuarioValidationException("A senha é obrigatória.");
        }

        if (usuario.getTipoUsuario() == null) {

            throw new UsuarioValidationException("O tipoMedidor do usuário é obrigatório.");
        }
    }

    /*
     * Normaliza o username:
     * remove espaços nas extremidades e converte para minúsculas.
     */
    private String normalizarUsername(String username) {

        if (username == null || username.isBlank()) {

            throw new UsuarioValidationException("O username é obrigatório.");
        }

        String usernameNormalizado = username.trim().toLowerCase(Locale.ROOT);

        if (usernameNormalizado.length() > 100) {

            throw new UsuarioValidationException("O username deve ter no máximo 100 caracteres.");
        }

        return usernameNormalizado;
    }

    /*
     * Verifica se o username já está sendo utilizado.
     */
    private void verificarUsernameDisponivel(String username) {

        if (usuarioRepo.findByUsername(username).isPresent()) {

            throw new UsernameAlreadyExistsException("Username já está sendo utilizado: " + username);
        }
    }
}