package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.fatura.FaturaResponseDTO;
import com.powermanager.powermanager.entity.Fatura;
import com.powermanager.powermanager.entity.Medidor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FaturaMapperTest {

    private final FaturaMapper mapper = new FaturaMapperImpl();

    @Test
    void deveConverterEntidadeParaResponseDTO() {
        UUID id = UUID.randomUUID();
        UUID medidorId = UUID.randomUUID();

        Medidor medidor = new Medidor();
        medidor.setId(medidorId);

        Fatura fatura = new Fatura();
        fatura.setId(id);
        fatura.setMedidor(medidor);
        fatura.setCompetencia(LocalDate.of(2026, 1, 1));
        fatura.setUnidadesConsumidas(new BigDecimal("120.50"));
        fatura.setCustoPorUnidadeAplicado(new BigDecimal("1.50"));
        fatura.setAluguelMedidorAplicado(new BigDecimal("10.00"));
        fatura.setTaxaServicoAplicada(new BigDecimal("2.00"));
        fatura.setImpostoServicoAplicado(new BigDecimal("3.00"));
        fatura.setCessAplicado(new BigDecimal("0.50"));
        fatura.setTaxaFixaAplicada(new BigDecimal("5.00"));
        fatura.setValorTotal(new BigDecimal("201.25"));

        // Se a entidade exigir um status no construtor,
        // ajuste este teste conforme a implementação real.
        FaturaResponseDTO dto = mapper.paraResponseDTO(fatura);

        assertNotNull(dto);
        assertEquals(id, dto.id());
        assertEquals(medidorId, dto.medidorId());
        assertEquals(LocalDate.of(2026, 1, 1), dto.competencia());
        assertEquals(new BigDecimal("120.50"), dto.unidadesConsumidas());
        assertEquals(new BigDecimal("1.50"), dto.custoPorUnidadeAplicado());
        assertEquals(new BigDecimal("10.00"), dto.aluguelMedidorAplicado());
        assertEquals(new BigDecimal("2.00"), dto.taxaServicoAplicada());
        assertEquals(new BigDecimal("3.00"), dto.impostoServicoAplicado());
        assertEquals(new BigDecimal("0.50"), dto.cessAplicado());
        assertEquals(new BigDecimal("5.00"), dto.taxaFixaAplicada());
        assertEquals(new BigDecimal("201.25"), dto.valorTotal());
    }
}
