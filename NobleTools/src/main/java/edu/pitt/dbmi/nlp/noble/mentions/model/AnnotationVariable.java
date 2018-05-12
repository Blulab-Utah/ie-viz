package edu.pitt.dbmi.nlp.noble.mentions.model;

import edu.pitt.dbmi.nlp.noble.coder.model.Modifier;
import edu.pitt.dbmi.nlp.noble.coder.model.Section;
import edu.pitt.dbmi.nlp.noble.ontology.*;
import edu.pitt.dbmi.nlp.noble.terminology.Annotation;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class represents a container for Annotation class inside DomainOntology.owl
 * @author tseytlin
 */
public class AnnotationVariable extends Instance {
	private Instance anchor;
	private String annotationType;

	/**
	 * create a new annotation variable wih anchor
	 * @param annotation class
	 * @param anchor instance
	 */
	public AnnotationVariable(IClass annotation,Instance anchor) {
		super(anchor.getDomainOntology(),anchor.getMention());
		this.anchor = anchor;
		this.cls = annotation;
		annotationType = DomainOntology.ANNOTATION_MENTION;
	}

	
	public Instance getAnchor() {
		return anchor;
	}


	/**
	 * get annotation type of this variable
	 * @return annotation type
	 */
	public String getAnnotationType() {
		return annotationType;
	}

	/**
	 * set annotation type of this variable
	 * @param annotationType - annotation type
	 */
	public void setAnnotationType(String annotationType) {
		this.annotationType = annotationType;
	}


	/**
	 * get an instance that represents this annotation variable
	 * @return an instance that represents this variable
	 */
	public IInstance getInstance(){
		if(instance == null){
			// create an instance
			IOntology ont = cls.getOntology();
			instance = domainOntology.createInstance(cls);
			
			// add anchor
			instance.addPropertyValue(ont.getProperty(DomainOntology.HAS_ANCHOR),anchor.getInstance());
			addModifierInstance(DomainOntology.HAS_ANCHOR,anchor);
			
			// add type
			IClass annotationCls = ont.getClass(getAnnotationType());
			instance.addPropertyValue(ont.getProperty(DomainOntology.HAS_ANNOTATION_TYPE),domainOntology.getDefaultInstance(annotationCls));
			addModifierInstance(DomainOntology.HAS_ANNOTATION_TYPE,new Instance(domainOntology,Modifier.getModifier(DomainOntology.HAS_ANNOTATION_TYPE,getAnnotationType())));
			
			// add section if available, if not use default
			Instance sectionInstance = null;
			Section section = mention.getSentence().getSection();
			if(section != null && section.getHeader() != null){
				sectionInstance = new Instance(domainOntology,section.getHeader());
			}else{
				// add generic upper level section if section is not available
				sectionInstance = new Instance(domainOntology,
						Modifier.getModifier(DomainOntology.HAS_SECTION,DomainOntology.DOCUMENT_SECTION));
			}
			instance.addPropertyValue(ont.getProperty(DomainOntology.HAS_SECTION), sectionInstance.getInstance());
			addModifierInstance(DomainOntology.HAS_SECTION,sectionInstance);
			
			// upgrade numbers
			upgradeNumericModifiers();
			
			
			// instantiate available modifiers
			List<Instance> modifierInstances = createModifierInstanceList();
		
			Set<IProperty> props = domainOntology.getProperties(cls);
			for(Instance modifierInstance: modifierInstances){
				for(IProperty prop : domainOntology.getProperties(modifierInstance.getModifier())){
					if(props.contains(prop) && isPropertyRangeSatisfied(prop,  modifierInstance)){
						addModifierInstance(prop.getName(),modifierInstance);
					}
				}
			}
			
			
			// now just add span
			instance.setPropertyValue(ont.getProperty(DomainOntology.HAS_SPAN),getInstanceSpan());
			instance.setPropertyValue(ont.getProperty(DomainOntology.HAS_ANNOTATION_TEXT),getText());


		}
		return instance;
	}
	
	
	/**
	 * get a set of text annotations associated with this instance
	 * @return set of annotations
	 */
	public Set<Annotation> getAnnotations() {
		if(annotations == null){
			annotations = new TreeSet<Annotation>();
			if(getMention() != null){
				annotations.addAll(getMention().getAnnotations());
				//annotations.addAll(getMention().getModifierAnnotations());
			}
			for(String type: getModifierInstances().keySet()){
				// skip annotation for section 
				if(DomainOntology.HAS_SECTION.equals(type)) {
					continue;
				}
				for(Instance modifier:getModifierInstances().get(type)){
					annotations.addAll(modifier.getAnnotations());
				}
			}
		}
		
		return annotations;
	}
	
	/**
	 * is the current annotation variable satisfied given its linguistic and semantic properties?
	 * @return is the variable satisfied
	 */
	public boolean isSatisfied() {
		return getConceptClass().getEquivalentRestrictions().evaluate(getInstance());
	}

	
	/**
	 * string representation of this variable
	 * @return string that is human readable
	 */
	public String toString(){
		StringBuilder str = new StringBuilder();
		str.append(getConceptClass()+"\n");
		for(String type: getModifierInstances().keySet()){
			for(Instance modifier:getModifierInstances().get(type)){
				str.append("\t"+type+": "+modifier+"\n");
			}
		}
		str.append("\thasText: "+getAnnotations());
		return str.toString();
	}


	public void findReasonForFail(){
		for(Object o: getConceptClass().getEquivalentRestrictions()){
			if(o instanceof  IRestriction){
				IRestriction r = (IRestriction) o;
				for(String prop: getModifierInstances().keySet()){
					if(prop.equals(r.getProperty().getName())){
						for(Instance i: getModifierInstances().get(prop)) {
							if(!r.getParameter().evaluate(i.getConceptClass())){
								i.setReasonForFail(true);
							}
						}
					}
				}
			}
		}
	}
	public Set<String> findMissingDefinedProperties(){
		Set<String> props = new LinkedHashSet<String>();
		Set<String> foundProps = getModifierInstances().keySet();
		for(Object o: getConceptClass().getEquivalentRestrictions()){
			if(o instanceof  IRestriction){
				IRestriction r = (IRestriction) o;
				String p = r.getProperty().getName();
				if(!foundProps.contains(p)){
					props.add(p);
				}
			}
		}
		return props;
	}
}
