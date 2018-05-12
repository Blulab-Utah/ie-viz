package edu.pitt.dbmi.nlp.noble.ontology.owl;

import edu.pitt.dbmi.nlp.noble.ontology.IClass;
import edu.pitt.dbmi.nlp.noble.ontology.IInstance;
import edu.pitt.dbmi.nlp.noble.ontology.IProperty;
import edu.pitt.dbmi.nlp.noble.ontology.LogicExpression;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The Class OInstance.
 */
public class OInstance extends OResource implements IInstance {
	private OWLIndividual individual;
	
	/**
	 * Instantiates a new o instance.
	 *
	 * @param obj the obj
	 * @param ont the ont
	 */
	protected OInstance(OWLObject obj, OOntology ont) {
		super(obj,ont);
		individual = (OWLIndividual) obj;
	}


	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.owl.OResource#getProperties()
	 */
	public IProperty[] getProperties() {
		List<IProperty> props = new ArrayList<IProperty>();
		Collections.addAll(props,super.getProperties());
		for(OWLOntology ont: getOWLOntologyManager().getOntologies()){
			for(OWLDataPropertyExpression e: getOWLIndividual().getDataPropertyValues(ont).keySet()){
				props.add((IProperty)convertOWLObject(e));
			}
			for(OWLObjectPropertyExpression e: getOWLIndividual().getObjectPropertyValues(ont).keySet()){
				props.add((IProperty)convertOWLObject(e));
			}
		}
		return props.toArray(new IProperty [0]);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.owl.OResource#getPropertyValue(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public Object getPropertyValue(IProperty prop) {
		Object [] o = getPropertyValues(prop);
		return o.length > 0?o[0]:null;
	}

