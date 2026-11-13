package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.fatura_tributo.FaturaTributoResponseDTO;
import com.powermanager.powermanager.entity.FaturaTributo;
import com.powermanager.powermanager.entity.RegraTributaria;
import com.powermanager.powermanager.enums.TipoCalculoTributario;
import com.powermanager.powermanager.enums.TipoTributo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FaturaTributoMapperTest {

    private FaturaTributoMapper mapper;

    @BeforeEach
    void setUp() {

        mapper = new FaturaTributoMapperImpl();
    }

    @Test
    void deveConverterEntidadeParaResponseDTO() {
        UUID id = UUID.randomUUID();
        UUID regraId = UUID.randomUUID();

        RegraTributaria regra = new RegraTributaria();
        regra.setId(regraId);

        FaturaTributo tributo = new FaturaTributo();
        tributo.setId(id);
        tributo.setRegraTributaria(regra);
        tributo.setTipoTributo(TipoTributo.ICMS);
        tributo.setTipoCalculo(TipoCalculoTributario.PERCENTUAL);
        tributo.setAliquotaAplicada(new BigDecimal("18.000000"));
        tributo.setBaseCalculo(new BigDecimal("150.00"));
        tributo.setValorFixoAplicado(null);
        tributo.setValorTributo(new BigDecimal("27.00"));

        FaturaTributoResponseDTO dto = mapper.paraResponseDTO(tributo);

        assertNotNull(dto);
        assertEquals(id, dto.id());
        assertEquals(regraId, dto.regraTributariaId());
        assertEquals(TipoTributo.ICMS, dto.tipoTributo());
        assertEquals(TipoCalculoTributario.PERCENTUAL, dto.tipoCalculo());
        assertEquals(0, new BigDecimal("18.000000").compareTo(dto.aliquotaAplicada()));
        assertEquals(0, new BigDecimal("150.00").compareTo(dto.baseCalculo()));
        assertNull(dto.valorFixoAplicado());
        assertEquals(0, new BigDecimal("27.00").compareTo(dto.valorTributo()));
    }

    @Test
    void deveMapearTributoComValorFixo() {
        RegraTributaria regra = new RegraTributaria();
        regra.setId(UUID.randomUUID());

        FaturaTributo tributo = new FaturaTributo();
        tributo.setId(UUID.randomUUID());
        tributo.setRegraTributaria(regra);
        tributo.setTipoTributo(TipoTributo.CIP_COSIP);
        tributo.setTipoCalculo(TipoCalculoTributario.VALOR_FIXO);
        tributo.setAliquotaAplicada(null);
        tributo.setBaseCalculo(null);
        tributo.setValorFixoAplicado(new BigDecimal("25.00"));
        tributo.setValorTributo(new BigDecimal("25.00"));

        FaturaTributoResponseDTO dto = mapper.paraResponseDTO(tributo);

        assertNotNull(dto);
        assertEquals(TipoTributo.CIP_COSIP, dto.tipoTributo());
        assertEquals(TipoCalculoTributario.VALOR_FIXO, dto.tipoCalculo());
        assertNull(dto.aliquotaAplicada());
        assertNull(dto.baseCalculo());
        assertEquals(0, new BigDecimal("25.00").compareTo(dto.valorFixoAplicado()));
        assertEquals(0, new BigDecimal("25.00").compareTo(dto.valorTributo()));
    }

    @Test
    void deveRetornarNullQuandoEntidadeForNull() {
        assertNull(mapper.paraResponseDTO(null));
    }
}
