package edu.pitt.dbmi.nlp.noble.coder.model;

import java.util.*;

import edu.pitt.dbmi.nlp.noble.terminology.Annotation;
import edu.pitt.dbmi.nlp.noble.terminology.Concept;

/**
 * This class represents an object that represents an input document.
 *
 * @author tseytlin
 */
public class Document extends Text {
	public static final String TYPE_MEDICAL_REPORT = "Medical Report";
	public static final String TYPE_MEDLINE_RECORD = "Medline Record";
	public static final String TYPE_ARTICLE  = "Article";
	
	public static final String STATUS_UNPROCESSED = "Unprocessed";
	public static final String STATUS_PARSED = "Parsed";
	public static final String STATUS_CODED = "Coded";
	
	private String name,location;
	private String documentStatus = STATUS_UNPROCESSED,documentType = TYPE_MEDICAL_REPORT;
	private List<Section> sections;
	private List<Sentence> sentences;
	private List<Paragraph> paragraphs;
	
	/**
	 * Instantiates a new document.
	 */
	public Document(){}
	
	/**
	 * Instantiates a new document.
	 *
	 * @param text the text
	 */
	public Document(String text){
		setText(text);
	}
	
	/**
	 * Gets the title.
	 *
	 * @return the title
	 */
	public String getTitle() {
		return name;
	}
	
	/**
	 * Sets the title.
	 *
	 * @param name the new title
	 */
	public void setTitle(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the location.
	 *
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}
	
	/**
	 * Sets the location.
	 *
	 * @param location the new location
	 */
	public void setLocation(String location) {
		this.location = location;
	}
	
	/**
	 * Gets the sections.
	 *
	 * @return the sections
	 */
	public List<Section> getSections() {
		if(sections == null)
			sections = new ArrayList<Section>();
		return sections;
	}
	
	/**
	 * Gets the sections.
	 *
	 * @return the sections
	 */
	public List<Paragraph> getParagraphs() {
		if(paragraphs == null)
			paragraphs = new ArrayList<Paragraph>();
		return paragraphs;
	}
	
	/**
	 * Sets the sections.
	 *
	 * @param sections the new sections
	 */
	public void setSections(List<Section> sections) {
		this.sections = null;
		addSections(sections);
	}
	
	/**
	 * Adds the sentence.
	 *
	 * @param s the s
	 */
	public void addSentence(Sentence s){
		getSentences().add(s);
		s.setDocument(this);
	}
	
	/**
	 * Adds the sentences.
	 *
	 * @param ss the ss
	 */
	public void addSentences(Collection<Sentence> ss){
		getSentences().addAll(ss);
		for(Sentence s: ss)
			s.setDocument(this);
	}
	
	/**
	 * Adds the section.
	 *
	 * @param s the s
	 */
	public void addSection(Section s){
		getSections().add(s);
		s.setDocument(this);
	}
	
	public void addParagraph(Paragraph p){
		getParagraphs().add(p);
	}
	
	/**
	 * Adds the sections.
	 *
	 * @param ss the ss
	 */
	public void addSections(Collection<Section> ss){
		getSections().addAll(ss);
		for(Section s: ss)
			s.setDocument(this);
	}

	/**
	 * Gets the sentences.
	 *
	 * @return the sentences
	 */
	public List<Sentence> getSentences() {
		if(sentences == null)
			sentences = new ArrayList<Sentence>();
		return sentences;
	}
	
	/**
	 * Sets the sentences.
	 *
	 * @param sentences the new sentences
	 */
	public void setSentences(List<Sentence> sentences) {
		this.sentences = sentences;
	}
	
	/**
	 * Gets the mentions.
	 *
	 * @return the mentions
	 */
	public List<Mention> getMentions(){
		List<Mention> mentions = new ArrayList<Mention>();
		for(Sentence s: getSentences()){
			mentions.addAll(s.getMentions());
		}
		return mentions;
	}
	
	/**
	 * Gets the concepts.
	 *
	 * @return the concepts
	 */
	public Set<Concept> getConcepts(){
		Set<Concept> mentions = new LinkedHashSet<Concept>();
		for(Mention s: getMentions()){
			mentions.add(s.getConcept());
		}
		return mentions;
	}
	
	/**
	 * Gets the annotations.
	 *
	 * @return the annotations
	 */
	public List<Annotation> getAnnotations(){
		List<Annotation> annotations = new ArrayList<Annotation>();
		for(Mention s: getMentions()){
			annotations.addAll(s.getAnnotations());
			for(Annotation a: s.getModifierAnnotations()){
				annotations.add(a);
			}
		}
		Collections.sort(annotations);
		return annotations;
	}
	
	/**
	 * Gets the document status.
	 *
	 * @return the document status
	 */
	public String getDocumentStatus() {
		return documentStatus;
	}
	
	/**
	 * Sets the document status.
	 *
	 * @param documentStatus the new document status
	 */
	public void setDocumentStatus(String documentStatus) {
		this.documentStatus = documentStatus;
	}
	
	/**
	 * Gets the document type.
	 *
	 * @return the document type
	 */
	public String getDocumentType() {
		return documentType;
	}
	
	/**
	 * Sets the document type.
	 *
	 * @param documentType the new document type
	 */
	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}
	
	
	/**
	 * get section that this spannable object belongs to.
	 *
	 * @param sp the sp
	 * @return null- if no such section exists
	 */
	public Section getSection(Spannable sp){
		for(Section s: getSections()){
			if(s.contains(sp))
				return s;
		}
		return null;
	}
	
	/**
	 * get section that this spannable object belongs to.
	 *
	 * @param sp the sp
	 * @return null- if no such section exists
	 */
	public Paragraph getParagraph(Spannable sp){
		for(Paragraph s: getParagraphs()){
			if(s.contains(sp))
				return s;
		}
		return null;
	}
	
	/**
	 * get a lost of sentences that are withing a given span
	 * @param sp - spannable text
	 * @return list of sentences within that span
	 */
	public List<Sentence> getSentences(Spannable sp){
		List<Sentence> list = new ArrayList<Sentence>();
		for(Sentence s: getSentences()){
			if(sp.contains(s)){
				list.add(s);
			}else if(!list.isEmpty()){
				break;
			}
		}
		return list;
	}
	
	/**
	 * get a lost of paragraphs that are withing a given span
	 * @param sp - spannable text
	 * @return list of paragraphs within that span
	 */
	public List<Paragraph> getParagraphs(Spannable sp){
		List<Paragraph> list = new ArrayList<Paragraph>();
		for(Paragraph s: getParagraphs()){
			if(sp.contains(s)){
				list.add(s);
			}else if(!list.isEmpty()){
				break;
			}
		}
		return list;
	}
}
