package edu.pitt.dbmi.nlp.noble.ontology;

import edu.pitt.dbmi.nlp.noble.ontology.owl.OFacetRestriction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * list of resources that could be conjunction or disjunction.
 *
 * @author tseytlin
 */
public class LogicExpression extends ArrayList implements ILogicExpression {
	private int type;

	
	/**
	 * create logic expression of a type.
	 *
	 * @param obj the obj
	 */
	public LogicExpression(Object obj){
		this(0,obj);
	}
	
	/**
	 * create logic expression of a type.
	 *
	 * @param type the type
	 */
	public LogicExpression(int type){
		super();
		this.type = type;
	}
	
	/**
	 * create logic expression of a type.
	 *
	 * @param type the type
	 * @param c the c
	 */
	public LogicExpression(int type, Collection c){
		super(c);
		this.type = type;
	}

	/**
	 * create a new logic expression from existing one
	 * @param exp - logic expression
	 */
	public LogicExpression(ILogicExpression exp){
		this(exp.getExpressionType(),exp);
	}


	/**
	 * create logic expression of a type.
	 *
	 * @param type the type
	 * @param obj the obj
	 */
	public LogicExpression(int type, Object obj){
		super();
		this.type = type;
		add(obj);
	}
	
	
	/**
	 * create logic expression of a type.
	 *
	 * @param type the type
	 * @param obj the obj
	 */
	public LogicExpression(int type, Object [] obj){
		super();
		this.type = type;
		Collections.addAll(this,obj);
	}
	
	/**
	 * get single operand, usefull when singleton expression.
	 *
	 * @return the operand
	 */
	public Object getOperand(){
		return (size() > 0)?get(0):null;
	}
	
	/**
	 * get all operands.
	 *
	 * @return the operands
	 */
	public List getOperands(){
		return this;
	}
	
	/**
	 * get expression type
	 * AND, OR, NOT
	 * if 0 is returned then expression is just a container 
	 * for a single value ex: (A)  .
	 *
	 * @return the expression type
	 */
	public int getExpressionType(){
		return type;
	}
	
	/**
	 * set expression type
	 * AND, OR, NOT
	 * if 0 is returned then expression is just a container 
	 * for a single value ex: (A)  .
	 *
	 * @param type the new expression type
	 */
	public void setExpressionType(int type){
		this.type = type;
	}
	
	/**
	 * true if expression has only one parameter
	 * Ex: NOT or empty expression.
	 *
	 * @return true, if is singleton
	 */
	public boolean isSingleton(){
		//return size() == 1;
		return type == NOT || type == EMPTY;
	}
	
	
	/**
	 * add object enforce singleton.
	 *
	 * @param obj the obj
	 * @return true, if successful
	 */
	public boolean add(Object obj){
		if(type <= NOT && !isEmpty()){
			return false;
		}
		if(obj != null)
			return super.add(obj);
		return false;
	}
	
	/**
	 * evaluate single parameter.
	 *
	 * @param obj the obj
	 * @param param the param
	 * @return true, if successful
	 */
	private boolean evaluateParameter(Object obj, Object param){
		if(obj instanceof ILogicExpression){
			return ((ILogicExpression) obj).evaluate(param);
		}else if(obj instanceof IClass){
			return ((IClass) obj).evaluate(param);
		}else if(obj instanceof OFacetRestriction){
			return ((OFacetRestriction)obj).evaluate(param);
		}else if(obj instanceof Number && param instanceof Number){
			return ((Number)obj).doubleValue() == ((Number)param).doubleValue();
		}else if(obj instanceof IDataRange){
			return ((IDataRange)obj).evaluate(param);
		}
		return obj.equals(param);
	}
	
	/**
	 * evaluate this expression against given object.
	 *
	 * @param obj the obj
	 * @return true if object passes this expression, false otherwise
	 */
	public boolean evaluate(Object obj){
		switch(type){
			case EMPTY: 
				return evaluateParameter(getOperand(),obj);
			case NOT:   
				//	TODO: maybe incorrect
				return !evaluateParameter(getOperand(),obj);  
			case AND: 
				//	iterate over expression
				boolean b = true;
				for(Object e : this){
					b &= evaluateParameter(e,obj);
				}
				return b;	
			case OR:
				// iterate over expression
				b = false;
				for(Object e : this){
					b |= evaluateParameter(e,obj);
				}
				return b;
		}
		return false;
	}
	
	
	/**
	 * create pretty printed expressions.
	 *
	 * @return the string
	 */
	public String toString(){
		if(type == AND)
			return super.toString().replaceAll(","," and");
		else if(type == OR)
			return super.toString().replaceAll(","," or");
		else if(type == NOT)
			return "[not "+super.toString().substring(1);
		return super.toString();
	}
	
	
	/**
	 * are two expressions equal?.
	 *
	 * @param obj the obj
	 * @return true, if successful
	 */
	public boolean equals(Object obj){
		if(obj instanceof ILogicExpression){
			ILogicExpression exp = (ILogicExpression) obj;
			if(exp.size() == size() && getExpressionType() == exp.getExpressionType()){
				for(int i=0;i<size();i++){
					if(!get(i).equals(exp.get(i)))
						return false;
				}
				return true;
			}
		}
		return false;
	}
	
	
	
	/**
	 * get all restrictions that are contained in an expression
	 * This method is recursive
	 * @param includeNested - include nested classes
	 * @return a list of restrictions
	 */
	public List<IRestriction> getRestrictions(boolean includeNested) {
		List<IRestriction> list = new ArrayList<IRestriction>();
		for(Object o: this){
			if(o instanceof IRestriction){
				list.add((IRestriction)o);
			}else if(o instanceof LogicExpression && includeNested){
				list.addAll(((LogicExpression)o).getRestrictions(includeNested));
			}
		}
		return list;
	}
	
	/**
	 * get all restrictions that are contained in an expression
	 * This method is recursive
	 * @return a list of restrictions
	 */
	public List<IRestriction> getRestrictions() {
		return getRestrictions(true);
	}
	
	/**
	 * get all classes contained in the expression
	 * @return list of classes
	 */
	public List<IClass> getClasses(){
		return getClasses(true);
	}
	
	/**
	 * get all classes contained in the expression
	 * @param includeNested - include nested classes
	 * @return list of classes
	 */
	public List<IClass> getClasses(boolean includeNested){
		List<IClass> list = new ArrayList<IClass>();
		for(Object o: this){
			if(o instanceof IClass){
				list.add((IClass)o);
			}else if(o instanceof LogicExpression && includeNested){
				list.addAll(((LogicExpression)o).getClasses(includeNested));
			}
		}
		return list;
	}
}
