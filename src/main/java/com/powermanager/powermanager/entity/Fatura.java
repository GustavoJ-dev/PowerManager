package com.powermanager.powermanager.entity;

import com.powermanager.powermanager.entity.enums.StatusFatura;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "fatura",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_fatura_medidor_competencia",
                            columnNames = {"medidor_id", "competencia"}
             )
        },
        check = {
            @CheckConstraint(name = "ck_fatura_unidades", constraint = "unidades_consumidas >= 0"),
            @CheckConstraint(name = "ck_fatura_valor", constraint = "valor_total >= 0"),
            @CheckConstraint(name = "ck_fatura_status", constraint = "status IN('PENDENTE', 'PAGO')"),
            @CheckConstraint(name = "ck_fatura_aluguel", constraint = "aluguel_medidor_aplicado >= 0"),
            @CheckConstraint(name = "ck_fatura_cess", constraint = "cess_aplicado >= 0"),
            @CheckConstraint(name = "ck_fatura_custo_unidade", constraint = "custo_por_unidade_aplicado >= 0"),
            @CheckConstraint(name = "ck_fatura_imposto_servico", constraint = "imposto_servico_aplicado >= 0"),
            @CheckConstraint(name = "ck_fatura_taxa_fixa", constraint = "taxa_fixa_aplicada >= 0"),
            @CheckConstraint(name = "ck_fatura_taxa_servico",constraint = "taxa_servico_aplicada >= 0"),

        }
)
@Getter
@Setter
@NoArgsConstructor
public class Fatura {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", nullable = false, length = 36)
    private UUID id;

    @Column(name = "competencia", nullable = false)
    private LocalDate competencia;

    @Column(name = "unidades_consumidas", nullable = false, precision = 10, scale = 2)
    private BigDecimal unidadesConsumidas;

    @Column(name = "custo_por_unidade_aplicado", nullable = false, precision = 10, scale = 2)
    private BigDecimal custoPorUnidadeAplicado;

    @Column(name = "aluguel_medidor_aplicado", nullable = false, precision = 10, scale = 2)
    private BigDecimal aluguelMedidorAplicado;

    @Column(name = "taxa_servico_aplicada", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxaServicoAplicada;

    @Column(name = "imposto_servico_aplicado", nullable = false, precision = 10, scale = 2)
    private BigDecimal impostoServicoAplicado;

    @Column(name = "cess_aplicado", nullable = false, precision = 10, scale = 2)
    private BigDecimal cessAplicado;

    @Column(name = "taxa_fixa_aplicada", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxaFixaAplicada;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private StatusFatura statusFatura;

    @ManyToOne
    @JoinColumn(name = "medidor_id", nullable = false)
    private Medidor medidor;
}
