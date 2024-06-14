package com.kdw.wanted.global.error.exception;

import com.kdw.wanted.global.error.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AccountException extends RuntimeException{
	private final ErrorCode errorCode;
}
