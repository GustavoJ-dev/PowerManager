package com.powermanager.powermanager.dto.usuario;

import com.powermanager.powermanager.enums.TipoUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UsuarioCadastroDTO(

        @NotBlank
        @Size(max = 50)
        String username,

        @NotBlank
        @Size(max = 100)
        String nome,

        @NotBlank
        @Size(min = 8, max = 72)
        String senha,

        @NotNull
        TipoUsuario tipoUsuario,

        UUID clienteId
) {
}
