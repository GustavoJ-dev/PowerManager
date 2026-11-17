package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.Leitura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeituraRepo extends JpaRepository<Leitura, UUID> {

    List<Leitura> findByMedidor_IdOrderByDataLeituraAsc(UUID medidorId);

    List<Leitura> findByMedidor_IdOrderByDataLeituraDesc(UUID medidorId);

    Optional<Leitura> findTopByMedidor_IdOrderByDataLeituraDesc(UUID medidorId);

    Optional<Leitura> findTopByMedidor_IdAndDataLeituraBeforeOrderByDataLeituraDesc(
            UUID medidorId, LocalDateTime dataLeitura);

    Optional<Leitura> findTopByMedidor_IdAndDataLeituraAfterOrderByDataLeituraAsc(
            UUID medidorId, LocalDateTime dataLeitura);

    boolean existsByMedidor_IdAndDataLeitura(UUID medidorId, LocalDateTime dataLeitura);
}