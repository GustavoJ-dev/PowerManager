package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.cliente.ClienteAtualizacaoDTO;
import com.powermanager.powermanager.dto.cliente.ClienteCadastroDTO;
import com.powermanager.powermanager.dto.cliente.ClienteResponseDTO;
import com.powermanager.powermanager.entity.Cliente;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ClienteMapperTest {

    private ClienteMapper mapper = new ClienteMapperImpl();

    @Test
    void deveConverterCadastroDTOParaEntidade() {

        ClienteCadastroDTO dto = new ClienteCadastroDTO(
                "Cliente Teste",
                "cliente@email.com",
                "(77) 99999-9999");

        Cliente cliente = mapper.paraEntidade(dto);

        assertNotNull(cliente);
        assertNull(cliente.getId());

        assertEquals("Cliente Teste", cliente.getNome());
        assertEquals("cliente@email.com", cliente.getEmail());
        assertEquals("(77) 99999-9999", cliente.getTelefone());

        assertNotNull(cliente.getUnidadeConsumidoras());
        assertTrue(cliente.getUnidadeConsumidoras().isEmpty());
    }

    @Test
    void deveConverterEntidadeParaResponseDTO() {

        UUID clienteId = UUID.randomUUID();

        Cliente cliente = new Cliente();
        cliente.setId(clienteId);
        cliente.setNome("Cliente Teste");
        cliente.setEmail("cliente@email.com");
        cliente.setTelefone("(77) 99999-9999");

        ClienteResponseDTO dto = mapper.paraResponseDTO(cliente);

        assertNotNull(dto);

        assertEquals(clienteId, dto.id());
        assertEquals("Cliente Teste", dto.nome());
        assertEquals("cliente@email.com", dto.email());
        assertEquals("(77) 99999-9999", dto.telefone());
    }

    @Test
    void deveAtualizarDadosDoClienteSemAlterarSeuId() {

        UUID clienteId = UUID.randomUUID();

        Cliente cliente = new Cliente();
        cliente.setId(clienteId);
        cliente.setNome("Nome Antigo");
        cliente.setEmail("antigo@email.com");
        cliente.setTelefone("(77) 90000-0000");

        ClienteAtualizacaoDTO dto = new ClienteAtualizacaoDTO(
                "Nome Atualizado",
                "novo@email.com",
                "(77) 98888-8888");

        mapper.atualizarEntidade(dto, cliente);

        assertEquals(clienteId, cliente.getId());
        assertEquals("Nome Atualizado", cliente.getNome());
        assertEquals("novo@email.com", cliente.getEmail());
        assertEquals("(77) 98888-8888", cliente.getTelefone());
    }

    @Test
    void deveRetornarNullQuandoOrigemForNull() {

        assertNull(mapper.paraEntidade(null));
        assertNull(mapper.paraResponseDTO(null));
    }
}
