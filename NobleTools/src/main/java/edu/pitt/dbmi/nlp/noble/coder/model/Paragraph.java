package edu.pitt.dbmi.nlp.noble.coder.model;

import java.util.List;

public class Paragraph extends Text {
	private String part;
	private Section section;
	private List<Sentence> sentences;
	
	/**
	 * Instantiates a new sentence.
	 *
	 * @param text the text
	 * @param offs the offs
	 */
	public Paragraph(String text,int offs){
		super(text,offs);
	}
	
	/**
	 * initialize with a document
	 * @param doc - document
	 * @param begin - start offset
	 * @param end - end offset
	 */
	public Paragraph(Document doc, int begin, int end){
		super(doc,begin,end);
	}
	
	/**
	 * get part name
	 * @return part name
	 */
	public String getPart() {
		return part;
	}
	/**
	 * set part name
	 * @param part name
	 */
	public void setPart(String part) {
		this.part = part;
	}
	
	/**
	 * is paragraph a part?
	 * @return true or false
	 */
	public boolean isPart(){
		return part != null;
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
	 * Gets the sentences.
	 * @return the sentences
	 */
	public List<Sentence> getSentences() {
		if(sentences == null){
			sentences = getDocument().getSentences(this);
		}
		return sentences;
	}
}
