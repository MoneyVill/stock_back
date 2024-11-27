package com.server.back.domain.quiz.repository;

import com.server.back.domain.quiz.entity.QuizEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface QuizRepository extends JpaRepository<QuizEntity, Long> {

    List<QuizEntity> findAllByCreatedAt(LocalDate createAt);

}
