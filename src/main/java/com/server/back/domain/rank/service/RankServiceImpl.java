package com.server.back.domain.rank.service;


import com.server.back.common.code.commonCode.AssetLevelType;
import com.server.back.common.code.commonCode.IsCompleted;
import com.server.back.common.code.commonCode.IsDeleted;
import com.server.back.domain.bank.repository.BankRepository;
import com.server.back.domain.rank.dto.RankResDto;
import com.server.back.domain.rank.dto.TaxDetailsDto;
import com.server.back.domain.rank.entity.RankEntity;
import com.server.back.domain.rank.repository.RankRepository;
import com.server.back.domain.stock.entity.MarketEntity;
import com.server.back.domain.stock.entity.UserDealEntity;
import com.server.back.domain.stock.repository.MarketRepository;
import com.server.back.domain.stock.repository.UserDealRepository;
import com.server.back.domain.store.repository.AssetPriceRepository;
import com.server.back.domain.store.repository.UserAssetRepository;
import com.server.back.domain.user.entity.UserEntity;
import com.server.back.domain.user.repository.UserRepository;
import com.server.back.exception.CustomException;
import com.server.back.exception.DbDataWebSocketHandler;
import com.server.back.exception.ErrorCode;
import com.server.back.exception.TaxWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springfox.documentation.annotations.Cacheable;
import com.server.back.common.code.commonCode.DealType;
import com.server.back.common.repository.DealRepository;
import com.server.back.common.entity.DealEntity;

import java.time.LocalDate;
import java.util.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class RankServiceImpl implements RankService {

    private final UserRepository userRepository;
    private final RankRepository rankRepository;
    private final UserDealRepository userDealRepository;
    private final MarketRepository marketRepository;
    private final BankRepository bankRepository;
    private final UserAssetRepository userAssetRepository;
    private final AssetPriceRepository assetPriceRepository;
    private final DealRepository dealRepository;
    private final TaxService taxService; // TaxService 추가

    @Override
    public List<RankResDto> getRanking() {
        List<RankEntity> rankEntities = rankRepository.findTop10ByOrderByTotalMoneyDesc();
        List<RankResDto> rankList = RankResDto.fromEntityList(rankEntities);

        return rankList;
    }

    // 사용자 총 자산 계산
    private Long calculateTotalMoney(UserEntity user) {
        Long totalMoney = user.getCurrentMoney();

        MarketEntity market = marketRepository.findTopByOrderByCreatedAtDesc()
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
        Long marketId = market.getId();
        List<UserDealEntity> stockList = userDealRepository.findAllByUserAndStockMarketId(user, marketId);

        for (UserDealEntity userDeal : stockList) {
            int amount = userDeal.getTotalAmount();
            Float average = userDeal.getAverage();
            Float rate = userDeal.getRate();
            Float plusMoney = ((100 + rate) / 100) * average * amount;
            totalMoney += plusMoney.longValue();
        }

        totalMoney += bankRepository.getPriceSumByUserIdAndIsCompleted(user.getId(), IsCompleted.N).orElse(0L);
        return totalMoney;
    }

    @Transactional
    @CachePut(value = "rank")
    @Scheduled(cron = "0/30 0-58 * * * *", zone = "Asia/Seoul") // 매 1분마다 실행
    public void calRanking() {
        Optional<MarketEntity> marketEntityOptional = marketRepository.findTopByOrderByCreatedAtDesc();
        if (marketEntityOptional.isEmpty()) {
            log.info("실행하고 아직 정각이 안되서 장이 시작이 안됨.");
            return;
        }

        log.info("[RankService] 랭킹 계산 시작");

        // 세금 부과 및 저장
        taxService.applyTaxToTopUsers();

        // 랭킹 초기화
        rankRepository.deleteAll();

        // 사용자 자산 계산 및 저장
        List<UserEntity> everyUser = userRepository.findAllByIsDeleted(IsDeleted.N);
        for (UserEntity user : everyUser) {
            Long totalMoney = calculateTotalMoney(user);
            RankEntity rank = RankEntity.builder()
                    .nickname(user.getNickname())
                    .totalMoney(totalMoney)
                    .profileImagePath(user.getProfileImagePath())
                    .build();
            rankRepository.save(rank);
            log.info("[RankService] 사용자: {}, 총 자산: {}", user.getNickname(), totalMoney);
        }

        log.info("[RankService] 랭킹 계산 완료.");
    }
}
