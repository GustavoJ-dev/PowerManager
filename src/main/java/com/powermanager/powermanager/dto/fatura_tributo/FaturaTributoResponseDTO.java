package com.powermanager.powermanager.dto.fatura_tributo;

import com.powermanager.powermanager.enums.TipoCalculoTributario;
import com.powermanager.powermanager.enums.TipoTributo;

import java.math.BigDecimal;
import java.util.UUID;

public record FaturaTributoResponseDTO(

        UUID id,
        UUID regraTributariaId,
        TipoTributo tipoTributo,
        TipoCalculoTributario tipoCalculo,
        BigDecimal aliquotaAplicada,
        BigDecimal baseCalculo,
        BigDecimal valorFixoAplicado,
        BigDecimal valorTributo
) {
}
