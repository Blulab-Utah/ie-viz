package edu.pitt.dbmi.nlp.noble.mentions.model;

import edu.pitt.dbmi.nlp.noble.coder.model.Mention;
import edu.pitt.dbmi.nlp.noble.coder.model.Modifier;
import edu.pitt.dbmi.nlp.noble.ontology.*;
import edu.pitt.dbmi.nlp.noble.terminology.Annotation;
import edu.pitt.dbmi.nlp.noble.tools.ConText;
import edu.pitt.dbmi.nlp.noble.tools.TextTools;

import java.util.*;

/**
 * a domain instance is a wrapper for any instance and mention found in text 
 * @author tseytlin
 *
 */
public class Instance {
	protected DomainOntology domainOntology;
	protected Mention mention;
	protected Modifier modifier;
	protected IClass cls;
	protected IInstance instance;
	protected Set<Modifier> modifiers;
	protected Map<String,Set<Instance>> modifierInstances;
	protected Set<Annotation> annotations;
	protected List<Mention> compoundComponents;
	protected boolean reasonForFail;
	
	/**
	 * initilize an instance
	 * @param ontology of the domain
	 * @param m - mention object
	 */
	
	public Instance(DomainOntology ontology,Mention m){
		setDomainOntology(ontology);
		setMention(m);
	}
	
	/**
	 * initilize an instance
	 * @param ontology object
	 * @param m - modifier object
	 */
	
	public Instance(DomainOntology ontology,Modifier m){
		setDomainOntology(ontology);
		setModifier(m);
	}
	
	/**
	 * initilize an instance
	 * @param ontology - domain ontology
	 * @param m  - mention object
	 * @param inst - ontology instance
	 */
	
	public Instance(DomainOntology ontology,Mention m, IInstance inst){
		setDomainOntology(ontology);
		setMention(m);
		cls = inst.getDirectTypes()[0];
		instance = inst;
	}
	
	
	public DomainOntology getDomainOntology() {
		return domainOntology;
	}

	public void setDomainOntology(DomainOntology domainOntology) {
		this.domainOntology = domainOntology;
	}

	/**
	 * set mention associated with this instnace
	 * @param mention object
	 */
	public void setMention(Mention mention) {
		this.mention = mention;
		cls = domainOntology.getConceptClass(mention);
		if(mention != null)
			getModifiers().addAll(mention.getModifiers());
		reset();
	}

	/**
	 * get modifier associated with this instance
	 * @return modifier object
	 */
	
	public Modifier getModifier() {
		return modifier;
	}
	
	/**
	 * get mentions that might make up this instance
	 * @return list of component mentions
	 */
	public List<Mention> getCompoundComponents() {
		if(compoundComponents == null)
			compoundComponents = new ArrayList<Mention>();
		return compoundComponents;
	}

	/**
	 * set compound component mentions
	 * @param compoundComponents - list of components
	 */
	public void setCompoundComponents(List<Mention> compoundComponents) {
		this.compoundComponents = compoundComponents;
	}

	/**
	 * set modifier object
	 * @param modifier object 
	 */
	public void setModifier(Modifier modifier) {
		this.modifier = modifier;
		setMention(modifier.getMention());
		if(mention == null){
			cls = domainOntology.getOntology().getClass(modifier.getValue());
		}
		reset();
	}
	
	/**
	 * reset instance information
	 */
	protected void reset(){
		instance = null;
		modifierInstances = null;
		annotations = null;
	}
	
	/**
	 * get mention associated with this class
	 * @return mention object
	 */

	public Mention getMention() {
		return mention;
	}
	
	/**
	 * get a list of mentions associated with this anchor
	 * @return
	 *
	public List<Mention> getMentions(){
		if(mentions == null){
			mentions = new ArrayList<Mention>();
		}
		return mentions;
	}
	 */
	
	/**
	 * get concept class representing this mention
	 * @return class that represents this instance
	 */
	
	public IClass getConceptClass() {
		return cls;
	}
	
