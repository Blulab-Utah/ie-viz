package edu.pitt.dbmi.nlp.noble.coder.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.pitt.dbmi.nlp.noble.coder.model.*;
import edu.pitt.dbmi.nlp.noble.terminology.TerminologyException;

public class SectionProcessor implements Processor<Document> {
	private long time;
	private Pattern SECTION_PATTERN = Pattern.compile("^([A-Z][A-Z/\\- ]{5,40}:)\\s+(.*)",Pattern.DOTALL|Pattern.MULTILINE);
	
	public Document process(Document doc) throws TerminologyException {
		time = System.currentTimeMillis();
		
		// lets do sectioning first
		Pattern pt = SECTION_PATTERN;
		doc.setSections(section(doc,0, pt, pt.matcher(doc.getText()), new ArrayList<Section>()));
		
		time = System.currentTimeMillis() -time;
		doc.getProcessTime().put(getClass().getSimpleName(),time);
		return doc;
	}
	
	/**
	 * Section.
	 *
	 * @param doc the doc
	 * @param offs the offs
	 * @param pt the pt
	 * @param mt the mt
	 * @param list the list
	 * @return the list
	 */
	private List<Section> section(Document document,int offs,Pattern pt, Matcher mt,List<Section> list){
		String doc = document.getText();
		while(mt.find()){
			int st = offs+mt.start();
			int en = offs+mt.end();
			int bst = offs+mt.start(2);
			String text = mt.group();
			String name = mt.group(1);
			String body = mt.group(2);
			
			// because we have a greedy pattern, look in body for sub-patterns
			Matcher m = pt.matcher(body);
			if(m.find()){
				en = bst+m.start();
				text = doc.substring(st,en);
				body = doc.substring(bst,en);
			}
			
			// create section object with text
			/*Section s = new Section();
			s.setText(text);
			s.setTitle(name);
			s.setTitleOffset(st);
			s.setBody(body);
			s.setBodyOffset(bst);*/
			
			Section s = new Section(document,st,bst,st+text.length());
			s.setTitle(name);
			
			list.add(s);
			
			// reset matcher
			m.reset();
			
			// recurse into body
			section(document,bst,pt,m,list);
		}
		return list;
	}
	
	public long getProcessTime() {
		return time;
	}

	
}
