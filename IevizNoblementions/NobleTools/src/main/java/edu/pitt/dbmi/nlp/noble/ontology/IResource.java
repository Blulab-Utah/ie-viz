package edu.pitt.dbmi.nlp.noble.ontology;

import java.net.*;
import java.util.Properties;

import edu.pitt.dbmi.nlp.noble.terminology.Describable;

/**
 * This class describes a generic resource that is part of ontology.
 *
 * @author tseytlin
 */
public interface IResource extends Describable, Comparable<IResource> {
	
	/**
	 * get description.
	 *
	 * @return the description
	 */
	public String getDescription();
	
	/**
	 * set description.
	 *
	 * @param text the new description
	 */
	public void setDescription(String text);
	
	/**
	 * get name of this resource.
	 *
	 * @return the name
	 */
	public String getName();
	
	/**
	 * called when this resource is no longer required. Implementations should clear any 
	 * associated resources 
	 */
	public void dispose();
	
	/**
	 * get version of this resource.
	 *
	 * @return the version
	 */
	public String getVersion();
	
	
	/**
	 * get URI of this resource.
	 *
	 * @return the uri
	 */
	public URI getURI();
	
	/**
	 * get namespace.
	 *
	 * @return the name space
	 */
	public String getNameSpace();
	
	/**
	 * get prefix for this resource (shortcut for a namespace) .
	 *
	 * @return the prefix
	 */
	public String getPrefix();
	
	/**
	 * get all properties associated with this resource.
	 *
	 * @return list of properties
	 */
	public IProperty [] getProperties();
	
	
	/**
	 * get a single value of the given properties.
	 *
	 * @param prop the prop
	 * @return list of properties
	 */
	public Object getPropertyValue(IProperty prop);
	
	/**
	 * get values of the given properties.
	 *
	 * @param prop the prop
	 * @return list of properties
	 */
	public Object [] getPropertyValues(IProperty prop);
	
	
	/**
	 * add single value of the given properties.
	 *
	 * @param prop the prop
	 * @param value the value
	 */
	public void addPropertyValue(IProperty prop, Object value);
	
	/**
	 * set single value of the given properties.
	 *
	 * @param prop the prop
	 * @param value the value
	 */
	public void setPropertyValue(IProperty prop, Object value);
	
	/**
	 * get values of the given properties.
	 *
	 * @param prop the prop
	 * @param values the values
	 */
	public void setPropertyValues(IProperty prop, Object[] values);
	
	/**
	 * remove all property values from resource for given property.
	 *
	 * @param prop the prop
	 */
	public void removePropertyValues(IProperty prop);
	
	/**
	 * remove all property values from resource for given property.
	 *
	 * @param prop the prop
	 * @param value the value
	 */
	public void removePropertyValue(IProperty prop, Object value);
	
	/**
	 * remove all property values from resource.
	 */
	public void removePropertyValues();
	
	
	/**
	 * has propety value.
	 *
	 * @param p the p
	 * @param value the value
	 * @return true, if successful
	 */
	public boolean hasPropetyValue(IProperty p, Object value);

	
	/**
	 * add label.
	 *
	 * @param label the label
	 */
	public void addLabel(String label);
	
	
	/**
	 * add comment.
	 *
	 * @param comment the comment
	 */
	public void addComment(String comment);
	
	
	/**
	 * add label.
	 *
	 * @param label the label
	 */
	public void removeLabel(String label);
	
	
	/**
	 * add comment.
	 *
	 * @param comment the comment
	 */
	public void removeComment(String comment);
	
	/**
	 * add version.
	 *
	 * @param version the version
	 */
	public void addVersion(String version);
	
	
	/**
	 * remove version.
	 *
	 * @param version the version
	 */
	public void removeVersion(String version);
	
	/**
	 * get labels.
	 *
	 * @return the labels
	 */
	public String [] getLabels();
	
	
	/**
	 * get labels.
	 *
	 * @return the comments
	 */
	public String [] getComments();
	
	
	/**
	 * is this a system resource.
	 *
	 * @return true, if is system
	 */
	public boolean isSystem();
	
	/**
	 * get an Ontology that is associated with this resource.
	 *
	 * @return the ontology
	 */
	public IOntology getOntology();
	
	/**
	 * remove this resource.
	 */
	public void delete();
	
	
	/**
	 * set name of a resource (fragment part of a URL).
	 *
	 * @param name the new name
	 */
	public void setName(String name);
	
	/**
	 * get logic expression that represents this resource
	 * usually this is an empty expression with this as its parameter
	 * if resource is LogicClass, then it might do something interesting.
	 *
	 * @return the logic expression
	 */
	public ILogicExpression getLogicExpression();
	
	
	/**
	 * get misc properties that are associated with this resource.
	 *
	 * @return the resource properties
	 */
	public Properties getResourceProperties();
	
	/**
	 * get a label of a resource if available, else returns a name of the resource.
	 *
	 * @return the label
	 */
	public String getLabel();
}
