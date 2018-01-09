package edu.pitt.dbmi.nlp.noble.coder.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.pitt.dbmi.nlp.noble.terminology.Annotation;
import edu.pitt.dbmi.nlp.noble.terminology.TerminologyError;
import edu.pitt.dbmi.nlp.noble.tools.ConText;

/**
 * The Class Modifier.
 */
public class Modifier {
	private String type,value;
	private Mention mention;
	private boolean defaultValue;
	
	/**
	 * Instantiates a new modifier.
	 *
	 * @param type the type
	 * @param value the value
	 */
	public Modifier(String type, String value){
		this.type = type;
		this.value = value;
	}
	
	/**
	 * get a list of modifiers from a given ConText mention.
	 *
	 * @param m the m
	 * @return the modifiers
	 */
	public static List<Modifier> getModifiers(Mention m){
		List<Modifier> list = new ArrayList<Modifier>();
		for(String type : ConText.getModifierTypes(m.getConcept())){
			String value = ConText.getModifierValue(type,m);
			Modifier mod = new Modifier(type,value);
			mod.setMention(m);
			list.add(mod);
		}
		// having an empty list is not OK
		if(list.isEmpty())
			throw new TerminologyError("failed to get a list of modifiers from mention: "+m.getName());
		
		return list;
	}
	
	/**
	 * get a list of modifiers from a given ConText mention.
	 *
	 * @param type the type
	 * @param value the value
	 * @param m the m
	 * @return the modifier
	 */
	public static Modifier getModifier(String type, String value, Mention m){
		Modifier mod = new Modifier(type,value);
		mod.setMention(m);
		return mod;
	}
	
	
	/**
	 * Checks if is default value.
	 *
	 * @return true, if is default value
	 */
	public boolean isDefaultValue(){
		return defaultValue; 
	}
	
	/**
	 * Sets the default value.
	 *
	 * @param defaultValue the new default value
	 */
	public void setDefaultValue(boolean defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * get modifier with type/value.
	 *
	 * @param type the type
	 * @param value the value
	 * @return the modifier
	 */
	public static Modifier getModifier(String type, String value){
		return new Modifier(type,value);
	}
	
	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Sets the type.
	 *
	 * @param type the new type
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * Sets the value.
	 *
	 * @param value the new value
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * Gets the mention.
	 *
	 * @return the mention
	 */
	public Mention getMention() {
		return mention;
	}
	
	/**
	 * Sets the mention.
	 *
	 * @param mention the new mention
	 */
	public void setMention(Mention mention) {
		this.mention = mention;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return value;
	}
	
	/**
	 * get annotations for this modifier.
	 *
	 * @return the annotations
	 */
	public List<Annotation> getAnnotations(){
		if(mention != null)
			return mention.getAnnotations();
		return Collections.EMPTY_LIST;
	}
	
	/**
	 * get annotations for this modifier.
	 *
	 * @return the annotations
	 */
	public List<Annotation> getQualifierAnnotations(){
		if(mention != null)
			return mention.getModifierAnnotations();
		return Collections.EMPTY_LIST;
	}

	public int hashCode() {
		return getInfo().hashCode();
	}

	public boolean equals(Modifier m) {
		if(mention != null && m.getMention() != null)
			return mention.equals(m.getMention());
		return getInfo().equals(m.getInfo());
	}

	private String getInfo(){
		return type+": "+value+" "+mention;
	}

	/**
	 * does this modifier has a mention associated with it
	 * @return true or false
	 */
	public boolean hasMention() {
		return mention != null;
	}
}
