package com.powermanager.powermanager.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id",nullable = false, length = 36)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(name = "nome",nullable = false, length = 100)
    private String nome;

    @Column(name = "email",nullable = false, length = 150)
    private String email;

    @Column(name = "telefone", nullable = false, length = 20)
    private String telefone;

    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    private List<UnidadeConsumidora> unidadeConsumidoras = new ArrayList<>();

}
