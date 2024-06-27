package com.kdw.wanted.global.error.exception;

import com.kdw.wanted.global.error.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ProductTransactionException extends RuntimeException{
	private final ErrorCode errorCode;
}
