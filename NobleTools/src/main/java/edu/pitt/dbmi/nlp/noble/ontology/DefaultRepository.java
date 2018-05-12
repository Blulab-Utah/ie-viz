package edu.pitt.dbmi.nlp.noble.ontology;

import edu.pitt.dbmi.nlp.noble.ontology.owl.OOntology;
import edu.pitt.dbmi.nlp.noble.ontology.owl.OReasoner;
import edu.pitt.dbmi.nlp.noble.terminology.Terminology;
import edu.pitt.dbmi.nlp.noble.terminology.impl.NobleCoderTerminology;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;

/**
 * The Class DefaultRepository.
 */
public class DefaultRepository implements IRepository{
	public static final File DEFAULT_TERMINOLOGY_LOCATION = new File(System.getProperty("user.home")+File.separator+".noble"+File.separator+"terminologies");
	public static final File DEFAULT_ONTOLOGY_LOCATION = new File(System.getProperty("user.home")+File.separator+".noble"+File.separator+"ontologies");
	
	
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private Map<URI,IOntology> ontologies; 
	private Map<String,Terminology> terminologies ; 
	private File terminologyLocation,ontologyLocation;
	
	/**
	 * Instantiates a new default repository.
	 */
	public DefaultRepository(){
		terminologyLocation = NobleCoderTerminology.getPersistenceDirectory();
		ontologyLocation = DEFAULT_ONTOLOGY_LOCATION;
	}
	
	public void reset(){
		terminologies = null;
		ontologies = null;
	}
	
	
	/**
	 * Gets the terminology location.
	 *
	 * @return the terminology location
	 */
	public File getTerminologyLocation() {
		return terminologyLocation;
	}


	/**
	 * Sets the terminology location.
	 *
	 * @param terminologyLocation the new terminology location
	 */
	public void setTerminologyLocation(File terminologyLocation) {
		this.terminologyLocation = terminologyLocation;
		NobleCoderTerminology.setPersistenceDirectory(terminologyLocation);
	}


	/**
	 * Gets the ontology location.
	 *
	 * @return the ontology location
	 */
	public File getOntologyLocation() {
		return ontologyLocation;
	}


