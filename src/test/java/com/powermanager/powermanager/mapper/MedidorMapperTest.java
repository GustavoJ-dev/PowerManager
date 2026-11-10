package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.medidor.MedidorAtualizacaoDTO;
import com.powermanager.powermanager.dto.medidor.MedidorCadastroDTO;
import com.powermanager.powermanager.dto.medidor.MedidorResponseDTO;
import com.powermanager.powermanager.entity.Cliente;
import com.powermanager.powermanager.entity.Medidor;
import com.powermanager.powermanager.enums.ClasseConsumo;
import com.powermanager.powermanager.enums.TipoLigacao;
import com.powermanager.powermanager.enums.TipoMedidor;
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
                TipoMedidor.ELETRONICO,
                TipoLigacao.MONOFASICO,
                ClasseConsumo.RESIDENCIAL,
                30,
                clienteId);

        Medidor medidor = mapper.paraEntidade(dto);

        assertNotNull(medidor);
        assertEquals("MED-001", medidor.getNumero());
        assertEquals("Sala", medidor.getLocalizacao());
        assertEquals(TipoMedidor.ELETRONICO, medidor.getTipoMedidor());
        assertEquals(TipoLigacao.MONOFASICO, medidor.getTipoLigacao());
        assertEquals(ClasseConsumo.RESIDENCIAL, medidor.getClasseConsumo());
        assertEquals(30, medidor.getDias());

        // Campos que devem ser tratados pelo service
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
        medidor.setTipoMedidor(TipoMedidor.ELETRONICO);
        medidor.setTipoLigacao(TipoLigacao.MONOFASICO);
        medidor.setClasseConsumo(ClasseConsumo.RESIDENCIAL);
        medidor.setDias(30);
        medidor.setCliente(cliente);

        MedidorResponseDTO dto = mapper.paraResponseDTO(medidor);

        assertNotNull(dto);
        assertEquals(id, dto.id());
        assertEquals("MED-001", dto.numero());
        assertEquals("Sala", dto.localizacao());
        assertEquals(TipoMedidor.ELETRONICO, dto.tipoMedidor());
        assertEquals(TipoLigacao.MONOFASICO, dto.tipoLigacao());
        assertEquals(ClasseConsumo.RESIDENCIAL, dto.classeConsumo());
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
        medidor.setTipoMedidor(TipoMedidor.ELETROMECANICO);
        medidor.setTipoLigacao(TipoLigacao.BIFASICO);
        medidor.setClasseConsumo(ClasseConsumo.COMERCIAL_SERVICOS);
        medidor.setDias(15);
        medidor.setCliente(cliente);

        MedidorAtualizacaoDTO dto = new MedidorAtualizacaoDTO(
                "MED-002",
                "Cozinha",
                TipoMedidor.INTELIGENTE,
                TipoLigacao.TRIFASICO,
                ClasseConsumo.RESIDENCIAL,
                30);

        mapper.atualizarEntidade(dto, medidor);

        // Identificador e relacionamento devem ser preservados
        assertEquals(id, medidor.getId());
        assertSame(cliente, medidor.getCliente());

        // Campos atualizados
        assertEquals("MED-002", medidor.getNumero());
        assertEquals("Cozinha", medidor.getLocalizacao());
        assertEquals(TipoMedidor.INTELIGENTE, medidor.getTipoMedidor());
        assertEquals(TipoLigacao.TRIFASICO, medidor.getTipoLigacao());
        assertEquals(ClasseConsumo.RESIDENCIAL, medidor.getClasseConsumo());
        assertEquals(30, medidor.getDias());
    }
}