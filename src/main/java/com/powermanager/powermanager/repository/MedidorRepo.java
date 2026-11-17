package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.Medidor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedidorRepo extends JpaRepository<Medidor, UUID> {

    Optional<Medidor> findByNumero(String numero);

    List<Medidor> findByUnidadeConsumidora_Id(UUID unidadeConsumidoraId);

    Optional<Medidor> findByUnidadeConsumidora_IdAndAtivoTrue(UUID unidadeConsumidoraId);
}