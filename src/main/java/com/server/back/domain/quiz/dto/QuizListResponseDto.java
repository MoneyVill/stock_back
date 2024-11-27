package com.server.back.domain.quiz.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizListResponseDto {

    private Long id;
    private String question;
    private boolean isSolved;


}
