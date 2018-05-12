package edu.pitt.dbmi.nlp.noble.terminology;

import edu.pitt.dbmi.nlp.noble.ontology.*;
import edu.pitt.dbmi.nlp.noble.tools.TextTools;
import edu.pitt.dbmi.nlp.noble.util.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;

/**
 * This class discribes a concept.
 *
 * @author Eugene Tseytlin (University of Pittsburgh)
 */
public class Concept implements  Serializable, Comparable<Concept> {
	private static final long serialVersionUID = 1234567890L;
	private boolean initialized;
	private String code, name = "unknown", definition = "", text, searchString;
	private Definition [] definitions = new Definition[0];
	private Source [] sources = new Source[0];
	private SemanticType [] semanticTypes = new SemanticType[0];
	private String [] synonyms = new String [0];
	private Term [] terms = new Term [0];
	private Term preferredTerm;
	private Relation [] relations = null;
	private Map<Source,String> codes;
	private Map<String,Set<String>> relationMap;
	private transient Map<Relation,Concept []> relatedConcepts;
	private Properties properties;
	private int offset;
	private int [] refs;
	private transient Terminology terminology;
	private transient IClass cls;
	private Annotation [] annotations;
	private String [] matchedTerm;
	private transient Content content;
	private double score;

	
	/**
	 * simple object that represents concept content.
	 *
	 * @author tseytlin
	 */
	public static class Content implements Serializable {
		private static final long serialVersionUID = 1234567890L;
		public transient Concept concept;
		public String code, name;
		public List<Definition> definitions;
		public List<Source> sources;
		public List<SemanticType> semanticTypes;
		public List<String> synonyms;
		public List<Term> terms;
		public Map<String,String> codeMap;
		public Map<String,Set<String>> relationMap;
		public Properties properties;
	}
	
	/**
	 * get concept from storage object.
	 *
	 * @param c the c
	 */
	public Concept(Content c){
		code = c.code;
		name = c.name;
		if(c.definitions != null){
			definitions = new Definition [c.definitions.size()];
			for(int i=0;i<definitions.length;i++){
				definitions[i] = c.definitions.get(i);
			}
		}
		if(c.sources != null){
			sources = new Source [c.sources.size()];
			for(int i=0;i<sources.length;i++)
				sources[i] = c.sources.get(i);
		}
		if(c.synonyms != null){
			synonyms = c.synonyms.toArray(new String [0]);
		}
		if(c.semanticTypes != null){
			semanticTypes = new SemanticType [c.semanticTypes.size()];
			for(int i=0;i<semanticTypes.length;i++){
			semanticTypes[i] = c.semanticTypes.get(i);
			}
		}
		if(c.relationMap != null){
			relationMap = c.relationMap;
		}
		if(c.codeMap != null){
			for(String key: c.codeMap.keySet()){
				addCode(c.codeMap.get(key),Source.getSource(key));
			}
		}
		if(c.terms != null){
			terms = new Term [c.terms.size()];
			for(int i=0;i<c.terms.size();i++){
				terms[i] = c.terms.get(i);
			}
		}
		if(c.properties != null)
			properties = c.properties;
	}
	
	/**
	 * get content object.
	 *
	 * @return the content
	 */
	public Content getContent(){
		if(content == null){
			Content c = new Content();
			c.name = name;
			c.code = code;
			c.synonyms = new ArrayList<String>();
			Collections.addAll(c.synonyms,synonyms);
			c.relationMap = relationMap;
			if(definitions != null){
				c.definitions = new ArrayList<Definition>();
				Collections.addAll(c.definitions,definitions);
			}
			if(sources != null){
				c.sources = new ArrayList<Source>();
				Collections.addAll(c.sources,sources);
			}
			if(semanticTypes != null){
				c.semanticTypes = new ArrayList<SemanticType> ();
				Collections.addAll(c.semanticTypes,semanticTypes);
			}
			if(codes != null){
				c.codeMap = new HashMap<String, String>();
				for(Source key: codes.keySet()){
					c.codeMap.put(key.getCode(),codes.get(key));
				}
			}
			if(terms != null){
				// do best possible job to map terms
				c.terms = new ArrayList<Term>();
				Collections.addAll(c.terms,terms);
			}
			if(properties != null && !properties.isEmpty())
				c.properties = properties;
			
			c.concept = this;
			content = c;
		}
		return content;
	}
	
	
	/**
	 * Constract a concept. CUI being the sole required argument
	 * @param code - the CUI representing this concept
	 */
	public Concept(String code){
		this.code = code;
		this.name = code;
		addSynonym(name);
	}
	
