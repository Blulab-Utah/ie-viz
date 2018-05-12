package edu.pitt.dbmi.nlp.noble.terminology.impl;

/**
 * improve concept scoring code
 */

import edu.pitt.dbmi.nlp.noble.coder.model.Mention;
import edu.pitt.dbmi.nlp.noble.coder.model.Processor;
import edu.pitt.dbmi.nlp.noble.coder.model.Sentence;
import edu.pitt.dbmi.nlp.noble.ontology.DefaultRepository;
import edu.pitt.dbmi.nlp.noble.ontology.IClass;
import edu.pitt.dbmi.nlp.noble.ontology.IOntology;
import edu.pitt.dbmi.nlp.noble.ontology.IOntologyException;
import edu.pitt.dbmi.nlp.noble.terminology.*;
import edu.pitt.dbmi.nlp.noble.terminology.impl.NobleCoderUtils.*;
import edu.pitt.dbmi.nlp.noble.tools.TextTools;
import edu.pitt.dbmi.nlp.noble.tools.TextTools.StringStats;
import edu.pitt.dbmi.nlp.noble.util.ConceptImporter;
import edu.pitt.dbmi.nlp.noble.util.JDBMMap;
import edu.pitt.dbmi.nlp.noble.util.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static edu.pitt.dbmi.nlp.noble.terminology.impl.NobleCoderUtils.*;


/**
 * Implementation of Terminology using IndexFinder algorithm.
 *
 * @author Eugene Tseytlin (University of Pittsburgh)
 */

public class NobleCoderTerminology extends AbstractTerminology implements Processor<Sentence>{
	// names of property events to monitor progress
	//public static final int CACHE_LIMIT = 10000;
	public static final String LOADING_MESSAGE  = ConceptImporter.LOADING_MESSAGE;
	public static final String LOADING_PROGRESS = ConceptImporter.LOADING_PROGRESS;
	public static final String LOADING_TOTAL    = ConceptImporter.LOADING_TOTAL;
	// names of search methods
	public static final String BEST_MATCH = "best-match";
	public static final String ALL_MATCH = "all-match";
	public static final String PRECISE_MATCH= "precise-match";
	public static final String PARTIAL_MATCH= "partial-match";
	public static final String NONOVERLAP_MATCH = "nonoverlap-match";
	public static final String CUSTOM_MATCH = "custom-match";
	
	public static final String TERM_SUFFIX = ".term";
	//public static final String MEM_FILE = "terminology.mem";
	public static final String TERM_FILE = "terms";
	public static final String CONCEPT_FILE = "concepts";
	public static final String INFO_FILE = "info.txt";
	public static final String SEARCH_PROPERTIES = "search.properties";
	public static final String TEMP_WORD_DIR = "tempWordTable";
	
	private File location;
	private String name;
	private Storage storage;
	//private CacheMap<String,Concept []> cache;
	
	// print rough size and time
	//private final boolean DEBUG = false;
	
	// setting parameters
	/*
	 * stripDigits 			- do not consider digits in text for lookup  (false)
	 * stemWords   			- do porter stemming of terms for storing and lookup (true)
	 * ignoreSmallWords		- do not lookup one-letter words (true)
	 * selectBestCandidate	- if multiple matches for the same term, returned the highest scored one (false)
	 * handleProblemTerms   - if term looks like it can be an abbreviation, do not normalize it for matching (true)
	 * ignoreCommonWords	- do not lookup 100 most frequent English words (false)
	 * scoreConcepts		- perform scoring for best candidate concept for a match (false)
	 * ignoreUsedWords		- do not lookup on a word in text if it is already part of another matched term (false)
	 * 						  If true, there is a big speedup, but there is a potential to miss some matches.
	 * subsumptionMode		- If true, the narrowest concept Ex: 'deep margin', subsumes broader concepts: 'deep' and 'margin' (true)
	 * overlapMode			- If true, concepts are allowed to overlap and share words: 
	 * orderedMode			- If true, an order of words in text has to reflect the synonym term (false)
	 * contiguousMode		- If true, words in a term must be next to eachother within (maxWordGap) (false)
	 * partialMode			- If true, text will match if more then 50% of the synonym words are in text (false)
	 * 
	 * windowSize			- Both a maximum number of words that can form a matched term AND a gap between words to make a match (disabled) 
	 * maxWordGap		- How far words can be apart to be apart as part of a term 
	 */
	
	private boolean stripDigits,crashing,stemWords = true,ignoreSmallWords = true,selectBestCandidate = false, handlePossibleAcronyms = true, stripStopWords = true;
	private boolean ignoreCommonWords,scoreConcepts = true,ignoreAcronyms,ignoreUsedWords = true, compacted;
	private boolean subsumptionMode = true,overlapMode=true, orderedMode, contiguousMode,partialMode; 
	private int windowSize = -1;
	private int maxWordGap = 1;
	private int maxWordsInTerm = 10;
	private double partialMatchThreshold = 0.5;
	private String defaultSearchMethod = BEST_MATCH;
	
	
	
	
	private static File dir;
	private Set<Source> filteredSources;
	private Set<SemanticType> filteredSemanticTypes;
	private Set<String> filteredLanguages;
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	//private boolean cachingEnabled = false,truncateURI = false;
	private long processTime;
	
	/**
	 * isolated storage object that deals with all of the MAPS.
	 *
	 * @author tseytlin
	 */
	public static class Storage implements Serializable{
		public int maxTermsPerWord,totalTermsPerWord;
		public boolean useTempWordFolder;
		private File location;
		private Map<String,Set<String>> wordMap,blacklist;
		private Map<String,WordStat> wordStatMap;
		private Map<String,Set<String>> termMap;
		private Map<String,Set<String>> regexMap;
		private Map<String,Concept.Content> conceptMap;
		private Map<String,String> infoMap;
		private Map<String,Source> sourceMap;
		private Map<String,String> rootMap;
		private Map<String,String> codeMap;
		
		
		/**
		 * Instantiates a new storage.
		 */
		public Storage(){
			init();
		}
		
		/**
		 * Instantiates a new storage.
		 *
		 * @param file the file
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public Storage(File file) throws IOException{
			load(file);
		}
		
		/**
		 * Gets the info map.
		 *
		 * @return the info map
		 */
		public Map<String, String> getInfoMap() {
			return infoMap;
		}
		
		/**
		 * Sets the info map.
		 *
		 * @param infoMap the info map
		 */
		public void setInfoMap(Map<String, String> infoMap) {
			this.infoMap = infoMap;
		}
		
		/**
		 * Gets the word map.
		 *
		 * @return the word map
		 */
		public Map<String, Set<String>> getWordMap() {
			return wordMap;
		}
		
		/**
		 * Gets the word stat map.
		 *
		 * @return the word stat map
		 */
		public Map<String, WordStat> getWordStatMap() {
			return wordStatMap;
		}
		
		/**
		 * Gets the term map.
		 *
		 * @return the term map
		 */
		public Map<String, Set<String>> getTermMap() {
			return termMap;
		}
		
		/**
		 * Gets the regex map.
		 *
		 * @return the regex map
		 */
		public Map<String, Set<String>> getRegexMap() {
			return regexMap;
		}
		
		/**
		 * Gets the concept map.
		 *
		 * @return the concept map
		 */
		public Map<String, Concept.Content> getConceptMap() {
			return conceptMap;
		}
		
		/**
		 * Gets the source map.
		 *
		 * @return the source map
		 */
		public Map<String, Source> getSourceMap() {
			return sourceMap;
		}
		
		/**
		 * Gets the root map.
		 *
		 * @return the root map
		 */
		public Map<String, String> getRootMap() {
			return rootMap;
		}
		
		/**
		 * Gets the code map.
		 *
		 * @return the code map
		 */
		public Map<String, String> getCodeMap() {
			return codeMap;
		}
		
		/**
		 * Gets the blacklist.
		 *
		 * @return the blacklist
		 */
		public Map<String, Set<String>> getBlacklist() {
			return blacklist;
		}
		
		/**
		 * Inits the.
		 */
		public void init(){
			wordMap = new HashMap<String,Set<String>>();
			blacklist = new HashMap<String,Set<String>>();
			wordStatMap = new HashMap<String, WordStat>();
			termMap = new HashMap<String,Set<String>>();
			regexMap = new HashMap<String,Set<String>>();
			conceptMap = new HashMap<String,Concept.Content>();
			infoMap = new HashMap<String,String>();
			sourceMap = new HashMap<String,Source>();
			rootMap = new HashMap<String,String>();
			codeMap = new HashMap<String,String>();
		}
		
