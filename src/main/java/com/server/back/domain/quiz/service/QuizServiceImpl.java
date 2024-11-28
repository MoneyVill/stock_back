package com.server.back.domain.quiz.service;

import com.server.back.common.entity.DealEntity;
import com.server.back.common.repository.DealRepository;
import com.server.back.common.service.AuthService;
import com.server.back.domain.quiz.dto.QuizResDto;
import com.server.back.domain.user.entity.UserEntity;
import com.server.back.domain.user.repository.UserRepository;
import com.server.back.exception.CustomException;
import com.server.back.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.server.back.common.code.commonCode.DealType;

import javax.transaction.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService{

    private final UserRepository userRepository;
    private final DealRepository dealRepository;
    private final AuthService authService;

    @Transactional
    @Override
    public QuizResDto submitAnswer(QuizResDto quizResDto) {

        log.info("Received isCorrect: {}, prizeMoney: {}", quizResDto.isCorrect(), quizResDto.getPrizeMoney());

        Long userId = authService.getUserId();
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        boolean isCorrect = quizResDto.isCorrect();
        Long prizeMoney = quizResDto.getPrizeMoney();

        if (isCorrect) {
            log.info("Correct answer logic executed with prizeMoney: {}", prizeMoney);
            //상금 지급
            user.increaseCurrentMoney(prizeMoney);
            DealEntity deal = new DealEntity(user, DealType.GET_MONEY_FOR_QUIZ, prizeMoney);
            dealRepository.save(deal);
            return new QuizResDto(true, prizeMoney);
        } else {
            log.info("Incorrect answer logic executed.");
            //오답 처리
            return new QuizResDto(false, 0L);
        }
    }
}
