
package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.FaturaTributo;
import com.powermanager.powermanager.enums.TipoTributo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FaturaTributoRepo extends JpaRepository<FaturaTributo, UUID> {

    List<FaturaTributo> findByFatura_Id(UUID faturaId);

    List<FaturaTributo> findByFatura_IdAndTipoTributo(UUID faturaId, TipoTributo tipoTributo);

    List<FaturaTributo> findByRegraTributaria_Id(UUID regraTributariaId);
}