		/**
		 * Load.
		 *
		 * @param name the name
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public void load(File name) throws IOException{
			load(name,false);
		}
		
		/**
		 * Load.
		 *
		 * @param location the location
		 * @param readonly the readonly
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public void load(File location,boolean readonly) throws IOException{
			this.location = location;
			String prefix = location.getAbsolutePath()+File.separator+"table";
			
			wordMap = new JDBMMap<String,Set<String>>(prefix,"wordMap",readonly);
			termMap = new JDBMMap<String,Set<String>>(prefix,"termMap",readonly);
			regexMap = new JDBMMap<String,Set<String>>(prefix,"regexMap",readonly);
			wordStatMap = new JDBMMap<String,WordStat>(prefix,"wordStatMap",readonly);
			blacklist = new JDBMMap<String,Set<String>>(prefix,"blacklist",readonly);
			conceptMap = new JDBMMap<String,Concept.Content>(prefix,"conceptMap",readonly);
			infoMap = new JDBMMap<String,String>(prefix,"infoMap",readonly);
			sourceMap = new JDBMMap<String,Source>(prefix,"sourceMap",readonly);
			rootMap = new JDBMMap<String,String>(prefix,"rootMap",readonly);
			codeMap = new JDBMMap<String,String>(prefix,"codeMap",readonly);

			// check if regexMap is kosher since we just changed the contract
			if(readonly && !regexMap.isEmpty()) {
				try {
					regexMap.get("");
				} catch(Exception ex){
					throw new IOException("RegEx table was generated by a previous version of NobleCoder. "+location+" needs to be re-imported.");
				}
			}

		}
		
		/**
		 * Table exists.
		 *
		 * @param tablename the tablename
		 * @return true, if successful
		 */
		public boolean tableExists(String tablename){
			String f = location.getAbsolutePath()+File.separator+"table"+"_"+tablename;
			return new File(f+JDBMMap.JDBM_SUFFIX ).exists();
		}
		
		/**
		 * Gets the location.
		 *
		 * @return the location
		 */
		public File getLocation() {
			return location;
		}
		
		/**
		 * Gets the temp location.
		 *
		 * @return the temp location
		 */
		public File getTempLocation(){
			return new File(location,TEMP_WORD_DIR);
		}
		
		/**
		 * Clear.
		 */
		public void clear(){
			wordMap.clear();
			blacklist.clear();
			wordStatMap.clear();
			termMap.clear();
			regexMap.clear();
			conceptMap.clear();
			infoMap.clear();
			sourceMap.clear();
			rootMap.clear();
			codeMap.clear();
		}
		
		
		
		/**
		 * Checks if is read only.
		 *
		 * @param map the map
		 * @return true, if is read only
		 */
		public boolean isReadOnly(Map map){
			return map instanceof JDBMMap && ((JDBMMap)map).isReadOnly();
		}
		
		/**
		 * Commit.
		 *
		 * @param map the map
		 */
		public void commit(Map map){
			if(map instanceof JDBMMap){
				((JDBMMap) map).commit();
			}
		}
		
		/**
		 * Commit.
		 */
		public void commit(){
			if(wordMap instanceof JDBMMap){
				//commit
				((JDBMMap) wordMap).commit();
				((JDBMMap) blacklist).commit();
				((JDBMMap) wordStatMap).commit();
				((JDBMMap) termMap).commit();
				((JDBMMap) conceptMap).commit();
				((JDBMMap) regexMap).commit();
				((JDBMMap) infoMap).commit();
				((JDBMMap) sourceMap).commit();
				((JDBMMap) rootMap).commit();
				((JDBMMap) codeMap).commit();
			}
		}
		
		/**
		 * Defrag.
		 */
		public void defrag(){
			if(wordMap instanceof JDBMMap){
				//defrag
				((JDBMMap) wordMap).compact();
				((JDBMMap) blacklist).compact();
				((JDBMMap) wordStatMap).compact();
				((JDBMMap) termMap).compact();
				((JDBMMap) conceptMap).compact();
				((JDBMMap) regexMap).compact();
				((JDBMMap) infoMap).compact();
				((JDBMMap) sourceMap).compact();
				((JDBMMap) rootMap).compact();
				((JDBMMap) codeMap).compact();
			}
		}
		
		/**
		 * save all information to disc.
		 */
		public void save(){
			commit();
			defrag();
		}
		
		/**
		 * Dispose.
		 */
		public void dispose(){
			if(wordMap instanceof JDBMMap){
				((JDBMMap) wordMap).dispose();
				((JDBMMap) blacklist).dispose();
				((JDBMMap) wordStatMap).dispose();
				((JDBMMap) termMap).dispose();
				((JDBMMap) conceptMap).dispose();
				((JDBMMap) regexMap).dispose();
				((JDBMMap) infoMap).dispose();
				((JDBMMap) sourceMap).dispose();
				((JDBMMap) rootMap).dispose();
				((JDBMMap) codeMap).dispose();
			}
		}
		
		/**
		 * save object.
		 *
		 * @param location the location
		 * @throws FileNotFoundException the file not found exception
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public void saveObject(File location) throws FileNotFoundException, IOException{
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(location));
			os.writeObject(this);
			os.close();
		}
		
		/**
		 * save object.
		 *
		 * @param location the location
		 * @return the storage
		 * @throws FileNotFoundException the file not found exception
		 * @throws IOException Signals that an I/O exception has occurred.
		 * @throws ClassNotFoundException the class not found exception
		 */
		public static Storage loadObject(File location) throws FileNotFoundException, IOException, ClassNotFoundException{
			ObjectInputStream os = new ObjectInputStream(new FileInputStream(location));
			Object obj = os.readObject();
			os.close();
			return (Storage) obj;
		}
	}

	/**
	 * Checks if is compacted.
	 *
	 * @return true, if is compacted
	 */
	public boolean isCompacted(){
		return compacted;
	}
	
	/*
	public boolean isCachingEnabled() {
		return cachingEnabled;
	}

	public void setCachingEnabled(boolean b) {
		this.cachingEnabled = b;
	}
	*/

	public void setName(String name) {
		this.name = name;
	}

	// init default persistence directory
	static{
		setPersistenceDirectory(DefaultRepository.DEFAULT_TERMINOLOGY_LOCATION);
	}

	/**
	 * set directory where persistence files should be saved.
	 *
	 * @param f the new persistence directory
	 */
	public static void setPersistenceDirectory(File f){
		dir = f;
	}
	
	/**
	 * Sets the location.
	 *
	 * @param location the new location
	 */
	public void setLocation(File location) {
		this.location = location;
		if(storage != null)
			storage.location = location;
	}

	/**
	 * set directory where persistence files should be saved.
	 *
	 * @return the persistence directory
	 */
	public static File getPersistenceDirectory(){
		if(dir != null && !dir.exists())
			dir.mkdirs();
		return dir;
	}
	
	
	/**
	 * represents several word stats.
	 *
	 * @author tseytlin
	 */
	public static class WordStat implements Serializable {
		public int termCount;
		public boolean isTerm;
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString(){
			return "termCount: "+termCount+(isTerm?", is a term":"");
		}
	}
	
	
	/**
	 * initialize empty in-memory terminology that has to be
	 * filled up manual using Terminology.addConcept()
	 */
	public NobleCoderTerminology(){
		init();
	}
	
	/**
	 * initialize empty in-memory terminology that has to be
	 * filled up manual using Terminology.addConcept()
	 *
	 * @param ont the ont
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 * @throws IOntologyException the i ontology exception
	 */
	public NobleCoderTerminology(IOntology ont) throws IOException, TerminologyException, IOntologyException{
		init();
		loadOntology(ont,null,true);
	}
	
	/**
	 * initialize empty in-memory terminology that has to be
	 * filled up manual using Terminology.addConcept()
	 *
	 * @param root class
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 * @throws IOntologyException the i ontology exception
	 */
	public NobleCoderTerminology(IClass root) throws IOException, TerminologyException, IOntologyException{
		init();
		loadOntology(root,null,true);
	}
	
	/**
	 * initialize with in memory maps.
	 */
	public void init(){
		storage = new Storage();
		/*cache = new CacheMap<String, Concept []>(CacheMap.FREQUENCY);
		cache.setSizeLimit(CACHE_LIMIT);*/		
	}
	
	/**
	 * initialize a named terminology that has already been 
	 * persisted on disk.
	 *
	 * @param name the name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public NobleCoderTerminology(String name) throws IOException{
		load(name,true);
	}
	
	/**
	 * initialize a named terminology that either has already been 
	 * persisted on disk, or will be persisted on disk.
	 *
	 * @param name the name
	 * @param readonly the readonly
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public NobleCoderTerminology(String name, boolean readonly) throws IOException{
		load(name,readonly);
	}
	
	/**
	 * initialize a named terminology that either has already been 
	 * persisted on disk, or will be persisted on disk from file.
	 *
	 * @param dir the dir
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public NobleCoderTerminology(File dir) throws IOException{
		setPersistenceDirectory(dir.getParentFile());
		load(dir.getName(),true);
	}
	
	/**
	 * initialize a named terminology that either has already been 
	 * persisted on disk, or will be persisted on disk from file.
	 *
	 * @param dir the dir
	 * @param readonly - boolean is readonly mode
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public NobleCoderTerminology(File dir, boolean readonly) throws IOException{
		setPersistenceDirectory(dir.getParentFile());
		load(dir.getName(),readonly);
	}
	
	/**
	 * check if terminology with a given name exists inside
	 * default persisted directory.
	 *
	 * @param name the name
	 * @return true, if successful
	 */
	public static boolean hasTerminology(String name){
		if(name.endsWith(TERM_SUFFIX))
			name = name.substring(0,name.length()-TERM_SUFFIX.length());
		return new File(getPersistenceDirectory(),name+TERM_SUFFIX).isDirectory();
	}
	
	
	
