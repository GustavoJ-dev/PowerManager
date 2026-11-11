package com.powermanager.powermanager.dto.fatura;

import com.powermanager.powermanager.enums.OrigemConsumo;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FaturaGeracaoDTO(

        @NotNull
        UUID unidadeConsumidoraId,

        @NotNull
        LocalDate competencia,

        @NotNull
        OrigemConsumo origemConsumo,

        @DecimalMin("0.0")
        @Digits(integer = 9, fraction = 3)
        BigDecimal consumoKwh
) {
}