	/**
	 * get an instance representing this mention
	 * @return ontology instance
	 */
	public IInstance getInstance() {
		// what's the point if we have no class?
		if(cls == null)
			return null;
		
		// init instance
		if(instance == null){
			// check if we have an actual mention or some generic default value w/out a mention
			if(mention != null){
				// if instance is DocumentSection, just make a default one
				if(domainOntology.isTypeOf(cls,DomainOntology.DOCUMENT_SECTION)){
					instance = domainOntology.getDefaultInstance(cls);
				}else{
					instance = domainOntology.createInstance(cls);
				}
				
				// if instance is modifier, but not linguistic modifier (see if we neet to set some other properties
				if(domainOntology.isTypeOf(cls,DomainOntology.MODIFIER) && !domainOntology.isTypeOf(cls,DomainOntology.LINGUISTIC_MODIFER)){
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
				}
				
				// now just add span
				instance.setPropertyValue(cls.getOntology().getProperty(DomainOntology.HAS_SPAN),getInstanceSpan());
				instance.setPropertyValue(cls.getOntology().getProperty(DomainOntology.HAS_ANNOTATION_TEXT),getText());
				
				
			}else if(modifier != null){
				if(DomainOntology.HAS_QUANTITY_VALUE.equals(modifier.getType())){
					return null;
				}else{
					// get default instance of something
					instance = domainOntology.getOntology().getInstance(cls.getName()+"_default");
					if(instance == null)
						instance = cls.createInstance(cls.getName()+"_default");
				}
				
			}
		}
		return instance;
	}
	
	/**
	 * is property range satisfied for a given instance
	 * @param prop - property
	 * @param modifierInstance instance
	 * @return true or false
	 */
	protected boolean isPropertyRangeSatisfied(IProperty prop, Instance modifierInstance){
		if(modifierInstance.getInstance() != null)
			return domainOntology.isPropertyRangeSatisfied(prop, modifierInstance.getInstance());
		if(DomainOntology.HAS_QUANTITY_VALUE.equals(modifierInstance.getModifier().getType())){
			Number num = new Double(modifierInstance.getModifier().getValue());
			return domainOntology.isPropertyRangeSatisfied(prop, num);
		}
		return false;
	}
	
	
	/**
	 * create a string representation of instance span
	 * @return span as string
	 */
	public String getInstanceSpan() {
		StringBuilder str = new StringBuilder();
		for(Annotation a: getAnnotations()){
			str.append(a.getStartPosition()+":"+a.getEndPosition()+" ");
		}
		return str.toString().trim();
	}

	public String getText(){
		StringBuilder str = new StringBuilder();
		for(Annotation a: getAnnotations()){
			str.append(a.getText()+" ");
		}
		return str.toString().trim();
	}

	/**
	 * get name of this instance (derived from class name)
	 * @return name of this instance
	 */
	public String getName(){
		if(getConceptClass() != null)
			return getConceptClass().getName();
		return modifier != null?modifier.getValue():"unknown";
	}
	
	/**
	 * get human preferred label for this instance
	 * @return label of this instance, returns name if label not available
	 */
	public String getLabel(){
		if(getConceptClass() != null)
			return getConceptClass().getLabel();
		return modifier != null?modifier.getValue():"unknown";
	}
	
	/**
	 * pretty print this name
	 * @return pretty printed version of instance
	 */
	public String toString(){
		StringBuilder str = new StringBuilder();
		str.append(getLabel());
		for(String type: getModifierInstances().keySet()){
			for(Instance modifier:getModifierInstances().get(type)){
				str.append(" "+type+": "+modifier);
			}
		}
		return str.toString();
	}
	
	
	/**
	 * get a mapping of linguistic context found for this mention.
	 *
	 * @return the modifiers map
	 */
	public Map<String,Set<Instance>> getModifierInstances(){
		if(modifierInstances == null){
			modifierInstances = new LinkedHashMap<String,Set<Instance>>();
		}
		return modifierInstances;
	}
	
	/**
	 * get a set of instances associated via given property
	 * @param prop - property by which instances are mapped
	 * @return set of instances
	 */
	public Set<Instance> getModifierInstances(String prop){
		Set<Instance> list =  getModifierInstances().get(prop);
		return list != null?list:Collections.<Instance>emptySet();
	}
	
	
	/**
	 * get a list of modifiers associated with this instance
	 * @return the modifiers
	 */
	public Set<Modifier> getModifiers(){
		if(modifiers == null){
			modifiers = new LinkedHashSet<Modifier>();
		}
		return modifiers;
	}
	
