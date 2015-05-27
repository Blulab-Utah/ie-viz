package edu.utah.blulab.domainontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class LexicalItem {
	private String uri,  actionEn, actionSv, actionDe; //prefTermEn, prefTermSv, prefTermDe
	private Term term;
	private ArrayList<String> source, creator; 
	
	public LexicalItem(OWLIndividual item, OWLOntologyManager manager, OWLDataFactory factory){
		
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
	
	public Term getTerm(){
		return term;
	}
	
	public void setTerm(Term term){
		this.term = term;
	}

	@Override
	public String toString() {
		return "LexicalItem [uri=" + uri + ", term=" + term 
				+ ", actionEn=" + actionEn + ", creator=" + creator
				+ ", source=" + source + "]";
	}
	
	
}
