package com.kdw.wanted.global.auth.service;

import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;

public interface JwtService {
	public UUID getId(HttpServletRequest request);
}
