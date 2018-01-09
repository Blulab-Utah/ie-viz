package edu.pitt.dbmi.nlp.noble.coder.model;

import com.sun.org.apache.xpath.internal.operations.Mod;
import edu.pitt.dbmi.nlp.noble.terminology.*;
import edu.pitt.dbmi.nlp.noble.terminology.impl.NobleCoderTerminology;
import edu.pitt.dbmi.nlp.noble.tools.ConText;
import edu.pitt.dbmi.nlp.noble.tools.TextTools;

import java.util.*;

/**
 * This object represents a concept mention in text.
 *
 * @author tseytlin
 */
public class Mention implements Spannable, Comparable<Mention> {
	private Concept concept;
	private List<Annotation> annotations;
	private Sentence sentence;
	private Map<String,List<Modifier>> modifiers;
	
	
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
	
	/**
	 * Gets the annotations.
	 *
	 * @return the annotations
	 */
	public List<Annotation> getAnnotations() {
		if(annotations == null)
			annotations = new ArrayList<Annotation>();
		return annotations;
	}
	
	/**
	 * Sets the annotations.
	 *
	 * @param annotations the new annotations
	 */
	public void setAnnotations(List<Annotation> annotations) {
		this.annotations = annotations;
	}
	
	/**
	 * Gets the sentence.
	 *
	 * @return the sentence
	 */
	public Sentence getSentence() {
		return sentence;
	}
	
