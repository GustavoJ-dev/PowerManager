package com.powermanager.powermanager.dto.leitura;

import com.powermanager.powermanager.enums.TipoLeitura;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record LeituraCadastroDTO(

        @NotNull(message = "O valor da leitura é obrigatório.")
        @DecimalMin(value = "0.0", message = "O valor da leitura não pode ser negativo.")
        @Digits(integer = 9, fraction = 3, message = "O valor deve possuir até 9 dígitos inteiros e 3 decimais.")
        BigDecimal valorLeitura,

        @NotNull(message = "A data e hora da leitura são obrigatórias.")
        LocalDateTime dataLeitura,

        @NotNull(message = "O tipo da leitura é obrigatório.")
        TipoLeitura tipoLeitura,

        @NotNull(message = "O medidor associado é obrigatório.")
        UUID medidorId
) {
}
