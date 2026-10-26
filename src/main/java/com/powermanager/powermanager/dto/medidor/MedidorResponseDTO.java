package com.powermanager.powermanager.dto.medidor;

import java.util.UUID;

public record MedidorResponseDTO (UUID id,
                                  String numero,
                                  String localizacao,
                                  String tipo,
                                  String codigoFase,
                                  String tipoFatura,
                                  Integer dias,
                                  UUID clienteId){
}
