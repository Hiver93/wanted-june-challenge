package com.kdw.wanted.domain.account.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.kdw.wanted.domain.account.domain.Account;
import com.kdw.wanted.domain.account.repository.AccountRepository;
import com.kdw.wanted.global.error.ErrorCode;
import com.kdw.wanted.global.error.exception.AccountException;

@SpringBootTest
@TestPropertySource(locations = "/application-test.properties")
public class AccountServiceTest {

	@Autowired
	AccountService accountService;
	@Autowired
	AccountRepository accountRepository;
	
	@BeforeEach
	public void init() {
		
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
		Account newAccount = Account.builder()
								.id(UUID.randomUUID())
								.username(lastAccount.getUsername())
								.password("1234")
								.build();
		accountRepository.save(lastAccount);
		
		// when
		Exception e = assertThrows(
		// then
				AccountException.class, ()->accountService.signup(newAccount));
		assertEquals(ErrorCode.USER_NAME_CONPLICT, ((AccountException)e).getErrorCode());
		
		
	}
}
