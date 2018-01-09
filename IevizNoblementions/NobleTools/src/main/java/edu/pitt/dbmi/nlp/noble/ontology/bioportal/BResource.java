package edu.pitt.dbmi.nlp.noble.ontology.bioportal;

import static edu.pitt.dbmi.nlp.noble.ontology.bioportal.BioPortalHelper.COMMENTS;
import static edu.pitt.dbmi.nlp.noble.ontology.bioportal.BioPortalHelper.LABELS;
import static edu.pitt.dbmi.nlp.noble.ontology.bioportal.BioPortalHelper.PROPERTIES;
import static edu.pitt.dbmi.nlp.noble.ontology.bioportal.BioPortalHelper.VERSIONS;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import edu.pitt.dbmi.nlp.noble.ontology.ILogicExpression;
import edu.pitt.dbmi.nlp.noble.ontology.IOntology;
import edu.pitt.dbmi.nlp.noble.ontology.IOntologyError;
import edu.pitt.dbmi.nlp.noble.ontology.IProperty;
import edu.pitt.dbmi.nlp.noble.ontology.IResource;
import edu.pitt.dbmi.nlp.noble.ontology.LogicExpression;

/**
 * basic BioPortal resource.
 *
 * @author Eugene Tseytlin
 */
public class BResource implements IResource {
	protected Properties properties = new Properties();
	protected BOntology ontology;
	protected String resourceType;
	
	/**
	 * set ontology.
	 *
	 * @param ontology the new ontology
	 */
	public void setOntology(BOntology ontology) {
		this.ontology = ontology;
	}

	/**
	 * get a type of this resource.
	 *
	 * @return the resource type
	 */
	public String getResourceType(){
		return resourceType;
	}
	
	/**
	 * get list of strings from the properties.
	 *
	 * @param key the key
	 * @return the list
	 */
	protected Set<String> getList(String key){
		/*
		Set<String> list = null;
		Object obj = properties.get(key);
		if(obj == null){
			list = new LinkedHashSet<String>();
			properties.put(key,list);
		}else if(obj instanceof Set){
			list = (Set<String>) obj;
		}else {
			list = new LinkedHashSet<String>();
			list.add(obj.toString());
			properties.put(key,list);
		}
		*/
		HashSet<String> list = (HashSet<String>) properties.get(key);
		if(list == null){
			list = new LinkedHashSet<String>();
			properties.put(key,list);
		}
		return list;
	}
	
