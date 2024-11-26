package com.server.back.domain.user.controller;


import com.server.back.common.code.dto.ResultDto;
import com.server.back.domain.user.dto.LoginReqDto;
import com.server.back.domain.user.dto.LoginResDto;
import com.server.back.domain.user.service.LoginService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = "유저 로그인 API")
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/api/login")
    @ApiOperation(value = "로그인합니다.", notes = "")
    public ResponseEntity<ResultDto<LoginResDto>> login(@RequestBody LoginReqDto loginReqDto, HttpServletResponse response) {
        // JWT Secret Key
        String secretKey = "ds3eg4sd4as3rr313434112r234dds3rr31343343rt3ws3rr313434112as3rr313434112fds6dfs";

        // Access Token 생성
        String accessToken = Jwts.builder()
                .setSubject(loginReqDto.getAccount()) // 토큰의 subject 설정
                .setIssuedAt(new Date())             // 토큰 발급 시간
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1시간 만료
                .signWith(SignatureAlgorithm.HS256, secretKey) // 서명
                .compact();

        // Refresh Token 생성 (더 긴 만료 시간)
        String refreshToken = Jwts.builder()
                .setSubject(loginReqDto.getAccount())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7)) // 7일 만료
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        // 임의의 닉네임 설정
        String nickname = "testUser";

        // 강제로 성공 응답 반환
        LoginResDto loginResDto = LoginResDto.fromEntity(accessToken, refreshToken, nickname);

        return ResponseEntity.ok().body(ResultDto.of(loginResDto));
    }


    @PostMapping("/refresh")
    @ApiOperation(value = "엑세스 토큰을 재발급합니다.", notes = "")
    public ResponseEntity<ResultDto<LoginResDto>> createAccessToken(@RequestHeader Map<String, String> loginRequestHeader, HttpServletResponse response) {
        LoginResDto loginResDto = loginService.createAccessToken(loginRequestHeader, response);

        return ResponseEntity.ok().body(ResultDto.of(loginResDto));
    }


    @GetMapping("/logout/redirect")
    @ApiOperation(value = "로그아웃합니다.", notes = "")
    public ResponseEntity<ResultDto<Boolean>> logoutPost(@RequestHeader Map<String, String> logoutRequestHeader) {
        loginService.deleteAccessToken(logoutRequestHeader);

        return ResponseEntity.ok().body(ResultDto.ofSuccess());
    }

}