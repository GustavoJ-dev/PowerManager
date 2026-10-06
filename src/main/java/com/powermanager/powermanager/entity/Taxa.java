package com.powermanager.powermanager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "taxa", check = {
        @CheckConstraint(name = "ck_taxa_custo", constraint = "custo_por_unidade >= 0"),
        @CheckConstraint(name = "ck_taxa_aluguel", constraint = "aluguel_medidor >= 0"),
        @CheckConstraint(name = "ck_taxa_servico", constraint = "taxa_servico >= 0"),
        @CheckConstraint(name = "ck_taxa_imposto", constraint = "imposto_servico >= 0"),
        @CheckConstraint(name = "ck_taxa_cess", constraint = "cess >= 0"),
        @CheckConstraint(name = "ck_taxa_fixa", constraint = "taxa_fixa >= 0")
})
@Getter
@Setter
@NoArgsConstructor
public class Taxa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", nullable = false, length = 36)
    private UUID id;

    @Column(name = "custo_por_unidade", nullable = false, precision = 10, scale = 2)
    private BigDecimal custoPorUnidade;

    @Column(name = "aluguel_medidor", nullable = false, precision = 10, scale = 2)
    private BigDecimal aluguelMedidor;

    @Column(name = "taxa_servico", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxaServico;

    @Column(name = "imposto_servico", nullable = false, precision = 10, scale = 2)
    private BigDecimal impostoServico;

    @Column(name = "cess", nullable = false, precision = 10, scale = 2)
    private BigDecimal cess;

    @Column(name = "taxa_fixa", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxaFixa;

    @Column(name = "inicio_vigencia", nullable = false)
    private LocalDateTime inicioVigencia;

    @Column(name = "fim_vigencia")
    private LocalDateTime fimVigencia;

}
