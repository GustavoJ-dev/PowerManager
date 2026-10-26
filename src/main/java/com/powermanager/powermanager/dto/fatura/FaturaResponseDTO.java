package com.powermanager.powermanager.dto.fatura;

import com.powermanager.powermanager.entity.enums.StatusFatura;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FaturaResponseDTO(UUID id,
                                LocalDate competencia,
                                BigDecimal unidadesConsumidas,
                                BigDecimal custoPorUnidadeAplicado,
                                BigDecimal aluguelMedidorAplicado,
                                BigDecimal taxaServicoAplicada,
                                BigDecimal impostoServicoAplicado,
                                BigDecimal cessAplicado,
                                BigDecimal taxaFixaAplicada,
                                BigDecimal valorTotal,
                                StatusFatura statusFatura,
                                UUID medidorId) {
}