	/**
	 * get a list of current modifiers as instance list
	 * @return list of modifier instances
	 */
	protected List<Instance> createModifierInstanceList(){
		// instantiate available modifiers as instances
		List<Instance> modifierInstances = new ArrayList<Instance>();
		for(Modifier m: getModifiers()){
			modifierInstances.add(new Instance(domainOntology, m));
		}
		return modifierInstances;
	}

	/**
	 * get a list of current modifiers as instance list
	 * @return list of modifier instances
	 */
	public List<Instance> getModifierInstanceList(){
		if(getModifierInstances().isEmpty())
            return createModifierInstanceList();

        // instantiate available modifiers
		List<Instance> modifierInstances = new ArrayList<Instance>();
		for(String key: getModifierInstances().keySet()){
			modifierInstances.addAll(getModifierInstances(key));
		}
		return modifierInstances;
	}

	/**
	 * add linguistic modifier of this mention.
	 *
	 * @param m the m
	 */
	public void addModifier(Modifier m) {
		getModifiers().add(m);
		reset();
	}
	
	/**
	 * remove a modifier of this mention.
	 *
	 * @param m the m
	 */
	public void removeModifier(Modifier m) {
		getModifiers().remove(m);
		reset();
	}
	
	/**
	 * does this variable has a modifier of a given type?
	 * @param type - type of modifier
	 * @return true or false
	 */
	
	public boolean hasModifierType(String type){
		for(Modifier m: getModifiers()){
			if(m.getType().equals(type))
				return true;
		}
		return false;
	}
	
	/**
	 * get the first modifier of a given type
	 * @param type - type of modifier
	 * @return modifier for that type 
	 */
	public Modifier getModifier(String type){
		for(Modifier m: getModifiers()){
			if(m.getType().equals(type))
				return m;
		}
		return null;
	}
	
	
	/**
	 * add modifier instance
	 * @param property by which modifier is related
	 * @param inst - instance of modifier
	 */
	public void addModifierInstance(String property, Instance inst){
		// add it to the instance
        IOntology ont = domainOntology.getOntology();
		IProperty prop = ont.getProperty(property);
        IClass number = ont.getClass(DomainOntology.NUMERIC_MODIFER);

        // check if this number instance is too general for THIS instance
      	IClass vc = inst.getConceptClass();
        if(vc != null && vc.hasSuperClass(number) && !prop.isDatatypeProperty() && !isSatisfied(getConceptClass(),prop,vc)) {
			//if we don't have a more specific numeric class, skip it
			boolean skip = false;
			for(Modifier m: getModifiers()){
				IClass mc = domainOntology.getConceptClass(m);
				if(mc.hasSuperClass(vc))
					skip = true;
			}
        	if(skip)
        		return;
        }

        // check that what is in the map doesn't already have the same thing
        for(String p: new ArrayList<String>(getModifierInstances().keySet())){
            IProperty pp =  ont.getProperty(p);
            // if the property that is already in the map is a super property?
            if(prop.hasSuperProperty(pp)){
                getModifierInstances().remove(p);
            // if the new property is more general, then don't add it
            }else if(prop.hasSubProperty(pp)){
                return;
            }
        }


        // add property
		if(prop != null && instance != null){
			if(inst.getInstance() != null){
				instance.addPropertyValue(prop, inst.getInstance());
			}else if(prop.isDatatypeProperty() && inst.getModifier() != null){
				instance.addPropertyValue(prop,new Double(TextTools.parseDecimalValue(inst.getLabel())));
			}
		}

		// add to a map
        Set<Instance> list = getModifierInstances().get(property);
        if(list == null){
            list = new LinkedHashSet<Instance>();
        }
        list.add(inst);
        getModifierInstances().put(property,list);


    }

