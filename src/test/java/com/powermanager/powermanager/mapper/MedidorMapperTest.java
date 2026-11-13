package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.medidor.MedidorAtualizacaoDTO;
import com.powermanager.powermanager.dto.medidor.MedidorCadastroDTO;
import com.powermanager.powermanager.dto.medidor.MedidorResponseDTO;
import com.powermanager.powermanager.entity.Medidor;
import com.powermanager.powermanager.entity.UnidadeConsumidora;
import com.powermanager.powermanager.enums.TipoLigacao;
import com.powermanager.powermanager.enums.TipoMedidor;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MedidorMapperTest {

    private final MedidorMapper mapper = new MedidorMapperImpl();

    @Test
    void deveConverterCadastroDTOParaEntidade() {

        MedidorCadastroDTO dto = new MedidorCadastroDTO(
                "MED-001",
                TipoMedidor.ELETRONICO,
                TipoLigacao.MONOFASICO,
                LocalDate.of(2026, 1, 10),
                UUID.randomUUID());

        Medidor medidor = mapper.paraEntidade(dto);

        assertNotNull(medidor);

        assertNull(medidor.getId());
        assertNull(medidor.getUnidadeConsumidora());
        assertNull(medidor.getDataInstalacao());
        assertNull(medidor.getDataRetirada());

        assertTrue(medidor.getAtivo());

        assertEquals("MED-001", medidor.getNumero());
        assertEquals(TipoMedidor.ELETRONICO, medidor.getTipoMedidor());
        assertEquals(TipoLigacao.MONOFASICO, medidor.getTipoLigacao());
    }

    @Test
    void deveConverterEntidadeParaResponseDTO() {

        UUID medidorId = UUID.randomUUID();
        UUID unidadeId = UUID.randomUUID();

        UnidadeConsumidora unidade = new UnidadeConsumidora();
        unidade.setId(unidadeId);

        Medidor medidor = new Medidor();
        medidor.setId(medidorId);
        medidor.setNumero("MED-001");
        medidor.setTipoMedidor(TipoMedidor.ELETRONICO);
        medidor.setTipoLigacao(TipoLigacao.MONOFASICO);
        medidor.setAtivo(true);
        medidor.setDataInstalacao(LocalDate.of(2026, 1, 10));
        medidor.setUnidadeConsumidora(unidade);

        MedidorResponseDTO dto = mapper.paraResponseDTO(medidor);

        assertNotNull(dto);

        assertEquals(medidorId, dto.id());
        assertEquals(unidadeId, dto.unidadeConsumidoraId());

        assertEquals("MED-001", dto.numero());
        assertEquals(TipoMedidor.ELETRONICO, dto.tipoMedidor());
        assertEquals(TipoLigacao.MONOFASICO, dto.tipoLigacao());
        assertTrue(dto.ativo());
        assertEquals(LocalDate.of(2026, 1, 10), dto.dataInstalacao());
        assertNull(dto.dataRetirada());
    }

    @Test
    void deveAtualizarSomenteTipoMedidorETipoLigacao() {

        UUID medidorId = UUID.randomUUID();
        UUID unidadeId = UUID.randomUUID();

        UnidadeConsumidora unidade = new UnidadeConsumidora();
        unidade.setId(unidadeId);

        Medidor medidor = new Medidor();
        medidor.setId(medidorId);
        medidor.setNumero("MED-001");
        medidor.setTipoMedidor(TipoMedidor.ELETROMECANICO);
        medidor.setTipoLigacao(TipoLigacao.BIFASICO);
        medidor.setAtivo(false);
        medidor.setDataInstalacao(LocalDate.of(2025, 1, 10));
        medidor.setDataRetirada(LocalDate.of(2025, 12, 10));
        medidor.setUnidadeConsumidora(unidade);

        MedidorAtualizacaoDTO dto = new MedidorAtualizacaoDTO(
                TipoMedidor.INTELIGENTE,
                TipoLigacao.TRIFASICO);

        mapper.atualizarEntidade(dto, medidor);

        assertEquals(medidorId, medidor.getId());
        assertEquals("MED-001", medidor.getNumero());

        assertSame(unidade, medidor.getUnidadeConsumidora());

        assertFalse(medidor.getAtivo());

        assertEquals(
                LocalDate.of(2025, 1, 10),
                medidor.getDataInstalacao());

        assertEquals(
                LocalDate.of(2025, 12, 10),
                medidor.getDataRetirada());

        assertEquals(TipoMedidor.INTELIGENTE, medidor.getTipoMedidor());
        assertEquals(TipoLigacao.TRIFASICO, medidor.getTipoLigacao());
    }

    @Test
    void deveRetornarNullQuandoOrigemForNull() {

        assertNull(mapper.paraEntidade(null));
        assertNull(mapper.paraResponseDTO(null));
    }
}
