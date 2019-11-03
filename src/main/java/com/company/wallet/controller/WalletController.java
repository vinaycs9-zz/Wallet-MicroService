package com.company.wallet.controller;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import com.company.wallet.entities.Wallet;
import com.company.wallet.exception.handler.WalletException;
import com.company.wallet.service.WalletService;

/**
 * Restful controller for managing wallets.
 *  @author Vinay Singh
 */
@RestController
@RequestMapping("/api")
public class WalletController {

	@Autowired
	private WalletService walletService;

	/**
    * Get all wallets.
    * @return list of wallets
    * @throws WalletException
    */
	@GetMapping(value = "/wallets", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Wallet> getAll() throws WalletException {
		return walletService.findAll();
	}

	/**
     * Creates new wallet.
     * In the form {"playerId":"1",amount":"200"}
     * @param dataHashMap Expecting playerId and amount to be set, e. g. {"playerId":"1",amount":"200"}. Expects hashmap in JSON format.
     * @param ucBuilder for creating URI of created wallet.
     * @return created wallet URI in response header
     * @throws WalletException when failed to create wallet
     */
	@PostMapping(value = "/wallets", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> createWallet(@RequestBody HashMap<String, String> dataHashMap,
			UriComponentsBuilder ucBuilder) throws WalletException {
		Wallet wallet = walletService.createWallet(dataHashMap.get("playerId"), dataHashMap.get("amount"));

		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(
				ucBuilder.path("/api/wallets/player/{playerId}").buildAndExpand(wallet.getPlayerId()).toUri());
		return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
	}

	/**
	 * Get wallet details (balance, creation time etc) for a player.
	 * @param playerId
	 * @return wallet
	 * @throws WalletException
	 */
	@GetMapping(value = "/wallets/player/{playerId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Wallet getWalletByPlayerId(@PathVariable("playerId") String playerId) throws WalletException {
		Wallet wallet = walletService.findWalletByPlayerId(playerId);
		return wallet;
	}

}
