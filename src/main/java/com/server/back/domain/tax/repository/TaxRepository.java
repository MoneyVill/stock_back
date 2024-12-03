package com.server.back.domain.tax.repository;

import com.server.back.domain.tax.entity.TaxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaxRepository extends JpaRepository<TaxEntity, Long> {

    List<TaxEntity> findTop3ByOrderByCreatedAtDesc();

}
