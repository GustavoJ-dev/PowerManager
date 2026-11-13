package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.unidade_consumidora.UnidadeConsumidoraAtualizacaoDTO;
import com.powermanager.powermanager.dto.unidade_consumidora.UnidadeConsumidoraCadastroDTO;
import com.powermanager.powermanager.dto.unidade_consumidora.UnidadeConsumidoraResponseDTO;
import com.powermanager.powermanager.entity.Cliente;
import com.powermanager.powermanager.entity.UnidadeConsumidora;
import com.powermanager.powermanager.enums.ClasseConsumo;
import com.powermanager.powermanager.enums.ModalidadeTarifaria;
import com.powermanager.powermanager.enums.SubgrupoTarifario;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UnidadeConsumidoraMapperTest {

    private final UnidadeConsumidoraMapper mapper =
            new UnidadeConsumidoraMapperImpl();

    @Test
    void deveConverterCadastroDTOParaEntidade() {

        UUID clienteId = UUID.randomUUID();

        UnidadeConsumidoraCadastroDTO dto = new UnidadeConsumidoraCadastroDTO(
                "UC-001",
                "Rua Principal, 100",
                "Paramirim",
                "BA",
                "46190-000",
                "Distribuidora Teste",
                ClasseConsumo.RESIDENCIAL,
                SubgrupoTarifario.B1,
                ModalidadeTarifaria.CONVENCIONAL,
                clienteId);

        UnidadeConsumidora unidade = mapper.paraEntidade(dto);

        assertNotNull(unidade);

        assertNull(unidade.getId());
        assertNull(unidade.getCliente());

        assertTrue(unidade.getAtiva());

        assertEquals("UC-001", unidade.getCodigo());
        assertEquals("Rua Principal, 100", unidade.getEndereco());
        assertEquals("Paramirim", unidade.getCidade());
        assertEquals("BA", unidade.getEstado());
        assertEquals("46190-000", unidade.getCep());
        assertEquals("Distribuidora Teste", unidade.getDistribuidora());

        assertEquals(ClasseConsumo.RESIDENCIAL, unidade.getClasseConsumo());
        assertEquals(SubgrupoTarifario.B1, unidade.getSubgrupoTarifario());
        assertEquals(ModalidadeTarifaria.CONVENCIONAL, unidade.getModalidadeTarifaria());
    }

    @Test
    void deveConverterEntidadeParaResponseDTO() {

        UUID unidadeId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();

        Cliente cliente = new Cliente();
        cliente.setId(clienteId);

        UnidadeConsumidora unidade = new UnidadeConsumidora();
        unidade.setId(unidadeId);
        unidade.setCodigo("UC-001");
        unidade.setEndereco("Rua Principal, 100");
        unidade.setCidade("Paramirim");
        unidade.setEstado("BA");
        unidade.setCep("46190-000");
        unidade.setDistribuidora("Distribuidora Teste");
        unidade.setClasseConsumo(ClasseConsumo.RESIDENCIAL);
        unidade.setSubgrupoTarifario(SubgrupoTarifario.B1);
        unidade.setModalidadeTarifaria(ModalidadeTarifaria.CONVENCIONAL);
        unidade.setAtiva(true);
        unidade.setCliente(cliente);

        UnidadeConsumidoraResponseDTO dto = mapper.paraResponseDTO(unidade);

        assertNotNull(dto);

        assertEquals(unidadeId, dto.id());
        assertEquals(clienteId, dto.clienteId());

        assertEquals("UC-001", dto.codigo());
        assertEquals("Rua Principal, 100", dto.endereco());
        assertEquals("Paramirim", dto.cidade());
        assertEquals("BA", dto.estado());
        assertEquals("46190-000", dto.cep());
        assertEquals("Distribuidora Teste", dto.distribuidora());

        assertEquals(ClasseConsumo.RESIDENCIAL, dto.classeConsumo());
        assertEquals(SubgrupoTarifario.B1, dto.subgrupoTarifario());
        assertEquals(ModalidadeTarifaria.CONVENCIONAL, dto.modalidadeTarifaria());

        assertTrue(dto.ativa());
    }

    @Test
    void deveAtualizarDadosSemAlterarIdClienteEStatus() {

        UUID unidadeId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();

        Cliente cliente = new Cliente();
        cliente.setId(clienteId);

        UnidadeConsumidora unidade = new UnidadeConsumidora();
        unidade.setId(unidadeId);
        unidade.setCliente(cliente);
        unidade.setAtiva(false);

        UnidadeConsumidoraAtualizacaoDTO dto = new UnidadeConsumidoraAtualizacaoDTO(
                "UC-002",
                "Rua Atualizada, 200",
                "Macaúbas",
                "BA",
                "46500-000",
                "Nova Distribuidora",
                ClasseConsumo.COMERCIAL_SERVICOS,
                SubgrupoTarifario.B3,
                ModalidadeTarifaria.BRANCA);

        mapper.atualizarEntidade(dto, unidade);

        assertEquals(unidadeId, unidade.getId());
        assertEquals(cliente, unidade.getCliente());
        assertFalse(unidade.getAtiva());

        assertEquals("UC-002", unidade.getCodigo());
        assertEquals("Rua Atualizada, 200", unidade.getEndereco());
        assertEquals("Macaúbas", unidade.getCidade());
        assertEquals("BA", unidade.getEstado());
        assertEquals("46500-000", unidade.getCep());
        assertEquals("Nova Distribuidora", unidade.getDistribuidora());

        assertEquals(ClasseConsumo.COMERCIAL_SERVICOS, unidade.getClasseConsumo());
        assertEquals(SubgrupoTarifario.B3, unidade.getSubgrupoTarifario());
        assertEquals(ModalidadeTarifaria.BRANCA, unidade.getModalidadeTarifaria());
    }

    @Test
    void deveRetornarNullQuandoOrigemForNull() {

        assertNull(mapper.paraEntidade(null));
        assertNull(mapper.paraResponseDTO(null));
    }
}