	/**
	 * Constract a concept with CUI and preferred name .
	 *
	 * @param code the code
	 * @param name the name
	 */
	public Concept(String code, String name){
		this.code = code;
		this.name = name;
		addSynonym(name);
	}

	/**
	 * Constract concept from another concept (copy).
	 *
	 * @param c the c
	 */
	public Concept(Concept c){
		c.copyTo(this);
	}
	
	
	
	
	/**
	 * create new concept from a class.
	 *
	 * @param cls the cls
	 */
	public Concept(IResource cls){
		if(cls instanceof IClass)
			this.cls = (IClass) cls;
		
		// figure out name
		String name = cls.getName();
		name =  OntologyUtils.toPrettyName(name);
		
		// assign code and name
		this.code = cls.getName(); //""+cls.getURI();
		this.name = name;
		
		IOntology ontology = cls.getOntology();
		setDefinitions(getDefinitions(cls));
		setSources(new Source[] {new Source(ontology.getName(),ontology.getDescription(),""+ontology.getURI())});
		addCode(""+cls.getURI(),Source.URI);
		
		String [] labels = cls.getLabels();
		if(labels.length > 0){
			setName(labels[0]);
			setSynonyms(labels);
		}else{
			setSynonyms(Collections.singleton(name).toArray(new String [0]));
		}
		if(ontology instanceof Terminology)
			setTerminology((Terminology)ontology);
		
		// try to guess some of the annotations
		for(IProperty p : cls.getProperties()){
			String pname = p.getName();
			String [] pl = p.getLabels();
			if(pl.length > 0)
				pname = pl[0];
			
			if(pname.matches("(?i)Sem(antic)?_?Type")){
				for(Object o: cls.getPropertyValues(p)){
					if(o.toString().trim().length() > 0)
						addSemanticType(SemanticType.getSemanticType(o.toString()));
				}
			}else if(pname.matches("(?i).*(regex).*")){
				for(Object o: cls.getPropertyValues(p)){
					if(o.toString().trim().length() > 0){
						String s = o.toString();
						if(!isRegExp(s))
							s = "/"+s+"/";
						addSynonym(s);
					}
				}
			}else if(pname.matches("(?i)pref.*(term|label).*")){
				setName(""+cls.getPropertyValue(p));
			}else if(pname.matches("(?i).*(abbr|synonym|term|variant|label|name|regex|misspell|subjectiveExpression).*") && !pname.toLowerCase().startsWith("legacy")){
				//((preferred|legacy)_)?
				for(Object o: cls.getPropertyValues(p)){
					if(o.toString().trim().length() > 0)
						addSynonym(o.toString());
				}
			}else if(pname.matches("(?i).*(definition|description)")){
				for(Object o: cls.getPropertyValues(p)){
					if(o.toString().trim().length() > 0)
						addDefinition(Definition.getDefinition(o.toString()));
				}
			}else if(pname.matches("(?i).*(cui|code|id)")){
				for(Object o: cls.getPropertyValues(p)){
					if(o.toString().trim().length() > 0){
						// is there a source embeded in the code???
						Source src = Source.getSource(pname);
						String code = o.toString();
						Matcher mt = Source.CODE_FROM_SOURCE_PATTERN.matcher(code);
						if(mt.matches()){
							code = mt.group(1).trim();
							src = Source.getSource(mt.group(2).trim());
						}
						
						if(getCode(src) != null){
							addCode(code,Source.getSource(o.toString()));
						}else{
							addCode(code,src);
						}
					}
				}
			}
		}
		setInitialized(true);
	}
	
	/**
	 * get concept definitions.
	 *
	 * @param cls the cls
	 * @return the definitions
	 */
	private Definition [] getDefinitions(IResource cls){
		String [] com = cls.getComments();
		Definition [] d = new Definition[com.length];
		for(int i=0;i<d.length;i++)
			d[i] = new Definition(com[i]);
		return d;
	}
	
	
	/**
	 * get concept class.
	 *
	 * @return the concept class
	 */
	public IClass getConceptClass(){
		return cls;
	}
	
