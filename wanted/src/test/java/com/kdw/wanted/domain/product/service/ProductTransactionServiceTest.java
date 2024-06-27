package com.kdw.wanted.domain.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
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
import com.kdw.wanted.domain.product.dto.service.response.ProductTransactionServiceResponse.Transactions;
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
		Transactions result = productTransactionService.getTransactions(provider.getId());
		
		// then
		Transactions expected = Transactions.builder()
												.complete(new ArrayList<>())
												.inProgress(List.of(ProductTransaction.builder()
														.product(product)
														.consumer(consumerList.get(0))
														.price(product.getPrice())
														.state(ProductTransactionState.RESERVED)
														.build()))
												.build();
		
		assertEquals(expected.getComplete().size(),result.getComplete().size());
		assertEquals(expected.getInProgress().size(), result.getInProgress().size());
		for(int i = 0; i < expected.getComplete().size(); ++i) {
			assertEquals(expected.getComplete().get(i).getProduct().getId(), result.getComplete().get(i).getProduct().getId());
			assertEquals(expected.getComplete().get(i).getConsumer().getId(), result.getComplete().get(i).getConsumer().getId());
			assertEquals(expected.getComplete().get(i).getPrice(), result.getComplete().get(i).getPrice());
			assertEquals(expected.getComplete().get(i).getState(), result.getComplete().get(i).getState());
		}
		for(int i = 0; i < expected.getInProgress().size(); ++i) {
			assertEquals(expected.getInProgress().get(i).getProduct().getId(), result.getInProgress().get(i).getProduct().getId());
			assertEquals(expected.getInProgress().get(i).getConsumer().getId(), result.getInProgress().get(i).getConsumer().getId());
			assertEquals(expected.getInProgress().get(i).getPrice(), result.getInProgress().get(i).getPrice());
			assertEquals(expected.getInProgress().get(i).getState(), result.getInProgress().get(i).getState());
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
		Transactions result = productTransactionService.getTransactions(consumer.getId());
		
		// then
		Transactions expected = Transactions.builder()
												.complete(new ArrayList<>())
												.inProgress(List.of(ProductTransaction.builder()
														.product(product)
														.consumer(consumerList.get(0))
														.price(product.getPrice())
														.state(ProductTransactionState.RESERVED)
														.build()))
												.build();
		
		assertEquals(expected.getComplete().size(),result.getComplete().size());
		assertEquals(expected.getInProgress().size(), result.getInProgress().size());
		for(int i = 0; i < expected.getInProgress().size(); ++i) {
			assertEquals(expected.getInProgress().get(i).getProduct().getId(), result.getInProgress().get(i).getProduct().getId());
			assertEquals(expected.getInProgress().get(i).getConsumer().getId(), result.getInProgress().get(i).getConsumer().getId());
			assertEquals(expected.getInProgress().get(i).getPrice(), result.getInProgress().get(i).getPrice());
			assertEquals(expected.getInProgress().get(i).getState(), result.getInProgress().get(i).getState());
		}
	}
	
	// approveTransaction
	@Test
	@DisplayName("거래 요청을 승인한다.")
	public void approveTransaction() {
		
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
		String result = productTransactionService.approveTransaction(productTransaction.getId(), provider.getId());
		
		// then
		ProductTransaction expected = ProductTransaction.builder()
											.state(ProductTransactionState.ACCEPTED)
											.build();
		ProductTransaction modified = productTransactionRepository.findById(productTransaction.getId()).get();
		assertEquals("success",result);
		assertEquals(expected.getState(), modified.getState());		
	}
	
	@Test
	@DisplayName("판매자가 아닌 사용자가 요청을 승인하면 UNAUTHORIZED_ACCOUNT 예외 발생")
	public void approveTransactionUnauthorizedAccount() {

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
		Long productTransactionId = productTransaction.getId();
		
		// when
		// then
		Exception e = assertThrows(
				AccountException.class, ()->productTransactionService.approveTransaction(productTransactionId, consumer.getId()));
		assertEquals(ErrorCode.UNAUTHORIZED_ACCOUNT, ((AccountException)e).getErrorCode());
	}
	
	@Test
	@DisplayName("해당하는 거래 내역이 없는 경우 TRANSACTION_NOT_FOUND 예외 발생")
	public void approveTranactionTransactionNotFound() {
		
		// given
		Long productTransactionId = 0l;
		UUID providerId = provider.getId();
		
		// when
		// then
		ProductTransactionException e = assertThrows(
				ProductTransactionException.class, ()->productTransactionService.approveTransaction(productTransactionId, providerId));
		assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, e.getErrorCode());
	}
	
	@Test
	@DisplayName("거래 승락할 수 있는 상태가 아니면 TRANSACTION_NOT_ACCEPTABLE 예외 발생")
	public void approveTransactionTransactionNotAcceptable() {
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
													.state(ProductTransactionState.ACCEPTED)
													.build();
		productTransaction = productTransactionRepository.save(productTransaction);
		Long productTransactionId = productTransaction.getId();
		
		// when
		// then
		ProductTransactionException e = assertThrows(
				ProductTransactionException.class, ()->productTransactionService.approveTransaction(productTransactionId, provider.getId()));
		assertEquals(ErrorCode.TRANSACTION_NOT_ACCEPTABLE, e.getErrorCode());
	}
	
	
	// confirmTransaction
	@Test
	@DisplayName("판매자가 승락한 거래를 구매자가 확정지어 상태를 변경한다.")
	public void confirmTransaction() {
		
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
													.state(ProductTransactionState.ACCEPTED)
													.build();
		productTransaction = productTransactionRepository.save(productTransaction);
		
		// when
		String result = productTransactionService.confirmTransaction(productTransaction.getId(), consumer.getId());
		
		// then
		ProductTransaction expected = ProductTransaction.builder()
											.state(ProductTransactionState.COMPLETE)
											.build();
		ProductTransaction modified = productTransactionRepository.findById(productTransaction.getId()).get();
		assertEquals("success",result);
		assertEquals(expected.getState(), modified.getState());				
	}
	
	@Test
	@DisplayName("해당하는 거래 내역이 없다면 TRANSACTION_NOT_FOUND 예외 발생")
	public void confirmTransactionTransactionNotFound() {
		
		// given
		Long productTransactionId = 0l;
		UUID consumerId = consumerList.get(0).getId();
		
		// when
		// then
		ProductTransactionException e = assertThrows(
				ProductTransactionException.class, ()->productTransactionService.confirmTransaction(productTransactionId, consumerId));
		assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, e.getErrorCode());		
	}
	
	@Test
	@DisplayName("구매자가 아닌 사용자가 요청을 하면 UNAUTHORIZED_ACCOUNT 예외 발생")
	public void confirmTransactionUnauthorizedAccount() {

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
													.state(ProductTransactionState.ACCEPTED)
													.build();
		productTransaction = productTransactionRepository.save(productTransaction);
		Long productTransactionId = productTransaction.getId();
		
		// when
		// then
		AccountException e = assertThrows(
				AccountException.class, ()->productTransactionService.confirmTransaction(productTransactionId, provider.getId()));
		assertEquals(ErrorCode.UNAUTHORIZED_ACCOUNT, e.getErrorCode());		
	}
	
	@Test
	@DisplayName("거래를 확정할 수 있는 상태가 아니면 TRANSACTION_NOT_ACCEPTABLE 예외 발생")
	public void confirmTransactionTransactionNotAcceptable() {
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
		Long productTransactionId = productTransaction.getId();
		
		// when
		// then
		ProductTransactionException e = assertThrows(
				ProductTransactionException.class, ()->productTransactionService.confirmTransaction(productTransactionId, consumer.getId()));
		assertEquals(ErrorCode.TRANSACTION_NOT_ACCEPTABLE, e.getErrorCode());		
	}

	
	// getProductTransactionForProduct
	@Test
	@DisplayName("해당 상품과 구매자의 거래 내역을 조회한다.")
	public void getProductTransactionForProduct() {
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
		ProductTransaction saved = productTransactionService.getProductTransactionForProduct(productTransaction.getId(), consumer.getId());
		
		// then
		ProductTransaction expected = ProductTransaction.builder()
												.product(product)
												.consumer(consumer)
												.price(product.getPrice())
												.state(ProductTransactionState.RESERVED)
												.build();
		assertEquals(expected.getProduct().getId(), saved.getProduct().getId());
		assertEquals(expected.getState(), saved.getState());			
		assertEquals(expected.getConsumer().getId(), saved.getConsumer().getId());
	}
}
