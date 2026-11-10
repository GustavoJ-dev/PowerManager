package com.powermanager.powermanager.entity;

import com.powermanager.powermanager.enums.TipoCalculoTributario;
import com.powermanager.powermanager.enums.TipoTributo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "fatura_tributo",
        indexes = {
                @Index(
                        name = "idx_fatura_tributo_fatura",
                        columnList = "fatura_id"
                ),
                @Index(
                        name = "idx_fatura_tributo_tipo",
                        columnList = "tipo_tributo"
                ),
                @Index(
                        name = "idx_fatura_tributo_regra",
                        columnList = "regra_tributaria_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class FaturaTributo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", nullable = false, length = 36)
    private UUID id;

    // Fatura à qual o tributo pertence
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fatura_id", nullable = false)
    private Fatura fatura;

    // Regra tributária utilizada no cálculo
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "regra_tributaria_id", nullable = false)
    private RegraTributaria regraTributaria;

    // Snapshot do tipo de tributo
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_tributo", nullable = false, length = 20)
    private TipoTributo tipoTributo;

    // Snapshot do tipo de cálculo
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_calculo", nullable = false, length = 20)
    private TipoCalculoTributario tipoCalculo;

    // Snapshot da alíquota utilizada
    @Column(name = "aliquota_aplicada", precision = 12, scale = 6)
    private BigDecimal aliquotaAplicada;

    // Snapshot da base de cálculo
    @Column(name = "base_calculo", precision = 14, scale = 2)
    private BigDecimal baseCalculo;

    // Snapshot do valor fixo, quando aplicável
    @Column(name = "valor_fixo_aplicado", precision = 14, scale = 2)
    private BigDecimal valorFixoAplicado;

    // Valor efetivamente cobrado na fatura
    @Column(name = "valor_tributo", nullable = false, precision = 14, scale = 2)
    private BigDecimal valorTributo;
}
