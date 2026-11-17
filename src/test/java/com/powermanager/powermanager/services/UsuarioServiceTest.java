
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepo usuarioRepo;

    @Mock
    private ClienteRepo clienteRepo;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private UUID usuarioId;
    private UUID clienteId;

    private Usuario usuario;
    private Cliente cliente;

    private UsuarioResponseDTO responseDTO;

    @BeforeEach
    void setUp() {

        usuarioId = UUID.randomUUID();
        clienteId = UUID.randomUUID();

        cliente = new Cliente();
        cliente.setId(clienteId);
        cliente.setNome("Cliente Teste");

        usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setUsername("usuario.teste");
        usuario.setNome("Usuário Teste");
        usuario.setPasswordHash("senhaCodificada");
        usuario.setTipoUsuario(TipoUsuario.ADMINISTRADOR);

        responseDTO = new UsuarioResponseDTO(
                usuarioId,
                "usuario.teste",
                "Usuário Teste",
                TipoUsuario.ADMINISTRADOR,
                null
        );
    }

    @Test
    void deveCadastrarAdministradorComSucesso() {

        UsuarioCadastroDTO dto = new UsuarioCadastroDTO(
                "Admin.Teste",
                "Administrador Teste",
                "senha123",
                TipoUsuario.ADMINISTRADOR,
                null
        );

        when(usuarioRepo.findByUsername("admin.teste"))
                .thenReturn(Optional.empty());

        when(usuarioMapper.paraEntidade(dto))
                .thenReturn(new Usuario());

        when(passwordEncoder.encode("senha123"))
                .thenReturn("senhaCodificada");

        when(usuarioRepo.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(usuarioMapper.paraResponseDTO(any(Usuario.class)))
                .thenReturn(responseDTO);

        UsuarioResponseDTO resultado = usuarioService.cadastrarUsuario(dto);

        assertNotNull(resultado);
        assertEquals(usuarioId, resultado.id());

        ArgumentCaptor<Usuario> captor =
                ArgumentCaptor.forClass(Usuario.class);

        verify(usuarioRepo).save(captor.capture());

        Usuario salvo = captor.getValue();

        assertEquals("admin.teste", salvo.getUsername());
        assertEquals("Administrador Teste", salvo.getNome());
        assertEquals("senhaCodificada", salvo.getPasswordHash());
        assertEquals(TipoUsuario.ADMINISTRADOR, salvo.getTipoUsuario());
        assertNull(salvo.getCliente());

        verify(passwordEncoder).encode("senha123");
    }

    @Test
    void deveCadastrarUsuarioClienteComSucesso() {

        UsuarioCadastroDTO dto = new UsuarioCadastroDTO(
                "cliente.teste",
                "Cliente Teste",
                "senha123",
                TipoUsuario.CLIENTE,
                clienteId
        );

        UsuarioResponseDTO resposta = new UsuarioResponseDTO(
                usuarioId,
                "cliente.teste",
                "Cliente Teste",
                TipoUsuario.CLIENTE,
                clienteId
        );

        when(usuarioRepo.findByUsername("cliente.teste"))
                .thenReturn(Optional.empty());

        when(usuarioRepo.existsByCliente_Id(clienteId))
                .thenReturn(false);

        when(clienteRepo.findById(clienteId))
                .thenReturn(Optional.of(cliente));

        when(usuarioMapper.paraEntidade(dto))
                .thenReturn(new Usuario());

        when(passwordEncoder.encode("senha123"))
                .thenReturn("senhaCodificada");

        when(usuarioRepo.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(usuarioMapper.paraResponseDTO(any(Usuario.class)))
                .thenReturn(resposta);

        UsuarioResponseDTO resultado = usuarioService.cadastrarUsuario(dto);

        assertNotNull(resultado);
        assertEquals(TipoUsuario.CLIENTE, resultado.tipoUsuario());
        assertEquals(clienteId, resultado.clienteId());

        ArgumentCaptor<Usuario> captor =
                ArgumentCaptor.forClass(Usuario.class);

        verify(usuarioRepo).save(captor.capture());

        assertEquals(cliente, captor.getValue().getCliente());
    }

    @Test
    void naoDeveCadastrarUsuarioComDadosNulos() {

        assertThrows(
                UsuarioValidationException.class,
                () -> usuarioService.cadastrarUsuario(null)
        );

        verifyNoInteractions(usuarioRepo, clienteRepo, usuarioMapper);
    }

    @Test
    void naoDeveCadastrarUsuarioComUsernameDuplicado() {

        UsuarioCadastroDTO dto = new UsuarioCadastroDTO(
                "usuario.teste",
                "Usuário Teste",
                "senha123",
                TipoUsuario.ADMINISTRADOR,
                null
        );

        when(usuarioRepo.findByUsername("usuario.teste"))
                .thenReturn(Optional.of(usuario));

        assertThrows(
                UsernameAlreadyExistsException.class,
                () -> usuarioService.cadastrarUsuario(dto)
        );

        verify(usuarioRepo, never()).save(any());
        verifyNoInteractions(usuarioMapper, passwordEncoder, clienteRepo);
    }

    @Test
    void naoDeveCadastrarClienteSemClienteId() {

        UsuarioCadastroDTO dto = new UsuarioCadastroDTO(
                "cliente.teste",
                "Cliente Teste",
                "senha123",
                TipoUsuario.CLIENTE,
                null
        );

        when(usuarioRepo.findByUsername("cliente.teste"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsuarioValidationException.class,
                () -> usuarioService.cadastrarUsuario(dto)
        );

        verify(usuarioRepo, never()).save(any());
        verifyNoInteractions(clienteRepo, usuarioMapper, passwordEncoder);
    }

    @Test
    void naoDeveCadastrarClienteInexistente() {

        UsuarioCadastroDTO dto = new UsuarioCadastroDTO(
                "cliente.teste",
                "Cliente Teste",
                "senha123",
                TipoUsuario.CLIENTE,
                clienteId
        );

        when(usuarioRepo.findByUsername("cliente.teste"))
                .thenReturn(Optional.empty());

        when(usuarioRepo.existsByCliente_Id(clienteId))
                .thenReturn(false);

        when(clienteRepo.findById(clienteId))
                .thenReturn(Optional.empty());

        assertThrows(
                ClienteNotFoundException.class,
                () -> usuarioService.cadastrarUsuario(dto)
        );

        verify(usuarioRepo, never()).save(any());
        verifyNoInteractions(usuarioMapper, passwordEncoder);
    }

    @Test
    void naoDeveAssociarClienteQueJaPossuiUsuario() {

        UsuarioCadastroDTO dto = new UsuarioCadastroDTO(
                "cliente.teste",
                "Cliente Teste",
                "senha123",
                TipoUsuario.CLIENTE,
                clienteId
        );

        when(usuarioRepo.findByUsername("cliente.teste"))
                .thenReturn(Optional.empty());

        when(usuarioRepo.existsByCliente_Id(clienteId))
                .thenReturn(true);

        assertThrows(
                UsuarioValidationException.class,
                () -> usuarioService.cadastrarUsuario(dto)
        );

        verify(clienteRepo, never()).findById(any());
        verify(usuarioRepo, never()).save(any());
        verifyNoInteractions(usuarioMapper, passwordEncoder);
    }

    @Test
    void naoDeveCadastrarAdministradorComClienteAssociado() {

        UsuarioCadastroDTO dto = new UsuarioCadastroDTO(
                "admin.teste",
                "Administrador Teste",
                "senha123",
                TipoUsuario.ADMINISTRADOR,
                clienteId
        );

        when(usuarioRepo.findByUsername("admin.teste"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsuarioValidationException.class,
                () -> usuarioService.cadastrarUsuario(dto)
        );

        verify(usuarioRepo, never()).save(any());
        verifyNoInteractions(clienteRepo, usuarioMapper, passwordEncoder);
    }

    @Test
    void naoDeveCadastrarUsuarioComUsernameVazio() {

        UsuarioCadastroDTO dto = new UsuarioCadastroDTO(
                " ",
                "Usuário Teste",
                "senha123",
                TipoUsuario.ADMINISTRADOR,
                null
        );

        assertThrows(
                UsuarioValidationException.class,
                () -> usuarioService.cadastrarUsuario(dto)
        );

        verifyNoInteractions(usuarioRepo, clienteRepo, usuarioMapper, passwordEncoder);
    }

    @Test
    void naoDeveCadastrarUsuarioComUsernameMaiorQue50Caracteres() {

        UsuarioCadastroDTO dto = new UsuarioCadastroDTO(
                "a".repeat(51),
                "Usuário Teste",
                "senha123",
                TipoUsuario.ADMINISTRADOR,
                null
        );

        assertThrows(
                UsuarioValidationException.class,
                () -> usuarioService.cadastrarUsuario(dto)
        );

        verifyNoInteractions(usuarioRepo, clienteRepo, usuarioMapper, passwordEncoder);
    }

    @Test
    void naoDeveCadastrarUsuarioComNomeVazio() {

        UsuarioCadastroDTO dto = new UsuarioCadastroDTO(
                "usuario.teste",
                " ",
                "senha123",
                TipoUsuario.ADMINISTRADOR,
                null
        );

        assertThrows(
                UsuarioValidationException.class,
                () -> usuarioService.cadastrarUsuario(dto)
        );

        verifyNoInteractions(usuarioRepo, clienteRepo, usuarioMapper, passwordEncoder);
    }

    @Test
    void naoDeveCadastrarUsuarioComNomeMaiorQue100Caracteres() {

        UsuarioCadastroDTO dto = new UsuarioCadastroDTO(
                "usuario.teste",
                "A".repeat(101),
                "senha123",
                TipoUsuario.ADMINISTRADOR,
                null
        );

        assertThrows(
                UsuarioValidationException.class,
                () -> usuarioService.cadastrarUsuario(dto)
        );

        verifyNoInteractions(usuarioRepo, clienteRepo, usuarioMapper, passwordEncoder);
    }

    @Test
    void naoDeveCadastrarUsuarioComSenhaMenorQue8Caracteres() {

        UsuarioCadastroDTO dto = new UsuarioCadastroDTO(
                "usuario.teste",
                "Usuário Teste",
                "1234567",
                TipoUsuario.ADMINISTRADOR,
                null
        );

        assertThrows(
                UsuarioValidationException.class,
                () -> usuarioService.cadastrarUsuario(dto)
        );

        verifyNoInteractions(usuarioRepo, clienteRepo, usuarioMapper, passwordEncoder);
    }

    @Test
    void naoDeveCadastrarUsuarioComSenhaMaiorQue72Caracteres() {

        UsuarioCadastroDTO dto = new UsuarioCadastroDTO(
                "usuario.teste",
                "Usuário Teste",
                "a".repeat(73),
                TipoUsuario.ADMINISTRADOR,
                null
        );

        assertThrows(
                UsuarioValidationException.class,
                () -> usuarioService.cadastrarUsuario(dto)
        );

        verifyNoInteractions(usuarioRepo, clienteRepo, usuarioMapper, passwordEncoder);
    }

    @Test
    void naoDeveCadastrarUsuarioSemTipo() {

        UsuarioCadastroDTO dto = new UsuarioCadastroDTO(
                "usuario.teste",
                "Usuário Teste",
                "senha123",
                null,
                null
        );

        assertThrows(
                UsuarioValidationException.class,
                () -> usuarioService.cadastrarUsuario(dto)
        );

        verifyNoInteractions(usuarioRepo, clienteRepo, usuarioMapper, passwordEncoder);
    }

    @Test
    void deveListarTodosOsUsuarios() {

        Usuario segundoUsuario = new Usuario();

        UsuarioResponseDTO segundaResposta = new UsuarioResponseDTO(
                UUID.randomUUID(),
                "outro.usuario",
                "Outro Usuário",
                TipoUsuario.ADMINISTRADOR,
                null
        );

        when(usuarioRepo.findAll())
                .thenReturn(List.of(usuario, segundoUsuario));

        when(usuarioMapper.paraResponseDTO(usuario))
                .thenReturn(responseDTO);

        when(usuarioMapper.paraResponseDTO(segundoUsuario))
                .thenReturn(segundaResposta);

        List<UsuarioResponseDTO> resultado = usuarioService.listarUsuarios();

        assertEquals(2, resultado.size());
        assertEquals(responseDTO, resultado.get(0));
        assertEquals(segundaResposta, resultado.get(1));

        verify(usuarioRepo).findAll();
        verify(usuarioMapper).paraResponseDTO(usuario);
        verify(usuarioMapper).paraResponseDTO(segundoUsuario);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremUsuarios() {

        when(usuarioRepo.findAll())
                .thenReturn(List.of());

        List<UsuarioResponseDTO> resultado = usuarioService.listarUsuarios();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(usuarioRepo).findAll();
        verifyNoInteractions(usuarioMapper);
    }

    @Test
    void deveBuscarUsuarioPorId() {

        when(usuarioRepo.findById(usuarioId))
                .thenReturn(Optional.of(usuario));

        when(usuarioMapper.paraResponseDTO(usuario))
                .thenReturn(responseDTO);

        UsuarioResponseDTO resultado = usuarioService.buscarPorId(usuarioId);

        assertNotNull(resultado);
        assertEquals(usuarioId, resultado.id());

        verify(usuarioRepo).findById(usuarioId);
        verify(usuarioMapper).paraResponseDTO(usuario);
    }

    @Test
    void naoDeveBuscarUsuarioComIdNulo() {

        assertThrows(
                UsuarioValidationException.class,
                () -> usuarioService.buscarPorId(null)
        );

        verifyNoInteractions(usuarioRepo, usuarioMapper);
    }

    @Test
    void naoDeveBuscarUsuarioComIdInexistente() {

        when(usuarioRepo.findById(usuarioId))
                .thenReturn(Optional.empty());

        assertThrows(
                UsuarioNotFoundException.class,
                () -> usuarioService.buscarPorId(usuarioId)
        );

        verify(usuarioRepo).findById(usuarioId);
        verifyNoInteractions(usuarioMapper);
    }

    // =========================================================
    // BUSCA POR USERNAME
    // =========================================================

    @Test
    void deveBuscarUsuarioPorUsername() {

        when(usuarioRepo.findByUsername("usuario.teste"))
                .thenReturn(Optional.of(usuario));

        when(usuarioMapper.paraResponseDTO(usuario))
                .thenReturn(responseDTO);

        UsuarioResponseDTO resultado =
                usuarioService.buscaPorUsername("Usuario.Teste");

        assertNotNull(resultado);
        assertEquals("usuario.teste", resultado.username());

        verify(usuarioRepo).findByUsername("usuario.teste");
        verify(usuarioMapper).paraResponseDTO(usuario);
    }

    @Test
    void naoDeveBuscarUsuarioComUsernameInexistente() {

        when(usuarioRepo.findByUsername("inexistente"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsuarioNotFoundException.class,
                () -> usuarioService.buscaPorUsername("inexistente")
        );

        verify(usuarioRepo).findByUsername("inexistente");
        verifyNoInteractions(usuarioMapper);
    }

    @Test
    void deveAtualizarUsuarioComSucesso() {

        UsuarioAtualizacaoDTO dto = new UsuarioAtualizacaoDTO(
                "usuario.teste",
                "Nome Atualizado"
        );

        when(usuarioRepo.findById(usuarioId))
                .thenReturn(Optional.of(usuario));

        when(usuarioRepo.save(usuario))
                .thenReturn(usuario);

        when(usuarioMapper.paraResponseDTO(usuario))
                .thenReturn(responseDTO);

        UsuarioResponseDTO resultado =
                usuarioService.atualizarUsuario(usuarioId, dto);

        assertNotNull(resultado);

        verify(usuarioMapper).atualizarEntidade(
                new UsuarioAtualizacaoDTO(
                        "usuario.teste",
                        "Nome Atualizado"
                ),
                usuario
        );

        verify(usuarioRepo).save(usuario);
        verify(usuarioRepo, never()).findByUsername(any());
    }

    @Test
    void deveAtualizarUsernameEVerificarDisponibilidade() {

        UsuarioAtualizacaoDTO dto = new UsuarioAtualizacaoDTO(
                "Novo.Usuario",
                "Nome Atualizado"
        );

        when(usuarioRepo.findById(usuarioId))
                .thenReturn(Optional.of(usuario));

        when(usuarioRepo.findByUsername("novo.usuario"))
                .thenReturn(Optional.empty());

        when(usuarioRepo.save(usuario))
                .thenReturn(usuario);

        when(usuarioMapper.paraResponseDTO(usuario))
                .thenReturn(responseDTO);

        usuarioService.atualizarUsuario(usuarioId, dto);

        verify(usuarioRepo).findByUsername("novo.usuario");

        verify(usuarioMapper).atualizarEntidade(
                new UsuarioAtualizacaoDTO(
                        "novo.usuario",
                        "Nome Atualizado"
                ),
                usuario
        );

        verify(usuarioRepo).save(usuario);
    }

    @Test
    void naoDeveAtualizarParaUsernameDeOutroUsuario() {

        Usuario outroUsuario = new Usuario();

        UsuarioAtualizacaoDTO dto = new UsuarioAtualizacaoDTO(
                "outro.usuario",
                "Nome Atualizado"
        );

        when(usuarioRepo.findById(usuarioId))
                .thenReturn(Optional.of(usuario));

        when(usuarioRepo.findByUsername("outro.usuario"))
                .thenReturn(Optional.of(outroUsuario));

        assertThrows(
                UsernameAlreadyExistsException.class,
                () -> usuarioService.atualizarUsuario(usuarioId, dto)
        );

        verify(usuarioRepo, never()).save(any());
        verifyNoInteractions(usuarioMapper);
    }

    @Test
    void naoDeveAtualizarUsuarioComIdNulo() {

        UsuarioAtualizacaoDTO dto = new UsuarioAtualizacaoDTO(
                "usuario.teste",
                "Nome Atualizado"
        );

        assertThrows(
                UsuarioValidationException.class,
                () -> usuarioService.atualizarUsuario(null, dto)
        );

        verifyNoInteractions(usuarioRepo, usuarioMapper);
    }

    @Test
    void naoDeveAtualizarUsuarioComDadosNulos() {

        assertThrows(
                UsuarioValidationException.class,
                () -> usuarioService.atualizarUsuario(usuarioId, null)
        );

        verifyNoInteractions(usuarioRepo, usuarioMapper);
    }

    @Test
    void naoDeveAtualizarUsuarioInexistente() {

        UsuarioAtualizacaoDTO dto = new UsuarioAtualizacaoDTO(
                "usuario.teste",
                "Nome Atualizado"
        );

        when(usuarioRepo.findById(usuarioId))
                .thenReturn(Optional.empty());

        assertThrows(
                UsuarioNotFoundException.class,
                () -> usuarioService.atualizarUsuario(usuarioId, dto)
        );

        verify(usuarioRepo, never()).save(any());
        verifyNoInteractions(usuarioMapper);
    }

    @Test
    void devePreservarSenhaPerfilEClienteAoAtualizar() {

        usuario.setTipoUsuario(TipoUsuario.CLIENTE);
        usuario.setCliente(cliente);

        String senhaOriginal = usuario.getPasswordHash();

        UsuarioAtualizacaoDTO dto = new UsuarioAtualizacaoDTO(
                "novo.username",
                "Novo Nome"
        );

        when(usuarioRepo.findById(usuarioId))
                .thenReturn(Optional.of(usuario));

        when(usuarioRepo.findByUsername("novo.username"))
                .thenReturn(Optional.empty());

        when(usuarioRepo.save(usuario))
                .thenReturn(usuario);

        when(usuarioMapper.paraResponseDTO(usuario))
                .thenReturn(new UsuarioResponseDTO(
                        usuarioId,
                        "novo.username",
                        "Novo Nome",
                        TipoUsuario.CLIENTE,
                        clienteId
                ));

        usuarioService.atualizarUsuario(usuarioId, dto);

        assertEquals(senhaOriginal, usuario.getPasswordHash());
        assertEquals(TipoUsuario.CLIENTE, usuario.getTipoUsuario());
        assertEquals(cliente, usuario.getCliente());
    }

    @Test
    void deveAutenticarUsuarioComCredenciaisCorretas() {

        usuario.setPasswordHash("senhaCodificada");

        when(usuarioRepo.findByUsername("usuario.teste"))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches("senha123", "senhaCodificada"))
                .thenReturn(true);

        Usuario resultado =
                usuarioService.autenticar("Usuario.Teste", "senha123");

        assertNotNull(resultado);
        assertEquals(usuarioId, resultado.getId());

        verify(usuarioRepo).findByUsername("usuario.teste");
        verify(passwordEncoder).matches("senha123", "senhaCodificada");
    }

    @Test
    void naoDeveAutenticarUsuarioComSenhaIncorreta() {

        usuario.setPasswordHash("senhaCodificada");

        when(usuarioRepo.findByUsername("usuario.teste"))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches("senhaErrada", "senhaCodificada"))
                .thenReturn(false);

        assertThrows(
                UsuarioValidationException.class,
                () -> usuarioService.autenticar(
                        "usuario.teste",
                        "senhaErrada"
                )
        );

        verify(passwordEncoder).matches(
                "senhaErrada",
                "senhaCodificada"
        );
    }

    @Test
    void naoDeveAutenticarUsuarioInexistente() {

        when(usuarioRepo.findByUsername("inexistente"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsuarioValidationException.class,
                () -> usuarioService.autenticar(
                        "inexistente",
                        "senha123"
                )
        );

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void naoDeveAutenticarComSenhaVazia() {

        assertThrows(
                UsuarioValidationException.class,
                () -> usuarioService.autenticar(
                        "usuario.teste",
                        " "
                )
        );

        verifyNoInteractions(usuarioRepo, passwordEncoder);
    }
}

