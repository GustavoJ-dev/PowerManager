package com.powermanager.powermanager.dto.cliente;

import com.powermanager.powermanager.dto.medidor.MedidorResponseDTO;
import com.powermanager.powermanager.entity.Medidor;

import java.util.List;
import java.util.UUID;

public record ClienteResponseDTO(UUID id,
                                 String nome,
                                 String endereco,
                                 String cidade,
                                 String estado,
                                 String email,
                                 String telefone,
                                 List<MedidorResponseDTO> medidores) {
}
