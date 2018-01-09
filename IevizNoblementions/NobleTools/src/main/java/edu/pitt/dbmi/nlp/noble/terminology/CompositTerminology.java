package edu.pitt.dbmi.nlp.noble.terminology;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.pitt.dbmi.nlp.noble.coder.model.Mention;
import edu.pitt.dbmi.nlp.noble.coder.model.Sentence;



/**
 * this terminology combines several terminologies to create one contigues access point.
 *
 * @author tseytlin
 */
public class CompositTerminology extends AbstractTerminology {
	private List<Terminology> terminologies;
	private Concept [] roots;
	private long time;
	
	/**
	 * add terminology to a stack.
	 *
	 * @param t the t
	 */
	public void addTerminology(Terminology t){
		getTerminologies().add(t);
		roots = null;
	}
	
	/**
	 * add terminology to a stack.
	 *
	 * @param t the t
	 */
	public void removeTerminology(Terminology t){
		getTerminologies().remove(t);
		roots = null;
	}
	
	/**
	 * get all terminologies.
	 *
	 * @return the terminologies
	 */
	public List<Terminology> getTerminologies(){
		if(terminologies == null)
			terminologies = new ArrayList<Terminology>();
		return terminologies;
			
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.Describable#getName()
	 */
	public String getName() {
		return getClass().getSimpleName();
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.Describable#getDescription()
	 */
	public String getDescription() {
		return "Access multiple terminologies through a single interface";
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.Describable#getVersion()
	 */
	public String getVersion() {
		return "1.0";
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.Describable#getURI()
	 */
	public URI getURI() {
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.Describable#getFormat()
	 */
	public String getFormat() {
		return "composit";
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.Describable#getLocation()
	 */
	public String getLocation() {
		return "memory";
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.AbstractTerminology#getSources()
	 */
	public Source[] getSources() {
		Set<Source> src = new LinkedHashSet<Source>();
		for(Terminology t: getTerminologies()){
			Collections.addAll(src,t.getSources());
		}
		return src.toArray(new Source [0]);
	}
	
	/**
	 * Get all supported relations between concepts.
	 *
	 * @return the relations
	 * @throws TerminologyException the terminology exception
	 */
	public Relation[] getRelations() throws TerminologyException {
		Set<Relation> rel = new LinkedHashSet<Relation>();
		for(Terminology t: getTerminologies()){
			Collections.addAll(rel,t.getRelations());
		}
		return rel.toArray(new Relation [0]);
	}
	
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.Terminology#getSourceFilter()
	 */
	public Source[] getSourceFilter() {
		List<Source> src = new ArrayList<Source>();
		for(Terminology t: getTerminologies()){
			Collections.addAll(src,t.getSourceFilter());
		}
		return src.toArray(new Source [0]);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.Terminology#setSourceFilter(edu.pitt.dbmi.nlp.noble.terminology.Source[])
	 */
	public void setSourceFilter(Source[] srcs) {
		for(Terminology t: getTerminologies()){
			t.setSourceFilter(srcs);
		}
	}
	
	/**
	 * search multiple terminologies.
	 *
	 * @param text the text
	 * @return the concept[]
	 * @throws TerminologyException the terminology exception
	 */
	public Concept[] search(String text) throws TerminologyException {
		return search(text,getSearchMethods()[0]);
	}

	/**
	 * lookup from multiple terminologies.
	 *
	 * @param cui the cui
	 * @return the concept
	 * @throws TerminologyException the terminology exception
	 */
	public Concept lookupConcept(String cui) throws TerminologyException {
		for(Terminology t: getTerminologies()){
			Concept c = t.lookupConcept(cui);
			if(c != null)
				return c;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.AbstractTerminology#convertConcept(java.lang.Object)
	 */
	protected Concept convertConcept(Object obj) {
		//NOOP
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.AbstractTerminology#getRelatedConcepts(edu.pitt.dbmi.nlp.noble.terminology.Concept, edu.pitt.dbmi.nlp.noble.terminology.Relation)
	 */
	public Concept[] getRelatedConcepts(Concept c, Relation r) throws TerminologyException {
		// get related concepts from source terminolgies
		return c.getRelatedConcepts(r);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.AbstractTerminology#getRelatedConcepts(edu.pitt.dbmi.nlp.noble.terminology.Concept)
	 */
	public Map getRelatedConcepts(Concept c) throws TerminologyException {
		// get related concepts from source terminolgies
		return c.getRelatedConcepts();
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.AbstractTerminology#search(java.lang.String, java.lang.String)
	 */
	public Concept[] search(String text, String method) throws TerminologyException {
		List<Concept> result = new ArrayList<Concept>();
		for(Terminology t: getTerminologies()){
			Collections.addAll(result,t.search(text,method));
		}
		return result.toArray(new Concept [0]);
	}

	/**
	 * process sentence.
	 *
	 * @param s the s
	 * @return the sentence
	 * @throws TerminologyException the terminology exception
	 */
	public Sentence process(Sentence s) throws TerminologyException {
		time = System.currentTimeMillis();
		List<Mention> mentions = new ArrayList<Mention>();
		for(Terminology t: getTerminologies()){
			mentions.addAll(t.process(s).getMentions());
		}
		s.setMentions(mentions);
		time = System.currentTimeMillis() - time;
		return s;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.AbstractTerminology#getProcessTime()
	 */
	public long getProcessTime() {
		return time;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.AbstractTerminology#getSearchMethods()
	 */
	public String[] getSearchMethods() {
		Set<String> result = new LinkedHashSet<String>();
		for(Terminology t: getTerminologies()){
			Collections.addAll(result,t.getSearchMethods());
		}
		return result.toArray(new String [0]);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.AbstractTerminology#getRootConcepts()
	 */
	public Concept[] getRootConcepts() throws TerminologyException {
		if(roots == null){
			List<Concept> result = new ArrayList<Concept>();
			for(Terminology t: getTerminologies()){
				Collections.addAll(result,t.getRootConcepts());
			}
			roots = result.toArray(new Concept [0]);
		}
		return roots;
		
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.AbstractTerminology#getConcepts()
	 */
	public Collection<Concept> getConcepts() throws TerminologyException {
		Collection<Concept> list = new ArrayList<Concept>();
		for(Terminology t: getTerminologies())
			list.addAll(t.getConcepts());
		return list;
	}

	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.Terminology#getSemanticTypeFilter()
	 */
	public SemanticType[] getSemanticTypeFilter() {
		List<SemanticType> src = new ArrayList<SemanticType>();
		for(Terminology t: getTerminologies()){
			Collections.addAll(src,t.getSemanticTypeFilter());
		}
		return src.toArray(new SemanticType [0]);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.Terminology#setSemanticTypeFilter(edu.pitt.dbmi.nlp.noble.terminology.SemanticType[])
	 */
	public void setSemanticTypeFilter(SemanticType[] srcs) {
		for(Terminology t: getTerminologies()){
			t.setSemanticTypeFilter(srcs);
		}
	}
	
	public void dispose(){
		for(Terminology t: getTerminologies()){
			t.dispose();
		}
	}
	
}
