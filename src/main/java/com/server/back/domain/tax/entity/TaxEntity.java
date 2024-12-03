package com.server.back.domain.tax.entity;

import com.server.back.domain.user.entity.UserEntity;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tax")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private Long stockTax;

    @Column(nullable = false)
    private Long quizTax;

    @Column(nullable = false)
    private Long assetTax;

    @Column(nullable = false)
    private Long totalTax;

    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0") // 필드 설정 확인
    private Long taxAmount = 0L;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}