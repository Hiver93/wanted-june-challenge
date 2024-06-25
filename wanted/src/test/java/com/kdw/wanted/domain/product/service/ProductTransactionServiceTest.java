package com.kdw.wanted.domain.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.kdw.wanted.domain.account.domain.Account;
import com.kdw.wanted.domain.account.repository.AccountRepository;
import com.kdw.wanted.domain.product.domain.Product;
import com.kdw.wanted.domain.product.domain.ProductTransaction;
import com.kdw.wanted.domain.product.domain.enums.ProductState;
import com.kdw.wanted.domain.product.domain.enums.ProductTransactionState;
import com.kdw.wanted.domain.product.repository.ProductRepository;
import com.kdw.wanted.domain.product.repository.ProductTransactionRepository;
import com.kdw.wanted.global.error.ErrorCode;
import com.kdw.wanted.global.error.exception.AccountException;
import com.kdw.wanted.global.error.exception.ProductTransactionException;


@TestPropertySource(locations = "/application-test.properties")
@SpringBootTest
public class ProductTransactionServiceTest {
	@Autowired
	private ProductTransactionService productTransactionService;
	
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private ProductTransactionRepository productTransactionRepository;

	List<Account> accountList;
	Account provider;
	List<Account> consumerList;
	
	@BeforeEach
	public void init() {
		for(int i = 0; i < 101; ++i) {
			accountRepository.save(Account.builder()
									.id(UUID.randomUUID())
									.username("account"+i)
									.password("1234")
									.build());
		}
		accountList = accountRepository.findAll();
		provider = accountList.get(0);
		consumerList = accountList.subList(1, accountList.size());
	}
	
	@AfterEach
	public void teardown() {
		productTransactionRepository.deleteAll();
		productRepository.deleteAll();
		accountRepository.deleteAll();
	}

	// makeTransaction
	@Test
	@DisplayName("거래요청를 DB에 저장한다. 상품의 남은 개수를 하나 줄인다.")
	public void makeTransaction(){
		
		// given
		Product product = Product.builder()
				.account(provider)
				.name("상품")
				.price(1000l)
				.quantity(100l)
				.remaining(100l)
				.state(ProductState.SALE)
				.build();
		product = productRepository.save(
							product
				);
		Account consumer = consumerList.get(0);
		
		// when
		String result = productTransactionService.makeTransaction(product.getId(), consumer.getId());
		
		// then		
		assertEquals(1, productTransactionRepository.count());
		ProductTransaction saved = productTransactionRepository.findAll().get(0);
		ProductTransaction expected = ProductTransaction.builder()
											.consumer(consumer)
											.price(product.getPrice())
											.state(ProductTransactionState.RESERVED)
											.product(Product.builder()
														.id(product.getId())
														.remaining(99l)
														.build())
											.build();
		assertEquals(expected.getConsumer().getId(),saved.getConsumer().getId());
		assertEquals(expected.getPrice(),saved.getPrice());
		assertEquals(expected.getState(),saved.getState());
		assertEquals(expected.getProduct().getId(),saved.getProduct().getId());
		assertEquals(expected.getProduct().getRemaining(), saved.getProduct().getRemaining());
	}
	
	@Test
	@DisplayName("판매자가 거래요청을 신청하면 UNAUTHORIZED_ACCOUNT 예외 발생")
	public void makeTransactionUnauthorizedAccount() {
		
		// given
		Product product = Product.builder()
				.account(provider)
				.name("상품")
				.price(1000l)
				.quantity(100l)
				.remaining(100l)
				.state(ProductState.SALE)
				.build();
		product = productRepository.save(
							product
				);
		Long productId = product.getId();
		// when
		// then
		Exception e = assertThrows(
				AccountException.class, ()->productTransactionService.makeTransaction(productId, provider.getId()));
		assertEquals(ErrorCode.UNAUTHORIZED_ACCOUNT, ((AccountException)e).getErrorCode());
	}
	
	@Test
	@DisplayName("남은 상품이 없다면 TRANSACTION_NOT_ACCEPTABLE 예외 발생")
	public void makeTransactionTransactionNotAcceptable() {
		
		// given
		Product product = Product.builder()
				.account(provider)
				.name("상품")
				.price(1000l)
				.quantity(100l)
				.remaining(0l)
				.state(ProductState.SALE)
				.build();
		product = productRepository.save(
							product
				);
		Long productId = product.getId();
		Account consumer = consumerList.get(0);
		
		// when
		// then
		Exception e = assertThrows(
				ProductTransactionException.class, ()->productTransactionService.makeTransaction(productId, consumer.getId()));
		assertEquals(ErrorCode.TRANSACTION_NOT_ACCEPTABLE, ((ProductTransactionException)e).getErrorCode());
	}
	
