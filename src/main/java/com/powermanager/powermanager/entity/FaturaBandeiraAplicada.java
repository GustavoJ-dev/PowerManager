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
        name = "fatura_bandeira_aplicada",
        indexes = {
                @Index(
                        name = "idx_fatura_bandeira_fatura",
                        columnList = "fatura_id"
                ),
                @Index(
                        name = "idx_fatura_bandeira_acionamento",
                        columnList = "acionamento_bandeira_id"
                )
        },
        check = {
                @CheckConstraint(
                        name = "ck_fatura_bandeira_valor_kwh",
                        constraint = "valor_adicional_kwh >= 0"
                ),
                @CheckConstraint(
                        name = "ck_fatura_bandeira_consumo",
                        constraint = "consumo_considerado_kwh >= 0"
                ),
                @CheckConstraint(
                        name = "ck_fatura_bandeira_valor",
                        constraint = "valor_adicional >= 0"
                ),
                @CheckConstraint(
                        name = "ck_fatura_bandeira_periodo",
                        constraint = "fim_aplicacao >= inicio_aplicacao"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class FaturaBandeiraAplicada {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", nullable = false, length = 36)
    private UUID id;

    // Fatura à qual este registro pertence
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fatura_id", nullable = false)
    private Fatura fatura;

    // Registro oficial do acionamento
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "acionamento_bandeira_id", nullable = false)
    private AcionamentoBandeira acionamentoBandeira;

    // Snapshot da bandeira aplicada
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_bandeira", nullable = false, length = 30)
    private TipoBandeira tipoBandeira;

    // Período em que a bandeira foi considerada no ciclo
    @Column(name = "inicio_aplicacao", nullable = false)
    private LocalDate inicioAplicacao;

    @Column(name = "fim_aplicacao", nullable = false)
    private LocalDate fimAplicacao;

    // Snapshot do adicional oficial por kWh
    @Column(name = "valor_adicional_kwh", nullable = false, precision = 12, scale = 6)
    private BigDecimal valorAdicionalKwh;

    // Consumo atribuído a este período
    @Column(name = "consumo_considerado_kwh", nullable = false, precision = 12, scale = 3)
    private BigDecimal consumoConsideradoKwh;

    // Valor adicional calculado para este período
    @Column(name = "valor_adicional", nullable = false, precision = 14, scale = 2)
    private BigDecimal valorAdicional;
}
