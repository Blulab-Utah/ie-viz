package edu.pitt.dbmi.nlp.noble.ontology.owl;

import edu.pitt.dbmi.nlp.noble.ontology.IDataRange;
import org.semanticweb.owlapi.model.OWLDataRange;

public class ODataRange extends OResource implements IDataRange{
	protected OWLDataRange dataRange;
	public ODataRange(OWLDataRange dt,OOntology ont){
		super(dt,ont);
		dataRange = dt;
	}
	
	public String toString(){
		return dataRange.toString();
	}
	/**
	 * evaluate a data range
	 * @param obj - input object
	 * @return does this object fit into data range
	 */
	public boolean evaluate(Object obj){
		if(dataRange.isDatatype()){
			if(obj instanceof Number){
				Number num = (Number) obj;
				if(dataRange.asOWLDatatype().isInteger()){
					// if int value is the same as double value, then it is an int
					return num.doubleValue() == num.intValue();
				}else if(dataRange.asOWLDatatype().isDouble() || dataRange.asOWLDatatype().isFloat()){
					// default is true, since input and ranges are both numbers
					return true;
				}
			}else if (obj instanceof Boolean && dataRange.asOWLDatatype().isBoolean()){
				return true;
			}else if (obj instanceof String && dataRange.asOWLDatatype().isString()){
				return true;
			}
		}
		return false;
	}
}
