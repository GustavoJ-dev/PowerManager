
package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.FaturaBandeiraAplicada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FaturaBandeiraAplicadaRepo extends JpaRepository<FaturaBandeiraAplicada, UUID> {

    List<FaturaBandeiraAplicada> findByFatura_IdOrderByInicioAplicacaoAsc(UUID faturaId);

    List<FaturaBandeiraAplicada> findByAcionamentoBandeira_Id(UUID acionamentoBandeiraId);
}