	/**
	 * Sets the ontology location.
	 *
	 * @param ontologyLocation the new ontology location
	 */
	public void setOntologyLocation(File ontologyLocation) {
		this.ontologyLocation = ontologyLocation;
	}


	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#addOntology(edu.pitt.dbmi.nlp.noble.ontology.IOntology)
	 */
	public void addOntology(IOntology ontology) {
		ontologies.put(ontology.getURI(),ontology);
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
		terminologies.put(terminology.getName(),terminology);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#createOntology(java.net.URI)
	 */
	public IOntology createOntology(URI path) throws IOntologyException {
		return OOntology.createOntology(path);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#exportOntology(edu.pitt.dbmi.nlp.noble.ontology.IOntology, int, java.io.OutputStream)
	 */
	public void exportOntology(IOntology ont, int format, OutputStream out) throws IOntologyException {
		ont.write(out,format);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#getDescription()
	 */
	public String getDescription() {
		return "OWL Ontology and NOBLE Terminology Repository.";
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#getName()
	 */
	public String getName() {
		return "OWL Ontology Repository";
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#getOntologies()
	 */
	public IOntology[] getOntologies() {
		if(ontologies == null){
			ontologies = new HashMap<URI, IOntology>();
			File dir = ontologyLocation;
			if(!dir.exists())
				dir.mkdirs();
			for(File f: dir.listFiles()){
				if(f.getName().endsWith(".owl")){
					try{
						addOntology(new OOntology(f.getAbsolutePath()));
					}catch(Throwable e){
						e.printStackTrace();
					}
				}
			}
			
		}
		return ontologies.values().toArray(new IOntology [0]);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#getOntologies(java.lang.String)
	 */
	public IOntology[] getOntologies(String name) {
		List<IOntology> list = new ArrayList<IOntology>();
		for(URI key: ontologies.keySet()){
			if(key.toString().contains(name)){
				list.add(ontologies.get(key));
			}
		}
		return list.toArray(new IOntology [0]);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#getOntology(java.net.URI)
	 */
	public IOntology getOntology(URI name) {
		return ontologies.get(name);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#getReasoner(edu.pitt.dbmi.nlp.noble.ontology.IOntology)
	 */
	public IReasoner getReasoner(IOntology ont) {
		if(ont instanceof OOntology){
			return new OReasoner((OOntology)ont);
		}
		return null;
	}

	/**
	 * convinience method
	 * get resource from one of the loaded ontologies.
	 *
	 * @param path - input uri
	 * @return resource or null if resource was not found
	 */
	public IResource getResource(URI path){
		String uri = ""+path;
		int i = uri.lastIndexOf("#");
		uri = (i > -1)?uri.substring(0,i):uri;
		// get ontology
		IOntology ont = getOntology(URI.create(uri));
		
		// if ontology is all you want, fine Girish
		if(i == -1)
			return ont;
		// 
		if(ont != null){
			return ont.getResource(""+path);
		}
		return null;
	}
	

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#getTerminologies()
	 */
	public Terminology[] getTerminologies() {
		if(terminologies == null){
			terminologies = new HashMap<String, Terminology>();
			File dir = terminologyLocation;
			if(!dir.exists())
				dir.mkdirs();
			for(File f: dir.listFiles()){
				String sf = NobleCoderTerminology.TERM_SUFFIX;
				if(f.getName().endsWith(sf)){
					Terminology t;
					try {
						String name = f.getName().substring(0,f.getName().length()-sf.length());
						t = new NobleCoderTerminology(name);
						/*
						((NobleCoderTerminology)t).load(name,false);
						((NobleCoderTerminology)t).save();*/
						
						terminologies.put(t.getName(),t);
					} catch (UnsupportedOperationException e){
						System.err.println("Corrupted termonology detected at "+f.getAbsolutePath()+". skipping ...");
						//e.printStackTrace();
					//} catch (Error e){
					//	System.err.println("Corrupted termonology detected at "+f.getAbsolutePath()+". skipping ...");
					//	e.printStackTrace();
					} catch (Exception e) {
						System.err.println("Corrupted termonology detected at "+f.getAbsolutePath()+". skipping ...");
						e.printStackTrace();
					} 
				}
			}
			
		}
		Set<Terminology> terms = new TreeSet<Terminology>(new Comparator<Terminology>(){
			public int compare(Terminology o1, Terminology o2) {
				return o1.getName().compareTo(o2.getName());
			}});
		terms.addAll(terminologies.values());
		return terms.toArray(new Terminology [0]);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#getTerminology(java.lang.String)
	 */
	public Terminology getTerminology(String path) {
		getTerminologies();
		return terminologies.get(path);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#hasOntology(java.lang.String)
	 */
	public boolean hasOntology(String name) {
		return getOntologies(name).length > 0;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#importOntology(java.net.URI)
	 */
	public IOntology importOntology(URI path) throws IOntologyException {
		URL url = null;
		try {
			url = path.toURL();
		} catch (MalformedURLException e) {
			throw new IOntologyException("Invalid URI supplied: "+path,e);
		}
		IOntology ont = OOntology.loadOntology(url);
		importOntology(ont);
		return getOntology(ont.getURI());
		
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#importOntology(edu.pitt.dbmi.nlp.noble.ontology.IOntology)
	 */
	public void importOntology(IOntology ont) throws IOntologyException {
		File file = new File(DEFAULT_ONTOLOGY_LOCATION,ont.getName());
		try {
			ont.write(new FileOutputStream(file),IOntology.OWL_FORMAT);
		} catch (FileNotFoundException e) {
			throw new IOntologyException("Unable to save file in the local cache at "+file.getAbsolutePath(),e);
		}
		// reload from cache
		ont.dispose();
		ont = OOntology.loadOntology(file);
		addOntology(ont);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#removeOntology(edu.pitt.dbmi.nlp.noble.ontology.IOntology)
	 */
	public void removeOntology(IOntology ontology) {
		ontologies.remove(ontology.getURI());
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
		terminologies.remove(terminology.getName());
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#getOntology(java.net.URI, java.lang.String)
	 */
	public IOntology getOntology(URI name, String version) {
		return getOntology(name);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IRepository#getVersions(edu.pitt.dbmi.nlp.noble.ontology.IOntology)
	 */
	public String[] getVersions(IOntology ont) {
		return (ont != null)?new String [] {ont.getVersion()}:new String [0];
	}
}
