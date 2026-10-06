package com.powermanager.powermanager.entity;

import com.powermanager.powermanager.entity.enums.TipoUsuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "usuario", uniqueConstraints = {
                            @UniqueConstraint(name = "uk_usuario_username",
                                            columnNames = {"username"})},
                         check = {
                            @CheckConstraint(name = "ck_usuario_tipo",
                                    constraint = "tipo IN ('ADMINISTRADOR', 'CLIENTE')")})
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", nullable = false, length = 36)
    private UUID id;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private TipoUsuario tipoUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
}
