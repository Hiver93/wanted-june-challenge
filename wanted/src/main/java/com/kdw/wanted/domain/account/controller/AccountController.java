package com.kdw.wanted.domain.account.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kdw.wanted.domain.account.dto.request.SigninRequestDto;
import com.kdw.wanted.domain.account.dto.request.SignupRequestDto;
import com.kdw.wanted.domain.account.service.AccountService;
import com.kdw.wanted.global.util.BaseResponseBody;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
	
	private final AccountService accountService;
	
	@PostMapping("/signup")
	public ResponseEntity<BaseResponseBody> signup(@RequestBody @Valid SignupRequestDto signupRequesttDto) {
		return new ResponseEntity<BaseResponseBody>(
				BaseResponseBody.of(accountService.signup(signupRequesttDto.toEntity())),
				HttpStatus.CREATED
				);
	}
	
	@PostMapping("/signin")
	public ResponseEntity<BaseResponseBody> signin(@RequestBody @Valid SigninRequestDto signinRequestDto){
		return new ResponseEntity<BaseResponseBody>(
					BaseResponseBody.of(accountService.signin(signinRequestDto.toEntity()),
					"success"),
				HttpStatus.OK
				);
	}
}
