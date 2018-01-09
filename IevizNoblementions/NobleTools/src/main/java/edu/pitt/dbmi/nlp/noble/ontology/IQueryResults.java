package edu.pitt.dbmi.nlp.noble.ontology;

import java.util.*;

/**
 * The Interface IQueryResults.
 */
public interface IQueryResults extends Iterator {

	/**
	 * get list of variables that were returned.
	 *
	 * @return the variables
	 */
	public String [] getVariables();
	
	/**
	 * get next row.
	 *
	 * @return the map
	 */
	public Map next();
}
