package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.regra_tributaria.RegraTributariaAtualizacaoDTO;
import com.powermanager.powermanager.dto.regra_tributaria.RegraTributariaCadastroDTO;
import com.powermanager.powermanager.dto.regra_tributaria.RegraTributariaResponseDTO;
import com.powermanager.powermanager.entity.RegraTributaria;
import com.powermanager.powermanager.enums.AbrangenciaTributaria;
import com.powermanager.powermanager.enums.TipoCalculoTributario;
import com.powermanager.powermanager.enums.TipoTributo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RegraTributariaMapperTest {

    private final RegraTributariaMapper mapper = new RegraTributariaMapperImpl();

    @Test
    void deveConverterCadastroDTOParaEntidade() {

        RegraTributariaCadastroDTO dto =
                new RegraTributariaCadastroDTO(
                        TipoTributo.ICMS,
                        AbrangenciaTributaria.ESTADUAL,
                        "BA",
                        null,
                        null,
                        TipoCalculoTributario.PERCENTUAL,
                        new BigDecimal("18.000000"),
                        null,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 12, 31));

        RegraTributaria regra = mapper.paraEntidade(dto);

        assertNotNull(regra);

        assertNull(regra.getId());

        assertEquals(TipoTributo.ICMS, regra.getTipoTributo());

        assertEquals(AbrangenciaTributaria.ESTADUAL,
                regra.getAbrangencia());

        assertEquals("BA", regra.getEstado());

        assertNull(regra.getMunicipio());
        assertNull(regra.getCodigoIbge());

        assertEquals(TipoCalculoTributario.PERCENTUAL,
                regra.getTipoCalculo());

        assertEquals(new BigDecimal("18.000000"),
                regra.getAliquota());

        assertNull(regra.getValorFixo());

        assertEquals(LocalDate.of(2026, 1, 1),
                regra.getInicioVigencia());

        assertEquals(LocalDate.of(2026, 12, 31),
                regra.getFimVigencia());
    }

    @Test
    void deveConverterEntidadeParaResponseDTO() {

        UUID id = UUID.randomUUID();

        RegraTributaria regra = new RegraTributaria();

        regra.setId(id);
        regra.setTipoTributo(TipoTributo.ICMS);
        regra.setAbrangencia(AbrangenciaTributaria.ESTADUAL);
        regra.setEstado("BA");
        regra.setMunicipio(null);
        regra.setCodigoIbge(null);
        regra.setTipoCalculo(TipoCalculoTributario.PERCENTUAL);
        regra.setAliquota(new BigDecimal("18.000000"));
        regra.setValorFixo(null);
        regra.setInicioVigencia(LocalDate.of(2026, 1, 1));
        regra.setFimVigencia(LocalDate.of(2026, 12, 31));

        RegraTributariaResponseDTO dto = mapper.paraResponseDTO(regra);

        assertNotNull(dto);

        assertEquals(id, dto.id());
        assertEquals(TipoTributo.ICMS, dto.tipoTributo());

        assertEquals(AbrangenciaTributaria.ESTADUAL,
                dto.abrangencia());

        assertEquals("BA", dto.estado());
        assertNull(dto.municipio());
        assertNull(dto.codigoIbge());

        assertEquals(TipoCalculoTributario.PERCENTUAL,
                dto.tipoCalculo());

        assertEquals(new BigDecimal("18.000000"),
                dto.aliquota());

        assertNull(dto.valorFixo());

        assertEquals(LocalDate.of(2026, 1, 1),
                dto.inicioVigencia());

        assertEquals(LocalDate.of(2026, 12, 31),
                dto.fimVigencia());
    }

    @Test
    void deveAtualizarExclusivamenteFimVigencia() {

        UUID id = UUID.randomUUID();

        RegraTributaria regra = new RegraTributaria();

        regra.setId(id);
        regra.setTipoTributo(TipoTributo.ICMS);
        regra.setAbrangencia(AbrangenciaTributaria.ESTADUAL);
        regra.setEstado("BA");
        regra.setMunicipio("Paramirim");
        regra.setCodigoIbge("2923602");
        regra.setTipoCalculo(TipoCalculoTributario.PERCENTUAL);
        regra.setAliquota(new BigDecimal("18.000000"));
        regra.setValorFixo(null);
        regra.setInicioVigencia(LocalDate.of(2026, 1, 1));
        regra.setFimVigencia(null);

        RegraTributariaAtualizacaoDTO dto =
                new RegraTributariaAtualizacaoDTO(
                        LocalDate.of(2026, 6, 30));

        mapper.atualizarEntidade(dto, regra);

        assertEquals(id, regra.getId());

        assertEquals(TipoTributo.ICMS, regra.getTipoTributo());

        assertEquals(AbrangenciaTributaria.ESTADUAL,
                regra.getAbrangencia());

        assertEquals("BA", regra.getEstado());
        assertEquals("Paramirim", regra.getMunicipio());
        assertEquals("2923602", regra.getCodigoIbge());

        assertEquals(TipoCalculoTributario.PERCENTUAL,
                regra.getTipoCalculo());

        assertEquals(new BigDecimal("18.000000"),
                regra.getAliquota());

        assertNull(regra.getValorFixo());

        assertEquals(LocalDate.of(2026, 1, 1),
                regra.getInicioVigencia());

        assertEquals(LocalDate.of(2026, 6, 30),
                regra.getFimVigencia());
    }

    @Test
    void devePreservarFimVigenciaNuloNoCadastro() {

        RegraTributariaCadastroDTO dto =
                new RegraTributariaCadastroDTO(
                        TipoTributo.PIS,
                        AbrangenciaTributaria.FEDERAL,
                        null,
                        null,
                        null,
                        TipoCalculoTributario.PERCENTUAL,
                        new BigDecimal("1.650000"),
                        null,
                        LocalDate.of(2026, 1, 1),
                        null);

        RegraTributaria regra = mapper.paraEntidade(dto);

        assertNotNull(regra);
        assertNull(regra.getFimVigencia());
    }

    @Test
    void deveRetornarNullQuandoOrigemForNull() {

        assertNull(mapper.paraEntidade(null));
        assertNull(mapper.paraResponseDTO(null));
    }
}
