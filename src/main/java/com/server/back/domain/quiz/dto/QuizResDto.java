package com.server.back.domain.quiz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuizResDto {
    @JsonProperty("isCorrect")
    private boolean isCorrect;  //정답 여부
    @JsonProperty("prizeMoney")
    private Long prizeMoney;    //상금 금액
}
