package com.powermanager.powermanager.dto.unidade_consumidora;

import com.powermanager.powermanager.enums.ClasseConsumo;
import com.powermanager.powermanager.enums.ModalidadeTarifaria;
import com.powermanager.powermanager.enums.SubgrupoTarifario;

import java.util.UUID;

public record UnidadeConsumidoraResponseDTO (

        UUID id,
        String codigo,
        String endereco,
        String cidade,
        String estado,
        String cep,
        String distribuidora,
        ClasseConsumo classeConsumo,
        SubgrupoTarifario subgrupoTarifario,
        ModalidadeTarifaria modalidadeTarifaria,
        Boolean ativa,
        UUID clienteId
){
}