	/**
	 * get property values for a given property (doesn't include sub properties)
	 * @param prop - property
	 * @return list of objects
	 */
	public Object[] getPropertyValues(IProperty prop) {
		return getPropertyValues(prop, false);
	}
	
	
	/**
	 * get property values for a given property
	 * @param prop - property
	 * @param includeSubProperties - if true, return values for all sub properties
	 * @return list of objects
	 */
	public Object[] getPropertyValues(IProperty prop, boolean includeSubProperties) {
		if(prop.isAnnotationProperty())
			return super.getPropertyValues(prop);
		else if(prop.isDatatypeProperty()){
			LogicExpression list = new LogicExpression(LogicExpression.AND);
			if(getOWLIndividual().isNamed()){
				for(OWLLiteral l: getOWLReasoner().getDataPropertyValues((OWLNamedIndividual)getOWLIndividual(),(OWLDataProperty)convertOntologyObject(prop))){
					list.add(convertOWLObject(l));
				}
				// optionally include sub properties
				if(includeSubProperties){
					for(IProperty p: prop.getSubProperties()){
						for(OWLLiteral l: getOWLReasoner().getDataPropertyValues((OWLNamedIndividual)getOWLIndividual(),(OWLDataProperty)convertOntologyObject(p))){
							list.add(convertOWLObject(l));
						}
					}
				}
			}
			return list.toArray();
		}else if(prop.isObjectProperty()){
			LogicExpression list = new LogicExpression(LogicExpression.AND);
			if(getOWLIndividual().isNamed()){
				for(OWLIndividual l: getOWLReasoner().getObjectPropertyValues((OWLNamedIndividual)getOWLIndividual(),
						(OWLObjectPropertyExpression)convertOntologyObject(prop)).getFlattened()){
					list.add(convertOWLObject(l));
				}
				// optionally include sub properties
				if(includeSubProperties){
					for(IProperty p: prop.getSubProperties()){
						for(OWLIndividual l: getOWLReasoner().getObjectPropertyValues((OWLNamedIndividual)getOWLIndividual(),
								(OWLObjectPropertyExpression)convertOntologyObject(p)).getFlattened()){
							list.add(convertOWLObject(l));
						}
					}
				}
			}
			return list.toArray();
		}
		return new Object [0];
	}


	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.owl.OResource#addPropertyValue(edu.pitt.dbmi.nlp.noble.ontology.IProperty, java.lang.Object)
	 */
	public void addPropertyValue(IProperty prop, Object value) {
		OWLDataFactory df = getOWLDataFactory();
		OWLIndividual subj = getOWLIndividual();
		if(prop.isAnnotationProperty()){
			super.addPropertyValue(prop, value);
		}else if(prop.isDatatypeProperty()){
			OWLDataProperty dp = (OWLDataProperty)convertOntologyObject(prop);
			OWLLiteral dl = (OWLLiteral)convertOntologyObject(value);
			addAxiom(df.getOWLDataPropertyAssertionAxiom(dp,subj,dl));
		}else if(prop.isObjectProperty()){
			OWLObjectProperty op = (OWLObjectProperty)convertOntologyObject(prop);
			OWLIndividual oo = (OWLIndividual)convertOntologyObject(value);
			addAxiom(df.getOWLObjectPropertyAssertionAxiom(op,subj,oo));
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.owl.OResource#setPropertyValue(edu.pitt.dbmi.nlp.noble.ontology.IProperty, java.lang.Object)
	 */
	public void setPropertyValue(IProperty prop, Object value) {
		removePropertyValues(prop);
		addPropertyValue(prop, value);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.owl.OResource#setPropertyValues(edu.pitt.dbmi.nlp.noble.ontology.IProperty, java.lang.Object[])
	 */
	public void setPropertyValues(IProperty prop, Object[] values) {
		removePropertyValues(prop);
		for(Object o: values)
			addPropertyValue(prop, o);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.owl.OResource#removePropertyValues(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public void removePropertyValues(IProperty prop) {
		if(prop.isAnnotationProperty()){
			super.removePropertyValues(prop);
		}else{
			for(Object o: getPropertyValues(prop)){
				removePropertyValue(prop,o);
			}
		}
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.owl.OResource#removePropertyValue(edu.pitt.dbmi.nlp.noble.ontology.IProperty, java.lang.Object)
	 */
	public void removePropertyValue(IProperty prop, Object value) {
		OWLDataFactory df = getOWLDataFactory();
		OWLIndividual subj = getOWLIndividual();
		if(prop.isAnnotationProperty()){
			super.removePropertyValue(prop, value);
		}else if(prop.isDatatypeProperty()){
			OWLDataProperty dp = (OWLDataProperty)convertOntologyObject(prop);
			OWLLiteral dl = (OWLLiteral)convertOntologyObject(value);
			removeAxiom(df.getOWLDataPropertyAssertionAxiom(dp,subj,dl));
		}else if(prop.isObjectProperty()){
			OWLObjectProperty op = (OWLObjectProperty)convertOntologyObject(prop);
			OWLIndividual oo = (OWLIndividual)convertOntologyObject(value);
			removeAxiom(df.getOWLObjectPropertyAssertionAxiom(op,subj,oo));
		}
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.owl.OResource#removePropertyValues()
	 */
	public void removePropertyValues() {
		for(IProperty p: getProperties()){
			removePropertyValues(p);
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.owl.OResource#hasPropetyValue(edu.pitt.dbmi.nlp.noble.ontology.IProperty, java.lang.Object)
	 */
	public boolean hasPropetyValue(IProperty p, Object value) {
		if(p.isAnnotationProperty())
			return super.hasPropetyValue(p, value);
		if(p.isDatatypeProperty()){
			for(OWLOntology ont: getOWLOntologyManager().getOntologies()){
				if(getOWLIndividual().hasDataPropertyValue((OWLDataPropertyExpression)convertOntologyObject(p),
					(OWLLiteral)convertOntologyObject(value), ont))
				return true;
			}
			return false;
		}
		if(p.isObjectProperty()){
			for(OWLOntology ont: getOWLOntologyManager().getOntologies()){
				if(getOWLIndividual().hasObjectPropertyValue((OWLObjectPropertyExpression)convertOntologyObject(p),
					(OWLIndividual)convertOntologyObject(value),ont)){
					return true;
				}
			}
			return false;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IInstance#addType(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public void addType(IClass cls) {
		OWLClass pr = (OWLClass) convertOntologyObject(cls);
		addAxiom(getOWLDataFactory().getOWLClassAssertionAxiom(pr,individual));
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IInstance#removeType(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public void removeType(IClass cls) {
		OWLClass pr = (OWLClass) convertOntologyObject(cls);
		removeAxiom(getOWLDataFactory().getOWLClassAssertionAxiom(pr,individual));
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IInstance#getTypes()
	 */
	public IClass[] getTypes() {
		NodeSet<OWLClass> sub = getOWLReasoner().getTypes((OWLNamedIndividual)individual,false);
		return getClasses(sub.getFlattened());
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IInstance#getDirectTypes()
	 */
	public IClass[] getDirectTypes() {
		NodeSet<OWLClass> sub = getOWLReasoner().getTypes((OWLNamedIndividual)individual,true);
		return getClasses(sub.getFlattened());
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IInstance#hasType(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public boolean hasType(IClass cls) {
		NodeSet<OWLClass> sub = getOWLReasoner().getTypes((OWLNamedIndividual)individual,false);
		return sub.containsEntity((OWLClass)convertOntologyObject(cls));
	}

	/**
	 * Gets the OWL individual.
	 *
	 * @return the OWL individual
	 */
	public OWLIndividual getOWLIndividual() {
		return individual;
	}

}
