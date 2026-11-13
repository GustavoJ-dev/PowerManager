package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.leitura.LeituraCadastroDTO;
import com.powermanager.powermanager.dto.leitura.LeituraResponseDTO;
import com.powermanager.powermanager.entity.Leitura;
import com.powermanager.powermanager.entity.Medidor;
import com.powermanager.powermanager.enums.TipoLeitura;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LeituraMapperTest {

    private final LeituraMapper mapper = new LeituraMapperImpl();

    @Test
    void deveConverterCadastroDTOParaEntidade() {

        LocalDateTime dataLeitura = LocalDateTime.of(
                2026, 11, 12, 10, 30);

        LeituraCadastroDTO dto = new LeituraCadastroDTO(
                new BigDecimal("1250.500"),
                dataLeitura,
                TipoLeitura.REAL,
                UUID.randomUUID());

        Leitura leitura = mapper.paraEntidade(dto);

        assertNotNull(leitura);

        assertNull(leitura.getId());
        assertNull(leitura.getMedidor());

        assertEquals(new BigDecimal("1250.500"),
                leitura.getValorLeitura());

        assertEquals(dataLeitura, leitura.getDataLeitura());
        assertEquals(TipoLeitura.REAL, leitura.getTipoLeitura());
    }

    @Test
    void deveConverterEntidadeParaResponseDTO() {

        UUID leituraId = UUID.randomUUID();
        UUID medidorId = UUID.randomUUID();

        LocalDateTime dataLeitura = LocalDateTime.of(
                2026, 11, 12, 10, 30);

        Medidor medidor = new Medidor();
        medidor.setId(medidorId);

        Leitura leitura = new Leitura();
        leitura.setId(leituraId);
        leitura.setValorLeitura(new BigDecimal("1250.500"));
        leitura.setDataLeitura(dataLeitura);
        leitura.setTipoLeitura(TipoLeitura.REAL);
        leitura.setMedidor(medidor);

        LeituraResponseDTO dto = mapper.paraResponseDTO(leitura);

        assertNotNull(dto);

        assertEquals(leituraId, dto.id());
        assertEquals(medidorId, dto.medidorId());

        assertEquals(new BigDecimal("1250.500"),
                dto.valorLeitura());

        assertEquals(dataLeitura, dto.dataLeitura());
        assertEquals(TipoLeitura.REAL, dto.tipoLeitura());
    }

    @Test
    void deveRetornarNullQuandoOrigemForNull() {

        assertNull(mapper.paraEntidade(null));
        assertNull(mapper.paraResponseDTO(null));
    }
}
