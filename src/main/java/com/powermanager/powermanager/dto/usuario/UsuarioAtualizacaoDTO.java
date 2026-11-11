package com.powermanager.powermanager.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioAtualizacaoDTO(

        @NotBlank
        @Size(max = 50)
        String username,

        @NotBlank
        @Size(max = 100)
        String nome
) {
}
