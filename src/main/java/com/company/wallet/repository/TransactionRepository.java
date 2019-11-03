package com.company.wallet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.company.wallet.entities.Transaction;
import com.company.wallet.entities.Wallet;

/**
 * Transaction JPA repository Generates SQL queries to access the database to
 * manage Transaction entities.
 * 
 * @author Vinay Singh
 */
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
	List<Transaction> findByWalletOrderByUpdatedTimeDesc(Wallet wallet);

	Transaction findOneById(Integer Id);

	List<Transaction> findByWallet(Wallet wallet);
}
