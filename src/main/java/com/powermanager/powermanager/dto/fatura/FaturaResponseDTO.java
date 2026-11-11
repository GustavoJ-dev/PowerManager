package com.powermanager.powermanager.dto.fatura;

import com.powermanager.powermanager.dto.fatura_tributo.FaturaTributoResponseDTO;
import com.powermanager.powermanager.enums.OrigemConsumo;
import com.powermanager.powermanager.enums.StatusFatura;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record FaturaResponseDTO(

        UUID id,
        UUID unidadeConsumidoraId,
        LocalDate competencia,
        LocalDate inicioPeriodo,
        LocalDate fimPeriodo,
        BigDecimal consumoKwh,
        OrigemConsumo origemConsumo,
        BigDecimal tarifaEnergiaTE,
        BigDecimal tarifaUsoDistribuicaoTUSD,
        BigDecimal valorTE,
        BigDecimal valorTUSD,
        BigDecimal valorBandeira,
        BigDecimal valorSubtotal,
        BigDecimal valorTotal,
        LocalDate dataEmissao,
        LocalDate dataVencimento,
        LocalDate dataPagamento,
        List<FaturaTributoResponseDTO> tributos,
        StatusFatura status
) {
}
