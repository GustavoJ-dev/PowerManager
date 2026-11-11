package com.powermanager.powermanager.dto.acionamento_bandeira;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AcionamentoBandeiraAtualizacaoDTO (

        @NotNull
        LocalDate fimVigencia
){
}
