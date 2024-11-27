package com.server.back.domain.quiz.service;

import com.server.back.domain.quiz.dto.QuizDetailResponseDto;
import com.server.back.domain.quiz.dto.QuizListResponseDto;
import com.server.back.domain.user.entity.UserEntity;

import java.util.List;

public interface QuizService {

    void generateDailyQuizzes();
    List<QuizListResponseDto> getQuizList(UserEntity user);


    QuizDetailResponseDto getQuizDetail(Long quizId);
    void submitAnswer(UserEntity user, Long quizId, String answer);
}
