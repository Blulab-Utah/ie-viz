package edu.pitt.dbmi.nlp.noble.ontology;

import edu.pitt.dbmi.nlp.noble.terminology.Concept;

/**
 * This describes an ontology class.
 *
 * @author tseytlin
 */
public interface IClass extends IResource {
	
	/**
	 * add subclass .
	 *
	 * @param child the child
	 */
	public void addSubClass(IClass child);
	
	/**
	 * add subclass .
	 *
	 * @param parent the parent
	 */
	public void addSuperClass(IClass parent);
	
	
	/**
	 * add disjoint .
	 *
	 * @param a the a
	 */
	public void addDisjointClass(IClass a);
	
	/**
	 * add disjoint .
	 *
	 * @param a the a
	 */
	public void addEquivalentClass(IClass a);
	
	
	/**
	 * create instance of this class.
	 *
	 * @param name the name
	 * @return IInstance that was created
	 */
	public IInstance createInstance(String name);
	
	
	/**
	 * create instance of this class
	 * auto-generate instance name.
	 *
	 * @return IInstance that was created
	 */
	public IInstance createInstance();
	
	
	/**
	 * create a class that is a child of this.
	 *
	 * @param name the name
	 * @return the i class
	 */
	public IClass createSubClass(String name);
	
	
	/**
	 * add necessary restriction to this class.
	 *
	 * @param restriction the restriction
	 */
	public void addNecessaryRestriction(IRestriction restriction);
	

	/**
	 * remove restriction to this class.
	 *
	 * @param restriction the restriction
	 */
	public void removeNecessaryRestriction(IRestriction restriction);
	
	/**
	 * add equivalent Necessary and Sufficient restriction to this class.
	 *
	 * @param restriction the restriction
	 */
	public void addEquivalentRestriction(IRestriction restriction);
	

	/**
	 * remove restriction to this class.
	 *
	 * @param restriction the restriction
	 */
	public void removeEquivalentRestriction(IRestriction restriction);
	
	
	/**
	 * add necessary restriction to this class.
	 *
	 * @param restriction the restriction
	 */
	public void addNecessaryRestriction(ILogicExpression restriction);
	

	/**
	 * remove restriction to this class.
	 *
	 * @param restriction the restriction
	 */
	public void removeNecessaryRestriction(ILogicExpression restriction);
	
	/**
	 * add equivalent Necessary and Sufficient restriction to this class.
	 *
	 * @param restriction the restriction
	 */
	public void addEquivalentRestriction(ILogicExpression restriction);
	

	/**
	 * remove restriction to this class.
	 *
	 * @param restriction the restriction
	 */
	public void removeEquivalentRestriction(ILogicExpression restriction);
	
	/**
	 * remove subclass .
	 *
	 * @param child the child
	 */
	public void removeSubClass(IClass child);
	
	/**
	 * remove subclass .
	 *
	 * @param parent the parent
	 */
	public void removeSuperClass(IClass parent);
	
	
	/**
	 * remove disjoint .
	 *
	 * @param a the a
	 */
	public void removeDisjointClass(IClass a);
	
	

	/**
	 * remove equivalent .
	 *
	 * @param a the a
	 */
	public void removeEquivalentClass(IClass a);
	
	/**
	 * Get all parent classes of a given class.
	 *
	 * @return parents
	 */
	public IClass [] getSuperClasses();

	
	/**
	 * Get all children classes of a given class.
	 *
	 * @return parents
	 */
	public IClass [] getSubClasses();

	
	/**
	 * Get direct parent classes of a given class.
	 *
	 * @return parents
	 */
	public IClass [] getDirectSuperClasses();

	
	/**
	 * Get direct children classes of a given class.
	 *
	 * @return parents
	 */
	public IClass [] getDirectSubClasses();
	

	/**
	 * get all instances of a given class.
	 *
	 * @return the instances
	 */
	public IInstance [] getInstances();
	
	/**
	 * get direct instances of a given class.
	 *
	 * @return the direct instances
	 */
	public IInstance [] getDirectInstances();
	
	
	/**
	 * get all restrictions associated with a given class.
	 *
	 * @return the equivalent restrictions
	 */
	public ILogicExpression getEquivalentRestrictions();
	
	/**
	 * get dircect necessary  restrictions associated with a given class.
	 *
	 * @return the direct necessary restrictions
	 */
	public ILogicExpression getDirectNecessaryRestrictions();
	
	/**
	 * get all restrictions on given property.
	 *
	 * @param p the p
	 * @return the restrictions
	 */
	public IRestriction [] getRestrictions(IProperty p);
	
	/**
	 * get all restrictions associated with a class either equivalent or necessary.
	 * @return the restrictions
	 */
	public IRestriction [] getRestrictions();
	
	/**
	 * get all necessary  restrictions associated with a given class
	 * including inferred restrictions.
	 *
	 * @return the necessary restrictions
	 */
	public ILogicExpression getNecessaryRestrictions();
	
	
	/**
	 * is parent a super class of child.
	 *
	 * @param parent the parent
	 * @return true, if successful
	 */
	public boolean hasSuperClass(IClass parent);
	
	
	/**
	 * is child a sub class of parent.
	 *
	 * @param child the child
	 * @return true, if successful
	 */
	public boolean hasSubClass(IClass child);
	
	
	/**
	 * is child a sub class of parent.
	 *
	 * @param child the child
	 * @return true, if successful
	 */
	public boolean hasEquivalentClass(IClass child);
	
	/**
	 * is parent a direct super class of child.
	 *
	 * @param parent the parent
	 * @return true, if successful
	 */
	public boolean hasDirectSuperClass(IClass parent);
	
	
	/**
	 * is child a direct sub class of parent.
	 *
	 * @param child the child
	 * @return true, if successful
	 */
	public boolean hasDirectSubClass(IClass child);
	
	
	/**
	 * get disjoint classes of a given class.
	 *
	 * @return the disjoint classes
	 */
	public IClass [] getDisjointClasses();
	
	
	/**
	 * get equivalent classes of a given class.
	 *
	 * @return the equivalent classes
	 */
	public IClass [] getEquivalentClasses();
	
	/**
	 * are two classes disjoint.
	 *
	 * @param a the a
	 * @return true, if successful
	 */
	public boolean hasDisjointClass(IClass a);
	
	
	/**
	 * is this class anonymous.
	 *
	 * @return true, if is anonymous
	 */
	public boolean isAnonymous();


	/**
	 * if possible get a Concept object for a given class.
	 *
	 * @return the concept
	 */
	public Concept getConcept();
	
	
	/**
	 * evaluate if given object can satisfy this class
	 * if parameter is a Class, then return true if it is child or equivalent class
	 * if parameter is an Instance, then return true if this class is its Type.
	 *
	 * @param obj the obj
	 * @return true, if successful
	 */
	public boolean evaluate(Object obj);
	
}
