package edu.pitt.dbmi.nlp.noble.ontology.bioportal;

import edu.pitt.dbmi.nlp.noble.ontology.*;
import edu.pitt.dbmi.nlp.noble.terminology.Concept;
import edu.pitt.dbmi.nlp.noble.terminology.SemanticType;
import edu.pitt.dbmi.nlp.noble.terminology.Source;
import edu.pitt.dbmi.nlp.noble.tools.TextTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static edu.pitt.dbmi.nlp.noble.ontology.bioportal.BioPortalHelper.*;

/**
 * this represents a bioportal class.
 *
 * @author Eugene Tseytlin
 */
public class BClass extends BResource implements IClass {
	private boolean loaded;
	private Concept concept;
	private Set<IClass> superClasses,subClasses,directSuperClasses,directSubClasses;
	
	/**
	 * create new class if id is not known.
	 *
	 * @param ont the ont
	 * @param id the id
	 */
	public BClass(BOntology ont, String id){
		super();
		setOntology(ont);
		properties.setProperty("id",id);
		
		// setup url
		properties.setProperty("location",ont.getLocation()+CONCEPTS+TextTools.escapeURL(getId()));
		properties.setProperty("uri",getNameSpace()+getName());
	}

	/**
	 * add necessary restriction to this class.
	 *
	 * @param restriction the restriction
	 */
	public void addNecessaryRestriction(ILogicExpression restriction){
		throw new IOntologyError("Not Implemented");
	}
	

	/**
	 * remove restriction to this class.
	 *
	 * @param restriction the restriction
	 */
	public void removeNecessaryRestriction(ILogicExpression restriction){
		throw new IOntologyError("Not Implemented");
	}
	
	/**
	 * add equivalent Necessary and Sufficient restriction to this class.
	 *
	 * @param restriction the restriction
	 */
	public void addEquivalentRestriction(ILogicExpression restriction){
		throw new IOntologyError("Not Implemented");
	}

	/**
	 * remove restriction to this class.
	 *
	 * @param restriction the restriction
	 */
	public void removeEquivalentRestriction(ILogicExpression restriction){
		throw new IOntologyError("Not Implemented");
	}
	
	/**
	 * create bclass from the element.
	 *
	 * @param ont the ont
	 * @param e the e
	 */
	BClass(BOntology ont, Element e){
		super();
		setOntology(ont);
		load(e);
	}
	
	
	/**
	 * load class content.
	 *
	 * @param element the element
	 */
	public void load(Element element){
		// pull in all class data
		NodeList list = element.getChildNodes();
		List<Element> synonyms = null;
		List<Element> definitions = null;
		
		for(int i=0;i<list.getLength();i++){
			if(list.item(i) instanceof Element){
				Element e = (Element) list.item(i);
				String text = e.getTextContent();
			if(e.getTagName().equals("synonymCollection")){
					synonyms = getElementsByTagName(e,"synonym");
				}else if(e.getTagName().equals("definitionCollection")){
					definitions = getElementsByTagName(e,"definition");
				}else if(text != null && text.trim().length() > 0){
					//String key = RELATIONSHIPS.getProperty(e.getTagName(),e.getTagName());
					properties.put(e.getTagName(),text.trim());
				}
			}
		}
		// now set some common elements in standard way
		if(properties.containsKey("prefLabel")){
			String name = (String) properties.get("prefLabel");
			addLabel(name);
			setName(deriveName(name));
		}
		
		// now set type
		if(properties.containsKey("type")){
			resourceType = properties.getProperty("type");
		}
		
		// add synonyms
		if(synonyms != null){
			for(Element e: synonyms){
				addLabel(e.getTextContent());
			}
		}
		// add definitions
		if(definitions != null){
			for(Element e: definitions){
				addComment(e.getTextContent());
			}
		}
				
		// set locations
		BOntology ont = (BOntology) getOntology();
		properties.setProperty("location",ont.getLocation()+CONCEPTS+TextTools.escapeURL(getId()));
		properties.setProperty("uri",getNameSpace()+getName());
		
		// don't bother if in quick mode
		/*
		if(quick){
			// register class if class
			if(TYPE_CLASS.equalsIgnoreCase(getResourceType()))
				ont.registerClass(this);
			return;
		}*/
		
		
		// create properties objects
		List<String> props = new ArrayList<String>();
		for(Object key: properties.keySet()){
			if(!isReservedProperty(key)){
				ont.createProperty(""+key,IProperty.ANNOTATION_DATATYPE);
				props.add(""+key);
			}
		}
		properties.put(PROPERTIES,props);
		
		// only consider it loaded when relations were available
		loaded = true;
		
		// cleanup?, Please clean up...
		element = null;
		list = null;
		synonyms = null;
		definitions = null;
			
		// register class if class
		if(TYPE_CLASS.equalsIgnoreCase(getResourceType()))
			ont.registerClass(this);
		
	}
	
