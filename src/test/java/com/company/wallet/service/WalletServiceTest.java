package com.company.wallet.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import com.company.wallet.entities.Wallet;
import com.company.wallet.exception.handler.WalletException;
import com.company.wallet.repository.WalletRepository;
import com.company.wallet.service.WalletService;
import com.company.wallet.service.WalletServiceImpl;

/**
 * WalletService tests.
 *
 * @author Vinay Singh
 */
@RunWith(SpringRunner.class)
public class WalletServiceTest {
	@TestConfiguration
	static class WalletServiceImplTestContextConfiguration {
		@Bean
		public WalletService walletService() {
			return new WalletServiceImpl();
		}
	}

	public static final String PLAYER_ID_1 = "player1";
	public static final String PLAYER_ID_2 = "player2";

	@Autowired
	private WalletService walletService;

	@MockBean
	private WalletRepository walletRepository;

	Wallet wallet1;
	Wallet wallet2;

	@Before
	public void setUp() {
		wallet1 = new Wallet(PLAYER_ID_1, new BigDecimal(10));
		wallet1.setId(1);
		wallet2 = new Wallet(PLAYER_ID_2, new BigDecimal(20));
		wallet2.setId(2);

		// walletService.findAll
		Mockito.when(walletRepository.findAllByOrderByIdAsc()).thenReturn(Arrays.asList(wallet1, wallet2));
		// findById
		Mockito.when(walletRepository.findById(wallet1.getId())).thenReturn(Optional.of(wallet1));
		Mockito.when(walletRepository.findById(110)).thenReturn(Optional.empty());

		// walletService.findWalletByPlayerId
		Mockito.when(walletRepository.findWalletByPlayerId(PLAYER_ID_1)).thenReturn(wallet1);
		Mockito.when(walletRepository.findWalletByPlayerId("test")).thenReturn(null);
		Mockito.when(walletRepository.save(wallet1)).thenReturn(wallet1);
		Mockito.when(walletRepository.save(wallet2)).thenReturn(wallet2);
	}

	@Test
	public void testFindAll() throws WalletException {
		List<Wallet> found = walletService.findAll();
		assertNotNull(found);
		assertTrue(found.size() == 2);
		assertTrue(found.get(0).getId().equals(wallet1.getId()));
		assertTrue(found.get(1).getId().equals(wallet2.getId()));
	}

	@Test
	public void testfindWalletByPlayerId_Success() throws WalletException {
		Wallet found = walletService.findWalletByPlayerId(wallet1.getPlayerId());
		assertNotNull(found);
		assertTrue(found.getPlayerId().equals(wallet1.getPlayerId()));
	}

	@Test(expected = WalletException.class)
	public void testFindWalletByPlayerId_DoesntExist() throws WalletException {
		Wallet found = walletService.findWalletByPlayerId("test");
	}

	@Test(expected = WalletException.class)
	public void testCreateWallet_Null() throws WalletException {
		Wallet found = walletService.createWallet(PLAYER_ID_1, null);
	}

	@Test(expected = WalletException.class)
	public void testCreateWallet_Blank() throws WalletException {
		Wallet found = walletService.createWallet(PLAYER_ID_1, "");
	}

	@Test
	public void testCreateWallet_Success() throws WalletException {
		Mockito.when(walletRepository.save(Mockito.any(Wallet.class))).thenReturn(wallet1);
		Wallet found = walletService.createWallet(PLAYER_ID_1, "10");
		assertEquals(found.getId(), wallet1.getId());
	}

	@Test
	public void testUpdateWalletAmount_isCredit() throws WalletException {
		int amount = 30;
		Wallet found = walletService.updateWalletAmount(wallet1, String.valueOf(amount), "CREDIT");
		assertEquals(found.getId(), wallet1.getId());
		assertEquals(found.getBalance(), new BigDecimal("40"));
	}

	@Test
	public void testUpdateWalletAmount_isDebitSuccess() throws WalletException {
		int amount = 10;
		Wallet found = walletService.updateWalletAmount(wallet2, String.valueOf(amount), "DEBIT");
		assertEquals(found.getId(), wallet2.getId());
		assertEquals(found.getBalance(), new BigDecimal(10));
	}

	@Test
	public void testUpdateWalletAmount_isDebitFailure() throws WalletException {
		int amount = 100;
		try {
			Wallet found = walletService.updateWalletAmount(wallet2, String.valueOf(amount), "DEBIT");
			fail();
		} catch (WalletException ex) {
			assertTrue(ex.getMessage().contains("No sufficient funds in account for withdrawl"));
			assertEquals(ex.getErrorCode(), HttpStatus.BAD_REQUEST.value());
		}
	}

	@Test
	public void testUpdateWalletAmount_AmountNotANumber() throws WalletException {
		String badAmount = "INVALID";
		try {
			Wallet found = walletService.updateWalletAmount(wallet2, badAmount, "DEBIT");
			fail();
		} catch (WalletException ex) {
			assertTrue(ex.getMessage().contains("Please specify valid amount"));
			assertEquals(ex.getErrorCode(), HttpStatus.BAD_REQUEST.value());
		}
	}

}
