package edu.utah.blulab.domainontology;

import java.util.ArrayList;


public class Variable {
	private String varID;
	private String varName;
	private String prefLabel;
	private String prefCUI;
	private ArrayList<String> semanticCategories;
	private ArrayList<String> altCUIs;
	private ArrayList<String> altLabels;
	private ArrayList<String> hiddenLabels;
	private ArrayList<String> abbrLabels;
	private ArrayList<String> subjExpLabels;
	private ArrayList<String> relationships; //May need feeback on the best representation here.
	private ArrayList<String> rules; //This may change once SWRL rules are implemented in ontology.
	private ArrayList<Modifier> modifiers;
	private ArrayList<String> regex; //may need to change to HashMap to allow for multiple languages
	private ArrayList<String> reportTypes; //may change once Document Ontology built
	private ArrayList<String> sectionHeadings; //may change once SecTag Ontology built
	private int windowSize;
	
	public Variable(){
		varID = "";
		varName = "";
		prefLabel = "";
		prefCUI = "";
		altCUIs = new ArrayList<String>();
		altLabels = new ArrayList<String>();
		hiddenLabels = new ArrayList<String>();
		abbrLabels = new ArrayList<String>();
		subjExpLabels = new ArrayList<String>();
		relationships = new ArrayList<String>();
		rules = new ArrayList<String>();
		modifiers = new ArrayList<Modifier>();
		regex = new ArrayList<String>();
		reportTypes = new ArrayList<String>();
		sectionHeadings = new ArrayList<String>();
		windowSize = 6;
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

	public String getPrefLabel() {
		return prefLabel;
	}

	public void setPrefLabel(String prefLabel) {
		this.prefLabel = prefLabel;
	}

	public String getPrefCUI() {
		return prefCUI;
	}

	public void setPrefCUI(String prefCUI) {
		this.prefCUI = prefCUI;
	}

	public ArrayList<String> getAltCUIs() {
		return altCUIs;
	}

	public void setAltCUIs(ArrayList<String> altCUIs) {
		this.altCUIs = altCUIs;
	}

	public ArrayList<String> getAltLabels() {
		return altLabels;
	}

	public void setAltLabels(ArrayList<String> altLabels) {
		this.altLabels = altLabels;
	}

	public ArrayList<String> getHiddenLabels() {
		return hiddenLabels;
	}

	public void setHiddenLabels(ArrayList<String> hiddenLabels) {
		this.hiddenLabels = hiddenLabels;
	}

	public ArrayList<String> getAbbrLabels() {
		return abbrLabels;
	}

	public void setAbbrLabels(ArrayList<String> abbrLabels) {
		this.abbrLabels = abbrLabels;
	}

	public ArrayList<String> getSubjExpLabels() {
		return subjExpLabels;
	}

	public void setSubjExpLabels(ArrayList<String> subjExpLabels) {
		this.subjExpLabels = subjExpLabels;
	}

	public ArrayList<String> getRelationships() {
		return relationships;
	}

	public void setRelationships(ArrayList<String> relationships) {
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

	public ArrayList<String> getRegex() {
		return regex;
	}

	public void setRegex(ArrayList<String> regex) {
		this.regex = regex;
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
	
	public int getWindowSize(){
		return windowSize;
	}
	
	public void setWindowSize(int windowSize){
		this.windowSize = windowSize;
	}
	
	public ArrayList<String> getSemanticCategories(){
		return semanticCategories;
	}
	
	public void setSemanticCategories(ArrayList<String> semanticCategories){
		this.semanticCategories = semanticCategories;
	}

	@Override
	public String toString() {
		return "Variable [varID=" + varID + ", varName=" + varName
				+ ", prefLabel=" + prefLabel + ", prefCUI=" + prefCUI
				+ ", altCUIs=" + altCUIs + ", altLabels=" + altLabels
				+ ", hiddenLabels=" + hiddenLabels + ", abbrLabels="
				+ abbrLabels + ", subjExpLabels=" + subjExpLabels
				+ ", relationships=" + relationships + ", rules=" + rules
				+ ", modifiers=" + modifiers + ", regex=" + regex
				+ ", reportTypes=" + reportTypes + ", sectionHeadings="
				+ sectionHeadings + ", windowSize=" + windowSize + ", categories=" + semanticCategories + "]";
	}
	
	
	
	
}
