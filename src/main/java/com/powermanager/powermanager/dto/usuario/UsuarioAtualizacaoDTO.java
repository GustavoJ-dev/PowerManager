package com.powermanager.powermanager.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioAtualizacaoDTO(

        @NotBlank(message = "O username é obrigatório.")
        @Size(max = 100, message = "O username deve ter no máximo 100 caracteres.")
        String username,

        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String nome) {
}
