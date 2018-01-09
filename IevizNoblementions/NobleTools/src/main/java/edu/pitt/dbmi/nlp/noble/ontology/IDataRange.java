package edu.pitt.dbmi.nlp.noble.ontology;

/**
 * specifies a data range
 * @author tseytlin
 */
public interface IDataRange {
	/**
	 * evaluate this expression against given object.
	 *
	 * @param obj the obj
	 * @return true if object passes this expression, false otherwise
	 */
	public boolean evaluate(Object obj);
}
