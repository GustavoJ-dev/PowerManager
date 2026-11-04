package com.powermanager.powermanager.entity;


import com.powermanager.powermanager.entity.enums.ClasseConsumo;
import com.powermanager.powermanager.entity.enums.TipoLigacao;
import com.powermanager.powermanager.entity.enums.TipoMedidor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(check = {
        @CheckConstraint(name = "ck_medidor_dias", constraint = "dias > 0")
})
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

    @Column(name = "localizacao", nullable = false, length = 30)
    private String localizacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_medidor", nullable = false)
    private TipoMedidor tipoMedidor;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ligacao", nullable = false,  length = 10)
    private TipoLigacao tipoLigacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "classe_consumo", nullable = false, length = 30)
    private ClasseConsumo classeConsumo;

    //Quantidade de dias considerada para o período de faturamento do medidor.
    @Column(name = "dias", nullable = false)
    private Integer dias;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
}
