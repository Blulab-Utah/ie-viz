package edu.pitt.dbmi.nlp.noble.extract.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.pitt.dbmi.nlp.noble.coder.NobleCoder;
import edu.pitt.dbmi.nlp.noble.coder.model.Mention;
import edu.pitt.dbmi.nlp.noble.coder.model.Sentence;
import edu.pitt.dbmi.nlp.noble.terminology.Annotation;
import edu.pitt.dbmi.nlp.noble.terminology.Concept;
import edu.pitt.dbmi.nlp.noble.terminology.TerminologyException;
import edu.pitt.dbmi.nlp.noble.terminology.impl.NobleCoderTerminology;
import edu.pitt.dbmi.nlp.noble.tools.NegEx;
import edu.pitt.dbmi.nlp.noble.tools.TextTools;
import edu.pitt.dbmi.nlp.noble.util.PathHelper;

/**
 * This class reperesents an identified instance of the TemplateItem.
 * It is essentially an "answer" to a template's "question"
 * @author tseytlin
 */
public class ItemInstance implements Comparable {
	private Mention mention;
	private Concept concept;
	private TemplateItem templateItem;
	private ItemInstance feature;
	private Map<ItemInstance,Set<ItemInstance>> attributeValues;
	private Set<ItemInstance> modifiers;
	private Set<Object> values;
	private boolean absent;
	private ItemInstance unit;
	private List<Annotation> annotations;
	private TemplateDocument doc;
	private boolean satisfied;

	//private TemplateDocument document;
	
	/**
	 * initialise instance from template item and matched concept.
	 *
	 * @param temp the temp
	 * @param m the m
	 */
	public ItemInstance(TemplateItem temp,Mention m){
		this.mention = m;
		this.concept = m.getConcept();
		this.templateItem = temp;
		getAnnotations().addAll(m.getAnnotations());
	}
	
	/**
	 * initialise instance from template item and matched concept.
	 *
	 * @param temp the temp
	 * @param c the c
	 */
	public ItemInstance(TemplateItem temp,Concept c){
		this.concept = c;
		this.templateItem = temp;
		Collections.addAll(getAnnotations(),c.getAnnotations());
	}
	
	/**
	 * get template item name.
	 *
	 * @return the name
	 */
	public String getName(){
		return concept.getName();
	}

	
	
	/**
	 * Checks if is absent.
	 *
	 * @return true, if is absent
	 */
	public boolean isAbsent() {
		return absent;
	}

	/**
	 * Sets the absent.
	 *
	 * @param absent the new absent
	 */
	public void setAbsent(boolean absent) {
		this.absent = absent;
	}

	/**
	 * get a list of annotations.
	 *
	 * @return the annotations
	 */
	public List<Annotation> getAnnotations() {
		if(annotations == null)
			annotations = new ArrayList<Annotation>();
		return annotations;
	}
	
	/**
	 * get a type of template item.
	 *
	 * @return the type
	 */
	public String getType(){
		return getTemplateItem().getType();
	}
	
	/**
	 * get template item definition.
	 *
	 * @return the description
	 */
	public String getDescription(){
		String d = concept.getDefinition();
		if(d == null || d.length() == 0)
			d  = getTemplateItem().getDescription();
		return d;
	}

	/**
	 * get a concept object representing this template iterm.
	 *
	 * @return the concept
	 */
	public Concept getConcept() {
		return concept;
	}

	/**
	 * get a concept object representing this template iterm.
	 *
	 * @return the mention
	 */
	public Mention getMention() {
		return mention;
	}
	
	/**
	 * set a concept object representing this template iterm.
	 *
	 * @param concept the new concept
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	/**
	 * get a set of attributes associated with this template item.
	 *
	 * @return the attributes
	 */
	public Set<ItemInstance> getAttributes(){
		return getAttributeValues().keySet();
	}


	/**
	 * get a mapping between attributes and its associated set of values.
	 *
	 * @return the attribute values
	 */
	public Map<ItemInstance, Set<ItemInstance>> getAttributeValues() {
		if(attributeValues == null)
			attributeValues = new HashMap<ItemInstance, Set<ItemInstance>>();
		return attributeValues;
	}
	
	/**
	 * get attribute values associated with a given attribute.
	 *
	 * @param attribute the attribute
	 * @return the attribute values
	 */
	public Set<ItemInstance> getAttributeValues(ItemInstance attribute){
		Set<ItemInstance> list = getAttributeValues().get(attribute);
		return (list != null)?list:Collections.EMPTY_SET;
	}
	