	/**
	 * Load.
	 */
	public void load(){
		// if not loaded 
		if(!isLoaded()){
			InputStream in = openURL(getLocation()+"?"+((BioPortalRepository)getOntology().getRepository()).getAPIKey());
			if(in != null){
				Document doc = parseXML(in);
				if(doc != null){
					//load(getElementByTagName(doc.getDocumentElement(),"classBean"));
					load(doc.getDocumentElement());
					// this is definately loaded
					loaded = true;
				}
			}
		}
	}
	
	/**
	 * dispose of resources.
	 */
	public void dispose(){
		super.dispose();
		superClasses = subClasses = null;
		loaded = false;
		concept = null;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#addDisjointClass(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public void addDisjointClass(IClass a) {
		throw new IOntologyError("Operation Not Supported");
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#addEquivalentClass(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public void addEquivalentClass(IClass a) {
		throw new IOntologyError("Operation Not Supported");	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#addEquivalentRestriction(edu.pitt.dbmi.nlp.noble.ontology.IRestriction)
	 */
	public void addEquivalentRestriction(IRestriction restriction) {
		throw new IOntologyError("Operation Not Supported");
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#addNecessaryRestriction(edu.pitt.dbmi.nlp.noble.ontology.IRestriction)
	 */
	public void addNecessaryRestriction(IRestriction restriction) {
		throw new IOntologyError("Operation Not Supported");
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#addSubClass(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public void addSubClass(IClass child) {
		throw new IOntologyError("Operation Not Supported");
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#addSuperClass(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public void addSuperClass(IClass parent) {
		throw new IOntologyError("Operation Not Supported");
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#createInstance(java.lang.String)
	 */
	public IInstance createInstance(String name) {
		throw new IOntologyError("Operation Not Supported");
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#createInstance()
	 */
	public IInstance createInstance() {
		throw new IOntologyError("Operation Not Supported");
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#createSubClass(java.lang.String)
	 */
	public IClass createSubClass(String name) {
		throw new IOntologyError("Operation Not Supported");
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#evaluate(java.lang.Object)
	 */
	public boolean evaluate(Object obj) {
		if(obj instanceof IClass){
			IClass c2 = (IClass) obj;
			return equals(c2) || hasSubClass(c2);
		}else if(obj instanceof IInstance){
			IInstance i2 = (IInstance) obj;
			return i2.hasType(this);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#getConcept()
	 */
	public Concept getConcept() {
		//return  new Concept(getId(),getName());
		if(concept == null){
			load();
			concept = new Concept(this);
			
			// add codes
			if(properties.containsKey(CODE)){
				Object val = properties.get(CODE);
				int i = 0;
				if(val instanceof Collection){
					for(Object o :(Collection) val)
						concept.addCode(""+o,new Source("SOURCE"+(i++)));
				}else
					concept.addCode(""+val,new Source("SOURCE"+i));
			}
			
			// add semantic types
			if(properties.containsKey(SEMANTIC_TYPE)){
				Object val = properties.get(SEMANTIC_TYPE);
				List<SemanticType> sems = new ArrayList<SemanticType>();
				if(val instanceof Collection){
					for(Object o :(Collection) val)
						sems.add(SemanticType.getSemanticType(""+o));
				}else
					sems.add(SemanticType.getSemanticType(""+val));
				concept.setSemanticTypes(sems.toArray(new SemanticType [0]));
			}
			
			
		}
		return concept;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#getDirectInstances()
	 */
	public IInstance[] getDirectInstances() {
		return new IInstance [0];
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#getDirectNecessaryRestrictions()
	 */
	public ILogicExpression getDirectNecessaryRestrictions() {
		return getOntology().createLogicExpression();
	}
	
	/**
	 * load list of classes.
	 *
	 * @param key the key
	 * @return the class list
	 */
	private Set<IClass> getClassList(String key){
		//load content
		load();
		
		// load list of classes
		return BioPortalHelper.getClassList((BOntology)getOntology(),getLocation()+key+"?"+getAPIKey()+BioPortalHelper.BIOPORTAL_OPTIONS);
	}
	
	/**
	 * get direct sub classes.
	 *
	 * @return the direct sub classes
	 */
	public IClass[] getDirectSubClasses() {
		if(directSubClasses == null){
			directSubClasses = getClassList(BioPortalHelper.CHILDREN);
		}
		return 	directSubClasses.toArray(new IClass [0]);
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.bioportal.BResource#getLabels()
	 */
	public String [] getLabels(){
		load();
		return super.getLabels();
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.bioportal.BResource#getComments()
	 */
	public String [] getComments(){
		load();
		return super.getComments();
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#getDirectSuperClasses()
	 */
	public IClass [] getDirectSuperClasses() {
		if(directSuperClasses == null){
			directSuperClasses = getClassList(BioPortalHelper.PARENTS);
		}
		return 	directSuperClasses.toArray(new IClass [0]);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#getDisjointClasses()
	 */
	public IClass[] getDisjointClasses() {
		return new IClass [0];
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#getEquivalentClasses()
	 */
	public IClass[] getEquivalentClasses() {
		return new IClass [0];
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#getEquivalentRestrictions()
	 */
	public ILogicExpression getEquivalentRestrictions() {
		return new LogicExpression(ILogicExpression.EMPTY);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#getInstances()
	 */
	public IInstance[] getInstances() {
		// TODO Auto-generated method stub
		return new IInstance [0];
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#getNecessaryRestrictions()
	 */
	public ILogicExpression getNecessaryRestrictions() {
		return new LogicExpression(ILogicExpression.EMPTY);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#getRestrictions(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public IRestriction[] getRestrictions(IProperty p) {
		return new IRestriction [0];
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#getRestrictions(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public IRestriction[] getRestrictions() {
		return new IRestriction [0];
	}

	
	/**
	 * get all subclasses.
	 *
	 * @return the sub classes
	 */
	public IClass[] getSubClasses() {
		if(subClasses == null){
			subClasses = getClassList(BioPortalHelper.DESCENDANTS);
		}
		return 	subClasses.toArray(new IClass [0]);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#getSuperClasses()
	 */
	public IClass[] getSuperClasses() {
		if(superClasses == null){
			superClasses = getClassList(BioPortalHelper.ANCESTORS);
		}
		return 	superClasses.toArray(new IClass [0]);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#hasSubClass(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public boolean hasSubClass(IClass child) {
		getSubClasses();
		return subClasses.contains(child);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#hasSuperClass(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public boolean hasSuperClass(IClass parent) {
		getSuperClasses();
		return superClasses.contains(parent);
	}
	
	/**
	 * is child a sub class of parent.
	 *
	 * @param child the child
	 * @return true, if successful
	 */
	public boolean hasEquivalentClass(IClass child){
		return false;
	}
	
	
	/**
	 * is parent a direct super class of child.
	 *
	 * @param parent the parent
	 * @return true, if successful
	 */
	public boolean hasDirectSuperClass(IClass parent){
		getDirectSuperClasses();
		return directSuperClasses.contains(parent);
	}
	
	
	/**
	 * is child a direct sub class of parent.
	 *
	 * @param child the child
	 * @return true, if successful
	 */
	public boolean hasDirectSubClass(IClass child){
		getDirectSubClasses();
		return directSubClasses.contains(child);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#isAnonymous()
	 */
	public boolean isAnonymous() {
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#hasDisjointClass(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public boolean hasDisjointClass(IClass a) {
		return getClassList(DISJOINT_CLASS).contains(a);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#removeDisjointClass(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public void removeDisjointClass(IClass a) {
		throw new IOntologyError("Operation Not Supported");

	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#removeEquivalentClass(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public void removeEquivalentClass(IClass a) {
		throw new IOntologyError("Operation Not Supported");

	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#removeEquivalentRestriction(edu.pitt.dbmi.nlp.noble.ontology.IRestriction)
	 */
	public void removeEquivalentRestriction(IRestriction restriction) {
		throw new IOntologyError("Operation Not Supported");

	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#removeNecessaryRestriction(edu.pitt.dbmi.nlp.noble.ontology.IRestriction)
	 */
	public void removeNecessaryRestriction(IRestriction restriction) {
		throw new IOntologyError("Operation Not Supported");

	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#removeSubClass(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public void removeSubClass(IClass child) {
		throw new IOntologyError("Operation Not Supported");
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#removeSuperClass(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public void removeSuperClass(IClass parent) {
		throw new IOntologyError("Operation Not Supported");

	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.bioportal.BResource#getNameSpace()
	 */
	public String getNameSpace(){
		return getOntology().getNameSpace();
	}
	
	/**
	 * Checks if is loaded.
	 *
	 * @return true, if is loaded
	 */
	public boolean isLoaded(){
		return loaded;
	}
}
