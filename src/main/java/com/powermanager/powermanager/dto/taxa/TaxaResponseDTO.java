package com.powermanager.powermanager.dto.taxa;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TaxaResponseDTO(UUID id,
                              BigDecimal custoPorUnidade,
                              BigDecimal aluguelMedidor,
                              BigDecimal taxaServico,
                              BigDecimal impostoServico,
                              BigDecimal cess,
                              BigDecimal taxaFixa,
                              LocalDateTime inicioVigencia,
                              LocalDateTime fimVigencia) {
}
