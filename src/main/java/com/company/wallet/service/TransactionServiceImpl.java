package com.company.wallet.service;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.company.wallet.entities.Transaction;
import com.company.wallet.entities.Wallet;
import com.company.wallet.exception.handler.WalletException;
import com.company.wallet.repository.TransactionRepository;
import com.company.wallet.utils.TransactionType;

/**
 * Service Implementation for managing transactions.
 * 
 * @author Vinay Singh
 */
@Service
public class TransactionServiceImpl implements TransactionService{

	@Autowired
    private WalletService walletService;
	
	@Autowired
    private TransactionRepository transactionRepository;
	
	/**
	 * Gets all transactions for given wallet.
	 * 
	 * @param wallet
	 * @return transaction list
	 * @throws WalletException
	 */
	@Transactional(rollbackFor = WalletException.class)
	@Override
	public List<Transaction> getTransactionsByWallet(Wallet wallet) throws WalletException {
		List<Transaction> transactionList = transactionRepository.findByWalletOrderByUpdatedTimeDesc(wallet);
		
		if (transactionList == null || transactionList.isEmpty()) {
			throw new WalletException("Transaction List for player " + wallet.getPlayerId() + " not found", HttpStatus.NOT_FOUND.value());
		}
		
		return transactionList;
	}

	/**
     * Creates transaction for wallet.
     * If there is not enough funds on wallet balance, throws WalletException
     * If transactionType='CREDIT' (credit transaction), takes absolute amount from  @param amount  and adds it to wallet balance.
     * If transactionType='DEBIT' (debit transaction), takes absolute amount from  @param amount  and subtracts it from wallet balance.
     * transactionId should be unique.
     * 
     * Set isolation = Isolation.SERIALIZABLE in order to avoid concurrency issues (in case of deploying application to multiple hosts)
     *
     * @param transactionId unique transactionId 
     * @param playerId valid playerId
     * @param transactionType valid transactionType - 'CREDIT' or 'DEBIT'
     * @param amount transaction amount
     * @return created transaction
     * @throws WalletException if couldn't create transaction
     */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, rollbackFor = WalletException.class)
	@Override
	public Transaction createTransaction(String transactionId, String playerId,
			String transactionType, String amount) throws WalletException {

		if(StringUtils.isBlank(transactionId)) {
			throw new WalletException("transactionId can not be null and empty", HttpStatus.BAD_REQUEST.value());
		}
		
		if (StringUtils.isBlank(playerId)) {
			throw new WalletException("playerId can not be null and empty", HttpStatus.BAD_REQUEST.value());
		}
		
		if(StringUtils.isBlank(amount)) {
			throw new WalletException("amount can not be null and empty", HttpStatus.BAD_REQUEST.value());
		}
		
		if(StringUtils.isBlank(transactionType)) {
			throw new WalletException("transactionType can not be null and empty", HttpStatus.BAD_REQUEST.value());
		}
		
		try {
			TransactionType.valueOf(transactionType);
		} catch (IllegalArgumentException ex) {
			throw new WalletException("Please specify valid transactionType: " + TransactionType.CREDIT.name() + " OR "
					+ TransactionType.DEBIT.name(), HttpStatus.BAD_REQUEST.value());
		}
		
		try {
			// Check wallet is present
			Wallet wallet = walletService.findWalletByPlayerId(playerId);			
			// walletService checks if wallet exists or not	
			
			wallet = walletService.updateWalletAmount(wallet, amount, transactionType);
			Transaction transaction = new Transaction(transactionId, TransactionType.valueOf(transactionType),
					new BigDecimal(amount), wallet);
			return transactionRepository.save(transaction);
		} catch (NumberFormatException ex) {
			throw new WalletException("Please specify valid amount", HttpStatus.BAD_REQUEST.value());
		}
	}

	/**
     * Gets Transaction by id.
     * @param id
     * @return transaction
     * @throws WalletException
     */
	@Transactional(rollbackFor = WalletException.class)
	@Override
	public Transaction findTransactionById(Integer id) throws WalletException {
		Transaction transaction = transactionRepository.findOneById(id);

		if (transaction == null) {
			throw new WalletException("Transaction with id " + id + " not found", HttpStatus.NOT_FOUND.value());
		}

		return transaction;
	}

}
