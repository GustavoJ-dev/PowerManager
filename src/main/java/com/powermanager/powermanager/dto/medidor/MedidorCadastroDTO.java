package com.powermanager.powermanager.dto.medidor;

import com.powermanager.powermanager.enums.ClasseConsumo;
import com.powermanager.powermanager.enums.TipoLigacao;
import com.powermanager.powermanager.enums.TipoMedidor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record MedidorCadastroDTO (

        @NotBlank(message = "O número do medidor é obrigatório.")
        @Size(max = 30, message = "O número deve ter no máximo 30 caracteres.")
        String numero,

        @NotBlank(message = "A localização é obrigatória.")
        @Size(max = 30, message = "A localização deve ter no máximo 30 caracteres.")
        String localizacao,

        @NotBlank(message = "O tipoMedidor do medidor é obrigatório.")
        @NotNull(message = "O tipoMedidor do medidor é obrigatório.")
        TipoMedidor tipoMedidor,

        @NotNull(message = "O código de fase é obrigatório.")
        TipoLigacao tipoLigacao,

        @NotNull(message = "O tipoMedidor de fatura é obrigatório.")
        ClasseConsumo classeConsumo,

        @NotNull(message = "A quantidade de dias é obrigatória.")
        @Positive(message = "A quantidade de dias deve ser positiva.")
        Integer dias,

        @NotNull(message = "O cliente associado é obrigatório.")
        UUID clienteId){
}