	/**
	 * get Object representing NobleCoder storage.
	 *
	 * @return the storage
	 */
	public Storage getStorage() {
		return storage;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		dispose();
	}

	/**
	 * add property change listener to subscribe to progress messages.
	 *
	 * @param l the l
	 */
	public void addPropertyChangeListener(PropertyChangeListener l){
		pcs.addPropertyChangeListener(l);
		ConceptImporter.getInstance().addPropertyChangeListener(l);
	}
	
	/**
	 * add property change listener to subscribe to progress messages.
	 *
	 * @param l the l
	 */
	public void removePropertyChangeListener(PropertyChangeListener l){
		pcs.removePropertyChangeListener(l);
		ConceptImporter.getInstance().removePropertyChangeListener(l);
	}
	
	/**
	 * load persitent tables.
	 *
	 * @param name the name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void load(String name) throws IOException{
		load(name,false);
	}
	
	/**
	 * load persitent tables.
	 *
	 * @param name the name
	 * @param readonly the readonly
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void load(String name,boolean readonly) throws IOException{
		if(name.endsWith(TERM_SUFFIX))
			name = name.substring(0,name.length()-TERM_SUFFIX.length());
		this.name = name;
		
		// setup location
		if(name.contains(File.separator))
			location = new File(name+TERM_SUFFIX);
		else
			location = new File(getPersistenceDirectory(),name+TERM_SUFFIX);
		
		// check if location exists
//		if(readonly && !location.exists())
//			throw new FileNotFoundException("Cannot open a non-existing terminology file in read-only mode: "+location.getAbsolutePath());
		
		// create a directory
		if(!location.exists())
			location.mkdirs();
		
		storage = new Storage();
		storage.load(location,readonly);
		
		// split into two seperate 
		/*
		File memFile = new File(location,MEM_FILE);
		if(memFile.exists()){
			//TODO: not very efficient
			try {
				storage = Storage.loadObject(memFile);
			} catch (ClassNotFoundException e) {
				throw new IOException(e);
			}
		}else{
			storage = new Storage();
			storage.load(location,readonly);
		}
		cache = new CacheMap<String, Concept []>(CacheMap.FREQUENCY);
		cache.setSizeLimit(CACHE_LIMIT);
		*/
		
		// load default values
		if(storage.getInfoMap().containsKey("stem.words"))
			stemWords = Boolean.parseBoolean(storage.getInfoMap().get("stem.words"));
		if(storage.getInfoMap().containsKey("strip.digits"))
			stripDigits = Boolean.parseBoolean(storage.getInfoMap().get("strip.digits"));
		if(storage.getInfoMap().containsKey("strip.stop.words"))
			stripStopWords = Boolean.parseBoolean(storage.getInfoMap().get("strip.stop.words"));
		if(storage.getInfoMap().containsKey("ignore.small.words"))
			ignoreSmallWords = Boolean.parseBoolean(storage.getInfoMap().get("ignore.small.words"));
		if(storage.getInfoMap().containsKey("max.words.in.term"))
			maxWordsInTerm = Integer.parseInt(storage.getInfoMap().get("max.words.in.term"));
		if(storage.getInfoMap().containsKey("compacted"))
			compacted = Boolean.parseBoolean(storage.getInfoMap().get("compacted"));
		
		//if(storage.getInfoMap().containsKey("handle.possible.acronyms"))
		//	handleProblemTerms = Boolean.parseBoolean(storage.getInfoMap().get("handle.possible.acronyms"));
		
		// load optional search options
		File sp = new File(location,SEARCH_PROPERTIES);
		if(sp.exists()){
			// pull this file
			Properties p = new Properties();
			FileReader r = new FileReader(sp);
			p.load(r);
			r.close();
			
			// lookup default search method
			setSearchProperties(this,p);
		}
		
