package com.powermanager.powermanager.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table
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

    @Column(name = "tipo", nullable = false, length = 30)
    private String tipo;

    @Column(name = "codigo_fase", nullable = false,  length = 10)
    private String codigoFase;

    @Column(name = "tipo_fatura", nullable = false, length = 30)
    private String tipoFatura;

    //Quantidade de dias considerada para o período de faturamento do medidor.
    @Column(name = "dias", nullable = false)
    private Integer dias;

    @OneToOne
    @JoinColumn(name = "cliente_id", nullable = false, unique = true)
    private Cliente cliente;
}
