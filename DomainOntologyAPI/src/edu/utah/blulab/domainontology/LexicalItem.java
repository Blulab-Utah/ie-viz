package edu.utah.blulab.domainontology;

import java.util.ArrayList;
import java.util.HashMap;

public class LexicalItem {
	private String itemName, uri,  actionEn, actionSv, actionDe, creator, prefTermEn, prefTermSv, prefTermDe;
	private ArrayList<String> source, prefTerm, altTerm, regEx;
	//private HashMap<String, String> prefTerm, altTerm, regEx;
	
	public LexicalItem(){
		
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getActionEn() {
		return actionEn;
	}

	public void setActionEn(String actionEn) {
		this.actionEn = actionEn;
	}

	public String getActionSv() {
		return actionSv;
	}

	public void setActionSv(String actionSv) {
		this.actionSv = actionSv;
	}

	public String getActionDe() {
		return actionDe;
	}

	public void setActionDe(String actionDe) {
		this.actionDe = actionDe;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getPrefTermEn() {
		return prefTermEn;
	}

	public void setPrefTermEn(String prefTermEn) {
		this.prefTermEn = prefTermEn;
	}

	public String getPrefTermSv() {
		return prefTermSv;
	}

	public void setPrefTermSv(String prefTermSv) {
		this.prefTermSv = prefTermSv;
	}

	public String getPrefTermDe() {
		return prefTermDe;
	}

	public void setPrefTermDe(String prefTermDe) {
		this.prefTermDe = prefTermDe;
	}

	public ArrayList<String> getSource() {
		return source;
	}

	public void setSource(ArrayList<String> source) {
		this.source = source;
	}

	public ArrayList<String> getPrefTerm() {
		return prefTerm;
	}

	public void setPrefTerm(ArrayList<String> prefTerm) {
		this.prefTerm = prefTerm;
	}

	public ArrayList<String> getAltTerm() {
		return altTerm;
	}

	public void setAltTerm(ArrayList<String> altTerm) {
		this.altTerm = altTerm;
	}

	public ArrayList<String> getRegEx() {
		return regEx;
	}

	public void setRegEx(ArrayList<String> regEx) {
		this.regEx = regEx;
	}
	
	
}
