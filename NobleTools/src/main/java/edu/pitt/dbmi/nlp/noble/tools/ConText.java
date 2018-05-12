package edu.pitt.dbmi.nlp.noble.tools;

import edu.pitt.dbmi.nlp.noble.coder.model.*;
import edu.pitt.dbmi.nlp.noble.ontology.IClass;
import edu.pitt.dbmi.nlp.noble.ontology.IInstance;
import edu.pitt.dbmi.nlp.noble.ontology.IOntology;
import edu.pitt.dbmi.nlp.noble.ontology.owl.OOntology;
import edu.pitt.dbmi.nlp.noble.terminology.*;
import edu.pitt.dbmi.nlp.noble.terminology.impl.NobleCoderTerminology;

import java.io.IOException;
import java.util.*;

/**
 * The Class ConText.
 */
public class ConText implements Processor<Sentence> {
	public static final String DEFAULT_MODIFIER_ONTOLOGY = "http://blulab.chpc.utah.edu/ontologies/v2/Modifier.owl";
	public static final List<String> CONTEXT_ROOTS =  Arrays.asList("Closure","Pseudo","LinguisticModifier");
	public static final String HAS_TERMINATION = "hasTermination";
	public static final String HAS_PSEUDO = "hasPseudo";
	public static final String HAS_SENTENCE_ACTION = "hasActionEn";
	public static final String HAS_PARAGRAPH_ACTION = "hasParagraphAction";
	public static final String HAS_SECTION_ACTION = "hasSectionAction";
	public static final String PROP_WINDOW_SIZE = "windowSize";
	public static final String PROP_IS_DEFAULT_VALUE = "isDefaultValue";
	public static final String PROP_HAS_DEFAULT_VALUE = "hasDefaultValue";
	
	public static final String SEMTYPE_INSTANCE = "Instance";
	public static final String SEMTYPE_CLASS = "Class";
	public static final String LANGUAGE = "en";
	public static final String CONTEXT_OWL = "ConText.owl";
	public static final String SCHEMA_OWL = "Schema.owl";
	public static final String ACTION_TERMINATE = "terminate";
	public static final String ACTION_FORWARD = "forward";
	public static final String ACTION_BACKWARD = "backward";
	public static final String ACTION_BIDIRECTIONAL = "bidirectional";
	public static final String ACTION_DISCONTINUOUS = "discontinuous";
	public static final String ACTION_FIRST_MENTION = "first_mention";
	public static final String ACTION_NEAREST_MENTION = "nearest_mention";;
	
	public static final List<String> BEFORE_ACTIONS = Arrays.asList(ACTION_FORWARD,ACTION_BIDIRECTIONAL,ACTION_NEAREST_MENTION,ACTION_FIRST_MENTION);
	public static final List<String> AFTER_ACTIONS = Arrays.asList(ACTION_BACKWARD,ACTION_BIDIRECTIONAL,ACTION_NEAREST_MENTION);
	
	public static final String LINGUISTIC_MODIFIER = "LinguisticModifier";
	public static final String SEMANTIC_MODIFIER = "SemanticModifier";
	public static final String NUMERIC_MODIFIER = "NumericModifier";
	public static final String BODY_MODIFIER = "BodyModifier";
	public static final String MODIFIER = "Modifier";
	public static final String PSEUDO = "Pseudo";
	public static final int DEFAULT_WINDOW_SIZE = 8;
	public static final String QUALIFIER = "Qualifier";
	public static final String LEXICON = "Lexicon";
	public static final String QUANTITY = "Quantity";
	
