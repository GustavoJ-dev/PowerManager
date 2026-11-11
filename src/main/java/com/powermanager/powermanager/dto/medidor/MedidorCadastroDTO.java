package com.powermanager.powermanager.dto.medidor;

import com.powermanager.powermanager.enums.TipoLigacao;
import com.powermanager.powermanager.enums.TipoMedidor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record MedidorCadastroDTO (

        @NotBlank(message = "O número do medidor é obrigatório.")
        @Size(max = 30, message = "O número deve ter no máximo 30 caracteres.")
        String numero,

        @NotNull(message = "O tipo do medidor é obrigatório.")
        TipoMedidor tipoMedidor,

        @NotNull(message = "O tipo de ligação é obrigatório.")
        TipoLigacao tipoLigacao,

        @NotNull(message = "A data de instalação é obrigatória.")
        LocalDate dataInstalacao,

        @NotNull(message = "A unidade consumidora associada é obrigatória.")
        UUID unidadeConsumidoraId
){
}
