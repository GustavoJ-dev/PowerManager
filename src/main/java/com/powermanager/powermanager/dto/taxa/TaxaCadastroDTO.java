package com.powermanager.powermanager.dto.taxa;

import com.powermanager.powermanager.enums.ClasseConsumo;
import com.powermanager.powermanager.enums.ModalidadeTarifaria;
import com.powermanager.powermanager.enums.SubgrupoTarifario;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TaxaCadastroDTO(

        @NotBlank(message = "A distribuidora é obrigatória.")
        @Size(max = 120, message = "A distribuidora deve ter no máximo 120 caracteres.")
        String distribuidora,

        @NotNull(message = "O subgrupo tarifário é obrigatório.")
        SubgrupoTarifario subgrupoTarifario,

        @NotNull(message = "A classe de consumo é obrigatória.")
        ClasseConsumo classeConsumo,

        @NotNull(message = "A modalidade tarifária é obrigatória.")
        ModalidadeTarifaria modalidadeTarifaria,

        @NotNull(message = "A tarifa de energia é obrigatória.")
        @DecimalMin(value = "0.0", message = "A TE não pode ser negativa.")
        @Digits(integer = 6, fraction = 6,
                message = "A TE deve possuir até 6 dígitos inteiros e 6 decimais.")
        BigDecimal tarifaEnergia,

        @NotNull(message = "A tarifa de uso da distribuição é obrigatória.")
        @DecimalMin(value = "0.0", message = "A TUSD não pode ser negativa.")
        @Digits(integer = 6, fraction = 6,
                message = "A TUSD deve possuir até 6 dígitos inteiros e 6 decimais.")
        BigDecimal tarifaUsoDistribuicao,

        @NotNull(message = "O início da vigência é obrigatório.")
        LocalDate inicioVigencia,

        LocalDate fimVigencia
) {
}
