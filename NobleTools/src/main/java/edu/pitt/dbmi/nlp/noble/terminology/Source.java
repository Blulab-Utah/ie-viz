package edu.pitt.dbmi.nlp.noble.terminology;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements Source.
 *
 * @author tseytlin
 */
public class Source implements Serializable, Comparable {
	private static final long serialVersionUID = 1234567890L;
	private String code,name,description;
	private transient String version;
	public static final Source URI = getSource("URI");
	public static final Pattern CODE_FROM_SOURCE_PATTERN = Pattern.compile("(.*)\\s*\\[(.*)\\]");
	
	/**
	 * Create empty source.
	 */
	public Source(){}
	
	
	/**
	 * Create empty source.
	 *
	 * @param name the name
	 */
	public Source(String name){
		this(name,"",name);
	}
	
	
	/**
	 * Constract source w all values filled in.
	 *
	 * @param name the name
	 * @param description the description
	 * @param code the code
	 */
	public Source(String name,String description, String code){
		this.name = name;
		this.description = description;
		this.code = code;
	}
	
	/**
	 * Gets the code.
	 *
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Sets the code.
	 *
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name == null?code:name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * String representation.
	 *
	 * @return the string
	 */
	public String toString(){
		return code;
	}
	
	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	public String getVersion() {
		if(version == null && description != null){
			Matcher m = Pattern.compile(".*,\\s*(.{1,15})").matcher(description);
			if(m.matches())
				return m.group(1);
		}
		return version;
	}


	/**
	 * Sets the version.
	 *
	 * @param version the new version
	 */
	public void setVersion(String version) {
		this.version = version;
	}


	/**
	 * compare 2 sources.
	 *
	 * @param obj the obj
	 * @return the int
	 */
	public int compareTo(Object obj){
		if(obj instanceof Source){
			return getCode().compareTo(((Source)obj).getCode());
		}
		return 0;
	}
	
	/**
	 * get instance of definition class.
	 *
	 * @param text the text
	 * @return the source
	 */
	public static Source getSource(String text){
		return new Source(text);
	}
	
	
	/**
	 * get instance of definition class.
	 *
	 * @param text the text
	 * @return the sources
	 */
	public static Source [] getSources(String [] text){
		if(text == null)
			return new Source [0];
		Source [] d = new Source [text.length];
		for(int i=0;i<d.length;i++)
			d[i] = new Source(text[i]);
		return d;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return toString().hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return toString().equals(obj.toString());
	}


	/**
	 * convert to DOM element.
	 *
	 * @param doc the doc
	 * @return the element
	 * @throws TerminologyException the terminology exception
	 */
	public Element toElement(Document doc) throws TerminologyException {
		Element root = doc.createElement("Source");
		root.setAttribute("name",getName());
		root.setAttribute("code",getCode());
		root.setAttribute("version",getVersion());
		if(getDescription() != null)
			root.setTextContent(getDescription());
		return root;
	}
	
	/**
	 * convert from DOM element.
	 *
	 * @param element the element
	 * @throws TerminologyException the terminology exception
	 */
	public void fromElement(Element element) throws TerminologyException{
		if(element.getTagName().equals("Source")){
			setName(element.getAttribute("name"));
			setCode(element.getAttribute("code"));
			setVersion(element.getAttribute("version"));
			String text = element.getTextContent().trim();
			if(text.length() > 0)
				setDescription(text);
		}
	}
}
