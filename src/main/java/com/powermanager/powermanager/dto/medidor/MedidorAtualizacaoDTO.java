package com.powermanager.powermanager.dto.medidor;

import com.powermanager.powermanager.enums.TipoLigacao;
import com.powermanager.powermanager.enums.TipoMedidor;
import jakarta.validation.constraints.NotNull;

public record MedidorAtualizacaoDTO (

        @NotNull(message = "O tipo do medidor é obrigatório.")
        TipoMedidor tipoMedidor,

        @NotNull(message = "O tipo de ligação é obrigatório.")
        TipoLigacao tipoLigacao
){
}
