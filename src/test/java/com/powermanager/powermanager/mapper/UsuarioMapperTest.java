package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.usuario.UsuarioAtualizacaoDTO;
import com.powermanager.powermanager.dto.usuario.UsuarioCadastroDTO;
import com.powermanager.powermanager.dto.usuario.UsuarioResponseDTO;
import com.powermanager.powermanager.entity.Cliente;
import com.powermanager.powermanager.entity.Usuario;
import com.powermanager.powermanager.enums.TipoUsuario;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioMapperTest {

    private final UsuarioMapper mapper = new UsuarioMapperImpl();

    @Test
    void deveConverterCadastroDTOParaEntidadeSemCopiarSenhaOuCliente() {
        UUID clienteId = UUID.randomUUID();

        UsuarioCadastroDTO dto = new UsuarioCadastroDTO(
                "usuario_teste",
                "Usuário Teste",
                "senha-temporaria",
                TipoUsuario.CLIENTE,
                clienteId);

        Usuario usuario = mapper.paraEntidade(dto);

        assertNotNull(usuario);

        assertEquals("usuario_teste", usuario.getUsername());
        assertEquals("Usuário Teste", usuario.getNome());
        assertEquals(TipoUsuario.CLIENTE, usuario.getTipoUsuario());

        // Campos tratados pelo service.
        assertNull(usuario.getId());
        assertNull(usuario.getPasswordHash());
        assertNull(usuario.getCliente());
    }

    @Test
    void deveConverterEntidadeParaResponseDTOSemExporSenha() {
        UUID id = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();

        Cliente cliente = new Cliente();
        cliente.setId(clienteId);

        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setUsername("usuario_teste");
        usuario.setNome("Usuário Teste");
        usuario.setPasswordHash("hash-da-senha");
        usuario.setTipoUsuario(TipoUsuario.CLIENTE);
        usuario.setCliente(cliente);

        UsuarioResponseDTO dto = mapper.paraResponseDTO(usuario);

        assertNotNull(dto);

        assertEquals(id, dto.id());
        assertEquals("usuario_teste", dto.username());
        assertEquals("Usuário Teste", dto.nome());
        assertEquals(TipoUsuario.CLIENTE, dto.tipoUsuario());
        assertEquals(clienteId, dto.clienteId());
    }

    @Test
    void deveAtualizarUsuarioPreservandoCamposProtegidos() {

        UUID id = UUID.randomUUID();
        String senha = "hash-existente";

        Cliente cliente = new Cliente();
        cliente.setId(UUID.randomUUID());

        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setUsername("antigo");
        usuario.setNome("Nome Antigo");
        usuario.setPasswordHash(senha);
        usuario.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
        usuario.setCliente(cliente);

        UsuarioAtualizacaoDTO dto = new UsuarioAtualizacaoDTO("usuario_novo", "Nome Novo");

        mapper.atualizarEntidade(dto, usuario);

        // Campos protegidos.
        assertEquals(id, usuario.getId());
        assertEquals(senha, usuario.getPasswordHash());
        assertEquals(TipoUsuario.ADMINISTRADOR, usuario.getTipoUsuario());
        assertSame(cliente, usuario.getCliente());

        // Campos atualizados.
        assertEquals("usuario_novo", usuario.getUsername());
        assertEquals("Nome Novo", usuario.getNome());
    }
}