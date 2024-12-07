package com.server.back.domain.loan.service;

import com.server.back.domain.loan.dto.LoanReqDto;
import com.server.back.domain.loan.dto.MyLoanResDto;
import com.server.back.domain.loan.dto.MyTotalLoanResDto;
import com.server.back.domain.loan.entity.LoanEntity;
import com.server.back.domain.loan.repository.LoanRepository;
import com.server.back.domain.user.entity.UserEntity;
import com.server.back.domain.user.repository.UserRepository;
import com.server.back.domain.user.service.UserService;
import com.server.back.common.service.AuthService;
import com.server.back.exception.CustomException;
import com.server.back.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {
    private final LoanRepository loanRepository;
    private final UserService userService;
    private final AuthService authService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void createLoan(LoanReqDto loanReqDto) {
        // 인증된 사용자 ID 가져오기
        Long userId = authService.getUserId();
        UserEntity user = userService.getUserById(userId);

        // 사용자 총 대출 금액 계산
        int currentTotalLoanAmount = loanRepository.getTotalLoanAmountByUserId(userId).orElse(0);
        int requestedAmount = loanReqDto.getAmount();

        // 대출 한도 초과 여부 확인
        int loanLimit = 20_000_000; // 2천만 원
        if (currentTotalLoanAmount + requestedAmount > loanLimit) {
            throw new CustomException(ErrorCode.LOAN_LIMIT_EXCEEDED);
        }

        // 대출 금액 추가
        user.increaseCurrentMoney((long) loanReqDto.getAmount());
        userRepository.save(user);

        // 대출 기록 저장
        LoanEntity loanEntity = new LoanEntity();
        loanEntity.setAmount(loanReqDto.getAmount());
        loanEntity.setUserId(user.getId());
        loanRepository.save(loanEntity);
    }

    @Override
    @Transactional
    public void deleteLoan(Long loanId) {
        // 대출 정보 및 사용자 확인
        LoanEntity loan = loanRepository.findById(loanId).orElseThrow(() -> new IllegalArgumentException("Loan not found"));
        UserEntity user = userService.getUserById(loan.getUserId());

        // 대출 상환 처리
        user.decreaseCurrentMoney((long) loan.getAmount());
        userRepository.save(user);

        loanRepository.deleteById(loanId);
    }

    @Override
    public List<MyLoanResDto> getLoanList() {
        Long userId = authService.getUserId();
        return loanRepository.findAllByUserId(userId).stream()
                .map(loan -> new MyLoanResDto(loan.getId(), loan.getAmount()))
                .collect(Collectors.toList());
    }

    @Override
    public MyTotalLoanResDto getTotalLoan() {
        Long userId = authService.getUserId();
        List<LoanEntity> loans = loanRepository.findAllByUserId(userId);
        int totalAmount = loans.stream().mapToInt(LoanEntity::getAmount).sum();
        return new MyTotalLoanResDto(totalAmount);
    }
}
