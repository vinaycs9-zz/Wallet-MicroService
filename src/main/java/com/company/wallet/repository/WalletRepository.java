package com.company.wallet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.company.wallet.entities.Wallet;

/**
 * Wallet JPA repository.
 * Generates SQL queries to access the database to manage Wallet entities.
 * @author Vinay Singh
 */
public interface WalletRepository extends JpaRepository<Wallet, Integer>{
	Wallet findWalletByPlayerId(String playerId);
	List<Wallet> findAllByOrderByIdAsc();	
}
