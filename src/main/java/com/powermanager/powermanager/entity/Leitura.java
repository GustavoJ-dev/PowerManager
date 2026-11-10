package com.powermanager.powermanager.entity;

import com.powermanager.powermanager.enums.TipoLeitura;
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
@Table(
        name = "leitura",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_leitura_medidor_data",
                    columnNames = {"medidor_id", "data_leitura"}
            )
        },

        check = {
            @CheckConstraint(
                    name = "ck_leitura_valor",
                    constraint = "valor_leitura >= 0"
            )
        }
)
@Getter
@Setter
@NoArgsConstructor

public class Leitura {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", nullable = false, length = 36)
    private UUID id;

    @Column(name = "valor_leitura", nullable = false, precision = 12, scale = 3)
    private BigDecimal valorLeitura;

    @Column(name = "data_leitura", nullable = false)
    private LocalDateTime dataLeitura;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_leitura", nullable = false, length = 20)
    private TipoLeitura tipoLeitura;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medidor_id", nullable = false)
    private Medidor medidor;
}
