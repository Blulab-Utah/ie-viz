package edu.pitt.dbmi.nlp.noble.ontology;


/**
 * exception caused for whatever reason
 * wraps other exceptions.
 *
 * @author tseytlin
 */
public class IOntologyException extends Exception {
	
	/**
	 * Instantiates a new i ontology exception.
	 *
	 * @param reason the reason
	 */
	public IOntologyException(String reason){
		super(reason);
	}
	
	/**
	 * Instantiates a new i ontology exception.
	 *
	 * @param reason the reason
	 * @param cause the cause
	 */
	public IOntologyException(String reason, Throwable cause){
		super(reason,cause);
	}
}
