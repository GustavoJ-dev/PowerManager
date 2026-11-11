package com.powermanager.powermanager.dto.medidor;

import com.powermanager.powermanager.enums.TipoLigacao;
import com.powermanager.powermanager.enums.TipoMedidor;

import java.time.LocalDate;
import java.util.UUID;

public record MedidorResponseDTO (

        UUID id,
        String numero,
        TipoMedidor tipoMedidor,
        TipoLigacao tipoLigacao,
        Boolean ativo,
        LocalDate dataInstalacao,
        LocalDate dataRetirada,
        UUID unidadeConsumidoraId
){
}
