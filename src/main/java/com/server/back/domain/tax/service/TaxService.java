package com.server.back.domain.tax.service;

import com.server.back.domain.tax.dto.TaxDetailsDto;

import java.util.List;

public interface TaxService {
    void saveTax(String nickname, Long stockTax, Long quizTax, Long assetTax);

    void applyAssetTaxToUsers(); // 자산세 부과 메서드 선언

    List<TaxDetailsDto> previewAssetTax(); // 세금 미리보기 메서드 선언
}
