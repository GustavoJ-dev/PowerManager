package com.powermanager.powermanager.dto.bandeira_tarifaria;

import com.powermanager.powermanager.enums.TipoBandeira;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BandeiraTarifariaCadastroDTO (

        @NotNull
        TipoBandeira tipoBandeira,

        @NotNull
        @DecimalMin("0.0")
        @Digits(integer = 6, fraction = 6)
        BigDecimal valorAdicional,

        @NotNull
        LocalDate inicioVigencia,

        LocalDate fimVigencia
){
}
