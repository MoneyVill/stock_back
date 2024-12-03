package com.server.back.common.util;

import com.server.back.common.code.commonCode.IsCompleted;
import com.server.back.domain.bank.repository.BankRepository;
import com.server.back.domain.stock.entity.MarketEntity;
import com.server.back.domain.stock.entity.UserDealEntity;
import com.server.back.domain.stock.repository.MarketRepository;
import com.server.back.domain.stock.repository.UserDealRepository;
import com.server.back.domain.user.entity.UserEntity;
import com.server.back.exception.CustomException;
import com.server.back.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AssetCalculator {

    private final MarketRepository marketRepository;
    private final UserDealRepository userDealRepository;
    private final BankRepository bankRepository;

    public Long calculateTotalAssets(UserEntity user) {
        Long totalMoney = user.getCurrentMoney();

        // 주식 자산 계산
        MarketEntity market = marketRepository.findTopByOrderByCreatedAtDesc()
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
        Long marketId = market.getId();
        List<UserDealEntity> stockList = userDealRepository.findAllByUserAndStockMarketId(user, marketId);

        for (UserDealEntity userDeal : stockList) {
            int amount = userDeal.getTotalAmount();
            Float average = userDeal.getAverage();
            Float rate = userDeal.getRate();
            Float stockValue = ((100 + rate) / 100) * average * amount;
            totalMoney += stockValue.longValue();
        }

        // 은행 잔액 계산
        totalMoney += bankRepository.getPriceSumByUserIdAndIsCompleted(user.getId(), IsCompleted.N).orElse(0L);

        return totalMoney;
    }
}
