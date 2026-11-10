package com.powermanager.powermanager.entity;


import com.powermanager.powermanager.enums.TipoBandeira;
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
        name = "bandeira_tarifaria",
        indexes = {
                @Index(
                        name = "idx_bandeira_tipo_vigencia",
                        columnList = "tipo_bandeira, inicio_vigencia"
                )
        },
        check = {
                @CheckConstraint(
                        name = "ck_bandeira_valor",
                        constraint = "valor_adicional >= 0"
                ),
                @CheckConstraint(
                        name = "ck_bandeira_vigencia",
                        constraint = "fim_vigencia IS NULL OR fim_vigencia > inicio_vigencia"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class BandeiraTarifaria {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", nullable = false, length = 36)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_bandeira", nullable = false, length = 30)
    private TipoBandeira tipoBandeira;

    @Column(name = "valor_adicional", nullable = false, precision = 12, scale = 6)
    private BigDecimal valorAdicional;

    @Column(name = "inicio_vigencia", nullable = false)
    private LocalDate inicioVigencia;

    @Column(name = "fim_vigencia")
    private LocalDate fimVigencia;
}
