package edu.utah.blulab.domainontology;

import java.util.ArrayList;

public class Term {
	private String prefTerm, prefCode, definition;
	private ArrayList<String> synonym, misspelling, abbreviation, subjExp, regex, altCode;
	public String getPrefTerm() {
		return prefTerm;
	}
	public void setPrefTerm(String prefTerm) {
		this.prefTerm = prefTerm;
	}
	public String getPrefCode() {
		return prefCode;
	}
	public void setPrefCode(String prefCode) {
		this.prefCode = prefCode;
	}
	public String getDefinition() {
		return definition;
	}
	public void setDefinition(String definition) {
		this.definition = definition;
	}
	public ArrayList<String> getSynonym() {
		return synonym;
	}
	public void setSynonym(ArrayList<String> synonym) {
		this.synonym = synonym;
	}
	public ArrayList<String> getMisspelling() {
		return misspelling;
	}
	public void setMisspelling(ArrayList<String> misspelling) {
		this.misspelling = misspelling;
	}
	public ArrayList<String> getAbbreviation() {
		return abbreviation;
	}
	public void setAbbreviation(ArrayList<String> abbreviation) {
		this.abbreviation = abbreviation;
	}
	public ArrayList<String> getSubjExp() {
		return subjExp;
	}
	public void setSubjExp(ArrayList<String> subjExp) {
		this.subjExp = subjExp;
	}
	public ArrayList<String> getRegex() {
		return regex;
	}
	public void setRegex(ArrayList<String> regex) {
		this.regex = regex;
	}
	public ArrayList<String> getAltCode() {
		return altCode;
	}
	public void setAltCode(ArrayList<String> altCode) {
		this.altCode = altCode;
	}
	@Override
	public String toString() {
		return "Term [prefTerm=" + prefTerm + ", prefCode=" + prefCode
				+ ", definition=" + definition + ", synonym=" + synonym
				+ ", misspelling=" + misspelling + ", abbreviation="
				+ abbreviation + ", subjExp=" + subjExp + ", regex=" + regex
				+ ", altCode=" + altCode + "]";
	}
	
	
}
