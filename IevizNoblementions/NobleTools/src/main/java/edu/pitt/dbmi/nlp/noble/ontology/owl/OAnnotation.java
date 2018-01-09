package edu.pitt.dbmi.nlp.noble.ontology.owl;

import java.util.LinkedHashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLDataFactory;

import edu.pitt.dbmi.nlp.noble.ontology.IClass;
import edu.pitt.dbmi.nlp.noble.ontology.IOntologyError;
import edu.pitt.dbmi.nlp.noble.ontology.IProperty;
import edu.pitt.dbmi.nlp.noble.ontology.IResource;

/**
 * represents annotation property.
 *
 * @author tseytlin
 */
public class OAnnotation extends OResource implements IProperty {
	private OWLAnnotationProperty annotation;
	
	/**
	 * Instantiates a new o annotation.
	 *
	 * @param obj the obj
	 * @param ont the ont
	 */
	protected OAnnotation(OWLAnnotationProperty obj, OOntology ont) {
		super(obj, ont);
		annotation = obj;
	}

	/**
	 * Gets the OWL annotation property.
	 *
	 * @return the OWL annotation property
	 */
	public OWLAnnotationProperty getOWLAnnotationProperty(){
		return annotation;
	}

	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#createSubProperty(java.lang.String)
	 */
	public IProperty createSubProperty(String name) {
		OWLDataFactory dataFactory = getOWLDataFactory();
		OWLAnnotationProperty ch = dataFactory.getOWLAnnotationProperty(IRI.create(getOntology().getNameSpace()+name));
		addAxiom(getOWLDataFactory().getOWLSubAnnotationPropertyOfAxiom(ch,annotation));
		return (IProperty) convertOWLObject(ch);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#getPropertyType()
	 */
	public int getPropertyType() {
		return IProperty.ANNOTATION_DATATYPE;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#isDatatypeProperty()
	 */
	public boolean isDatatypeProperty() {
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#isObjectProperty()
	 */
	public boolean isObjectProperty() {
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#isAnnotationProperty()
	 */
	public boolean isAnnotationProperty() {
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#getDomain()
	 */
	public IClass[] getDomain() {
		return new IClass [0];
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#getRange()
	 */
	public Object[] getRange() {
		return new Object [0];
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#setDomain(edu.pitt.dbmi.nlp.noble.ontology.IResource[])
	 */
	public void setDomain(IResource[] domain) {
		//throw new IOntologyError("Not implemented");
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#setRange(java.lang.Object[])
	 */
	public void setRange(Object[] range) {
		//throw new IOntologyError("Not implemented");
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#isInverseOf(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public boolean isInverseOf(IProperty p) {
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#isTransitive()
	 */
	public boolean isTransitive() {
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#isFunctional()
	 */
	public boolean isFunctional() {
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#isSymmetric()
	 */
	public boolean isSymmetric() {
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#getSubProperties()
	 */
	public IProperty[] getSubProperties() {
		Set<IProperty> list = new LinkedHashSet<IProperty>();
		return getSubProperties(this, list).toArray(new IProperty [0]);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#getSuperProperties()
	 */
	public IProperty[] getSuperProperties() {
		Set<IProperty> list = new LinkedHashSet<IProperty>();
		return getSuperProperties(this, list).toArray(new IProperty [0]);
	}

	/**
	 * Gets the sub properties.
	 *
	 * @param p the p
	 * @param list the list
	 * @return the sub properties
	 */
	private Set<IProperty> getSubProperties(IProperty p, Set<IProperty> list){
		for(IProperty c: p.getDirectSubProperties()){
			list.add(c);
			getSubProperties(c,list);
		}
		return list;
	}
	
	/**
	 * Gets the super properties.
	 *
	 * @param p the p
	 * @param list the list
	 * @return the super properties
	 */
	private Set<IProperty> getSuperProperties(IProperty p, Set<IProperty> list){
		for(IProperty c: p.getDirectSuperProperties()){
			list.add(c);
			getSuperProperties(c,list);
		}
		return list;
	}
	
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#getDirectSubProperties()
	 */
	public IProperty[] getDirectSubProperties() {
		return getProperties(annotation.getSubProperties(getDefiningOntologies()));
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#getDirectSuperProperties()
	 */
	public IProperty[] getDirectSuperProperties() {
		return getProperties(annotation.getSuperProperties(getDefiningOntologies()));
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#getInverseProperty()
	 */
	public IProperty getInverseProperty() {
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#setInverseProperty(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public void setInverseProperty(IProperty p) {
		throw new IOntologyError("Not implemented");
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#addSuperProperty(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public void addSuperProperty(IProperty p) {
		if(!p.isAnnotationProperty())
			throw new IOntologyError("Can't add non-annotation superproperty");
		OWLAnnotationProperty ch = (OWLAnnotationProperty) convertOntologyObject(p);
		addAxiom(getOWLDataFactory().getOWLSubAnnotationPropertyOfAxiom(annotation,ch));
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#addSubProperty(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public void addSubProperty(IProperty p) {
		if(!p.isAnnotationProperty())
			throw new IOntologyError("Can't add non-annotation subproperty");
		OWLAnnotationProperty ch = (OWLAnnotationProperty) convertOntologyObject(p);
		addAxiom(getOWLDataFactory().getOWLSubAnnotationPropertyOfAxiom(ch,annotation));
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#removeSuperProperty(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public void removeSuperProperty(IProperty p) {
		if(!p.isAnnotationProperty())
			throw new IOntologyError("Can't remove non-annotation superproperty");
		OWLAnnotationProperty ch = (OWLAnnotationProperty) convertOntologyObject(p);
		removeAxiom(getOWLDataFactory().getOWLSubAnnotationPropertyOfAxiom(annotation,ch));
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#removeSubProperty(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public void removeSubProperty(IProperty p) {
		if(!p.isAnnotationProperty())
			throw new IOntologyError("Can't remove non-annotation subproperty");
		OWLAnnotationProperty ch = (OWLAnnotationProperty) convertOntologyObject(p);
		removeAxiom(getOWLDataFactory().getOWLSubAnnotationPropertyOfAxiom(ch,annotation));
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#setTransitive(boolean)
	 */
	public void setTransitive(boolean b) {
		throw new IOntologyError("Not implemented");
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#setFunctional(boolean)
	 */
	public void setFunctional(boolean b) {
		throw new IOntologyError("Not implemented");
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#setSymmetric(boolean)
	 */
	public void setSymmetric(boolean b) {
		throw new IOntologyError("Not implemented");
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#hasSuperProperty(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public boolean hasSuperProperty(IProperty o) {
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#hasSubProperty(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public boolean hasSubProperty(IProperty o) {
		return false;
	}

}
