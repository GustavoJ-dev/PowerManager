package com.powermanager.powermanager.dto.taxa;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TaxaCadastroDTO(

        @NotNull(message = "O custo por unidade é obrigatório.")
        @DecimalMin(value = "0.00", message = "O custo por unidade não pode ser negativo.")
        @Digits(integer = 8, fraction = 2,
                message = "O custo por unidade deve ter no máximo 8 dígitos inteiros e 2 decimais.")
        BigDecimal custoPorUnidade,

        @NotNull(message = "O aluguel do medidor é obrigatório.")
        @DecimalMin(value = "0.00", message = "O aluguel do medidor não pode ser negativo.")
        @Digits(integer = 8, fraction = 2,
                message = "O aluguel do medidor deve ter no máximo 8 dígitos inteiros e 2 decimais.")
        BigDecimal aluguelMedidor,

        @NotNull(message = "A taxa de serviço é obrigatória.")
        @DecimalMin(value = "0.00", message = "A taxa de serviço não pode ser negativa.")
        @Digits(integer = 8, fraction = 2,
                message = "A taxa de serviço deve ter no máximo 8 dígitos inteiros e 2 decimais.")
        BigDecimal taxaServico,

        @NotNull(message = "O imposto de serviço é obrigatório.")
        @DecimalMin(value = "0.00", message = "O imposto de serviço não pode ser negativo.")
        @Digits(integer = 8, fraction = 2,
                message = "O imposto de serviço deve ter no máximo 8 dígitos inteiros e 2 decimais.")
        BigDecimal impostoServico,

        @NotNull(message = "O CESS é obrigatório.")
        @DecimalMin(value = "0.00", message = "O CESS não pode ser negativo.")
        @Digits(integer = 8, fraction = 2,
                message = "O CESS deve ter no máximo 8 dígitos inteiros e 2 decimais.")
        BigDecimal cess,

        @NotNull(message = "A taxa fixa é obrigatória.")
        @DecimalMin(value = "0.00", message = "A taxa fixa não pode ser negativa.")
        @Digits(integer = 8, fraction = 2,
                message = "A taxa fixa deve ter no máximo 8 dígitos inteiros e 2 decimais.")
        BigDecimal taxaFixa,

        LocalDateTime inicioVigencia,

        LocalDateTime fimVigencia) {
}