	/**
	 * get list of strings from the properties.
	 *
	 * @param key the key
	 * @return the object list
	 */
	protected List getObjectList(Object key){
		// maybe there is an object, but not a list
		List list = null;
		Object obj = properties.get(""+key);
		if(obj == null){
			list = new ArrayList();
			properties.put(""+key,list);
		}else if(obj instanceof List){
			list = (List) obj;
		}else if(obj instanceof Collection){
			list = new ArrayList((Collection)obj);
		}else{
			list = Collections.singletonList(obj);
		}
		return list;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#addComment(java.lang.String)
	 */
	public void addComment(String comment) {
		getList(COMMENTS).add(comment);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#addLabel(java.lang.String)
	 */
	public void addLabel(String label) {
		getList(LABELS).add(label);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#addPropertyValue(edu.pitt.dbmi.nlp.noble.ontology.IProperty, java.lang.Object)
	 */
	public void addPropertyValue(IProperty prop, Object value) {
		// add prop -value
		List list = getObjectList(prop);
		if(!list.contains(value))
			list.add(value);
		// add property to list of all properties
		list = getObjectList(BioPortalHelper.PROPERTIES);
		if(!list.contains(prop))
			list.add(prop);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#addVersion(java.lang.String)
	 */
	public void addVersion(String version) {
		getList(VERSIONS).add(version);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#delete()
	 */
	public void delete() {
		throw new IOntologyError("Read-Only Ontology");
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#dispose()
	 */
	public void dispose() {
		String loc = getLocation();
		properties.clear();
		properties.setProperty("location", loc);
	}

	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#getComments()
	 */
	public String[] getComments() {
		return getList(COMMENTS).toArray(new String [0]);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#getDescription()
	 */
	public String getDescription() {
		Set<String> str = getList(COMMENTS);
		return (str.size()>0)?str.iterator().next():"";
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#getLabels()
	 */
	public String[] getLabels() {
		return getList(LABELS).toArray(new String[0]);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#getLogicExpression()
	 */
	public ILogicExpression getLogicExpression() {
		return new LogicExpression(this);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#getName()
	 */
	public String getName() {
		return properties.getProperty("name","");
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#getNameSpace()
	 */
	public String getNameSpace() {
		return properties.getProperty("namespace","");
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#getOntology()
	 */
	public IOntology getOntology() {
		return ontology;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#getPrefix()
	 */
	public String getPrefix() {
		return  properties.getProperty("prefix","");
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#getProperties()
	 */
	public IProperty[] getProperties() {
		List<IProperty> list = new ArrayList<IProperty>();
		for(Object key: getObjectList(PROPERTIES)){
			list.add(ontology.getProperty(""+key));
		}
		return list.toArray(new IProperty [0]);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#getPropertyValue(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public Object getPropertyValue(IProperty prop) {
		Object [] obj = getPropertyValues(prop);
		return (obj.length > 0)?obj[0]:null;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#getPropertyValues(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public Object[] getPropertyValues(IProperty prop) {
		BProperty bp = (BProperty) prop;
		if(properties.containsKey(prop.getName()))
			return getObjectList(prop).toArray();
		else if(properties.containsKey(bp.getOrignalName()))
			return getObjectList(bp.getOrignalName()).toArray();
		return new Object [0];
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#getURI()
	 */
	public URI getURI() {
		
		Object u = properties.get("uri");
		try{
		if(u == null){
			u = URI.create(ontology.getNameSpace()+getName());
		}else if(!(u instanceof URI))
			u = URI.create(""+u);
		return (URI) u;
		}catch (Exception ex ){
			System.err.println(u);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#getVersion()
	 */
	public String getVersion() {
		Set<String> str = getList(VERSIONS);
		return (str.size()>0)?str.iterator().next():"";
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#hasPropetyValue(edu.pitt.dbmi.nlp.noble.ontology.IProperty, java.lang.Object)
	 */
	public boolean hasPropetyValue(IProperty p, Object value) {
		if(properties.containsKey(p.getName())){
			return getObjectList(p).contains(value);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#isSystem()
	 */
	public boolean isSystem() {
		return Boolean.parseBoolean(properties.getProperty("isSystem","false"));
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#removeComment(java.lang.String)
	 */
	public void removeComment(String comment) {
		getList(COMMENTS).remove(comment);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#removeLabel(java.lang.String)
	 */
	public void removeLabel(String label) {
		getList(LABELS).remove(label);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#removePropertyValue(edu.pitt.dbmi.nlp.noble.ontology.IProperty, java.lang.Object)
	 */
	public void removePropertyValue(IProperty prop, Object value) {
		if(properties.containsKey(prop.getName())){
			List list = getObjectList(prop);
			list.remove(value);
			if(list.isEmpty())
				removePropertyValues(prop);
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#removePropertyValues(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public void removePropertyValues(IProperty prop) {
		if(properties.containsKey(prop.getName())){
			properties.remove(prop.getName());
		}
		getObjectList(BioPortalHelper.PROPERTIES).remove(prop);

	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#removePropertyValues()
	 */
	public void removePropertyValues() {
		for(IProperty p: getProperties())
			removePropertyValues(p);
		properties.remove(BioPortalHelper.PROPERTIES);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#removeVersion(java.lang.String)
	 */
	public void removeVersion(String version) {
		getList(VERSIONS).remove(version);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#setDescription(java.lang.String)
	 */
	public void setDescription(String text) {
		List<String> c = new ArrayList<String>(getList(COMMENTS));
		if(c.size()> 0 &&  text.equals(c.get(0)))
			c.set(0,text);
		else
			c.add(0,text);
		properties.put(COMMENTS,new LinkedHashSet<String>(c));
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#setName(java.lang.String)
	 */
	public void setName(String name) {
		properties.setProperty("name",name);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#setPropertyValue(edu.pitt.dbmi.nlp.noble.ontology.IProperty, java.lang.Object)
	 */
	public void setPropertyValue(IProperty prop, Object value) {
		List list = getObjectList(prop);
		if(!list.contains(value)){
			// check if functional
			if(prop.isFunctional())
				list.clear();
			list.add(value);
		}
		list = getObjectList(BioPortalHelper.PROPERTIES);
		if(!list.contains(prop))
			list.add(prop);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#setPropertyValues(edu.pitt.dbmi.nlp.noble.ontology.IProperty, java.lang.Object[])
	 */
	public void setPropertyValues(IProperty prop, Object[] values) {
		List list = getObjectList(prop);
		list.clear();
		Collections.addAll(list,values);
		list = getObjectList(BioPortalHelper.PROPERTIES);
		if(!list.contains(prop))
			list.add(prop);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.Describable#getFormat()
	 */
	public String getFormat() {
		return properties.getProperty("format","");
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.Describable#getLocation()
	 */
	public String getLocation() {
		return properties.getProperty("location","");
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(IResource r) {
		return getURI().compareTo(r.getURI());
	}
	
	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId(){
		return properties.getProperty("id","");
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return getName();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj){
		if(obj instanceof BResource){
			return getURI().equals(((BResource)obj).getURI());
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode(){
		return getURI().hashCode();
	}
	
	/**
	 * get metadata information.
	 *
	 * @return the resource properties
	 */
	public Properties getResourceProperties(){
		return properties;
	}
	
	/**
	 * Gets the API key.
	 *
	 * @return the API key
	 */
	public String getAPIKey(){
		if(getOntology() != null && getOntology().getRepository() != null && getOntology().getRepository() instanceof BioPortalRepository){
			return ((BioPortalRepository)getOntology().getRepository()).getAPIKey();
		}
		return "";
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#getLabel()
	 */
	public String getLabel(){
		if(getLabels().length > 0)
			return getLabels()[0];
		return getName();
	}
}
