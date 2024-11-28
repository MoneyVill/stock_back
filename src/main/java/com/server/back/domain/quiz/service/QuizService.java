package com.server.back.domain.quiz.service;

import com.server.back.domain.quiz.dto.QuizResDto;

public interface QuizService {
    QuizResDto submitAnswer(QuizResDto quizResDto);
}
