package edu.pitt.dbmi.nlp.noble.ontology.owl;

import edu.pitt.dbmi.nlp.noble.ontology.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.util.OWLEntityRenamer;

import java.net.URI;
import java.util.*;


/**
 * The Class OResource.
 */
public class OResource implements IResource{
	private OOntology ontology;
	protected OWLObject obj;
	protected Properties info;
	
	
	/**
	 * Instantiates a new o resource.
	 *
	 * @param obj the obj
	 */
	protected OResource(OWLObject obj){
		this.obj = obj;
	}
	
	/**
	 * Instantiates a new o resource.
	 *
	 * @param obj the obj
	 * @param ont the ont
	 */
	protected OResource(OWLObject obj,OOntology ont){
		this.obj = obj;
		this.ontology = ont;
	}
	
	/**
	 * Sets the ontology.
	 *
	 * @param ont the new ontology
	 */
	public void setOntology(OOntology ont){
		ontology = ont;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.Describable#getFormat()
	 */
	public String getFormat() {
		OWLOntology o = getOWLOntology();
		if(o != null)
			return o.getOWLOntologyManager().getOntologyFormat(o).toString();
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.terminology.Describable#getLocation()
	 */
	public String getLocation() {
		return getIRI().toString();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(IResource o) {
		return getURI().compareTo(o.getURI());
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#getDescription()
	 */
	public String getDescription() {
		return getComments().length > 0?getComments()[0]:"";
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#setDescription(java.lang.String)
	 */
	public void setDescription(String text) {
		addComment(text);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#getName()
	 */
	public String getName() {
		IRI iri = getIRI();
		URI uri = iri.toURI();
		String nm = uri.getFragment();
		if(nm == null)
			nm = iri.getFragment();
		if(nm == null)
			nm = ""+iri;
		return nm;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#getLabel()
	 */
	public String getLabel(){
		if(getLabels().length > 0)
			return getLabels()[0];
		return getName();
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return getName();
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#dispose()
	 */
	public void dispose() {
		obj = null;
		info = null;
		ontology = null;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#getVersion()
	 */
	public String getVersion() {
		String [] v = getAnnotations(getOWLDataFactory().getOWLVersionInfo()).toArray(new String [0]);
		return v.length > 0?v[0]:null;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#getURI()
	 */
	public URI getURI() {
		return getIRI().toURI();
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#getNameSpace()
	 */
	public String getNameSpace() {
		return getIRI().getNamespace();
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#getPrefix()
	 */
	public String getPrefix() {
		PrefixManager pm = ((OOntology)getOntology()).getPrefixManager(); 
		Map<String,String> map = pm.getPrefixName2PrefixMap();
		for(String prefix: map.keySet()){
			if(getNameSpace().equals(map.get(prefix)))
				return prefix;
		}
		return ":";
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#getProperties()
	 */
	public IProperty[] getProperties() {
		OWLEntity e = asOWLEntity();
		if(e != null){
			Set<IProperty> list = new LinkedHashSet<IProperty>();
			for(OWLOntology o: getDefiningOntologies()){
				for(OWLAnnotation a: e.getAnnotations(o)){
					list.add((IProperty)convertOWLObject(a.getProperty()));
				}
			}
			return list.toArray(new IProperty [0]);
		}
		return new IProperty [0];
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#getPropertyValue(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public Object getPropertyValue(IProperty prop) {
		Object [] a = getPropertyValues(prop);
		return a.length > 0?a[0]:null;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#getPropertyValues(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public Object[] getPropertyValues(IProperty prop) {
		OWLEntity e = asOWLEntity();
		if(e != null){
			Set list = new LinkedHashSet();
			for(OWLOntology o: getDefiningOntologies()){
				for(OWLAnnotation a: e.getAnnotations(o,(OWLAnnotationProperty)convertOntologyObject(prop))){
					Object oo = convertOWLObject(a.getValue());
					if(oo != null)
						list.add(oo);
				}
			}
			return list.toArray();
		}
		return new Object [0];
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#addPropertyValue(edu.pitt.dbmi.nlp.noble.ontology.IProperty, java.lang.Object)
	 */
	public void addPropertyValue(IProperty prop, Object value) {
		if(prop.isAnnotationProperty()){
			addAxiom(getAnnotationAxiom((OWLAnnotationProperty)convertOntologyObject(prop),(OWLAnnotationValue)convertOntologyObject(value)));
		}else
			throw new IOntologyError("Not implemented for "+getClass().getName());
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#setPropertyValue(edu.pitt.dbmi.nlp.noble.ontology.IProperty, java.lang.Object)
	 */
	public void setPropertyValue(IProperty prop, Object value) {
		if(prop.isAnnotationProperty()){
			removePropertyValues(prop);
			addPropertyValue(prop, value);
		}else
			throw new IOntologyError("Not implemented for "+getClass().getName());
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#setPropertyValues(edu.pitt.dbmi.nlp.noble.ontology.IProperty, java.lang.Object[])
	 */
	public void setPropertyValues(IProperty prop, Object[] values) {
		if(prop.isAnnotationProperty()){
			removePropertyValues(prop);
			for(Object o: values){
				addPropertyValue(prop,o);
			}
		}else
			throw new IOntologyError("Not implemented for "+getClass().getName());
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#removePropertyValues(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public void removePropertyValues(IProperty prop) {
		OWLEntity e = asOWLEntity();
		if(e != null){
			for(OWLOntology o: getDefiningOntologies()){
				for(OWLAnnotation a: e.getAnnotations(o)){
					if(prop.equals(convertOWLObject(a.getProperty()))){
						removeAxiom(getAnnotationAxiom(a.getProperty(),a.getValue()));
					}
					
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#removePropertyValue(edu.pitt.dbmi.nlp.noble.ontology.IProperty, java.lang.Object)
	 */
	public void removePropertyValue(IProperty prop, Object value) {
		if(prop.isAnnotationProperty()){
			removeAxiom(getAnnotationAxiom((OWLAnnotationProperty)convertOntologyObject(prop),(OWLAnnotationValue)convertOntologyObject(value)));
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#removePropertyValues()
	 */
	public void removePropertyValues() {
		OWLEntity e = asOWLEntity();
		if(e != null){
			for(OWLOntology o: getDefiningOntologies()){
				for(OWLAnnotation a: e.getAnnotations(o)){
					removeAxiom(getAnnotationAxiom(a.getProperty(),a.getValue()));
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#hasPropetyValue(edu.pitt.dbmi.nlp.noble.ontology.IProperty, java.lang.Object)
	 */
	public boolean hasPropetyValue(IProperty p, Object value) {
		OWLEntity e = asOWLEntity();
		if(e != null){
			for(OWLOntology o: getDefiningOntologies()){
				for(OWLAnnotation a: e.getAnnotations(o)){
					if(p.equals(convertOWLObject(a.getProperty())) && 
					   value.equals(convertOWLObject(a.getValue()))){
						return true;
					}
				}
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#addLabel(java.lang.String)
	 */
	public void addLabel(String label) {
		List<String> labels = getAnnotations(getOWLDataFactory().getRDFSLabel());
		if(!labels.contains(label)){
			addAnnotation(getOWLDataFactory().getRDFSLabel(),label);
		}
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#addComment(java.lang.String)
	 */
	public void addComment(String comment) {
		List<String> labels = getAnnotations(getOWLDataFactory().getRDFSComment());
		if(!labels.contains(comment)){
			addAnnotation(getOWLDataFactory().getRDFSComment(),comment);
		}
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#removeLabel(java.lang.String)
	 */
	public void removeLabel(String label) {
		List<String> labels = getAnnotations(getOWLDataFactory().getRDFSLabel());
		if(labels.contains(label)){
			removeAnnotation(getOWLDataFactory().getRDFSLabel(),label);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#removeComment(java.lang.String)
	 */
	public void removeComment(String comment) {
		List<String> labels = getAnnotations(getOWLDataFactory().getRDFSComment());
		if(labels.contains(comment)){
			removeAnnotation(getOWLDataFactory().getRDFSComment(),comment);
		}
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#addVersion(java.lang.String)
	 */
	public void addVersion(String version) {
		List<String> labels = getAnnotations(getOWLDataFactory().getOWLVersionInfo());
		if(!labels.contains(version)){
			addAnnotation(getOWLDataFactory().getOWLVersionInfo(),version);
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#removeVersion(java.lang.String)
	 */
	public void removeVersion(String version) {
		List<String> labels = getAnnotations(getOWLDataFactory().getOWLVersionInfo());
		if(labels.contains(version)){
			removeAnnotation(getOWLDataFactory().getOWLVersionInfo(),version);
		}
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#getLabels()
	 */
	public String[] getLabels() {
		return getAnnotations(getOWLDataFactory().getRDFSLabel()).toArray(new String [0]);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#getComments()
	 */
	public String[] getComments() {
		return getAnnotations(getOWLDataFactory().getRDFSComment()).toArray(new String [0]);
	}
	
	
	/**
	 * get annotations associated with a given entry.
	 *
	 * @param prop the prop
	 * @return the annotations
	 */
	private List<String> getAnnotations(OWLAnnotationProperty prop){
		List<String> list = new ArrayList<String>();
		OWLEntity entity = asOWLEntity();
		if(entity != null){
			for(OWLOntology o: getDefiningOntologies()){
				for(OWLAnnotation a: entity.getAnnotations(o,prop)){
					String ss = (String)convertOWLObject(a.getValue());
					if(ss != null)
						list.add(ss);
			    }
			}
		}else if(obj instanceof OWLOntology){
			for(OWLAnnotation a: getOWLOntology().getAnnotations()){
				if(a.getProperty().equals(prop)){
					String ss = (String)convertOWLObject(a.getValue());
					if(ss != null)
						list.add(ss);
				}
		    }
		}
		return list;
	}
	
	
	/**
	 * get defining ontology for this attribute.
	 *
	 * @return 	protected OWLOntology getDefiningOntology(){
	 * 		OWLEntity e = asOWLEntity();
	 * 		OWLOntology o = null;
	 * 		if(e != null){
	 * 			OWLOntologyManager man = getOWLOntologyManager();
	 * 			String s = getNameSpace();
	 * 			if(s.endsWith("#"))
	 * 				s = s.substring(0,s.length()-1);
	 * 			o =  man.getOntology(IRI.create(s));
	 * 		}
	 * 		return o != null?o:getOWLOntology();
	 * 	}
	 */
	
	protected Set<OWLOntology> getDefiningOntologies(){
		return getOWLOntologyManager().getOntologies();
	}

	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#isSystem()
	 */
	public boolean isSystem() {
		OWLEntity o = asOWLEntity();
		return (o != null)?o.isBuiltIn():false;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#getOntology()
	 */
	public OOntology getOntology() {
		return ontology;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#delete()
	 */
	public void delete() {
		if(obj instanceof OWLEntity){
			OWLEntityRemover remover = getOWLEntityRemover();
			((OWLEntity)obj).accept(remover);
			getOWLOntologyManager().applyChanges(remover.getChanges());
	        remover.reset();
		}
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#setName(java.lang.String)
	 */
	public void setName(String name) {
		if(obj instanceof OWLEntity){
			OWLEntityRenamer renamer = getOWLEntityRenamer();
			IRI newIRI = IRI.create(getNameSpace()+name);
			List<OWLOntologyChange> changes = renamer.changeIRI((OWLEntity)obj,newIRI);
			getOWLOntologyManager().applyChanges(changes);
	 	}
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#getLogicExpression()
	 */
	public ILogicExpression getLogicExpression() {
		return new LogicExpression(this);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResource#getResourceProperties()
	 */
	public Properties getResourceProperties() {
		if(info == null)
			info = new Properties();
		return info;
	}

	/**
	 * Gets the OWL object.
	 *
	 * @return the OWL object
	 */
	protected OWLObject getOWLObject(){
		return obj;
	}
	
	/**
	 * Gets the iri.
	 *
	 * @return the iri
	 */
	protected IRI getIRI(){
		if(obj instanceof OWLOntology)
			return ((OWLOntology)obj).getOntologyID().getOntologyIRI();
		else if(obj instanceof OWLNamedObject)
			return ((OWLNamedObject)obj).getIRI();
		return null;
	}
	
	/**
	 * As OWL entity.
	 *
	 * @return the OWL entity
	 */
	protected OWLEntity asOWLEntity(){
		if(obj instanceof OWLEntity)
			return (OWLEntity) obj;
		return null;
	}

	
	/**
	 * Adds the annotation.
	 *
	 * @param prop the prop
	 * @param str the str
	 */
	protected void addAnnotation(OWLAnnotationProperty prop,String str){
		OWLDataFactory df = getOWLDataFactory();
		addAxiom(getAnnotationAxiom(prop, df.getOWLLiteral(str)));
	}
	
	/**
	 * Removes the annotation.
	 *
	 * @param prop the prop
	 * @param str the str
	 */
	protected void removeAnnotation(OWLAnnotationProperty prop,String str){
		OWLDataFactory df = getOWLDataFactory();
		removeAxiom(getAnnotationAxiom(prop, df.getOWLLiteral(str)));
	}
	
	/**
	 * get annotation axiom.
	 *
	 * @param prop the prop
	 * @param val the val
	 * @return the annotation axiom
	 */
	private OWLAxiom getAnnotationAxiom(OWLAnnotationProperty prop,OWLAnnotationValue val){
		OWLDataFactory df = getOWLDataFactory();
		OWLAnnotation commentAnno = df.getOWLAnnotation(prop,val);
		return df.getOWLAnnotationAssertionAxiom(getIRI(),commentAnno);
	}
	
	/**
	 * check if a language is in a filter.
	 *
	 * @param lang the lang
	 * @return true, if is language in filter
	 */
	protected boolean isLanguageInFilter(String lang){
		if(lang == null || lang.length() == 0 || getOntology().getLanguageFilter() == null || getOntology().getLanguageFilter().isEmpty())
			return true;
		return getOntology().getLanguageFilter().contains(lang);
	}
		
	
	/**
	 * get annotation value as java object.
	 *
	 * @param val the val
	 * @return the object
	 */
	protected Object convertOWLObject(OWLObject val){
		if(val == null)
			return null;
		
		if(val instanceof OWLLiteral){
			OWLLiteral l = (OWLLiteral) val;
			String type = l.getDatatype().toString().toLowerCase();
			
			if(l.isInteger() || type.contains("int") || type.contains("short"))
				return l.parseInteger();
			if(l.isBoolean())
				return l.parseBoolean();
			if(l.isDouble())
				return l.parseDouble();
			if(l.isFloat())
				return l.parseFloat();
			//if(l.getDatatype().equals(OWL2Datatype.XSD_DATE_TIME))
			//	return l.
			if(isLanguageInFilter(l.getLang()))
				return l.getLiteral();
			return null;
		}else if (val instanceof OWLClass){
			return new OClass((OWLClass)val,getOntology());
		}else if (val instanceof OWLIndividual){
			return new OInstance((OWLIndividual)val,getOntology());
		}else if (val instanceof OWLAnnotationProperty){
			return new OAnnotation((OWLAnnotationProperty)val,getOntology());
		}else if (val instanceof OWLPropertyExpression){
			return new OProperty((OWLPropertyExpression)val,getOntology());
		}else if(val instanceof OWLNaryBooleanClassExpression){
			LogicExpression exp = new LogicExpression(LogicExpression.EMPTY);
			if(val instanceof OWLObjectIntersectionOf)
				exp.setExpressionType(ILogicExpression.AND);
			else if(val instanceof OWLObjectUnionOf)
				exp.setExpressionType(ILogicExpression.OR);
			for(OWLClassExpression e: ((OWLNaryBooleanClassExpression)val).getOperands()){
				exp.add(convertOWLObject(e));
			}
			return exp;
		}else if(val instanceof OWLNaryDataRange){
			LogicExpression exp = new LogicExpression(LogicExpression.EMPTY);
			if(val instanceof OWLDataIntersectionOf)
				exp.setExpressionType(ILogicExpression.AND);
			else if(val instanceof OWLDataUnionOf)
				exp.setExpressionType(ILogicExpression.OR);
			for(OWLDataRange e: ((OWLNaryDataRange)val).getOperands()){
				exp.add(convertOWLObject(e));
			}
			return exp;
		}else if(val instanceof OWLDataOneOf){
			LogicExpression exp = new LogicExpression(LogicExpression.OR);
			for(OWLLiteral e: ((OWLDataOneOf)val).getValues()){
				exp.add(convertOWLObject(e));
			}
			return exp;
		}else if(val instanceof OWLObjectComplementOf){
			LogicExpression exp = new LogicExpression(LogicExpression.NOT);
			exp.add(convertOWLObject(((OWLObjectComplementOf) val).getOperand()));
			return exp;
		}else if(val instanceof OWLRestriction){
			return new ORestriction((OWLRestriction)val,getOntology());
		}else if (val instanceof OWLDatatypeRestriction){
			LogicExpression exp = new LogicExpression(LogicExpression.AND);
			for(OWLFacetRestriction fr: ((OWLDatatypeRestriction) val).getFacetRestrictions()){
				exp.add(new OFacetRestriction(fr, ontology));
			}
			return exp;
		}else if (val instanceof OWLDataRange){
			OWLDataRange l = (OWLDataRange) val;
			/*
			if(l.isDatatype()){
				if(l.asOWLDatatype().isBoolean()){
					return Boolean.FALSE;
				}else if(l.asOWLDatatype().isInteger()){
					return new Integer(0);
				}else if(l.asOWLDatatype().isDouble()){
					return new Double(0);
				}else if(l.asOWLDatatype().isFloat()){
					return new Float(0);
				}else{
					return new String("string");
				}
				return l.toString();
			}*/
			return new ODataRange(l,getOntology());
		}
		return null;
	}
	
	/**
	 * convert Ontology object back to OWL-API.
	 *
	 * @param val the val
	 * @return the object
	 */
	protected Object convertOntologyObject(Object val){
		if(val == null)
			return null;
		
		OWLDataFactory df = getOWLDataFactory();
		// Ontology Objects
		if(val instanceof ORestriction)
			return ((ORestriction)val).getOWLRestriction();
		if(val instanceof OClass)
			return ((OClass) val).getOWLClass();
		if(val instanceof OInstance)
			return ((OInstance) val).getOWLIndividual();
		if(val instanceof OAnnotation)
			return ((OAnnotation) val).getOWLAnnotationProperty();
		if(val instanceof OProperty)
			return ((OProperty) val).getOWLProperty();
		
		// data types
		if(val instanceof String)
			return df.getOWLLiteral((String) val);
		if(val instanceof Double)
			return df.getOWLLiteral((Double) val);
		if(val instanceof Float)
			return df.getOWLLiteral((Float) val);
		if(val instanceof Integer )
			return df.getOWLLiteral((Integer) val);
		if(val instanceof Boolean )
			return df.getOWLLiteral((Boolean) val);
		if(val instanceof ILogicExpression){
			ILogicExpression exp = (ILogicExpression) val;
			if(exp.isEmpty())
				return null;
			Object obj = convertOntologyObject(exp.get(0));
			switch(exp.getExpressionType()){
			case ILogicExpression.EMPTY:
				return obj;
			case ILogicExpression.NOT:
				if(obj instanceof OWLLiteral)
					return df.getOWLDataComplementOf(((OWLLiteral)obj).getDatatype());
				else if(obj instanceof OWLClassExpression )
					return df.getOWLObjectComplementOf((OWLClassExpression)obj);
			case ILogicExpression.AND:
				if(obj instanceof OWLLiteral){
					Set<OWLDataRange> dataRanges = new LinkedHashSet<OWLDataRange>();
					for(Object o: exp){
						dataRanges.add(((OWLLiteral)convertOntologyObject(o)).getDatatype());
					}
					return df.getOWLDataIntersectionOf(dataRanges);
				}else if(obj instanceof OWLClassExpression ){
					Set<OWLClassExpression> dataRanges = new LinkedHashSet<OWLClassExpression>();
					for(Object o: exp){
						dataRanges.add((OWLClassExpression)convertOntologyObject(o));
					}
					return df.getOWLObjectIntersectionOf(dataRanges);
				}
			case ILogicExpression.OR:
				if(obj instanceof OWLLiteral){
					Set<OWLDataRange> dataRanges = new LinkedHashSet<OWLDataRange>();
					for(Object o: exp){
						dataRanges.add(((OWLLiteral)convertOntologyObject(o)).getDatatype());
					}
					return df.getOWLDataUnionOf(dataRanges);
				}else if(obj instanceof OWLClassExpression ){
					Set<OWLClassExpression> dataRanges = new LinkedHashSet<OWLClassExpression>();
					for(Object o: exp){
						dataRanges.add((OWLClassExpression)convertOntologyObject(o));
					}
					return df.getOWLObjectUnionOf(dataRanges);
				}
			}
		}
		return null;
	}
	
	
	/**
	 * method to get super/sub direct/all classes.
	 *
	 * @param list the list
	 * @return the classes
	 */
	protected IClass [] getClasses(Collection<OWLClass> list){
		Set<IClass> c = new LinkedHashSet<IClass>();
		for(OWLClass child: list){
			if(!(child.isAnonymous() || child.isBottomEntity()))
				c.add((IClass) convertOWLObject(child));
		}
		return (IClass []) c.toArray(new IClass [0]);
	}
	
	/**
	 * Gets the properties.
	 *
	 * @param list the list
	 * @return the properties
	 */
	protected IProperty [] getProperties(Collection list){
		List<IProperty> props = new ArrayList<IProperty>();
		for(Object p: list){
			if(p instanceof OWLEntity){
				OWLEntity e = (OWLEntity) p;
				if(!e.isBottomEntity() && !e.isTopEntity())
					props.add((IProperty)convertOWLObject(e));
			}
		}
		return props.toArray(new IProperty [0]);
	}
	
	/**
	 * method to get super/sub direct/all classes.
	 *
	 * @param list the list
	 * @return the instances
	 */
	protected IInstance [] getInstances(Collection<OWLNamedIndividual> list){
		Set<IInstance> c = new LinkedHashSet<IInstance>();
		for(OWLNamedIndividual child: list){
			if(!(child.isAnonymous() || child.isBottomEntity()))
				c.add((IInstance) convertOWLObject(child));
		}
		return (IInstance []) c.toArray(new IInstance [0]);
	}
	
	/**
	 * Adds the axiom.
	 *
	 * @param ax the ax
	 */
	protected void addAxiom(OWLAxiom ax){
		getOWLOntologyManager().addAxiom(getOWLOntology(),ax);
	}
	
	/**
	 * Removes the axiom.
	 *
	 * @param ax the ax
	 */
	protected void removeAxiom(OWLAxiom ax){
		getOWLOntologyManager().removeAxiom(getOWLOntology(),ax);
	}

	/**
	 * Gets the OWL ontology.
	 *
	 * @return the OWL ontology
	 */
	protected OWLOntology getOWLOntology(){
		return getOntology().getOWLOntology();
	}
	
	/**
	 * Gets the OWL data factory.
	 *
	 * @return the OWL data factory
	 */
	protected OWLDataFactory getOWLDataFactory(){
		return getOntology().getOWLDataFactory();
	}
	
	/**
	 * Gets the OWL ontology manager.
	 *
	 * @return the OWL ontology manager
	 */
	protected OWLOntologyManager getOWLOntologyManager(){
		return getOntology().getOWLOntologyManager();
	}
	
	/**
	 * Gets the OWL reasoner.
	 *
	 * @return the OWL reasoner
	 */
	protected OWLReasoner getOWLReasoner(){
		return getOntology().getOWLReasoner();
	}
	
	/**
	 * Gets the OWL entity remover.
	 *
	 * @return the OWL entity remover
	 */
	protected OWLEntityRemover getOWLEntityRemover(){
		return getOntology().getOWLEntityRemover();
	}
	
	/**
	 * Gets the OWL entity renamer.
	 *
	 * @return the OWL entity renamer
	 */
	protected OWLEntityRenamer getOWLEntityRenamer(){
		return getOntology().getOWLEntityRenamer();
	}
	
	/**
	 * where there is equals, there is hashCode.
	 *
	 * @return the int
	 */
	public int hashCode() {
		return getURI().hashCode();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		if(obj instanceof IResource){
			return getURI().equals(((IResource)obj).getURI());
		}else
			return super.equals(obj);
	}
}
