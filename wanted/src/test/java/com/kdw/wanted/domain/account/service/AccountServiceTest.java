package com.kdw.wanted.domain.account.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.TestPropertySource;

import com.kdw.wanted.domain.account.domain.Account;
import com.kdw.wanted.domain.account.repository.AccountRepository;
import com.kdw.wanted.global.auth.dto.JwtToken;
import com.kdw.wanted.global.error.ErrorCode;
import com.kdw.wanted.global.error.exception.AccountException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

@SpringBootTest
@TestPropertySource(locations = "/application-test.properties")
public class AccountServiceTest {

	@Autowired
	AccountService accountService;
	@Autowired
	AccountRepository accountRepository;
	
	@Value("${jwt.secret}")
	String SECRET_KEY_STRING;
	
	Key SECRET_KEY;
	
	@BeforeEach
	public void init() {
		byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY_STRING);
		this.SECRET_KEY = Keys.hmacShaKeyFor(keyBytes);
	}
	
	@AfterEach
	public void teardown() {
		accountRepository.deleteAll();
	}
	
	// signup
	@Test
	@DisplayName("새로운 회원정보가 DB에 저장된다.")
	public void signup() {
		
		// given
		Account account = Account.builder()
							.password("1234")
							.username("name")
							.build();
		Account expected = Account.builder()
				.password("1234")
				.username("name")
				.roles(List.of("USER"))
				.build();
		
		// when
		String result =  accountService.signup(account);
		
		
		// then
		assertEquals("success", result);
		assertEquals(1,accountRepository.count());
		
		Account saved = accountRepository.findAll().get(0);
		assertEquals(expected.getUsername(), saved.getUsername());
		assertEquals(expected.getPassword(), saved.getPassword());
		assertEquals(expected.getRoles(), saved.getRoles());
	}
	
	@Test
	@DisplayName("제출받은 유저네임이 중복되면 USER_NAME_COMPLICT 예외를 발생시킨다")
	public void signupUserNameComplict() {
		
		// given
		Account lastAccount = Account.builder()
								.id(UUID.randomUUID())
								.username("name")
								.password("password")
								.build();
		accountRepository.save(lastAccount);
		Account newAccount = Account.builder()
								.id(UUID.randomUUID())
								.username(lastAccount.getUsername())
								.password("1234")
								.build();
		
		// when
		// then
		Exception e = assertThrows(
				AccountException.class, ()->accountService.signup(newAccount));
		assertEquals(ErrorCode.USER_NAME_CONPLICT, ((AccountException)e).getErrorCode());
	}
	
	// signin
	@Test
	@DisplayName("제출받은 유저 정보를 담은 JWT Token을 받아온다.")
	public void signin() {
		
		// given
		Account account = Account.builder()
							.id(UUID.randomUUID())
							.username("name")
							.password("password")
							.roles(List.of("USER"))
							.build();	
		accountRepository.save(account);
		
		Account loginInfo = Account.builder()
								.username(account.getUsername())
								.password(account.getPassword())
								.build();
		
		// when
		JwtToken token = accountService.signin(loginInfo);
		
		// then
		Claims claims = Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token.getAccessToken()).getBody();
		assertEquals(account.getId().toString(),claims.get("id").toString());
		assertEquals(account.getUsername(), claims.get("username"));
	}
	
	@Test
	@DisplayName("해당하는 유저이름이 없다면 BadCredentialsException 예외 발생")
	public void signinInvalidCredentialsUsername() {
		// given
		Account account = Account.builder()
				.id(UUID.randomUUID())
				.username("name")
				.password("password")
				.roles(List.of("USER"))
				.build();	
		accountRepository.save(account);
		
		Account loginInfo = Account.builder()
								.username("wrong")
								.password(account.getPassword())
								.build();
		
		// when
		// then
		Exception e = assertThrows(
				BadCredentialsException.class, ()->accountService.signin(loginInfo));
	}
	
	@Test
	@DisplayName("비밀번호가 잘못되었다면 BadCredentialsException 예외 발생")
	public void signinInvalidCredentialsPassword() {
		// given
		Account account = Account.builder()
				.id(UUID.randomUUID())
				.username("name")
				.password("password")
				.roles(List.of("USER"))
				.build();	
		accountRepository.save(account);
		
		Account loginInfo = Account.builder()
								.username(account.getUsername())
								.password("wrong")
								.build();
		
		// when
		// then
		Exception e = assertThrows(
				BadCredentialsException.class, ()->accountService.signin(loginInfo));
	}
}
