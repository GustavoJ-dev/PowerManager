package com.powermanager.powermanager.dto.medidor;

import com.powermanager.powermanager.entity.enums.ClasseConsumo;
import com.powermanager.powermanager.entity.enums.TipoLigacao;
import com.powermanager.powermanager.entity.enums.TipoMedidor;

import java.util.UUID;

public record MedidorResponseDTO (UUID id,
                                  String numero,
                                  String localizacao,
                                  TipoMedidor tipoMedidor,
                                  TipoLigacao tipoLigacao,
                                  ClasseConsumo classeConsumo,
                                  Integer dias,
                                  UUID clienteId){
}
