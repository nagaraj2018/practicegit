package com.training.exception;

import java.io.Serializable;
import java.util.List;

import javax.ws.rs.core.Response.Status;



/**
 * Base exception class for all the ecommerce application related exceptions.
 * 
 * An application exception will be thrown from the ecommerce application whenever a retrieable exception occurs.
 */
@javax.ejb.ApplicationException(rollback = true)
public class ApplicationException extends Exception implements Serializable {
	
	private static final long serialVersionUID = 4754526254040645906L;

	private String message;
	private Status status = Status.FORBIDDEN;
	private List<String> errorCodes;
	private String errorCode;
	private Throwable parentException;

	/**
	 * {@inheritDoc}
	 */
	public ApplicationException() {
		super();
	}

	/**
	 * Set message and status.
	 * 
	 * @param message
	 *            - Error Message
	 * @param status
	 *            - Commonly used status codes defined by HTTP.
	 */
	public ApplicationException(String message, Status status) {
		super(message);
		this.message = message;
		this.status = status;
	}

	/**
	 * Set message, status and exception.
	 * 
	 * @param message
	 *            - Error Message
	 * @param status
	 *            - Commonly used status codes defined by HTTP.
	 * @param th
	 *            - Throwable implementation.
	 */
	public ApplicationException(String message, Status status, Throwable th) {
		super(message, th);
		this.message = message;
		this.status = status;
		this.parentException = th;
	}

	/**
	 * Set message, status and ErrorCode.
	 * 
	 * @param message
	 *            - Error Message
	 * @param status
	 *            - Commonly used status codes defined by HTTP.
	 * @param errorCodes
	 *            - Collection of Error Codes, holds error code values.
	 */
	public ApplicationException(String message, Status status, List<String> errorCodes) {
		super(message);
		this.message = message;
		this.status = status;
		this.errorCodes = errorCodes;
	}

	/**
	 * Set message, status, ErrorCode and exception .
	 * 
	 * @param message
	 *            - Error Message
	 * @param status
	 *            - Commonly used status codes defined by HTTP.
	 * @param errorCodes
	 *            - Collection of Error Codes, holds error code values.
	 * @param th
	 *            - Throwable implementation.
	 */
	public ApplicationException(String message, Status status, List<String> errorCodes, Throwable th) {
		super(message, th);
		this.message = message;
		this.status = status;
		this.errorCodes = errorCodes;
		this.parentException = th;
	}

	/**
	 * Set message and ErrorCode.
	 * 
	 * @param message
	 *            - Error Message
	 * @param errorCodes
	 *            -Collection of Error Codes, holds error code.
	 */
	public ApplicationException(String message, List<String> errorCodes) {
		super(message);
		this.message = message;
		this.errorCodes = errorCodes;
	}

	/**
	 * Set message, status and ErrorCode.
	 * 
	 * @param message
	 *            - Error Message
	 * @param status
	 *            - Commonly used status codes defined by HTTP.
	 * @param errorCodes
	 *            - Error Code, holds error code value.
	 */
	public ApplicationException(String message, Status status, String errorCode) {
		super(message);
		this.message = message;
		this.status = status;
		this.errorCode = errorCode;
	}

	/**
	 * Set message and ErrorCode.
	 * 
	 * @param message
	 *            - Error Message
	 * @param errorCodes
	 *            - Error Codes, holds error code value.
	 */
	public ApplicationException(String message, String errorCode) {
		super(message);
		this.message = message;
		this.errorCode = errorCode;
	}

	/**
	 * Set message and ErrorCode.
	 * 
	 * @param message
	 *            - Error Message
	 * @param errorCodes
	 *            - Error Codes, holds error code value.
	 * @param th
	 *            - Throwable implementation.
	 */
	public ApplicationException(String message, String errorCode, Throwable th) {
		super(message);
		this.message = message;
		this.errorCode = errorCode;
		this.parentException = th;
	}

	/**
	 * {@inheritDoc}
	 */
	public ApplicationException(String message) {
		super(message);
		this.message = message;
	}

	/**
	 * {@inheritDoc}
	 */
	public ApplicationException(Throwable cause) {
		super(cause);
	}

	/**
	 * {@inheritDoc}
	 */
	public ApplicationException( String msg,Throwable cause) {
		super(msg,cause);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMessage() {
		return message;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public List<String> getErrorCodes() {
		return errorCodes;
	}

	public void setErrorCodes(List<String> errorCodes) {
		this.errorCodes = errorCodes;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		//please don't change the formate of the toString(). As this format is being used in commons  helper class.
		if (null != parentException) {
			return errorCode
					+ " : " + message + " : " + parentException.getMessage();
		}
		else {
			return errorCode
					+ " : " + message;
		}

	}
}