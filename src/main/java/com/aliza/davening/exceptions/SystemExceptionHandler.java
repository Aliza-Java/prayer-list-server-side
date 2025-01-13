package com.aliza.davening.exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailParseException;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.aliza.davening.SchemeValues;

import jakarta.mail.SendFailedException;
import jakarta.mail.internet.AddressException;

@ControllerAdvice // Directing exceptions to be handled here before handling defaultively
public class SystemExceptionHandler {

	//commenting Throwable.class to enable developer to see error sources.  
	//After project is completed with all forseeable cases taken care of, allow Throwable to make errors nice and readable
	
	// Catch-All - if does not fit any of the extended methods to Throwable, will
	// default to this one. 
	//@ExceptionHandler(Throwable.class)
	//public ResponseEntity<Object> handleThrowable(Throwable e) {
	//	ApiError apiError = new ApiError("SERVER_ERROR",
	//			"We are sorry, but something wrong happened. Please contact the admin.");
	//	return new ResponseEntity<Object>(apiError, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
	//}

	@ExceptionHandler({ DaveningSystemException.class, ReorderCategoriesException.class })
	public ResponseEntity<Object> handleGeneralException(DaveningSystemException e) {
		ApiError apiError = new ApiError("SERVER_ERROR",
				"The Davening System experienced an error.  Please contact the admin or try again.");
		return new ResponseEntity<Object>(apiError, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(ObjectNotFoundException.class)
	public ResponseEntity<Object> handleObjectNotFoundException(ObjectNotFoundException e) {
		ApiError apiError = new ApiError("OBJECT_NOT_FOUND_ERROR", e.getMessage());
		return new ResponseEntity<Object>(apiError, new HttpHeaders(), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(DatabaseException.class)
	public ResponseEntity<Object> handleDatabaseException(DatabaseException e) {
		ApiError apiError = new ApiError("DATABASE_EXCEPTION", e.getMessage());
		return new ResponseEntity<Object>(apiError, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler({EmailException.class, MailParseException.class, AddressException.class, SendFailedException.class} )
	public ResponseEntity<Object> handleEmailException(EmailException e) {
		ApiError apiError = new ApiError("EMAIL_EXCEPTION", e.getMessage());
		return new ResponseEntity<Object>(apiError, new HttpHeaders(), HttpStatus.SERVICE_UNAVAILABLE);
	}

	@ExceptionHandler({ EmptyInformationException.class, NoRelatedEmailException.class }) 
	// Specifying which classes that this method handles
	public ResponseEntity<Object> handleEmptyInformationException(EmptyInformationException e) { 
	// Send the more general Exception or root of multiple ones.
		ApiError apiError = new ApiError("EMPTY_INFORMATION", e.getMessage());
		return new ResponseEntity<Object>(apiError, new HttpHeaders(), HttpStatus.NO_CONTENT);
	}
	
	
	@ExceptionHandler(JpaObjectRetrievalFailureException.class) 
	public ResponseEntity<Object> handleDbRetrievalException(JpaObjectRetrievalFailureException e) { 
		ApiError apiError = new ApiError("DATABASE_RETRIEVAL_EXCEPTION", e.getMessage());
		return new ResponseEntity<Object>(apiError, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler({LoginException.class, PermissionException.class})
	public ResponseEntity<Object> handleLoginException(Exception e) {
		ApiError apiError = new ApiError("LOGIN_ERROR", e.getMessage());
		if(e.getMessage().equals(SchemeValues.getNotAdminsEmailMessage())) {
			apiError.setCode("NOT_ADMIN_EMAIL");
		}
		return new ResponseEntity<Object>(apiError, new HttpHeaders(), HttpStatus.UNAUTHORIZED);
	}

}
