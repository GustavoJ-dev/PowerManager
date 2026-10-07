package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepo extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByUsername(String username);
}
