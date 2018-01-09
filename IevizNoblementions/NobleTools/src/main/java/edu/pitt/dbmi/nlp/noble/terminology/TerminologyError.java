package edu.pitt.dbmi.nlp.noble.terminology;

/**
 * exception caused for whatever reason, unhandled
 * wraps other exceptions.
 *
 * @author tseytlin
 */
public class TerminologyError extends RuntimeException {
	
	/**
	 * Instantiates a new terminology error.
	 *
	 * @param reason the reason
	 */
	public TerminologyError(String reason){
		super(reason);
	}
	
	/**
	 * Instantiates a new terminology error.
	 *
	 * @param reason the reason
	 * @param cause the cause
	 */
	public TerminologyError(String reason, Throwable cause){
		super(reason,cause);
	}
}