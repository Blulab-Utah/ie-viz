package edu.pitt.dbmi.nlp.noble.coder.model;

import edu.pitt.dbmi.nlp.noble.terminology.TerminologyException;

/**
 * interface for processing spannable resourcess.
 *
 * @author tseytlin
 * @param <T> the generic type
 */
public interface Processor<T extends Spannable> {
	
	/**
	 * process a spannable text resources s.a. Document or Sentence
	 *
	 * @param r the r
	 * @return the t
	 * @throws TerminologyException the terminology exception
	 */
	public T process(T r) throws TerminologyException;
	
	/**
	 * get running time in milis for the last called process() method.
	 *
	 * @return the process time
	 */
	public long getProcessTime();
}
