package com.powermanager.powermanager.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioSenhaAtualizacaoDTO (

        @NotBlank
        String senhaAtual,

        @NotBlank
        @Size(min = 8, max = 72)
        String novaSenha
){
}
