package com.server.back.domain.loan.controller;

import com.server.back.common.code.dto.ResultDto;
import com.server.back.domain.loan.dto.LoanReqDto;
import com.server.back.domain.loan.dto.MyLoanResDto;
import com.server.back.domain.loan.dto.MyTotalLoanResDto;
import com.server.back.domain.loan.service.LoanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loan")
@RequiredArgsConstructor
@Api(tags = "대출 API")
public class LoanController {
    private final LoanService loanService;

    @PostMapping()
    @ApiOperation(value = "대출을 실행합니다.", notes = "")
    public ResponseEntity<ResultDto<Boolean>> createLoan(@RequestBody LoanReqDto loanReqDto) {
        loanService.createLoan(loanReqDto);
        return ResponseEntity.ok().body(ResultDto.ofSuccess());
    }

    @DeleteMapping("/{loanId}")
    @ApiOperation(value = "대출을 상환합니다.", notes = "")
    public ResponseEntity<ResultDto<Boolean>> deleteLoan(@PathVariable Long loanId) {
        loanService.deleteLoan(loanId);
        return ResponseEntity.ok().body(ResultDto.ofSuccess());
    }

    @GetMapping("/list")
    @ApiOperation(value = "내 대출 리스트를 확인합니다.", notes = "")
    public ResponseEntity<ResultDto<List<MyLoanResDto>>> getLoanList() {
        List<MyLoanResDto> myLoanResDtoList = loanService.getLoanList();
        return ResponseEntity.ok().body(ResultDto.of(myLoanResDtoList));
    }

    @GetMapping()
    @ApiOperation(value = "내 총 대출 금액을 확인합니다.", notes = "")
    public ResponseEntity<ResultDto<MyTotalLoanResDto>> getTotalLoan() {
        MyTotalLoanResDto myTotalLoanResDto = loanService.getTotalLoan();
        return ResponseEntity.ok().body(ResultDto.of(myTotalLoanResDto));
    }
}