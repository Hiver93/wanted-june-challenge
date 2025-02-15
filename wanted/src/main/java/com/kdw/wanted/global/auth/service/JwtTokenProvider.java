package com.kdw.wanted.global.auth.service;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.kdw.wanted.domain.account.domain.Account;
import com.kdw.wanted.global.auth.dto.JwtToken;
import com.kdw.wanted.global.error.ErrorCode;
import com.kdw.wanted.global.error.exception.AuthException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JwtTokenProvider {
	private final Key key;
	private final Long EXPIRATION;
	
	public JwtTokenProvider(@Value("${jwt.secret}") String secretKey, @Value("${jwt.access_expiration}") Long expiration){
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		this.key = Keys.hmacShaKeyFor(keyBytes);
		this.EXPIRATION = expiration;
	}
	
	public JwtToken genrateToken(Authentication authentication) {
		String authorities = authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(","));
		Account account = (Account)authentication.getPrincipal();
		Map<String, Object> claims = Map.of("id", account.getId(),"username", account.getUsername());
		
		long now = new Date().getTime();
		Date accessTokenExpiresln = new Date(now + EXPIRATION);
		String accessToken = Jwts.builder()
				.setSubject(authentication.getName())
				.claim("auth", authorities)
				.addClaims(claims)
				.setExpiration(accessTokenExpiresln)
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();
		String refreshToken = Jwts.builder()
				.setExpiration(accessTokenExpiresln)
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();
		
		return JwtToken.builder()
				.grantType("Bearer")
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.build();
	}
	
	public Authentication getAuthentication(String accessToken) {
		Claims claims = parseClaims(accessToken);
		String auth = Optional.of(claims.get("auth")).orElseThrow(()->new RuntimeException("권한 정보가 없는 토큰")).toString();
		Collection<? extends GrantedAuthority> authorities =
				Arrays.stream(auth.split(","))
						.map(SimpleGrantedAuthority::new)
						.toList();
		UserDetails principal = new User(claims.getSubject(),"",authorities);
		return new UsernamePasswordAuthenticationToken(principal, "",authorities);
		
	}
	
	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token);
			return true;
		} catch (SecurityException | MalformedJwtException e) {
			log.info("INVALID_JWT_TOKEN", e);
			throw new AuthException(ErrorCode.INVALID_JWT_TOKEN);
        } catch (ExpiredJwtException e) {
        	log.info("Expired JWT Token", e);
        	throw new AuthException(ErrorCode.EXPIRED_JWT_TOKEN);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
            throw new AuthException(ErrorCode.UNSUPPORTED_JWT_TOKEN);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
            throw new AuthException(ErrorCode.JWT_CLAIMS_EMPTY);
        }
	}

	private Claims parseClaims(String accessToken) {
		
		try {
			return Jwts.parserBuilder()
					.setSigningKey(key)
					.build()
					.parseClaimsJws(accessToken)
					.getBody();
		} catch(ExpiredJwtException e) {
			return e.getClaims();
		}
	}
}