package com.powermanager.powermanager.dto.acionamento_bandeira;

import com.powermanager.powermanager.enums.TipoBandeira;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AcionamentoBandeiraCadastroDTO (

        @NotNull
        TipoBandeira tipoBandeira,

        @NotNull
        LocalDate inicioVigencia,
        LocalDate fimVigencia,

        @NotNull
        LocalDate dataDivulgacao,

        @NotBlank
        @Size(max = 500)
        String referenciaOficial
){
}
