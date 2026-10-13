package com.powermanager.powermanager.services;

import com.powermanager.powermanager.entity.Cliente;
import com.powermanager.powermanager.entity.Usuario;
import com.powermanager.powermanager.entity.enums.TipoUsuario;
import com.powermanager.powermanager.exception.UsernameAlreadyExistsException;
import com.powermanager.powermanager.exception.UsuarioNotFoundException;
import com.powermanager.powermanager.exception.UsuarioValidationException;
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
    private ClienteService clienteService;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario criarUsuarioValido(){

        Usuario usuario = new Usuario();

        usuario.setUsername("usuario.teste");
        usuario.setNome("Usuário Teste");
        usuario.setPasswordHash("hash-teste");
        usuario.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
        return usuario;
    }

    private Cliente criarClienteValido(){

        Cliente cliente = new Cliente();
        cliente.setId(UUID.randomUUID());
        return cliente;
    }

    @Test
    void deveCadastrarAdministrador(){

        Usuario usuario = criarUsuarioValido();

        when(usuarioRepo.findByUsername(usuario.getUsername())).thenReturn(Optional.empty());

        when(usuarioRepo.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.cadastrarUsuario(usuario);

        assertNotNull(resultado);
        assertEquals(TipoUsuario.ADMINISTRADOR, resultado.getTipoUsuario());
        assertNull(resultado.getCliente());

        verify(usuarioRepo).save(usuario);
        verify(clienteService, never()).buscarPorId(any());
    }

    @Test
    void deveCadastrarUsuarioClienteAssociadoAoClienteExistente(){

        Usuario usuario = criarUsuarioValido();
        usuario.setTipoUsuario(TipoUsuario.CLIENTE);

        Cliente clienteRef = criarClienteValido();
        Cliente clienteEncontrado = criarClienteValido();
        usuario.setCliente(clienteRef);

        when(usuarioRepo.findByUsername(usuario.getUsername())).thenReturn(Optional.empty());

        when(clienteService.buscarPorId(clienteRef.getId())).thenReturn(clienteEncontrado);

        when(usuarioRepo.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.cadastrarUsuario(usuario);

        assertNotNull(resultado);
        assertSame(clienteEncontrado, resultado.getCliente());

        verify(clienteService).buscarPorId(clienteRef.getId());
        verify(usuarioRepo).save(usuario);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioForNulo(){

        assertThrows(UsuarioValidationException.class,
                ()-> usuarioService.cadastrarUsuario(null));

        verify(usuarioRepo, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoUsernameForNulo(){

        Usuario usuario = criarUsuarioValido();
        usuario.setUsername(null);

        assertThrows(UsuarioValidationException.class,
                ()-> usuarioService.cadastrarUsuario(usuario));

        verify(usuarioRepo, never()).findByUsername(anyString());
        verify(usuarioRepo, never()).save(any(Usuario.class));
    }

    @Test
    void deveLancarExcecaoQuandoNomeForNulo() {

        Usuario usuario = criarUsuarioValido();
        usuario.setNome(null);

        assertThrows(UsuarioValidationException.class,
                () -> usuarioService.cadastrarUsuario(usuario));

        verify(usuarioRepo, never()).findByUsername(anyString());
        verify(usuarioRepo, never()).save(any(Usuario.class));
    }

    @Test
    void deveLancarExcecaoQuandoUsernameEstiverBranco(){

        Usuario usuario = criarUsuarioValido();
        usuario.setUsername("   ");

        assertThrows(UsuarioValidationException.class,
                ()->usuarioService.cadastrarUsuario(usuario));

        verify(usuarioRepo, never()).findByUsername(anyString());
        verify(usuarioRepo, never()).save(any(Usuario.class));
    }

    @Test
    void deveLancarExcecaoQuandoSenhaForNula() {

        Usuario usuario = criarUsuarioValido();
        usuario.setPasswordHash(null);

        assertThrows(UsuarioValidationException.class,
                () -> usuarioService.cadastrarUsuario(usuario));

        verify(usuarioRepo, never()).findByUsername(anyString());
        verify(usuarioRepo, never()).save(any(Usuario.class));
    }

    @Test
    void deveLancarExcecaoQuandoSenhaEstiverEmBranco() {

        Usuario usuario = criarUsuarioValido();
        usuario.setPasswordHash(" ");

        assertThrows(UsuarioValidationException.class,
                () -> usuarioService.cadastrarUsuario(usuario));

        verify(usuarioRepo, never()).findByUsername(anyString());
        verify(usuarioRepo, never()).save(any(Usuario.class));
    }

    @Test
    void deveLancarExcecaoQuandoNomeForBlank() {

        Usuario usuario = criarUsuarioValido();
        usuario.setNome("   ");

        assertThrows(UsuarioValidationException.class,
                () -> usuarioService.cadastrarUsuario(usuario));

        verify(usuarioRepo, never()).save(any(Usuario.class));
    }

    @Test
    void deveLancarExcecaoQuandoTipoUsuarioForNulo() {

        Usuario usuario = criarUsuarioValido();
        usuario.setTipoUsuario(null);

        assertThrows(UsuarioValidationException.class,
                () -> usuarioService.cadastrarUsuario(usuario));

        verify(usuarioRepo, never()).findByUsername(anyString());
        verify(usuarioRepo, never()).save(any(Usuario.class));
    }

    @Test
    void deveLancarExcecaoQuandoUsernameJaExistirNoCadastro() {

        Usuario usuario = criarUsuarioValido();

        when(usuarioRepo.findByUsername(usuario.getUsername()))
                .thenReturn(Optional.of(criarUsuarioValido()));

        assertThrows(UsernameAlreadyExistsException.class,
                () -> usuarioService.cadastrarUsuario(usuario));

        verify(usuarioRepo, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioClienteNaoPossuirCliente() {

        Usuario usuario = criarUsuarioValido();
        usuario.setTipoUsuario(TipoUsuario.CLIENTE);
        usuario.setCliente(null);

        when(usuarioRepo.findByUsername(usuario.getUsername()))
                .thenReturn(Optional.empty());

        assertThrows(UsuarioValidationException.class,
                () -> usuarioService.cadastrarUsuario(usuario));

        verify(usuarioRepo, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoClienteNaoPossuirId() {

        Usuario usuario = criarUsuarioValido();
        usuario.setTipoUsuario(TipoUsuario.CLIENTE);
        usuario.setCliente(new Cliente());

        when(usuarioRepo.findByUsername(usuario.getUsername())).thenReturn(Optional.empty());

        assertThrows(UsuarioValidationException.class,
                () -> usuarioService.cadastrarUsuario(usuario));

        verify(clienteService, never()).buscarPorId(any());
        verify(usuarioRepo, never()).save(any());
    }

    @Test
    void deveListarUsuarios() {

        List<Usuario> usuarios = List.of(criarUsuarioValido(), criarUsuarioValido());

        when(usuarioRepo.findAll()).thenReturn(usuarios);

        List<Usuario> resultado = usuarioService.listarUsuarios();

        assertEquals(2, resultado.size());

        assertSame(usuarios, resultado);

        verify(usuarioRepo).findAll();
    }

    @Test
    void deveBuscarUsuarioPorId() {

        UUID id = UUID.randomUUID();

        Usuario usuario = criarUsuarioValido();

        when(usuarioRepo.findById(id)).thenReturn(Optional.of(usuario));

        Usuario resultado = usuarioService.buscarPorId(id);

        assertSame(usuario, resultado);

        verify(usuarioRepo).findById(id);
    }

    @Test
    void deveLancarExcecaoQuandoIdNaoEncontrado() {

        UUID id = UUID.randomUUID();

        when(usuarioRepo.findById(id)).thenReturn(Optional.empty());

        assertThrows(UsuarioNotFoundException.class,
                () -> usuarioService.buscarPorId(id));

        verify(usuarioRepo).findById(id);
        verify(usuarioRepo, never()).save(any(Usuario.class));
    }

    @Test
    void deveBuscarUsuarioPorUsername() {

        Usuario usuario = criarUsuarioValido();

        when(usuarioRepo.findByUsername(usuario.getUsername())).thenReturn(Optional.of(usuario));

        Usuario resultado = usuarioService.buscaPorUsername(usuario.getUsername());

        assertSame(usuario, resultado);

        verify(usuarioRepo).findByUsername(usuario.getUsername());
    }

    @Test
    void deveLancarExcecaoQuandoUsernameNaoForEncontrado() {

        String username = "inexistente";

        when(usuarioRepo.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UsuarioNotFoundException.class,
                () -> usuarioService.buscaPorUsername(username));

        verify(usuarioRepo).findByUsername(username);
        verify(usuarioRepo, never()).save(any(Usuario.class));
    }

    @Test
    void deveAtualizarNomeEUsername() {

        UUID id = UUID.randomUUID();
        Usuario usuarioExistente = criarUsuarioValido();

        Usuario dadosAtualizados = new Usuario();
        dadosAtualizados.setUsername("novo.username");
        dadosAtualizados.setNome("Novo Nome");

        when(usuarioRepo.findById(id)).thenReturn(Optional.of(usuarioExistente));

        when(usuarioRepo.findByUsername("novo.username")).thenReturn(Optional.empty());

        when(usuarioRepo.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.atualizarUsuario(id, dadosAtualizados);

        assertEquals("novo.username", resultado.getUsername());
        assertEquals("Novo Nome", resultado.getNome());

        verify(usuarioRepo).save(usuarioExistente);
    }

    @Test
    void deveAtualizarNomeSemVerificarUsernameQuandoNaoMudar() {

        UUID id = UUID.randomUUID();
        Usuario usuarioExistente = criarUsuarioValido();

        Usuario dadosAtualizados = new Usuario();
        dadosAtualizados.setUsername(usuarioExistente.getUsername());
        dadosAtualizados.setNome("Nome Alterado");

        when(usuarioRepo.findById(id)).thenReturn(Optional.of(usuarioExistente));

        when(usuarioRepo.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.atualizarUsuario(id, dadosAtualizados);

        assertEquals("Nome Alterado", resultado.getNome());

        verify(usuarioRepo, never()).findByUsername(anyString());
        verify(usuarioRepo).save(usuarioExistente);
    }

    @Test
    void deveLancarExcecaoQuandoUsernameAtualizadoForNulo() {

        UUID id = UUID.randomUUID();
        Usuario usuarioExistente = criarUsuarioValido();

        Usuario dadosAtualizados = new Usuario();
        dadosAtualizados.setUsername(null);

        when(usuarioRepo.findById(id)).thenReturn(Optional.of(usuarioExistente));

        assertThrows(UsuarioValidationException.class,
                () -> usuarioService.atualizarUsuario(id, dadosAtualizados));

        verify(usuarioRepo, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoUsernameAtualizadoEstiverEmBranco() {

        UUID id = UUID.randomUUID();
        Usuario usuarioExistente = criarUsuarioValido();

        Usuario dadosAtualizados = new Usuario();
        dadosAtualizados.setUsername(" ");

        when(usuarioRepo.findById(id)).thenReturn(Optional.of(usuarioExistente));

        assertThrows(UsuarioValidationException.class,
                () -> usuarioService.atualizarUsuario(id, dadosAtualizados));

        verify(usuarioRepo, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoNovoUsernameJaEstiverEmUso() {

        UUID id = UUID.randomUUID();
        Usuario usuarioExistente = criarUsuarioValido();

        Usuario outroUsuario = criarUsuarioValido();
        outroUsuario.setUsername("username.ocupado");

        Usuario dadosAtualizados = new Usuario();
        dadosAtualizados.setUsername("username.ocupado");
        dadosAtualizados.setNome("Nome Novo");

        when(usuarioRepo.findById(id)).thenReturn(Optional.of(usuarioExistente));

        when(usuarioRepo.findByUsername("username.ocupado"))
                .thenReturn(Optional.of(outroUsuario));

        assertThrows(
                UsernameAlreadyExistsException.class,
                () -> usuarioService.atualizarUsuario(id, dadosAtualizados));

        verify(usuarioRepo, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoAtualizarUsuarioInexistente() {

        UUID id = UUID.randomUUID();

        when(usuarioRepo.findById(id)).thenReturn(Optional.empty());

        Usuario dadosAtualizados = new Usuario();
        dadosAtualizados.setUsername("novo.username");
        dadosAtualizados.setNome("Novo Nome");

        assertThrows(UsuarioNotFoundException.class,
                () -> usuarioService.atualizarUsuario(id, dadosAtualizados));

        verify(usuarioRepo, never()).save(any());
    }

    @Test
    void deveAutenticarUsuarioComCredenciaisValidas() {

        Usuario usuario = criarUsuarioValido();

        when(usuarioRepo.findByUsername(usuario.getUsername())).thenReturn(Optional.of(usuario));

        Usuario resultado = usuarioService.autenticar(usuario.getUsername(), usuario.getPasswordHash());

        assertSame(usuario, resultado);

        verify(usuarioRepo).findByUsername(usuario.getUsername());
        verify(usuarioRepo, never()).save(any(Usuario.class));
    }

    @Test
    void deveLancarExcecaoQuandoSenhaEstiverIncorreta() {

        Usuario usuario = criarUsuarioValido();

        when(usuarioRepo.findByUsername(usuario.getUsername())).thenReturn(Optional.of(usuario));

        assertThrows(UsuarioValidationException.class, () -> usuarioService.autenticar(
                        usuario.getUsername(), "senha-incorreta"));
    }

    @Test
    void deveLancarExcecaoAoAutenticarUsuarioInexistente() {

        when(usuarioRepo.findByUsername("inexistente")).thenReturn(Optional.empty());

        assertThrows(UsuarioValidationException.class, () -> usuarioService.autenticar(
                        "inexistente",
                        "qualquer-senha"));

        verify(usuarioRepo).findByUsername("inexistente");
        verify(usuarioRepo, never()).save(any(Usuario.class));
    }
}
