package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.taxa.TaxaCadastroDTO;
import com.powermanager.powermanager.dto.taxa.TaxaResponseDTO;
import com.powermanager.powermanager.entity.Taxa;
import com.powermanager.powermanager.enums.ClasseConsumo;
import com.powermanager.powermanager.enums.ModalidadeTarifaria;
import com.powermanager.powermanager.enums.SubgrupoTarifario;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TaxaMapperTest {

    private final TaxaMapper mapper = new TaxaMapperImpl();

    @Test
    void deveConverterCadastroDTOParaEntidade() {

        TaxaCadastroDTO dto = new TaxaCadastroDTO(
                "Distribuidora Teste",
                SubgrupoTarifario.B1,
                ClasseConsumo.RESIDENCIAL,
                ModalidadeTarifaria.CONVENCIONAL,
                new BigDecimal("0.350000"),
                new BigDecimal("0.420000"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31));

        Taxa taxa = mapper.paraEntidade(dto);

        assertNotNull(taxa);

        assertNull(taxa.getId());

        assertEquals("Distribuidora Teste", taxa.getDistribuidora());

        assertEquals(
                SubgrupoTarifario.B1,
                taxa.getSubgrupoTarifario());

        assertEquals(
                ClasseConsumo.RESIDENCIAL,
                taxa.getClasseConsumo());

        assertEquals(
                ModalidadeTarifaria.CONVENCIONAL,
                taxa.getModalidadeTarifaria());

        assertEquals(
                new BigDecimal("0.350000"),
                taxa.getTarifaEnergia());

        assertEquals(
                new BigDecimal("0.420000"),
                taxa.getTarifaUsoDistribuicao());

        assertEquals(
                LocalDate.of(2026, 1, 1),
                taxa.getInicioVigencia());

        assertEquals(
                LocalDate.of(2026, 12, 31),
                taxa.getFimVigencia());
    }

    @Test
    void deveConverterEntidadeParaResponseDTO() {

        UUID id = UUID.randomUUID();

        Taxa taxa = new Taxa();

        taxa.setId(id);
        taxa.setDistribuidora("Distribuidora Teste");
        taxa.setSubgrupoTarifario(SubgrupoTarifario.B1);
        taxa.setClasseConsumo(ClasseConsumo.RESIDENCIAL);
        taxa.setModalidadeTarifaria(ModalidadeTarifaria.CONVENCIONAL);
        taxa.setTarifaEnergia(new BigDecimal("0.350000"));
        taxa.setTarifaUsoDistribuicao(new BigDecimal("0.420000"));
        taxa.setInicioVigencia(LocalDate.of(2026, 1, 1));
        taxa.setFimVigencia(LocalDate.of(2026, 12, 31));

        TaxaResponseDTO dto = mapper.paraResponseDTO(taxa);

        assertNotNull(dto);

        assertEquals(id, dto.id());

        assertEquals("Distribuidora Teste", dto.distribuidora());

        assertEquals(
                SubgrupoTarifario.B1,
                dto.subgrupoTarifario());

        assertEquals(
                ClasseConsumo.RESIDENCIAL,
                dto.classeConsumo());

        assertEquals(
                ModalidadeTarifaria.CONVENCIONAL,
                dto.modalidadeTarifaria());

        assertEquals(
                new BigDecimal("0.350000"),
                dto.tarifaEnergia());

        assertEquals(
                new BigDecimal("0.420000"),
                dto.tarifaUsoDistribuicao());

        assertEquals(
                LocalDate.of(2026, 1, 1),
                dto.inicioVigencia());

        assertEquals(
                LocalDate.of(2026, 12, 31),
                dto.fimVigencia());
    }

    @Test
    void devePreservarFimVigenciaNulo() {

        TaxaCadastroDTO dto = new TaxaCadastroDTO(
                "Distribuidora Teste",
                SubgrupoTarifario.B1,
                ClasseConsumo.RESIDENCIAL,
                ModalidadeTarifaria.CONVENCIONAL,
                new BigDecimal("0.350000"),
                new BigDecimal("0.420000"),
                LocalDate.of(2026, 1, 1),
                null
        );

        Taxa taxa = mapper.paraEntidade(dto);

        assertNotNull(taxa);
        assertNull(taxa.getFimVigencia());
    }

    @Test
    void deveRetornarNullQuandoOrigemForNull() {

        assertNull(mapper.paraEntidade(null));
        assertNull(mapper.paraResponseDTO(null));
    }
}