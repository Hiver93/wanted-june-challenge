package com.kdw.wanted.domain.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.kdw.wanted.domain.account.domain.Account;
import com.kdw.wanted.domain.account.repository.AccountRepository;
import com.kdw.wanted.domain.product.domain.Product;
import com.kdw.wanted.domain.product.domain.enums.ProductState;
import com.kdw.wanted.domain.product.repository.ProductRepository;
import com.kdw.wanted.global.error.ErrorCode;
import com.kdw.wanted.global.error.exception.ProductException;

@SpringBootTest
@TestPropertySource(locations = "/application-test.properties")
@Transactional
public class ProductServiceTest {
	
	@Autowired
	AccountRepository accountRepository;
	@Autowired
	ProductRepository productRepository;
	@Autowired
	ProductService productService;
	
	@Autowired
	TestProductRepository testProductRepository;
	
	List<Account> accountList;
	
	@BeforeEach
	public void init() {
		for(int i = 0; i < 5; ++i) {
			accountRepository.save(Account.builder()
									.id(UUID.randomUUID())
									.username("account"+i)
									.password("1234")
									.build());
		}
		accountList = accountRepository.findAll();
	}
	
	@AfterEach
	public void teardown(){
		productRepository.deleteAll();
		accountRepository.deleteAll();
	}
	
	
	//createProduct	
	@Test
	@DisplayName("상품이 저장된다.")
	public void createProduct() {
		// given
		Product product = Product.builder()
							.account(accountList.get(0))
							.name("미소")
							.price(1000l)
							.quantity(10l)
							.remaining(10l)
							.build();
		Product expected = Product.builder()
							.id(testProductRepository.getLastId()+1)
							.account(accountList.get(0))
							.name("미소")
							.price(1000l)
							.quantity(10l)
							.remaining(10l)
							.state(ProductState.SALE)
							.build();
		
		// when
		productService.createProduct(product);
		
		
		// then
		assertTrue(productRepository.existsById((Long)expected.getId()));
		Product saved = productRepository.findById((Long)expected.getId()).get();
		
		assertEquals(expected.getId(), saved.getId());
		assertEquals(expected.getAccount(), saved.getAccount());
		assertEquals(expected.getName(), saved.getName());
		assertEquals(expected.getPrice(), saved.getPrice());
		assertEquals(expected.getQuantity(), saved.getQuantity());
		assertEquals(expected.getRemaining(), saved.getRemaining());
		assertEquals(expected.getState(), saved.getState());
	}
		
	@Test
	@DisplayName("전체 상품 리스트를 조회한다.")
	public void  getProductList() {
		//given
		final int SAVED_COUNT = 10;
		for(int i = 0; i < SAVED_COUNT; ++i) {
			productRepository.save(Product.builder()
					.account(accountList.get(0))
					.name("미소"+i)
					.price(1000l+i)
					.quantity(10l+i)
					.remaining(10l+i)
					.state(ProductState.SALE)
					.build());
		}
		
		
		// when
		List<Product> productList = productService.getProductList();
		
		
		// then
		assertEquals(productList.size(), SAVED_COUNT);
	}
	
	@Test
	@DisplayName("상품 하나를 조회한다.")
	public void getProduct() {
		// given
		Product product = Product.builder()
				.id(testProductRepository.getLastId()+1)
				.account(accountList.get(0))
				.name("미소")
				.price(1000l)
				.quantity(10l)
				.remaining(10l)
				.state(ProductState.SALE)
				.build();
		Product expected = Product.builder()
				.id(testProductRepository.getLastId()+1)
				.account(accountList.get(0))
				.name("미소")
				.price(1000l)
				.quantity(10l)
				.remaining(10l)
				.state(ProductState.SALE)
				.build();

		productRepository.save(product);
		
		// when
		Product saved = productService.getProduct(expected.getId());
		
		// then
		assertEquals(expected.getId(), saved.getId());
		assertEquals(expected.getAccount(), saved.getAccount());
		assertEquals(expected.getName(), saved.getName());
		assertEquals(expected.getPrice(), saved.getPrice());
		assertEquals(expected.getQuantity(), saved.getQuantity());
		assertEquals(expected.getRemaining(), saved.getRemaining());
		assertEquals(expected.getState(), saved.getState());
	}

	@Test
	@DisplayName("상품 id에 해당하는 상품이 없다면 PRODUCT_NOT_FOUND 예외발생")
	public void createProductNoAccount() {
		// given
		Long productId = testProductRepository.getLastId() + 1;
		ProductException expectedException = new ProductException(ErrorCode.PRODUCT_NOT_FOUND);
		
		// when
		Exception exception = assertThrows(
				ProductException.class,
				()-> productService.getProduct(productId)
		);
		
		// then
		assertEquals(expectedException.getErrorCode(), ((ProductException)exception).getErrorCode());
	}

	
	
}



@Repository
interface TestProductRepository extends JpaRepository<Product, Long>{ 
	
	@Query(value = "SELECT LAST_INSERT_ID()", nativeQuery = true)
	Long getLastId();
}