	/**
	 * Concept object is only required to contain a CUI
	 * the rest of the information can be requested from the
	 * server on demand. This method must be invoked to request that
	 * information
	 *
	 * @throws TerminologyException the terminology exception
	 */
	public void initialize() throws TerminologyException{
		if(terminology != null){
			Concept c = terminology.lookupConcept(code);
			if(c != null)
				c.copyTo(this);
			initialized = true;
		}
	}
	
	
	/**
	 * Copy content of this concept to target concept.
	 *
	 * @param c the c
	 */
	private void copyTo(Concept c){
		c.setCode(getCode());
		c.setTerminology(getTerminology());
		c.setName(getName());
		c.setText(getText());
		c.setOffset(getOffset());
		c.setDefinitions(getDefinitions());
		c.setSources(getSources());
		c.setProperties(getProperties());
		c.setSemanticTypes(getSemanticTypes());
		c.setRelations(getRelations());
		c.setSynonyms(getSynonyms());
		c.setTerms(terms);
		c.setAnnotations(getAnnotations());
		c.setMatchedTerm(getMatchedTerm());
		c.setSearchString(getSearchString());
		c.getRelationMap().putAll(getRelationMap());
		Map map = getCodes();
		if(map != null)
			for(Object n: map.keySet())
				c.addCode((String)map.get(n),(Source)n);
	}
	
	
	/**
	 * get a copy of this dataset.
	 *
	 * @return the concept
	 */
	public Concept clone(){
		Concept c = new Concept(getContent());
		c.setMatchedTerm(getMatchedTerm());
		c.setSearchString(getSearchString());
		c.setScore(getScore());
		return c;
	}
	
	
	/**
	 * Pick some definition from the set of definitions (if available).
	 *
	 * @return the definition
	 */
	public String getDefinition(){
		if(definition == null || definition.length() == 0){
			if(definitions != null){
				// search for preferred definition
				// if none, found last one is used
				for(int i=0;i<definitions.length;i++){
					definition = definitions[i].getDefinition();
					if(definitions[i].isPreferred())
						break;
				}
			}
		}
		return definition;
	}
	
	/**
	 * score is a level of confidence from 0 to 1.0 that a terminology MAY
	 * assign to a discovered concept, not all terminologies may assigne scores.
	 * @return value from 0 to 1.0
	 */
	public double getScore() {
		return score;
	}

	/**
	 * score is a level of confidence from 0 to 1.0 that a terminology MAY
	 * assign to a discovered concept, not all terminologies may assigne scores.
	 *
	 * @param score the new score
	 */
	public void setScore(double score) {
		this.score = score;
	}

	
	/**
	 * Name should be returned as a string representation.
	 *
	 * @return the string
	 */
	public String toString(){
		//return name;
		return code;
	}
	
	/**
	 * Get Concept Unique Identifier.
	 *
	 * @return the code
	 */
	public String getCode() {
		return code;
	}
	
	/**
	 * Get Concept Unique Identifier.
	 *
	 * @param code the new code
	 */
	public void setCode(String code) {
		this.code = code;
	}
	
	/**
	 * Gets the definitions.
	 *
	 * @return the definitions
	 */
	public Definition [] getDefinitions() {
		return (definitions != null)?definitions: new Definition [0];
	}

	/**
	 * Sets the definitions.
	 *
	 * @param definitions the definitions to set
	 */
	public void setDefinitions(Definition [] definitions) {
		this.definitions = definitions;
		content = null;
	}
	
