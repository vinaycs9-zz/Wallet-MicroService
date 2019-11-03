package com.company.wallet.exception.handler;

/**
 * Custom wallet exception.
 *
 * @author Vinay Singh
 */
public class WalletException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -162339027642481L;
	/**
	 * 
	 */
	
	private int errorCode;

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public WalletException(String message, int errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	public WalletException() {
		super();

	}

	public WalletException(String message) {
		super(message);
	}

	public WalletException(Exception e) {
		super(e);
	}
}
