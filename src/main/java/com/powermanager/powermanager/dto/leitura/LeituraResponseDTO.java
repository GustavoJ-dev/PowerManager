package com.powermanager.powermanager.dto.leitura;

import com.powermanager.powermanager.enums.TipoLeitura;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record LeituraResponseDTO(

        UUID id,
        BigDecimal valorLeitura,
        LocalDateTime dataLeitura,
        TipoLeitura tipoLeitura,
        UUID medidorId
) {
}