	/**
	 * add synonym.
	 *
	 * @param def the def
	 */
	public void addDefinition(Definition def){
		setDefinitions(TextTools.addAll(getDefinitions(),def));
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the properties.
	 *
	 * @return the properties
	 */
	public Properties getProperties() {
		if(properties == null)
			properties = new Properties();
		return properties;
	}

	/**
	 * Adds the property.
	 *
	 * @param key the key
	 * @param val the val
	 */
	public void addProperty(String key, String val){
		getProperties().setProperty(key, val);
	}
	
	/**
	 * Gets the property.
	 *
	 * @param key the key
	 * @return the property
	 */
	public String getProperty(String key){
		return getProperties().getProperty(key);
	}
	
	/**
	 * Sets the properties.
	 *
	 * @param p the new properties
	 */
	public void setProperties(Properties p) {
		Properties prop = getProperties();
		prop.putAll(p);
		/*
		for(Iterator i=p.keySet().iterator();i.hasNext();){
			String key = (String) i.next();
			prop.setProperty(key,p.getProperty(key));
		}
		*/
	}


	/**
	 * Gets the sources.
	 *
	 * @return the sources
	 */
	public Source [] getSources() {
		return (sources != null)?sources:new Source [0];
	}

	/**
	 * Sets the sources.
	 *
	 * @param sources the sources to set
	 */
	public void setSources(Source [] sources) {
		this.sources = sources;
	}

	/**
	 * add synonym.
	 *
	 * @param src the src
	 */
	public void addSource(Source src){
		// check if it is there already
		for(Source s: getSources())
			if(s.equals(src))
				return;
		setSources(TextTools.addAll(getSources(),src));
	}
	
	/**
	 * Gets the synonyms.
	 *
	 * @return the synonyms
	 */
	public String [] getSynonyms() {
		return (synonyms != null)?synonyms:new String [0];
	}

	/**
	 * Sets the synonyms.
	 *
	 * @param synonyms the synonyms to set
	 */
	public void setSynonyms(String [] synonyms) {
		this.synonyms = synonyms;
		content = null;
	}

	/**
	 * add synonym.
	 *
	 * @param synonym the synonym
	 */
	public void addSynonym(String synonym){
		// check if it is there already
		for(String s: getSynonyms())
			if(s.equals(synonym))
				return;
		setSynonyms(TextTools.addAll(getSynonyms(),synonym));
	}
	
	/**
	 * Checks if is initialized.
	 *
	 * @return the initialized
	 */
	public boolean isInitialized() {
		return initialized;
	}
	
	
	/**
	 * If concept is extracted from some text, then 
	 * start position is the offset of the exact string 
	 * that the concept covers in the phrase/sentance/doc.
	 *
	 * @return the offset
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Sets the offset.
	 *
	 * @param offset the offset to set
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}

	/**
	 * If concept is extracted from some text, then 
	 * text is the exact string that the concept covers.
	 * @return the text
	 */
	public String getText() {
		if(text == null){
			if(searchString != null){
				// try to find what matches what
				matchText(searchString);
			}else
				text = name;
		}
		return text;
	}

	
	/**
	 * remove weird chars from string.
	 *
	 * @param str the str
	 * @return the string
	 */
	private String filter(String str){
		// lowercase
		String str2 = str.toLowerCase();
		
		// get rid of non-word chars
		str2 = str2.replaceAll("\\W"," ");
		
		// strip posesive
		str2 = str2.replaceAll("'s\\b","  ");
		
		// get rid of stop words
		// replace with something not intellegible
		// that won't match to anything
		// if word is all uppoer case, it could be useful though
		if(TextTools.isStopWord(str2) && !str.matches("[A-Z]+"))
			str2 = "@#$%&";
		
		return str2.trim();
	}
	
	/**
	 * get number of chars.
	 *
	 * @param str the str
	 * @param test the test
	 * @return the int
	 */
	private int charCount(String str, char test){
		int n = 0;
		char [] a = str.toCharArray();
		for(int i=0;i<a.length;i++)
			if(a[i] == test)
				n++;
		return n;
	}
	
	
	/**
	 * match text, setup text and offset.
	 *
	 * @param query the query
	 */
	private void matchText(String query){
		//long time = System.currentTimeMillis();
		String [] words = TextTools.getWords(query).toArray(new String [0]);
		String [] fwords = new String [words.length];
		String [] swords = new String [words.length];
		String [] pwords = new String [words.length];
		// convert words to singular strip possessive etc
		for(int i=0;i<fwords.length;i++){
			fwords[i] = filter(words[i]);
			swords[i] = TextTools.stem(fwords[i]);
			pwords[i] = TextTools.convertToSingularForm(fwords[i]);
		}
				
		//	(?i) case insentetive
		int hits = 0;
		refs = new int [words.length];
		String [] terms = synonyms;
		
		// if we know which synonym matched, then use it,
		//String name = getName();
		if(matchedTerm != null){
			terms = getMatchedTerms();		
			//name = matchedTerm;
		}
		// go over all available tersm
		for(int i=0;i<terms.length;i++){
			if(isRegExp(terms[i]))
				continue;
			
			String synonym = filter(terms[i]);
			for(int j=0;j<fwords.length;j++){
				if(fwords[j].length() == 0)
					continue;
				// check original word, then stem, then singular
				if( synonym.matches(".*\\b"+fwords[j]+"\\b.*") ||
				    (swords[j].length() > 3 && synonym.matches(".*\\b"+swords[j]+".*")) ||
				    synonym.matches(".*\\b"+pwords[j]+"\\b.*")){
					if(refs[j] == 0)
						hits ++;
					refs[j]++;
				}
			}
		}
		
		
		// lets do gap analysis, if we have multi-word concept
		// then most words should be withing some window of eachother
		// if there is a lone word somewhere far it is probably an outlier
		//System.out.println("map of -"+query+"-"+Arrays.toString(getWordMap()));
		/*if(hits > 1 && hits > charCount(name,' ')+1){
			final int WINDOW_SIZE = 3;
			int windowCount = -1, wordPosition = -1;
			for(int i=0;i<refs.length;i++){
				// if window count is greater then threshold
				// then last word was an outlier and should be
				// "deleted" from word map
				if(windowCount >= WINDOW_SIZE && wordPosition > -1 ){
					refs[wordPosition] = 0;
				}
				
				
				// once we hit a word, start counting the window
				if(refs[i] > 0){
					// remember position, if we exceeded max window
					// or has not started counting, else ignore pos
					if(windowCount < 0 || windowCount > WINDOW_SIZE){
						wordPosition = i;
					}else{
						wordPosition = -1;
					}
					windowCount =  0;
				}else if(windowCount >= 0){
					// if we initialized window, then increment it
					windowCount ++;
				}
			}
			// clear potential outlier to the right
			if(wordPosition > -1){
				refs[wordPosition] = 0;
			}
			//System.out.println(query+Arrays.toString(getWordMap()));
		}*/
		
		
		// analyze found words
		// get bounds
		int i=0,j=refs.length-1;
		while(i<=j ){
			if(refs[i]==0)
				i++;
			if(refs[j]==0)
				j--;
			if(i >= refs.length || (refs[i]  > 0 && refs[j] > 0))
				break;
		}
		
		// rebuild the string
		if(i<= j){
			// calculate offset to start search for words
			// to avoid finding short words in other words before them
			int sti,eni,k;
			for(sti=0,k=0;k<i;sti+=words[k].length(),k++);
			for(eni=0,k=0;k<j;eni+=words[k].length(),k++);
			
			// now calculate the offset
			int st = query.indexOf(words[i],sti);
			int en = query.indexOf(words[j],eni)+words[j].length();
			//System.out.println((System.currentTimeMillis()-time));
			
			// set member vars
			try{
				text   = query.substring(st,en);
				offset = st;
			}catch(Exception ex){
				text = query;
			}
			
			//System.out.println(text+" "+sti+" "+st+" "+en+" "+Arrays.toString(getWordMap()));
		}
		//System.out.println("time="+(System.currentTimeMillis()-time));
	}
	
	/**
	 * get an array of hit words in matched text (from getText()).
	 *
	 * @return the word map
	 */
	public int [] getWordMap(){
		// try to get word map in the unique case, where we have text, but not map
		// most likely cause is that this concept came from regexp
		if(refs == null && searchString != null && text != null){
			List<String> words = TextTools.getWords(searchString);
			refs = new int [words.size()];
			
			boolean span = false;
			for(int i=0;i< words.size();i++){
				if(text.startsWith(words.get(i)))
					span = true;
				if(span)
					refs[i] = 1;
				if(text.endsWith(words.get(i)))
					span = false;
			}
			
			
		}
		return (refs != null)?refs:new int [0];
	}
	
	/**
	 * Reset word map.
	 */
	public void resetWordMap(){
		refs = null;
	}
	
	/**
	 * Sets the text.
	 *
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Gets the terminology.
	 *
	 * @return the terminology
	 */
	public Terminology getTerminology() {
		return terminology;
	}

	/**
	 * Sets the terminology.
	 *
	 * @param terminology the terminology to set
	 */
	public void setTerminology(Terminology terminology) {
		this.terminology = terminology;
	}


	/**
	 * Gets the semantic types.
	 *
	 * @return the symanticTypes
	 */
	public SemanticType [] getSemanticTypes() {
		return semanticTypes != null?semanticTypes: new SemanticType [0];
	}

	/**
	 * Sets the semantic types.
	 *
	 * @param semanticTypes the new semantic types
	 */
	public void setSemanticTypes(SemanticType[] semanticTypes) {
		this.semanticTypes = semanticTypes;
		content = null;
	}
	
	/**
	 * Adds the semantic type.
	 *
	 * @param semanticType the semantic type
	 */
	public void addSemanticType(SemanticType semanticType) {
		// check if it is there already
		for(SemanticType s: getSemanticTypes())
			if(s.equals(semanticType))
				return;
		setSemanticTypes(TextTools.addAll(getSemanticTypes(),semanticType));
	}
	
	
	/**
	 * add related concept.
	 *
	 * @param r - relationship in question
	 * @param code - code of the related concept
	 */
	public void addRelatedConcept(Relation r, String code){
		if(relationMap == null)
			relationMap = new HashMap<String, Set<String>>();
		Set<String> list = relationMap.get(r.getName());
		if(list == null){
			list = new LinkedHashSet<String>();
			relationMap.put(r.getName(),list);
		}
		list.add(code);
		content = null;
	}
	
	/**
	 * print info.
	 *
	 * @param out the out
	 */
	public void printInfo(PrintStream out){
		out.println(getName()+" ("+getCode()+")\t"+Arrays.toString(getSemanticTypes()));
		out.println("\tdefinition:\t"+getDefinition());
		out.println("\tsynonyms:\t"+Arrays.toString(getSynonyms()));
		out.println("\tsources:\t"+Arrays.toString(getSources()));	
		//if(relations != null)
		//	out.println("\trelations:\t"+Arrays.toString(relations));	
	}

	/**
	 * Gets the parent concepts.
	 *
	 * @return the parentConcepts
	 * @throws TerminologyException the terminology exception
	 */
	public Concept[] getParentConcepts() throws TerminologyException {
		return getRelatedConcepts(Relation.BROADER);
	}

	/**
	 * Gets the children concepts.
	 *
	 * @return the parentConcepts
	 * @throws TerminologyException the terminology exception
	 */
	public Concept[] getChildrenConcepts() throws TerminologyException {
		return getRelatedConcepts(Relation.NARROWER);
	}

	/**
	 * Gets the relations.
	 *
	 * @return the relations
	 */
	public Relation[] getRelations() {
		try{
			if(relations == null && terminology != null)
				relations = terminology.getRelations(this);
		}catch(TerminologyException ex){
			relations = new Relation [0];
		}
		return relations;
	}

	/**
	 * Sets the relations.
	 *
	 * @param relations the relations to set
	 */
	public void setRelations(Relation[] relations) {
		this.relations = relations;
	}

	/**
	 * Gets the related concepts.
	 *
	 * @param relation the relation
	 * @return the relatedConcepts
	 * @throws TerminologyException the terminology exception
	 */
	public Concept [] getRelatedConcepts(Relation relation) throws TerminologyException {
		return (getRelatedConcepts().containsKey(relation))?getRelatedConcepts().get(relation):new Concept [0];
	}

	/**
	 * Gets the related concepts.
	 *
	 * @return the relatedConcepts
	 * @throws TerminologyException the terminology exception
	 */
	public Map<Relation,Concept []>  getRelatedConcepts() throws TerminologyException {
		// if there was a privous request, get cache
		if(relatedConcepts == null){
			relatedConcepts = new HashMap<Relation,Concept []>();
		
			// see if this information is available locally
			if(terminology != null)
				relatedConcepts.putAll(terminology.getRelatedConcepts(this));
			
		}
		return relatedConcepts;
	}
	
	/**
	 * get relation map that is associated with this concept .
	 *
	 * @return the relation map
	 */
	
	public Map<String,Set<String>> getRelationMap(){
		if(relationMap == null)
			relationMap = new HashMap<String, Set<String>>();
		return relationMap;
	}
	
	/**
	 * Sets the initialized.
	 *
	 * @param initialized the initialized to set
	 */
	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}
	
	
	/**
	 * get preferred term.
	 *
	 * @return the preferred term
	 */
	public Term getPreferredTerm(){
		// get preferred term from terms
		if(preferredTerm == null){
			if(terms != null){
				for(int i=0;i<terms.length;i++){
					if(terms[i].isPreferred()){
						preferredTerm = terms[i];
						break;
					}
				}
			}
		}
		// if preferred term is still null, make one up
		if(preferredTerm == null)
			preferredTerm = new Term(getName());
		return preferredTerm;
	}

