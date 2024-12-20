package com.server.back.common.code.commonCode;


import lombok.Getter;


@Getter
public enum DealType implements TypeModel{
    GET_MONEY_FOR_STOCK("주식팔아서 돈 얻기"),
    GET_MONEY_FOR_AUCTION("경매에서 돈 얻기"),
    GET_MONEY_FOR_DEPOSIT("예금깨서 돈 얻기"),
    GET_MONEY_FOR_RESALE("은행에 되팔기"),
    GET_MONEY_FOR_DAILY("하루에 한 번 돈 받기"),
    GET_MONEY_FOR_TRANSFER("송금에서 돈 얻기"),
    GET_MONEY_FOR_BLOTTO("스피드에서 돈 얻기"),
    GET_MONEY_FOR_DLOTTO("어둠의 복권에서 돈 얻기"),
    GET_MONEY_FOR_LOAN("대출에서 돈 얻기"),
    LOSE_MONEY_FOR_LOAN("대출에서 돈 갚기"),
    LOSE_MONEY_FOR_STOCK("주식 사서 돈 까먹기"),
    LOSE_MONEY_FOR_AUCTION("경매에서 사서 돈 까먹기"),
    LOSE_MONEY_FOR_DEPOSIT("예금에 돈 넣기"),
    LOSE_MONEY_FOR_INFO("정보 사기"),
    LOSE_MONEY_FOR_ASSET("에셋 사기"),
    LOSE_MONEY_FOR_TRANSFER("송금에서 돈 주기"),
    LOSE_MONEY_FOR_BLOTTO("스피드 복권 사기"),
    LOSE_MONEY_FOR_DLOTTO("어둠의 복권 사기"),
    GET_MONEY_FOR_QUIZ("퀴즈 맞추고 돈 얻기"),
    PAY_TAX("세금 부과"); //퀴즈 기능을 위해 추가

    private final String description;

    DealType(String description) {
        this.description = description;
    }

    @Override
    public String getName() {
        return name();
    }


}
