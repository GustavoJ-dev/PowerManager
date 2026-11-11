package com.powermanager.powermanager.dto.regra_tributaria;

import com.powermanager.powermanager.enums.AbrangenciaTributaria;
import com.powermanager.powermanager.enums.TipoCalculoTributario;
import com.powermanager.powermanager.enums.TipoTributo;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegraTributariaCadastroDTO (

        @NotNull
        TipoTributo tipoTributo,

        @NotNull
        AbrangenciaTributaria abrangencia,

        // regex permitindo apenas duas referenciar o Estado. EX: BA ou ba
        @Size(min = 2, max = 2)
        @Pattern(regexp = "(?i)[a-z]{2}")
        String estado,

        @Size(max = 100)
        String municipio,

        // garante 7 dígitos numéricos.
        @Pattern(regexp = "\\d{7}")
        String codigoIbge,

        @NotNull
        TipoCalculoTributario tipoCalculo,

        @DecimalMin("0.0")
        @Digits(integer = 6, fraction = 6)
        BigDecimal aliquota,

        @DecimalMin("0.0")
        @Digits(integer = 12, fraction = 2)
        BigDecimal valorFixo,

        @NotNull
        LocalDate inicioVigencia,

        LocalDate fimVigencia
){
}
