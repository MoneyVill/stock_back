package com.server.back.domain.loan.service;

import com.server.back.domain.loan.dto.LoanReqDto;
import com.server.back.domain.loan.dto.MyLoanResDto;
import com.server.back.domain.loan.dto.MyTotalLoanResDto;

import java.util.List;

public interface LoanService {
    void createLoan(LoanReqDto loanReqDto);

    void deleteLoan(Long loanId);

    List<MyLoanResDto> getLoanList();

    MyTotalLoanResDto getTotalLoan();
}