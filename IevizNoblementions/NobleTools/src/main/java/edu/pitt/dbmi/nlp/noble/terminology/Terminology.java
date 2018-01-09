package edu.pitt.dbmi.nlp.noble.terminology;

import java.util.Collection;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.pitt.dbmi.nlp.noble.coder.model.Processor;
import edu.pitt.dbmi.nlp.noble.coder.model.Sentence;



/**
 * This class performs basic terminology lookup.
 *
 * @author Eugene Tseytlin (University of Pittsburgh)
 */
public interface Terminology extends Describable, Processor<Sentence> {

	/**
	 * add new concept to the terminology.
	 *
	 * @param c the c
	 * @return true, if successful
	 * @throws TerminologyException the terminology exception
	 */
	public boolean addConcept(Concept c) throws TerminologyException;
	
	/**
	 * update concept information.
	 *
	 * @param c the c
	 * @return true, if successful
	 * @throws TerminologyException the terminology exception
	 */
	public boolean updateConcept(Concept c) throws TerminologyException;
	
	/**
	 * remove existing concept.
	 *
	 * @param c the c
	 * @return true, if successful
	 * @throws TerminologyException the terminology exception
	 */
	public boolean removeConcept(Concept c) throws TerminologyException;
	
	
	/**
	 * Return list of all sources in this terminology.
	 *
	 * @return the sources
	 */
	public Source [] getSources();
	
	
	/**
	 * get list of sources that match some criteria
	 * '*' or 'all' means all sources
	 * Ex: NCI,SNOMED,MEDLINE will find relevant source objects in given order.
	 *
	 * @param matchtext the matchtext
	 * @return the sources
	 */
	public Source [] getSources(String matchtext);
	
	
	/**
	 * get list of sources that are used as a filter.
	 *
	 * @return the source filter
	 */
	public Source [] getSourceFilter();
	
	/**
	 * get list of semantic types that are used as a filter.
	 *
	 * @return the semantic type filter
	 */
	public SemanticType [] getSemanticTypeFilter();
	
	/**
	 * Set source filter. When terminology is used only use stuff from given sources.
	 * The order of sources in Source [] array, should also determine precedence
	 * @param srcs
	 * NOTE: functionality of this call is limmited by underlying implementation
	 * of Terminology
	 */
	public void setSourceFilter(Source[] srcs);
	
	/**
	 * Set semantic type filter. When terminology is used only use stuff from given list of
	 * semantic types
	 * The order of sources in SemanticType [] array, should also determine precedence
	 *
	 * @param srcs the new semantic type filter
	 */
	public void setSemanticTypeFilter(SemanticType[] srcs);
	
	/**
	 * Return a list of concepts that can be mapped to the input string. 
	 * The list is flat. The input string may contain several concepts.
	 * Each Concept object contains a reference to the text that concept 
	 * was mapped to as well as offset within an input string
	 *
	 * @param text to be mapped to concepts
	 * @return List of Concept objects
	 * @throws TerminologyException the terminology exception
	 */
	public Concept[] search(String text) throws TerminologyException;

	
	/**
	 * Return a list of concepts that can be mapped to the input string. 
	 * The list is flat. The input string may contain several concepts.
	 * Each Concept object contains a reference to the text that concept 
	 * was mapped to as well as offset within an input string
	 *
	 * @param text to be mapped to concepts
	 * @param method - search method, use getSearchMethods to see available
	 * search methods
	 * @return List of Concept objects
	 * @throws TerminologyException the terminology exception
	 */
	public Concept[] search(String text, String method) throws TerminologyException;
	
	
	/**
	 * return supported search methods for this terminology.
	 *
	 * @return the search methods
	 */
	public String [] getSearchMethods();
	
	
	/**
	 * Lookup concept information if unique identifier is available.
	 *
	 * @param cui the cui
	 * @return Concept object
	 * @throws TerminologyException the terminology exception
	 */
	public Concept lookupConcept(String cui) throws TerminologyException;
	
	

	/**
	 * Get concepts related to parameter concept based on some relationship.
	 *
	 * @param c the c
	 * @param r the r
	 * @return related concepts
	 * @throws TerminologyException the terminology exception
	 */
	public Concept [] getRelatedConcepts(Concept c, Relation r) throws TerminologyException;
	
	/**
	 * Get all concepts related to parameter concept.
	 *
	 * @param c the c
	 * @return Map where relation is a key and list of related concepts is a value
	 * @throws TerminologyException the terminology exception
	 */
	public Map getRelatedConcepts(Concept c) throws TerminologyException;
	
	
	/**
	 * Get all supported relations between concepts.
	 *
	 * @return the relations
	 * @throws TerminologyException the terminology exception
	 */
	public Relation[] getRelations() throws TerminologyException ;

	/**
	 * Get all relations for specific concept, one actually needs to explore
	 * a concept graph (if available) to determine those.
	 *
	 * @param c the c
	 * @return the relations
	 * @throws TerminologyException the terminology exception
	 */
	public Relation[] getRelations(Concept c) throws TerminologyException ;
	
	/**
	 * Get all supported languages.
	 *
	 * @return the languages
	 */
	public String [] getLanguages();
	
	
	/**
	 * get all root concepts. This makes sense if Terminology is in fact ontology
	 * that has heirchichal structure
	 *
	 * @return the root concepts
	 * @throws TerminologyException the terminology exception
	 */
	public Concept[] getRootConcepts() throws TerminologyException;
	
	
	/**
	 * convert Terminology to XML DOM object representation.
	 *
	 * @param doc the doc
	 * @return the element
	 * @throws TerminologyException the terminology exception
	 */
	public Element toElement(Document doc) throws TerminologyException;
	
	/**
	 * initialize terminology from XML DOM object representation.
	 *
	 * @param element the element
	 * @throws TerminologyException the terminology exception
	 */
	public void fromElement(Element element) throws TerminologyException;
	
	/**
	 * get all available concept objects in terminology. Only sensible for small terminologies
	 *
	 * @return the concepts
	 * @throws TerminologyException the terminology exception
	 */
	public Collection<Concept> getConcepts() throws TerminologyException;
	
	/**
	 * Dispose terminology resources
	 */
	public void dispose();
}
