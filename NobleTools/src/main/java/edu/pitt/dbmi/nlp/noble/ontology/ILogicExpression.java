package edu.pitt.dbmi.nlp.noble.ontology;

import java.util.List;

/**
 * a list of resources that could be either
 * conjunction or disjunction.
 *
 * @author tseytlin
 */
public interface ILogicExpression extends List {
	public static int OR = 3;
	public static int AND = 2;
	public static int NOT = 1;
	public static int EMPTY = 0;
	
	/**
	 * get expression type
	 * AND, OR, NOT
	 * if 0 is returned then expression is just a container 
	 * for a single value ex: (A)  .
	 *
	 * @return the expression type
	 */
	public int getExpressionType();
	
	
	/**
	 * set expression type
	 * AND, OR, NOT
	 * if 0 is returned then expression is just a container 
	 * for a single value ex: (A)  .
	 *
	 * @param type the new expression type
	 */
	public void setExpressionType(int type);
	
	/**
	 * true if expression has only one parameter
	 * Ex: NOT or empty expression.
	 *
	 * @return true, if is singleton
	 */
	public boolean isSingleton();
	
	/**
	 * get single operand, usefull when singleton expression.
	 *
	 * @return the operand
	 */
	public Object getOperand();
	
	/**
	 * get all operands.
	 *
	 * @return the operands
	 */
	public List getOperands();
	
	
	/**
	 * evaluate this expression against given object.
	 *
	 * @param obj the obj
	 * @return true if object passes this expression, false otherwise
	 */
	public boolean evaluate(Object obj);
	
	
	
	/**
	 * get all restrictions that are contained in an expression
	 * This method is recursive
	 * @param includeNested - include nested classes
	 * @return a list of restrictions
	 */
	public List<IRestriction> getRestrictions(boolean includeNested);
	
	/**
	 * get all restrictions that are contained in an expression
	 * This method is recursive
	 * @return a list of restrictions
	 */
	public List<IRestriction> getRestrictions();
	
	/**
	 * get all classes contained in the expression
	 * @return list of classes
	 */
	public List<IClass> getClasses();
	
	/**
	 * get all classes contained in the expression
	 * @param includeNested - include nested classes
	 * @return list of classes
	 */
	public List<IClass> getClasses(boolean includeNested);
	
}
