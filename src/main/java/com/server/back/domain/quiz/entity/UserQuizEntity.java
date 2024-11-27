package com.server.back.domain.quiz.entity;

import com.server.back.domain.user.entity.UserEntity;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserQuizEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private UserEntity user;

    @ManyToOne
    private QuizEntity quiz;

    private boolean isSolved;
    private LocalDate solvedAt;
}
