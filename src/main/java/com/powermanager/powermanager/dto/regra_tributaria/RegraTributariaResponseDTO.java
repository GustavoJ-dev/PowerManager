package com.powermanager.powermanager.dto.regra_tributaria;

import com.powermanager.powermanager.enums.AbrangenciaTributaria;
import com.powermanager.powermanager.enums.TipoCalculoTributario;
import com.powermanager.powermanager.enums.TipoTributo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RegraTributariaResponseDTO(

        UUID id,
        TipoTributo tipoTributo,
        AbrangenciaTributaria abrangencia,
        String estado,
        String municipio,
        String codigoIbge,
        TipoCalculoTributario tipoCalculo,
        BigDecimal aliquota,
        BigDecimal valorFixo,
        LocalDate inicioVigencia,
        LocalDate fimVigencia
) {
}