	@Test
	@DisplayName("다중 스레드 환경에서 product의 남은 개수가 정상적으로 줄어든다.")
	public void makeTransactionWithLock() throws InterruptedException {
		// given
		Product product = Product.builder()
				.account(provider)
				.name("상품")
				.price(1000l)
				.quantity(100l)
				.remaining(100l)
				.state(ProductState.SALE)
				.build();
		product = productRepository.save(
							product
				);
		Long productId = product.getId();
		UUID consumerId = consumerList.get(0).getId();
		
		//when
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		CountDownLatch countDown = new CountDownLatch(100);
		for(int i = 0; i< 100;++i)
		executorService.execute(()->
			new Runnable() {
					@Override
					public void run() {
						try {
							productTransactionService.makeTransaction(productId, consumerId);
						}
						finally {
						countDown.countDown();
						}
						
					}
				}.run()
			);
		countDown.await();
		
		// then
		product = productRepository.findAll().get(0);
		assertEquals(0, product.getRemaining());
	}
	
	// getTransactions
	@Test
	@DisplayName("거래요청 내역 리스트를 조회한다. (판매자)")
	public void getTransactionsProvider() {
		// given
		Product product = Product.builder()
				.account(provider)
				.name("상품")
				.price(1000l)
				.quantity(100l)
				.remaining(100l)
				.state(ProductState.SALE)
				.build();
		product = productRepository.save(product);
		Account consumer = consumerList.get(0);
		ProductTransaction productTransaction = ProductTransaction.builder()
													.product(product)
													.consumer(consumer)
													.price(product.getPrice())
													.state(ProductTransactionState.RESERVED)
													.build();
		productTransaction = productTransactionRepository.save(productTransaction);
		
		// when
		List<ProductTransaction> result = productTransactionService.getTransactions(provider.getId());
		
		// then
		List<ProductTransaction> expected = List.of(ProductTransaction.builder()
				.product(product)
				.consumer(consumerList.get(0))
				.price(product.getPrice())
				.state(ProductTransactionState.RESERVED)
				.build());
		
		assertEquals(expected.size(),result.size());
		for(int i = 0; i < expected.size(); ++i) {
			assertEquals(expected.get(i).getProduct().getId(), result.get(i).getProduct().getId());
			assertEquals(expected.get(i).getConsumer().getId(), result.get(i).getConsumer().getId());
			assertEquals(expected.get(i).getPrice(), result.get(i).getPrice());
			assertEquals(expected.get(i).getState(), result.get(i).getState());
		}
	}
	
	@Test
	@DisplayName("거래요청 내역 리스트를 조회한다. (구매자)")
	public void getTransactionsConsumer() {
		// given
		Product product = Product.builder()
				.account(provider)
				.name("상품")
				.price(1000l)
				.quantity(100l)
				.remaining(100l)
				.state(ProductState.SALE)
				.build();
		product = productRepository.save(product);
		Account consumer = consumerList.get(0);
		ProductTransaction productTransaction = ProductTransaction.builder()
													.product(product)
													.consumer(consumer)
													.price(product.getPrice())
													.state(ProductTransactionState.RESERVED)
													.build();
		productTransaction = productTransactionRepository.save(productTransaction);
		
		// when
		List<ProductTransaction> result = productTransactionService.getTransactions(consumer.getId());
		
		// then
		List<ProductTransaction> expected = List.of(ProductTransaction.builder()
				.product(product)
				.consumer(consumerList.get(0))
				.price(product.getPrice())
				.state(ProductTransactionState.RESERVED)
				.build());
		
		assertEquals(expected.size(),result.size());
		for(int i = 0; i < expected.size(); ++i) {
			assertEquals(expected.get(i).getProduct().getId(), result.get(i).getProduct().getId());
			assertEquals(expected.get(i).getConsumer().getId(), result.get(i).getConsumer().getId());
			assertEquals(expected.get(i).getPrice(), result.get(i).getPrice());
			assertEquals(expected.get(i).getState(), result.get(i).getState());
		}
	}

}