    private boolean isSatisfied(IClass cls, IProperty prop, IClass value){
        // we only care about the first restriction
        for(IRestriction r: cls.getRestrictions(prop)){
            return r.getParameter().evaluate(value);
        }
        return false;
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
			}
		}
		return annotations;
	}

	public boolean isReasonForFail() {
		return reasonForFail;
	}

	public void setReasonForFail(boolean reasonForFail) {
		this.reasonForFail = reasonForFail;
	}

	public int hashCode() {
		if(mention != null)
			return mention.hashCode();
		if(modifier != null)
			return modifier.hashCode();
		return super.hashCode();
	}

	public boolean equals(Instance m) {
		if(mention != null && m.getMention() != null)
			return mention.equals(m.getMention());
		else if(modifier != null && m.getModifier() != null)
			return modifier.equals(m.getModifier());
		return super.equals(m);
	}
	
	public String getDefinedConstraints(){
		IClass cls = getConceptClass();
		StringBuilder str = new StringBuilder();
		str.append(cls.getLabel()+"\n");
		for(Object o: cls.getEquivalentRestrictions()){
			str.append("\t"+o+"\n");
		}
		return str.toString();
	}
	
	/**
	 * go over numeric quantities and see if any can be upgraded
	 */
	protected void upgradeNumericModifiers(){
		Set<Modifier> newVals = new HashSet<Modifier>();

		for(Modifier mm:  getModifiers()){
			if(mm.hasMention()){
				Mention m = mm.getMention();
				IClass modifierCls = domainOntology.getConceptClass(m);
				if(domainOntology.isTypeOf(modifierCls,DomainOntology.NUMERIC_MODIFER)){
					IProperty hasNumValue = domainOntology.getRelatedProperty(getConceptClass(),mm);
					if(hasNumValue == null)
						continue;
					
					// now go over potential specific instances
					for(IInstance inst: domainOntology.getSpecificInstances(modifierCls)){
						
						// don't bother looking into an instance if it doesn't satisfy the property of this instance
						if(!isSatisfied(getConceptClass(), hasNumValue, inst.getDirectTypes()[0]))
							continue;
						
						// clear values
						inst.removePropertyValues();
					
						
						// set data properties
						for(String prop: m.getModifierMap().keySet()){
							IProperty property = domainOntology.getProperty(prop);
							if(property == null)
								continue;
							// add all values
							for(Modifier mod : m.getModifiers(prop)) {
								if (mod.getMention() != null) {
									inst.addPropertyValue(property,domainOntology.getConceptInstance(mod.getMention()));
								} else {
									inst.addPropertyValue(property, new Double(mod.getValue()));
								}
							}
						}
						
						// now check the equivalence
						IClass parentCls = inst.getDirectTypes()[0];

						// make a copy of equivalent restriction so we can test the units
						LogicExpression expression = new LogicExpression(parentCls.getEquivalentRestrictions());

						// add units to expression if not there and units are available
						// units are unique in the sense that they are often omitted however you
						// do want to validate it when it is in fact available
						IProperty hasUnit = domainOntology.getProperty(DomainOntology.HAS_UNIT);
						if(inst.getPropertyValues(hasUnit).length > 0){
							for(IRestriction r: parentCls.getDirectNecessaryRestrictions().getRestrictions()){
								if(r.getProperty().equals(hasUnit)) {
									expression.add(r);
								}
							}
						}


						// if instance valid, we found a more specific numeric class
						if(expression.evaluate(inst) && isModifierWithinWindowSize(parentCls,m)){
							Mention specificM = domainOntology.getModifierFromClass(parentCls,m);
							Modifier mod = Modifier.getModifier(hasNumValue.getName(),parentCls.getName(),specificM);
							newVals.add(mod);
						}
					}
				}
			}
		}
		getModifiers().addAll(newVals);
	}

    /**
     * check if the new upgraded numeric modifier is within the new window size
     * @param cls - upgraded numeric class
     * @param m - quantity modifier
     * @return true or false
     */
    private boolean isModifierWithinWindowSize(IClass cls, Mention m){
        // find a more specific named instance
        int windowSize = -1;
        List<String> actions = new ArrayList<String>();

        // see if there is custom action and window size defined
        for(IInstance i: cls.getDirectInstances()){
           for(Object a: i.getPropertyValues(domainOntology.getProperty(ConText.PROP_WINDOW_SIZE))){
               windowSize = Integer.parseInt(a.toString());
           }
            for(Object a: i.getPropertyValues(domainOntology.getProperty(ConText.HAS_SENTENCE_ACTION))){
                actions.add(((IInstance)a).getName());
            }
       }
       if(windowSize > -1 && !actions.isEmpty()){
           List<Mention> targets = ConText.getTargetMentionsInRange(m,getMention().getSentence(),actions,windowSize);
           return targets.contains(getMention());
       }


       return true;
    }

}
