package com.server.back.domain.loan.repository;

import com.server.back.domain.loan.entity.LoanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface LoanRepository extends JpaRepository<LoanEntity, Long> {
    List<LoanEntity> findAllByUserId(Long userId);
    @Query("SELECT SUM(l.amount) FROM LoanEntity l WHERE l.userId = :userId")
    Optional<Integer> getTotalLoanAmountByUserId(@Param("userId") Long userId);
}
