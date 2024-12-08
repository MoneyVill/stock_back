package com.server.back.domain.stock.dto;

import com.server.back.domain.stock.entity.ChartEntity;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class StockChartResDto {
    Long priceBefore;
    Long priceEnd;
    LocalDate date;
    Long id;
    Long companyId;
    Float changeRate;
    Long stockHigh;
    Long stockLow;
    Long stockVolume;
    Long stockDividend;

    public static StockChartResDto fromEntity(ChartEntity chart) {
        return StockChartResDto.builder()
                .companyId(chart.getCompany().getId())
                .priceEnd(chart.getPriceEnd())
                .priceBefore(chart.getPriceBefore())
                .date(chart.getDate())
                .id(chart.getId())
                .changeRate(chart.getChangeRate())
                .stockHigh(chart.getStockHigh())
                .stockLow(chart.getStockLow())
                .stockVolume(chart.getStockVolume())
                .stockDividend(chart.getStockDividend())
                .build();
    }
    public static List<StockChartResDto> fromEntityList(List<ChartEntity> chartList) {
        List<StockChartResDto> result = new ArrayList<>();
        ChartEntity prev = null;
        for (ChartEntity curr : chartList) {
            if (prev == null) {
                result.add(StockChartResDto.fromEntity(curr));
            } else {
                Long calculatedPriceEnd = (prev.getChangeRate() != null && prev.getChangeRate() > 0)
                        ? (long) ((curr.getPriceEnd() != null ? curr.getPriceEnd() : 0) * prev.getChangeRate())
                        : (curr.getPriceEnd() != null ? curr.getPriceEnd() : 0);

                StockChartResDto currDto = StockChartResDto.builder()
                        .companyId(curr.getCompany() != null ? curr.getCompany().getId() : 0)
                        .priceEnd(calculatedPriceEnd)
                        .priceBefore(curr.getPriceBefore() != null ? curr.getPriceBefore() : 0)
                        .date(curr.getDate() != null ? curr.getDate() : LocalDate.now())
                        .id(curr.getId() != null ? curr.getId() : 0)
                        .changeRate(curr.getChangeRate() != null ? curr.getChangeRate() : 0)
                        .stockHigh(curr.getStockHigh() != null ? curr.getStockHigh() : 0)
                        .stockLow(curr.getStockLow() != null ? curr.getStockLow() : 0)
                        .stockVolume(curr.getStockVolume() != null ? curr.getStockVolume() : 0)
                        .stockDividend(curr.getStockDividend() != null ? curr.getStockDividend() : 0)
                        .build();

                result.add(currDto);
            }
            prev = curr;
        }
        return result;
    }
}
