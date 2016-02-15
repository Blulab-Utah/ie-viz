package edu.utah.blulab.domainontology;

import java.util.ArrayList;

import org.semanticweb.owlapi.model.IRI;


public class LexicalItem {
	private DomainOntology domain;
	private String uri;
	
	public LexicalItem(String itemURI, DomainOntology domain){
		uri = itemURI;
		this.domain = domain;
	}
		
	public String getUri() {
		return uri;
	}

	public String getPrefTerm(){
		return domain.getAnnotationString(domain.getIndividual(uri), 
				domain.getFactory().getOWLAnnotationProperty(IRI.create(OntologyConstants.PREF_LABEL)), "en");
	}
	
	public ArrayList<String> getPrefCode(){
		return domain.getAnnotationStringList(domain.getIndividual(uri), 
				domain.getFactory().getOWLAnnotationProperty(IRI.create(OntologyConstants.PREF_CUI)), "en");
	}
	
	public ArrayList<String> getSynonym(){
		return domain.getAnnotationStringList(domain.getIndividual(uri), 
				domain.getFactory().getOWLAnnotationProperty(IRI.create(OntologyConstants.ALT_LABEL)), "en");
	}
	
	public ArrayList<String>getMisspelling(){
		return domain.getAnnotationStringList(domain.getIndividual(uri), 
				domain.getFactory().getOWLAnnotationProperty(IRI.create(OntologyConstants.HIDDEN_LABEL)), "en");
	}
	
	public ArrayList<String>getAbbreviation(){
		return domain.getAnnotationStringList(domain.getIndividual(uri), 
				domain.getFactory().getOWLAnnotationProperty(IRI.create(OntologyConstants.ABR_LABEL)), "en");
	}
	
	public ArrayList<String>getSubjExp(){
		return domain.getAnnotationStringList(domain.getIndividual(uri), 
				domain.getFactory().getOWLAnnotationProperty(IRI.create(OntologyConstants.SUBJ_EXP_LABEL)), "en");
	}
	
	public ArrayList<String>getRegex(){
		return domain.getAnnotationStringList(domain.getIndividual(uri), 
				domain.getFactory().getOWLAnnotationProperty(IRI.create(OntologyConstants.REGEX)), "en");
	}
	
	public ArrayList<String>getAltCode(){
		return domain.getAnnotationStringList(domain.getIndividual(uri), 
				domain.getFactory().getOWLAnnotationProperty(IRI.create(OntologyConstants.ALT_CUI)), "en");
	}
	
	public String getActionEn(boolean returnDisplayName) {
		String str = domain.getObjectPropertyFillerIndividual(domain.getIndividual(uri), 
				domain.getFactory().getOWLObjectProperty(IRI.create(OntologyConstants.ACTION_EN)));
		if(returnDisplayName){
			str = domain.getDisplayName(str);
		}
		return str;
	}

	public int getWindowSize(){
		String str = domain.getAnnotationString(domain.getIndividual(uri), 
				domain.getFactory().getOWLAnnotationProperty(IRI.create(OntologyConstants.WINDOW)));
		if(str.isEmpty() || str.equals(null)){
			return 8;
		}else{
			return Integer.parseInt(str);
		}
		
	}
	

	@Override
	public String toString() {
		return "LexicalItem [uri=" + uri + ", prefLabel=" + this.getPrefTerm() + 
				 ", regex=" + this.getRegex() +
				 ", altLabel=" + this.getSynonym() +
				", windowSize=" + this.getWindowSize() + ", actionEn=" + this.getActionEn(true) + "]";

	}
	
	
}