	/**
	 * add a new attribute value to .
	 *
	 * @param attribute the attribute
	 * @param value the value
	 */
	public void addAttributeValue(ItemInstance attribute, ItemInstance value){
		Set<ItemInstance> list = getAttributeValues().get(attribute);
		if(list == null){
			list = new TreeSet<ItemInstance>();
			getAttributeValues().put(attribute,list);
		}
		list.add(value);
	}
	
	/**
	 * add a new attribute value to .
	 *
	 * @param attribute the attribute
	 * @param value the value
	 */
	public void removeAttributeValue(ItemInstance attribute, ItemInstance value){
		Set<ItemInstance> list = getAttributeValues().get(attribute);
		if(list != null){
			list.remove(value);
		}
	}

	/**
	 * get a set of modifiers.
	 *
	 * @return the modifiers
	 */
	public Set<ItemInstance> getModifiers() {
		if(modifiers == null)
			modifiers = new TreeSet<ItemInstance>();
		return modifiers;
	}
	
	/**
	 * add a modifier.
	 *
	 * @param mod the mod
	 */
	public void addModifier(ItemInstance mod){
		getModifiers().add(mod);
	}

	/**
	 * get all instances that were detected as part of this instance.
	 *
	 * @return the component instances
	 */
	public List<ItemInstance> getComponentInstances(){
		List<ItemInstance> items = new ArrayList<ItemInstance>();
		if(getFeature() != null)
			items.add(getFeature());
		for(ItemInstance a: getAttributes())
			items.addAll(getAttributeValues().get(a));
		items.addAll(getModifiers());
		if(getUnit() != null)
			items.add(getUnit());
		
		return items;
	}
	
	/**
	 * get a template item representing this instamce
	 * @return templateItem - that represents it
	 */

	public TemplateItem getTemplateItem() {
		return templateItem;
	}

	/**
	 * Sets the template item.
	 *
	 * @param templateItem the new template item
	 */
	public void setTemplateItem(TemplateItem templateItem) {
		this.templateItem = templateItem;
	}
	
	/**
	 * get value .
	 *
	 * @return the values
	 */
	public Set getValues(){
		if(values == null)
			values = new LinkedHashSet();
		return values;
	}
	
	/**
	 * Gets the feature.
	 *
	 * @return the feature
	 */
	public ItemInstance getFeature() {
		return feature;
	}

	/**
	 * Sets the feature.
	 *
	 * @param feature the new feature
	 */
	public void setFeature(ItemInstance feature) {
		this.feature = feature;
	}

	/**
	 * Gets the unit.
	 *
	 * @return the unit
	 */
	public ItemInstance getUnit() {
		return unit;
	}

	/**
	 * Sets the unit.
	 *
	 * @param unit the new unit
	 */
	public void setUnit(ItemInstance unit) {
		this.unit = unit;
	}

	/**
	 * Adds the value.
	 *
	 * @param value the value
	 */
	public void addValue(Object value) {
		getValues().add(value);
	}

	/**
	 * get the question.
	 *
	 * @return the question
	 */
	public String getQuestion(){
		if(TemplateItem.TYPE_DIAGNOSIS.equals(templateItem.getType()))
			return templateItem.getType();
		return templateItem.getName();
	}
	
	/**
	 * get the answer.
	 *
	 * @return the answer
	 */
	public String getAnswer(){
		return getAnswer(true);
	}
	
	/**
	 * get the answer.
	 *
	 * @param humanReadable the human readable
	 * @return the answer
	 */
	public String getAnswer(boolean humanReadable){
		// if we have a value, then value plus units
		if(!getValues().isEmpty()){
			StringBuffer buf = new StringBuffer();
			for(Object value: getValues()){
				if(value instanceof Double){
					buf.append(TextTools.toString((Double)value)+((unit != null)?" "+unit.getName():""));
				}else{
					buf.append(value.toString());
				}
			}
			return buf.toString();
		}
		
		// if we have an attribute
		if(TemplateItem.DOMAIN_SELF.equals(templateItem.getValueDomain())){
			return getAnswer(this, humanReadable);
		}
		
		// if triggered then return both triggers
		//if(TemplateItem.DOMAIN_TRIGGER.equals(templateItem.getValueDomain())){
		//	return 
		//}
		
		// else return attribute if present
		if(!templateItem.getAttributeValues().isEmpty()){
			StringBuffer str = new StringBuffer();
			boolean includedAttribute = false;
			for(ItemInstance a: getAttributes()){
				for(ItemInstance v : getAttributeValues(a)){
					if(!templateItem.getName().toLowerCase().contains(v.getName().toLowerCase())){
						str.append(getAnswer(v,humanReadable)+" ");
					}else{
						includedAttribute = true;
					}
				}
			}
			if(!includedAttribute && str.length() > 0)
				return str.toString().trim();
		}
		
		// what about modifiers?
		if(!getModifiers().isEmpty()){
			StringBuffer str = new StringBuffer();
			for(ItemInstance v:	getModifiers()){
				str.append(getAnswer(v,humanReadable)+" ");
			}
			if(str.length() > 0)
				return str.toString().trim();
		}
		
		// if this is a not a feature???
		if(!getType().equals(TemplateItem.TYPE_FINDING))
			return getAnswer(this, humanReadable);
		
		
		// return if thing is present or absent
		return isAbsent()?"absent":"present";
	}
	
