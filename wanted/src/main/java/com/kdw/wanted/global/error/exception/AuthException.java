package com.kdw.wanted.global.error.exception;

import com.kdw.wanted.global.error.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthException extends RuntimeException {
	private ErrorCode errorCode;
}
