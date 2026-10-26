package com.powermanager.powermanager.dto.usuario;

import com.powermanager.powermanager.entity.enums.TipoUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UsuarioCadastroDTO(

        @NotBlank(message = "O username é obrigatório.")
        @Size(max = 100, message = "O username deve ter no máximo 100 caracteres.")
        String username,

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String nome,

        @NotBlank(message = "A senha é obrigatória.")
        String senha,

        @NotNull(message = "O tipo de Usuário é obrigatório.")
        TipoUsuario tipoUsuario,

        UUID clienteId) {
}