	public static final String MODIFIER_TYPE_POLARITY = "Polarity";
	public static final String MODIFIER_TYPE_EXPERIENCER = "Experiencer";
	public static final String MODIFIER_TYPE_TEMPORALITY = "Temporality";
	public static final String MODIFIER_TYPE_CERTAINTY = "Certainty";
	public static final String MODIFIER_TYPE_ASPECT = "ContextualAspect";
	public static final String MODIFIER_TYPE_MODALITY = "ContextualModality";
	public static final String MODIFIER_TYPE_DEGREE = "Degree";
	public static final String MODIFIER_TYPE_PERMENENCE = "Permanence";
	
	
	public static final String MODIFIER_VALUE_POSITIVE = "Positive_Polarity";
	public static final String MODIFIER_VALUE_NEGATIVE = "Negative_Polarity";
	public static final String MODIFIER_VALUE_HEDGED = "Hedged_ContextualModality";
	public static final String MODIFIER_VALUE_FAMILY_MEMBER = "FamilyMember_Experiencer";
	public static final String MODIFIER_VALUE_HISTORICAL = "Before_DocTimeRel";
	public static final List<String> MODIFIER_TYPES_FILTER =  
			Arrays.asList(SEMTYPE_INSTANCE,MODIFIER,LINGUISTIC_MODIFIER,NUMERIC_MODIFIER,SEMANTIC_MODIFIER,QUALIFIER,BODY_MODIFIER,LEXICON);
	public static final List<String> IMPORTED_ONTOLOGIES = Arrays.asList("Schema","ConText","SemanticType","TermMapping");
	
	
	public static final List<String> MODIFIER_TYPES = Arrays.asList(
			MODIFIER_TYPE_CERTAINTY,
			MODIFIER_TYPE_ASPECT,
			MODIFIER_TYPE_MODALITY,
			MODIFIER_TYPE_DEGREE,
			MODIFIER_TYPE_EXPERIENCER,
			MODIFIER_TYPE_PERMENENCE,
			MODIFIER_TYPE_POLARITY,
			MODIFIER_TYPE_TEMPORALITY);




	
	
	private long time;
	private Terminology terminology;
	private Map<String,String> defaultValues;
	
	/**
	 * a possible way for an outside code to validate if modifier can be linked to a target
	 * @author tseytlin
	 */
	public static interface ModifierResolver {
		/**
		 * is a given modifier applicable for a given target
		 * @param modifier - modifier in question
		 * @param target - target mention
		 * @return true if it is applicable
		 */
		public boolean isModifierApplicable(Mention modifier, Mention target);
		
		/**
		 * add semanticaly relevant information for numeric modifiers s.a. numerator/denominator
		 * add new more specific numeric mentions if the numeric value satisfies equivalence relations
		 * @param sentence - sentence with modifier mentions
		 */
		public void processNumericModifiers(Sentence sentence);
	}
	private ModifierResolver modifierResolver;
	
	
	/**
	 * initialize ConText with default modifier ontology
	 * first check the cache, if not there load/save from the web.
	 */
	public ConText(){
		try{
			// check if pre-existing terminology exists
			if(NobleCoderTerminology.hasTerminology(getClass().getSimpleName())){
				terminology = new NobleCoderTerminology(getClass().getSimpleName());
			}else{
				load(OOntology.loadOntology(DEFAULT_MODIFIER_ONTOLOGY));
				if(terminology instanceof NobleCoderTerminology)
					((NobleCoderTerminology)terminology).dispose();
				terminology = new NobleCoderTerminology(getClass().getSimpleName());
			}
		}catch(Exception ex){
			throw new TerminologyError("Unable to load ConText ontology", ex);
		}
	}
	
	
	/**
	 * Instantiates a new con text.
	 *
	 * @param ont the ont
	 */
	public ConText(IOntology ont){
		try {
			load(ont);
		} catch (Exception e) {
			throw new TerminologyError("Unable to load ConText ontology",e);
		}
	}
	
	
	/**
	 * initialize context with existing and initialized modifier terminology
	 * @param terminology - the terminology
	 */
	public ConText(Terminology terminology){
		this.terminology = terminology;
	}
	
	
	/**
	 * load ConText ontology from a given ontology object.
	 *
	 * @param ontology the ontology
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void load(IOntology ontology) throws TerminologyException, IOException {
		// setup special interest of noble coder
		NobleCoderTerminology terminology = new NobleCoderTerminology();
		this.terminology = terminology;
		terminology.load(getClass().getSimpleName(),false);
		terminology.setDefaultSearchMethod(NobleCoderTerminology.CUSTOM_MATCH);
		terminology.setContiguousMode(true);
		terminology.setSubsumptionMode(false);
		terminology.setOverlapMode(true);
		terminology.setPartialMode(false);
		terminology.setOrderedMode(true);
		terminology.setMaximumWordGap(0);
		terminology.setScoreConcepts(false);
		terminology.setHandlePossibleAcronyms(false);
		terminology.setLanguageFilter(new String [] {LANGUAGE});
		terminology.setStemWords(false);
		terminology.setStripStopWords(false);
		terminology.setIgnoreSmallWords(false);
		terminology.setIgnoreDigits(false);
		terminology.setSemanticTypeFilter(SEMTYPE_INSTANCE);
		
		// set language filter to only return English values
		if(ontology instanceof OOntology)
			((OOntology)ontology).setLanguageFilter(Arrays.asList(LANGUAGE));
		
		
		// load classes 
		for(String root: CONTEXT_ROOTS ){
			IClass cls = ontology.getClass(root);
			if(cls != null){
				// add roots to terminology
				terminology.addRoot(addConcept(cls).getCode());
				for(IInstance inst : cls.getDirectInstances()){
					addConcept(inst);
				}
				
				// go over all subclasses
				for(IClass c: cls.getSubClasses()){
					addConcept(c);
					for(IInstance inst : c.getDirectInstances()){
						addConcept(inst);
					}
				}
			}
		}
		
		// save terminology
		terminology.save();
	}

	/**
	 * Adds the concept.
	 *
	 * @param inst the inst
	 * @return the concept
	 * @throws TerminologyException the terminology exception
	 */
	private Concept addConcept(IInstance inst) throws TerminologyException {
		Concept concept = ConTextHelper.createConcept(inst);
		terminology.addConcept(concept);
		return concept;
	}
	

	
	/**
	 * get semantic modifier validator associated with ConText
	 * @return modifier resolver
	 */
	public ModifierResolver getModifierResolver() {
		return modifierResolver;
	}

