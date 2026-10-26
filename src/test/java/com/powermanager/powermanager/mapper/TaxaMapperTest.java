package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.taxa.TaxaAtualizacaoDTO;
import com.powermanager.powermanager.dto.taxa.TaxaCadastroDTO;
import com.powermanager.powermanager.dto.taxa.TaxaResponseDTO;
import com.powermanager.powermanager.entity.Taxa;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


public class TaxaMapperTest {


    private final TaxaMapper mapper = new TaxaMapperImpl();

    @Test
    void deveConverterCadastroDTOParaEntidade() {
        LocalDateTime inicio = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 12, 31, 23, 59);

        TaxaCadastroDTO dto = new TaxaCadastroDTO(
                new BigDecimal("1.50"),
                new BigDecimal("10.00"),
                new BigDecimal("2.00"),
                new BigDecimal("3.00"),
                new BigDecimal("0.50"),
                new BigDecimal("5.00"),
                inicio,
                fim
        );

        Taxa taxa = mapper.paraEntidade(dto);

        assertNotNull(taxa);
        assertEquals(new BigDecimal("1.50"), taxa.getCustoPorUnidade());
        assertEquals(new BigDecimal("10.00"), taxa.getAluguelMedidor());
        assertEquals(new BigDecimal("2.00"), taxa.getTaxaServico());
        assertEquals(new BigDecimal("3.00"), taxa.getImpostoServico());
        assertEquals(new BigDecimal("0.50"), taxa.getCess());
        assertEquals(new BigDecimal("5.00"), taxa.getTaxaFixa());
        assertEquals(inicio, taxa.getInicioVigencia());
        assertEquals(fim, taxa.getFimVigencia());
        assertNull(taxa.getId());
    }

    @Test
    void deveConverterEntidadeParaResponseDTO() {
        UUID id = UUID.randomUUID();
        LocalDateTime inicio = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 12, 31, 23, 59);

        Taxa taxa = new Taxa();
        taxa.setId(id);
        taxa.setCustoPorUnidade(new BigDecimal("1.50"));
        taxa.setAluguelMedidor(new BigDecimal("10.00"));
        taxa.setTaxaServico(new BigDecimal("2.00"));
        taxa.setImpostoServico(new BigDecimal("3.00"));
        taxa.setCess(new BigDecimal("0.50"));
        taxa.setTaxaFixa(new BigDecimal("5.00"));
        taxa.setInicioVigencia(inicio);
        taxa.setFimVigencia(fim);

        TaxaResponseDTO dto = mapper.paraResponseDTO(taxa);

        assertNotNull(dto);
        assertEquals(id, dto.id());
        assertEquals(new BigDecimal("1.50"), dto.custoPorUnidade());
        assertEquals(new BigDecimal("10.00"), dto.aluguelMedidor());
        assertEquals(new BigDecimal("2.00"), dto.taxaServico());
        assertEquals(new BigDecimal("3.00"), dto.impostoServico());
        assertEquals(new BigDecimal("0.50"), dto.cess());
        assertEquals(new BigDecimal("5.00"), dto.taxaFixa());
        assertEquals(inicio, dto.inicioVigencia());
        assertEquals(fim, dto.fimVigencia());
    }

    @Test
    void deveAtualizarEntidadePreservandoIdEFimVigencia() {
        UUID id = UUID.randomUUID();
        LocalDateTime inicioAnterior = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 12, 31, 23, 59);
        LocalDateTime novoInicio = LocalDateTime.of(2026, 2, 1, 0, 0);

        Taxa taxa = new Taxa();
        taxa.setId(id);
        taxa.setCustoPorUnidade(new BigDecimal("1.00"));
        taxa.setAluguelMedidor(new BigDecimal("8.00"));
        taxa.setTaxaServico(new BigDecimal("1.00"));
        taxa.setImpostoServico(new BigDecimal("2.00"));
        taxa.setCess(new BigDecimal("0.25"));
        taxa.setTaxaFixa(new BigDecimal("4.00"));
        taxa.setInicioVigencia(inicioAnterior);
        taxa.setFimVigencia(fim);

        TaxaAtualizacaoDTO dto = new TaxaAtualizacaoDTO(
                new BigDecimal("2.50"),
                new BigDecimal("12.00"),
                new BigDecimal("3.00"),
                new BigDecimal("4.00"),
                new BigDecimal("0.75"),
                new BigDecimal("6.00"),
                novoInicio
        );

        mapper.atualizarEntidade(dto, taxa);

        assertEquals(id, taxa.getId());
        assertEquals(fim, taxa.getFimVigencia());

        assertEquals(new BigDecimal("2.50"), taxa.getCustoPorUnidade());
        assertEquals(new BigDecimal("12.00"), taxa.getAluguelMedidor());
        assertEquals(new BigDecimal("3.00"), taxa.getTaxaServico());
        assertEquals(new BigDecimal("4.00"), taxa.getImpostoServico());
        assertEquals(new BigDecimal("0.75"), taxa.getCess());
        assertEquals(new BigDecimal("6.00"), taxa.getTaxaFixa());
        assertEquals(novoInicio, taxa.getInicioVigencia());
    }
}