	/**
	 * Gets the terms.
	 *
	 * @return the terms
	 */
	public Term[] getTerms() {
		if(terms == null || terms.length == 0){
			Set<Term> termList = new LinkedHashSet<Term>();
			Term t = Term.getTerm(name);
			t.setPreferred(true);
			//t.setForm("PT");
			termList.add(t);
			for(String s: getSynonyms()){
				t = Term.getTerm(s);
				if(isRegExp(s)){
					t.setText(s.substring(1,s.length()-1));
					t.setForm("RegEx");
				}
				termList.add(t);
			}
			terms = termList.toArray(new Term [0]);
		}
		//return (terms != null)?terms:new Term [0];
		return terms;
	}
	
	/**
	 * check if string is a regular expression.
	 *
	 * @param s the s
	 * @return true, if is reg exp
	 */
	protected boolean isRegExp(String s){
		return s != null && s.startsWith("/") && s.endsWith("/");
	}

	/**
	 * Sets the terms.
	 *
	 * @param terms the terms to set
	 */
	public void setTerms(Term[] terms) {
		this.terms = terms;
	}
	
	/**
	 * add synonym.
	 *
	 * @param term the term
	 */
	public void addTerm(Term term){
		setTerms(TextTools.addAll((terms != null)?terms:new Term [0],term));
		String t = term.getText();
		addSynonym(term.isRegularExpression()?"/"+t+"/":t);
	}
	
