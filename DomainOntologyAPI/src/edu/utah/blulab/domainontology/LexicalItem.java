package edu.utah.blulab.domainontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class LexicalItem {
	private String itemName, uri,  actionEn, actionSv, actionDe; //prefTermEn, prefTermSv, prefTermDe
	private ArrayList<String> source, creator; //altTermEn, altTermSv, altTermDe, regEx,
	private HashMap<String, Set<String>> prefTerm, altTerm, regEx;
	
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

	public ArrayList<String> getCreator() {
		return creator;
	}

	public void setCreator(ArrayList<String> creator) {
		this.creator = creator;
	}

	public ArrayList<String> getSource() {
		return source;
	}

	public void setSource(ArrayList<String> source) {
		this.source = source;
	}
	
	public HashMap<String, Set<String>> getAltTerm(){
		return altTerm;
	}
	
	public void setAltTerm(HashMap<String, Set<String>> altTerm){
		this.altTerm = altTerm;
	}
	
	public HashMap<String, Set<String>> getPrefTerm(){
		return prefTerm;
	}
	
	public void setPrefTerm(HashMap<String, Set<String>> prefTerm){
		this.prefTerm = prefTerm;
	}
	
	public HashMap<String, Set<String>> getRegEx(){
		return regEx;
	}
	
	public void setRegEx(HashMap<String, Set<String>> regEx){
		this.regEx = regEx;
	}

	

	@Override
	public String toString() {
		return "LexicalItem [itemName=" + itemName + ", uri=" + uri
				+ ", actionEn=" + actionEn + ", creator=" + creator
				+ ", prefTermEn=" + prefTerm + ", source=" + source + ", altTerm=" + altTerm
				+ ", regEx=" + regEx + "]";
	}
	
	
}
