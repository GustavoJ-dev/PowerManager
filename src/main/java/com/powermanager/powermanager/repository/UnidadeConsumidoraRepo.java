package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.UnidadeConsumidora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UnidadeConsumidoraRepo extends JpaRepository<UnidadeConsumidora, UUID> {

    List<UnidadeConsumidora> findByCliente_Id(UUID clienteId);

    Optional<UnidadeConsumidora> findByDistribuidoraAndCodigo(String distribuidora, String codigo);

    boolean existsByDistribuidoraAndCodigo(String distribuidora, String codigo);
}