	/**
	 * add a code for some source.
	 *
	 * @param code the code
	 * @param source the source
	 */
	public void addCode(String code, Source source){
		if(codes == null)
			codes = new HashMap<Source,String>();
		codes.put(source,code);
	}
	
	/**
	 * Return a code for a specific source
	 * if code is not found, return null.
	 *
	 * @param source the source
	 * @return the code
	 */
	public String getCode(Source source){
		if(codes != null)
			return (String) codes.get(source);
		return null;
	}
	
	/**
	 * get mapping of codes from different sources.
	 *
	 * @return the codes
	 */
	public Map getCodes(){
		return codes == null?Collections.emptyMap():codes;
	}

	/**
	 * if concept was searched, what was the original search query.
	 *
	 * @return the searchQuery
	 */
	public String getSearchString() {
		return searchString;
	}

	/**
	 * Sets the search string.
	 *
	 * @param searchQuery the searchQuery to set
	 */
	public void setSearchString(String searchQuery) {
		this.searchString = searchQuery;
	}
	
	/**
	 * is concept fully covered by search string.
	 *
	 * @return true, if is fully covered
	 */
	public boolean isFullyCovered(){
		if(searchString != null){
			String ss = searchString.toLowerCase();
			for(String s: getSynonyms()){
				if(ss.contains(s))
					return true;
			}
			return false;
		}
		return true;
	}
	
