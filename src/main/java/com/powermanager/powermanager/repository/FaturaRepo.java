package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.Fatura;
import com.powermanager.powermanager.entity.Medidor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FaturaRepo extends JpaRepository<Fatura, UUID> {

    List<Fatura> findByMedidor(Medidor medidor);
    List<Fatura> findByMedidorOrderByCompetenciaDesc(Medidor medidor);

    Optional<Fatura> findByMedidorAndCompetencia(Medidor medidor, LocalDate competencia);
}
