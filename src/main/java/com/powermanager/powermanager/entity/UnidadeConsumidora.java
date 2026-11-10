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

import java.util.UUID;


@Entity
@Table(
        name = "unidade_consumidora",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_unidade_distribuidora_codigo",
                        columnNames = {"distribuidora", "codigo"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_unidade_cliente",
                        columnList = "cliente_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class UnidadeConsumidora {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", nullable = false, length = 36)
    private UUID id;

    @Column(name = "codigo", nullable = false, length = 30)
    private String codigo;

    @Column(name = "endereco", nullable = false, length = 200)
    private String endereco;

    @Column(name = "cidade", nullable = false, length = 100)
    private String cidade;

    @Column(name = "estado", nullable = false, length = 2)
    private String estado;

    @Column(name = "cep", length = 9)
    private String cep;

    @Column(name = "distribuidora", nullable = false, length = 120)
    private String distribuidora;

    @Enumerated(EnumType.STRING)
    @Column(name = "classe_consumo", nullable = false, length = 30)
    private ClasseConsumo classeConsumo;

    @Enumerated(EnumType.STRING)
    @Column(name = "subgrupo_tarifario", nullable = false, length = 10)
    private SubgrupoTarifario subgrupoTarifario;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidade_tarifaria", nullable = false, length = 20)
    private ModalidadeTarifaria modalidadeTarifaria;

    @Column(name = "ativa", nullable = false)
    private Boolean ativa = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
}
