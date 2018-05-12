package edu.pitt.dbmi.nlp.noble.ontology.owl;


import edu.pitt.dbmi.nlp.noble.ontology.*;
import edu.pitt.dbmi.nlp.noble.ontology.concept.ConceptRegistry;
import edu.pitt.dbmi.nlp.noble.terminology.Concept;
import edu.pitt.dbmi.nlp.noble.util.StringUtils;
import org.coode.owlapi.obo.parser.OBOOntologyFormat;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OWL Ontology implementation.
 *
 * @author tseytlin
 */
public class OOntology extends OResource implements IOntology {
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private OWLOntologyManager manager;
	private OWLOntology ontology;
	private OWLDataFactory data;
	private OWLReasoner reasoner;
	private OWLEntityRemover remover;
	private OWLEntityRenamer renamer;
	private OWLOntologyLoaderConfiguration ontologyLoaderConfig;
	private IRepository repository;
	private PrefixManager prefixManager;
	private boolean modified;
	private String location;
	private IRI locationIRI;
	private List<String> languageFilter;
	private Map<String,IRI> iriMap;
	
	/**
	 * create new ow.
	 *
	 * @param ont the ont
	 */
	private OOntology(OWLOntology ont) {
		super(ont);
		manager = ont.getOWLOntologyManager();
		ontology = ont;
		data = manager.getOWLDataFactory();
		setOntology(this);
		prefixManager = manager.getOntologyFormat(ontology).asPrefixOWLOntologyFormat();
		prefixManager = new DefaultPrefixManager(prefixManager);
		ontologyLoaderConfig = new OWLOntologyLoaderConfiguration();
	}


	private Map<String,IRI> getIRIMap(){
		if(iriMap == null)
			iriMap = new HashMap<String, IRI>();
		return iriMap;
	}
	
	/**
	 * Instantiates a new o ontology.
	 *
	 * @param location the location
	 */
	public OOntology(String location) {
		super(null);
		this.location = location;
	}


	public String getLocation() {
		return location == null ? super.getLocation() : location;
	}


	/**
	 * get prefix manager .
	 *
	 * @return the prefix manager
	 */
	PrefixManager getPrefixManager() {
		return prefixManager;
	}

	/**
	 * Lazy load.
	 */
	private void lazyLoad() {
		try {
			load();
		} catch (IOntologyException e) {
			throw new IOntologyError("Unable to load ontology " + location, e);
		}
	}


	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#load()
	 */
	public void load() throws IOntologyException {
		if (!isLoaded() && location != null) {
			try {
				manager = OWLManager.createOWLOntologyManager();
				File f = new File(location);
				// this is file
				if (f.exists()) {
					ontology = manager.loadOntologyFromOntologyDocument(f);
					// this is URL
				} else if (location.matches("[a-zA-Z]+://(.*)")) {
					ontology = manager.loadOntologyFromOntologyDocument(IRI.create(location));
				}
				obj = ontology;
				data = manager.getOWLDataFactory();
				setOntology(this);
				prefixManager = manager.getOntologyFormat(ontology).asPrefixOWLOntologyFormat();

			} catch (OWLOntologyCreationException e) {
				throw new IOntologyException("Unable to create ontology " + location, e);
			}
		}
	}


	/**
	 * Infer IRI.
	 *
	 * @param loc the loc
	 * @return the iri
	 */
	private IRI inferIRI(String loc) {
		if (loc.matches("[a-zA-Z]+://(.*)"))
			return IRI.create(loc);
		File f = new File(location);
		// this is file
		if (f.exists()) {
			try {
				String url = null;
				Pattern p = Pattern.compile("(ontologyIRI|xml:base)=\"(.*?)\"");
				BufferedReader r = new BufferedReader(new FileReader(f));
				for (String l = r.readLine(); l != null; l = r.readLine()) {
					Matcher m = p.matcher(l);
					if (m.find()) {
						url = m.group(2);
						break;
					}
				}
				r.close();
				if(url == null)
					throw new IOntologyError("Unable to find ontology URI inside "+loc);
				return IRI.create(url);
			} catch (FileNotFoundException e) {
				throw new IOntologyError("Error reading ontology from " + location, e);
			} catch (IOException e) {
				throw new IOntologyError("Error reading ontology from " + location, e);
			}
		}
		return null;
	}

