package com.powermanager.powermanager.entity;

import com.powermanager.powermanager.enums.ClasseConsumo;
import com.powermanager.powermanager.enums.ModalidadeTarifaria;
import com.powermanager.powermanager.enums.SubgrupoTarifario;
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
        name = "taxa",
       indexes = {
            @Index(
                    name = "idx_taxa_busca_vigencia",
                    columnList = "distribuidora, subgrupo_tarifario, classe_consumo, " +
                            "modalidade_tarifaria, inicio_vigencia"
            )
       },
       check = {
        @CheckConstraint(
                name = "ck_taxa_te",
                constraint = "tarifa_energia >=0"
        ),
        @CheckConstraint(
                name = "ck_taxa_tusd",
                constraint = "tarifa_uso_distribuicao >= 0"
        ),
        @CheckConstraint(
                name = "ck_taxa_vigencia",
                constraint = "fim_vigencia IS NULL OR fim_vigencia > inicio_vigencia"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class Taxa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", nullable = false, length = 36)
    private UUID id;

    @Column(name = "distribuidora", nullable = false, length = 120)
    private String distribuidora;

    @Enumerated(EnumType.STRING)
    @Column(name = "subgrupo_tarifario", nullable = false, length = 10)
    private SubgrupoTarifario subgrupoTarifario;

    @Enumerated(EnumType.STRING)
    @Column(name = "classe_consumo", nullable = false, length = 30)
    private ClasseConsumo classeConsumo;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidade_tarifaria", nullable = false, length = 20)
    private ModalidadeTarifaria modalidadeTarifaria;

    @Column(name = "tarifa_energia", nullable = false, precision = 12, scale = 6)
    private BigDecimal tarifaEnergia;

    @Column(name = "tarifa_uso_distribuicao", nullable = false, precision = 12, scale = 6)
    private BigDecimal tarifaUsoDistribuicao;

    @Column(name = "inicio_vigencia", nullable = false)
    private LocalDate inicioVigencia;

    @Column(name = "fim_vigencia")
    private LocalDate fimVigencia;


}
