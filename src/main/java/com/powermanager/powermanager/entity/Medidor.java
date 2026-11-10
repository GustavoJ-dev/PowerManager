package com.powermanager.powermanager.entity;

import com.powermanager.powermanager.enums.TipoLigacao;
import com.powermanager.powermanager.enums.TipoMedidor;
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
        name = "medidor",
        indexes = {
                @Index(
                        name = "idx_medidor_unidade_ativo",
                        columnList = "unidade_consumidora_id, ativo"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Medidor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", nullable = false, length = 36)
    private UUID id;

    @Column(name = "numero", nullable = false, length = 30, unique = true)
    private String numero;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_medidor", nullable = false)
    private TipoMedidor tipoMedidor;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ligacao", nullable = false, length = 10)
    private TipoLigacao tipoLigacao;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "data_instalacao", nullable = false)
    private LocalDate dataInstalacao;

    @Column(name = "data_retirada")
    private LocalDate dataRetirada;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unidade_consumidora_id", nullable = false)
    private UnidadeConsumidora unidadeConsumidora;
}
