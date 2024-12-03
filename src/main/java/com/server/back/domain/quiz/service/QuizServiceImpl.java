package com.server.back.domain.quiz.service;

import com.server.back.common.entity.DealEntity;
import com.server.back.common.repository.DealRepository;
import com.server.back.common.service.AuthService;
import com.server.back.domain.quiz.dto.QuizResDto;
import com.server.back.domain.tax.service.TaxService;
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
    private final TaxService taxService; // TaxService 추가

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

            // 세금 계산 및 차감
            Long taxAmount = Math.round(prizeMoney * 0.1); // 상금의 10% 세금
            user.decreaseCurrentMoney(taxAmount);

            DealEntity deal = new DealEntity(user, DealType.GET_MONEY_FOR_QUIZ, prizeMoney);
            dealRepository.save(deal);

            // 세금 내역 저장
            taxService.saveTax(user.getNickname(), 0L, taxAmount, 0L);

            log.info("Quiz Tax Applied: User: {}, Prize: {}, Tax: {}", user.getNickname(), prizeMoney, taxAmount);

            return QuizResDto.builder()
                    .isCorrect(true)
                    .prizeMoney(prizeMoney - taxAmount) // 세금 차감된 상금
                    .taxAmount(taxAmount) // 세금 금액 추가
                    .build();
        } else {
            log.info("Incorrect answer logic executed.");
            //오답 처리
            return QuizResDto.builder()
                    .isCorrect(false)
                    .prizeMoney(0L)
                    .taxAmount(0L) // 틀린 경우 세금 0
                    .build();
        }
    }
}
