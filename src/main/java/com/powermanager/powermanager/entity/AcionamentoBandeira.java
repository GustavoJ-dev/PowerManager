package com.powermanager.powermanager.entity;


import com.powermanager.powermanager.enums.TipoBandeira;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "acionamento_bandeira",
        indexes = {
                @Index(
                        name = "idx_acionamento_bandeira_vigencia",
                        columnList = "inicio_vigencia, fim_vigencia"
                )
        },
        check = {
                @CheckConstraint(
                        name = "ck_acionamento_bandeira_vigencia",
                        constraint = "fim_vigencia IS NULL OR fim_vigencia > inicio_vigencia"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class AcionamentoBandeira {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", nullable = false, length = 36)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_bandeira", nullable = false, length = 30)
    private TipoBandeira tipoBandeira;

    @Column(name = "inicio_vigencia", nullable = false)
    private LocalDate inicioVigencia;

    @Column(name = "fim_vigencia")
    private LocalDate fimVigencia;

    @Column(name = "data_divulgacao")
    private LocalDate dataDivulgacao;

    @Column(name = "referencia_oficial", length = 500)
    private String referenciaOficial;
}
