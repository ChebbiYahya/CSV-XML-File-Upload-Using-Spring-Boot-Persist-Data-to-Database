package com.bank.uploadfileanddatapersistdb.repository;

import com.bank.uploadfileanddatapersistdb.entity.LogChargement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogChargementRepository extends JpaRepository<LogChargement, Long> {
}
