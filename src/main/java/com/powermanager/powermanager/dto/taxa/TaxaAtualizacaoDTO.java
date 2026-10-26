package com.powermanager.powermanager.dto.taxa;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TaxaAtualizacaoDTO(

        @NotNull(message = "O custo por unidade é obrigatório.")
        @DecimalMin("0.00")
        @Digits(integer = 8, fraction = 2)
        BigDecimal custoPorUnidade,

        @NotNull(message = "O aluguel do medidor é obrigatório.")
        @DecimalMin("0.00")
        @Digits(integer = 8, fraction = 2)
        BigDecimal aluguelMedidor,

        @NotNull(message = "A taxa de serviço é obrigatória.")
        @DecimalMin("0.00")
        @Digits(integer = 8, fraction = 2)
        BigDecimal taxaServico,

        @NotNull(message = "O imposto de serviço é obrigatório.")
        @DecimalMin("0.00")
        @Digits(integer = 8, fraction = 2)
        BigDecimal impostoServico,

        @NotNull(message = "O CESS é obrigatório.")
        @DecimalMin("0.00")
        @Digits(integer = 8, fraction = 2)
        BigDecimal cess,

        @NotNull(message = "A taxa fixa é obrigatória.")
        @DecimalMin("0.00")
        @Digits(integer = 8, fraction = 2)
        BigDecimal taxaFixa,

        LocalDateTime inicioVigencia) {
}
