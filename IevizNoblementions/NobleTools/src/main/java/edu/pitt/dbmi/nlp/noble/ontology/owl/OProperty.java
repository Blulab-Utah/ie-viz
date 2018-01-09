package edu.pitt.dbmi.nlp.noble.ontology.owl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;

import edu.pitt.dbmi.nlp.noble.ontology.IClass;
import edu.pitt.dbmi.nlp.noble.ontology.IOntologyError;
import edu.pitt.dbmi.nlp.noble.ontology.IProperty;
import edu.pitt.dbmi.nlp.noble.ontology.IResource;
import edu.pitt.dbmi.nlp.noble.ontology.LogicExpression;

/**
 * The Class OProperty.
 */
public class OProperty extends OResource implements IProperty {
	private OWLPropertyExpression property;
	
	/**
	 * Instantiates a new o property.
	 *
	 * @param obj the obj
	 * @param ont the ont
	 */
	protected OProperty(OWLPropertyExpression obj,OOntology ont) {
		super(obj,ont);
		property = obj;
	}

	/**
	 * Gets the OWL property.
	 *
	 * @return the OWL property
	 */
	public OWLProperty getOWLProperty(){
		return (OWLProperty) property;
	}
	
	/**
	 * As OWL data property.
	 *
	 * @return the OWL data property
	 */
	OWLDataProperty asOWLDataProperty(){
		return (OWLDataProperty)property;
	}
	
