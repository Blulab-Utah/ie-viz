package edu.pitt.dbmi.nlp.noble.coder.processor;

import edu.pitt.dbmi.nlp.noble.coder.model.Document;
import edu.pitt.dbmi.nlp.noble.coder.model.Processor;
import edu.pitt.dbmi.nlp.noble.coder.model.Section;
import edu.pitt.dbmi.nlp.noble.coder.model.Sentence;
import edu.pitt.dbmi.nlp.noble.tools.SentenceDetector;
import edu.pitt.dbmi.nlp.noble.tools.SynopticReportDetector;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SentenceProcessor implements Processor<Document> {
	private static final String PROSE_PATTERN = ".*\\b[a-z]+(\\.|\\?|!)\\s+[A-Z][a-z]+\\b.*";
	private static final String BULLET_PATTERN =  	"^(\\s*(?:[A-Z][0-9]?|[0-9]{1,2}|[\\*\\-])(?:[\\.:\\)]\\s+|\\s{2,}))\\w.*"; //"^\\s*(([A-Z]?[0-9-\\)\\*]{1,2}\\.?:?)\\s+)\\w.*";
	private static final String DIVIDER_PATTERN = "(\\-{5,}|_{5,}|={5,})" ;
	private static final String PROPERTIES_PATTERN = "([A-Z][A-Za-z /]{3,25})(?:\\.{2,}|\\:)(.{2,25})";
	private static final String PROSE_SENTENCE_END = ".+\\s([A-Z]?[a-z]+|\\d+),?\\s*";
	private static final String PROSE_SENTENCE_START = "\\s*([a-z]+|\\d+)\\b.+"; //[A-Z]? 
	private static final String BULLET_SENTENCE_START = "\\s*([A-Z]{2,}|\\d+)\\b.+"; //[A-Z]? 
	private static final String BULLET_SENTENCE_END = ".+\\s([A-Z]{2,}|\\d+),?\\s*";
	private long time;
	
	
	/**
	 * add sentences to a document
	 */
	public Document process(Document doc) {
		time = System.currentTimeMillis();
		
		int offset = 0, strOffset = 0;
		StringBuilder str = new StringBuilder();
		String last = null;
		boolean doubleSpace = true;
		for(String s: doc.getText().split("\n")){
			// skip blank lines for the purpose of merging them
			if(s.trim().length() == 0 && doubleSpace){
				str.append(s+"\n");
				offset += s.length()+1;
				doubleSpace = false;
				continue;
			}
			
			// check if this sentence does not need to be merged
			// with the previous one, lets save it
			if(!mergeLines(str,last,s)){
				// save previous region
				if(str.toString().trim().length() > 0){
					// if multiline buffer, then do prose parsing
					if(str.toString().trim().contains("\n") || str.toString().trim().matches(PROSE_PATTERN)){
						parseSentences(doc, str.toString(), strOffset, Sentence.TYPE_PROSE);
					}else{
						parseSentences(doc, str.toString(), strOffset, Sentence.TYPE_LINE);
					}
					// start the counter again
					str = new StringBuilder();
					strOffset = offset;
				}
			}
			// add this line to the next buffer
			str.append(s+"\n");
			offset += s.length()+1;
			last  = s;
			doubleSpace = true;
		}
		// take care of the last sentence
		if(str.length() > 0){
			if(str.toString().trim().contains("\n") || str.toString().trim().matches(PROSE_PATTERN)){
				parseSentences(doc, str.toString(), strOffset, Sentence.TYPE_PROSE);
			}else{
				parseSentences(doc, str.toString(), strOffset, Sentence.TYPE_LINE);
			}
		}
		time = System.currentTimeMillis() - time;
		doc.getProcessTime().put(getClass().getSimpleName(),time);
		return doc;
	}
	
	private int getWhiteSpacePrefixSize(String text){
		int whitespacePrefix = 0;
		for(char a: text.toCharArray()){
			if(a != ' ' &&  a != '\n')
				break;
			whitespacePrefix++;
		}
		return whitespacePrefix;
	}

	/**
	 * parse sentences for a region of text based on type.
	 *
	 * @param doc the doc
	 * @param text the text
	 * @param offset the offset
	 * @param type the type
	 */
	private void parseSentences(Document doc, String text, int offset, String type){
		// if sentence starts with lots of spaces or bullets 
		// old pattern
		Pattern p = Pattern.compile(BULLET_PATTERN,Pattern.DOTALL|Pattern.MULTILINE);
		boolean bullet = false;
		
		Matcher m = p.matcher(text);
		if(m.matches()){
			String prefix = m.group(1);
			if(prefix.length()>0){
				bullet = true;
				text = text.substring(prefix.length());
				offset = offset + prefix.length();
			}
		}

		// remove leading whitespace
		int whitespacePrefix = getWhiteSpacePrefixSize(text);
		if(whitespacePrefix > 0){
			text = text.substring(whitespacePrefix);
			offset = offset + whitespacePrefix;
		}

		// remove following spaces
		text = text.trim();

		// start adding sentences
		List<Sentence> sentences = new ArrayList<Sentence>();
		if(Sentence.TYPE_PROSE.equals(type)){
			sentences = SentenceDetector.getSentences(text,offset);
			if(bullet && !sentences.isEmpty()){
				sentences.get(0).setSentenceType(Sentence.TYPE_BULLET);
			}
			
		}else{
			Sentence s = new Sentence(text,offset,Sentence.TYPE_LINE);
			if(bullet)
				s.setSentenceType(Sentence.TYPE_BULLET);
			if(isDivider(s))
				s.setSentenceType(Sentence.TYPE_DIVIDER);
			
			parseProperties(doc,text);
			if(SynopticReportDetector.detect(text)){
				s.setSentenceType(Sentence.TYPE_WORKSHEET);
			}
			sentences.add(s);
		}
			
		// add to section
		//should it really be here???? Why not?
		if(!sentences.isEmpty()){
			Section sec = doc.getSection(sentences.get(0));
			if(sec != null){
				Sentence s = sentences.get(0);
				if(s.contains(sec.getTitleSpan())){
					//OK, this sentence contains header, do we need to 
					// parse it fruther?
					int en = sec.getTitleSpan().getEndPosition()-offset;
					String first = s.getText().substring(0,en);
					String rest = s.getText().substring(en);
					if(rest.trim().length() > 0){
						// there is more after header, need to break it in two
						sentences.remove(s);
						sentences.add(0,new Sentence(rest,offset+en,s.getSentenceType()));
						sentences.add(0,new Sentence(first,offset,Sentence.TYPE_HEADER));
						
					}else{
						// just set this sentence as header
						s.setSentenceType(Sentence.TYPE_HEADER);
					}
				}
				//sec.addSentences(sentences);
			}
		}
		

		
		
		// add sentences to document
		doc.addSentences(sentences);
	}
	

	private boolean isDivider(Sentence s) {
		return s.getText().trim().matches(DIVIDER_PATTERN);
	}


	/**
	 * parse properties in a document.
	 *
	 * @param doc the doc
	 * @param text the text
	 */
	private void parseProperties(Document doc, String text){
		Pattern p = Pattern.compile(PROPERTIES_PATTERN);
		Matcher m = p.matcher(text);
		while(m.find()){
			doc.getProperties().put(m.group(1).trim(),m.group(2).trim());
		}
	}
	
	/**
	 * Merge lines.
	 *
	 * @param last the last
	 * @param s the s
	 * @return true, if successful
	 */
	private boolean mergeLines(StringBuilder buf, String last, String s) {
		if(last == null)
			return false;
		// if previous item is worksheet ..
		if(!last.matches(BULLET_PATTERN) && SynopticReportDetector.detect(last))
			return false;
		
		// if current line is a bullet, then it is irrelevant of what the last line is
		if(s.matches(BULLET_PATTERN))
			return false;
		
		// if previous sentence ends with a lower case word or digit or comma
		// and next one starts with normal non-upper case word
		if(last.matches(PROSE_SENTENCE_END) && s.matches(PROSE_SENTENCE_START)){
			return true;
		}
		// if last line is a bullet and the following is upper case continuation
		if(Pattern.compile(BULLET_PATTERN, Pattern.DOTALL|Pattern.MULTILINE).matcher(buf).matches() && last.matches(BULLET_SENTENCE_END) && s.matches(BULLET_SENTENCE_START)){
			return true;
		}
		
		return false;
	}

	

	public long getProcessTime() {
		return time;
	}

}
