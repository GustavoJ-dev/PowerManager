package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.medidor.MedidorAtualizacaoDTO;
import com.powermanager.powermanager.dto.medidor.MedidorCadastroDTO;
import com.powermanager.powermanager.dto.medidor.MedidorResponseDTO;
import com.powermanager.powermanager.entity.Cliente;
import com.powermanager.powermanager.entity.Medidor;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MedidorMapperTest {

    private final MedidorMapper mapper = new MedidorMapperImpl();

    @Test
    void deveConverterCadastroDTOParaEntidade() {
        UUID clienteId = UUID.randomUUID();

        MedidorCadastroDTO dto = new MedidorCadastroDTO(
                "MED-001",
                "Sala",
                "Digital",
                "A",
                "Mensal",
                30,
                clienteId);

        Medidor medidor = mapper.paraEntidade(dto);

        assertNotNull(medidor);
        assertEquals("MED-001", medidor.getNumero());
        assertEquals("Sala", medidor.getLocalizacao());
        assertEquals("Digital", medidor.getTipo());
        assertEquals("A", medidor.getCodigoFase());
        assertEquals("Mensal", medidor.getTipoFatura());
        assertEquals(30, medidor.getDias());
        assertNull(medidor.getId());
        assertNull(medidor.getCliente());
    }

    @Test
    void deveConverterEntidadeParaResponseDTO() {
        UUID id = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();

        Cliente cliente = new Cliente();
        cliente.setId(clienteId);

        Medidor medidor = new Medidor();
        medidor.setId(id);
        medidor.setNumero("MED-001");
        medidor.setLocalizacao("Sala");
        medidor.setTipo("Digital");
        medidor.setCodigoFase("A");
        medidor.setTipoFatura("Mensal");
        medidor.setDias(30);
        medidor.setCliente(cliente);

        MedidorResponseDTO dto = mapper.paraResponseDTO(medidor);

        assertNotNull(dto);
        assertEquals(id, dto.id());
        assertEquals("MED-001", dto.numero());
        assertEquals("Sala", dto.localizacao());
        assertEquals("Digital", dto.tipo());
        assertEquals("A", dto.codigoFase());
        assertEquals("Mensal", dto.tipoFatura());
        assertEquals(30, dto.dias());
        assertEquals(clienteId, dto.clienteId());
    }

    @Test
    void deveAtualizarCamposSemAlterarIdOuCliente() {
        UUID id = UUID.randomUUID();

        Cliente cliente = new Cliente();
        cliente.setId(UUID.randomUUID());

        Medidor medidor = new Medidor();
        medidor.setId(id);
        medidor.setNumero("MED-ANTIGO");
        medidor.setLocalizacao("Antiga");
        medidor.setTipo("Analógico");
        medidor.setCodigoFase("B");
        medidor.setTipoFatura("Anual");
        medidor.setDias(15);
        medidor.setCliente(cliente);

        MedidorAtualizacaoDTO dto = new MedidorAtualizacaoDTO(
                "MED-002",
                "Cozinha",
                "Digital",
                "C",
                "Mensal",
                30);

        mapper.atualizarEntidade(dto, medidor);

        assertEquals(id, medidor.getId());
        assertSame(cliente, medidor.getCliente());
        assertEquals("MED-002", medidor.getNumero());
        assertEquals("Cozinha", medidor.getLocalizacao());
        assertEquals("Digital", medidor.getTipo());
        assertEquals("C", medidor.getCodigoFase());
        assertEquals("Mensal", medidor.getTipoFatura());
        assertEquals(30, medidor.getDias());
    }
}
