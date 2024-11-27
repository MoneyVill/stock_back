package com.server.back.domain.quiz.service;

import com.server.back.domain.quiz.dto.QuizDetailResponseDto;
import com.server.back.domain.quiz.entity.QuizEntity;
import com.server.back.domain.quiz.repository.QuizRepository;
import com.server.back.domain.quiz.repository.UserQuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizServiceImpl implements QuizService{

    private final QuizRepository quizRepository;
    private final UserQuizRepository userQuizRepository;

    @Override
    @Scheduled(cron = "0 0 0 * * ?")
    public void generateDailyQuizzes() {
        for (int i = 0; i < 5; i++) {
            QuizEntity quiz = QuizEntity.builder()
                    .question("문제" + i)
                    .answer("정답" + i)
                    .reward(100)
                    .createdAt(LocalDate.now())
                    .build();
            quizRepository.save(quiz);
        }
    }

    @Override
    public List<QuizListResponseDto> getQuizList(UserEntity user) {
        List<QuizEntity> quizzes = quizRepository.findAllByCreatedAt(LocalDate.now());
        List<UserQuizEntity> userQuizzes = userQuizRepository.findAllByUser(user);

        return quizzes.stream()
                .map(quiz -> {
                    boolean isSolved = userQuizzes.stream()
                            .anyMatch(userQuiz -> user.Quiz.getQuiz().getId().equals(quiz.getId()) && userQuiz.isSolved());
                    return QuizListResponseDto.builder()
                            .id(quiz.getId())
                            .question(quiz.getQuestion())
                            .isSolved(isSolved)
                            .build();

                }).collect(Collectors.toList());
    }

    //문제 상세 조회
    @Override
    public QuizDetailResponseDto getQuizDetail(Long quizId) {
        QuizEntity quiz = quizRepository.findByid(quizId)
                .orElseThrow(() -> new RuntimeException("문제를 찾을 수 없습니다"));
        return QuizDetailResponseDto.builder()
                .id(quiz.getId())
                .question(quiz.getQuestion())
                .reward(quiz.getReward())
                .build();
    }

    //답 제출 처리
    @Override
    public void submitAnswer(UserEntity user, Long quizId, String answer) {
        QuizEntity quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("문제를 찾을 수 없습니다."));

        UserQuizEntity userQuiz = userQuizRepository.findByUserAndQuiz(user, quiz)
                .orElse(UserQuizEntity.builder()
                        .user(user)
                        .quiz(quiz)
                        .isSolved(false)
                        .build()
                );

        if (userQuiz.isSolved()) {
            throw new RuntimeException("이미 푼 문제입니다.");
        }

        if (!quiz.getAnswer().equals(answer)) {
            throw new RuntimeException("정답이 아닙니다.");
        }

        userQuiz.setSolved(true);
        userQuiz.setSolvedAt(LocalDate.now());
        userQuizRepository.save(userQuiz);

        //유저 자산 증가 처리(UserEntity 업데이트)
        user.addAsset(quiz.getReward());
    }
}