		// load info file for better meta-info
		File ip = new File(location,INFO_FILE);
		if(!readonly && ip.exists()){
			loadMetaInfo(ip);
		}
		
	}
	
	/**
	 * Reload.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void reload() throws IOException{
		dispose();
		load(name);
	}
	
	/**
	 * Gets the terminology properties.
	 *
	 * @return the terminology properties
	 */
	public Map<String,String> getTerminologyProperties(){
		return storage.getInfoMap();
	}
	
	
	/**
	 * get properties map with search options.
	 *
	 * @return the search properties
	 */
	public Properties getSearchProperties(){
		return NobleCoderUtils.getSearchProperties(this);
	}
	
	
	/**
	 * save meta information.
	 *
	 * @param f the f
	 */
	private void loadMetaInfo(File f){
		try{
			for(String l: TextTools.getText(new FileInputStream(f)).split("\n")){
				if(l.trim().length() > 0){
					int n = l.indexOf(':');
					String key = l.substring(0,n).trim();
					String val = l.substring(n+1).trim();
					storage.getInfoMap().put(key,val);
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}
	
	
	
	/**
	 * get the entire set of concept codes.
	 *
	 * @return the all concepts
	 */
	public Set<String> getAllConcepts(){
		return storage.getConceptMap().keySet();
	}
	
	/**
	 * get all available concept objects in terminology. Only sensible for small terminologies
	 *
	 * @return the concepts
	 * @throws TerminologyException the terminology exception
	 */
	public Collection<Concept> getConcepts()  throws TerminologyException{
		List<Concept> list = new ArrayList<Concept>();
		for(Concept.Content c: storage.getConceptMap().values()){
			list.add(convertConcept(c));
		}
		return list;
	}
	
	/**
	 * reload tables to save space.
	 */
	public void crash(){
		if(crashing)
			return;
		crashing = true;
		// save current content 
		pcs.firePropertyChange(LOADING_MESSAGE,null,"Running low on memory.Saving work and crashing ...");
		save();
		System.exit(1);
		crashing = false;
	}
	
	
	/**
	 * load index finder tables from an IOntology object.
	 *
	 * @param ontology the ontology
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 * @throws IOntologyException the i ontology exception
	 */
	public void loadOntology(IOntology ontology) throws IOException, TerminologyException, IOntologyException {
		loadOntology(ontology,null);
	}
	
	/**
	 * load index finder tables from an IOntology object.
	 *
	 * @param ontology the ontology
	 * @param name the name
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 * @throws IOntologyException the i ontology exception
	 */
	public void loadOntology(IOntology ontology, String name) throws IOException, TerminologyException, IOntologyException {
		loadOntology(ontology,name,false);
	}
	
	/**
	 * load index finder tables from an IOntology object.
	 *
	 * @param ontology the ontology
	 * @param name the name
	 * @param inmemory the inmemory
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 * @throws IOntologyException the i ontology exception
	 */
	public void loadOntology(IOntology ontology, String name, boolean inmemory) throws IOException, TerminologyException, IOntologyException {
		ConceptImporter.getInstance().setInMemory(inmemory);
		ConceptImporter.getInstance().loadOntology(this, ontology, name);
	}
	
	/**
	 * load index finder tables from an IOntology object.
	 *
	 * @param root the root class to import from
	 * @param name the name
	 * @param inmemory the inmemory
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 * @throws IOntologyException the i ontology exception
	 */
	public void loadOntology(IClass root, String name, boolean inmemory) throws IOException, TerminologyException, IOntologyException {
		ConceptImporter.getInstance().setInMemory(inmemory);
		ConceptImporter.getInstance().loadOntology(this, root, name);
	}
	
	/**
	 * load from RRF files (Rich Release Files)
	 * This is a common distribution method for UMLS and NCI Meta.
	 *
	 * @param dir the dir
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 */
	public void loadRRF(File dir) throws FileNotFoundException, IOException, TerminologyException {
		Map<String,List<String>> params = new HashMap<String, List<String>>();
		params.put("languages",Arrays.asList("ENG"));
		loadRRF(dir,params);
	}
	
	
	/**
	 * load from RRF files (Rich Release Files)
	 * This is a common distribution method for UMLS and NCI Meta.
	 *
	 * @param dir the dir
	 * @param params the params
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 */
	public void loadRRF(File dir, Map<String,List<String>> params) throws FileNotFoundException, IOException, TerminologyException {
		ConceptImporter.getInstance().loadRRF(this, dir, params);
	}
	
	/**
	 * load from RRF files (Rich Release Files)
	 * This is a common distribution method for UMLS and NCI Meta.
	 *
	 * @param dir the dir
	 * @param params the params
	 * @param inmem the inmem
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 */
	public void loadRRF(File dir, Map<String,List<String>> params,boolean inmem) throws FileNotFoundException, IOException, TerminologyException {
		ConceptImporter.getInstance().setInMemory(inmem);
		ConceptImporter.getInstance().loadRRF(this, dir, params);
	}
	
	/**
	 * load terms file.
	 *
	 * @param file the file
	 * @param name the name
	 * @throws Exception the exception
	 */
	public void loadText(File file,String name) throws Exception {
		ConceptImporter.getInstance().loadText(this,file, name);
	}
	
	/**
	 * load terms file.
	 *
	 * @param file the file
	 * @param name the name
	 * @param term the term
	 * @throws Exception the exception
	 */
	public void loadText(File file,String name,Terminology term) throws Exception {
		ConceptImporter.getInstance().loadText(this,file, name,term);
	}
	
	/**
	 * returns true if this terminology doesn't contain any terms.
	 *
	 * @return true, if is empty
	 */
	public boolean isEmpty(){
		return storage.getWordMap().isEmpty();
	}
	
	
	/**
	 * clear storage 
	 *
	 */
	public void clear(){
		storage.clear();
	}
	
	/**
	 * clear cache
	 *
	public void clearCache(){
		cache.clear();
	}
	*/
	/**
	 * save all information to disc
	 */
	public void save(){
		pcs.firePropertyChange(LOADING_PROGRESS,null,"Saving Index Finder Tables ...");
		saveMetaInfo(this,new File(location,INFO_FILE));
		saveSearchProperteis(this);
		storage.save();
	}
	
	/**
	 * Dispose.
	 */
	public void dispose(){
		storage.dispose();
	}
	
	/**
	 * ignore digits in concept names for matching
	 * default is false.
	 *
	 * @param b the new ignore digits
	 */
	public void setIgnoreDigits(boolean b){
		stripDigits = b;
	}
	
	
	/**
	 * Checks if is strip stop words.
	 *
	 * @return true, if is strip stop words
	 */
	public boolean isStripStopWords() {
		return stripStopWords;
	}

	/**
	 * Sets the strip stop words.
	 *
	 * @param stripStopWords the new strip stop words
	 */
	public void setStripStopWords(boolean stripStopWords) {
		this.stripStopWords = stripStopWords;
	}

	/**
	 * use porter stemmer to stem words during search
	 * default is true.
	 *
	 * @param stemWords the new stem words
	 */
	public void setStemWords(boolean stemWords) {
		this.stemWords = stemWords;
	}

	/**
	 * Checks if is stem words.
	 *
	 * @return true, if is stem words
	 */
	public boolean isStemWords(){
		return stemWords;
	}
	
	/**
	 * ignore one letter words to avoid parsing common junk
	 * default is true.
	 *
	 * @param ignoreSmallWords the new ignore small words
	 */

	public void setIgnoreSmallWords(boolean ignoreSmallWords) {
		this.ignoreSmallWords = ignoreSmallWords;
	}



	/**
	 * add concept to terminology.
	 *
	 * @param c the c
	 * @return true, if successful
	 * @throws TerminologyException the terminology exception
	 */
	public boolean addConcept(Concept c) throws TerminologyException {
		// don't go into classes that we already visited
		if (storage.getConceptMap().containsKey(c.getCode()))
			return true;

		// check if read only
		if (storage.isReadOnly(storage.getConceptMap())) {
			dispose();
			try {
				load(name, false);
			} catch (IOException e) {
				throw new TerminologyException("Unable to gain write access to data tables", e);
			}
		}

		// get list of terms
		Set<String> terms = getNormalizedTerms(this, c);
		for (String term : terms) {
			// check if term is a regular expression
			if (isRegExp(term)) {
				String regex = term.substring(1, term.length() - 1);
				try {
					Pattern.compile(regex);
					// if pattern matches non-word characters, then  don't use  word, boundaries
					if(regex.matches("\\W+"))
						regex = "("+regex+")";
					else
						regex = "\\b(" + regex + ")\\b";

					// insert concept concept into a set
					Set<String> codeList = new HashSet<String>();
					codeList.add(c.getCode());
					// add concept codes that were already in a set
					if (storage.getRegexMap().containsKey(regex)) {
						codeList.addAll(storage.getRegexMap().get(regex));
					}
					storage.getRegexMap().put(regex, codeList);
					//storage.getRegexMap().put(regex, c.getCode());
				} catch (PatternSyntaxException ex) {
					pcs.firePropertyChange(LOADING_MESSAGE, null, "Warning: failed to add regex /" + regex
							+ "/ as synonym, because of pattern error : " + ex.getMessage());
				}
			} else {
				// insert concept concept into a set
				Set<String> codeList = new HashSet<String>();
				codeList.add(c.getCode());
				// add concept codes that were already in a set
				if (storage.getTermMap().containsKey(term)) {
					codeList.addAll(storage.getTermMap().get(term));
				}
				// insert the set
				storage.getTermMap().put(term, codeList);

				// insert words
				for (String word : TextTools.getWords(term)) {
					Set<String> termList = singleton(term); 
					saveWordTermsInStorage(getStorage(), word, termList);
					saveWordStats(storage, termList, word);
				}
			}

		}
		storage.getConceptMap().put(c.getCode(), c.getContent());

		// now, why can't we insert on other valid codes :) ???? I think we can
		for (Object code : c.getCodes().values()) {
			//only put stuff that is not there already and that is not complete BS s.a. MTH NOCODE
			if (!storage.getCodeMap().containsKey(code) && !"NOCODE".equals(code))
				storage.getCodeMap().put(code.toString(), c.getCode());
		}

		return true;
	}
	
	/**
	 * add concept as a root.
	 *
	 * @param code the code
	 * @return true, if successful
	 */
	public boolean addRoot(String code){
		if(storage.getConceptMap().containsKey(code)){
			storage.getRootMap().put(code,"");	
			return true;
		}
		return false;
	}
	
	
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.AbstractTerminology#removeConcept(edu.pitt.dbmi.nlp.noble.terminology.Concept)
	 */
	public boolean removeConcept(Concept c) throws TerminologyException {
		// find concept terms
		if(storage.getConceptMap().containsKey(c.getCode()))
			c = convertConcept(storage.getConceptMap().get(c.getCode()));
		Set<String> terms = getNormalizedTerms(this,c);
		// remove all terms and words
		for(String term: terms){
			storage.getTermMap().remove(term);
			//remove from words
			for(String word: TextTools.getWords(term)){
				Set<String> list = getWordTerms(word);
				if(list != null){
					list.remove(term);
					// if the only entry, remove the word as well
					if(list.isEmpty())
						storage.getWordMap().remove(word);	
				}
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.AbstractTerminology#updateConcept(edu.pitt.dbmi.nlp.noble.terminology.Concept)
	 */
	public boolean updateConcept(Concept c) throws TerminologyException {
		removeConcept(c);
		addConcept(c);
		return true;
	}

	/**
	 * Get Search Methods supported by this terminology 
	 * Values are
	 * 
	 * best-match : subsumption of concepts, overlap of concepts
	 * all-match  : overlap of concepts
	 * precise-match: subsumption of concepts, overlap of concepts, contiguity of term
	 * nonoverlap-match: subsumption of concepts
	 * partial-match: partial term match, overlap of concepts
	 * custom-match: use flags to tweak search.
	 *
	 * @return the search methods
	 */
	public String[] getSearchMethods() {
		return new String [] {BEST_MATCH,ALL_MATCH,PRECISE_MATCH,PARTIAL_MATCH,CUSTOM_MATCH};
	}
	
	/**
	 * try to find the best possible match for given query.
	 *
	 * @param text the text
	 * @return the concept[]
	 * @throws TerminologyException the terminology exception
	 */
	public Concept[] search(String text) throws TerminologyException {
		return search(text,defaultSearchMethod);
	}
	
	/**
	 * setup search method.
	 *
	 * @param method the new up search
	 */
	private void setupSearch(String method){
		if(method == null)
			method = defaultSearchMethod;
		
		if(BEST_MATCH.equals(method)){
			subsumptionMode = true;
			overlapMode = true;
			contiguousMode = true;
			orderedMode = false;
			partialMode = false;
			maxWordGap = 1;
		}else if(ALL_MATCH.equals(method)){
			subsumptionMode = false;
			overlapMode = true;
			contiguousMode = false;
			orderedMode = false;
			partialMode = false;
			ignoreUsedWords = false;
		}else if(PRECISE_MATCH.equals(method)){
			subsumptionMode = true;
			overlapMode = true;
			contiguousMode = true;
			orderedMode = true;
			partialMode = false;
			maxWordGap = 0;
		}else if(NONOVERLAP_MATCH.equals(method)){
			subsumptionMode = true;
			overlapMode = false;
			contiguousMode = false;
			orderedMode = false;
			partialMode = false;
		}else if(PARTIAL_MATCH.equals(method)){
			subsumptionMode = false;
			overlapMode = false;
			contiguousMode = false;
			orderedMode = false;
			partialMode = true;
		}
		
		// if compacted, you want to disable ignore used words
		if(compacted){
			ignoreUsedWords = false;
			//ignoreSmallWords = false;
			ignoreCommonWords = false;
		}
		
	}
	
	
	/**
	 * How far can the words in a matched term be apart for it to match.
	 * Stop words are ignored in this count
	 * Example: 'red swift dog' won't match 'red dog' if word gap is 0,
	 *          but will match if it is 1 or more.
	 *
	 * @return the maximum word gap
	 */
	public int getMaximumWordGap() {
		return maxWordGap;
	}

	/**
	 * How far can the words in a matched term be apart for it to match.
	 * Stop words are ignored in this count
	 * Example: 'red swift dog' won't match 'red dog' if word gap is 0,
	 *          but will match if it is 1 or more.
	 *
	 * @param wordWindowSize the new maximum word gap
	 */
	public void setMaximumWordGap(int wordWindowSize) {
		this.maxWordGap = wordWindowSize;
	}


	
	/**
	 * try to find the best possible match for given query.
	 *
	 * @param text the text
	 * @param method the method
	 * @return the concept[]
	 * @throws TerminologyException the terminology exception
	 */
	public Concept[] search(String text,String method) throws TerminologyException {
		Map<Concept,Concept> result = new TreeMap<Concept,Concept>(new Comparator<Concept>() {
			public int compare(Concept o1, Concept o2) {
				if(o2.getCode().equals(o1.getCode()))
					return 0;
				int n = (int)(1000 * (o2.getScore()-o1.getScore()));
				if(n == 0)
					return o2.getCode().compareTo(o1.getCode());
				return n;
			}
		});
		// replace default search for the druation of this query
		String ds = defaultSearchMethod;
		defaultSearchMethod = method;
		
		// process sentences with mentions
		List<Mention> mentions  = process(new Sentence(text)).getMentions();
		
		// switch back the search
		defaultSearchMethod = ds;
		
		// now add concepts from mentions back into results
		for(Mention m: mentions){
			Concept c = m.getConcept();
			Concept o = result.get(c);
			if(o == null){
				result.put(c,c);
			}else{
				o.addMatchedTerm(c.getMatchedTerm());
				for(Annotation a: c.getAnnotations()){
					o.addAnnotation(a);
				}
			}
		}
		
		//return result
		return result.keySet().toArray(new Concept[0]);
	}
	
	
	/**
	 * Checks if is acronym.
	 *
	 * @param c the c
	 * @return true, if is acronym
	 */
	private boolean isAcronym(Concept c) {
		for(Term t: c.getTerms()){
			if(("ACR".equals(t.getForm()) || t.getForm().endsWith("AB")) && t.getText().equalsIgnoreCase(c.getMatchedTerm()))
				return true;
		}
		return false;
	}

	
	
	/**
	 * set the maximum size a single term can take, to limit the search for very long input
	 * default is 0, which means no limit.
	 *
	 * @param n the new window size
	 */
	public void setWindowSize(int n){
		windowSize = n;
	}
	
	
	
	/**
	 * get best candidates for all concepts that match a single term.
	 *
	 * @param concepts the concepts
	 * @return the best candidates
	 */
	private List<Concept> getBestCandidates(List<Concept> concepts){
		final double THRESHOLD = 0.0;
		// do default return original list
		// if concepts were not scored or list is empty
		if(concepts.isEmpty() || !scoreConcepts)
			return concepts;
	
		// if selecting one best candidate
		if(selectBestCandidate){
			// now find best scoring concept in a list
			Concept best = null;
			for(Concept c: concepts){
				if(best == null || best.getScore() < c.getScore())
					best = c;
			}
			return best.getScore() >= THRESHOLD?Collections.singletonList(best):Collections.EMPTY_LIST;
		// else we have scored concepts, but not best candidate
		}else if(scoreConcepts){
			// filter out concepts that FAIL basic scoring
			// independent of how they compare to to other candidates
			for(ListIterator<Concept> i=concepts.listIterator();i.hasNext();){
				if(i.next().getScore() < THRESHOLD)
					i.remove();
			}
		}
		return concepts;
	}
	
	
	
	/**
	 * get all terms associated with a word.
	 *
	 * @param word the word
	 * @return the word terms
	 */
	private Set<String> getWordTerms(String word){
		return storage.getWordMap().get(word);
	}
	
	
	/**
	 * search through regular expressions.
	 *
	 * @param term the term
	 * @return the collection
	 */
	private Collection<Concept> searchRegExp(String term){
		List<Concept> result = null;
		term = new String(term);
		// iterate over expression
		for(String re: storage.getRegexMap().keySet()){
			// match regexp from file to
			Pattern p = Pattern.compile(re,Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher( term );
			while ( m.find() ){
				if(result == null)
					result = new ArrayList<Concept>();
				
				for(String cls_str : storage.getRegexMap().get(re)) {
					String txt = m.group(1);    // THIS BETTER BE THERE,

					// well, we don't care about empty space right???
					// if regex was messed up we don't want to return junk, right???
					if (txt.length() == 0)
						continue;

					//System.out.println(cls_str+" "+txt+" for re: "+re);
					// create concept from class
					Concept c = convertConcept(storage.getConceptMap().get(cls_str));
					c = c.clone();
					c.setTerminology(this);
					c.setSearchString(term);
					Annotation.addAnnotation(c, txt, m.start());

					// check if results already have similar entry
					// if new entry is better replace the old one
					boolean toadd = true;
					for (ListIterator<Concept> it = result.listIterator(); it.hasNext(); ) {
						Concept b = it.next();

						// get offsets of concepts
						int st = c.getOffset();
						int en = c.getOffset() + c.getText().length();

						int stb = b.getOffset();
						int enb = b.getOffset() + b.getText().length();

						// if text is identical, then we need to add both concepts
						if(c.getText().equals(b.getText()))
							continue;

						// if concept b (previous concept) is within concept c (new concept)
						if (st <= stb && enb <= en)
							it.remove();
						else if (stb <= st && en <= enb)
							toadd = false;

					}

					// add concept to result
					if (toadd)
						result.add(c);
				}
				// this is bad, cause this fucks up next match
				// that can potentially overlap, use case ex: \\d vs \\d.\\d
				//term = term.replaceAll(txt,"");
			}
		}
						
		return (result != null)?result:Collections.EMPTY_LIST;
	}
	
	/**
	 * get best term that spans most words.
	 *
	 * @param words in search string
	 * @param swords the swords
	 * @param usedWords the used words
	 * @param word in question
	 * @return the best terms
	 */
	private Collection<String> getBestTerms(List<String> words, Set<String> swords,Set<String> usedWords, String word){
		// get list of terms that have a given word associated with it
		Set<String> terms = storage.getBlacklist().containsKey(word)?storage.getBlacklist().get(word):getWordTerms(word);
		if(terms == null || words.isEmpty())
			return Collections.EMPTY_LIST;
		
		// best-match vs all-match
		// in best-match terms that are subsumed by others are excluded 
		List<String> best = new ArrayList<String>();
		int bestCount = 0;
		for(String term: terms){
			// check if term should not be used 
			//if(isFilteredOut(term))
			//	continue;
			
			boolean all = true;
			int hits = 0;
			List<String> twords  = TextTools.getWords(term);
			
			// if at least one word not in list of words, don't have a match
			for(String tword : twords ){
				// if term word doesn't occur in text, then NO match
				if(!swords.contains(tword)){
					all = false;
					if(!partialMode)
						break;
				}else{
					// if not in overlap mode,then make sure that this term word is not used already
					if(!overlapMode){
						if(usedWords.contains(tword)){
							all = false;
							hits --;
							if(!partialMode)
								break;
						}
					}
					hits++;	
				}
			}
			
			// do partial match
			if(partialMode && !all && hits > 0){
				//all = hits >= twords.length/2.0;
				all = ((double)hits/twords.size()) >= partialMatchThreshold;
			}
			
			// optionally inforce term contiguity in text
			if(all && contiguousMode && twords.size() > 1){
				// go over every word in a sentence
				all = checkContiguity(words, twords,maxWordGap);
			}
			
			
			// optionally inforce term order in text
			if(all && orderedMode && twords.size() > 1){
				// if we are here, lets find the original synonym that matched this normalized term
				// reset all variable, if not ordered
				all = checkWordOrder(words, twords, term);
			}

			
			// if all words match
			if(all){
				// if best-match mode, then keep the best term only
				if(subsumptionMode){
					// select the narrowest best
					if(twords.size() > bestCount){
						best = new ArrayList<String>();
						best.add(term);
						bestCount = twords.size();
					}else if(twords.size() == bestCount){
						best.add(term);
					}
				// else use all-matches mode and keep all of them
				}else{
					best.add(term);
				}
			}	
		}
		return best;
	}
	
	
	
	/**
	 * should the concept be filtered out based on some filtering technique.
	 *
	 * @param c the c
	 * @return true, if is filtered out
	 */
	private boolean isFilteredOut(Concept c) {
		boolean filteredOut = false;
		
		// do not filter anything if filtered sources are not set
		if(filteredSources != null && !filteredSources.isEmpty()){
			filteredOut = true;
			Source [] src = c.getSources();
			if(src != null){
				for(Source s: src){
					// if at least one source is contained 
					// in filter list, then do not filter it out
					if(filteredSources.contains(s)){
						filteredOut = false;
						break;
					}
				}
			}else{
				// if we have no sources set,
				// well, meybe we should use this concept
				filteredOut =  false;
			}
			// if we can't find concept or it doesn't have and sources difined
			// discard it (filter out)
			if(filteredOut)
				return true;
		}
		
		// do not filter anything if filtered semantic types are not set
		if(filteredSemanticTypes != null && !filteredSemanticTypes.isEmpty()){
			filteredOut = true;
			SemanticType [] src = c.getSemanticTypes();
			if(src != null){
				for(SemanticType s: src){
					// if at least one source is contained 
					// in filter list, then do not filter it out
					if(filteredSemanticTypes.contains(s)){
						filteredOut = false;
						break;
					}
				}
			}else{
				// if we have no semantic types set,
				// well, meybe we should use this concept
				filteredOut = false;
			}
			// if we can't find concept or it doesn't have and semantic types difined
			// discard it (filter out)
			if(filteredOut)
				return true;
		}
		// keep concept , if everything else is cool
		
		// if we have a match of a small word that is a common word
		// then check exact case, we don't want to match abbreviations
		// by mistake
		// NEW matching strategy should take care of it now
		/*
		if(handleProblemTerms){
			String term = c.getMatchedTerm();
			if(term != null && term.length() < 5 && TextTools.isCommonWord(term)){
				boolean exactMatch = false;
				for(String s: c.getSynonyms()){
					if(s.equals(term) || (s.startsWith("/") && term.matches("\\b("+s.substring(1,s.length()-1)+")\\b"))){
						exactMatch = true;
						break;
					}
				}
				if(!exactMatch)
					return true;
			}
			
		}
		*/
		
		return filteredOut;
			
	}

	
	/**
	 * get all root concepts. This makes sence if Terminology is in fact ontology
	 * that has heirchichal structure
	 *
	 * @return the root concepts
	 * @throws TerminologyException the terminology exception
	 */
	public Concept[] getRootConcepts() throws TerminologyException {
		List<Concept> roots = new ArrayList<Concept>();
		for(String code: storage.getRootMap().keySet()){
			Concept c = lookupConcept(code);
			if(c != null)
				roots.add(c);
		}
		return roots.toArray(new Concept [0]);
	}
	

	/**
	 * get related concepts map.
	 *
	 * @param c the c
	 * @return the related concepts
	 * @throws TerminologyException the terminology exception
	 */
	public Map getRelatedConcepts(Concept c) throws TerminologyException {
		Map<String,Set<String>> relationMap = c.getRelationMap();
		if(relationMap != null){
			Map<Relation,Concept []> map = new HashMap<Relation,Concept []>();
			for(String key: relationMap.keySet()){
				List<Concept> list = new ArrayList<Concept>();
				for(String cui: relationMap.get(key)){
					Concept con = lookupConcept(cui);
					if(con != null)
						list.add(con);
				}
				map.put(Relation.getRelation(key),list.toArray(new Concept [0]));
			}
			return map;
		}
		
		// else return an empty map
		return Collections.EMPTY_MAP;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.AbstractTerminology#getRelatedConcepts(edu.pitt.dbmi.nlp.noble.terminology.Concept, edu.pitt.dbmi.nlp.noble.terminology.Relation)
	 */
	public Concept[] getRelatedConcepts(Concept c, Relation r) throws TerminologyException {
		// if we have a class already, use the ontology
		if(getRelatedConcepts(c).containsKey(r)){
			return (Concept []) getRelatedConcepts(c).get(r);
		}
		// else return empty list
		return new Concept [0];
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.AbstractTerminology#convertConcept(java.lang.Object)
	 */
	public Concept convertConcept(Object obj) {
		if(obj instanceof Concept)
			return (Concept) obj;
		if(obj instanceof Concept.Content){
			Concept.Content c = (Concept.Content)obj;
			return (c.concept == null)?new Concept(c):c.concept;
		}
		if(obj instanceof String || obj instanceof URI){
			try{
				return lookupConcept(""+obj);
			}catch(Exception ex){
				// should not generate one
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.AbstractTerminology#lookupConcept(java.lang.String)
	 */
	public Concept lookupConcept(String cui) throws TerminologyException {
		Concept c =  convertConcept(storage.getConceptMap().get(cui));
		// try other code mappings
		if(c == null && storage.getCodeMap().containsKey(cui)){
			c =  convertConcept(storage.getConceptMap().get(storage.getCodeMap().get(cui)));
		}
		
		if(c != null){
			c.setTerminology(this);
			c.setInitialized(true);
		}
		return c;
	}
	
	/**
	 * Get all supported relations between concepts.
	 *
	 * @return the relations
	 * @throws TerminologyException the terminology exception
	 */
	public Relation[] getRelations() throws TerminologyException {
		return new Relation [] { Relation.BROADER, Relation.NARROWER, Relation.SIMILAR };
	}

	/**
	 * Get all relations for specific concept, one actually needs to explore
	 * a concept graph (if available) to determine those.
	 *
	 * @param c the c
	 * @return the relations
	 * @throws TerminologyException the terminology exception
	 */
	public Relation[] getRelations(Concept c) throws TerminologyException {
		return getRelations();
	}
	
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.Terminology#getSourceFilter()
	 */
	public Source[] getSourceFilter() {
		return (filteredSources == null)?new Source [0]:filteredSources.toArray(new Source [0]);
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.Terminology#getSemanticTypeFilter()
	 */
	public SemanticType[] getSemanticTypeFilter() {
		return (filteredSemanticTypes == null)?new SemanticType [0]:filteredSemanticTypes.toArray(new SemanticType [0]);
	}
	
	/**
	 * Gets the language filter.
	 *
	 * @return the language filter
	 */
	public String [] getLanguageFilter() {
		return (filteredLanguages == null)?new String [0]:filteredLanguages.toArray(new String [0]);
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.AbstractTerminology#getSources()
	 */
	public Source[] getSources() {
		if(storage != null && !storage.getSourceMap().isEmpty())
			return storage.getSourceMap().values().toArray(new Source [0]);
		return new Source[]{new Source(getName(),getDescription(),""+getURI())};
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.Terminology#setSourceFilter(edu.pitt.dbmi.nlp.noble.terminology.Source[])
	 */
	public void setSourceFilter(Source[] srcs) {
		if(srcs == null || srcs.length == 0)
			filteredSources = null;
		else{
			//if(filteredSources == null)
			filteredSources = new LinkedHashSet();
			Collections.addAll(filteredSources, srcs);
		}
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.Terminology#setSemanticTypeFilter(edu.pitt.dbmi.nlp.noble.terminology.SemanticType[])
	 */
	public void setSemanticTypeFilter(SemanticType[] srcs) {
		if(srcs == null || srcs.length == 0)
			filteredSemanticTypes = null;
		else{
		//if(filteredSemanticTypes == null)
			filteredSemanticTypes = new LinkedHashSet();
			Collections.addAll(filteredSemanticTypes, srcs);
		}
	}
	
	/**
	 * Sets the language filter.
	 *
	 * @param lang the new language filter
	 */
	public void setLanguageFilter(String [] lang) {
		if(filteredLanguages == null)
			filteredLanguages = new LinkedHashSet();
		Collections.addAll(filteredLanguages, lang);
	}
	
	/**
	 * Sets the select best candidate.
	 *
	 * @param selectBestCandidate the new select best candidate
	 */
	public void setSelectBestCandidate(boolean selectBestCandidate) {
		this.selectBestCandidate = selectBestCandidate;
		if(selectBestCandidate)
			this.scoreConcepts = selectBestCandidate;
	}

	/**
	 * Sets the default search method.
	 *
	 * @param s the new default search method
	 */
	public void setDefaultSearchMethod(String s){
		this.defaultSearchMethod = s;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.Describable#getDescription()
	 */
	public String getDescription() {
		if(storage != null && storage.getInfoMap().containsKey("description"))
			return storage.getInfoMap().get("description");
		return "NobleCoderTerminlogy uses an IndexFinder-like algorithm to map text to concepts.";
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.Describable#getFormat()
	 */
	public String getFormat() {
		return "index finder tables";
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.Describable#getLocation()
	 */
	public String getLocation() {
		return (location != null)?location.getAbsolutePath():"memory";
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.Describable#getName()
	 */
	public String getName() {
		if(name != null)
			return name;
		if(storage != null && storage.getInfoMap().containsKey("name"))
			return storage.getInfoMap().get("name");
		if(location != null)
			return location.getName();
		return "NobleCoderTool Terminology";
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.Describable#getURI()
	 */
	public URI getURI() {
		if(storage != null && storage.getInfoMap().containsKey("uri"))
			return URI.create(storage.getInfoMap().get("uri"));
		return URI.create("http://slidetutor.upmc.edu/curriculum/terminolgies/"+getName().replaceAll("\\W+","_"));
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.Describable#getVersion()
	 */
	public String getVersion() {
		if(storage != null && storage.getInfoMap().containsKey("version"))
			return storage.getInfoMap().get("version");
		return "1.0";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return getName();
	}
	
	/**
	 * don't try to match common English words.
	 *
	 * @param ignoreCommonWords the new ignore common words
	 */
	public void setIgnoreCommonWords(boolean ignoreCommonWords) {
		this.ignoreCommonWords = ignoreCommonWords;
	}
	
	/**
	 * Gets the partial match threshold.
	 *
	 * @return the partial match threshold
	 */
	public double getPartialMatchThreshold() {
		return partialMatchThreshold;
	}

	/**
	 * Sets the partial match threshold.
	 *
	 * @param partialMatchThreshold the new partial match threshold
	 */
	public void setPartialMatchThreshold(double partialMatchThreshold) {
		this.partialMatchThreshold = partialMatchThreshold;
	}


	
	/**
	 * comput concept match score.
	 *
	 * @param b the new score concepts
	 */
	public void setScoreConcepts(boolean b) {
		this.scoreConcepts = b;
	}
	
	/**
	 * Checks if is score concepts.
	 *
	 * @return true, if is score concepts
	 */
	public boolean isScoreConcepts(){
		return scoreConcepts;
	}

	/**
	 * Checks if is ignore digits.
	 *
	 * @return true, if is ignore digits
	 */
	public boolean isIgnoreDigits() {
		return stripDigits;
	}

	/**
	 * Checks if is ignore small words.
	 *
	 * @return true, if is ignore small words
	 */
	public boolean isIgnoreSmallWords() {
		return ignoreSmallWords;
	}

	/**
	 * Checks if is ignore common words.
	 *
	 * @return true, if is ignore common words
	 */
	public boolean isIgnoreCommonWords() {
		return ignoreCommonWords;
	}

	/**
	 * Checks if is select best candidate.
	 *
	 * @return true, if is select best candidate
	 */
	public boolean isSelectBestCandidate() {
		return selectBestCandidate;
	}
	
	/**
	 * Gets the window size.
	 *
	 * @return the window size
	 */
	public int getWindowSize() {
		return windowSize;
	}
	
	

	/**
	 * Gets the maximum words in term.
	 *
	 * @return the maximum words in term
	 */
	public int getMaximumWordsInTerm() {
		return maxWordsInTerm;
	}

	/**
	 * Sets the maximum words in term.
	 *
	 * @param maxWordsInTerm the new maximum words in term
	 */
	public void setMaximumWordsInTerm(int maxWordsInTerm) {
		this.maxWordsInTerm = maxWordsInTerm;
	}

	/**
	 * Gets the default search method.
	 *
	 * @return the default search method
	 */
	public String getDefaultSearchMethod() {
		return defaultSearchMethod;
	}
	
	/**
	 * Checks if is ignore acronyms.
	 *
	 * @return true, if is ignore acronyms
	 */
	public boolean isIgnoreAcronyms() {
		return ignoreAcronyms;
	}

	/**
	 * Sets the ignore acronyms.
	 *
	 * @param ignoreAcronyms the new ignore acronyms
	 */
	public void setIgnoreAcronyms(boolean ignoreAcronyms) {
		this.ignoreAcronyms = ignoreAcronyms;
	}
	
	/**
	 * Checks if is ignore used words.
	 *
	 * @return true, if is ignore used words
	 */
	public boolean isIgnoreUsedWords() {
		return ignoreUsedWords;
	}

	/**
	 * Sets the ignore used words.
	 *
	 * @param ignoreUsedWords the new ignore used words
	 */
	public void setIgnoreUsedWords(boolean ignoreUsedWords) {
		this.ignoreUsedWords = ignoreUsedWords;
	}

	/**
	 * Checks if is subsumption mode.
	 *
	 * @return true, if is subsumption mode
	 */
	public boolean isSubsumptionMode() {
		return subsumptionMode;
	}

	/**
	 * Sets the subsumption mode.
	 *
	 * @param subsumptionMode the new subsumption mode
	 */
	public void setSubsumptionMode(boolean subsumptionMode) {
		this.subsumptionMode = subsumptionMode;
	}

	/**
	 * Checks if is overlap mode.
	 *
	 * @return true, if is overlap mode
	 */
	public boolean isOverlapMode() {
		return overlapMode;
	}

	/**
	 * Sets the overlap mode.
	 *
	 * @param overlapMode the new overlap mode
	 */
	public void setOverlapMode(boolean overlapMode) {
		this.overlapMode = overlapMode;
	}

	/**
	 * Checks if is ordered mode.
	 *
	 * @return true, if is ordered mode
	 */
	public boolean isOrderedMode() {
		return orderedMode;
	}

	/**
	 * Sets the ordered mode.
	 *
	 * @param orderedMode the new ordered mode
	 */
	public void setOrderedMode(boolean orderedMode) {
		this.orderedMode = orderedMode;
	}

	/**
	 * Checks if is contiguous mode.
	 *
	 * @return true, if is contiguous mode
	 */
	public boolean isContiguousMode() {
		return contiguousMode;
	}

	/**
	 * Sets the contiguous mode.
	 *
	 * @param contiguousMode the new contiguous mode
	 */
	public void setContiguousMode(boolean contiguousMode) {
		this.contiguousMode = contiguousMode;
	}

	/**
	 * Checks if is partial mode.
	 *
	 * @return true, if is partial mode
	 */
	public boolean isPartialMode() {
		return partialMode;
	}

	/**
	 * Sets the partial mode.
	 *
	 * @param partialMode the new partial mode
	 */
	public void setPartialMode(boolean partialMode) {
		this.partialMode = partialMode;
	}
	
	/**
	 * Checks if is handle possible acronyms.
	 *
	 * @return true, if is handle possible acronyms
	 */
	public boolean isHandlePossibleAcronyms() {
		return handlePossibleAcronyms;
	}

	/**
	 * Sets the handle possible acronyms.
	 *
	 * @param handleProblemTerms the new handle possible acronyms
	 */
	public void setHandlePossibleAcronyms(boolean handleProblemTerms) {
		this.handlePossibleAcronyms = handleProblemTerms;
	}

	/**
	 * convert Template to XML DOM object representation.
	 *
	 * @param doc the doc
	 * @return the element
	 * @throws TerminologyException the terminology exception
	 */
	public Element toElement(Document doc)  throws TerminologyException{
		Element root = super.toElement(doc);
		Element options = doc.createElement("Options");
		Properties p = getSearchProperties();
		for(Object key: p.keySet()){
			Element opt = doc.createElement("Option");
			opt.setAttribute("name",""+key);
			opt.setAttribute("value",""+p.get(key));
			options.appendChild(opt);
		}
		root.appendChild(options);
		return root;
	}
	
	/**
	 * convert Template to XML DOM object representation.
	 *
	 * @param element the element
	 * @throws TerminologyException the terminology exception
	 */
	public void fromElement(Element element) throws TerminologyException{
		name = element.getAttribute("name");
		String str = element.getAttribute("version");
		if(str.length() > 0)
			storage.getInfoMap().put("version",str);
		str = element.getAttribute("uri");
		if(str.length() > 0)
			storage.getInfoMap().put("uri",str);
		str = element.getAttribute("location");
		if(str.length() > 0)
			storage.getInfoMap().put("location",str);
		
		// get child element
		for(Element e: XMLUtils.getChildElements(element)){
			if("Sources".equals(e.getTagName())){
				for(Element cc: XMLUtils.getElementsByTagName(e,"Source")){
					Source c = new Source("");
					c.fromElement(cc);
					storage.getSourceMap().put(c.getCode(),c);
				}
			}else if("Relations".equals(e.getTagName())){
				//NOOP
			}else if("Languages".equals(e.getTagName())){
				storage.getInfoMap().put("languages",e.getTextContent().trim());
			}else if("Roots".equals(e.getTagName())){
				for(String r: e.getTextContent().trim().split(",")){
					storage.getRootMap().put(r.trim(),"");
				}
			}else if("Description".equals(e.getTagName())){
				storage.getInfoMap().put("description",e.getTextContent().trim());
			}else if("Concepts".equals(e.getTagName())){
				for(Element cc: XMLUtils.getElementsByTagName(e,"Concept")){
					Concept c = new Concept("");
					c.fromElement(cc);
					addConcept(c);
				}
			}else if("Options".equals(e.getTagName())){
				Properties p = new Properties();
				for(Element op: XMLUtils.getElementsByTagName(e,"Option")){
					p.setProperty(op.getAttribute("name"),op.getAttribute("value"));
				}
				setSearchProperties(this,p);
			}
		}
	}

	/**
	 * process sentence and add Mentions to it.
	 *
	 * @param sentence the sentence
	 * @return the sentence
	 * @throws TerminologyException the terminology exception
	 */
	
	public Sentence process(Sentence sentence) throws TerminologyException {
		processTime = System.currentTimeMillis();
		
		// setup flags
		setupSearch(getDefaultSearchMethod());
		
		String text = sentence.getText();
		
		
		// split text into words (don't strip digits)
		NormalizedWordsContainer nwc = getNormalizedWordMap(this,text);
		List<String> words = nwc.normalizedWordsList;
		Map<String,String> normWords = nwc.normalizedWordsMap;
		Set<String> resultTerms = new LinkedHashSet<String>();
		List<Mention> result = new ArrayList<Mention>();
		
		// sort if possible
		
		Set<String> swords = null; //words
		if(ignoreUsedWords){
			swords = new TreeSet<String>(new Comparator<String>() {
				public int compare(String o1, String o2) {
					if(storage.getWordStatMap().containsKey(o1) && storage.getWordStatMap().containsKey(o2)){
						if( storage.getWordStatMap().get(o1).termCount == storage.getWordStatMap().get(o2).termCount){
							return o1.compareTo(o2);
						}
						return storage.getWordStatMap().get(o1).termCount-storage.getWordStatMap().get(o2).termCount;
					}
					if(storage.getWordStatMap().containsKey(o1))
						return -1;
					return 1;
				}
			});
			swords.addAll(words);
		}else{
			swords = new LinkedHashSet<String>(words);
		}
		

		// search regexp
		for(Concept c: searchRegExp(text)){
			if(!isFilteredOut(c)){
				c.setScore(1.0);
				result.addAll(Mention.getMentions(c));
			}
		}
		
		
		// for each word
		Set<String> usedWords = new HashSet<String>();
		Set<String> hashWords = new HashSet<String>(words); // for faster term matching
		int count = 0;
		for(String word : swords){
			count ++;
			
			// filter out junk
			if(ignoreSmallWords && word.length() <= 1)
				continue;
			
			// filter out common words
			if(ignoreCommonWords && TextTools.isCommonWord(word))
				continue;
				
			// if word is already in list of used words
			// save time and go on this time, but re-added for
			// later use in case the word is repeated later on
			if(ignoreUsedWords && usedWords.contains(word)){
				continue;
			}
			
			List<String> textWords = getTextWords(this,words,count);
			// if textWords is not the same size, regenerate the hash set
			if(words.size() != textWords.size())
				hashWords = new HashSet<String>(textWords);
			
			// select matched terms for a given word
			for(String term: getBestTerms(textWords,hashWords,usedWords,word)){
				resultTerms.add(term);
				if(ignoreUsedWords)
					usedWords.addAll(getUsedWords(this,textWords,term));
			}
			
		}
		
		
		// now lets remove subsumed terms
		if(subsumptionMode){
			List<String> torem = new ArrayList<String>();
			for(String a: resultTerms){
				for(String b: resultTerms){
					if(a.length() > b.length()){
						List<String> aa = Arrays.asList(a.split(" "));
						List<String> bb = Arrays.asList(b.split(" "));
						if(aa.size() > bb.size() && aa.containsAll(bb)){
							torem.add(b);
						}
					}
				}
			}
			resultTerms.removeAll(torem);
		}
		
		
		// create result list
		//time = System.currentTimeMillis();
		Set<String> seenOriginalTerms = new HashSet<String>();
		for(String term: resultTerms){
			Set<String> codes = storage.getTermMap().get(term);
			if(codes == null){
				continue;
			}
			// Derive original looking term
			String oterm = getOriginalTerm(text, term, normWords);
			
			// if have multiple normalized terms, resolve to the same original term
			// then skip subsequent onces
			if(seenOriginalTerms.contains(oterm))
				continue;
			seenOriginalTerms.add(oterm);
			
			// create 
			List<Concept> termConcepts = new ArrayList<Concept>();
			for(String code: codes){
				Concept c = convertConcept(code);
				if(c != null){
					c.setInitialized(true);
				}else{
					c = new Concept(code,term);
				}
				// clone
				c = c.clone();
				c.setTerminology(this);
				c.addMatchedTerm(oterm);
				c.setSearchString(text);
				
				if(ignoreAcronyms && isAcronym(c))
					continue;
			
				// score concepts, based on several parameters
				scoreConcept(c,term,resultTerms);
				
				// filter out really bad ones
				//if(!scoreConcepts || c.getScore() >= 0.5)
				termConcepts.add(c);
			}
			// add to results
			for(Concept c: getBestCandidates(termConcepts)){
				if(!isFilteredOut(c)){
					// if we have multiple annotations, deal with it better
					result.addAll(Mention.getMentions(c,getAnnotations(c,nwc.originalWordsList)));
				}
			}
		}
		// add mentions to Sentence
		sentence.setMentions(result);
		processTime = System.currentTimeMillis() - processTime;
		sentence.getProcessTime().put(getClass().getSimpleName(),processTime);
		return sentence;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.AbstractTerminology#getProcessTime()
	 */
	public long getProcessTime() {
		return processTime;
	}

	

	/**
	 * Score concept.
	 *
	 * @param c the c
	 * @param normalizedTerm the normalized term
	 * @param resultTerms the result terms
	 */
	
	private void scoreConcept(Concept c, String normalizedTerm, Set<String> resultTerms){
		if(!scoreConcepts)
			return;
		
		// get original text
		String originalTerm = c.getMatchedTerm();
		String synonymTerm = null;
		
		boolean singleWord = TextTools.charCount(originalTerm,' ') == 0;
		boolean exactMatch = false, caseMatch = false, stemmedMatch = false;
		
		
		// assign default weight
		double weight = 1.0;
		
		// if this term subsumes any other in the list, give it 5 points
		if(!singleWord){
			List<String> wt = Arrays.asList(normalizedTerm.split(" "));
			for(String t: resultTerms){
				if(!t.equals(normalizedTerm) && wt.containsAll(Arrays.asList(t.split(" ")))){
					weight += 5.0;
				}
			}
		// if single word, then try to identify matched synonym
		}else{
			Set<String> synonyms = getSingleWordSynonyms(c.getSynonyms());
			// check if there is a case sensitive match
			for(String s: synonyms){
				if(s.equals(originalTerm)){
					caseMatch = exactMatch = stemmedMatch = true;
					synonymTerm = s;
					break;
				}
			}
			// check if there is a case insensitive match
			if( synonymTerm == null){
				for(String s: synonyms){
					if(s.equalsIgnoreCase(originalTerm)){
						exactMatch = stemmedMatch = true;
						synonymTerm = s;
						break;
					}
				}
			}
			// check if there is a stemmed match only
			if( synonymTerm == null){
				for(String s: synonyms){
					if(normalizedTerm.equalsIgnoreCase(TextTools.stem(s))){
						stemmedMatch = true;
						synonymTerm = s;
						break;
					}
				}
			}
		}
		
		
		
		// if concept contains a synonym that matches some nasty pattern, remove 10 points
		//NOTE: we really want to select an original matched synonym
		/*boolean exactMatch = false, caseMatch = false, synonymLikelyAbbrev = false;
		for(String s: c.getSynonyms()){
			String ss = s;
			s = s.toLowerCase();
			String o = originalTerm.toLowerCase();
			// check if this concept is listed after proposition Ex: melanoma of skin, with skin being original term
			if(!s.startsWith("structure of ") && s.matches("(?i).*\\s+(of|at|in|from)\\s+"+Pattern.quote(o)+".*")){
				weight -= 10;
				break;
			}else 
			// check if it is part of some device ex 123,SKIN,234
			if(s.contains(","+o+",")){
				weight -= 10;
				break;
			}
			if(s.equals(o)){
				exactMatch = true;
				if(ss.equals(originalTerm))
					caseMatch = true;
				else	
					synonymLikelyAbbrev = TextTools.isLikelyAbbreviation(ss);
			}
		}*/
		
		// handle possible acronyms here
		// if matched term is a single word && likely abbreviation && rest of text is not uppercase
		if(singleWord && !caseMatch && ((synonymTerm == null || TextTools.isLikelyAbbreviation(synonymTerm)) ^ TextTools.isLikelyAbbreviation(originalTerm))){
			 StringStats st = TextTools.getStringStats(c.getSearchString());
			 // if input IS NOT mostly uppercase text
			 if(!(st.upperCase > st.lowerCase && st.whiteSpace > 0 && st.length > 5)){
				 weight -= 10;
			 }
		}
	
		// check if we have a normalized match only for a single word that is not plural
		// it is OK to have plural match though
		if(singleWord && !exactMatch && stemmedMatch && !TextTools.isPlural(originalTerm)){
			weight -= 10;
		}
		
		// add small points for more sources (cannot be more then 0.5)
		weight += 0.05*(c.getSources().length>10?10:c.getSources().length);
		
		
		// add some points if exact match to preferred name
		if(c.getName().equalsIgnoreCase(originalTerm))
			weight += 2.0;
		
		
		// now if we have filtered sources add points for source priority
		if(filteredSources != null){
			for(Source s: c.getSources()){
				int n = indexOf(s,filteredSources);
				if(n > 0)
					weight += 1.0/n;
			}
		}
		// now if we have filtered semtypes add points for source priority
		if(filteredSemanticTypes != null){
			for(SemanticType s: c.getSemanticTypes()){
				int n = indexOf(s,filteredSemanticTypes);
				if(n > 0)
					weight += 2.0/n;
			}
		}
		// set score
		c.setScore(weight);
	}
	
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String [] args) throws Exception{
		
		/*;
		System.out.println("compacting .. ");
		ConceptImporter.getInstance().compactTerminology(t);
		System.out.println("done");*/
		/*for(Source s: t.getSources()){
			System.out.println(s.getCode()+" v. "+s.getVersion());
		}
		
		//t.setSelectBestCandidate(false);
		//t.setScoreConcepts(false);
		for(String text : Arrays.asList("The nasal septum deviates to the left with a rather large spur.")){
			//,"It was found at that time that the patient had a third aneurysm and that the MCA aneurysm which was clipped was at the origin of the anterior temporal artery and not the one at the MCA bifurcation.")){
			//"age","sexes","sex","There is a fish under the sea.", "I had a genetic test done using a FISH method.", "WHERE ARE ALL OF THE FISH?", "He has DCIS as a diagnosis", "What about dcis", "skin, hello","SKIN, BACKWORDS")){
			System.out.println("\n\n"+text);
			for(Concept c: t.search(text)){
				System.out.println("matched: "+c.getMatchedTerm()+", score: "+c.getScore());
				c.printInfo(System.out);
			}
		}*/
		
	}

}
