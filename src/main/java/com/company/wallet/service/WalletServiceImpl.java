package com.company.wallet.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.company.wallet.entities.Wallet;
import com.company.wallet.exception.handler.WalletException;
import com.company.wallet.repository.WalletRepository;
import com.company.wallet.utils.TransactionType;

/**
 * Service implementation for managing wallets.
 * @author Vinay Singh
 */
@Service
public class WalletServiceImpl implements WalletService {

	@Autowired
	private WalletRepository walletRepository;

	@Transactional(rollbackFor = WalletException.class)
	@Override
	public Wallet findWalletByPlayerId(String playerId) throws WalletException {
		Wallet wallet = walletRepository.findWalletByPlayerId(playerId);
		
		if (wallet == null) {
			throw new WalletException("No wallet found with playerId " + playerId, HttpStatus.NOT_FOUND.value());
		}
		
		return wallet;
	}

	/**
     * Creates wallet based on playerId and startingAmount.
     * @param playerId 
     * @param startingAmount
     * @return created wallet
     * @throws WalletException
     */
	@Transactional(rollbackFor = WalletException.class)
	@Override
	public Wallet createWallet(String playerId, String startingAmount) throws WalletException {
		if (StringUtils.isBlank(playerId)) {
			throw new WalletException("playerId can not be null and empty", HttpStatus.BAD_REQUEST.value());
		}
		
		if (StringUtils.isBlank(startingAmount)) {
			throw new WalletException("amount can not be null and empty", HttpStatus.BAD_REQUEST.value());
		}

		try {
			BigDecimal bigDecimal = new BigDecimal(startingAmount).abs();
			return walletRepository.save(new Wallet(playerId, bigDecimal));
		} catch (NumberFormatException ex) {
			throw new WalletException("Please specify valid amount", HttpStatus.BAD_REQUEST.value());
		}
	}

	/**
     * Updates wallet balance. Before updating balance it checks if there is enough funds on wallet balance.
     * If there is not enough funds, throws WalletException
     * If transactionType is CREDIT, takes absolute amount from  @param amount  and adds it to wallet balance.
     * If transactionType is DEBIT, takes absolute amount from  @param amount  and subtracts it from wallet balance.
     *
     * Set isolation = Isolation.SERIALIZABLE in order to avoid concurrency issues.
     * @param wallet
     * @param amount
     * @param transactionType CREDIT or DEBIT
     * @return updated wallet
     * @throws WalletException if couldn't update wallet balance, e.g. not enough funds.
     */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, rollbackFor = WalletException.class)	
	@Override
	public Wallet updateWalletAmount(Wallet wallet, String amount, String transactionType) throws WalletException {
		BigDecimal transactionAmount;

		try {
			if (transactionType.equals(TransactionType.CREDIT.name())) {
				transactionAmount = new BigDecimal(amount).abs();
			} else {
				transactionAmount = new BigDecimal(amount).abs().negate();
				if (wallet.getBalance().compareTo(transactionAmount.abs()) <= 0) {
					throw new WalletException("No sufficient funds in account for withdrawl",
							HttpStatus.BAD_REQUEST.value());
				}
			}

			// update wallet
			wallet.setBalance(wallet.getBalance().add(transactionAmount));
			wallet.setUpdatedTime(new Date());

			return walletRepository.save(wallet);
		} catch (NumberFormatException ex) {
			throw new WalletException("Please specify valid amount", HttpStatus.BAD_REQUEST.value());
		}
	}
	
	@Transactional(rollbackFor = WalletException.class)
	@Override
	public List<Wallet> findAll() throws WalletException {
		List<Wallet> walletList = walletRepository.findAllByOrderByIdAsc();

		if (walletList == null || walletList.isEmpty()) {
			throw new WalletException("No wallet found", HttpStatus.NOT_FOUND.value());
		}
		
		return walletList;
	}

}
