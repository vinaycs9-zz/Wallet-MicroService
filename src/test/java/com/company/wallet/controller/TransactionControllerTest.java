package com.company.wallet.controller;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.company.wallet.controller.TransactionController;
import com.company.wallet.entities.Transaction;
import com.company.wallet.entities.Wallet;
import com.company.wallet.exception.handler.WalletException;
import com.company.wallet.service.TransactionService;
import com.company.wallet.service.WalletService;
import com.company.wallet.utils.TransactionType;
import com.google.gson.GsonBuilder;

/**
 * TransactionController tests.
 * @author Vinay Singh
 */
@RunWith(SpringRunner.class)
@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

	public static final String PLAYER_ID_1 = "player1";
	public static final String PLAYER_ID_2 = "player2";
	public static final String AMOUNT = "10";

	@Autowired
	private MockMvc mvc;

	@MockBean
	private WalletService walletService;

	@MockBean
	private TransactionService service;

	private Wallet wallet;
	private Transaction transactionCredit;
	private TransactionType typeCredit;
	static int transactionIdCounter = 1;

	@Before
	public void before() {
		wallet = new Wallet(PLAYER_ID_1, new BigDecimal(10));
		wallet.setId(1);
		typeCredit = TransactionType.CREDIT;
		transactionCredit = new Transaction(String.valueOf(transactionIdCounter++), typeCredit, new BigDecimal(20),
				wallet);
		transactionCredit.setId(5);
	}

	@Test
	public void testGetWalletTransactionsById_whenGetTransaction_thenReturnJsonArray() throws Exception {
		List<Transaction> allTransactions = Arrays.asList(transactionCredit);

		given(walletService.findWalletByPlayerId(PLAYER_ID_1)).willReturn(wallet);
		given(service.getTransactionsByWallet(wallet)).willReturn(allTransactions);

		mvc.perform(
				get("/api/wallets/player/{playerId}/transactions", PLAYER_ID_1).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id", is(transactionCredit.getId())))
				.andExpect(jsonPath("$[0].transactionId", is(transactionCredit.getTransactionId())))
				.andExpect(jsonPath("$[0].transactionType", is(transactionCredit.getTransactionType().name())))
				.andExpect(jsonPath("$[0].amount", is(transactionCredit.getAmount().intValue())));
	}

	@Test
	public void testCreateTransaction_thenReturnJson() throws Exception {
		Map<String, String> dataMap = new HashMap<>();
		dataMap.put("transactionId", transactionCredit.getTransactionId());
		dataMap.put("playerId", transactionCredit.getWallet().getPlayerId());
		dataMap.put("transactionType", transactionCredit.getTransactionType().name());
		dataMap.put("amount", transactionCredit.getAmount().toString());

		given(service.createTransaction(dataMap.get("transactionId"), dataMap.get("playerId"),
				dataMap.get("transactionType"), dataMap.get("amount"))).willReturn(transactionCredit);
		String validJson = new GsonBuilder().create().toJson(dataMap);

		mvc.perform(post("/api/transactions").content(validJson).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());
	}

	@Test
	public void testCreateTransaction_NoTransactionId() throws Exception {

		Map<String, String> dataMap = new HashMap<>();
		// dataMap.put("transactionId", transactionCredit.getTransactionId());
		dataMap.put("playerId", transactionCredit.getWallet().getPlayerId());
		dataMap.put("transactionType", transactionCredit.getTransactionType().name());
		dataMap.put("amount", transactionCredit.getAmount().toString());

		String errorMessage = "transactionId can not be null and empty";

		given(service.createTransaction(null, dataMap.get("playerId"), dataMap.get("transactionType"),
				dataMap.get("amount"))).willThrow(new WalletException(errorMessage, HttpStatus.BAD_REQUEST.value()));
		String json = new GsonBuilder().create().toJson(dataMap);

		mvc.perform(post("/api/transactions").content(json).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message", is(errorMessage)))
				.andExpect(jsonPath("$.details", is("uri=/api/transactions")));
	}

	@Test
	public void testCreateTransaction_NoPlayerId() throws Exception {

		Map<String, String> dataMap = new HashMap<>();
		dataMap.put("transactionId", transactionCredit.getTransactionId());
		// dataMap.put("playerId", transactionCredit.getWallet().getPlayerId());
		dataMap.put("transactionType", transactionCredit.getTransactionType().name());
		dataMap.put("amount", transactionCredit.getAmount().toString());

		String errorMessage = "playerId can not be null and empty";

		given(service.createTransaction(dataMap.get("transactionId"), null, dataMap.get("transactionType"),
				dataMap.get("amount"))).willThrow(new WalletException(errorMessage, HttpStatus.BAD_REQUEST.value()));
		String json = new GsonBuilder().create().toJson(dataMap);

		mvc.perform(post("/api/transactions").content(json).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message", is(errorMessage)))
				.andExpect(jsonPath("$.details", is("uri=/api/transactions")));
	}

	@Test
	public void testCreateTransaction_NoTransactionType() throws Exception {

		Map<String, String> dataMap = new HashMap<>();
		dataMap.put("transactionId", transactionCredit.getTransactionId());
		dataMap.put("playerId", transactionCredit.getWallet().getPlayerId());
		// dataMap.put("transactionType",
		// transactionCredit.getTransactionType().name());
		dataMap.put("amount", transactionCredit.getAmount().toString());

		String errorMessage = "transactionType can not be null and empty";

		given(service.createTransaction(dataMap.get("transactionId"), dataMap.get("playerId"), null,
				dataMap.get("amount"))).willThrow(new WalletException(errorMessage, HttpStatus.BAD_REQUEST.value()));
		String json = new GsonBuilder().create().toJson(dataMap);

		mvc.perform(post("/api/transactions").content(json).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message", is(errorMessage)))
				.andExpect(jsonPath("$.details", is("uri=/api/transactions")));
	}

	@Test
	public void testCreateTransaction_NoAmount() throws Exception {

		Map<String, String> dataMap = new HashMap<>();
		dataMap.put("transactionId", transactionCredit.getTransactionId());
		dataMap.put("playerId", transactionCredit.getWallet().getPlayerId());
		dataMap.put("transactionType", transactionCredit.getTransactionType().name());
		// dataMap.put("amount", transactionCredit.getAmount().toString());

		String errorMessage = "amount can not be null and empty";

		given(service.createTransaction(dataMap.get("transactionId"), dataMap.get("playerId"),
				dataMap.get("transactionType"), null))
						.willThrow(new WalletException(errorMessage, HttpStatus.BAD_REQUEST.value()));
		String json = new GsonBuilder().create().toJson(dataMap);

		mvc.perform(post("/api/transactions").content(json).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message", is(errorMessage)))
				.andExpect(jsonPath("$.details", is("uri=/api/transactions")));
	}

	@Test
	public void testCreateTransaction_InvalidTransactionType() throws Exception {
		Map<String, String> dataMap = new HashMap<>();
		dataMap.put("transactionId", transactionCredit.getTransactionId());
		dataMap.put("playerId", transactionCredit.getWallet().getPlayerId());
		dataMap.put("transactionType", "INVALID");
		dataMap.put("amount", transactionCredit.getAmount().toString());

		String errorMessage = "Please specify valid transactionType: \" + TransactionType.CREDIT.name() + \" OR \"\n"
				+ "					+ TransactionType.DEBIT.name()";

		given(service.createTransaction(dataMap.get("transactionId"), dataMap.get("playerId"),
				dataMap.get("transactionType"), dataMap.get("amount")))
						.willThrow(new WalletException(errorMessage, HttpStatus.BAD_REQUEST.value()));
		String json = new GsonBuilder().create().toJson(dataMap);

		mvc.perform(post("/api/transactions").content(json).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message", is(errorMessage)))
				.andExpect(jsonPath("$.details", is("uri=/api/transactions")));
	}

}
