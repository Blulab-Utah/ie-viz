package edu.pitt.dbmi.nlp.noble.ontology.owl;


import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLRestriction;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;

import edu.pitt.dbmi.nlp.noble.ontology.IClass;
import edu.pitt.dbmi.nlp.noble.ontology.IInstance;
import edu.pitt.dbmi.nlp.noble.ontology.ILogicExpression;
import edu.pitt.dbmi.nlp.noble.ontology.IProperty;
import edu.pitt.dbmi.nlp.noble.ontology.IRestriction;
import edu.pitt.dbmi.nlp.noble.ontology.LogicExpression;
import edu.pitt.dbmi.nlp.noble.terminology.Concept;


/**
 * The Class OClass.
 */
public class OClass extends OResource implements IClass{
	private OWLClassExpression cls;
	private transient Concept concept;
	
	/**
	 * Instantiates a new o class.
	 *
	 * @param obj the obj
	 * @param ont the ont
	 */
	protected OClass(OWLClassExpression obj,OOntology ont){
		super(obj,ont);
		cls = obj;
	}

	/**
	 * Gets the OWL class.
	 *
	 * @return the OWL class
	 */
	public OWLClass getOWLClass(){
		return (OWLClass) cls;
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#addSubClass(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public void addSubClass(IClass child) {
		OWLClass ch = (OWLClass) convertOntologyObject(child);
		addAxiom(getOWLDataFactory().getOWLSubClassOfAxiom(ch,cls));
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#addSuperClass(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public void addSuperClass(IClass parent) {
		OWLClass pr = (OWLClass) convertOntologyObject(parent);
		addAxiom(getOWLDataFactory().getOWLSubClassOfAxiom(cls,pr));
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#addDisjointClass(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public void addDisjointClass(IClass a) {
		OWLClass ca = (OWLClass) convertOntologyObject(a);
		addAxiom(getOWLDataFactory().getOWLDisjointClassesAxiom(cls,ca));
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#addEquivalentClass(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public void addEquivalentClass(IClass a) {
		OWLClass ca = (OWLClass) convertOntologyObject(a);
		addAxiom(getOWLDataFactory().getOWLEquivalentClassesAxiom(cls,ca));
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#createInstance(java.lang.String)
	 */
	public IInstance createInstance(String name) {
		OWLDataFactory dataFactory = getOWLDataFactory();
		OWLIndividual ind = dataFactory.getOWLNamedIndividual(IRI.create(getOntology().getNameSpace()+name));
		addAxiom(dataFactory.getOWLClassAssertionAxiom(cls,ind));
		return (IInstance)convertOWLObject(ind);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#createInstance()
	 */
	public IInstance createInstance() {
		/*OWLDataFactory dataFactory = getOWLDataFactory();
		OWLIndividual ind = dataFactory.getOWLAnonymousIndividual();
		addAxiom(dataFactory.getOWLClassAssertionAxiom(cls,ind));
		return (IInstance)convertOWLObject(ind);*/
		return createInstance("Instance-"+System.currentTimeMillis()+"-"+((int)(Math.random()*1000)));
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#createSubClass(java.lang.String)
	 */
	public IClass createSubClass(String name) {
		OWLDataFactory dataFactory = getOWLDataFactory();
		OWLClass ch = dataFactory.getOWLClass(IRI.create(getOntology().getNameSpace()+name));
		addAxiom(getOWLDataFactory().getOWLSubClassOfAxiom(ch,cls));
		return (IClass) convertOWLObject(ch);
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#addNecessaryRestriction(edu.pitt.dbmi.nlp.noble.ontology.IRestriction)
	 */
	public void addNecessaryRestriction(IRestriction restriction) {
		OWLRestriction r = ((ORestriction)restriction).getOWLRestriction();
		OWLSubClassOfAxiom ax = getOWLDataFactory().getOWLSubClassOfAxiom(getOWLClass(),r);
	    addAxiom(ax);
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#addNecessaryRestriction(edu.pitt.dbmi.nlp.noble.ontology.ILogicExpression)
	 */
	public void addNecessaryRestriction(ILogicExpression exp) {
		OWLSubClassOfAxiom ax = getOWLDataFactory().getOWLSubClassOfAxiom(getOWLClass(),(OWLClassExpression)convertOntologyObject(exp));
	    addAxiom(ax);
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#removeNecessaryRestriction(edu.pitt.dbmi.nlp.noble.ontology.IRestriction)
	 */
	public void removeNecessaryRestriction(IRestriction restriction) {
		OWLRestriction r = ((ORestriction)restriction).getOWLRestriction();
		OWLSubClassOfAxiom ax = getOWLDataFactory().getOWLSubClassOfAxiom(getOWLClass(),r);
	    removeAxiom(ax);
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#removeNecessaryRestriction(edu.pitt.dbmi.nlp.noble.ontology.ILogicExpression)
	 */
	public void removeNecessaryRestriction(ILogicExpression exp) {
		OWLSubClassOfAxiom ax = getOWLDataFactory().getOWLSubClassOfAxiom(getOWLClass(),(OWLClassExpression)convertOntologyObject(exp));
	    removeAxiom(ax);
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#addEquivalentRestriction(edu.pitt.dbmi.nlp.noble.ontology.IRestriction)
	 */
	public void addEquivalentRestriction(IRestriction restriction) {
		OWLRestriction r = ((ORestriction)restriction).getOWLRestriction();
		OWLEquivalentClassesAxiom ax = getOWLDataFactory().getOWLEquivalentClassesAxiom(getOWLClass(),r);
	    addAxiom(ax);
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#removeEquivalentRestriction(edu.pitt.dbmi.nlp.noble.ontology.IRestriction)
	 */
	public void removeEquivalentRestriction(IRestriction restriction) {
		OWLRestriction r = ((ORestriction)restriction).getOWLRestriction();
		OWLEquivalentClassesAxiom ax = getOWLDataFactory().getOWLEquivalentClassesAxiom(getOWLClass(),r);
	    removeAxiom(ax);
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#addEquivalentRestriction(edu.pitt.dbmi.nlp.noble.ontology.ILogicExpression)
	 */
	public void addEquivalentRestriction(ILogicExpression exp) {
		OWLEquivalentClassesAxiom ax = getOWLDataFactory().getOWLEquivalentClassesAxiom(getOWLClass(),(OWLClassExpression)convertOntologyObject(exp));
	    addAxiom(ax);
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#removeEquivalentRestriction(edu.pitt.dbmi.nlp.noble.ontology.ILogicExpression)
	 */
	public void removeEquivalentRestriction(ILogicExpression exp) {
		OWLEquivalentClassesAxiom ax = getOWLDataFactory().getOWLEquivalentClassesAxiom(getOWLClass(),(OWLClassExpression)convertOntologyObject(exp));
	    removeAxiom(ax);
	}
	
	
	/**
	 * get equivalent restrictions for this class.
	 *
	 * @return the equivalent restrictions
	 */
	public ILogicExpression getEquivalentRestrictions() {
		ILogicExpression exp = getOntology().createLogicExpression();
		exp.setExpressionType(ILogicExpression.AND);
		for(OWLOntology ont : getOWLOntologyManager().getOntologies()){
			for(OWLEquivalentClassesAxiom ax: ont.getEquivalentClassesAxioms(getOWLClass())){ //getDefiningOntology()
				for(OWLClassExpression ex: ax.getClassExpressions()){
					if(ex.isAnonymous()){
						exp.add(convertOWLObject(ex));
					}
				}
			}
		}
		//return (exp.size() == 1 && exp.get(0) instanceof ILogicExpression)?(ILogicExpression)exp.get(0):exp;
		return exp;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#getDirectNecessaryRestrictions()
	 */
	public ILogicExpression getDirectNecessaryRestrictions() {
		ILogicExpression exp = getOntology().createLogicExpression();
		exp.setExpressionType(ILogicExpression.AND);
	    for(OWLOntology ont : getOWLOntologyManager().getOntologies()){
			for (OWLSubClassOfAxiom ax : ont.getSubClassAxiomsForSubClass(getOWLClass())) { // getDefiningOntology()
		    	OWLClassExpression ex = ax.getSuperClass();
				if(ex.isAnonymous()){
					exp.add(convertOWLObject(ex));
				}
				
			}
	    }
		//return (exp.size() == 1 && exp.get(0) instanceof ILogicExpression)?(ILogicExpression)exp.get(0):exp;
		return exp;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#getRestrictions(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public IRestriction[] getRestrictions(IProperty p) {
		List<IRestriction> list = new ArrayList<IRestriction>();
		for(List l: new List [] {getEquivalentRestrictions(),getNecessaryRestrictions()}){
			addRestriction(p, l,list);
		}
		return list.toArray(new IRestriction [0]);
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#getRestrictions(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public IRestriction[] getRestrictions() {
		return getRestrictions(null);
	}
	
	/**
	 * Adds the restriction.
	 *
	 * @param p the p
	 * @param source the source
	 * @param target the target
	 */
	private void addRestriction(IProperty p, List source, List<IRestriction> target){
		for(Object o: source){
			if(o instanceof IRestriction){
				IRestriction r = (IRestriction)o;
				if(p == null || r.getProperty().equals(p) || r.getProperty().hasSuperProperty(p))
					target.add(r);
			}else if(o instanceof List){
				addRestriction(p, (List) o, target);
			}
		}
	}

	/**
	 * get necessary restrictions.
	 *
	 * @return the necessary restrictions
	 */
	public ILogicExpression getNecessaryRestrictions() {
		ILogicExpression exp = new LogicExpression(ILogicExpression.AND);
		for(Object o: getDirectNecessaryRestrictions())
			exp.add(o);
		for(IClass parent: getSuperClasses()){
			for(Object o: parent.getDirectNecessaryRestrictions())
				exp.add(o);
		}
		return exp;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#removeSubClass(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public void removeSubClass(IClass child) {
		OWLClass ch = (OWLClass) convertOntologyObject(child);
		removeAxiom(getOWLDataFactory().getOWLSubClassOfAxiom(ch,cls));
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#removeSuperClass(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public void removeSuperClass(IClass parent) {
		OWLClass pr = (OWLClass) convertOntologyObject(parent);
		removeAxiom(getOWLDataFactory().getOWLSubClassOfAxiom(cls,pr));
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#removeDisjointClass(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public void removeDisjointClass(IClass a) {
		OWLClass ca = (OWLClass) convertOntologyObject(a);
		removeAxiom(getOWLDataFactory().getOWLDisjointClassesAxiom(cls,ca));
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#removeEquivalentClass(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public void removeEquivalentClass(IClass a) {
		OWLClass ca = (OWLClass) convertOntologyObject(a);
		removeAxiom(getOWLDataFactory().getOWLEquivalentClassesAxiom(cls,ca));
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#getSuperClasses()
	 */
	public IClass[] getSuperClasses() {
		NodeSet<OWLClass> sub = getOWLReasoner().getSuperClasses(cls, false);
		return getClasses(sub.getFlattened());
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#getSubClasses()
	 */
	public IClass[] getSubClasses() {
		NodeSet<OWLClass> sub = getOWLReasoner().getSubClasses(cls, false);
		return getClasses(sub.getFlattened());
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#getDirectSuperClasses()
	 */
	public IClass[] getDirectSuperClasses() {
		NodeSet<OWLClass> sub = getOWLReasoner().getSuperClasses(cls, true);
		return getClasses(sub.getFlattened());
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#getDirectSubClasses()
	 */
	public IClass[] getDirectSubClasses() {
		NodeSet<OWLClass> sub = getOWLReasoner().getSubClasses(cls, true);
		return getClasses(sub.getFlattened());
	}

	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#getInstances()
	 */
	public IInstance[] getInstances() {
		NodeSet<OWLNamedIndividual> sub = getOWLReasoner().getInstances(cls,false);
		return getInstances(sub.getFlattened());
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#getDirectInstances()
	 */
	public IInstance[] getDirectInstances() {
		NodeSet<OWLNamedIndividual> sub = getOWLReasoner().getInstances(cls,true);
		return getInstances(sub.getFlattened());
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#hasSuperClass(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public boolean hasSuperClass(IClass parent) {
		return getOWLReasoner().getSuperClasses(cls,false).containsEntity((OWLClass)convertOntologyObject(parent));
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#hasSubClass(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public boolean hasSubClass(IClass child) {
		return getOWLReasoner().getSubClasses(cls,false).containsEntity((OWLClass)convertOntologyObject(child));
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#hasEquivalentClass(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public boolean hasEquivalentClass(IClass child) {
		return getOWLReasoner().getEquivalentClasses(cls).contains((OWLClass)convertOntologyObject(child));
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#hasDirectSuperClass(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public boolean hasDirectSuperClass(IClass parent) {
		return getOWLReasoner().getSuperClasses(cls, true).containsEntity((OWLClass)convertOntologyObject(parent));
		//return hasClass(sub.getFlattened(),parent);
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#hasDirectSubClass(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public boolean hasDirectSubClass(IClass child) {
		return getOWLReasoner().getSubClasses(cls, true).containsEntity((OWLClass)convertOntologyObject(child));
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#getDisjointClasses()
	 */
	public IClass[] getDisjointClasses() {
		NodeSet<OWLClass> sub = getOWLReasoner().getDisjointClasses(cls);
		return getClasses(sub.getFlattened());
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#getEquivalentClasses()
	 */
	public IClass[] getEquivalentClasses() {
		Node<OWLClass> sub = getOWLReasoner().getEquivalentClasses(cls);
		return getClasses(sub.getEntities());
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#hasDisjointClass(edu.pitt.dbmi.nlp.noble.ontology.IClass)
	 */
	public boolean hasDisjointClass(IClass a) {
		return getOWLReasoner().getDisjointClasses(cls).containsEntity((OWLClass)convertOntologyObject(a));
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#isAnonymous()
	 */
	public boolean isAnonymous() {
		return cls.isAnonymous();
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#getConcept()
	 */
	public Concept getConcept() {
		if(concept == null){
			concept = getOntology().getConcept(this);
		}
		return concept;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IClass#evaluate(java.lang.Object)
	 */
	public boolean evaluate(Object obj) {
		if(obj instanceof IInstance)
			return ((IInstance)obj).hasType(this);
		else if(obj instanceof IClass)
			return equals(obj) || hasSubClass((IClass)obj);
		return false;
	}
	
}
