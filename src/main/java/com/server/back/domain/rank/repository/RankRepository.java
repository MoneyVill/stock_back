package com.server.back.domain.rank.repository;

import com.server.back.domain.rank.entity.RankEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RankRepository extends JpaRepository<RankEntity,Long> {
    List<RankEntity> findTop10ByOrderByTotalMoneyDesc();

    // Top 3 추가
    List<RankEntity> findTop3ByOrderByTotalMoneyDesc();


    Optional<RankEntity> findByNickname(String nickname);
}
