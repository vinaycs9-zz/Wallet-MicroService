package com.company.wallet.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.company.wallet.entities.Transaction;
import com.company.wallet.entities.Wallet;
import com.company.wallet.exception.handler.WalletException;
import com.company.wallet.service.TransactionService;
import com.company.wallet.service.WalletService;

/**
 * Restful controller for managing wallet transactions.
 *
 * @author Vinay Singh
 */
@RestController
@RequestMapping("/api")
public class TransactionController {

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private WalletService walletService;

	/**
	 * Gets all transactions of a given playerId.
	 * @param playerId
	 * @throws WalletException
	 */
	@GetMapping(value = "/wallets/player/{playerId}/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Transaction> getWalletTransactionsByPlayerId(@PathVariable("playerId") String playerId) throws WalletException {
		Wallet wallet = walletService.findWalletByPlayerId(playerId);
		return transactionService.getTransactionsByWallet(wallet);
	}

	/**
     * Creates wallet transaction.
     * <p>
     * Example of  credit transaction JSON body
     * {"transactionId":"1","playerId": "1","transactionType":"CREDIT","amount":"100"}
     *
     * Example of debit transaction JSON body
     * {"transactionId":"1","playerId": "1","transactionType":"DEBIT","amount":"100"}
     * </p>
     * @param dataMap contains input parameters in the following format:
     *                {"transactionId":"1","playerId": "1","transactionType":"CREDIT","amount":"100"}
     * @return created transaction URI in response header.
     * @throws WalletException when couldn't create transaction (e.g. transactionId not unique, not enough funds on wallet balance, etc.)
     */
	@PostMapping(value = "/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> createWalletTransaction(@RequestBody Map<String, String> dataMap,
			UriComponentsBuilder ucBuilder) throws WalletException {
		String transactionId = dataMap.get("transactionId");
		String playerId = dataMap.get("playerId");
		String transactionType = dataMap.get("transactionType");
		String amount = dataMap.get("amount");
		Transaction transaction = transactionService.createTransaction(transactionId, playerId, transactionType,
				amount);

		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(ucBuilder.path("/api/transactions/{id}").buildAndExpand(transaction.getId()).toUri());

		return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
	}

	/**
    * Gets transaction details for given id.
    * @param id
    * @return transaction
    * @throws NumberFormatException
    * @throws WalletException
    */
	@GetMapping(value = "/transactions/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Transaction getTransactionById(@PathVariable("id") String id) throws NumberFormatException, WalletException {
		Transaction transaction = transactionService.findTransactionById(Integer.valueOf(id));
		return transaction;
	}
}
