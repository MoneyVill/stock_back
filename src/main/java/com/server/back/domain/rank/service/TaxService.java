package com.server.back.domain.rank.service;

import com.server.back.domain.rank.dto.TaxDetailsDto;

import java.util.List;

public interface TaxService {
    void saveTax(String nickname, Long taxAmount);

    void applyTaxToTopUsers();

    List<TaxDetailsDto> getRecentTaxDetails();
}
