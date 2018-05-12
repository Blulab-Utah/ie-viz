package edu.pitt.dbmi.nlp.noble.ontology;

import java.beans.PropertyChangeListener;

/**
 * this class wraps the functions of a reasoner.
 *
 * @author tseytlin
 */
public interface IReasoner  {
	public static final String TASK_COMPLETED = "TASK_COMPLETED";
	public static final String TASK_FAILED = "TASK_FAILED";
	
	
	/**
	 * initialize this reasoner.
	 *
	 * @throws IOntologyException the i ontology exception
	 */
	public void initialize() throws IOntologyException;
	
	/**
	 * dispose of this reasoner instance.
	 */
	public void dispose();
	
	
	/**
	 * add property changed listener.
	 *
	 * @param listener the listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener);
	
	/**
	 * remove property changed listener.
	 *
	 * @param listener the listener
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener);
	
	
	/**
	 * get ontology this reasoner is using.
	 *
	 * @return the ontology
	 */
	public IOntology getOntology();
	
	/**
	 * classify the entire ontology, For each named class in the ontology, 
	 * this method queries the reasoner for the consistency of the class, 
	 * its inferred super classes and its inferred equivalent classes.
	 * Assert the result
	 *
	 * @return the i result[]
	 */
	public IResult [] computeInferredHierarchy();
	
	/**
	 * infer types for all individuals in ontology. Assert the result
	 *
	 * @return the i result[]
	 */
	public IResult [] computeInferredTypes();
	
	
	/**
	 * classify the entire ontology, For each named class in the ontology, 
	 * this method queries the reasoner for the consistency of the class, 
	 * its inferred super classes and its inferred equivalent classes.
	 * Assert the result
	 *
	 * @param cls the cls
	 * @return the i result[]
	 */
	public IResult [] computeInferredHierarchy(IClass cls);
	
	/**
	 * infer types for all individuals in ontology. Assert the result
	 *
	 * @param inst the inst
	 * @return the i result[]
	 */
	public IResult [] computeInferredTypes(IInstance inst);
	
	
	/**
	 * get inferrred super classes.
	 *
	 * @param cls the cls
	 * @return the super classes
	 */
	public IClass [] getSuperClasses(IClass cls);
	
	/**
	 * get inferrred direct sub classes.
	 *
	 * @param cls the cls
	 * @return the direct sub classes
	 */
	public IClass [] getDirectSubClasses(IClass cls);
	
	
	/**
	 * get inferrred direct super classes.
	 *
	 * @param cls the cls
	 * @return the direct super classes
	 */
	public IClass [] getDirectSuperClasses(IClass cls);
	
	/**
	 * get inferrred sub classes.
	 *
	 * @param cls the cls
	 * @return the sub classes
	 */
	public IClass [] getSubClasses(IClass cls);
	
	
	/**
	 * get inferrred sub classes.
	 *
	 * @param cls the cls
	 * @return the equivalent classes
	 */
	public IClass [] getEquivalentClasses(IClass cls);
	
	/**
	 * get inferred instances.
	 *
	 * @param cls the cls
	 * @return the instances
	 */
	public IInstance [] getInstances(IClass cls);
	
	/**
	 * get inferred types.
	 *
	 * @param inst the inst
	 * @return the types
	 */
	public IClass [] getTypes(IInstance inst);
	
	/**
	 * get inferred types.
	 *
	 * @param inst the inst
	 * @return the direct types
	 */
	public IClass [] getDirectTypes(IInstance inst);
	
	
	/**
	 * this class represents a TODO action item that was produced
	 * by a reasoner.
	 *
	 * @author tseytlin
	 */
	public static interface IResult{
		public static final int ADD_SUPERCLASS = 11;
		public static final int REMOVE_SUPERCLASS = 21;
		public static final int ADD_SUBCLASS = 12;
		public static final int REMOVE_SUBCLASS = 22;
		public static final int SUPERCLASS = 1;
		public static final int SUBCLASS = 2;
		public static final int ADD = 10;
		public static final int REMOVE = 20;
		
		
		/**
		 * assert this action item.
		 */
		public void assertResult();
		
		/**
		 * get a resource that this operation was on.
		 *
		 * @return the operand
		 */
		public IResource getOperand();
		
		/**
		 * get operation type.
		 *
		 * @return the operation
		 */
		public int getOperation();
		
		/**
		 * get parameter of this operation.
		 *
		 * @return the parameter
		 */
		public IResource getParameter();
	}
	
}
