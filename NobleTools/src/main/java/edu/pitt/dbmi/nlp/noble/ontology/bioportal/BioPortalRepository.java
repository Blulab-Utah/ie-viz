package edu.pitt.dbmi.nlp.noble.ontology.bioportal;

import edu.pitt.dbmi.nlp.noble.ontology.*;
import edu.pitt.dbmi.nlp.noble.terminology.Terminology;
import edu.pitt.dbmi.nlp.noble.ui.RepositoryManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;

import static edu.pitt.dbmi.nlp.noble.ontology.bioportal.BioPortalHelper.*;

/**
 * provides view into BioPortal Repository.
 *
 * @author Eugene Tseytlin
 */
public class BioPortalRepository implements IRepository {
	public static final String DEFAULT_BIOPORTAL_URL = "http://data.bioontology.org"; // "http://rest.bioontology.org/bioportal/";
	public static final String DEFAULT_BIOPORTAL_API_KEY = "6ebc962a-e7ae-40e4-af41-472224ef81aa";
	public static final String BIOPORTAL_FORMAT = "&format=xml";
	
	private URL bioPortalURL;
	private String bioPortalAPIKey;
	private Map<String,BOntology> ontologyMap;
	private IOntology [] ontologies;
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		BioPortalRepository repository = new BioPortalRepository();
		RepositoryManager rm = new RepositoryManager();
		rm.start(repository);
	}
	
	
	/**
	 * creat new bioportal repository.
	 *
	 * @param url the url
	 */
	public BioPortalRepository(URL url){
		bioPortalURL = url;
		bioPortalAPIKey = "apikey="+DEFAULT_BIOPORTAL_API_KEY;
	}
	
	/**
	 * creat new bioportal repository.
	 *
	 * @param url the url
	 * @param apiKey the api key
	 */
	public BioPortalRepository(URL url, String apiKey){
		bioPortalURL = url;
		bioPortalAPIKey = "apikey="+apiKey;
	}
	
	
	/**
	 * creat new bioportal repository.
	 */
	public BioPortalRepository(){
		try{
			bioPortalURL = new URL(DEFAULT_BIOPORTAL_URL);
		}catch(MalformedURLException ex){
			ex.printStackTrace();
		}
		bioPortalAPIKey = "apikey="+DEFAULT_BIOPORTAL_API_KEY;
	}
	
	/**
	 * Gets the url.
	 *
	 * @return the url
	 */
	public URL getURL(){
		return bioPortalURL;
	}
	
	/**
	 * Gets the API key.
	 *
	 * @return the API key
	 */
	public String getAPIKey(){
		return bioPortalAPIKey;
	}
	
	/**
	 * fetch all ontologies from URL.
	 *
	 * @return the map
	 */
	private Map<String,BOntology> fetchOntologies(){
		// init map
		ontologyMap = new LinkedHashMap<String,BOntology>();
		
		// get document
		Document doc = parseXML(openURL(getURL()+ONTOLOGIES+"?"+getAPIKey()));
		if(doc != null){
			// since ontologyBean are not nested we can simple
			// get their list
			NodeList list = doc.getDocumentElement().getElementsByTagName("ontology");
			for(int i=0;i<list.getLength();i++){
				BOntology ont = new BOntology(this,(Element)list.item(i));
				ontologyMap.put(ont.getName(),ont);
				ontologyMap.put(""+ont.getURI(),ont);
			}
		}
		return ontologyMap;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#addOntology(edu.pitt.dbmi.nlp.noble.ontology.IOntology)
	 */
	public void addOntology(IOntology ontology) {
		throw new IOntologyError("BioPortal Repository is read-only");
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#addTerminology(edu.pitt.dbmi.nlp.noble.terminology.Terminology)
	 */
	public void addTerminology(Terminology terminology) {
		throw new IOntologyError("BioPortal Repository is read-only");
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#createOntology(java.net.URI)
	 */
	public IOntology createOntology(URI path) throws IOntologyException {
		throw new IOntologyError("BioPortal Repository is read-only");
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#exportOntology(edu.pitt.dbmi.nlp.noble.ontology.IOntology, int, java.io.OutputStream)
	 */
	public void exportOntology(IOntology ontology, int format, OutputStream out) throws IOntologyException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#getOntologies()
	 */
	public IOntology[] getOntologies() {
		if(ontologyMap == null)
			ontologyMap = fetchOntologies();
		if(ontologies == null){
			SortedSet<BOntology> set = new TreeSet<BOntology>(new Comparator<BOntology>() {
				public int compare(BOntology o1, BOntology o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			set.addAll(ontologyMap.values());
			ontologies = set.toArray(new IOntology [0]);
		}
		return ontologies;
	}
	
	/**
	 * get ontologies that are loaded in repository.
	 *
	 * @param name the name
	 * @return the ontologies
	 */
	public IOntology [] getOntologies(String name){
		if(ontologyMap == null)
			ontologyMap = fetchOntologies();
		ArrayList<IOntology> onts = new ArrayList<IOntology>();
		for(String str: ontologyMap.keySet()){
			if(str.contains(name)){
				onts.add(ontologyMap.get(str));
			}
		}
		return onts.toArray(new IOntology [0]);
	}
	

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#getOntology(java.net.URI)
	 */
	public IOntology getOntology(URI u) {
		if(ontologyMap == null)
			ontologyMap = fetchOntologies();
		return ontologyMap.get(""+u);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#getResource(java.net.URI)
	 */
	public IResource getResource(URI path) {
		String uri = path.toASCIIString();
		int i = uri.lastIndexOf("#");
		uri = (i > -1)?uri.substring(0,i):uri;
		// get ontology
		IOntology ont = getOntology(URI.create(uri));
		// if ontology is all you want, fine Girish
		if(i == -1)
			return ont;
		// 

		if(ont != null){
			uri = path.toASCIIString();
			return ont.getResource(uri.substring(i+1));
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#getTerminologies()
	 */
	public Terminology[] getTerminologies() {
		SortedSet<BOntology> set = new TreeSet<BOntology>();
		set.addAll(ontologyMap.values());
		return set.toArray(new Terminology [0]);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#getTerminology(java.lang.String)
	 */
	public Terminology getTerminology(String path) {
		if(ontologyMap == null)
			ontologyMap = fetchOntologies();
		return ontologyMap.get(path);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#hasOntology(java.lang.String)
	 */
	public boolean hasOntology(String name) {
		if(ontologyMap == null)
			ontologyMap = fetchOntologies();
		return ontologyMap.containsKey(name) || getOntologies(name).length > 0;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#importOntology(java.net.URI)
	 */
	public IOntology importOntology(URI path) throws IOntologyException {
		throw new IOntologyError("BioPortal Repository is read-only");
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#importOntology(edu.pitt.dbmi.nlp.noble.ontology.IOntology)
	 */
	public void importOntology(IOntology ont) throws IOntologyException {
		throw new IOntologyError("BioPortal Repository is read-only");
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#removeOntology(edu.pitt.dbmi.nlp.noble.ontology.IOntology)
	 */
	public void removeOntology(IOntology ontology) {
		throw new IOntologyError("BioPortal Repository is read-only");
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#removePropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#removeTerminology(edu.pitt.dbmi.nlp.noble.terminology.Terminology)
	 */
	public void removeTerminology(Terminology terminology) {
		throw new IOntologyError("BioPortal Repository is read-only");
	}

	/**
	 * get reasoner that can handle this ontology
	 * you can configure the type of reasoner by 
	 * specifying reasoner class and optional URL
	 * in System.getProperties()
	 * reasoner.class and reasoner.url
	 *
	 * @param ont the ont
	 * @return null if no reasoner is available
	 */
	public IReasoner getReasoner(IOntology ont){
		return null;
	}
		
	/**
	 * get name of this repository.
	 *
	 * @return the name
	 */
	public String getName(){
		return "BioPortal Repository";
	}
	
	
	/**
	 * get description of repository.
	 *
	 * @return the description
	 */
	public String getDescription(){
		return "Use BioPortal to access and share ontologies that are actively used in biomedical communities.";
	}
	

	/**
	 * get specific ontology version.
	 *
	 * @param name the name
	 * @param version the version
	 * @return the ontology
	 */
	public IOntology getOntology(URI name, String version) {
		BOntology ont = (BOntology) getOntology(name);
		return (ont != null)?ont.getOntologyVersions().get(version):null;
	}

	
	/**
	 * get versions available for an ontology.
	 *
	 * @param ont the ont
	 * @return the versions
	 */
	public String[] getVersions(IOntology ont) {
		if(ont instanceof BOntology){
			List<String> vers = new ArrayList<String>(((BOntology)ont).getOntologyVersions().keySet());
			return vers.toArray(new String [0]);
		}
		return (ont != null)?new String [] {ont.getVersion()}:new String [0];
	}
}
