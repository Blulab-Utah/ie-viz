package edu.pitt.dbmi.nlp.noble.coder.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class Text.
 */
public class Text implements Spannable {
	protected String text;
	protected int start,end = -1;
	protected Map<String,Long> processTime;
	protected Document document;
	protected Map<String,String> properties;
	
	/**
	 * Instantiates a new text.
	 */
	public Text() {}
	
	/**
	 * Instantiates a new text.
	 *
	 * @param text the text
	 */
	public Text(String text) {
		this.text = text;
	}
	
	/**
	 * Instantiates a new text.
	 *
	 * @param text the text
	 * @param offset the offset
	 */
	public Text(String text, int offset) {
		this.text = text;
		this.start = offset;
	}
	
	/**
	 * initialize text as substring of document
	 * @param doc - original document
	 * @param begin - start offset
	 * @param end - end offset
	 */
	public Text(Document doc, int begin, int end){
		this.document = doc;
		this.start = begin;
		this.end = end;
	}
	

	/**
	 * Gets the document.
	 *
	 * @return the document
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * Sets the document.
	 *
	 * @param document the new document
	 */
	public void setDocument(Document document) {
		this.document = document;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.coder.model.Spannable#getText()
	 */
	public String getText() {
		if(text != null)
			return text;
		if(document != null && end > -1)
			return document.getText().substring(start,end);
		return null;
	}
	
	/**
	 * Gets the trimmed text.
	 *
	 * @return the trimmed text
	 */
	public String getTrimmedText() {
		return getText().trim();
	}
	
	/**
	 * Gets the properties.
	 *
	 * @return the properties
	 */
	public Map<String,String> getProperties() {
		if(properties == null)
			properties = new LinkedHashMap<String, String>();
		return properties;
	}
	
	/**
	 * Sets the properties.
	 *
	 * @param properties the properties
	 */
	public void setProperties(Map<String,String> properties) {
		this.properties = properties;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.coder.model.Spannable#getStartPosition()
	 */
	public int getStartPosition() {
		return start;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.coder.model.Spannable#getEndPosition()
	 */
	public int getEndPosition() {
		return start+getLength();
	}
	
	/**
	 * Gets the offset.
	 *
	 * @return the offset
	 */
	public int getOffset() {
		return start;
	}
	
	/**
	 * Sets the offset.
	 *
	 * @param offset the new offset
	 */
	public void setOffset(int offset) {
		this.start = offset;
	}
	
	/**
	 * Update offset.
	 *
	 * @param delta the delta
	 */
	public void updateOffset(int delta){
		this.start += delta;
		if(end > -1)
			this.end += delta;
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
	 * Gets the length.
	 *
	 * @return the length
	 */
	public int getLength(){
		return (text != null)?text.length():end-start;
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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return getText();
	}
	
	/**
	 * Gets the process time.
	 *
	 * @return the process time
	 */
	public Map<String,Long> getProcessTime(){
		if(processTime == null){
			processTime = new LinkedHashMap<String, Long>();
		}
		return processTime;
	}

	
	/**
	 * get a distance between two spannable objects in a same search string
	 * in a number of words.
	 *
	 * @param search the search
	 * @param a the a
	 * @param b the b
	 * @return 0 - if one span contains or intersects the other, or distance in words, -1 something went wrong
	 */
	public static int getWordDistance(Spannable search, Spannable a, Spannable b){
		if(a.contains(b) || a.intersects(b))
			return 0;
		
		int s = a.getStartPosition() < b.getStartPosition()?a.getEndPosition():b.getEndPosition();
		int e = a.getStartPosition() < b.getStartPosition()?b.getStartPosition():a.getStartPosition();
		
		// make sure that spannable search contains those annotations
		if(s < search.getStartPosition() || e > search.getEndPosition()){
			return -1;
		}
		String sub = search.getText().substring(s-search.getStartPosition(),e-search.getStartPosition());
		return sub.replaceAll("\\w+","").length()-1;
	}
	
	/**
	 * get a distance between two spannable objects in a same search string
	 * in a number of words.
	 *
	 * @param a the a
	 * @param b the b
	 * @return 0 - if one span contains or intersects the other, or distance in words, -1 something went wrong
	 */
	public static int getOffsetDistance(Spannable a, Spannable b){
		if(a.contains(b) || a.intersects(b))
			return 0;
		
		int s = a.getStartPosition() < b.getStartPosition()?a.getEndPosition():b.getEndPosition();
		int e = a.getStartPosition() < b.getStartPosition()?b.getStartPosition():a.getStartPosition();
		
		return e - s;
	}
}
