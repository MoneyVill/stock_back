package com.server.back.domain.rank.service;

import com.server.back.common.code.commonCode.IsDeleted;
import com.server.back.common.util.AssetCalculator;
import com.server.back.domain.rank.dto.RankResDto;
import com.server.back.domain.rank.entity.RankEntity;
import com.server.back.domain.rank.repository.RankRepository;
import com.server.back.domain.tax.service.TaxService;
import com.server.back.domain.user.entity.UserEntity;
import com.server.back.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class RankServiceImpl implements RankService {

    private final UserRepository userRepository;
    private final RankRepository rankRepository;
    private final TaxService taxService;
    private final AssetCalculator assetCalculator;

    @Override
    public List<RankResDto> getRanking() {
        List<RankEntity> rankEntities = rankRepository.findTop10ByOrderByTotalMoneyDesc();
        return RankResDto.fromEntityList(rankEntities);
    }

    @Transactional
    @CachePut(value = "rank")
    @Scheduled(cron = "0/30 0-58 * * * *", zone = "Asia/Seoul") // 매 1분마다 실행
    public void calRanking() {
        log.info("[RankService] 랭킹 계산 시작");

        taxService.applyAssetTaxToUsers();

        log.info("세금 계산 끝");

        rankRepository.deleteAll();

        List<UserEntity> everyUser = userRepository.findAllByIsDeleted(IsDeleted.N);
        for (UserEntity user : everyUser) {
            Long totalMoney = assetCalculator.calculateTotalAssets(user);
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
