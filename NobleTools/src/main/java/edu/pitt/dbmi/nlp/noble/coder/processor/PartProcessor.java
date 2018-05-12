package edu.pitt.dbmi.nlp.noble.coder.processor;

import edu.pitt.dbmi.nlp.noble.coder.model.Processor;
import edu.pitt.dbmi.nlp.noble.coder.model.Section;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Class PartProcessor.
 */
public class PartProcessor implements Processor<Section> {
	public static final String PART_PATTERN = "PARTS?\\s+\\d+(\\s+AND\\s+\\d+)?:"; 
	private long time;
	
	/**
	 * identify parts in a section and attach them to this section.
	 *
	 * @param text the text
	 * @return the section
	 */
	public Section process(String text) {
		Section section = new Section();
		section.setText(text);
		section.setBody(text);
		return process(section);
	}
	
	/**
	 * identify parts in a section and attach them to this section.
	 *
	 * @param section the section
	 * @return the section
	 */
	public Section process(Section section)  {
		time = System.currentTimeMillis();
		Pattern pt = Pattern.compile(PART_PATTERN,Pattern.MULTILINE|Pattern.DOTALL);
		Matcher mt = pt.matcher(section.getBody());
		List<Section> parts = new ArrayList<Section>();
		String text = section.getBody();
		Section part = null;
		while(mt.find()){
			// finish previous part
			if(part != null){
				part.setText(text.substring(part.getOffset(),mt.start()));
				part.setBody(text.substring(part.getBodyOffset(),mt.start()));
			}
			// init new text section
			part = new Section();
			part.setTitle(mt.group());
			part.setTitleOffset(mt.start());
			part.setBodyOffset(mt.end());
			parts.add(part);
		}
		// finish the last part
		if(part != null){
			part.setText(text.substring(part.getOffset()));
			part.setBody(text.substring(part.getBodyOffset()));
		}
		// reset offsets
		for(Section p: parts){
			p.updateOffset(section.getOffset());
			/*for(Sentence s: section.getSentences()){
				if(p.contains(s))
					p.addSentence(s);
			}*/
		}
		section.setSections(parts);
		time = System.currentTimeMillis() - time;
		return section;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.coder.model.Processor#getProcessTime()
	 */
	public long getProcessTime() {
		return time;
	}
}
