package com.powermanager.powermanager.dto.bandeira_tarifaria;

import com.powermanager.powermanager.enums.TipoBandeira;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BandeiraTarifariaResponseDTO(

        UUID id,
        TipoBandeira tipoBandeira,
        BigDecimal valorAdicional,
        LocalDate inicioVigencia,
        LocalDate fimVigencia
) {
}
