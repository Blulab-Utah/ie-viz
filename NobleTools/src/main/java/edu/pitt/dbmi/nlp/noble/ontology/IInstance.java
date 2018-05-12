package edu.pitt.dbmi.nlp.noble.ontology;


/**
 * This class describes an ontology instance.
 *
 * @author tseytlin
 */
public interface IInstance extends IResource {
	
	/**
	 * add a direct type to this instance.
	 *
	 * @param cls the cls
	 */
	public void addType(IClass cls);
	
	/**
	 * remove a type from this instance.
	 *
	 * @param cls the cls
	 */
	public void removeType(IClass cls);		
	
	/**
	 * get all types of a given instance.
	 *
	 * @return the types
	 */
	public IClass [] getTypes();
	
	/**
	 * get direct types of a given instance.
	 *
	 * @return the direct types
	 */
	public IClass [] getDirectTypes();
	
	/**
	 * Is given instance has a type of cls.
	 *
	 * @param cls the cls
	 * @return true, if successful
	 */
	public boolean hasType(IClass cls);
	
	
	/**
	 * get values of the given properties.
	 *
	 * @param prop the prop
	 * @param includeSubPropeties - include values for subproperties
	 * @return list of values
	 */
	public Object [] getPropertyValues(IProperty prop, boolean includeSubPropeties);
}
