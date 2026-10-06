package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.Fatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FaturaRepo extends JpaRepository<Fatura, UUID> {
}
