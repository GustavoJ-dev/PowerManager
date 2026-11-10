package com.powermanager.powermanager.dto.usuario;

import com.powermanager.powermanager.enums.TipoUsuario;

import java.util.UUID;

public record UsuarioResponseDTO(UUID id,
                                 String username,
                                 String nome,
                                 TipoUsuario tipoUsuario,
                                 UUID clienteId) {
}
