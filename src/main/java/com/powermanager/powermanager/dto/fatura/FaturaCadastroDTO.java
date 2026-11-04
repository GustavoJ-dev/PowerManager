package com.powermanager.powermanager.dto.fatura;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FaturaCadastroDTO(

        @NotNull(message = "O medidor é obrigatório.")
        UUID medidorId,

        @NotNull(message = "A competência é obrigatória.")
        LocalDate competencia,

        @NotNull(message = "As unidades consumidas são obrigatórias.")
        @PositiveOrZero(message = "As unidades consumidas não podem ser negativas.")
        @Digits(integer = 8, fraction = 2,
                message = "O consumo deve possuir no máximo 8 dígitos inteiros e 2 casas decimais.")
        BigDecimal unidadesConsumidas) {
}
