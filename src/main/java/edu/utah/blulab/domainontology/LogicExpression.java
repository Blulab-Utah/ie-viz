package edu.utah.blulab.domainontology;

import java.util.ArrayList;

public class LogicExpression<Object> extends ArrayList<Object> {
	private String type;
	public final String AND = "AND";
	public final String OR = "OR";
	public final String COMPLEMENT = "COMPLEMENT";
	public final String SINGLE = "SINGLE";
	
	public LogicExpression(String type){
		this.type = type;
	}
	
	public LogicExpression(){
		type = "";
	}
	
	public String getType(){
		return type;
	}
	
	public void setType(String type){
		this.type = type;
	}
	
	public boolean isAndExpression(){
		if(type.equalsIgnoreCase(AND)){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean isOrExpression(){
		if(type.equalsIgnoreCase(OR)){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean isComplementExpression(){
		if(type.equalsIgnoreCase(COMPLEMENT)){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean isSingleExpression(){
		if(type.equalsIgnoreCase(SINGLE)){
			return true;
		}else{
			return false;
		}
	}
}
