package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.bandeira_tarifaria.BandeiraTarifariaAtualizacaoDTO;
import com.powermanager.powermanager.dto.bandeira_tarifaria.BandeiraTarifariaCadastroDTO;
import com.powermanager.powermanager.dto.bandeira_tarifaria.BandeiraTarifariaResponseDTO;
import com.powermanager.powermanager.entity.BandeiraTarifaria;
import com.powermanager.powermanager.enums.TipoBandeira;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BandeiraTarifariaMapperTest {

    private final BandeiraTarifariaMapper mapper = new BandeiraTarifariaMapperImpl();

    @Test
    void deveConverterCadastroDTOParaEntidade() {

        BandeiraTarifariaCadastroDTO dto =
                new BandeiraTarifariaCadastroDTO(
                        TipoBandeira.VERDE,
                        new BigDecimal("0.000000"),
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 12, 31));

        BandeiraTarifaria bandeira = mapper.paraEntidade(dto);

        assertNotNull(bandeira);

        assertNull(bandeira.getId());

        assertEquals(TipoBandeira.VERDE, bandeira.getTipoBandeira());

        assertEquals(
                new BigDecimal("0.000000"),
                bandeira.getValorAdicional());

        assertEquals(
                LocalDate.of(2026, 1, 1),
                bandeira.getInicioVigencia());

        assertEquals(
                LocalDate.of(2026, 12, 31),
                bandeira.getFimVigencia());
    }

    @Test
    void deveConverterEntidadeParaResponseDTO() {

        UUID id = UUID.randomUUID();

        BandeiraTarifaria bandeira = new BandeiraTarifaria();

        bandeira.setId(id);
        bandeira.setTipoBandeira(TipoBandeira.AMARELA);
        bandeira.setValorAdicional(new BigDecimal("0.018850"));
        bandeira.setInicioVigencia(LocalDate.of(2026, 1, 1));
        bandeira.setFimVigencia(LocalDate.of(2026, 12, 31));

        BandeiraTarifariaResponseDTO dto =
                mapper.paraResponseDTO(bandeira);

        assertNotNull(dto);

        assertEquals(id, dto.id());
        assertEquals(TipoBandeira.AMARELA, dto.tipoBandeira());

        assertEquals(
                new BigDecimal("0.018850"),
                dto.valorAdicional());

        assertEquals(
                LocalDate.of(2026, 1, 1),
                dto.inicioVigencia());

        assertEquals(
                LocalDate.of(2026, 12, 31),
                dto.fimVigencia());
    }

    @Test
    void deveAtualizarSomenteFimVigencia() {

        UUID id = UUID.randomUUID();

        BandeiraTarifaria bandeira = new BandeiraTarifaria();

        bandeira.setId(id);
        bandeira.setTipoBandeira(TipoBandeira.AMARELA);
        bandeira.setValorAdicional(new BigDecimal("0.018850"));
        bandeira.setInicioVigencia(LocalDate.of(2026, 1, 1));
        bandeira.setFimVigencia(null);

        BandeiraTarifariaAtualizacaoDTO dto = new BandeiraTarifariaAtualizacaoDTO(
                        LocalDate.of(2026, 6, 30));

        mapper.atualizarEntidade(dto, bandeira);

        assertEquals(id, bandeira.getId());

        assertEquals(
                TipoBandeira.AMARELA,
                bandeira.getTipoBandeira());

        assertEquals(
                new BigDecimal("0.018850"),
                bandeira.getValorAdicional());

        assertEquals(
                LocalDate.of(2026, 1, 1),
                bandeira.getInicioVigencia());

        assertEquals(
                LocalDate.of(2026, 6, 30),
                bandeira.getFimVigencia());
    }

    @Test
    void devePreservarFimVigenciaNuloNoCadastro() {

        BandeiraTarifariaCadastroDTO dto =
                new BandeiraTarifariaCadastroDTO(
                        TipoBandeira.VERDE,
                        BigDecimal.ZERO,
                        LocalDate.of(2026, 1, 1),
                        null
                );

        BandeiraTarifaria bandeira = mapper.paraEntidade(dto);

        assertNotNull(bandeira);
        assertNull(bandeira.getFimVigencia());
    }

    @Test
    void deveRetornarNullQuandoOrigemForNull() {

        assertNull(mapper.paraEntidade(null));
        assertNull(mapper.paraResponseDTO(null));
    }
}
