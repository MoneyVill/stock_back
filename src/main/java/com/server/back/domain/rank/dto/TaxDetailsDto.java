package com.server.back.domain.rank.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaxDetailsDto {
    private String nickname;
    private Long taxAmount;
    private String createdAt; // 생성 시간 (포맷팅된 값)
}
