package edu.pitt.dbmi.nlp.noble.coder.model;

import java.util.*;

/**
 * This object represent a sentence in a document
 * "Sentence" can be of different types: from standard prose, to list, to synoptic.
 *
 * @author tseytlin
 */
public class Sentence extends Text {
	public static final String TYPE_PROSE = "Prose";
	public static final String TYPE_LINE  = "Line";
	public static final String TYPE_BULLET  = "Bullet";
	public static final String TYPE_WORKSHEET = "Worksheet";
	public static final String TYPE_HEADER = "Header";
	public static final String TYPE_DIVIDER = "Divider";
	
	
	private String sentenceType = TYPE_PROSE;
	private List<Mention> mentions;
	private Section section;
	private Paragraph paragraph;
	
	/**
	 * Instantiates a new sentence.
	 */
	public Sentence(){}
	
	/**
	 * Instantiates a new sentence.
	 *
	 * @param text the text
	 */
	public Sentence(String text){
		this(text,0,TYPE_PROSE);
	}
	
	/**
	 * Instantiates a new sentence.
	 *
	 * @param s the s
	 */
	public Sentence(Sentence s){
		this(s.getText(),s.getOffset(),s.getSentenceType());
		setDocument(s.getDocument());
	}
	
	
	/**
	 * Instantiates a new sentence.
	 *
	 * @param text the text
	 * @param offs the offs
	 * @param type the type
	 */
	public Sentence(String text,int offs,String type){
		setText(text);
		setOffset(offs);
		this.sentenceType = type;
	}

	
	/**
	 * Gets the sentence type.
	 *
	 * @return the sentence type
	 */
	public String getSentenceType() {
		return sentenceType;
	}
	
	/**
	 * Sets the sentence type.
	 *
	 * @param sentenceType the new sentence type
	 */
	public void setSentenceType(String sentenceType) {
		this.sentenceType = sentenceType;
	}
	
	/**
	 * Gets the mentions.
	 *
	 * @return the mentions
	 */
	public List<Mention> getMentions() {
		if(mentions == null)
			mentions = new ArrayList<Mention>();
		return mentions;
	}
	/**
	 * add a mention to a list of mentions
	 * @param m mention
	 */
	public void addMention(Mention m){
		getMentions().add(m);
		setMentions(getMentions());
	}
	
	/**
	 * add a mention to a list of mentions
	 * @param m collection of mentions
	 */
	public void addMentions(Collection<Mention> m){
		getMentions().addAll(m);
		setMentions(getMentions());
	}
	
	
	/**
	 * Sets the mentions.
	 *
	 * @param mentions the new mentions
	 */
	public void setMentions(List<Mention> mentions) {
		this.mentions = mentions;
		Collections.sort(this.mentions);
		for(Mention m: this.mentions){
			m.setSentence(this);
		}
	}

	/**
	 * get paragraph that this sentence is part of
	 * @return a paragram that this sentence belongs to
	 */
	public Paragraph getParagraph() {
		if(paragraph == null){
			paragraph = getDocument().getParagraph(this);
		}
		return paragraph;
	}
	
	/**
	 * Gets the section.
	 *
	 * @return the section
	 */
	public Section getSection() {
		if(section == null)
			section = getDocument().getSection(this);
		return section;
	}
	
	/**
	 * Sets the section.
	 *
	 * @param section the new section
	 *
	public void setSection(Section section) {
		this.section = section;
	}
	*/
	
}
