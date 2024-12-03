package com.server.back.domain.tax.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TaxDetailsDto {
    private String nickname;
    private long stockTax;
    private long quizTax;
    private long assetTax;
    private long totalTax;
    private LocalDateTime createdAt;
}