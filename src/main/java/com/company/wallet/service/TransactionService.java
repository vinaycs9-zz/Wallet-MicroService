package com.company.wallet.service;

import java.util.List;

import com.company.wallet.entities.Transaction;
import com.company.wallet.entities.Wallet;
import com.company.wallet.exception.handler.WalletException;

/**
 * Service for managing transactions.
 * 
 * @author Vinay Singh
 */
public interface TransactionService {
	public List<Transaction> getTransactionsByWallet(Wallet wallet) throws WalletException;

	public Transaction createTransaction(String transactionId, String playerId, String transactionType, String amount)
			throws WalletException;

	public Transaction findTransactionById(Integer Id) throws WalletException;
}
