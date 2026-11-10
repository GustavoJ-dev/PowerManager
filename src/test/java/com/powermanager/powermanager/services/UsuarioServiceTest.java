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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepo usuarioRepo;

    @Mock
    private ClienteRepo clienteRepo;

    @Mock
    private UsuarioMapper usuarioMapper;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void deveCadastrarUsuarioCliente() {

        UUID clienteId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        UsuarioCadastroDTO usuarioCadastroDTO = new UsuarioCadastroDTO(
                "joao",
                "João Silva",
                "senha123",
                TipoUsuario.CLIENTE,
                clienteId);

        Cliente cliente = new Cliente();

        Usuario usuario = new Usuario();
        usuario.setUsername("joao");
        usuario.setNome("João Silva");
        usuario.setPasswordHash("senha123");
        usuario.setTipoUsuario(TipoUsuario.CLIENTE);

        Usuario usuarioSalvo = new Usuario();

        UsuarioResponseDTO resposta = new UsuarioResponseDTO(
                usuarioId,
                "joao",
                "João Silva",
                TipoUsuario.CLIENTE,
                clienteId);

        when(usuarioRepo.findByUsername("joao")).thenReturn(Optional.empty());
        when(usuarioMapper.paraEntidade(usuarioCadastroDTO)).thenReturn(usuario);
        when(clienteRepo.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(usuarioRepo.save(usuario)).thenReturn(usuarioSalvo);
        when(usuarioMapper.paraResponseDTO(usuarioSalvo)).thenReturn(resposta);

        UsuarioResponseDTO resultado = usuarioService.cadastrarUsuario(usuarioCadastroDTO);

        assertEquals(resposta, resultado);

        verify(usuarioRepo).findByUsername("joao");
        verify(clienteRepo).findById(clienteId);
        verify(usuarioRepo).save(usuario);
        verify(usuarioMapper).paraEntidade(usuarioCadastroDTO);
        verify(usuarioMapper).paraResponseDTO(usuarioSalvo);

        assertSame(cliente, usuario.getCliente());
    }

    @Test
    void deveCadastrarAdministradorSemCliente() {

        UsuarioCadastroDTO dto = new UsuarioCadastroDTO(
                "admin",
                "Administrador",
                "senha123",
                TipoUsuario.ADMINISTRADOR,
                null);

        Usuario usuario = new Usuario();
        usuario.setUsername("admin");
        usuario.setNome("Administrador");
        usuario.setPasswordHash("senha123");
        usuario.setTipoUsuario(TipoUsuario.ADMINISTRADOR);

        Usuario usuarioSalvo = new Usuario();
        usuarioSalvo.setUsername("admin");
        usuarioSalvo.setNome("Administrador");
        usuarioSalvo.setPasswordHash("senha123");
        usuarioSalvo.setTipoUsuario(TipoUsuario.ADMINISTRADOR);

        UsuarioResponseDTO resposta = new UsuarioResponseDTO(
                UUID.randomUUID(),
                "admin",
                "Administrador",
                TipoUsuario.ADMINISTRADOR,
                null);

        when(usuarioRepo.findByUsername("admin")).thenReturn(Optional.empty());
        when(usuarioMapper.paraEntidade(dto)).thenReturn(usuario);
        when(usuarioRepo.save(usuario)).thenReturn(usuarioSalvo);
        when(usuarioMapper.paraResponseDTO(usuarioSalvo)).thenReturn(resposta);

        UsuarioResponseDTO resultado = usuarioService.cadastrarUsuario(dto);

        assertEquals(resposta, resultado);
        assertNull(usuario.getCliente());

        verify(clienteRepo, never()).findById(any());
        verify(usuarioRepo).save(usuario);
    }

    @Test
    void deveLancarExcecaoAoCadastrarUsuarioNulo(){

        assertThrows(UsuarioValidationException.class,
                ()-> usuarioService.cadastrarUsuario(null));

        verifyNoInteractions(usuarioRepo, clienteRepo, usuarioMapper);
    }

    @Test
    void deveLancarExcecaoQuandoUsernameJaExiste() {

        UsuarioCadastroDTO dto = new UsuarioCadastroDTO(
                "joao",
                "João Silva",
                "senha123",
                TipoUsuario.ADMINISTRADOR,
                null
        );

        when(usuarioRepo.findByUsername("joao"))
                .thenReturn(Optional.of(new Usuario()));

        assertThrows(
                UsernameAlreadyExistsException.class,
                () -> usuarioService.cadastrarUsuario(dto)
        );

        verify(usuarioRepo, never()).save(any());
        verifyNoInteractions(clienteRepo, usuarioMapper);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioClienteNaoPossuiClienteId() {

        UsuarioCadastroDTO dto = new UsuarioCadastroDTO(
                "joao",
                "João Silva",
                "senha123",
                TipoUsuario.CLIENTE,
                null
        );

        when(usuarioRepo.findByUsername("joao")).thenReturn(Optional.empty());
        when(usuarioMapper.paraEntidade(dto)).thenReturn(new Usuario());

        assertThrows(
                UsuarioValidationException.class,
                () -> usuarioService.cadastrarUsuario(dto)
        );

        verify(clienteRepo, never()).findById(any());
        verify(usuarioRepo, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoClienteNaoExiste() {

        UUID clienteId = UUID.randomUUID();

        UsuarioCadastroDTO dto = new UsuarioCadastroDTO(
                "joao",
                "João Silva",
                "senha123",
                TipoUsuario.CLIENTE,
                clienteId);

        Usuario usuario = new Usuario();
        usuario.setUsername("joao");
        usuario.setNome("João Silva");
        usuario.setPasswordHash("senha123");
        usuario.setTipoUsuario(TipoUsuario.CLIENTE);

        when(usuarioRepo.findByUsername("joao")).thenReturn(Optional.empty());
        when(usuarioMapper.paraEntidade(dto)).thenReturn(usuario);
        when(clienteRepo.findById(clienteId)).thenReturn(Optional.empty());

        assertThrows(ClienteNotFoundException.class,
                () -> usuarioService.cadastrarUsuario(dto));

        verify(usuarioRepo).findByUsername("joao");
        verify(usuarioMapper).paraEntidade(dto);
        verify(clienteRepo).findById(clienteId);
        verify(usuarioRepo, never()).save(any());
    }

    @Test
    void deveListarUsuarios() {

        Usuario usuario1 = new Usuario();
        Usuario usuario2 = new Usuario();

        UsuarioResponseDTO resposta1 = new UsuarioResponseDTO(
                UUID.randomUUID(), "joao", "João",
                TipoUsuario.CLIENTE, UUID.randomUUID()
        );

        UsuarioResponseDTO resposta2 = new UsuarioResponseDTO(
                UUID.randomUUID(), "admin", "Administrador",
                TipoUsuario.ADMINISTRADOR, null
        );

        when(usuarioRepo.findAll()).thenReturn(List.of(usuario1, usuario2));
        when(usuarioMapper.paraResponseDTO(usuario1)).thenReturn(resposta1);
        when(usuarioMapper.paraResponseDTO(usuario2)).thenReturn(resposta2);

        List<UsuarioResponseDTO> resultado = usuarioService.listarUsuarios();

        assertEquals(List.of(resposta1, resposta2), resultado);
        verify(usuarioRepo).findAll();
        verify(usuarioMapper).paraResponseDTO(usuario1);
        verify(usuarioMapper).paraResponseDTO(usuario2);
    }

    @Test
    void deveBuscarUsuarioPorId() {

        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario();

        UsuarioResponseDTO resposta = new UsuarioResponseDTO(
                id, "joao", "João",
                TipoUsuario.CLIENTE, null
        );

        when(usuarioRepo.findById(id)).thenReturn(Optional.of(usuario));
        when(usuarioMapper.paraResponseDTO(usuario)).thenReturn(resposta);

        assertEquals(resposta, usuarioService.buscarPorId(id));

        verify(usuarioRepo).findById(id);
        verify(usuarioMapper).paraResponseDTO(usuario);
    }

    @Test
    void deveLancarExcecaoAoBuscarUsuarioInexistente() {

        UUID id = UUID.randomUUID();

        when(usuarioRepo.findById(id)).thenReturn(Optional.empty());

        assertThrows(
                UsuarioNotFoundException.class,
                () -> usuarioService.buscarPorId(id)
        );

        verifyNoInteractions(usuarioMapper);
    }

    @Test
    void deveBuscarUsuarioPorUsername() {

        Usuario usuario = new Usuario();

        UsuarioResponseDTO resposta = new UsuarioResponseDTO(
                UUID.randomUUID(), "joao", "João",
                TipoUsuario.CLIENTE, null
        );

        when(usuarioRepo.findByUsername("joao")).thenReturn(Optional.of(usuario));
        when(usuarioMapper.paraResponseDTO(usuario)).thenReturn(resposta);

        assertEquals(resposta, usuarioService.buscaPorUsername("joao"));

        verify(usuarioRepo).findByUsername("joao");
        verify(usuarioMapper).paraResponseDTO(usuario);
    }

    @Test
    void deveLancarExcecaoAoBuscarUsernameInexistente() {

        when(usuarioRepo.findByUsername("inexistente"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsuarioNotFoundException.class,
                () -> usuarioService.buscaPorUsername("inexistente")
        );

        verifyNoInteractions(usuarioMapper);
    }

    @Test
    void deveAtualizarUsuario() {

        UUID id = UUID.randomUUID();

        UsuarioAtualizacaoDTO dto = new UsuarioAtualizacaoDTO(
                "novoUsername",
                "Novo Nome");

        Usuario usuario = new Usuario();
        usuario.setUsername("antigoUsername");
        usuario.setNome("Nome Antigo");
        usuario.setPasswordHash("hash-preservado");
        usuario.setTipoUsuario(TipoUsuario.ADMINISTRADOR);

        Usuario usuarioSalvo = new Usuario();

        UsuarioResponseDTO resposta = new UsuarioResponseDTO(
                id,
                "novoUsername",
                "Novo Nome",
                TipoUsuario.ADMINISTRADOR,
                null);

        when(usuarioRepo.findById(id)).thenReturn(Optional.of(usuario));
        when(usuarioRepo.findByUsername("novoUsername")).thenReturn(Optional.empty());

        doAnswer(invocation -> {
            UsuarioAtualizacaoDTO dados = invocation.getArgument(0);
            Usuario alvo = invocation.getArgument(1);
            alvo.setUsername(dados.username());
            alvo.setNome(dados.nome());
            return null;
        }).when(usuarioMapper).atualizarEntidade(dto, usuario);

        when(usuarioRepo.save(usuario)).thenReturn(usuarioSalvo);
        when(usuarioMapper.paraResponseDTO(usuarioSalvo)).thenReturn(resposta);

        assertEquals(resposta, usuarioService.atualizarUsuario(id, dto));

        assertEquals("hash-preservado", usuario.getPasswordHash());
        assertEquals(TipoUsuario.ADMINISTRADOR, usuario.getTipoUsuario());

        verify(usuarioMapper).atualizarEntidade(dto, usuario);
        verify(usuarioRepo).save(usuario);
    }

    @Test
    void deveLancarExcecaoAoAtualizarUsuarioNulo() {

        assertThrows(UsuarioValidationException.class,
                () -> usuarioService.atualizarUsuario(UUID.randomUUID(), null));

        verifyNoInteractions(usuarioRepo, clienteRepo, usuarioMapper);
    }

    @Test
    void deveLancarExcecaoAoAtualizarUsuarioInexistente() {

        UUID id = UUID.randomUUID();

        when(usuarioRepo.findById(id)).thenReturn(Optional.empty());

        assertThrows(
                UsuarioNotFoundException.class,
                () -> usuarioService.atualizarUsuario(
                        id,
                        new UsuarioAtualizacaoDTO("novo", "Novo Nome")));

        verifyNoInteractions(usuarioMapper);
    }

    @Test
    void deveLancarExcecaoQuandoUsernameAtualizadoJaExiste() {

        UUID id = UUID.randomUUID();

        Usuario usuario = new Usuario();
        usuario.setUsername("antigo");

        UsuarioAtualizacaoDTO dto = new UsuarioAtualizacaoDTO(
                "ocupado",
                "Novo Nome");

        when(usuarioRepo.findById(id)).thenReturn(Optional.of(usuario));
        when(usuarioRepo.findByUsername("ocupado"))
                .thenReturn(Optional.of(new Usuario()));

        assertThrows(UsernameAlreadyExistsException.class,
                () -> usuarioService.atualizarUsuario(id, dto));

        verify(usuarioMapper, never()).atualizarEntidade(any(), any());
        verify(usuarioRepo, never()).save(any());
    }

    @Test
    void deveAutenticarUsuario() {

        Usuario usuario = new Usuario();
        usuario.setPasswordHash("senha-atual");

        when(usuarioRepo.findByUsername("joao")).thenReturn(Optional.of(usuario));

        assertSame(usuario, usuarioService.autenticar("joao", "senha-atual"));
    }

    @Test
    void deveLancarExcecaoQuandoSenhaEstiverIncorreta() {

        Usuario usuario = new Usuario();
        usuario.setPasswordHash("senha-correta");

        when(usuarioRepo.findByUsername("joao")).thenReturn(Optional.of(usuario));

        assertThrows(
                UsuarioValidationException.class,
                () -> usuarioService.autenticar("joao", "senha-incorreta"));
    }

}
