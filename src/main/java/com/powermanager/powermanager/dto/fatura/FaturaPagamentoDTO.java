package com.powermanager.powermanager.dto.fatura;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record FaturaPagamentoDTO(

        @NotNull
        LocalDate dataPagamento
) {
}
