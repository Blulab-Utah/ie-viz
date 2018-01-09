package edu.pitt.dbmi.nlp.noble.ontology;

/**
 * exception caused for whatever reason, unhandled
 * wraps other exceptions.
 *
 * @author tseytlin
 */
public class IOntologyError extends RuntimeException {
	
	/**
	 * Instantiates a new i ontology error.
	 *
	 * @param reason the reason
	 */
	public IOntologyError(String reason){
		super(reason);
	}
	
	/**
	 * Instantiates a new i ontology error.
	 *
	 * @param reason the reason
	 * @param cause the cause
	 */
	public IOntologyError(String reason, Throwable cause){
		super(reason,cause);
	}
}