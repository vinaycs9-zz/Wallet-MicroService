package com.company.wallet.entities;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.company.wallet.utils.TransactionType;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 *  Transaction entity.
 *
 *  @author Vinay Singh
 */
@Entity
@Table(name = "transaction")
public class Transaction {
	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@NotBlank(message = "Transaction Id must not be empty")
	@NotNull(message = "Transaction Id must be provided")
	@Column(name = "transaction_id", unique = true, nullable = false)
	private String transactionId;

	@NotNull(message = "Transaction Type must be provided")
	@Column(name = "transaction_type", nullable = false)
	private TransactionType transactionType;

	@NotNull(message = "Transaction amount must be provided")
	@Column(name = "amount", nullable = false)
	private BigDecimal amount;

	@JsonIgnore
	@NotNull(message = "Transaction wallet must be provided")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "wallet_id")
	private Wallet wallet;

	@Column(name = "updated_time")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedTime;

	public Transaction() {
	}

	public Transaction(String transactionId, TransactionType transactionType, BigDecimal amount, Wallet wallet) {
		this.transactionId = transactionId;
		this.transactionType = transactionType;
		this.amount = amount;
		this.wallet = wallet;
		this.updatedTime = new Date();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public TransactionType getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(TransactionType transactionType) {
		this.transactionType = transactionType;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public Wallet getWallet() {
		return wallet;
	}

	public void setWallet(Wallet wallet) {
		this.wallet = wallet;
	}

	public Date getUpdatedTime() {
		return updatedTime;
	}

	public void setUpdatedTime(Date updatedTime) {
		this.updatedTime = updatedTime;
	}

	
}
