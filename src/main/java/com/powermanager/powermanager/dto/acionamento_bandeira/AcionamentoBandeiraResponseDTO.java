package com.powermanager.powermanager.dto.acionamento_bandeira;

import com.powermanager.powermanager.enums.TipoBandeira;

import java.time.LocalDate;
import java.util.UUID;

public record AcionamentoBandeiraResponseDTO (

        UUID id,
        TipoBandeira tipoBandeira,
        LocalDate inicioVigencia,
        LocalDate fimVigencia,
        LocalDate dataDivulgacao,
        String referenciaOficial
){
}