	/**
	 * Gets the answer.
	 *
	 * @param c the c
	 * @param humanReadable the human readable
	 * @return the answer
	 */
	private String getAnswer(ItemInstance c, boolean humanReadable){
		String name =  c.getName();
		if(!humanReadable){
			Concept cc = c.getConcept();
			String codePattern  = getTemplateItem().getTemplate().getProperties().getProperty(Template.OPTION_PREFERRED_CODE_PATTERN);
			if(codePattern != null){
				for(Object code: cc.getCodes().values()){
					if(code.toString().matches(codePattern)){
						name = name +" ("+code+")";
						break;
					}
				}
			}else{
				name = name +" ("+cc.getCode()+")";
			}
		}
		return name;
	}
	
	/**
	 * extract instances from the document that fit this template.
	 *
	 * @param doc the doc
	 * @throws TerminologyException the terminology exception
	 */
	public void process(TemplateDocument doc) throws TerminologyException{
		this.doc = doc;
		satisfied = true;
		PathHelper paths = getTemplateItem().getTemplate().getPathHelper();
		
		// parse additional text to extract meaning
		List<Mention> r = mention.getSentence() != null?mention.getSentence().getMentions():new ArrayList<Mention>(); //getNeighbors(doc);
		List<Annotation> annotations = new ArrayList<Annotation>();
		
		// check if DOMAIN has triggers and invalidate it if
		// not there
		if(TemplateItem.DOMAIN_TRIGGER.equals(templateItem.getValueDomain())){
			// check if there are mentions in the same sentence 
			// that match any of the values
			annotations.addAll(mention.getAnnotations());
			for(TemplateItem a: templateItem.getAttributeValues().keySet()){
				satisfied = false;
				for(TemplateItem v: templateItem.getAttributeValues(a)){
					for(Mention m: mention.getSentence().getMentions()){
						if(paths.hasAncestor(m.getConcept(),v.getConcept())){
							addAttributeValue(new ItemInstance(a,a.getConcept()),new ItemInstance(v,m));
							annotations.addAll(m.getAnnotations());
							satisfied = true;
						}
					}
				}
			}
		}else if(!getConcept().equals(templateItem.getConcept()) && paths.hasAncestor(getConcept(),templateItem.getConcept()) && TemplateItem.DOMAIN_VALUE.equals(templateItem.getValueDomain())){
			// this instance is really is the value not some feature/attribute BS
			for(Annotation a: getAnnotations()){
				double d = TextTools.parseDecimalValue(a.getText());
				addValue(d == TextTools.NO_VALUE?a.getText():d);
			}
			
		}else{
			// check for negation
			if(mention.isNegated() && ! isAttributeValueDomain()){
				setAbsent(true);
				annotations.addAll(mention.getModifierAnnotations());
			}
			
			// parse concept for its attributes and values
			Mention c = findConcept(templateItem.getFeature());
			if(c != null){
				//Mention m = getMention(c);
				//if(m != null)
				setFeature(new ItemInstance(templateItem.getFeature(),c));
			}
	
			// set attributes and modifiers
			for(TemplateItem attr: templateItem.getAttributes()){
				List<ItemInstance> inst = getMatchingInstances(templateItem.getAttributeValues().get(attr),null);
				if(inst.isEmpty())
					inst = getMatchingInstances(templateItem.getAttributeValues().get(attr),r);
				for(ItemInstance i: inst){
					addAttributeValue(new ItemInstance(attr,attr.getConcept()),i);
					annotations.addAll(i.getAnnotations());
				}
			}
			
			// set modifiers 
			List<ItemInstance> inst = getMatchingInstances(templateItem.getModifiers(),null);
			if(inst.isEmpty())
				inst = getMatchingInstances(templateItem.getModifiers(),r);
			for(ItemInstance i: inst){
				addModifier(i);
				annotations.addAll(i.getAnnotations());
			}
			// set units 
			inst = getMatchingInstances(templateItem.getUnits(),null);
			if(inst.isEmpty())
				inst = getMatchingInstances(templateItem.getUnits(),r);
			for(ItemInstance i: inst){
				setUnit(i);
				annotations.addAll(i.getAnnotations());
			}
			
			// set value 
			inst = getMatchingInstances(templateItem.getValues(),null);
			if(inst.isEmpty())
				inst = getMatchingInstances(templateItem.getValues(),r);
			for(ItemInstance i: inst){
				filterValues(i,annotations);
				for(Annotation a: i.getAnnotations()){
					double d = TextTools.parseDecimalValue(a.getText());
					addValue(d == TextTools.NO_VALUE?a.getText():d);
				}
				annotations.addAll(i.getAnnotations());
			}
			
			// if no attribute/values/modifiers were found, dissatisfy it
			if(annotations.isEmpty() && isAttributeValueDomain())
				satisfied = false;
		}
		// add annotations
		for(Annotation a: annotations){
			if(!getAnnotations().contains(a))
				getAnnotations().add(a);
		}
		//System.out.println(getQuestion()+" instance: "+concept.getName()+" : feature: "+feature+" attr: "+attributeValues+" units: "+unit+" value: "+value);
	}
	
