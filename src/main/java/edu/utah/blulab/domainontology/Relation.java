package edu.utah.blulab.domainontology;

public class Relation {
	private Object argument1, argument2, relationship;
	
	public Relation(Object argument1, Object argument2, Object relationship){
		this.argument1= argument1;
		this.argument2= argument2;
		this.relationship = relationship;
	}

	public Object getArgument1() {
		return argument1;
	}

	public void setArgument1(Object argument1) {
		this.argument1 = argument1;
	}

	public Object getArgument2() {
		return argument2;
	}

	public void setArgument2(Object argument2) {
		this.argument2 = argument2;
	}

	public Object getRelationship() {
		return relationship;
	}

	public void setRelationship(Object relationship) {
		this.relationship = relationship;
	}
	
	
}
