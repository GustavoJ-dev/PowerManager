package com.powermanager.powermanager.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @Column(name = "endereco",nullable = false, length = 200)
    private String endereco;

    @Column(name = "cidade",nullable = false, length = 100)
    private String cidade;

    @Column(name = "estado",nullable = false, length = 50)
    private String estado;

    @Column(name = "email",nullable = false, length = 150)
    private String email;

    @Column(name = "telefone", nullable = false, length = 20)
    private String telefone;

}
