package com.powermanager.powermanager.dto.regra_tributaria;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RegraTributariaAtualizacaoDTO (

        @NotNull
        LocalDate fimVigencia
){
}
