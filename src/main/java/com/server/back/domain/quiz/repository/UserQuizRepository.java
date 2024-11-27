package com.server.back.domain.quiz.repository;

import com.server.back.domain.quiz.entity.QuizEntity;
import com.server.back.domain.quiz.entity.UserQuizEntity;
import com.server.back.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserQuizRepository extends JpaRepository<UserQuizEntity, Long> {

    List<UserQuizEntity> findAllByUser(UserEntity user);
    Optional<UserQuizEntity> findByUserAndQuiz(UserEntity user, QuizEntity quiz);
}
