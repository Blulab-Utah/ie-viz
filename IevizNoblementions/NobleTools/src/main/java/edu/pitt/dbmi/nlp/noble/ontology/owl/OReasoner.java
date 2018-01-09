package edu.pitt.dbmi.nlp.noble.ontology.owl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import edu.pitt.dbmi.nlp.noble.ontology.IClass;
import edu.pitt.dbmi.nlp.noble.ontology.IInstance;
import edu.pitt.dbmi.nlp.noble.ontology.IOntology;
import edu.pitt.dbmi.nlp.noble.ontology.IOntologyException;
import edu.pitt.dbmi.nlp.noble.ontology.IReasoner;

/**
 * wraps reasoner for this ontolgoy.
 *
 * @author tseytlin
 */
public class OReasoner implements IReasoner {
	private OOntology ontology;
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	/**
	 * Instantiates a new o reasoner.
	 *
	 * @param ont the ont
	 */
	public OReasoner(OOntology ont){
		this.ontology = ont;
	}
	
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IReasoner#initialize()
	 */
	public void initialize() throws IOntologyException {
		// TODO Auto-generated method stub

	}

	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IReasoner#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IReasoner#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IReasoner#removePropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);

	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IReasoner#getOntology()
	 */
	public IOntology getOntology() {
		return ontology;
	}

	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IReasoner#computeInferredHierarchy()
	 */
	public IResult[] computeInferredHierarchy() {
		// TODO Auto-generated method stub
		return null;
	}

	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IReasoner#computeInferredTypes()
	 */
	public IResult[] computeInferredTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IReasoner#computeInferredHierarchy(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public IResult[] computeInferredHierarchy(IClass cls) {
		// TODO Auto-generated method stub
		return null;
	}

	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IReasoner#computeInferredTypes(edu.pitt.dbmi.nlp.noble.ontology.IInstance)
	 */
	public IResult[] computeInferredTypes(IInstance inst) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IReasoner#getSuperClasses(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public IClass[] getSuperClasses(IClass cls) {
		return cls.getSuperClasses();
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IReasoner#getDirectSubClasses(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public IClass[] getDirectSubClasses(IClass cls) {
		return cls.getDirectSubClasses();
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IReasoner#getDirectSuperClasses(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public IClass[] getDirectSuperClasses(IClass cls) {
		return cls.getDirectSuperClasses();
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IReasoner#getSubClasses(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public IClass[] getSubClasses(IClass cls) {
		return cls.getSubClasses();
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IReasoner#getEquivalentClasses(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public IClass[] getEquivalentClasses(IClass cls) {
		return cls.getEquivalentClasses();
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IReasoner#getInstances(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public IInstance[] getInstances(IClass cls) {
		return cls.getInstances();
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IReasoner#getTypes(edu.pitt.dbmi.nlp.noble.ontology.IInstance)
	 */
	public IClass[] getTypes(IInstance inst) {
		return inst.getTypes();
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IReasoner#getDirectTypes(edu.pitt.dbmi.nlp.noble.ontology.IInstance)
	 */
	public IClass[] getDirectTypes(IInstance inst) {
		return inst.getDirectTypes();
	}

}
