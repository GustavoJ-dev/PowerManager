package com.powermanager.powermanager.dto.unidade_consumidora;

import com.powermanager.powermanager.enums.ClasseConsumo;
import com.powermanager.powermanager.enums.ModalidadeTarifaria;
import com.powermanager.powermanager.enums.SubgrupoTarifario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UnidadeConsumidoraAtualizacaoDTO (

        @NotBlank(message = "O código da unidade consumidora é obrigatório.")
        @Size(max = 30, message = "O código deve ter no máximo 30 caracteres.")
        String codigo,

        @NotBlank(message = "O endereço é obrigatório.")
        @Size(max = 200, message = "O endereço deve ter no máximo 200 caracteres.")
        String endereco,

        @NotBlank(message = "A cidade é obrigatória.")
        @Size(max = 100, message = "A cidade deve ter no máximo 100 caracteres.")
        String cidade,

        // regex de case sensitive, validando apenas duas letras. EX: BA ou ba
        @NotBlank(message = "O estado é obrigatório.")
        @Size(min = 2, max = 2, message = "O estado deve possuir exatamente 2 caracteres.")
        @Pattern(regexp = "(?i)[a-z]{2}", message = "Informe uma sigla de estado válida.")
        String estado,

        @Size(max = 9, message = "O CEP deve ter no máximo 9 caracteres.")
        String cep,

        @NotBlank(message = "A distribuidora é obrigatória.")
        @Size(max = 120, message = "A distribuidora deve ter no máximo 120 caracteres.")
        String distribuidora,

        @NotNull(message = "A classe de consumo é obrigatória.")
        ClasseConsumo classeConsumo,

        @NotNull(message = "O subgrupo tarifário é obrigatório.")
        SubgrupoTarifario subgrupoTarifario,

        @NotNull(message = "A modalidade tarifária é obrigatória.")
        ModalidadeTarifaria modalidadeTarifaria
){
}
