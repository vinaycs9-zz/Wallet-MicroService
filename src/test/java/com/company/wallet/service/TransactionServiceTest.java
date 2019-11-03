package com.company.wallet.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

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

import com.company.wallet.entities.Transaction;
import com.company.wallet.entities.Wallet;
import com.company.wallet.exception.handler.WalletException;
import com.company.wallet.repository.TransactionRepository;
import com.company.wallet.repository.WalletRepository;
import com.company.wallet.service.TransactionService;
import com.company.wallet.service.TransactionServiceImpl;
import com.company.wallet.service.WalletService;
import com.company.wallet.utils.TransactionType;

/**
 * TransactionService tests.
 *
 * @author Vinay Singh
 */
@RunWith(SpringRunner.class)
public class TransactionServiceTest {
	@TestConfiguration
	static class TransactionServiceImplTestContextConfiguration {
		@Bean
		public TransactionService transactionService() {
			return new TransactionServiceImpl();
		}
	}
	
	public static final String PLAYER_ID_1 = "player1";
	public static final String PLAYER_ID_2 = "player2";
	String notFoundPlayerId = "notFoundPlayer";
	
	static int transactionIdCounter = 1;
	
	@Autowired
    private TransactionService transactionService;

    @MockBean
    private WalletRepository walletRepository;

    @MockBean
    private TransactionRepository transactionRepository;
    
    @MockBean
    private WalletService walletService;
    
    private Wallet wallet1;
    private Wallet wallet2;
    private TransactionType typeCredit;
    private TransactionType typeDebit;
    private Transaction transactionCredit;
    private Transaction transactionDebit;

	@Before
	public void setUp() throws WalletException {
		wallet1 = new Wallet(PLAYER_ID_1, new BigDecimal(10));
		wallet1.setId(1);
		wallet2 = new Wallet(PLAYER_ID_2, new BigDecimal(20));
		wallet2.setId(2);
		typeCredit = TransactionType.CREDIT;
		typeDebit = TransactionType.DEBIT;
		transactionCredit = new Transaction(String.valueOf(transactionIdCounter++), typeCredit, new BigDecimal(20),
				wallet1);
		transactionCredit.setId(5);
		transactionDebit = new Transaction(String.valueOf(transactionIdCounter++), typeDebit, new BigDecimal(20),
				wallet1);
		transactionDebit.setId(6);

		Mockito.when(walletService.findWalletByPlayerId(wallet1.getPlayerId())).thenReturn(wallet1);
		Mockito.when(walletService.findWalletByPlayerId(wallet2.getPlayerId())).thenReturn(wallet2);
		Mockito.when(walletService.findWalletByPlayerId(notFoundPlayerId)).thenThrow(new WalletException("No wallet found with playerId " + notFoundPlayerId, HttpStatus.NOT_FOUND.value()));

		Mockito.when(transactionRepository.findByWalletOrderByUpdatedTimeDesc(wallet1)).thenReturn(Arrays.asList(transactionCredit));

		Mockito.when(transactionRepository.findByWalletOrderByUpdatedTimeDesc(wallet2)).thenReturn(Arrays.asList(transactionCredit));
		
		
	}
	
	@Test
    public void testGetTransactionsByWalletId_Success() throws WalletException {
        List<Transaction> found = transactionService.getTransactionsByWallet(wallet1);
        assertNotNull(found);
        assertTrue(found.size() == 1);
        assertTrue(found.get(0).getId().equals(transactionCredit.getId()) );
     }
	
	@Test
    public void testGetTransactionsByWalletId_Failed() throws WalletException { 
		Wallet wallet = null;
                try {
                 wallet = new Wallet("player3", new BigDecimal(20));
            List<Transaction> found = transactionService.getTransactionsByWallet(new Wallet("player3", new BigDecimal(20)));
            fail();
        } catch (WalletException ex){
        	assertTrue(ex.getMessage().contains("Transaction List for player " + wallet.getPlayerId() + " not found"));
        	assertEquals(ex.getErrorCode(),HttpStatus.NOT_FOUND.value());
        }
    }	
	
    @Test
    public void testCreateTransaction_SuccessCredit() throws WalletException {
        int amount = 100;
        Mockito.when(walletService.updateWalletAmount(wallet1,String.valueOf(amount),"CREDIT")).thenReturn(wallet1);
        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transactionCredit);
        int counter = transactionIdCounter++;
        Transaction found = transactionService.createTransaction(String.valueOf(counter),wallet1.getPlayerId(),"CREDIT",String.valueOf(amount));
        assertNotNull(found);
        assertTrue(found.getId().equals(transactionCredit.getId()) );
    }
    
    @Test
    public void testCreateTransaction_SuccessDebit() throws WalletException {
        int amount = -10;
        Mockito.when(walletService.updateWalletAmount(wallet2,String.valueOf(amount),"DEBIT")).thenReturn(wallet2);
        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transactionDebit);
        int counter = transactionIdCounter++;
        Transaction found = transactionService.createTransaction(String.valueOf(counter),wallet2.getPlayerId(),"DEBIT",String.valueOf(amount));
        assertNotNull(found);
        assertTrue(found.getId().equals(transactionDebit.getId()) );
    }
    
    @Test
    public void testCreateTransaction_WalletNotFound() throws WalletException {
        int amount = 100;
        int counter = transactionIdCounter++;
        
        Mockito.when(walletService.updateWalletAmount(wallet1,String.valueOf(amount),"CREDIT")).thenReturn(wallet1);
        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transactionCredit);
        try {
            Transaction found = transactionService.createTransaction(String.valueOf(counter),notFoundPlayerId,"CREDIT",String.valueOf(amount));
            fail();
        } catch (WalletException ex){
        	assertTrue(ex.getMessage().contains("No wallet found with playerId " + notFoundPlayerId));
        	assertEquals(ex.getErrorCode(),HttpStatus.NOT_FOUND.value());
        }
    }
    
    @Test
    public void testCreateTransaction_DebitFailure() throws WalletException {
        int amount = -100;
        int counter = transactionIdCounter++;
        Mockito.when(walletService.updateWalletAmount(wallet2,String.valueOf(amount),"DEBIT")).
                thenThrow(new WalletException("No sufficient funds in account for withdrawl", HttpStatus.BAD_REQUEST.value()));
        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transactionDebit);
        try {
        	Transaction found = transactionService.createTransaction(String.valueOf(counter),wallet2.getPlayerId(),"DEBIT",String.valueOf(amount));
            fail();
        } catch (WalletException ex){
        	assertTrue(ex.getMessage().contains("No sufficient funds in account for withdrawl"));
            assertEquals(ex.getErrorCode(),HttpStatus.BAD_REQUEST.value());
        }
    }
    
    @Test
    public void testCreateTransaction_AmountNotNumber() throws WalletException {
        String wrongAmount = "INVALID_AMOUNT";
        Mockito.when(walletService.updateWalletAmount(wallet1,String.valueOf(wrongAmount),"CREDIT")).thenReturn(wallet1);
        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transactionCredit);
        int counter = transactionIdCounter++;
        try {
            Transaction found = transactionService.createTransaction(String.valueOf(counter),wallet1.getPlayerId(),"CREDIT",String.valueOf(wrongAmount));
        }catch (WalletException ex){
        	assertTrue(ex.getMessage().contains("Please specify valid amount"));
            assertEquals(ex.getErrorCode(),HttpStatus.BAD_REQUEST.value());
        }
    }

	
}
