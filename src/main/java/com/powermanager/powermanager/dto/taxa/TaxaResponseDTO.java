package com.powermanager.powermanager.dto.taxa;

import com.powermanager.powermanager.enums.ClasseConsumo;
import com.powermanager.powermanager.enums.ModalidadeTarifaria;
import com.powermanager.powermanager.enums.SubgrupoTarifario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TaxaResponseDTO(

        UUID id,
        String distribuidora,
        SubgrupoTarifario subgrupoTarifario,
        ClasseConsumo classeConsumo,
        ModalidadeTarifaria modalidadeTarifaria,
        BigDecimal tarifaEnergia,
        BigDecimal tarifaUsoDistribuicao,
        LocalDate inicioVigencia,
        LocalDate fimVigencia
) {
}
