package edu.pitt.dbmi.nlp.noble.terminology;

import java.net.URI;

/**
 * a class that has some description.
 *
 * @author tseytlin
 */
public interface Describable {
	
	/**
	 * get name of an item.
	 *
	 * @return the name
	 */
	public String getName();
	
	/**
	 * get description of an item.
	 *
	 * @return the description
	 */
	public String getDescription();
	
	/**
	 * get version of an item.
	 *
	 * @return the version
	 */
	public String getVersion();
	
	/**
	 * get URI of an item.
	 *
	 * @return the uri
	 */
	public URI getURI();
	
	
	/**
	 * get format.
	 *
	 * @return the format
	 */
	public String getFormat();
	
	
	/**
	 * get location.
	 *
	 * @return the location
	 */
	public String getLocation();
}
