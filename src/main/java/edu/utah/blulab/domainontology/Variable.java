package edu.utah.blulab.domainontology;

import java.util.ArrayList;


public class Variable {
	private String varID;
	private String varName;
	private Term anchor;
	private ArrayList<String> semanticCategory;
	private ArrayList<Relation> relationships; //May need feeback on the best representation here.
	private ArrayList<String> rules; //This may change once SWRL rules are implemented in ontology.
	private ArrayList<Modifier> modifiers;
	private ArrayList<String> reportTypes; //may change once Document Ontology built
	private ArrayList<String> sectionHeadings; //may change once SecTag Ontology built
	
	
	public Variable(){
		varID = "";
		varName = "";
		anchor = new Term();
		semanticCategory = new ArrayList<String>();
		relationships = new ArrayList<Relation>();
		rules = new ArrayList<String>();
		modifiers = new ArrayList<Modifier>();
		reportTypes = new ArrayList<String>();
		sectionHeadings = new ArrayList<String>();
	}

	public String getVarID() {
		return varID;
	}

	public void setVarID(String varID) {
		this.varID = varID;
	}

	public String getVarName() {
		return varName;
	}

	public void setVarName(String varName) {
		this.varName = varName;
	}
	
	public ArrayList<String> getSemanticCategory(){
		return semanticCategory;
	}
	
	public void setSemanticCategory(ArrayList<String> semanticCategory){
		this.semanticCategory = semanticCategory;
	}

	public ArrayList<Relation> getRelationships() {
		return relationships;
	}

	public void setRelationships(ArrayList<Relation> relationships) {
		this.relationships = relationships;
	}

	public ArrayList<String> getRules() {
		return rules;
	}

	public void setRules(ArrayList<String> rules) {
		this.rules = rules;
	}

	public ArrayList<Modifier> getModifiers() {
		return modifiers;
	}

	public void setModifiers(ArrayList<Modifier> modifiers) {
		this.modifiers = modifiers;
	}

	public ArrayList<String> getReportTypes() {
		return reportTypes;
	}

	public void setReportTypes(ArrayList<String> reportTypes) {
		this.reportTypes = reportTypes;
	}

	public ArrayList<String> getSectionHeadings() {
		return sectionHeadings;
	}

	public void setSectionHeadings(ArrayList<String> sectionHeadings) {
		this.sectionHeadings = sectionHeadings;
	}
	
	
	
	public Term getAnchor(){
		return anchor;
	}
	
	public void setAnchor(Term anchor){
		this.anchor = anchor;
	}


	@Override
	public String toString() {
		return "Variable [varID=" + varID + ", varName=" + varName
				+ ", anchor=" + anchor + ", relationships=" + relationships + ", rules=" + rules
				+ ", modifiers=" + modifiers + ", reportTypes=" + reportTypes + ", sectionHeadings="
				+ sectionHeadings + ", categories=" + semanticCategory + "]";
	}
	
	
	
	
}
