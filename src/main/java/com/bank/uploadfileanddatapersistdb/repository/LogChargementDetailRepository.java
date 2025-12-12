package com.bank.uploadfileanddatapersistdb.repository;

import com.bank.uploadfileanddatapersistdb.entity.LogChargementDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogChargementDetailRepository extends JpaRepository<LogChargementDetail, Long> {
}
