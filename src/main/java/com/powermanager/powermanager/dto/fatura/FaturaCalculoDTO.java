package com.powermanager.powermanager.dto.fatura;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record FaturaCalculoDTO(

        @NotNull(message = "As unidades consumidas são obrigatórias.")
        @PositiveOrZero(message = "As unidades consumidas não podem ser negativas.")
        BigDecimal unidadesConsumidas) {
}