	/**
	 * this class is a comparator that sorts Concepts based on length of
	 * text found in search string.
	 *
	 * @author tseytlin
	 */
	public static class TextLengthComparator implements Comparator, Serializable{ 
		
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2){
			if(o1 instanceof Concept && o2 instanceof Concept){
				String t1 = ((Concept)o1).getText();
				String t2 = ((Concept)o2).getText();
				t1 = (t1 == null)?"":t1; 
				t2 = (t2 == null)?"":t2;
				return t2.length() - t1.length();
			}
			return 0;	
		}
	};
	
	/**
	 * this class is a comparator that sorts Concepts based on name.
	 *
	 * @author tseytlin
	 */
	public static class NameComparator implements Comparator, Serializable{ 
		
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2){
			if(o1 instanceof Concept && o2 instanceof Concept){
				String t1 = ((Concept)o1).getName();
				String t2 = ((Concept)o2).getName();
				t1 = (t1 == null)?"":t1; 
				t2 = (t2 == null)?"":t2;
				return t1.compareTo(t2);
			}
			return 0;	
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object e) {
		if(e != null && e instanceof Concept)
			return getCode().equals(((Concept)e).getCode());
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getCode().hashCode();
	};
	
	
	/**
	 * Gets the matched term.
	 *
	 * @return the matched term
	 */
	public String getMatchedTerm() {
		return (matchedTerm != null && matchedTerm.length > 0)?matchedTerm[0]:null;
	}
	
	/**
	 * Gets the matched terms.
	 *
	 * @return the matched terms
	 */
	public String [] getMatchedTerms() {
		return matchedTerm;
	}

	/**
	 * Sets the matched term.
	 *
	 * @param matchedTerm the new matched term
	 */
	public void setMatchedTerm(String matchedTerm) {
		if(matchedTerm != null)
			this.matchedTerm = new String [] {matchedTerm};
	}
	
	/**
	 * Adds the matched term.
	 *
	 * @param matchedTerm the matched term
	 */
	public void addMatchedTerm(String matchedTerm) {
		if(this.matchedTerm == null){
			setMatchedTerm(matchedTerm);
		}else{
			this.matchedTerm = TextTools.addAll(this.matchedTerm,matchedTerm);
		}
	}
	
	/**
	 * set annotations.
	 *
	 * @param a the new annotations
	 */
	public void setAnnotations(Annotation [] a){
		annotations = a;
	}
	
	/**
	 * add annotation.
	 *
	 * @param a the a
	 */
	public void addAnnotation(Annotation a){
		if(annotations == null){
			setAnnotations(new Annotation []{a});
		}else{
			// don't add annotation that is already there
			for(Annotation s: annotations){
				if(s.equals(a))
					return;
			}
			setAnnotations(TextTools.addAll(annotations,a));
		}
	}
	
	/**
	 * get annotations.
	 *
	 * @return the annotations
	 */
	public Annotation [] getAnnotations(){
		if(annotations == null){
			if(searchString != null){
				// do our regular matching
				matchText(searchString);
				
				// go over words
				List<String> words = TextTools.getWords(searchString);
				int [] wMap = getWordMap();
				if(words.size() == wMap.length){
					List<Annotation> as = new ArrayList<Annotation>();
					int end = 0;
					for(int i=0;i<words.size();i++){
						if(wMap[i] > 0){
							int st = searchString.indexOf(words.get(i),end);
							Annotation a = new Annotation();
							a.setConcept(this);
							a.setText(words.get(i));
							a.setOffset(st);
							a.setSearchString(searchString);
							as.add(a);
							end = a.getEndPosition()+1;
						}else
							end += words.get(i).length()+1;
					}
					annotations = as.toArray(new Annotation [0]);
				}else{
					Annotation a = new Annotation();
					a.setConcept(this);
					a.setText(getText());
					a.setOffset(getOffset());
					a.setSearchString(searchString);
					annotations = new Annotation [] {a};
				}
			}else{
				annotations = new Annotation [0];
			}
		}
		return annotations;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Concept o) {
		return getName().compareTo(o.getName());
	}
	
	/**
	 * convert to DOM element.
	 *
	 * @param doc the doc
	 * @return the element
	 * @throws TerminologyException the terminology exception
	 */
	public Element toElement(Document doc) throws TerminologyException {
		Element e = doc.createElement("Concept");
		e.setAttribute("name",getName());
		e.setAttribute("code",getCode());
		for(Definition d: getDefinitions()){
			e.appendChild(d.toElement(doc));
		}
		for(SemanticType st: getSemanticTypes()){
			e.appendChild(st.toElement(doc));
		}
		for(Source src: getSources()){
			Element sr = doc.createElement("Source");
			sr.setAttribute("name",src.getCode());
			e.appendChild(sr);
		}
		for(Term t: getTerms()){
			e.appendChild(t.toElement(doc));
		}
		if(codes != null && !codes.isEmpty()){
			Element ce = doc.createElement("Codes");
			for(Source src: codes.keySet()){
				String cd = codes.get(src);
				Element cc = doc.createElement("Code");
				cc.setAttribute("source",src.getCode());
				cc.setAttribute("code",cd);
				ce.appendChild(cc);
			}
			e.appendChild(ce);
		}
		if(relationMap != null && !relationMap.isEmpty()){
			Element ce = doc.createElement("Relations");
			for(String r: relationMap.keySet()){
				Set<String> codes = relationMap.get(r);
				Element cc = doc.createElement("Relation");
				cc.setAttribute("name",r);
				String t = ""+codes;
				cc.setTextContent(t.substring(1,t.length()-1));
				ce.appendChild(cc);
			}
			e.appendChild(ce);
		}
		
		
		return e;
	}
	
	/**
	 * convert from DOM element.
	 *
	 * @param e the e
	 * @throws TerminologyException the terminology exception
	 */
	public void fromElement(Element e) throws TerminologyException{
		if(e.getTagName().equals("Concept")){
			setName(e.getAttribute("name"));
			setCode(e.getAttribute("code"));
			for(Element c: XMLUtils.getChildElements(e)){
				if("Term".equals(c.getTagName())){
					Term d = new Term("");
					d.fromElement(c);
					addTerm(d);
				}else if("Definition".equals(c.getTagName())){
					Definition d = new Definition();
					d.fromElement(c);
					addDefinition(d);
				}else if("Source".equals(c.getTagName())){
					Source d = new Source();
					d.fromElement(c);
					addSource(d);
				}else if("SemanticType".equals(c.getTagName())){
					SemanticType d = SemanticType.getSemanticType("");
					d.fromElement(c);
					addSemanticType(d);
				}else if("Relations".equals(c.getTagName())){
					for(Element r: XMLUtils.getElementsByTagName(c,"Relation")){
						Relation rl = Relation.getRelation(r.getAttribute("name"));
						for(String cd: r.getTextContent().trim().split(","))
							addRelatedConcept(rl, cd.trim());
					}
				}else if("Codes".equals(c.getTagName())){
					for(Element r: XMLUtils.getElementsByTagName(c,"Code")){
						Source sr = Source.getSource(r.getAttribute("source"));
						String cd = r.getAttribute("code");
						addCode(cd, sr);
					}
				}
			}
		}
	}
}
