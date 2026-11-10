package com.powermanager.powermanager.entity;


import com.powermanager.powermanager.enums.AbrangenciaTributaria;
import com.powermanager.powermanager.enums.TipoCalculoTributario;
import com.powermanager.powermanager.enums.TipoTributo;
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
@Table(
        name = "regra_tributaria",
        indexes = {
                @Index(
                        name = "idx_regra_tributaria_busca",
                        columnList = "tipo_tributo, abrangencia, estado, codigo_ibge, inicio_vigencia"
                )
        },
        check = {

                @CheckConstraint(
                        name = "ck_regra_tributaria_aliquota",
                        constraint = "aliquota IS NULL OR aliquota >= 0"
                ),
                @CheckConstraint(
                        name = "ck_regra_tributaria_valor_fixo",
                        constraint = "valor_fixo IS NULL OR valor_fixo >= 0"
                ),
                @CheckConstraint(
                        name = "ck_regra_tributaria_vigencia",
                        constraint = "fim_vigencia IS NULL OR fim_vigencia > inicio_vigencia"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class RegraTributaria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", nullable = false, length = 36)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_tributo", nullable = false, length = 20)
    private TipoTributo tipoTributo;

    @Enumerated(EnumType.STRING)
    @Column(name = "abrangencia", nullable = false, length = 20)
    private AbrangenciaTributaria abrangencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_calculo", nullable = false, length = 20)
    private TipoCalculoTributario tipoCalculo;

    @Column(name = "estado", length = 2)
    private String estado;

    @Column(name = "municipio", length = 120)
    private String municipio;

    @Column(name = "codigo_ibge", length = 7)
    private String codigoIbge;

    @Column(name = "aliquota", precision = 12, scale = 6)
    private BigDecimal aliquota;

    @Column(name = "valor_fixo", precision = 14, scale = 2)
    private BigDecimal valorFixo;

    @Column(name = "inicio_vigencia", nullable = false)
    private LocalDate inicioVigencia;

    @Column(name = "fim_vigencia")
    private LocalDate fimVigencia;
}