	/**
	 * As OWL object property.
	 *
	 * @return the OWL object property
	 */
	OWLObjectProperty asOWLObjectProperty(){
		return (OWLObjectProperty)property;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#createSubProperty(java.lang.String)
	 */
	public IProperty createSubProperty(String name) {
		OWLDataFactory dataFactory = getOWLDataFactory();
		if(isDatatypeProperty()){
			OWLDataProperty ch = dataFactory.getOWLDataProperty(IRI.create(getOntology().getNameSpace()+name));
			addAxiom(getOWLDataFactory().getOWLSubDataPropertyOfAxiom(ch,asOWLDataProperty()));
			return (IProperty) convertOWLObject(ch);
		}else{
			OWLObjectProperty ch = dataFactory.getOWLObjectProperty(IRI.create(getOntology().getNameSpace()+name));
			addAxiom(getOWLDataFactory().getOWLSubObjectPropertyOfAxiom(ch,asOWLObjectProperty()));
			return (IProperty) convertOWLObject(ch);
		}
		
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#getPropertyType()
	 */
	public int getPropertyType() {
		return isDatatypeProperty()?IProperty.DATATYPE:IProperty.OBJECT;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#isDatatypeProperty()
	 */
	public boolean isDatatypeProperty() {
		return getOWLProperty().isOWLDataProperty();
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#isObjectProperty()
	 */
	public boolean isObjectProperty() {
		return getOWLProperty().isOWLObjectProperty();
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#isAnnotationProperty()
	 */
	public boolean isAnnotationProperty() {
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#getDomain()
	 */
	public IClass[] getDomain() {
		if(isObjectProperty()){
			NodeSet<OWLClass> sub = getOWLReasoner().getObjectPropertyDomains(asOWLObjectProperty(),true);
			return getClasses(sub.getFlattened());
		}else{
			NodeSet<OWLClass> sub = getOWLReasoner().getDataPropertyDomains(asOWLDataProperty(),true);
			return getClasses(sub.getFlattened());
		}
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#getRange()
	 */
	public Object[] getRange() {
		if(isObjectProperty()){
			NodeSet<OWLClass> sub = getOWLReasoner().getObjectPropertyRanges(asOWLObjectProperty(),true);
			return getClasses(sub.getFlattened());
		}else{
			LogicExpression lst = new LogicExpression(LogicExpression.AND);
			for(OWLDataRange r: asOWLDataProperty().getRanges(getDefiningOntologies())){
				lst.add(convertOWLObject(r));
			}
			return lst.toArray();
		}
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#setDomain(edu.pitt.dbmi.nlp.noble.ontology.IResource[])
	 */
	public void setDomain(IResource[] domain) {
		if(isObjectProperty()){
			for(OWLClass c: getOWLReasoner().getObjectPropertyDomains(asOWLObjectProperty(),false).getFlattened())
				removeAxiom(getOWLDataFactory().getOWLObjectPropertyDomainAxiom(asOWLObjectProperty(),c));
			for(IResource r: domain)
				addAxiom(getOWLDataFactory().getOWLObjectPropertyDomainAxiom(asOWLObjectProperty(),(OWLClassExpression)convertOntologyObject(r)));
		}else{
			for(OWLClass c: getOWLReasoner().getDataPropertyDomains(asOWLDataProperty(),false).getFlattened())
				removeAxiom(getOWLDataFactory().getOWLDataPropertyDomainAxiom(asOWLDataProperty(),c));
			for(IResource r: domain)
				addAxiom(getOWLDataFactory().getOWLDataPropertyDomainAxiom(asOWLDataProperty(),(OWLClassExpression)convertOntologyObject(r)));
		}

	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#setRange(java.lang.Object[])
	 */
	public void setRange(Object[] range) {
		if(isObjectProperty()){
			for(OWLClass c: getOWLReasoner().getObjectPropertyRanges(asOWLObjectProperty(),false).getFlattened())
				removeAxiom(getOWLDataFactory().getOWLObjectPropertyRangeAxiom(asOWLObjectProperty(),c));
			for(Object o: range)
				addAxiom(getOWLDataFactory().getOWLObjectPropertyRangeAxiom(asOWLObjectProperty(),(OWLClassExpression) convertOntologyObject(o)));
		}else{
			for(OWLDataRange r: asOWLDataProperty().getRanges(getDefiningOntologies()))
				removeAxiom(getOWLDataFactory().getOWLDataPropertyRangeAxiom(asOWLDataProperty(),((OWLLiteral)convertOntologyObject(r)).getDatatype()));
			for(Object o: range)
				addAxiom(getOWLDataFactory().getOWLDataPropertyRangeAxiom(asOWLDataProperty(),((OWLLiteral)convertOntologyObject(o)).getDatatype()));
		}
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#isInverseOf(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public boolean isInverseOf(IProperty p) {
		IProperty i = getInverseProperty();
		return (i != null)?i.equals(p):false;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#isTransitive()
	 */
	public boolean isTransitive() {
		if(isObjectProperty())
			return asOWLObjectProperty().isTransitive(getDefiningOntologies());
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#isFunctional()
	 */
	public boolean isFunctional() {
		//return property.isFunctional(getDefiningOntology());
		for(OWLOntology ont: getOWLOntologyManager().getOntologies()){
			if(property.isFunctional(ont))
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#isSymmetric()
	 */
	public boolean isSymmetric() {
		if(isObjectProperty())
			return asOWLObjectProperty().isSymmetric(getDefiningOntologies());
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#getSubProperties()
	 */
	public IProperty[] getSubProperties() {
		if(isDatatypeProperty()){
			return getProperties(getOWLReasoner().
					getSubDataProperties(asOWLDataProperty(),false).getFlattened());
		}else{
			return getProperties(getOWLReasoner().
					getSubObjectProperties(asOWLObjectProperty(),false).getFlattened());
		}
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#getSuperProperties()
	 */
	public IProperty[] getSuperProperties() {
		if(isDatatypeProperty()){
			return getProperties(getOWLReasoner().
					getSuperDataProperties(asOWLDataProperty(),false).getFlattened());
		}else{
			return getProperties(getOWLReasoner().
					getSuperObjectProperties(asOWLObjectProperty(),false).getFlattened());
		}
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#getDirectSubProperties()
	 */
	public IProperty[] getDirectSubProperties() {
		if(isDatatypeProperty()){
			return getProperties(getOWLReasoner().
					getSubDataProperties(asOWLDataProperty(),true).getFlattened());
		}else{
			return getProperties(getOWLReasoner().
					getSubObjectProperties(asOWLObjectProperty(),true).getFlattened());
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#getDirectSuperProperties()
	 */
	public IProperty[] getDirectSuperProperties() {
		if(isDatatypeProperty()){
			return getProperties(getOWLReasoner().
					getSuperDataProperties(asOWLDataProperty(),true).getFlattened());
		}else{
			return getProperties(getOWLReasoner().
					getSuperObjectProperties(asOWLObjectProperty(),true).getFlattened());
		}
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#hasSuperProperty(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public boolean hasSuperProperty(IProperty o){
		return  Arrays.asList(getSuperProperties()).contains(o);
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#hasSubProperty(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public boolean hasSubProperty(IProperty o){
		return  Arrays.asList(getSubProperties()).contains(o);
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#getInverseProperty()
	 */
	public IProperty getInverseProperty() {
		if(isObjectProperty()){
			// this just returns an anonymous inverse property expression
			//return (IProperty) convertOWLObject(asOWLObjectProperty().getInverseProperty());
			// instead look in ontologies if any inverses were defined
			for(OWLPropertyExpression exp: asOWLObjectProperty().getInverses(getOWLOntologyManager().getOntologies())){
				return (IProperty) convertOWLObject(exp);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#setInverseProperty(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public void setInverseProperty(IProperty p) {
		if(isObjectProperty() && p.isObjectProperty()){
			addAxiom(getOWLDataFactory().
					getOWLInverseObjectPropertiesAxiom(asOWLObjectProperty(),
							(OWLObjectProperty)convertOntologyObject(p)));
		}else{
			throw new IOntologyError("Can't set inverse property for non object properties");
		}
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#addSuperProperty(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public void addSuperProperty(IProperty p) {
		if(p.getPropertyType() != getPropertyType())
			throw new IOntologyError("Can't add super property of different type");
		if(isDatatypeProperty()){
			OWLDataProperty ch = (OWLDataProperty) convertOntologyObject(p);
			addAxiom(getOWLDataFactory().getOWLSubDataPropertyOfAxiom(asOWLDataProperty(),ch));
		}else{
			OWLObjectProperty ch = (OWLObjectProperty) convertOntologyObject(p);
			addAxiom(getOWLDataFactory().getOWLSubObjectPropertyOfAxiom(asOWLObjectProperty(),ch));
		}
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#addSubProperty(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public void addSubProperty(IProperty p) {
		if(p.getPropertyType() != getPropertyType())
			throw new IOntologyError("Can't add sub property of different type");
		if(isDatatypeProperty()){
			OWLDataProperty ch = (OWLDataProperty) convertOntologyObject(p);
			addAxiom(getOWLDataFactory().getOWLSubDataPropertyOfAxiom(ch,asOWLDataProperty()));
		}else{
			OWLObjectProperty ch = (OWLObjectProperty) convertOntologyObject(p);
			addAxiom(getOWLDataFactory().getOWLSubObjectPropertyOfAxiom(ch,asOWLObjectProperty()));
		}
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#removeSuperProperty(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public void removeSuperProperty(IProperty p) {
		if(p.getPropertyType() != getPropertyType())
			throw new IOntologyError("Can't add super property of different type");
		if(isDatatypeProperty()){
			OWLDataProperty ch = (OWLDataProperty) convertOntologyObject(p);
			removeAxiom(getOWLDataFactory().getOWLSubDataPropertyOfAxiom(asOWLDataProperty(),ch));
		}else{
			OWLObjectProperty ch = (OWLObjectProperty) convertOntologyObject(p);
			removeAxiom(getOWLDataFactory().getOWLSubObjectPropertyOfAxiom(asOWLObjectProperty(),ch));
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#removeSubProperty(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public void removeSubProperty(IProperty p) {
		if(p.getPropertyType() != getPropertyType())
			throw new IOntologyError("Can't add sub property of different type");
		if(isDatatypeProperty()){
			OWLDataProperty ch = (OWLDataProperty) convertOntologyObject(p);
			removeAxiom(getOWLDataFactory().getOWLSubDataPropertyOfAxiom(ch,asOWLDataProperty()));
		}else{
			OWLObjectProperty ch = (OWLObjectProperty) convertOntologyObject(p);
			removeAxiom(getOWLDataFactory().getOWLSubObjectPropertyOfAxiom(ch,asOWLObjectProperty()));
		}

	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#setTransitive(boolean)
	 */
	public void setTransitive(boolean b) {
		if(isObjectProperty()){
			OWLAxiom a = getOWLDataFactory().getOWLTransitiveObjectPropertyAxiom(asOWLObjectProperty()); 
			if(b)
				addAxiom(a);
			else
				removeAxiom(a);
		}else{
			throw new IOntologyError("Can't set non-object property as transitive");
		}
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#setFunctional(boolean)
	 */
	public void setFunctional(boolean b) {
		if(isObjectProperty()){
			OWLAxiom a = getOWLDataFactory().getOWLFunctionalObjectPropertyAxiom(asOWLObjectProperty());
			if(b)
				addAxiom(a);
			else
				removeAxiom(a);
		}else{
			OWLAxiom a = getOWLDataFactory().getOWLFunctionalDataPropertyAxiom(asOWLDataProperty());
			if(b)
				addAxiom(a);
			else
				removeAxiom(a);
		}
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#setSymmetric(boolean)
	 */
	public void setSymmetric(boolean b) {
		if(isObjectProperty()){
			OWLAxiom a = getOWLDataFactory().getOWLSymmetricObjectPropertyAxiom(asOWLObjectProperty());
			if(b)
				addAxiom(a);
			else
				removeAxiom(a);
		}else{
			throw new IOntologyError("Can't set non-object property as symmetric");
		}
	}

}
