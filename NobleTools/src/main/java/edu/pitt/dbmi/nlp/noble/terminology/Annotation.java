package edu.pitt.dbmi.nlp.noble.terminology;

import edu.pitt.dbmi.nlp.noble.coder.model.Spannable;
import edu.pitt.dbmi.nlp.noble.tools.TextTools;

import java.io.Serializable;
import java.util.*;

/**
 * concept annotation.
 *
 * @author tseytlin
 */
public class Annotation implements Serializable, Spannable, Comparable<Annotation>{
	private static final long serialVersionUID = 1234567890L;
	private String text, searchString;
	private int offset;
	private transient boolean updated;
	private transient Concept concept;
	
	/**
	 * Gets the concept.
	 *
	 * @return the concept
	 */
	public Concept getConcept() {
		return concept;
	}
	
	/**
	 * Sets the concept.
	 *
	 * @param concept the new concept
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.coder.model.Spannable#getText()
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * Sets the text.
	 *
	 * @param text the new text
	 */
	public void setText(String text) {
		this.text = text;
	}
	
	/**
	 * Gets the search string.
	 *
	 * @return the search string
	 */
	public String getSearchString() {
		return searchString;
	}
	
	/**
	 * Sets the search string.
	 *
	 * @param searchString the new search string
	 */
	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}
	
	/**
	 * Gets the offset.
	 *
	 * @return the offset
	 */
	public int getOffset() {
		return offset;
	}
	
	/**
	 * Update offset.
	 *
	 * @param o the o
	 */
	public void updateOffset(int o){
		updated = true;
		offset += o;
	}
	
	/**
	 * Checks if is offset updated.
	 *
	 * @return true, if is offset updated
	 */
	public boolean isOffsetUpdated(){
		return updated;
	}
	
	/**
	 * Gets the length.
	 *
	 * @return the length
	 */
	public int getLength(){
		return text.length();
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.coder.model.Spannable#getStartPosition()
	 */
	public int getStartPosition(){
		return offset;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.coder.model.Spannable#getEndPosition()
	 */
	public int getEndPosition(){
		return offset+text.length();
	}
	
	/**
	 * Sets the offset.
	 *
	 * @param offset the new offset
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return text+"/"+offset;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return toString().hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if(obj instanceof Annotation){
			if(offset != ((Annotation)obj).offset)
				return false;
		}else{
			return false;
		}
		return toString().equals(obj.toString());
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Annotation a) {
		int d = getOffset() - a.getOffset();
		if(d == 0)
			return getLength() - a.getLength();
		return d;
	}
	
	
	/**
	 * if annotation is already known, this method adds annotation properly to a concept.
	 *
	 * @param c the c
	 * @param txt the txt
	 * @param offset the offset
	 */
	public static void addAnnotation(Concept c, String txt, int offset){
		// add matched text
		c.addMatchedTerm(txt);
		
		// add annotation
		Annotation a = new Annotation();
		a.setText(txt);
		a.setConcept(c);
		a.setOffset(offset);
		c.addAnnotation(a);
		
		// add matched text
		int offs = Integer.MAX_VALUE;
		StringBuffer b = new StringBuffer();
		for(Annotation an: c.getAnnotations()){
			if(an.getOffset() < offs)
				offs = an.getOffset();
			b.append(an.getText()+" ");
		}
		c.setOffset(offs);
		c.setText(b.toString().trim());
	}
	
	/**
	 * Get a list of contiguous concept annotations from a given concept
	 * Essentially converts a single concepts that annotates multiple related words to text
	 * to potentially multiple instances of a concept in text.
	 *
	 * @param c the c
	 * @return the annotations
	 */
	public static List<Annotation> getAnnotations(Concept c){
		String text = c.getSearchString();
		List<Annotation> list = new ArrayList<Annotation>();
		try{
			int st = -1,en = -1;
			Set<String> usedWords = new HashSet<String>();
			for(Annotation a: c.getAnnotations()){
				String w = TextTools.normalize(a.getText(),true);
				// this word was encountered before, saved previous annoation
				if(usedWords.contains(w)){
					Annotation an = new Annotation();
					an.setSearchString(text);
					an.setConcept(c);
					an.setOffset(st);
					an.setText(text.substring(st,en));
					list.add(an);
					usedWords.clear();
					st = -1;
				}
				
				// start w/ first annotation
				if(st < 0)
					st = a.getStartPosition();
				// remember end position
				en = a.getEndPosition();
				
				usedWords.add(w);
			}
			// finish last annotation
			if(st >= 0 && en >= 0){
				Annotation an = new Annotation();
				an.setSearchString(text);
				an.setConcept(c);
				an.setOffset(st);
				an.setText(text.substring(st,en));
				list.add(an);
			}
		}catch(Exception ex){
			System.err.println("match: "+c.getMatchedTerm()+" | name: "+c.getName()+" | code: "+c.getCode());
			System.err.println("annotations: "+Arrays.toString(c.getAnnotations()));
			System.err.println("search: "+c.getSearchString()+"\n");
			System.err.println("error: "+ex.getMessage()+"\n");
	
		}
		return list;
	}
	
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.coder.model.Spannable#contains(edu.pitt.dbmi.nlp.noble.coder.model.Spannable)
	 */
	public boolean contains(Spannable s) {
		return getStartPosition() <= s.getStartPosition() && s.getEndPosition() <= getEndPosition();
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.coder.model.Spannable#intersects(edu.pitt.dbmi.nlp.noble.coder.model.Spannable)
	 */
	public boolean intersects(Spannable s) {
		//NOT this region ends before this starts or other region ends before this one starts
		return !(getEndPosition() < s.getStartPosition() || s.getEndPosition() < getStartPosition());
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.coder.model.Spannable#before(edu.pitt.dbmi.nlp.noble.coder.model.Spannable)
	 */
	public boolean before(Spannable s) {
		return getEndPosition() <= s.getStartPosition();
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.coder.model.Spannable#after(edu.pitt.dbmi.nlp.noble.coder.model.Spannable)
	 */
	public boolean after(Spannable s) {
		return s.getEndPosition() <= getStartPosition();
	}
}
