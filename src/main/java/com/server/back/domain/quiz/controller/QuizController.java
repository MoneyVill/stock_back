package com.server.back.domain.quiz.controller;

import com.server.back.common.code.dto.ResultDto;
import com.server.back.domain.quiz.dto.QuizResDto;
import com.server.back.domain.quiz.service.QuizService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/quiz")
@RequiredArgsConstructor
@Api(tags = "퀴즈 API")
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/submit")
    @ApiOperation(value = "퀴즈 답안을 제출합니다.")
    public ResponseEntity<ResultDto<QuizResDto>> submitAnswer(@RequestBody QuizResDto quizResDto) {
        QuizResDto result = quizService.submitAnswer(quizResDto);
        return ResponseEntity.ok(ResultDto.of(result));
    }
}