	/**
	 * Checks if is attribute value domain.
	 *
	 * @return true, if is attribute value domain
	 */
	public boolean isAttributeValueDomain(){
		return Arrays.asList(TemplateItem.DOMAIN_ATTRIBUTE,TemplateItem.DOMAIN_VALUE).contains(getTemplateItem().getValueDomain());
	}

	/**
	 * filter numeric values if they fit some criteria.
	 *
	 * @param item the item
	 * @param anat the anat
	 */
	private void filterValues(ItemInstance item, List<Annotation> anat) {
		// remove annotations that happen to be part of other annotations
		// Ex: annotations: 6 and 10 where 10 is part of per 10 hpf
		for(ListIterator<Annotation> it=item.getAnnotations().listIterator();it.hasNext();){
			Annotation a = it.next();
			if(anat.contains(a))
				it.remove();
		}
	}

	/**
	 * get immediate neighbors for a concept.
	 *
	 * @param doc the doc
	 * @return the neighbors
	 */
	private List<Concept> getNeighbors(TemplateDocument doc) {
		List<Concept> r = new ArrayList<Concept>();
		List<Annotation> annotations = getMention().getAnnotations();
		final int WINDOW = 4;
		int st = -1, en = -1;
		for(int i=0;i<doc.getAnnotations().size();i++){
			Annotation a = doc.getAnnotations().get(i);
			if(annotations.contains(a)){
				if(st == -1)
					st = i;
			}else if(st > -1){
				en = i;
				break;
			}
		}
		// now that we have the range
		if(st > -1){
			Map<Integer,Integer> offsets = new LinkedHashMap<Integer,Integer>();
			offsets.put(st-WINDOW,st);
			if(en > -1)
				offsets.put(en,en+WINDOW);
			
			for(int off: offsets.keySet()){
				for(int i=off;i>=0 && i<offsets.get(off) && i< doc.getAnnotations().size();i++){
					Annotation a = doc.getAnnotations().get(i);
					Concept c = a.getConcept();
					if(r.contains(c)){
						for(Concept b: r){
							if(b != null && !Arrays.asList(b.getAnnotations()).contains(a)){
								r.add(c);
								break;
							}
						}
					}else{
						r.add(c);
					}
				}
			}
		}
		return r;
	}
	
	
	/**
	 * get matching instances.
	 *
	 * @param items the items
	 * @param r - optional list of neighbors
	 * @return the matching instances
	 */
	private List<ItemInstance> getMatchingInstances(Collection<TemplateItem> items, List<Mention> r){
		// make sure you don't add one item which is part of another
		List<ItemInstance> result = new ArrayList<ItemInstance>(){
			public boolean add(ItemInstance e) {
				// check if previous content subsumes or is subsumed by new entry
				for(ListIterator<ItemInstance> it = listIterator();it.hasNext();){
					ItemInstance in = it.next();
					// existing entry subsumes new entry
					if(in.getAnnotations().containsAll(e.getAnnotations())){
						return false;
					// new entry subsumes existing entry	
					}else if(e.getAnnotations().containsAll(in.getAnnotations())){
						it.remove();
					}
				}
				return super.add(e);
			}
			
		};
		for(TemplateItem mod: items){
			Mention c = (r == null)?findConcept(mod):findConcept(r,mod);
			if(c != null){
				result.add(new ItemInstance(mod,c)); //getMention(c,r == null)));
			}
		}
		return result;
	}



