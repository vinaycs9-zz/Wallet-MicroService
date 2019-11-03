package com.company.wallet.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
/**
 *  Exception handler to create custom error messages for user.
 *
 *  @author Vinay Singh
 */
public class ExceptionsHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(value = { WalletException.class })
	protected ResponseEntity<ErrorDetails> handleWalletException(WalletException ex, WebRequest request) {
		HttpStatus status = HttpStatus.valueOf(ex.getErrorCode());
		ErrorDetails errorDetails = new ErrorDetails(ex.getMessage(), request.getDescription(false));
		return new ResponseEntity<>(errorDetails, status);
	}

}
