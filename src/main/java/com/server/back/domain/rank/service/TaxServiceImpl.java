package com.server.back.domain.rank.service;

import com.server.back.common.code.commonCode.IsDeleted;
import com.server.back.domain.rank.dto.TaxDetailsDto;
import com.server.back.domain.rank.entity.RankEntity;
import com.server.back.domain.rank.entity.TaxEntity;
import com.server.back.domain.rank.repository.RankRepository;
import com.server.back.domain.rank.repository.TaxRepository;
import com.server.back.domain.user.entity.UserEntity;
import com.server.back.domain.user.repository.UserRepository;
import com.server.back.exception.CustomException;
import com.server.back.exception.ErrorCode;
import com.server.back.exception.TaxWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaxServiceImpl implements TaxService {
    private final TaxRepository taxRepository;
    private final UserRepository userRepository;
    private final RankRepository rankRepository;
    private final TaxWebSocketHandler taxWebSocketHandler; // WebSocket 핸들러 추가

    @Override
    public void saveTax(String nickname, Long taxAmount) {
        UserEntity user = userRepository.findByNicknameAndIsDeleted(nickname, IsDeleted.N).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        TaxEntity taxEntity = TaxEntity.builder()
                .user(user)
                .taxAmount(taxAmount)
                .createdAt(LocalDateTime.now())
                .build();

        taxRepository.save(taxEntity);
        log.info("[TaxService] 세금 저장 - 사용자: {}, 세금 금액: {}", nickname, taxAmount);
    }

    @Override
    public List<TaxDetailsDto> getRecentTaxDetails() {
        return taxRepository.findTop3ByOrderByCreatedAtDesc()
                .stream()
                .map(tax -> TaxDetailsDto.builder()
                        .nickname(tax.getUser().getNickname())
                        .taxAmount(tax.getTaxAmount())
                        .createdAt(tax.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                        .build())
                .collect(Collectors.toList());
    }


    public void applyTaxToTopUsers() {
        log.info("[TaxService] 세금 부과 프로세스 시작");
        List<RankEntity> rankings = rankRepository.findTop3ByOrderByTotalMoneyDesc();
        if (rankings.isEmpty()) {
            log.warn("[TaxService] 세금을 부과할 상위 랭킹이 없습니다.");
            return;
        }

        double[] taxRates = {0.0015, 0.0010, 0.0005};
        List<TaxDetailsDto> taxDetailsList = new ArrayList<>();

        for (int i = 0; i < rankings.size(); i++) {
            RankEntity rank = rankings.get(i);
            UserEntity user = userRepository.findByNicknameAndIsDeleted(rank.getNickname(), IsDeleted.N)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            log.info("[TaxService] {}위 사용자: {}, 현재 자산: {}", i + 1, user.getNickname(), user.getCurrentMoney());

            Long currentMoney = user.getCurrentMoney();
            if (currentMoney > 0) {
                Long taxAmount = Math.round(currentMoney * taxRates[i]);
                user.decreaseCurrentMoney(taxAmount);
                userRepository.save(user);

                saveTax(user.getNickname(), taxAmount); // 세금 정보 저장

                TaxDetailsDto taxDetail = TaxDetailsDto.builder()
                        .nickname(user.getNickname())
                        .taxAmount(taxAmount)
                        .build();
                taxDetailsList.add(taxDetail);
                log.info("[TaxService] {}위 사용자: {}, 세금 부과: {}", i + 1, user.getNickname(), taxAmount);
            } else {
                log.warn("[TaxService] {}위 사용자: {}, 자산이 0 이하로 세금 미부과", i + 1, user.getNickname());
            }
        }

        // WebSocket으로 전송
        taxWebSocketHandler.broadcastTaxDetails(taxDetailsList);

        log.info("[TaxService] 세금 부과 완료");
    }
}
