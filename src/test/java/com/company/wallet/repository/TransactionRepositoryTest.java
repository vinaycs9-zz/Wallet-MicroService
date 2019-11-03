package com.company.wallet.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit4.SpringRunner;

import com.company.wallet.entities.Transaction;
import com.company.wallet.entities.Wallet;
import com.company.wallet.repository.TransactionRepository;
import com.company.wallet.repository.WalletRepository;
import com.company.wallet.utils.TransactionType;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.ConstraintViolationException;

/**
 * TransactionRepository tests.
 * Use in-memory h2database
 * 
 * @author Elena Medvedeva
 */
@RunWith(SpringRunner.class)
@DataJpaTest
public class TransactionRepositoryTest {
	public static final String PLAYER_ID_1 = "player1";
	public static final String PLAYER_ID_2 = "player2";

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private WalletRepository walletRepository;

	private Wallet wallet1;
	private Wallet wallet2;

	private TransactionType typeCredit;
	private Transaction transaction;

	static int transactionIdCounter = 1;

	@Before
	public void before() {
		wallet1 = new Wallet(PLAYER_ID_1, new BigDecimal(10));
		wallet2 = new Wallet(PLAYER_ID_2, new BigDecimal(20));

		entityManager.persist(wallet1);
		entityManager.persist(wallet2);
		entityManager.flush();

		typeCredit = TransactionType.CREDIT;

		transaction = new Transaction(String.valueOf(transactionIdCounter++), typeCredit, new BigDecimal(20), wallet1);
		entityManager.persist(transaction);
		entityManager.flush();
	}

	@Test
	public void testFindByWallet() {
		List<Transaction> trns = transactionRepository.findByWallet(wallet1);
		assertTrue(trns.size() > 0);
		assertTrue(trns.get(0).getWallet().getId().equals(wallet1.getId()));
		assertTrue(trns.get(0).getId().equals(transaction.getId()));
	}

	@Test
	public void testSave_Credit() {
		int counter = transactionIdCounter++;
		Transaction transaction = new Transaction(String.valueOf(counter), typeCredit, new BigDecimal(20), wallet2);
		Transaction found = transactionRepository.save(transaction);
		assertNotNull(found);
		assertTrue(found.getAmount().equals(new BigDecimal(20)));
		assertTrue(found.getTransactionType().equals(typeCredit));
		assertTrue(found.getTransactionId().equals(String.valueOf(counter)));
		assertTrue(found.getWallet().getId().equals(wallet2.getId()));
	}

	@Test
	public void testSave_Debit() {
		int counter = transactionIdCounter++;
		Transaction transactionDebit = new Transaction(String.valueOf(counter), typeCredit, new BigDecimal(-10),
				wallet1);
		Transaction found = transactionRepository.save(transactionDebit);
		assertNotNull(found);
		assertTrue(found.getAmount().equals(new BigDecimal(-10)));
		assertTrue(found.getTransactionType().equals(typeCredit));
		assertTrue(found.getTransactionId().equals(String.valueOf(counter)));
		assertTrue(found.getWallet().getId().equals(wallet1.getId()));
	}

	@Test
	public void whenSave_NotUniqueTransactionId() {
		int counter = transactionIdCounter - 1;
		Transaction transaction = new Transaction(String.valueOf(counter), typeCredit, new BigDecimal(20), wallet2);
		try {
			Transaction found = transactionRepository.save(transaction);
			entityManager.flush();
			fail();
		} catch (DataIntegrityViolationException ex) {
			assertTrue(ex.getMessage().contains("could not execute statement"));
		}
	}

	@Test
	public void whenSave_NoBalance() {
		int counter = transactionIdCounter++;
		Transaction transaction = new Transaction(String.valueOf(counter), typeCredit, null, wallet2);
		try {
			Transaction found = transactionRepository.save(transaction);
			entityManager.flush();
			fail();
		} catch (ConstraintViolationException ex) {
			assertFalse(ex.getConstraintViolations().isEmpty());
			assertTrue(ex.getConstraintViolations().iterator().next().getMessage()
					.contains("Transaction amount must be provided"));
		}
	}

	@Test
	public void whenSave_FailWrongWallet() {
		Wallet wallet = walletRepository.getOne(100);
		int counter = transactionIdCounter++;
		Transaction transaction = new Transaction(String.valueOf(counter), typeCredit, new BigDecimal(20), wallet);
		try {
			Transaction found = transactionRepository.save(transaction);
			entityManager.flush();
			fail();
		} catch (DataIntegrityViolationException ex) {
			assertTrue(ex.getMessage().contains("could not execute statement"));
		}
	}

}
