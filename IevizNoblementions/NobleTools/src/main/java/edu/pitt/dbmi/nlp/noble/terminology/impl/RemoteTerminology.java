package edu.pitt.dbmi.nlp.noble.terminology.impl;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import edu.pitt.dbmi.nlp.noble.terminology.AbstractTerminology;
import edu.pitt.dbmi.nlp.noble.terminology.Concept;
import edu.pitt.dbmi.nlp.noble.terminology.Relation;
import edu.pitt.dbmi.nlp.noble.terminology.SemanticType;
import edu.pitt.dbmi.nlp.noble.terminology.Source;
import edu.pitt.dbmi.nlp.noble.util.Parcel;
import edu.pitt.dbmi.nlp.noble.util.Sender;


/**
 * This class simply forwards all terminology requests to some
 * terminology EVS, CTS, LexBIG running on some given server
 * This class uses servlets to communicate.
 *
 * @author tseytlin
 */
public class RemoteTerminology extends AbstractTerminology {
	public static final String DEFAULT_SERVER = "http://slidetutor.upmc.edu/term/servlet/TerminologyServlet";
	private Sender sender;
	private Source [] filter;
	private String term;
	
	/**
	 * Terminology located on a server (forward to some implementation).
	 *
	 * @param servlet the servlet
	 */
	public RemoteTerminology(URL servlet){
		sender = new Sender(servlet);
	}
	
	/**
	 * Terminology located on a server (forward to some implementation).
	 */
	public RemoteTerminology(){
		try {
			sender = new Sender(new URL(DEFAULT_SERVER));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * get available terminologies.
	 *
	 * @return the available terminologies
	 */
	public String [] getAvailableTerminologies(){
		Set terms = (Set) sender.sendObject(new Parcel("get_terminologies",null));
		return (String []) terms.toArray(new String [0]);
	}
	
	/**
	 * specify a particular terminology.
	 *
	 * @param str the new terminology
	 */
	public void setTerminology(String str){
		term = str;
	}
	
	
	/**
	 * filter parcel before sending.
	 *
	 * @param p the p
	 * @return the parcel
	 */
	private Parcel filter(Parcel p){
		// set specific terminology
		if(p != null && term != null)
			p.getProperties().setProperty("term",term);
		
		return p;
	}
	
	/**
	 * This method is irrelevant in this context and hence not implemented.
	 *
	 * @param obj the obj
	 * @return the concept
	 */
	protected Concept convertConcept(Object obj) {
		return null;
	}

	/**
	 * get related concepts.
	 *
	 * @param c the c
	 * @param r the r
	 * @return the related concepts
	 */
	public Concept[] getRelatedConcepts(Concept c, Relation r) {
		Concept [] result = (Concept []) sender.sendObject(filter(new Parcel("get_related_concepts",new Object[]{c,r})));
		for(Concept co: result)
			co.setTerminology(this);
		return result;
	}

	/**
	 * get related concepts.
	 *
	 * @param c the c
	 * @return the related concepts
	 */
	public Map getRelatedConcepts(Concept c) {
		return (Map) sender.sendObject(filter(new Parcel("get_related_concept_map",c)));
	}
	
	
	/**
	 * Get list of sources that are supported by this terminology.
	 *
	 * @return the sources
	 */
	public Source[] getSources() {
		return (Source []) sender.sendObject(filter(new Parcel("get_sources",null)));
	}

	
	/**
	 * Set terminology sources.
	 *
	 * @param filter the new source filter
	 */
	public void setSourceFilter(Source [] filter) {
		this.filter = filter;
		sender.sendObject(filter(new Parcel("set_sources",filter)));
	}
	
	/**
	 * get filter set from before.
	 *
	 * @return the source filter
	 */
	public Source [] getSourceFilter(){
		return filter;
	}
	
	/**
	 * lookup concept object based on CUI.
	 *
	 * @param cui the cui
	 * @return the concept
	 */
	public Concept lookupConcept(String cui) {
		Concept c = (Concept) sender.sendObject(filter(new Parcel("lookup_concept",cui)));
		if(c == null)
			return null;
		c.setTerminology(this);
		return c;
	}

	
	/**
	 * Search terminology for concepts.
	 *
	 * @param text the text
	 * @return the concept[]
	 */
	public Concept[] search(String text) {
		Concept [] result = (Concept []) sender.sendObject(filter(new Parcel("search",text)));
		if(result == null)
			return new Concept [0];
		for(Concept c: result){
			c.setTerminology(this);
		}
		return result;
	}

	
	/**
	 * get name of an item.
	 *
	 * @return the name
	 */
	public String getName(){
		return "Remote Terminology";
	}
	
	/**
	 * get description of an item.
	 *
	 * @return the description
	 */
	public String getDescription(){
		return "Forwards all terminology requests to a servlet";
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return getName();
	}
	
	/**
	 * get version of an item.
	 *
	 * @return the version
	 */
	public String getVersion(){
		return "1.0";
	}
	
	/**
	 * get URI of an item.
	 *
	 * @return the uri
	 */
	public URI getURI(){
		return URI.create(""+sender.getServletURL());
	}
	
	/**
	 * get format.
	 *
	 * @return the format
	 */
	public String getFormat(){
		return "HTTP";
	}
	
	/**
	 * get location.
	 *
	 * @return the location
	 */
	public String getLocation(){
		return ""+getURI();
	}

	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.Terminology#getSemanticTypeFilter()
	 */
	public SemanticType[] getSemanticTypeFilter() {
		// TODO Auto-generated method stub
		return null;
	}

	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.Terminology#setSemanticTypeFilter(edu.pitt.dbmi.nlp.noble.terminology.SemanticType[])
	 */
	public void setSemanticTypeFilter(SemanticType[] srcs) {
		// TODO Auto-generated method stub
		
	}
}
