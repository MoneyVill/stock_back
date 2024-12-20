package com.server.back.domain.user.controller;

import com.server.back.common.code.dto.ResultDto;
import com.server.back.domain.user.dto.*;
import com.server.back.domain.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Api(tags = "유저 API")
public class UserController {

    private final UserService userService;

    @PostMapping()
    @ApiOperation(value = "회원가입을 합니다.", notes = "")
    public ResponseEntity<ResultDto<Boolean>> signUp(@RequestBody UsersRegisterReqDto usersRegisterReqDto) {
        userService.createUser(usersRegisterReqDto);

        return ResponseEntity.ok().body(ResultDto.ofSuccess());
    }

    @GetMapping()
    @ApiOperation(value = "로그인한 유저 정보 반환합니다.", notes = "")
    public ResponseEntity<ResultDto<UserInfoLoginResDto>> getLoginUser() {
        UserInfoLoginResDto user = userService.getLoginUser();

        return ResponseEntity.ok().body(ResultDto.of(user));
    }

    @GetMapping("/account/{account}")
    @ApiOperation(value = "계정 중복확인을 합니다.", notes = "")
    public ResponseEntity<ResultDto<Boolean>> checkAccount(@PathVariable String account) {
        Boolean checkAccount = Boolean.TRUE;
        log.info("@@@@@@@@@@@@@");
        return ResponseEntity.ok().body(ResultDto.ofSuccess());
    }


    @GetMapping("/nickname/{nickname}")
    @ApiOperation(value = "닉네임 중복확인을 합니다.", notes = "")
    public ResponseEntity<ResultDto<Boolean>> checkNickname(@PathVariable String nickname) {
        Boolean checkNickname = Boolean.TRUE;

        return ResponseEntity.ok().body(ResultDto.ofSuccess());
    }

    @PutMapping()
    @ApiOperation(value = "회원정보 수정합니다.", notes = "")
    public ResponseEntity<ResultDto<Boolean>> modifyUser(@RequestBody UsersModifyReqDto usersModifyReqDto) {
        userService.updateUser(usersModifyReqDto);

        return ResponseEntity.ok().body(ResultDto.ofSuccess());
    }

    @DeleteMapping()
    @ApiOperation(value = "회원탈퇴를 합니다.", notes = "")
    public ResponseEntity<ResultDto<Boolean>> deleteUser() {
        userService.deleteUser();

        return ResponseEntity.ok().body(ResultDto.ofSuccess());
    }


    @GetMapping(params = {"search"})
    @ApiOperation(value = "회원을 검색합니다.", notes = "")
    public ResponseEntity<ResultDto<List<UserResDto>>> getUserList(@RequestParam String search) {
        List<UserResDto> userResDtoList = userService.getUserList(search);

        return ResponseEntity.ok().body(ResultDto.of(userResDtoList));
    }

    @GetMapping("/random")
    @ApiOperation(value = "랜덤 회원 조회합니다. (랜덤 방문)")
    public ResponseEntity<ResultDto<UserResDto>> getUserRandom() {
        UserResDto userResDto = userService.getUserRandom();

        return ResponseEntity.ok().body(ResultDto.of(userResDto));
    }

    @GetMapping("/info/{nickname}")
    @ApiOperation(value = "방문한 회원 정보를 조회합니다.", notes = "")
    public ResponseEntity<ResultDto<UserInfoResDto>> getUser(@PathVariable String nickname) {
        UserInfoResDto userInfoResDto = userService.getUser(nickname);

        return ResponseEntity.ok().body(ResultDto.of(userInfoResDto));
    }


}