package com.powermanager.powermanager.dto.cliente;

import java.util.UUID;

public record ClienteResponseDTO(UUID id,
                                 String nome,
                                 String email,
                                 String telefone) {
}
