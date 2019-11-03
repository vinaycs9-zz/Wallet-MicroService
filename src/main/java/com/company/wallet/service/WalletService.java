package com.company.wallet.service;

import java.util.List;

import com.company.wallet.entities.Wallet;
import com.company.wallet.exception.handler.WalletException;

/**
 * Service for managing wallets.
 * @author Vinay Singh
 */
public interface WalletService {
	public Wallet findWalletByPlayerId(String playerId) throws WalletException;

	public Wallet createWallet(String playerId, String startingAmount) throws WalletException;

	public Wallet updateWalletAmount(Wallet wallet, String amount, String transactionType) throws WalletException;

	public List<Wallet> findAll() throws WalletException;
}
