package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.fatura.FaturaResponseDTO;
import com.powermanager.powermanager.dto.fatura_tributo.FaturaTributoResponseDTO;
import com.powermanager.powermanager.entity.Fatura;
import com.powermanager.powermanager.entity.UnidadeConsumidora;
import com.powermanager.powermanager.enums.OrigemConsumo;
import com.powermanager.powermanager.enums.StatusFatura;
import com.powermanager.powermanager.enums.TipoCalculoTributario;
import com.powermanager.powermanager.enums.TipoTributo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FaturaMapperTest {

    private final FaturaMapper mapper = new FaturaMapperImpl();

    @Test
    void deveConverterEntidadeParaResponseDTO() {

        UUID faturaId = UUID.randomUUID();
        UUID unidadeId = UUID.randomUUID();

        UnidadeConsumidora unidade = new UnidadeConsumidora();
        unidade.setId(unidadeId);

        Fatura fatura = new Fatura();
        fatura.setId(faturaId);
        fatura.setUnidadeConsumidora(unidade);
        fatura.setCompetencia(LocalDate.of(2026, 1, 1));
        fatura.setInicioPeriodo(LocalDate.of(2025, 12, 10));
        fatura.setFimPeriodo(LocalDate.of(2026, 1, 10));
        fatura.setConsumoKwh(new BigDecimal("150.500"));
        fatura.setOrigemConsumo(OrigemConsumo.LEITURA);
        fatura.setTarifaEnergiaTE(new BigDecimal("0.350000"));
        fatura.setTarifaUsoDistribuicaoTUSD(new BigDecimal("0.450000"));
        fatura.setValorTE(new BigDecimal("52.68"));
        fatura.setValorTUSD(new BigDecimal("67.73"));
        fatura.setValorBandeira(new BigDecimal("12.00"));
        fatura.setValorSubtotal(new BigDecimal("132.41"));
        fatura.setValorTotal(new BigDecimal("150.00"));
        fatura.setDataEmissao(LocalDate.of(2026, 1, 11));
        fatura.setDataVencimento(LocalDate.of(2026, 1, 20));
        fatura.setDataPagamento(LocalDate.of(2026, 1, 18));
        fatura.setStatus(StatusFatura.PAGO);

        FaturaTributoResponseDTO tributo = new FaturaTributoResponseDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                TipoTributo.ICMS,
                TipoCalculoTributario.PERCENTUAL,
                new BigDecimal("18.00"),
                new BigDecimal("132.41"),
                null,
                new BigDecimal("23.83"));

        FaturaResponseDTO dto = mapper.paraResponseDTO(
                fatura,
                List.of(tributo));

        assertNotNull(dto);

        assertEquals(faturaId, dto.id());
        assertEquals(unidadeId, dto.unidadeConsumidoraId());
        assertEquals(LocalDate.of(2026, 1, 1), dto.competencia());
        assertEquals(LocalDate.of(2025, 12, 10), dto.inicioPeriodo());
        assertEquals(LocalDate.of(2026, 1, 10), dto.fimPeriodo());

        assertEquals(new BigDecimal("150.500"), dto.consumoKwh());
        assertEquals(OrigemConsumo.LEITURA, dto.origemConsumo());

        assertEquals(new BigDecimal("0.350000"), dto.tarifaEnergiaTE());
        assertEquals(new BigDecimal("0.450000"), dto.tarifaUsoDistribuicaoTUSD());

        assertEquals(new BigDecimal("52.68"), dto.valorTE());
        assertEquals(new BigDecimal("67.73"), dto.valorTUSD());
        assertEquals(new BigDecimal("12.00"), dto.valorBandeira());
        assertEquals(new BigDecimal("132.41"), dto.valorSubtotal());
        assertEquals(new BigDecimal("150.00"), dto.valorTotal());

        assertEquals(LocalDate.of(2026, 1, 11), dto.dataEmissao());
        assertEquals(LocalDate.of(2026, 1, 20), dto.dataVencimento());
        assertEquals(LocalDate.of(2026, 1, 18), dto.dataPagamento());

        assertEquals(1, dto.tributos().size());
        assertEquals(tributo, dto.tributos().get(0));

        assertEquals(StatusFatura.PAGO, dto.status());
    }

    @Test
    void deveConverterFaturaSemTributos() {

        Fatura fatura = new Fatura();

        List<FaturaTributoResponseDTO> tributos = List.of();

        FaturaResponseDTO dto = mapper.paraResponseDTO(fatura, tributos);

        assertNotNull(dto);
        assertNotNull(dto.tributos());
        assertTrue(dto.tributos().isEmpty());
    }

    @Test
    void deveRetornarNullQuandoTodasAsEntradasForemNull() {

        FaturaResponseDTO dto = mapper.paraResponseDTO(null, null);

        assertNull(dto);
    }
}
