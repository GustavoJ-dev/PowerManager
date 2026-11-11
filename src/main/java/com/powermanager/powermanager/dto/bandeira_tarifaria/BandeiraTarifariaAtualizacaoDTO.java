package com.powermanager.powermanager.dto.bandeira_tarifaria;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BandeiraTarifariaAtualizacaoDTO(

        @NotNull
        LocalDate fimVigencia
) {
}