	/**
	 * set an outside validator that can check if a modifier can actually map to a target
	 * @param modifierResolver - semantic modifier validator
	 */
	public void setModifierResolver(ModifierResolver modifierResolver) {
		this.modifierResolver = modifierResolver;
	}


	/**
	 * Adds the concept.
	 *
	 * @param cls the cls
	 * @return the concept
	 * @throws TerminologyException the terminology exception
	 */
	private Concept addConcept(IClass cls) throws TerminologyException{
		Concept concept =  ConTextHelper.createConcept(cls);
		terminology.addConcept(concept);
		return concept;
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
	 * get default values map.
	 *
	 * @return the default values
	 * @throws TerminologyException the terminology exception
	 */
	public Map<String,String> getDefaultValues() throws TerminologyException{
		if(defaultValues == null){
			defaultValues = new LinkedHashMap<String,String>();
			for(String type: MODIFIER_TYPES){
				Concept context = terminology.lookupConcept(type);
				if(context != null && context.getProperties().containsKey(PROP_HAS_DEFAULT_VALUE)){
					defaultValues.put(type,context.getProperty(PROP_HAS_DEFAULT_VALUE));
				}
			}
		}
		return defaultValues;
	}
	
	/**
	 * overwrite default values map.
	 * if not set, the mapping will be re-generated
	 * @param values - default vaulues map
	 */
	public void setDefaultValues(Map<String,String> values){
		defaultValues = values;
	}
	
	
	
	
	/**
	 * now actually process sentence and see what we have.
	 *
	 * @param sentence the sentence
	 * @return the sentence
	 * @throws TerminologyException the terminology exception
	 */
	
	public Sentence process(Sentence sentence) throws TerminologyException {
		time = System.currentTimeMillis();
		
		
		// if the sentence is a section header and it has concepts that were found,
		// then entire section is a source of attributes for this mention
		Sentence newSentence = null;
		if(Sentence.TYPE_HEADER.equals(sentence.getSentenceType()) && !sentence.getMentions().isEmpty() && sentence.getSection() != null){
			Spannable span = sentence.getSection();
			for(Paragraph p : sentence.getSection().getParagraphs()){
				span = new Paragraph(sentence.getDocument(),sentence.getOffset(),p.getEndPosition()); 
				break;
			}
			newSentence = new Sentence(span.getText(),span.getStartPosition(),Sentence.TYPE_PROSE);
		}else{
			newSentence = new Sentence(sentence);
		}
		
		
		// get mentions for this sentence, make a copy of since we don't add mentions
		// to the original sentence
		Sentence text = terminology.process(newSentence);
		
		
		// assign qualifiers to modifiers: Ex: Units to Quality or Laterality to BodySite
		for(Mention m:  getRelevantModifiers(text)){
			// check if mention is qualifier, otherwise don't bother
			if(isTypeOf(m,QUALIFIER)){
				// add modifiers to modifiers if relevant
				for(Mention target: getTargetMentions(m,text,getTerminators(m,text))){
					target.addModifiers(getModifiers(m));
				}
			}
		}
		// process numeric modifiers (this will upgrade some of them based on equivalence classes)
		if(getModifierResolver() != null)
			getModifierResolver().processNumericModifiers(text);
		
		
		// get relevant modifiers from parsed text, takes care of pseudo stuff too
		List<Mention> relevantModifiers = getRelevantModifiers(text);
		
		//add defaults for stuff that was not picked up
		for(Mention m: sentence.getMentions()){
			for(String type: getDefaultValues().keySet()){
				m.addModifier(getModifier(type,getDefaultValues().get(type)));
			}
		}
		
		// go over all modifier mentions
		for(Mention m: relevantModifiers){
			// don't bother with modifiers of modifiers, they don't connect to targets anyhow
			if(!isTypeOf(m,QUALIFIER)){
				// add relevant modifiers to target mentions
				for(Mention target: getTargetMentions(m,sentence,getTerminators(m,text))){
					target.addModifiers(getModifiers(m));
				}
			}
		}
		
		// add modifiers to anchor sentence mentions if it spans beyound sentence boundaries
		sentence.getMentions().addAll(getGlobalModifierMentions(relevantModifiers));
		time = System.currentTimeMillis() - time;
		sentence.getProcessTime().put(getClass().getSimpleName(),time);
		return sentence;
	}

	/**
	 * get a list of modifier mentions that have actions outside of sentence boundaries
	 * @param mentions
	 * @return
	 */
	private List<Mention> getGlobalModifierMentions(List<Mention> mentions){
		List<Mention> list = new ArrayList<Mention>();
		for(Mention m: mentions){
			Map map = m.getConcept().getProperties();
			if(map.containsKey(HAS_PARAGRAPH_ACTION) || map.containsKey(HAS_SECTION_ACTION)){
				list.add(m);
			}
		}
		return list;
	}
	
	
	/**
	 * Gets the modifiers.
	 *
	 * @param m the m
	 * @return the modifiers
	 * @throws TerminologyException the terminology exception
	 */
	private List<Modifier> getModifiers(Mention m) throws TerminologyException{
		List<Modifier> modifiers = Modifier.getModifiers(m);
		for(Modifier mod: modifiers){
			String val = getDefaultValues().get(mod.getType());
			mod.setDefaultValue(mod.getValue().equals(val));
		}
		return modifiers;
	}
	
	/**
	 * Gets the modifier.
	 *
	 * @param type the type
	 * @param value the value
	 * @return the modifier
	 */
	private Modifier getModifier(String type, String value){
		Modifier modifier = Modifier.getModifier(type,value);
		modifier.setDefaultValue(true);
		return modifier;
	}
	
	
	/**
	 * Gets the target mentions.
	 *
	 * @param modifier the modifier
	 * @param targetText the target text
	 * @param terminators the terminators
	 * @return the target mentions
	 * @throws TerminologyException the terminology exception
	 */
	private List<Mention> getTargetMentions(Mention modifier, Sentence targetText, List<Mention> terminators) throws TerminologyException {
		List<Mention> list = new ArrayList<Mention>();

		List<String> acts = getAction(modifier.getConcept());
		boolean forward =  acts.contains(ACTION_FORWARD) || acts.contains(ACTION_BIDIRECTIONAL);
		boolean backward = acts.contains(ACTION_BACKWARD) || acts.contains(ACTION_BIDIRECTIONAL);

		// this is no good, but if we got no actions defined, can we assume some default?
		if(forward == false && backward == false){
			forward = backward = true;
		}
		
		
		
		// figure out termination offset
		int start = getWordWindowIndex(modifier,targetText,false);
		int end   = getWordWindowIndex(modifier,targetText,true);
		
		//System.out.println(modifier+" st: "+start+"\tend: "+end+"\tsubs: "+targetText.getText().substring(start,end));
		
		// figure out terminator offset
		for(Mention m: terminators){
			// if going forward, make sure that the terminator is after modifier
			if(forward && modifier.before(m) && m.getStartPosition() < end){
				end = m.getStartPosition();
			}
			// if looking backward, make sure that the terminator is before modifier
			if(backward && modifier.after(m) && m.getStartPosition() > start)
				start = m.getStartPosition();
		}
		

		// go over mentions in a sentence
		for(Mention target: targetText.getMentions()){
			boolean add = false;

			// skip itself
			if(target.equals(modifier))
				continue;
			
			// looking forward, if modifier is before target and target is before termination point
			if(forward && (modifier.getStartPosition() <= target.getStartPosition() ||  modifier.getEndPosition() < target.getEndPosition()) && target.getStartPosition() <= end){
				add = true;
			}
			// looking backward, if modifier is after target and target is after termination point
			if(backward &&  modifier.getStartPosition() >= target.getStartPosition() && start <= target.getStartPosition()){
				add = true;
			}

			if(add && isModifierApplicable(modifier, target))
				list.add(target);
		}

		return list;
	}

	
	/**
	 * is a given modifier applicable for a given target
	 * @param target
	 * @param modifier
	 * @return true if it is applicable
	 */
	private boolean isModifierApplicable(Mention modifier,Mention target){
		//linguistic modifiers are applicable to everything
		if(isTypeOf(modifier, LINGUISTIC_MODIFIER) && !isTypeOf(target, MODIFIER))
			return true;
		
		// if we have an outside validator supplied, check with that
		if(modifierResolver != null){
			return modifierResolver.isModifierApplicable(modifier, target);
		}
		
		return false;
	}
	
	/**
	 * is a mention a type of some concept in ontology
	 * @param m - mention
	 * @param type - type
	 * @return true or false
	 */
	public static boolean isTypeOf(Mention m, String type){
		for(SemanticType st: m.getConcept().getSemanticTypes())
			if(st.getName().equals(type))
				return true;
		return false;
	}

    /**
     * Gets the word window index.
     *
     * @param modifier the modifier
     * @param targetText the target text
     * @param beforeModifier the before modifier
     * @return the word window index
     * @throws TerminologyException the terminology exception
     */
    private static int getWordWindowIndex(Mention modifier, Sentence targetText, boolean beforeModifier) throws TerminologyException {
        return getWordWindowIndex(modifier,targetText,beforeModifier,getWindowSize(modifier.getConcept()));
    }

	/**
	 * Gets the word window index.
	 *
	 * @param modifier the modifier
	 * @param targetText the target text
	 * @param beforeModifier the before modifier
     * @param windowSize the window size of a given modifier
	 * @return the word window index
	 */
	private static int getWordWindowIndex(Mention modifier, Sentence targetText, boolean beforeModifier,int windowSize){
		int offs;
		//int windowSize = getWindowSize(modifier.getConcept());
		String txt = targetText.getText();
		int offset = targetText.getOffset();
		
		// if windows size after modifier
		if(beforeModifier){
			offs = targetText.getLength();
			for(int i = modifier.getEndPosition()-offset,j=0,k=i;i>=0 && i<txt.length();i = txt.indexOf(' ',i+1)){ //j++
				// to avoid multiple consecutive spaces only increment word count if the delta is more then 1
				if(i > k +1)
					j++;
				if(j >= windowSize){
					offs = i;
					break;
				}
				k = i;
			}
		// if windows size before modifier	
		}else{
			offs = 0;
			for(int i = modifier.getStartPosition()-offset,j=0,k=i;i>=0;i = txt.lastIndexOf(' ',i-1)){ //,j++
				// to avoid multiple consecutive spaces only increment word count if the delta is more then 1
				if(i < k -1)
					j++;
				if(j >= windowSize){
					offs = i;
					break;
				}
				k=i;
			}
		}
		return offs+offset;
	}



	/**
	 * Gets the terminators.
	 *
	 * @param modifier the modifier
	 * @param text the text
	 * @return the terminators
	 * @throws TerminologyException the terminology exception
	 */
	private static List<Mention> getTerminators(Mention modifier,Sentence text) throws TerminologyException{
		List<Mention> list = new ArrayList<Mention>();
		List<String> terminators = getTermination(modifier.getConcept());

		for(Mention m: text.getMentions()){
			if(getAction(m.getConcept()).contains(ACTION_TERMINATE)) {
				for (Concept parent : m.getConcept().getParentConcepts()) {
					if (terminators.contains(parent.getCode())) {
						list.add(m);
					}
				}
			}
		}
		return list;
	}

	
	/**
	 * get a list of linguistic modifiers that are not pseudo modifiers.
	 *
	 * @param text the text
	 * @return the linguistic modifiers
	 * @throws TerminologyException the terminology exception
	 */
	private List<Mention> getRelevantModifiers(Sentence text) throws TerminologyException{
		List<Mention> list = new ArrayList<Mention>();
		List<Mention> pseudo = getPseudoModifiers(text);
		for(Mention m: text.getMentions()){
			if(isTypeOf(m,MODIFIER) && !isPseudo(m,pseudo)){
				list.add(m);
			}
		}
		return list;
	}
	
	
	/**
	 * get a list of pseudo modifier.
	 *
	 * @param text the text
	 * @return the pseudo modifiers
	 * @throws TerminologyException the terminology exception
	 */
	private List<Mention> getPseudoModifiers(Sentence text) throws TerminologyException{
		List<Mention> list = new ArrayList<Mention>();
		for(Mention m: text.getMentions()){
			if(isTypeOf(m,PSEUDO)){	
				list.add(m);
			}
		}
		return list;
	}
	

	/**
	 * is this method interacting with any of the pseudo modifiers?.
	 *
	 * @param m the m
	 * @param pseudo the pseudo
	 * @return true, if is pseudo
	 * @throws TerminologyException the terminology exception
	 */
	private boolean isPseudo(Mention m, List<Mention> pseudo) throws TerminologyException {
		if(pseudo.isEmpty())
			return false;
		
		// get a list of valid pseudo categories for this modifier
		List<String> actions = getPseudo(m.getConcept());
		
		// if we do have possible pseudo actions
		if(!actions.isEmpty()){
			for(Mention p: pseudo){
				// if this modifier intesects with this pseudo
				if(m.intersects(p)){
					// make sure that this pseudo is a pseudo for this modifier
					for(String a: actions){
						for(Concept pp : p.getConcept().getParentConcepts()){
							// if this is a valid group, then cancel this modifier
							if(a.equals(pp.getCode()))
								return true;
						}
					}
				}
			}
		}
		return false;
	}


	/**
	 * Gets the action.
	 *
	 * @param c the c
	 * @return the action
	 * @throws TerminologyException the terminology exception
	 */
	private static List<String> getAction(Concept c) throws TerminologyException {
		List<String> list = new ArrayList<String>();
		list.add(c.getProperty(HAS_SENTENCE_ACTION));
		return list;
	}
	
	
	/**
	 * get window size.
	 *
	 * @param c the c
	 * @return the window size
	 * @throws TerminologyException the terminology exception
	 */
	private static int getWindowSize(Concept c) throws TerminologyException {
		if(c.getProperties().containsKey(PROP_WINDOW_SIZE))
			return Integer.parseInt(""+c.getProperty(PROP_WINDOW_SIZE));
		for(Concept p: c.getParentConcepts()){
			return getWindowSize(p);
		}
		return DEFAULT_WINDOW_SIZE;
	}
	
	/**
	 * get modifier type for a given modifier mention.
	 *
	 * @param c the c
	 * @return the modifier types
	 */
	public static List<String> getModifierTypes(Concept c){
		List<String> types = new ArrayList<String>();
		for(SemanticType st: c.getSemanticTypes()){
			// skip general semantic types and UMLS semantic types as possible modifier types
			if(!MODIFIER_TYPES_FILTER.contains(st.getName()) && !SemanticType.isDefinedSemanticType(st.getName()))
				types.add(st.getCode());
		}
		return types;
	}
	
	/**
	 * get modifier value for a given mention.
	 *
	 * @param type the type
	 * @param m the mention
	 * @return the modifier value
	 */
	public static String getModifierValue(String type, Mention m) {
		Concept c = m.getConcept();
		
		// if quantity, then text is its value
		if(isTypeOf(m,QUANTITY))
			return m.getText();
		
		String val = c.getProperty(type);
		if(val == null)
			val = m.getText();
		return val;
	}
	
	
	/**
	 * Gets the termination.
	 *
	 * @param c the c
	 * @return the termination
	 * @throws TerminologyException the terminology exception
	 */
	private static List<String> getTermination(Concept c) throws TerminologyException {
		List<String> list = new ArrayList<String>();
		for(Concept p: c.getParentConcepts()){
			for(Concept t: p.getRelatedConcepts(Relation.getRelation(HAS_TERMINATION))){
				list.add(t.getCode());
			}
		}
		return list;
	}
	
	/**
	 * Gets the pseudo.
	 *
	 * @param c the c
	 * @return the pseudo
	 * @throws TerminologyException the terminology exception
	 */
	private List<String> getPseudo(Concept c) throws TerminologyException {
		List<String> list = new ArrayList<String>();
		for(Concept p: c.getParentConcepts()){
			for(Concept t: p.getRelatedConcepts(Relation.getRelation(HAS_PSEUDO))){
				list.add(t.getCode());
			}
		}
		return list;
	}
	
	
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.coder.model.Processor#getProcessTime()
	 */
	public long getProcessTime() {
		return time;
	}

	
	private void addModifier(Modifier m, String action, String location, Map<String,Map<String,List<Modifier>>> candidateModifiers){
		Map<String,List<Modifier>> map = candidateModifiers.get(m.getType());
		if(map == null){
			map = new HashMap<String, List<Modifier>>();
			candidateModifiers.put(m.getType(),map);
		}
		List<Modifier> list = map.get(location);
		if(list == null){
			if(ConText.ACTION_FIRST_MENTION.equals(action)){
				list = new ArrayList<Modifier>();
			}else{
				list = new Stack<Modifier>();
			}
			map.put(location,list);
		}
		list.add(m);
	}
	
	/**
	 * find semantically matching modifiers from a given list given a target mention
	 * @param globalModifiers - list of global modifier
	 * @param target - target mention
	 * @return list of modifiers that can be applied
	 */
	public List<Modifier> getMatchingModifiers(List<Mention> globalModifiers, Mention target) {
		
		// if modifier validator is not defined, no point in going further
		if(getModifierResolver() == null || globalModifiers.isEmpty() || target == null)
			return Collections.EMPTY_LIST;
		
		// allocate best modifiers
		//Map<String,Modifier> bestModifiers = new LinkedHashMap<String, Modifier>();
		
		// get the sentence
		Sentence sentence = target.getSentence();
		Spannable section = sentence.getSection();
		Spannable paragraph   = sentence.getParagraph();
		
		// if paragraph not there, make it a section
		if(paragraph == null)
			paragraph = section;
		
		// if still no paragraph (as section), just give up
		if(paragraph == null)
			return  Collections.EMPTY_LIST;
		
		// if we actually don't have a section defined, then make it a parapgraph as we know that 
		// think is defined already
		if(section == null)
			section = paragraph;
		
		// create a mapping of candidate modifiers for each type
		Map<String,Map<String,List<Modifier>>> candidateModifiers = new HashMap<String,Map<String,List<Modifier>>>();
		for(Mention modifier: globalModifiers){
			// lets see if this modifier fits the variable semantically
			if(section.contains(modifier)){
				Spannable span = section;
				String action = modifier.getConcept().getProperties().getProperty(ConText.HAS_PARAGRAPH_ACTION);
				if(action != null){
					span = paragraph;
				}else{
					action = modifier.getConcept().getProperties().getProperty(ConText.HAS_SECTION_ACTION);
				}
				
				// check if we have a modifier within the span
				if(span.contains(modifier)){
					// if we have a valid before action and the modifier is before
					if(modifier.before(target) && ConText.BEFORE_ACTIONS.contains(action)){
						for(Modifier mod : Modifier.getModifiers(modifier)){
							addModifier(mod, action, "before",candidateModifiers);
						}
					// if we have a valid after action and the modifier is after	
					}else if(modifier.after(target) && ConText.AFTER_ACTIONS.contains(action)){
						for(Modifier mod : Modifier.getModifiers(modifier)){
							addModifier(mod, action, "after",candidateModifiers);
						}
					}
				}
			}
		}
		
		// add best modifier
		List<Modifier> modifierList = new ArrayList<Modifier>();
		for(String type: candidateModifiers.keySet()){
			List<Modifier> beforeList = candidateModifiers.get(type).get("before");
			List<Modifier> afterList = candidateModifiers.get(type).get("after");
			
			// get the closest before modifier
			Modifier before = null;
			if(beforeList != null){
				for(Modifier modifier: beforeList){
					if(getModifierResolver().isModifierApplicable(modifier.getMention(), target)){
						before = modifier;
						break;
					}
				}
			}
			// get the closest after modifier
			Modifier after = null;
			if(afterList != null){
				for(Modifier modifier: afterList){
					if(getModifierResolver().isModifierApplicable(modifier.getMention(), target)){
						after = modifier;
						break;
					}
				}
			}

			// add the best modifier
			if(before != null && after != null){
				int bd = target.getStartPosition()-before.getMention().getEndPosition();
				int ad = after.getMention().getStartPosition() - target.getEndPosition();
				modifierList.add(bd < ad? before:after);
			}else if(before != null){
				modifierList.add(before);
			}else if(after != null){
				modifierList.add(after);
			}
		}
		return modifierList;
	}


    /**
     * Gets the target mentions in range
     *
     * @param modifier the modifier
     * @param targetText the target text
     * @param actions the modifier actions
     * @param windowSize the modifier window size
     * @return the target mentions
     */
    public static List<Mention> getTargetMentionsInRange(Mention modifier, Sentence targetText,List<String> actions, int windowSize){
        List<Mention> list = new ArrayList<Mention>();

        List<String> acts = actions; // getAction(modifier.getConcept());
        List<Mention> terminators = Collections.EMPTY_LIST;
        try{
            terminators = getTerminators(modifier,modifier.getSentence());
        }catch (TerminologyException ex){
            throw new TerminologyError("Oops",ex);
        }

        boolean forward =  acts.contains(ACTION_FORWARD) || acts.contains(ACTION_BIDIRECTIONAL);
        boolean backward = acts.contains(ACTION_BACKWARD) || acts.contains(ACTION_BIDIRECTIONAL);

        // this is no good, but if we got no actions defined, can we assume some default?
        if(forward == false && backward == false){
            forward = backward = true;
        }

        // figure out termination offset
        int start = getWordWindowIndex(modifier,targetText,false,windowSize);
        int end   = getWordWindowIndex(modifier,targetText,true,windowSize);

        //System.out.println(modifier+" st: "+start+"\tend: "+end+"\tsubs: "+targetText.getText().substring(start,end));

        // figure out terminator offset
        for(Mention m: terminators){
            // if going forward, make sure that the terminator is after modifier
            if(forward && modifier.before(m) && m.getStartPosition() < end){
                end = m.getStartPosition();
            }
            // if looking backward, make sure that the terminator is before modifier
            if(backward && modifier.after(m) && m.getStartPosition() > start)
                start = m.getStartPosition();
        }


        // go over mentions in a sentence
        for(Mention target: targetText.getMentions()){
            boolean add = false;

            // skip itself
            if(target.equals(modifier))
                continue;

            // looking forward, if modifier is before target and target is before termination point
            if(forward && (modifier.getStartPosition() <= target.getStartPosition() ||  modifier.getEndPosition() < target.getEndPosition()) && target.getStartPosition() <= end){
                add = true;
            }
            // looking backward, if modifier is after target and target is after termination point
            if(backward &&  modifier.getStartPosition() >= target.getStartPosition() && start <= target.getStartPosition()){
                add = true;
            }

            if(add)
                list.add(target);
        }

        return list;
    }

}
