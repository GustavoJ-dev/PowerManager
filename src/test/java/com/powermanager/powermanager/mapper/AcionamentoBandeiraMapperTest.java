package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.acionamento_bandeira.AcionamentoBandeiraAtualizacaoDTO;
import com.powermanager.powermanager.dto.acionamento_bandeira.AcionamentoBandeiraCadastroDTO;
import com.powermanager.powermanager.dto.acionamento_bandeira.AcionamentoBandeiraResponseDTO;
import com.powermanager.powermanager.entity.AcionamentoBandeira;
import com.powermanager.powermanager.enums.TipoBandeira;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AcionamentoBandeiraMapperTest {

    private final AcionamentoBandeiraMapper mapper = new AcionamentoBandeiraMapperImpl();

    @Test
    void deveConverterCadastroDTOParaEntidade() {

        AcionamentoBandeiraCadastroDTO dto =
                new AcionamentoBandeiraCadastroDTO(
                        TipoBandeira.AMARELA,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 12, 31),
                        LocalDate.of(2025, 12, 20),
                        "https://fonte-oficial.gov.br");

        AcionamentoBandeira acionamento = mapper.paraEntidade(dto);

        assertNotNull(acionamento);

        assertNull(acionamento.getId());

        assertEquals(TipoBandeira.AMARELA,
                acionamento.getTipoBandeira());

        assertEquals(LocalDate.of(2026, 1, 1),
                acionamento.getInicioVigencia());

        assertEquals(LocalDate.of(2026, 12, 31),
                acionamento.getFimVigencia());

        assertEquals(LocalDate.of(2025, 12, 20),
                acionamento.getDataDivulgacao());

        assertEquals("https://fonte-oficial.gov.br",
                acionamento.getReferenciaOficial());
    }

    @Test
    void deveConverterEntidadeParaResponseDTO() {

        UUID id = UUID.randomUUID();

        AcionamentoBandeira acionamento = new AcionamentoBandeira();

        acionamento.setId(id);
        acionamento.setTipoBandeira(TipoBandeira.VERMELHA_PATAMAR_1);

        acionamento.setInicioVigencia(LocalDate.of(2026, 1, 1));

        acionamento.setFimVigencia(LocalDate.of(2026, 6, 30));

        acionamento.setDataDivulgacao(LocalDate.of(2025, 12, 20));

        acionamento.setReferenciaOficial("https://fonte-oficial.gov.br");

        AcionamentoBandeiraResponseDTO dto =
                mapper.paraResponseDTO(acionamento);

        assertNotNull(dto);

        assertEquals(id, dto.id());

        assertEquals(TipoBandeira.VERMELHA_PATAMAR_1,
                dto.tipoBandeira());

        assertEquals(LocalDate.of(2026, 1, 1),
                dto.inicioVigencia());

        assertEquals(LocalDate.of(2026, 6, 30),
                dto.fimVigencia());

        assertEquals(LocalDate.of(2025, 12, 20),
                dto.dataDivulgacao());

        assertEquals("https://fonte-oficial.gov.br",
                dto.referenciaOficial());
    }

    @Test
    void deveAtualizarSomenteFimVigencia() {

        UUID id = UUID.randomUUID();

        AcionamentoBandeira acionamento =
                new AcionamentoBandeira();

        acionamento.setId(id);
        acionamento.setTipoBandeira(
                TipoBandeira.VERMELHA_PATAMAR_2);

        acionamento.setInicioVigencia(
                LocalDate.of(2026, 1, 1));

        acionamento.setFimVigencia(null);

        acionamento.setDataDivulgacao(LocalDate.of(2025, 12, 20));
        acionamento.setReferenciaOficial("https://fonte-original.gov.br");

        AcionamentoBandeiraAtualizacaoDTO dto =
                new AcionamentoBandeiraAtualizacaoDTO(
                        LocalDate.of(2026, 6, 30));

        mapper.atualizarEntidade(dto, acionamento);

        assertEquals(id, acionamento.getId());

        assertEquals(TipoBandeira.VERMELHA_PATAMAR_2,
                acionamento.getTipoBandeira());

        assertEquals(LocalDate.of(2026, 1, 1),
                acionamento.getInicioVigencia());

        assertEquals(LocalDate.of(2026, 6, 30),
                acionamento.getFimVigencia());

        assertEquals(LocalDate.of(2025, 12, 20),
                acionamento.getDataDivulgacao());

        assertEquals("https://fonte-original.gov.br",
                acionamento.getReferenciaOficial());
    }

    @Test
    void devePreservarFimVigenciaNuloNoCadastro() {

        AcionamentoBandeiraCadastroDTO dto =
                new AcionamentoBandeiraCadastroDTO(
                        TipoBandeira.VERDE,
                        LocalDate.of(2026, 1, 1),
                        null,
                        LocalDate.of(2025, 12, 20),
                        "https://fonte-oficial.gov.br");

        AcionamentoBandeira acionamento =
                mapper.paraEntidade(dto);

        assertNotNull(acionamento);
        assertNull(acionamento.getFimVigencia());
    }

    @Test
    void deveRetornarNullQuandoOrigemForNull() {

        assertNull(mapper.paraEntidade(null));
        assertNull(mapper.paraResponseDTO(null));
    }
}