	/**
	 * load ontology from file.
	 *
	 * @param file the file
	 * @return the o ontology
	 * @throws IOntologyException the i ontology exception
	 */
	public static OOntology loadOntology(File file) throws IOntologyException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		setupLocalIRImapper(manager, file);
		try {
			return new OOntology(manager.loadOntologyFromOntologyDocument(file));
		} catch (OWLOntologyCreationException e) {
			throw new IOntologyException("Unable to create ontology " + file, e);
		}
	}

	/**
	 * setup local IRI mapper from an local ontology file
	 *
	 * @param manager
	 * @param dir
	 */
	private static void setupLocalIRImapper(OWLOntologyManager manager, File file) {
		for(File dir: Arrays.asList(file.getParentFile(),DefaultRepository.DEFAULT_ONTOLOGY_LOCATION)){
			if(dir.exists()){
				for (File f : dir.listFiles()) {
					if (f.getName().endsWith(".owl") && !file.equals(f)) {
						URI uri = null;
						try {
							uri = OntologyUtils.getOntologyURI(f);
						} catch (IOException e) {
							new IOntologyException("Error: unable to extract URI from file " + f, e);
						}
						if (uri != null)
							manager.addIRIMapper(new SimpleIRIMapper(IRI.create(uri), IRI.create(f)));
					}
				}
			}
		}
	}


	/**
	 * load ontology from file.
	 *
	 * @param url the url
	 * @return the o ontology
	 * @throws IOntologyException the i ontology exception
	 */
	public static OOntology loadOntology(String url) throws IOntologyException {
		File f = new File(url);
		if (f.exists())
			return loadOntology(f);
		if (url.startsWith("http://")) {
			try {
				return loadOntology(new URL(url));
			} catch (MalformedURLException e) {
				throw new IOntologyError("This is not a valid URL: " + url, e);
			}
		}
		throw new IOntologyException("Unable to load ontology " + url);
	}


	/**
	 * create new ontology with this URI.
	 *
	 * @param uri the uri
	 * @return the o ontology
	 * @throws IOntologyException the i ontology exception
	 */
	public static OOntology createOntology(URI uri) throws IOntologyException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		//manager.addOntologyStorer(new OWLXMLOntologyStorer());
		try {
			return new OOntology(manager.createOntology(IRI.create(uri)));
		} catch (OWLOntologyCreationException e) {
			throw new IOntologyException("Unable to create ontology " + uri, e);
		}
	}


	/**
	 * create new ontology with this URI.
	 *
	 * @param ontologyURI the uri of future ontology
	 * @param parentURI   the uri of parent ontology
	 * @return the o ontology
	 * @throws IOntologyException the i ontology exception
	 */
	public static OOntology createOntology(URI ontologyURI, URI parentURI) throws IOntologyException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		//manager.addOntologyStorer(new OWLXMLOntologyStorer());
		try {

			OWLOntology ont = manager.createOntology(IRI.create(ontologyURI));
			OWLImportsDeclaration importDeclaraton = manager.getOWLDataFactory().getOWLImportsDeclaration(IRI.create(parentURI));
			manager.makeLoadImportRequest(importDeclaraton, new OWLOntologyLoaderConfiguration());
			manager.applyChange(new AddImport(ont, importDeclaraton));

			return new OOntology(ont);
		} catch (OWLOntologyCreationException e) {
			throw new IOntologyException("Unable to create ontology " + ontologyURI, e);
		}
	}

	/**
	 * create new ontology with this URI and parent location
	 *
	 * @param ontologyURI    the uri of future ontology
	 * @param location the file or URL of parent ontology
	 * @return the o ontology
	 * @throws IOntologyException the i ontology exception
	 */
	public static OOntology createOntology(URI ontologyURI, String location) throws IOntologyException {
		File f = new File(location);
		if (f.exists()) {
			return createOntology(ontologyURI, f);
		} else if (location.startsWith("http:")){
			return createOntology(ontologyURI,URI.create(location));
		}else{
			throw new IOntologyException("Unable to identify ontology schema location "+location);
		}
	}
	
	
	/**
	 * create new ontology with this URI.
	 *
	 * @param ontologyURI the uri of future ontology
	 * @param parentOntology the file of parent ontology
	 * @return the o ontology
	 * @throws IOntologyException the i ontology exception
	 */
	public static OOntology createOntology(URI ontologyURI, File parentOntology) throws IOntologyException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		setupLocalIRImapper(manager,parentOntology);
		//manager.addOntologyStorer(new OWLXMLOntologyStorer());
		try{
			URI parentURI = OntologyUtils.getOntologyURI(parentOntology);
			manager.addIRIMapper(new SimpleIRIMapper(IRI.create(parentURI), IRI.create(parentOntology)));
			manager.loadOntologyFromOntologyDocument(parentOntology);
			OWLOntology ont = manager.createOntology(IRI.create(ontologyURI));
			OWLImportsDeclaration importDeclaraton = manager.getOWLDataFactory().getOWLImportsDeclaration(IRI.create(parentURI));
			manager.makeLoadImportRequest(importDeclaraton,new OWLOntologyLoaderConfiguration());
			manager.applyChange(new AddImport(ont,importDeclaraton));
			
			return new OOntology(ont);
		} catch (OWLOntologyCreationException e) {
			throw new IOntologyException("Unable to create ontology "+ontologyURI,e);
		} catch (IOException e) {
			throw new IOntologyException("Unable to create ontology "+ontologyURI,e);
		}
	}
	
	
	
	/**
	 * load ontology from uri.
	 *
	 * @param file the file
	 * @return the o ontology
	 * @throws IOntologyException the i ontology exception
	 */
	public static OOntology loadOntology(URL file) throws IOntologyException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		try {
			return new OOntology(manager.loadOntologyFromOntologyDocument(IRI.create(file)));
		} catch (OWLOntologyCreationException e) {
			throw new IOntologyException("Unable to create ontology "+file,e);
		} catch (URISyntaxException e) {
			throw new IOntologyException("Unable to create ontology "+file,e);
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#removePropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#addImportedOntology(edu.pitt.dbmi.nlp.noble.ontology.IOntology)
	 */
	public void addImportedOntology(IOntology o) throws IOntologyException {
		lazyLoad();
		IRI toImport=IRI.create(o.getURI());
		OWLImportsDeclaration importDeclaraton = getOWLDataFactory().getOWLImportsDeclaration(toImport);
		getOWLOntologyManager().applyChange(new AddImport(getOWLOntology(),importDeclaraton));
		try {
			getOWLOntologyManager().makeLoadImportRequest(importDeclaraton,ontologyLoaderConfig);
		} catch (UnloadableImportException e) {
			throw new IOntologyError("Unable to load ontology "+o.getURI(),e);
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#removeImportedOntology(edu.pitt.dbmi.nlp.noble.ontology.IOntology)
	 */
	public void removeImportedOntology(IOntology o) {
		lazyLoad();
		IRI toImport=IRI.create(o.getURI());
		OWLImportsDeclaration importDeclaraton = getOWLDataFactory().getOWLImportsDeclaration(toImport);
		getOWLOntologyManager().applyChange(new RemoveImport(getOWLOntology(),importDeclaraton));
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#getImportedOntologies()
	 */
	public IOntology[] getImportedOntologies() {
		lazyLoad();
		List<IOntology> io = new ArrayList<IOntology>();
		for(OWLOntology o: ontology.getImports()){
			io.add(new OOntology(o));
		}
		return io.toArray(new IOntology [0]);
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#getRepository()
	 */
	public IRepository getRepository() {
		return repository;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#setRepository(edu.pitt.dbmi.nlp.noble.ontology.IRepository)
	 */
	public void setRepository(IRepository r) {
		repository = r;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#createClass(java.lang.String)
	 */
	public IClass createClass(String name) {
		return getRoot().createSubClass(name);
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#createClass(edu.pitt.dbmi.nlp.noble.ontology.ILogicExpression)
	 */
	public IClass createClass(ILogicExpression exp) {
		return (IClass)convertOWLObject((OWLClass)convertOntologyObject(exp));
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#createProperty(java.lang.String, int)
	 */
	public IProperty createProperty(String name, int type) {
		IRI iri = getIRI(name);
		switch(type){
			case(IProperty.OBJECT):
				return getTopObjectProperty().createSubProperty(name);
			case(IProperty.DATATYPE):
				return getTopDataProperty().createSubProperty(name);
			case(IProperty.ANNOTATION_OBJECT):
			case(IProperty.ANNOTATION_DATATYPE):
			case(IProperty.ANNOTATION):
				return (IProperty) convertOWLObject(data.getOWLAnnotationProperty(iri));
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#createLogicExpression(int, java.lang.Object)
	 */
	public ILogicExpression createLogicExpression(int type, Object param) {
		if(param instanceof Collection)
			return new LogicExpression(type,(Collection) param);
		else if(param instanceof Object [])
			return new LogicExpression(type,(Object []) param);
		else
			return new LogicExpression(type,param);
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#createLogicExpression()
	 */
	public ILogicExpression createLogicExpression() {
		return new LogicExpression(ILogicExpression.EMPTY);
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#createRestriction(int)
	 */
	public IRestriction createRestriction(int type) {
		return new ORestriction(type, getOntology());
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#executeQuery(edu.pitt.dbmi.nlp.noble.ontology.IQuery)
	 */
	public IQueryResults executeQuery(IQuery iQuery) {
		throw new IOntologyError("Not implemented yet");
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#getMatchingResources(edu.pitt.dbmi.nlp.noble.ontology.IProperty, java.lang.Object)
	 */
	public IResourceIterator getMatchingResources(IProperty p, Object value) {
		throw new IOntologyError("Not implemented yet");
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#getMatchingResources(java.lang.String)
	 */
	public IResourceIterator getMatchingResources(String regex) {
		lazyLoad();
		List<OWLEntity> list = new ArrayList<OWLEntity>();
		for(OWLEntity e: ontology.getSignature(true)){
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(e.getIRI().toString());
			if(m.find()){
				list.add(e);
			}else{
				for(OWLAnnotation t: e.getAnnotations(ontology,getOWLDataFactory().getRDFSComment())){
					m = p.matcher((String)convertOWLObject(t.getValue()));
					if(m.find()){
						list.add(e);
					}
				}
			}
		}
		return  new OResourceIterator(list,this);
	}
	
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#getResource(java.lang.String)
	 */
	public IResource getResource(String name) {
		IRI iri = getIRI(name);
		if(ontology.containsClassInSignature(iri,true)){
			return (IClass) convertOWLObject(data.getOWLClass(iri));
		}else if(ontology.containsIndividualInSignature(iri,true)){
			return (IInstance) convertOWLObject(data.getOWLNamedIndividual(iri));
		}else if(ontology.containsAnnotationPropertyInSignature(iri,true)){
			return (IProperty) convertOWLObject(data.getOWLAnnotationProperty(iri));
		}else if(ontology.containsDataPropertyInSignature(iri,true)){
			return (IProperty) convertOWLObject(data.getOWLDataProperty(iri));
		}else if(ontology.containsObjectPropertyInSignature(iri,true)){
			return (IProperty) convertOWLObject(data.getOWLObjectProperty(iri));
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#getClass(java.lang.String)
	 */
	public IClass getClass(String name) {
		IRI iri = getIRI(name);
		if(ontology.containsClassInSignature(iri,true))
			return (IClass) convertOWLObject(data.getOWLClass(iri));
		return null;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#getInstance(java.lang.String)
	 */
	public IInstance getInstance(String name) {
		IRI iri = getIRI(name);
		if(ontology.containsIndividualInSignature(iri,true))
			return (IInstance) convertOWLObject(data.getOWLNamedIndividual(iri));
		return null;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#getProperty(java.lang.String)
	 */
	public IProperty getProperty(String name) {
		IRI iri = getIRI(name);
		if(ontology.containsAnnotationPropertyInSignature(iri,true)){
			return (IProperty) convertOWLObject(data.getOWLAnnotationProperty(iri));
		}else if(ontology.containsDataPropertyInSignature(iri,true)){
			return (IProperty) convertOWLObject(data.getOWLDataProperty(iri));
		}else if(ontology.containsObjectPropertyInSignature(iri,true)){
			return (IProperty) convertOWLObject(data.getOWLObjectProperty(iri));
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#hasResource(java.lang.String)
	 */
	public boolean hasResource(String path) {
		return ontology.containsEntityInSignature(getIRI(path),true);
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#getRootClasses()
	 */
	public IClass[] getRootClasses() {
		return getRoot().getDirectSubClasses();
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#getRoot()
	 */
	public IClass getRoot() {
		return (IClass) convertOWLObject(data.getOWLThing());
	}
	
	/**
	 * Gets the thing.
	 *
	 * @return the thing
	 */
	public IClass getThing() {
		return (IClass) convertOWLObject(data.getOWLThing());
	}
	
	/**
	 * Gets the nothing.
	 *
	 * @return the nothing
	 */
	public IClass getNothing() {
		return (IClass) convertOWLObject(data.getOWLNothing());
	}
	
	/**
	 * Gets the top object property.
	 *
	 * @return the top object property
	 */
	public IProperty getTopObjectProperty() {
		return (IProperty) convertOWLObject(data.getOWLTopObjectProperty());
	}
	
	/**
	 * Gets the top data property.
	 *
	 * @return the top data property
	 */
	public IProperty getTopDataProperty() {
		return (IProperty) convertOWLObject(data.getOWLTopDataProperty());
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#getAllResources()
	 */
	public IResourceIterator getAllResources() {
		return  new OResourceIterator(ontology.getSignature(true),this);
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#getAllProperties()
	 */
	public IResourceIterator getAllProperties() {
		List list = new ArrayList();
		list.addAll(ontology.getDataPropertiesInSignature(true));
		list.addAll(ontology.getObjectPropertiesInSignature(true));
		list.addAll(ontology.getAnnotationPropertiesInSignature());
		return new OResourceIterator(list,this);
		
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#getAllClasses()
	 */
	public IResourceIterator getAllClasses() {
		return new OResourceIterator(ontology.getClassesInSignature(true),this);
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#isLoaded()
	 */
	public boolean isLoaded() {
		return ontology != null;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#reload()
	 */
	public void reload() throws IOntologyException {
		dispose();
		load();
		iriMap = null;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#flush()
	 */
	public void flush() {}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.owl.OResource#getNameSpace()
	 */
	public String getNameSpace(){
		return getIRI().toString()+"#";
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#save()
	 */
	public void save() throws IOntologyException {
		modified = false;
		try {
			manager.saveOntology(ontology);
		} catch (OWLOntologyStorageException e) {
			if(e.getCause() instanceof ProtocolException)
				throw new IOntologyException("Unable to save ontology opened from URL "+getIRI()+". You should use IOntology.write() to save it as a file first.",e);
			throw new IOntologyException("Unable to save ontology "+getIRI(),e);
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#write(java.io.OutputStream, int)
	 */
	public void write(OutputStream out, int format) throws IOntologyException {
		OWLOntologyFormat ontologyFormat = manager.getOntologyFormat(ontology);
		switch(format){
		case OWL_FORMAT:
			ontologyFormat = new OWLXMLOntologyFormat();break;
		case RDF_FORMAT:
			ontologyFormat = new RDFXMLOntologyFormat();break;
		case NTRIPLE_FORMAT:
			throw new IOntologyException("Unsupported export format");
		case OBO_FORMAT:
			ontologyFormat = new OBOOntologyFormat();break;
		case TURTLE_FORMAT:
			ontologyFormat = new TurtleOntologyFormat();break;
		}
		
		
		try {
			manager.saveOntology(ontology, ontologyFormat, out);
		} catch (OWLOntologyStorageException e) {
			if(e.getCause() instanceof ProtocolException)
				throw new IOntologyException("Unable to save ontology opened from URL "+getIRI()+". You should use IOntology.write() to save it as a file first.",e);
			throw new IOntologyException("Unable to save ontology "+getIRI(),e);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IOntology#isModified()
	 */
	public boolean isModified() {
		return modified;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.owl.OResource#getOWLEntityRemover()
	 */
	protected OWLEntityRemover getOWLEntityRemover(){
		if(remover == null){ 
			remover = new OWLEntityRemover(manager,Collections.singleton(ontology));
		}
		return remover;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.owl.OResource#getOWLEntityRenamer()
	 */
	protected OWLEntityRenamer getOWLEntityRenamer(){
		if(renamer == null){ 
			renamer = new OWLEntityRenamer(manager,Collections.singleton(ontology));
		}
		return renamer;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.owl.OResource#getOWLOntology()
	 */
	protected OWLOntology getOWLOntology(){
		return ontology;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.owl.OResource#getOWLDataFactory()
	 */
	protected OWLDataFactory getOWLDataFactory(){
		lazyLoad();
		return data;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.owl.OResource#getOWLOntologyManager()
	 */
	protected OWLOntologyManager getOWLOntologyManager(){
		lazyLoad();
		return manager;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.owl.OResource#getOWLReasoner()
	 */
	protected OWLReasoner getOWLReasoner(){
		if(reasoner == null)
			reasoner = new StructuralReasonerFactory().createReasoner(ontology);
		return reasoner;
	}
	
	
	/**
	 * get appropriate concept for a given class.
	 *
	 * @param cls the cls
	 * @return the concept
	 */
	public Concept getConcept(IResource cls){
		// lets see if we have any special concept handlers defined
		for(String pt: ConceptRegistry.REGISTRY.keySet()){
			// if regular expression or simple equals
			if((pt.matches("/.*/") && getURI().toString().matches(pt.substring(1,pt.length()-1))) || getURI().toString().startsWith(pt)){
				String className = ConceptRegistry.REGISTRY.get(pt);
				try {
					Class c = Class.forName(className);
					return (Concept) c.getConstructors()[0].newInstance(cls);
				}catch(Exception ex){
					ex.printStackTrace();
					//NOOP, just do default
				}
			}
		}
		return new Concept(cls);
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.owl.OResource#getIRI()
	 */
	protected IRI getIRI(){
		if(!isLoaded()){
			if(locationIRI == null)
				locationIRI = inferIRI(location);
			return locationIRI;
		}
		return ontology.getOntologyID().getOntologyIRI();
	}
	
	

	/**
	 * convert name of resource to IRI.
	 *
	 * @param name the name
	 * @return the iri
	 */
	protected IRI getIRI(String name){
		if(name == null)
			return null;
		// lazy load ontology for most things
		lazyLoad();
		
		//full URI given
		if(name.indexOf("://") > -1)
			return IRI.create(name);
	
		// get saved key
		if(getIRIMap().containsKey(name))
			return getIRIMap().get(name);
		
		
		//prefix given
		int of = name.indexOf(":"); 
		if( of > -1){
			String p = prefixManager.getPrefix(name.substring(0,of+1));
			IRI iri =  IRI.create(p+name.substring(of+1));
			getIRIMap().put(name,iri);
			return iri;
		}
		// just name is given
		Map<String,String> prefixes = prefixManager.getPrefixName2PrefixMap();
		for(String p: prefixes.keySet()){
			String val = prefixes.get(p);
			if(!p.equals(":") && lookupIRI(val)){
				IRI iri = getIRI(val+name);
				if(ontology.containsEntityInSignature(iri,true)){
					getIRIMap().put(name,iri);
					return iri; 
				}
			}
		}
		// do we have in one of imports?
		for(OWLOntology o: ontology.getImports()){
			IRI iri = getIRI(o.getOntologyID().getOntologyIRI()+"#"+name);
			if(ontology.containsEntityInSignature(iri,true)){
				getIRIMap().put(name,iri);
				return iri; 
			}
		}
		
		// use default
		IRI iri = IRI.create(getNameSpace()+name);
		getIRIMap().put(name,iri);
		return iri;
	}
	
	/**
	 * Lookup IRI.
	 *
	 * @param val the val
	 * @return true, if successful
	 */
	private boolean lookupIRI(String val){
		final String [] generic = new String [] {"w3.org","protege.stanford.edu","purl.org","xsp.owl"};
		for(String s: generic){
			if(val.contains(s))
				return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.owl.OResource#getDescription()
	 */
	public String getDescription() {
		String dsc = "";
		IProperty p = getProperty(IProperty.DC_DESCRIPTION);
		if(p != null)
			dsc = ""+getPropertyValue(p);
		if(dsc.length() == 0) 
			dsc = super.getDescription();
		return dsc;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.owl.OResource#getName()
	 */
	public String getName() {
		return StringUtils.getOntologyName(getURI(),true);
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.owl.OResource#getPropertyValues(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public Object[] getPropertyValues(IProperty prop) {
		OWLOntology e = getOWLOntology();
		if(e != null){
			Set list = new LinkedHashSet();
			for(OWLAnnotation a: e.getAnnotations()){
				if(a.getProperty().equals(convertOntologyObject(prop))){
					Object oo = convertOWLObject(a.getValue());
					if (oo != null){
						list.add(oo);
					}
				}
			}
			return list.toArray();
		}
		return new Object [0];
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.owl.OResource#getProperties()
	 */
	public IProperty[] getProperties() {
		OWLOntology e = getOWLOntology();
		if(e != null){
			Set<IProperty> list = new LinkedHashSet<IProperty>();
			for(OWLAnnotation a: e.getAnnotations()){
				list.add((IProperty)convertOWLObject(a.getProperty()));
			}
			return list.toArray(new IProperty [0]);
		}
		return new IProperty [0];
	}
	
	
	/**
	 * Gets the language filter.
	 *
	 * @return the language filter
	 */
	public List<String> getLanguageFilter() {
		return languageFilter;
	}


	/**
	 * Sets the language filter.
	 *
	 * @param languageFilter the new language filter
	 */
	public void setLanguageFilter(List<String> languageFilter) {
		this.languageFilter = languageFilter;
	}


	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.owl.OResource#addAnnotation(org.semanticweb.owlapi.model.OWLAnnotationProperty, java.lang.String)
	 */
	protected void addAnnotation(OWLAnnotationProperty prop,String str){
		OWLDataFactory df = getOWLDataFactory();
		OWLAnnotation commentAnno = df.getOWLAnnotation(prop,df.getOWLLiteral(str));
		getOWLOntologyManager().applyChange(new AddOntologyAnnotation(ontology, commentAnno));
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.owl.OResource#removeAnnotation(org.semanticweb.owlapi.model.OWLAnnotationProperty, java.lang.String)
	 */
	protected void removeAnnotation(OWLAnnotationProperty prop,String str){
		OWLDataFactory df = getOWLDataFactory();
		OWLAnnotation commentAnno = df.getOWLAnnotation(prop,df.getOWLLiteral(str));
		getOWLOntologyManager().applyChange(new RemoveOntologyAnnotation(ontology, commentAnno));
	}
	
	
	
	/**
	 * get usage for a given resource
	 * @param resource
	 * @return
	 *
	public List<ILogicExpression> getEquivalentReferencesTo(IResource resource ){
		
		List<ILogicExpression> list = new ArrayList<ILogicExpression>();
		OWLEntity owlEntity = (OWLEntity) ((OResource)resource).getOWLObject();
		Set<OWLAxiom> axioms = getOWLOntology().getReferencingAxioms(owlEntity,true);
		for (OWLAxiom ax : axioms) {
			if(ax instanceof OWLEquivalentClassesAxiom){
				LogicExpression logic = new LogicExpression(ILogicExpression.AND);
				for(OWLClassExpression exp: ((OWLEquivalentClassesAxiom)ax).getClassExpressions()){
					logic.add(convertOWLObject(exp));
				}
				list.add(logic);
			}
		}
		return list;
	}
	*/
}
