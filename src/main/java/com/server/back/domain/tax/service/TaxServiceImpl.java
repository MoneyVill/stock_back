
package com.server.back.domain.tax.service;

import com.server.back.common.code.commonCode.IsDeleted;
import com.server.back.common.util.AssetCalculator;
import com.server.back.domain.notification.websocket.NotificationWebSocketHandler;
import com.server.back.domain.rank.entity.RankEntity;
import com.server.back.domain.rank.repository.RankRepository;
import com.server.back.domain.stock.repository.UserDealRepository;
import com.server.back.domain.tax.dto.TaxDetailsDto;
import com.server.back.domain.tax.entity.TaxEntity;
import com.server.back.domain.tax.repository.TaxRepository;
import com.server.back.domain.user.entity.UserEntity;
import com.server.back.domain.user.repository.UserRepository;
import com.server.back.exception.CustomException;
import com.server.back.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaxServiceImpl implements TaxService {

    private final TaxRepository taxRepository;
    private final UserRepository userRepository;
    private final AssetCalculator assetCalculator;
    private final RankRepository rankRepository;
    private final UserDealRepository userDealRepository;
    private final NotificationWebSocketHandler notificationHandler;

    @Override
    @Transactional
    public void applyAssetTaxToUsers() {
        log.info("[TaxService] 자산세 부과 프로세스 시작");

        // 세율 계산
        Map<String, Double> taxRates = calculateTaxRates();

        // 모든 사용자 가져오기
        List<UserEntity> users = userRepository.findAllByIsDeleted(IsDeleted.N);
        if (users.isEmpty()) {
            log.warn("[TaxService] 자산세를 부과할 사용자가 없습니다.");
            return;
        }

        for (UserEntity user : users) {
            // 사용자 세율 가져오기 (기본 세율 1%)
            double taxRate = taxRates.getOrDefault(user.getNickname(), 0.001);

            // 자산 계산 및 세금 계산
            Long totalAssets = assetCalculator.calculateTotalAssets(user);

            // 자산이 음수면 세금 부과하지 않음
            if (totalAssets <= 0) {
                log.info("[TaxService] 사용자: {}, 총 자산이 0 이하로 세금 부과 제외", user.getNickname());

                // 알림 전송: 음수 또는 0 자산 상태 알림
                notificationHandler.sendNotification(user.getNickname(),
                        String.format("현재 자산이 %d원으로, 자산세가 부과되지 않았습니다.", totalAssets));
                continue;
            }

            Long taxAmount = Math.round(totalAssets * taxRate);

            // 사용자 자산 차감
            user.decreaseCurrentMoney(taxAmount);
            userRepository.save(user);

            // 세금 저장
            saveTax(user.getNickname(), 0L, 0L, taxAmount);

            log.info("[TaxService] 사용자: {}, 자산: {}, 세율: {}%, 부과된 세금: {}",
                    user.getNickname(), totalAssets, taxRate * 100, taxAmount);

            // **알림 전송 추가**
            notificationHandler.sendTaxNotification(user.getNickname(), taxAmount);
        }

        log.info("[TaxService] 자산세 부과 완료");
    }

    @Override
    public void saveTax(String nickname, Long stockTax, Long quizTax, Long assetTax) {

        log.info("[TaxService] saveTax 호출 - 닉네임: {}, stockTax: {}, quizTax: {}, assetTax: {}", nickname, stockTax, quizTax, assetTax);
        // 닉네임 유효성 검사
        if (nickname == null || nickname.isEmpty()) {
            throw new IllegalArgumentException("[TaxService] 닉네임은 필수입니다.");
        }

        // 사용자 찾기
        UserEntity user = userRepository.findByNicknameAndIsDeleted(nickname, IsDeleted.N)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 세금 저장
        TaxEntity taxEntity = TaxEntity.builder()
                .user(user)
                .stockTax(stockTax)
                .quizTax(quizTax)
                .assetTax(assetTax)
                .totalTax(assetTax) // 총 세금은 assetTax로 대체
                .taxAmount(stockTax + quizTax + assetTax) // tax_amount 계산
                .createdAt(LocalDateTime.now())
                .build();

        taxRepository.save(taxEntity);
        log.info("[TaxService] 세금 저장 - 사용자: {}, 부과된 세금: {}", nickname, assetTax);
    }

    @Override
    public List<TaxDetailsDto> previewAssetTax() {
        log.info("[TaxService] 자산세 미리보기 시작");

        // 세율 계산
        Map<String, Double> taxRates = calculateTaxRates();

        // 사용자 목록 가져오기
        List<UserEntity> users = userRepository.findAllByIsDeleted(IsDeleted.N);
        if (users.isEmpty()) {
            log.warn("[TaxService] 자산세를 부과할 사용자가 없습니다.");
            return Collections.emptyList();
        }

        // 예상 세금 계산
        return users.stream()
                .map(user -> {
                    double taxRate = taxRates.getOrDefault(user.getNickname(), 0.01);
                    Long totalAssets = assetCalculator.calculateTotalAssets(user);
                    Long taxAmount = Math.round(totalAssets * taxRate);

                    return TaxDetailsDto.builder()
                            .nickname(user.getNickname())
                            .assetTax(taxAmount)
                            .totalTax(taxAmount) // totalTax는 assetTax로 대체
                            .build();
                })
                .collect(Collectors.toList());
    }

    private Map<String, Double> calculateTaxRates() {
        Map<String, Double> taxRates = new HashMap<>();
        double[] topRankRates = {0.01, 0.005, 0.002};

        // 랭킹 가져오기
        List<RankEntity> rankings = rankRepository.findTop3ByOrderByTotalMoneyDesc();
        if (rankings.isEmpty()) {
            log.warn("[TaxService] 상위 랭킹 데이터가 없습니다.");
            return taxRates; // 빈 맵 반환
        }

        // 세율 할당
        for (int i = 0; i < rankings.size(); i++) {
            taxRates.put(rankings.get(i).getNickname(), topRankRates[i]);
        }

        log.info("[TaxService] 세율 계산 완료: {}", taxRates);
        return taxRates;
    }
}