	/**
	 * Gets the mention.
	 *
	 * @param c the c
	 * @return the mention
	 */
	private Mention getMention(Concept c){
		return getMention(c,true);
	}
	
	/**
	 * Gets the mention.
	 *
	 * @param c the c
	 * @param updateRefs the update refs
	 * @return the mention
	 */
	private Mention getMention(Concept c, boolean updateRefs){
		List<Mention> mentions = Mention.getMentions(c);
		if(mentions.isEmpty())
			return null;
		if(updateRefs){
			for(Mention m: mentions){
				m.setSentence(mention.getSentence());
			}
		}
		int x = 0;
		if(mentions.size() > 1 && doc != null){
			// return the mention that has not been used
			for(Mention m: mentions){
				boolean found = false;
				for(ItemInstance i: doc.getItemInstances(getTemplateItem().getTemplate())){
					if(i.getAnnotations().containsAll(m.getAnnotations())){
						found = true;
						break;
					}
				}
				if(!found)
					return m;
			}
		}
		return mentions.get(x);
	}
	
	/**
	 * merge item instances (if one is more general/specific then other).
	 *
	 * @param o the o
	 */
	public void merge(ItemInstance o) {
		getAnnotations().addAll(o.getAnnotations());
	}

	
	/**
	 * does concept contain other concept.
	 *
	 * @param r the r
	 * @param in the in
	 * @return 	private Concept findConcept(List<Concept> r, TemplateItem in){
	 * 		for(Concept c: r ){
	 * 			if(c != null && (c.equals(in.getConcept()) || in.getPathHelper().hasAncestor(in.getConcept(),c)))
	 * 				return c;
	 * 		}
	 * 		return null;
	 * 	}
	 */
	/**
	 * does concept contain other concept
	 * @param out - 
	 * @param s
	 * @return
	 */
	private Mention findConcept(List<Mention> r, TemplateItem in){
		for(Mention c: r ){
			if(c != null && (c.equals(in.getConcept()) || in.getPathHelper().hasAncestor(in.getConcept(),c.getConcept())))
				return c;
		}
		return null;
	}
	
	/**
	 * does concept contain other concept.
	 *
	 * @param in the in
	 * @return 	private Concept findConcept(TemplateItem in){
	 * 		Concept c = in.getConcept();
	 * 		try{
	 * 			NobleCoderTerminology term = new NobleCoderTerminology();
	 * 			term.setIgnoreSmallWords(false);
	 * 			term.setScoreConcepts(false);
	 * 			term.setSelectBestCandidate(false);
	 * 			term.setIgnoreUsedWords(true);
	 * 			term.setCachingEnabled(false);
	 * 			term.addConcept(c);
	 * 			
	 * 			for(Concept rc: term.search(getConcept().getSearchString())){
	 * 				rc.setTerminology(c.getTerminology());
	 * 				return rc;
	 * 			}
	 * 		}catch(TerminologyException ex){
	 * 			//ex.printStackTrace();
	 * 			
	 * 		}
	 * 		return null;
	 * 	}
	 */
	
	/**
	 * does concept contain other concept
	 * @param out - 
	 * @param s
	 * @return
	 */
	private Mention findConcept(TemplateItem in){
		Concept c = in.getConcept();
		try{
			NobleCoderTerminology term = new NobleCoderTerminology();
			term.setIgnoreSmallWords(false);
			term.setScoreConcepts(false);
			term.setSelectBestCandidate(false);
			term.setIgnoreUsedWords(true);
			//term.setCachingEnabled(false);
			term.addConcept(c);
			Sentence s = mention.getSentence();
			if(s == null)
				return null;
			
			Sentence sent = new Sentence(s.getText());
			sent.setOffset(s.getOffset());
			NobleCoder coder = new NobleCoder(term);
			coder.setAcronymExpansion(false);
			coder.setContextDetection(false);
			
			for(Mention rc: coder.process(sent).getMentions()){
				//rc.setTerminology(c.getTerminology());
				return rc;
			}
		}catch(TerminologyException ex){
			//ex.printStackTrace();
			
		}
		return null;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return concept.getName();
	}
	
	/**
	 * compare to other template item.
	 *
	 * @param o the o
	 * @return the int
	 */
	public int compareTo(Object o) {
		return getConcept().compareTo(((ItemInstance)o).getConcept());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getConcept().hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return getConcept().equals(((ItemInstance)obj).getConcept());
	}

	/**
	 * is this item instance satisfied.
	 *
	 * @return true, if is satisfied
	 */
	public boolean isSatisfied() {
		return satisfied;
	}
	
	
}
