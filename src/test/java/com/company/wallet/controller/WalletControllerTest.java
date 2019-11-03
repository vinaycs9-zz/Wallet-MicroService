package com.company.wallet.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.company.wallet.controller.WalletController;
import com.company.wallet.entities.Wallet;
import com.company.wallet.exception.handler.WalletException;
import com.company.wallet.service.WalletService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.mockito.BDDMockito.given;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.*;

/**
 * WalletController tests.
 * @author Vinay Singh
 */
@RunWith(SpringRunner.class)
@WebMvcTest(WalletController.class)
public class WalletControllerTest {

	public static final String PLAYER_ID_1 = "player1";
	public static final String PLAYER_ID_2 = "player2";
	public static final String AMOUNT = "10";

	@Autowired
	private MockMvc mvc;

	@MockBean
	private WalletService service;

	private Wallet wallet;

	@Before
	public void before() {
		wallet = new Wallet(PLAYER_ID_1, new BigDecimal(10));
		wallet.setId(1);
	}

	@Test
	public void testGetAll_whenGetWallet_thenReturnJsonArray() throws Exception {
		List<Wallet> allWallets = Arrays.asList(wallet);

		given(service.findAll()).willReturn(allWallets);

		mvc.perform(get("/api/wallets").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].id", is(wallet.getId())));
	}

	@Test
	public void testGetWalletByUserId_thenReturnJson() throws Exception {

		given(service.findWalletByPlayerId(wallet.getPlayerId())).willReturn((wallet));

		mvc.perform(get("/api/wallets/player/{playerId}", PLAYER_ID_1)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				// .andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$.playerId", is(wallet.getPlayerId())));

	}

	@Test
	public void testCreateWallet_thenReturnJson() throws Exception {

		given(service.createWallet(PLAYER_ID_1, AMOUNT)).willReturn(wallet);
		String validCurrencyJson = "{\"playerId\":\"" + PLAYER_ID_1 + "\",\"amount\":\"" + AMOUNT + "\"}";

		mvc.perform(post("/api/wallets").content(validCurrencyJson).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());
	}

	@Test
	public void testCreateWallet_NoPlayerId() throws Exception {

		String errorMessage = "playerId can not be null and empty";
		given(service.createWallet(null, AMOUNT))
				.willThrow(new WalletException(errorMessage, HttpStatus.BAD_REQUEST.value()));
		String json = "{\"amount\":\"" + AMOUNT + "\"}";

		mvc.perform(post("/api/wallets").content(json).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message", is(errorMessage)))
				.andExpect(jsonPath("$.details", is("uri=/api/wallets")));
	}

	@Test
	public void testCreateWallet_MalformedJson() throws Exception {

		given(service.createWallet(PLAYER_ID_1, AMOUNT)).willReturn(wallet);
		String json = "INAVLID_JSON";

		mvc.perform(post("/api/wallets").content(json).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

}
