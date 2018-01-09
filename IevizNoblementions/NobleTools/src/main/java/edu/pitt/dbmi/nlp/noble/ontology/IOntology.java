package edu.pitt.dbmi.nlp.noble.ontology;
import java.beans.PropertyChangeListener;
import java.io.OutputStream;
import java.net.*;

/**
 * this class describes an ontology, get an instance of this class
 * from Repository factory class.
 *
 * @author tseytlin
 */
public interface IOntology extends IResource {
	// dublin core ontology URI
	public static final URI DUBLIN_CORE_ONTOLOGY_URI = URI.create("http://protege.stanford.edu/plugins/owl/dc/protege-dc.owl");
	
	public static final int RDF_FORMAT = 0;
	public static final int NTRIPLE_FORMAT = 1;
	public static final int TURTLE_FORMAT = 2;
	public static final int OWL_FORMAT = 3;
	public static final int OBO_FORMAT = 4;
	
	public static final String ONTOLOGY_LOADING_EVENT = "Ontology Loading";
	public static final String ONTOLOGY_LOADED_EVENT = "Ontology Loaded";
	public static final String ONTOLOGY_SAVED_EVENT = "Ontology Saved";
	
	/**
	 * add listener to listen to misc ontology events.
	 *
	 * @param listener the listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener);
	
	
	/**
	 * remove listener to listen to misc ontology events.
	 *
	 * @param listener the listener
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener);
	
	
	/**
	 * add new included ontology.
	 *
	 * @param o the o
	 * @throws IOntologyException the i ontology exception
	 */
	public void addImportedOntology(IOntology o) throws IOntologyException;
	
	
	/**
	 * add new included ontology.
	 *
	 * @param o the o
	 */
	public void removeImportedOntology(IOntology o);
	
	/**
	 * get invluded ontologies.
	 *
	 * @return the imported ontologies
	 */
	public IOntology [] getImportedOntologies();
	
	
	/**
	 * get repository ontology belongs to (if available).
	 *
	 * @return the repository
	 */
	public IRepository getRepository();
	
	/**
	 * set repository ontology belongs to (if available).
	 *
	 * @param r the new repository
	 */
	public void setRepository(IRepository r);
	
	/**
	 * add class to Ontology.
	 *
	 * @param name the name
	 * @return the i class
	 */
	public IClass createClass(String name);
	
	/**
	 * create a class that represents this expression.
	 *
	 * @param exp the exp
	 * @return the i class
	 */
	public IClass createClass(ILogicExpression exp);
	
	
	/**
	 * add class to Ontology.
	 *
	 * @param name the name
	 * @param type the type
	 * @return the i property
	 */
	public IProperty createProperty(String name, int type);
	
	/**
	 * create logic expression .
	 *
	 * @param type the type
	 * @param param the param
	 * @return the i logic expression
	 */
	public ILogicExpression createLogicExpression(int type, Object param);
	
	/**
	 * create logic expression .
	 *
	 * @return the i logic expression
	 */
	public ILogicExpression createLogicExpression();
	
	
	/**
	 * add class to Ontology.
	 *
	 * @param type the type
	 * @return the i restriction
	 */
	public IRestriction createRestriction(int type);

	
	/**
	 * get specific resource.
	 *
	 * @param iQuery the i query
	 * @return the i query results
	 */
	public IQueryResults executeQuery(IQuery iQuery);
	
	/**
	 * get specific resource.
	 *
	 * @param p the p
	 * @param value the value
	 * @return the matching resources
	 */
	public IResourceIterator getMatchingResources(IProperty p, Object value);
	
	
	/**
	 * get resource whose name matches RegEx.
	 *
	 * @param regex the regex
	 * @return the matching resources
	 */
	public IResourceIterator getMatchingResources(String regex);
	
	
	/**
	 * get specific resource
	 * Ex: Cat, kb:Cat or http://www.owl-ontologies.com/Animals.owl#Cat
	 *
	 * @param name the name
	 * @return resource that was found, null if not found
	 */
	public IResource getResource(String name);
	
	/**
	 * get specific class.
	 *
	 * @param name the name
	 * @return resource that was found, null if not found
	 */
	public IClass getClass(String name);
	
	/**
	 * get specific instance
	 * Ex: Cat, kb:Cat or http://www.owl-ontologies.com/Animals.owl#Cat
	 *
	 * @param name the name
	 * @return resource that was found, null if not found* @return
	 */
	public IInstance getInstance(String name);
	
	/**
	 * get specific property
	 * Ex: Cat, kb:Cat or http://www.owl-ontologies.com/Animals.owl#Cat
	 *
	 * @param name the name
	 * @return resource that was found, null if not found
	 */
	public IProperty getProperty(String name);
	
	
	
	/**
	 * check if such resource is available.
	 *
	 * @param path the path
	 * @return true, if successful
	 */
	public boolean hasResource(String path);
	
	
	/**
	 * get root classes .
	 *
	 * @return root class bojects
	 */
	public IClass [] getRootClasses();
	
	/**
	 * get one and only OWL system root class
	 * OWLThing.
	 *
	 * @return the root
	 */
	public IClass getRoot();

	
	/**
	 * get all resources.
	 *
	 * @return the all resources
	 */
	public IResourceIterator getAllResources();
	
	/**
	 * get all properties.
	 *
	 * @return the all properties
	 */
	public IResourceIterator getAllProperties();
	
	
	/**
	 * get all classes.
	 *
	 * @return the all classes
	 */
	public IResourceIterator getAllClasses();
	
	
	/**
	 * is ontology loaded into memory.
	 *
	 * @return true or false
	 */
	public boolean isLoaded();
	
	
	/**
	 * load this ontology into memory 
	 * this method loads OWL model into memory, if not already loaded
	 * if OWL model was already loaded, then this method is NOOP.
	 *
	 * @throws IOntologyException the i ontology exception
	 */
	public void load() throws IOntologyException;
	
	
	/**
	 * reload this ontology from persistant storeage
	 * this method loads OWL model into memory, if not already loaded
	 * if OWL model was already loaded, then this method is NOOP.
	 *
	 * @throws IOntologyException the i ontology exception
	 */
	public void reload() throws IOntologyException;
	
	
	/**
	 * persist this ontology from memory.
	 */
	public void flush();
	
	/**
	 * persist this ontology from memory to disk/db.
	 *
	 * @throws IOntologyException the i ontology exception
	 */
	public void save() throws IOntologyException;
	
	
	/**
	 * unload ontology, release all of held resources
	 * if there are any.
	 */
	public void dispose();
	
	
	/**
	 * export ontology to output stream in some format.
	 *
	 * @param out the out
	 * @param format the format
	 * @throws IOntologyException the i ontology exception
	 */
	public void write(OutputStream out, int format) throws IOntologyException;
	
	
	/**
	 * has ontology been modified.
	 *
	 * @return true, if is modified
	 */
	public boolean isModified();
}
