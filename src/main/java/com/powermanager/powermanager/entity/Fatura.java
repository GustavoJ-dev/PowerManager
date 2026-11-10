package com.powermanager.powermanager.entity;

import com.powermanager.powermanager.enums.OrigemConsumo;
import com.powermanager.powermanager.enums.StatusFatura;
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
        name = "fatura",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_fatura_unidade_competencia",
                        columnNames = {"unidade_consumidora_id", "competencia"}
                )
        },

        check = {
                @CheckConstraint(
                        name = "ck_fatura_consumo",
                        constraint = "consumo_kwh >= 0"
                )
        },
        indexes = {
                @Index(
                        name = "idx_fatura_competencia",
                        columnList = "competencia"
                ),
                @Index(
                        name = "idx_fatura_status",
                        columnList = "status"
                )
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

    // Unidade consumidora responsável pela fatura
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unidade_consumidora_id", nullable = false)
    private UnidadeConsumidora unidadeConsumidora;

    // Competência: sempre o primeiro dia do mês
    @Column(name = "competencia", nullable = false)
    private LocalDate competencia;

    // Período considerado no faturamento
    @Column(name = "inicio_periodo", nullable = false)
    private LocalDate inicioPeriodo;

    @Column(name = "fim_periodo", nullable = false)
    private LocalDate fimPeriodo;

    // Consumo faturado
    @Column(name = "consumo_kwh", nullable = false, precision = 12, scale = 3)
    private BigDecimal consumoKwh;

    @Enumerated(EnumType.STRING)
    @Column(name = "origem_consumo", nullable = false, length = 20)
    private OrigemConsumo origemConsumo;

    // Snapshot das tarifas TE e TUSD
    @Column(name = "tarifa_energia_te", nullable = false, precision = 12, scale = 6)
    private BigDecimal tarifaEnergiaTE;

    @Column(name = "tarifa_uso_distribuicao_tusd", nullable = false, precision = 12, scale = 6)
    private BigDecimal tarifaUsoDistribuicaoTUSD;

    @Column(name = "valor_te", nullable = false, precision = 14, scale = 2)
    private BigDecimal valorTE;

    @Column(name = "valor_tusd", nullable = false, precision = 14, scale = 2)
    private BigDecimal valorTUSD;

    // Snapshot do valor total adicional das bandeiras aplicadas
    @Column(name = "valor_bandeira", nullable = false, precision = 14, scale = 2)
    private BigDecimal valorBandeira;

    // Valores consolidados
    @Column(name = "valor_subtotal", nullable = false, precision = 14, scale = 2)
    private BigDecimal valorSubtotal;

    @Column(name = "valor_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal valorTotal;

    // Datas da fatura
    @Column(name = "data_emissao", nullable = false)
    private LocalDate dataEmissao;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    // Situação da fatura
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusFatura status;
}
