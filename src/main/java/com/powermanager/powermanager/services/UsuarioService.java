
package com.powermanager.powermanager.services;

import com.powermanager.powermanager.dto.usuario.UsuarioAtualizacaoDTO;
import com.powermanager.powermanager.dto.usuario.UsuarioCadastroDTO;
import com.powermanager.powermanager.dto.usuario.UsuarioResponseDTO;
import com.powermanager.powermanager.entity.Cliente;
import com.powermanager.powermanager.entity.Usuario;
import com.powermanager.powermanager.enums.TipoUsuario;
import com.powermanager.powermanager.exception.UsernameAlreadyExistsException;
import com.powermanager.powermanager.exception.cliente.ClienteNotFoundException;
import com.powermanager.powermanager.exception.usuario.UsuarioNotFoundException;
import com.powermanager.powermanager.exception.usuario.UsuarioValidationException;
import com.powermanager.powermanager.mapper.UsuarioMapper;
import com.powermanager.powermanager.repository.ClienteRepo;
import com.powermanager.powermanager.repository.UsuarioRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    /**
     * Cadastra um novo usuário.
     */
    @Transactional
    public UsuarioResponseDTO cadastrarUsuario(UsuarioCadastroDTO dto) {

        if (dto == null) {

            throw new UsuarioValidationException("Os dados do usuário não podem ser nulos");
        }

        String username = normalizarUsername(dto.username());
        validarNome(dto.nome());
        validarSenha(dto.senha());

        if (dto.tipoUsuario() == null) {

            throw new UsuarioValidationException("O tipo de usuário é obrigatório");
        }

        verificarUsernameDisponivel(username);

        Cliente cliente = associarCliente(
                dto.tipoUsuario(),
                dto.clienteId());

        Usuario usuario = usuarioMapper.paraEntidade(dto);

        if (usuario == null) {

            throw new UsuarioValidationException("Não foi possível converter os dados do usuário");
        }

        usuario.setUsername(username);
        usuario.setNome(dto.nome().trim());
        usuario.setPasswordHash(passwordEncoder.encode(dto.senha()));
        usuario.setTipoUsuario(dto.tipoUsuario());
        usuario.setCliente(cliente);

        Usuario usuarioSalvo = usuarioRepo.save(usuario);

        return usuarioMapper.paraResponseDTO(usuarioSalvo);
    }

    /**
     * Lista todos os usuários.
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarUsuarios() {

        return usuarioRepo.findAll()
                .stream()
                .map(usuarioMapper::paraResponseDTO)
                .toList();
    }

    /**
     * Busca um usuário pelo identificador.
     */
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(UUID id) {

        if (id == null) {

            throw new UsuarioValidationException("O identificador do usuário não pode ser nulo");
        }

        Usuario usuario = buscarEntidadePorId(id);

        return usuarioMapper.paraResponseDTO(usuario);
    }

    /**
     * Busca um usuário pelo username.
     */
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscaPorUsername(String username) {

        String usernameNormalizado = normalizarUsername(username);

        Usuario usuario = usuarioRepo.findByUsername(usernameNormalizado)
                .orElseThrow(
                        () -> new UsuarioNotFoundException(
                                "Usuário não encontrado: " + usernameNormalizado)
                );

        return usuarioMapper.paraResponseDTO(usuario);
    }

    /**
     * Atualiza somente username e nome.
     * Senha, perfil e associação com cliente permanecem preservados.
     */
    @Transactional
    public UsuarioResponseDTO atualizarUsuario(UUID id, UsuarioAtualizacaoDTO dto) {

        if (id == null || dto == null) {

            throw new UsuarioValidationException("Identificador e dados de atualização são obrigatórios");
        }

        Usuario usuario = buscarEntidadePorId(id);

        String username = normalizarUsername(dto.username());
        validarNome(dto.nome());

        if (!username.equals(usuario.getUsername())) {

            verificarUsernameDisponivel(username);
        }

        UsuarioAtualizacaoDTO dtoNormalizado =
                new UsuarioAtualizacaoDTO(username, dto.nome().trim());

        usuarioMapper.atualizarEntidade(dtoNormalizado, usuario);

        Usuario usuarioAtualizado = usuarioRepo.save(usuario);

        return usuarioMapper.paraResponseDTO(usuarioAtualizado);
    }

    /**
     * Autentica um usuário utilizando a senha codificada.
     *
     * Retorna a entidade para uso interno no processo de autenticação.
     */
    @Transactional(readOnly = true)
    public Usuario autenticar(String username, String senha) {

        String usernameNormalizado = normalizarUsername(username);

        if (senha == null || senha.isBlank()) {

            throw new UsuarioValidationException("A senha é obrigatória");
        }

        Usuario usuario = usuarioRepo.findByUsername(usernameNormalizado)
                .orElseThrow(
                        () -> new UsuarioValidationException(
                                "Username ou senha inválidos")
                );

        if (!passwordEncoder.matches(senha, usuario.getPasswordHash())) {

            throw new UsuarioValidationException("Username ou senha inválidos");
        }

        return usuario;
    }

    /**
     * Busca a entidade ou lança exceção.
     */
    private Usuario buscarEntidadePorId(UUID id) {

        return usuarioRepo.findById(id)
                .orElseThrow(
                        () -> new UsuarioNotFoundException(
                                "Usuário não encontrado: " + id)
                );
    }

    /**
     * Verifica se o username já está cadastrado.
     */
    private void verificarUsernameDisponivel(String username) {

        if (usuarioRepo.findByUsername(username).isPresent()) {

            throw new UsernameAlreadyExistsException("Já existe um usuário cadastrado com esse username");
        }
    }

    /**
     * Valida e associa o cliente conforme o tipo de usuário.
     */
    private Cliente associarCliente(TipoUsuario tipoUsuario, UUID clienteId) {

        if (tipoUsuario == TipoUsuario.ADMINISTRADOR) {

            if (clienteId != null) {

                throw new UsuarioValidationException("Usuários administradores não devem possuir cliente associado");
            }

            return null;
        }

        if (tipoUsuario == TipoUsuario.CLIENTE) {

            if (clienteId == null) {

                throw new UsuarioValidationException("Usuários do tipo CLIENTE devem possuir um cliente associado");
            }

            if (usuarioRepo.existsByCliente_Id(clienteId)) {

                throw new UsuarioValidationException("Este cliente já possui um usuário associado");
            }

            return clienteRepo.findById(clienteId)
                    .orElseThrow(
                            () -> new ClienteNotFoundException(
                                    "Cliente não encontrado: " + clienteId)
                    );
        }

        throw new UsuarioValidationException(
                "Tipo de usuário inválido");
    }

    /**
     * Normaliza o username.
     */
    private String normalizarUsername(String username) {

        if (username == null || username.isBlank()) {

            throw new UsuarioValidationException("O username é obrigatório");
        }

        String usernameNormalizado =
                username.trim().toLowerCase(Locale.ROOT);

        if (usernameNormalizado.length() > 50) {

            throw new UsuarioValidationException("O username deve possuir no máximo 50 caracteres");
        }

        return usernameNormalizado;
    }

    /**
     * Valida o nome.
     */
    private void validarNome(String nome) {

        if (nome == null || nome.isBlank()) {

            throw new UsuarioValidationException("O nome é obrigatório");
        }

        if (nome.trim().length() > 100) {

            throw new UsuarioValidationException("O nome deve possuir no máximo 100 caracteres");
        }
    }

    /**
     * Valida a senha antes da codificação.
     */
    private void validarSenha(String senha) {

        if (senha == null || senha.isBlank()) {

            throw new UsuarioValidationException("A senha é obrigatória");
        }

        if (senha.length() < 8 || senha.length() > 72) {

            throw new UsuarioValidationException("A senha deve possuir entre 8 e 72 caracteres");
        }
    }
}
