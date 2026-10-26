package com.powermanager.powermanager.dto.cliente;

import java.util.UUID;

public record ClienteResponseDTO(UUID id,
                                 String nome,
                                 String endereco,
                                 String cidade,
                                 String estado,
                                 String email,
                                 String telefone) {
}
