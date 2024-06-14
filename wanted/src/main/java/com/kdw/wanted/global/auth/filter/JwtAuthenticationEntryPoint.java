package com.kdw.wanted.global.auth.filter;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.kdw.wanted.global.error.ErrorCode;
import com.kdw.wanted.global.error.exception.AuthException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
	
	private final HandlerExceptionResolver resolver;
	
	public JwtAuthenticationEntryPoint(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		if(authException instanceof InsufficientAuthenticationException) {
			resolver.resolveException(request, response, null, new AuthException(ErrorCode.MISSING_AUTHENTICATION));
		}
		else {
			resolver.resolveException(request, response, null,
					Optional.ofNullable((Exception)request.getAttribute("exception"))
						.orElse(authException)
					);
		}
	}
	
	

}