	/**
	 * Sets the sentence.
	 *
	 * @param sentence the new sentence
	 */
	public void setSentence(Sentence sentence) {
		this.sentence = sentence;
		for(Annotation a: getAnnotations()){
			if(!a.isOffsetUpdated())
				a.updateOffset(sentence.getOffset());
		}
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.coder.model.Spannable#getText()
	 */
	public String getText(){
		StringBuilder b = new StringBuilder();
		
		for(Annotation a: getAnnotations()){
			b.append(" "+a.getText());
		}
		
		return b.toString().trim();
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return concept.getName();
	}
	
	/**
	 * Gets the code.
	 *
	 * @return the code
	 */
	public String getCode() {
		return concept.getCode();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return getText();
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.coder.model.Spannable#getStartPosition()
	 */
	public int getStartPosition() {
		return !getAnnotations().isEmpty()?getAnnotations().get(0).getStartPosition():0;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.coder.model.Spannable#getEndPosition()
	 */
	public int getEndPosition() {
		return !getAnnotations().isEmpty()?getAnnotations().get(getAnnotations().size()-1).getEndPosition():0;
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

	/**
	 * get length of this mention span
	 * @return length of the span
	 */
	public int getLength() {
		return getText().length();
	}

	/**
	 * compare to other mentions.
	 *
	 * @param o the o
	 * @return the int
	 */
	public int compareTo(Mention o) {
		int n = getStartPosition() - o.getStartPosition();
		if(n == 0){
			n = getEndPosition() - o.getEndPosition();
			// if still equal, just arbitrary assign something
			// this is done, so that mentions would not be dropped in
			// some sorted set, since this method is often used to compare them
			//if(n == 0)
			//	n = -1;
		}
		return n;
	}
	
	/**
	 * convert a found concept to a set of mentions.
	 *
	 * @param c the c
	 * @return the mentions
	 */
	public static List<Mention> getMentions(Concept c) {
		return getMentions(c,Arrays.asList(c.getAnnotations()));
	}
	
	/**
	 * convert a found concept to a set of mentions.
	 *
	 * @param c the c
	 * @param annotations the annotations
	 * @return the mentions
	 */
	public static List<Mention> getMentions(Concept c, List<Annotation> annotations) {
		List<Mention> list = new ArrayList<Mention>();
		
		// if we could not get annotations, is it really worth it getting this concept?
		if(annotations.isEmpty())
			return Collections.EMPTY_LIST;
		
		
		// lets make a short cut, if we have the same number of annotations as words in match term, then we are good
		if(!(c.getMatchedTerms() != null && c.getMatchedTerms().length == 1 && annotations.size() == TextTools.getWords(c.getMatchedTerm()).size())){
			List<String> words = TextTools.getWords(c.getSearchString());
			// go over every word in a sentence
			for(String term: c.getMatchedTerms()){
				List<String> twords  = TextTools.getWords(term);
				int offs = 0;
				for(int i=0;i<words.size();i++){
					// if term words contain that word, then
					// look at the sublist that includes it and + allowed gap
					// if this sublist contains ALL term words, then we have contigous match
					// FROM MELISSA: the word window span is actually good, just need to do gap analysis after to 
					// make sure that no gap exceeds the word gap 
					
					if(twords.contains(words.get(i)) && c.getTerminology() != null && c.getTerminology() instanceof NobleCoderTerminology){
						int n = i+((((NobleCoderTerminology)c.getTerminology()).getMaximumWordGap()+1)*(twords.size()-1))+1;
						if(n >= words.size())
							n = words.size();

						if(words.subList(i,n).containsAll(twords)){
							int st = c.getSearchString().indexOf(words.get(i),offs);
							int en = c.getSearchString().indexOf(words.get(n-1),offs)+words.get(n-1).length();
							
							List<Annotation> alist = new ArrayList<Annotation>();
							for(Annotation a: annotations){
								if(st <= a.getStartPosition() && a.getEndPosition() <= en){
									alist.add(a);
								}
							}
							
							// create a mention for a contigus span of text
							if(!alist.isEmpty()){
								Mention m = new Mention();
								m.setConcept(c);
								m.setAnnotations(alist);
								list.add(m);
							}
						}
					}
					
					// keep track of offset
					offs += words.get(i).length()+1;
				}
			}
		}
		
		// if our prior step fail, do default action and simply add all annotation to a single mention
		if(list.isEmpty()){
			Mention m = new Mention();
			m.setConcept(c);
			m.setAnnotations(annotations);
			list.add(m);
		}
		
		return list;
	}


	/**
	 * modifier types used for this mention
	 *
	 * @return the modifier types
	 */
	public Set<String> getModifierTypes(){
		return getModifierMap().keySet();
	}

	/**
	 * linguistic modifier types used for this mention
	 *
	 * @return the modifier types
	 */
	public static List<String> getLinguisticModifierTypes(){
		return ConText.MODIFIER_TYPES;
	}


	/**
	 * get a mapping of linguistic context found for this mention.
	 *
	 * @return the modifiers
	 */
	public Map<String,List<Modifier>> getModifierMap(){
		if(modifiers == null){
			modifiers = new LinkedHashMap<String, List<Modifier>>();
		}
		return modifiers;
	}

	/**
	 * get a mapping of linguistic context found for this mention.
	 *
	 * @return the modifiers
	 */
	public List<Modifier> getModifiers(){
		List<Modifier> list = new ArrayList<Modifier>();
		for(List<Modifier> l : getModifierMap().values())
			list.addAll(l);
		return list;
	}
	
	/**
	 * Gets the modifier annotations.
	 *
	 * @return the modifier annotations
	 */
	public List<Annotation> getModifierAnnotations(){
		List<Annotation> list = new ArrayList<Annotation>();
		for(Modifier m: getModifiers()){
			list.addAll(m.getAnnotations());
			list.addAll(m.getQualifierAnnotations());
		}
		return list;
	}

	
	/**
	 * Gets the modifier.
	 *
	 * @param type the type
	 * @return the modifier
	 */
	public List<Modifier> getModifiers(String type){
		if(getModifierMap().containsKey(type)){
			return getModifierMap().get(type);
		}
		return Collections.EMPTY_LIST;
	}
	
	/**
	 * Gets the modifier value of the first mention of that type.
	 *
	 * @param type the type
	 * @return the modifier value
	 */
	public String getModifierValue(String type){
		for(Modifier m: getModifiers(type)){
			return m.getValue();
		}
		return null;
	}

	/**
	 * Gets the modifier value.
	 *
	 * @param type the type
	 * @return the modifier value
	 */
	public List<String> getModifierValues(String type){
		List<String> values = new ArrayList<String>();
		for(Modifier m: getModifiers(type)){
			values.add(m.getValue());
		}
		return values;
	}
	
	/**
	 * Checks if is negated.
	 *
	 * @return true, if is negated
	 */
	public boolean isNegated(){
		return ConText.MODIFIER_VALUE_NEGATIVE.equals(getModifierValue(ConText.MODIFIER_TYPE_POLARITY));
	}
	
	/**
	 * Checks if is hedged.
	 *
	 * @return true, if is hedged
	 */
	public boolean isHedged(){
		return ConText.MODIFIER_VALUE_HEDGED.equals(getModifierValue(ConText.MODIFIER_TYPE_MODALITY));
	}
	
	/**
	 * Checks if is historical.
	 *
	 * @return true, if is historical
	 */
	public boolean isHistorical(){
		return  ConText.MODIFIER_VALUE_HISTORICAL.equals(getModifierValue(ConText.MODIFIER_TYPE_TEMPORALITY));
	}
	
	/**
	 * Checks if is family member.
	 *
	 * @return true, if is family member
	 */
	public boolean isFamilyMember(){
		return ConText.MODIFIER_VALUE_FAMILY_MEMBER.equals(getModifierValue(ConText.MODIFIER_TYPE_EXPERIENCER));
	}

	/**
	 * add modifier to this mention.
	 *
	 * @param m the m
	 */
	public void addModifier(Modifier m) {
		// get existing values
		List<Modifier> list = getModifierMap().get(m.getType());
		// if nothing there, then just add
		if(list == null){
			list = new ArrayList<Modifier>();
		}else{
			// augment or replace the existing list
			for(ListIterator<Modifier> it=list.listIterator();it.hasNext();){
				Modifier oldM = it.next();
				// replace default modifier, with non default modifier
				if(oldM.isDefaultValue() && !m.isDefaultValue()){
					it.remove(); // remove the old (default) modifier
					continue;
				}
				// if old modifier has no mention, but new one does, replace
				if(oldM.getMention() == null && m.getMention() != null){
					it.remove(); // remove the old (default) modifier
					continue;
				}

				// if both modifiers have mentions, which should be most of the time
				if(m.getMention() != null && oldM.getMention() != null ){
					Mention nM = m.getMention();
					Mention oM = oldM.getMention();
					// if a new modifier contains an old modifier and is larger then remove old
					if(nM.contains(oM) && nM.getLength() > oM.getLength()){
						it.remove();
					// if an older modifier contains a new modifier
					}else if (oM.contains(nM) && oM.getLength() > nM.getLength()){
						// old modifier is better as it has a larger span,
						return;
					// don't add identical mentions please
					}else if(nM.equals(oM)) {
						return;
						// if both are linguistic modifiers, keep the nearest one
						//}else if(ConText.isTypeOf(nM,ConText.LINGUISTIC_MODIFIER) && ConText.isTypeOf(oM,ConText.LINGUISTIC_MODIFIER)){
					//TODO: decide if this is what we want and possibly refactor
					}else{
						int d1 = Text.getOffsetDistance(this,nM);
						int d2 = Text.getOffsetDistance(this,oM);
						if(d1 < d2){
							it.remove(); // remove old mention
						}else{
							return;     // keep old mention
						}
						
					}
				}
			}
		}
		// add/re-add the list back to the map
		list.add(m);
		getModifierMap().put(m.getType(),list);
	}

	/**
	 * add linguistic mofifier of this mention.
	 *
	 * @param list the list
	 */
	public void addModifiers(List<Modifier> list) {
		for(Modifier m: list){
			addModifier(m);
		}
	}

	/**
	 * add linguistic mofifier of this mention.
	 *
	 * @param map of modifier lists
	 */
	public void addModifiers(Map<String,List<Modifier>> map) {
		for(String type: map.keySet()){
			if(getModifierMap().containsKey(type)){
				addModifiers(map.get(type));
			}else {
				getModifierMap().put(type, map.get(type));
			}
		}
	}

	/**
	 * is mention equal to another mention?
	 * It is if they point to the same concept and have the same set of text annotations
	 * @param m - mention to compare to
	 * @return true or false
	 */
	public boolean equals(Mention m){
		return getConcept().equals(m.getConcept()) && getAnnotations().containsAll(m.getAnnotations());
	}

	/**
	 * generate hash code from concept code and annotations
	 * @return true or false
	 */
	public int hashCode() {
		return (getCode()+getAnnotations()).hashCode();
	}
}
