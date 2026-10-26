package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.cliente.ClienteCadastroDTO;
import com.powermanager.powermanager.dto.cliente.ClienteResponseDTO;
import com.powermanager.powermanager.entity.Cliente;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

public class ClienteMapperTest {

    private final ClienteMapper mapper = Mappers.getMapper(ClienteMapper.class);

    @Test
    void deveConverterCadastroDTOParaEntidade() {
        ClienteCadastroDTO dto = new ClienteCadastroDTO(
                "Cliente Teste",
                "Rua das Flores, 123",
                "Paramirim",
                "BA",
                "cliente@email.com",
                "(77) 99999-9999"
        );

        Cliente cliente = mapper.paraEntidade(dto);

        assertNotNull(cliente);
        assertEquals("Cliente Teste", cliente.getNome());
        assertEquals("Rua das Flores, 123", cliente.getEndereco());
        assertEquals("Paramirim", cliente.getCidade());
        assertEquals("BA", cliente.getEstado());
        assertEquals("cliente@email.com", cliente.getEmail());
        assertEquals("(77) 99999-9999", cliente.getTelefone());
    }

    @Test
    void deveConverterEntidadeParaResponseDTO() {
        Cliente cliente = new Cliente();
        cliente.setNome("Cliente Teste");
        cliente.setEndereco("Rua das Flores, 123");
        cliente.setCidade("Paramirim");
        cliente.setEstado("BA");
        cliente.setEmail("cliente@email.com");
        cliente.setTelefone("(77) 99999-9999");

        ClienteResponseDTO dto = mapper.paraResponseDTO(cliente);

        assertNotNull(dto);
        assertEquals("Cliente Teste", dto.nome());
        assertEquals("Rua das Flores, 123", dto.endereco());
        assertEquals("Paramirim", dto.cidade());
        assertEquals("BA", dto.estado());
        assertEquals("cliente@email.com", dto.email());
        assertEquals("(77) 99999-9999", dto.telefone());
    }
}
