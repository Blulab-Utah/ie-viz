package edu.pitt.dbmi.nlp.noble.coder.processor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.pitt.dbmi.nlp.noble.coder.model.*;
import edu.pitt.dbmi.nlp.noble.terminology.TerminologyException;

public class ParagraphProcessor implements Processor<Document> {
	private static final String PARAGRAPH = "(?:\\s*\\n){2,}";
	private static final String DSPACE_PARAGRAPH = "(?:\\s*\\n){3,}";
	private static final String DIVS = "\\-{5,}|_{5,}|={5,}";
	private static final String PARTS = "PARTS?\\s+\\d+(\\s+AND\\s+\\d+)?:?";
	private static final Pattern PATTERN = Pattern.compile("("+PARAGRAPH+"|"+DIVS+"|"+PARTS+")",Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
	private static final Pattern DSPACE_PATTERN = Pattern.compile("("+DSPACE_PARAGRAPH+"|"+DIVS+"|"+PARTS+")",Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
	private static final Pattern SINGLE_SPACE = Pattern.compile("^[^\\n]{5,}\\n[^\\n]{5,}$",Pattern.MULTILINE|Pattern.DOTALL); 
	private long time;
	
	
	/**
	 * find paragraphs
	 */
	public Document process(Document doc) throws TerminologyException {
		time = System.currentTimeMillis();
		
		// lets try to rely on sections first
		if(doc.getSections().isEmpty()){
			parseParagraphs(doc);
		}else{
			for(Section section: doc.getSections()){
				parseParagraphs(doc,section.getBody(),section.getBodyOffset(),section.getEndPosition());
			}
		}
		
		time = System.currentTimeMillis()-time;
		doc.getProcessTime().put(getClass().getSimpleName(),time);
		return doc;
	}
	
	/**
	 * parse paragraphs from given text
	 * @param doc - document to add paragraphs to
	 */
	private void parseParagraphs(Document doc){
		parseParagraphs(doc,doc.getText(),0,doc.getText().length());
	}

	/**
	 * parse paragraphs from given text
	 * @param doc - document to add paragraphs to
	 * @param text - text 
	 * @param bodyOffset -offset of section
	 * @param endPosition - end offset of section
	 */
	private void parseParagraphs(Document doc, String text, int bodyOffset,int endPosition){
		// don't bother with empty text
		if(text.trim().length() == 0)
			return;
		
		int offs = 0;
		Pattern whitespace = Pattern.compile("^\\s+",Pattern.DOTALL|Pattern.MULTILINE);
		Matcher mt = isDoubleSpace(text)?DSPACE_PATTERN.matcher(text):PATTERN.matcher(text);
		String delim = null;
		while(mt.find()){
			delim = mt.group();
			
			//add whitespace buffer at the end
			int whiteSpaceBufferOffset = 0;
			Matcher wm = whitespace.matcher(delim);
			if(wm.find()){
				whiteSpaceBufferOffset = wm.group().length();
			}
				
			//String txt = text.substring(offs,mt.start());
			//Paragraph pgh = new Paragraph(txt,offs+section.getBodyOffset());
			Paragraph pgh = new Paragraph(doc,offs+bodyOffset,mt.start()+bodyOffset+whiteSpaceBufferOffset);
			if(delim.matches(PARTS))
				pgh.setPart(delim);
			doc.addParagraph(pgh);
			offs = mt.end();
		}
		// mopup 
		//Paragraph pgh = new Paragraph(text.substring(offs),offs+section.getBodyOffset());
		Paragraph pgh = new Paragraph(doc,offs+bodyOffset,endPosition);
		if(delim != null && delim.matches(PARTS))
			pgh.setPart(delim);
		doc.addParagraph(pgh);
	}
	
	
	
	private boolean isDoubleSpace(String text) {
		return !SINGLE_SPACE.matcher(text).find();
	}

	public long getProcessTime() {
		return time;
	}

}
