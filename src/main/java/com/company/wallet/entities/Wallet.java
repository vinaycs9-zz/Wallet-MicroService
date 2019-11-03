package com.company.wallet.entities;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 *  Wallet entity.
 *
 *  @author Vinay Singh
 */
@Entity
@Table(name = "wallet")
public class Wallet {

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@NotNull(message = "Player Id must be provided")
	@NotBlank(message = "Player Id must not be empty")
	@Column(name = "player_id", unique = true, nullable = false)
	private String playerId;

	@Min(0)
	@Column(name = "balance", nullable = false)
	@NotNull(message = "Wallet balance must be provided")	
	private BigDecimal balance;

	@Column(name = "created_time")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;

	@Column(name = "updated_time")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedTime;

	@JsonIgnore
	@OneToMany(mappedBy = "wallet", fetch = FetchType.LAZY)
	private List<Transaction> transactions;

	public Wallet() {
	}

	public Wallet(String playerId, BigDecimal balance) {
		this.playerId = playerId;
		this.balance = balance;
		this.createdTime = new Date();
		this.updatedTime = new Date();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public Date getUpdatedTime() {
		return updatedTime;
	}

	public void setUpdatedTime(Date updatedTime) {
		this.updatedTime = updatedTime;
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}

	public void setTransactions(List<Transaction> transactions) {
		this.transactions = transactions;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

}
