package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.Fatura;
import com.powermanager.powermanager.enums.StatusFatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FaturaRepo extends JpaRepository<Fatura, UUID> {

    List<Fatura> findByUnidadeConsumidora_Id(UUID unidadeConsumidoraId);

    List<Fatura> findByUnidadeConsumidora_IdOrderByCompetenciaDesc(UUID unidadeConsumidoraId);

    Optional<Fatura> findByUnidadeConsumidora_IdAndCompetencia(UUID unidadeConsumidoraId, LocalDate competencia);

    List<Fatura> findByStatus(StatusFatura status);
}