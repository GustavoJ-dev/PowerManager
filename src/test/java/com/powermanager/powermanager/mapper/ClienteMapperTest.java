package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.cliente.ClienteCadastroDTO;
import com.powermanager.powermanager.dto.cliente.ClienteResponseDTO;
import com.powermanager.powermanager.dto.medidor.MedidorResponseDTO;
import com.powermanager.powermanager.entity.Cliente;
import com.powermanager.powermanager.entity.Medidor;
import com.powermanager.powermanager.enums.ClasseConsumo;
import com.powermanager.powermanager.enums.TipoLigacao;
import com.powermanager.powermanager.enums.TipoMedidor;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ClienteMapperTest {

    private final MedidorMapper medidorMapper = new MedidorMapperImpl();

    private final ClienteMapper mapper = new ClienteMapperImpl(medidorMapper);

    @Test
    void deveConverterCadastroDTOParaEntidade() {
        ClienteCadastroDTO dto = new ClienteCadastroDTO(
                "Cliente Teste",
                "Rua das Flores, 123",
                "Paramirim",
                "BA",
                "cliente@email.com",
                "(77) 99999-9999");

        Cliente cliente = mapper.paraEntidade(dto);

        assertNotNull(cliente);
        assertNull(cliente.getId());

        assertEquals("Cliente Teste", cliente.getNome());
        assertEquals("Rua das Flores, 123", cliente.getEndereco());
        assertEquals("Paramirim", cliente.getCidade());
        assertEquals("BA", cliente.getEstado());
        assertEquals("cliente@email.com", cliente.getEmail());
        assertEquals("(77) 99999-9999", cliente.getTelefone());

        assertNotNull(cliente.getMedidores());
        assertTrue(cliente.getMedidores().isEmpty());
    }

    @Test
    void deveConverterEntidadeParaResponseDTO() {
        UUID clienteId = UUID.randomUUID();

        Cliente cliente = new Cliente();
        cliente.setId(clienteId);
        cliente.setNome("Cliente Teste");
        cliente.setEndereco("Rua das Flores, 123");
        cliente.setCidade("Paramirim");
        cliente.setEstado("BA");
        cliente.setEmail("cliente@email.com");
        cliente.setTelefone("(77) 99999-9999");

        ClienteResponseDTO dto = mapper.paraResponseDTO(cliente);

        assertNotNull(dto);
        assertEquals(clienteId, dto.id());

        assertEquals("Cliente Teste", dto.nome());
        assertEquals("Rua das Flores, 123", dto.endereco());
        assertEquals("Paramirim", dto.cidade());
        assertEquals("BA", dto.estado());
        assertEquals("cliente@email.com", dto.email());
        assertEquals("(77) 99999-9999", dto.telefone());

        assertNotNull(dto.medidores());
        assertTrue(dto.medidores().isEmpty());
    }

    @Test
    void deveConverterClienteComSeusMedidoresParaResponseDTO() {
        UUID clienteId = UUID.randomUUID();
        UUID medidorId = UUID.randomUUID();

        Cliente cliente = new Cliente();
        cliente.setId(clienteId);
        cliente.setNome("Cliente Teste");

        Medidor medidor = new Medidor();
        medidor.setId(medidorId);
        medidor.setNumero("MED-001");
        medidor.setLocalizacao("Residência");
        medidor.setTipoMedidor(TipoMedidor.ELETRONICO);
        medidor.setTipoLigacao(TipoLigacao.MONOFASICO);
        medidor.setClasseConsumo(ClasseConsumo.RESIDENCIAL);
        medidor.setDias(30);
        medidor.setCliente(cliente);

        cliente.getMedidores().add(medidor);

        ClienteResponseDTO dto = mapper.paraResponseDTO(cliente);

        assertNotNull(dto);
        assertEquals(1, dto.medidores().size());

        MedidorResponseDTO medidorDTO = dto.medidores().get(0);

        assertEquals(medidorId, medidorDTO.id());
        assertEquals("MED-001", medidorDTO.numero());
        assertEquals("Residência", medidorDTO.localizacao());
        assertEquals(TipoMedidor.ELETRONICO, medidorDTO.tipoMedidor());
        assertEquals(TipoLigacao.MONOFASICO, medidorDTO.tipoLigacao());
        assertEquals(ClasseConsumo.RESIDENCIAL, medidorDTO.classeConsumo());
        assertEquals(30, medidorDTO.dias());

        assertEquals(clienteId, medidorDTO.clienteId());
    }

    @Test
    void deveRetornarNullQuandoOrigemForNull() {
        assertNull(mapper.paraEntidade(null));
        assertNull(mapper.paraResponseDTO(null));
    }
}