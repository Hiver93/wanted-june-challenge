package com.kdw.wanted.global.auth.service;

import java.security.Key;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.kdw.wanted.global.error.ErrorCode;
import com.kdw.wanted.global.error.exception.AuthException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class JwtServiceImpl implements JwtService {
	private final Key SECRET_KEY;
	private final JwtTokenProvider jwtTokenProvider;
	
	public JwtServiceImpl(@Value("${jwt.secret}") String secretKey, JwtTokenProvider jwtTokenProvider) {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		this.SECRET_KEY = Keys.hmacShaKeyFor(keyBytes);
		this.jwtTokenProvider = jwtTokenProvider;
	}
	
	
	@Override
	public UUID getId(HttpServletRequest request) {
        Claims claims = getClaims(request);
        return getId(claims);
	}
	
    private Claims getClaims(HttpServletRequest request) {
        return parseClaims(getToken(request));
    }
	
    private String getToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")) {
            return bearerToken.substring(7);
        }
        else {
        	throw new AuthException(ErrorCode.INVALID_JWT_TOKEN);
        }

    }
    
    private Claims parseClaims(String token) throws ExpiredJwtException {

        if(2 !=  token.chars().filter(c -> c == '.').count()){
            throw new AuthException(ErrorCode.INVALID_JWT_TOKEN);
        }
        jwtTokenProvider.validateToken(token);
        Claims claims = Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token).getBody();
        return claims;
    }

    private UUID getId(Claims claims) {
    	return UUID.fromString(claims.get("id").toString());
    }
}
