package com.powermanager.powermanager.dto.medidor;

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

        @NotBlank(message = "O tipo do medidor é obrigatório.")
        @Size(max = 30, message = "O tipo deve ter no máximo 30 caracteres.")
        String tipo,

        @NotBlank(message = "O código de fase é obrigatório.")
        @Size(max = 10, message = "O código de fase deve ter no máximo 10 caracteres.")
        String codigoFase,

        @NotBlank(message = "O tipo de fatura é obrigatório.")
        @Size(max = 30, message = "O tipo de fatura deve ter no máximo 30 caracteres.")
        String tipoFatura,

        @NotNull(message = "A quantidade de dias é obrigatória.")
        @Positive(message = "A quantidade de dias deve ser positiva.")
        Integer dias,

        @NotNull(message = "O cliente associado é obrigatório.")
        UUID clienteId){
}
