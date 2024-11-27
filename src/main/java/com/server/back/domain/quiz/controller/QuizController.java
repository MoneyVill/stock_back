package com.server.back.domain.quiz.controller;

import com.server.back.domain.quiz.service.QuizService;
import com.server.back.domain.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @GetMapping
    public ResponseEntity<List<QuizListResponseDto>> getQuizList(@AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(quizService.getQuizList(user));
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<QuizDetailResponseDto> getQuizDetail(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.getQuizDetail(quizId));
    }

    @PostMapping("/{quizId}/answer")
    public ResponseEntity<Void> submitAnswer(
            @AuthenticationPricipal UserEntity user,
            @PathVariable Long quizId,
            @ReqestBody String answer
    ) {
        quizService.submitAnswer(user, quizId, answer);
        return ResponseEntity.ok().build();
    }
}
