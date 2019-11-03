package com.company.wallet.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import javax.validation.ConstraintViolationException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import com.company.wallet.entities.Wallet;
import com.company.wallet.repository.WalletRepository;

/**
 * WalletRepository tests. 
 * Use in-memory h2database
 * 
 * @author Vinay Singh
 */
@RunWith(SpringRunner.class)
@DataJpaTest
public class WalletRepositoryTest {
	public static final String PLAYER_ID_1 = "player1";
	public static final String PLAYER_ID_2 = "player2";

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private WalletRepository walletRepository;

	private Wallet wallet1;
	private Wallet wallet2;

	@Before
	public void before() {
		wallet1 = new Wallet(PLAYER_ID_1, new BigDecimal(10));
		wallet2 = new Wallet(PLAYER_ID_2, new BigDecimal(20));
		entityManager.persist(wallet1);
		entityManager.persist(wallet2);
		entityManager.flush();
	}

	@Test
	public void whenFindByPlayerId_thenReturnWallet() {
		// when
		Wallet found = walletRepository.findWalletByPlayerId(wallet1.getPlayerId());

		// then
		assertTrue(found.getPlayerId().equals(PLAYER_ID_1));
		assertTrue(found.getBalance().equals(new BigDecimal(10)));
	}

	@Test
	public void whenFindByPlayerId_NotFound() {
		// when
		Wallet found = walletRepository.findWalletByPlayerId("wrongUser");
		// then
		assertNull(found);
	}

	@Test
	public void testFindAllByOrderByIdAsc() {
		List<Wallet> found = walletRepository.findAllByOrderByIdAsc();
		assertNotNull(found);
		assertTrue(!found.isEmpty());
		assertTrue(found.size() >= 2);
		System.out.println(found.get(0).getId());
		System.out.println(found.get(1).getId());
		assertTrue(found.get(0).getId().equals(wallet1.getId()));
		assertTrue(found.get(1).getId().equals(wallet2.getId()));
	}

	@Test
	public void whenSave_Success() {
		Wallet wallet = new Wallet("player3", new BigDecimal(100));
		Wallet found = walletRepository.save(wallet);
		assertNotNull(found);
		assertTrue(found.getBalance().equals(new BigDecimal(100)));
	}

	@Test
	public void update_Balance() {
		Optional<Wallet> found = walletRepository.findById(wallet1.getId());
		Wallet updated = found.get();
		updated.setBalance(new BigDecimal(300));
		Wallet found1 = walletRepository.save(updated);
		assertNotNull(found1);
		assertTrue(found1.getBalance().equals(new BigDecimal(300)));
	}

	@Test
	public void update_BalanceNegative() {
		Optional<Wallet> found = walletRepository.findById(wallet2.getId());
		Wallet updated = found.get();
		updated.setBalance(new BigDecimal(-300));
		Wallet found1 = walletRepository.save(updated);
		try {
			entityManager.flush();
			fail();
		} catch (ConstraintViolationException ex) {
			assertFalse(ex.getConstraintViolations().isEmpty());
			assertTrue(ex.getConstraintViolations().iterator().next().getMessage()
					.contains("must be greater than or equal to 0"));
		}
	}

	@Test
	public void update_BalanceNull() {
		Optional<Wallet> found = walletRepository.findById(wallet2.getId());
		Wallet updated = found.get();
		updated.setBalance(null);
		Wallet found1 = walletRepository.save(updated);
		try {
			entityManager.flush();
			fail();
		} catch (ConstraintViolationException ex) {
			assertFalse(ex.getConstraintViolations().isEmpty());
			System.out.println(ex.getConstraintViolations().iterator().next().getMessage());
			assertTrue(ex.getConstraintViolations().iterator().next().getMessage()
					.contains("Wallet balance must be provided"));
		}
	}

}
