package edu.utah.blulab.domainontology;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class Term {
	private String uri;
	private DomainOntology domain;
	
	public Term(String clsURI, DomainOntology domain){
		this.uri = clsURI;
		this.domain = domain;
	}
	
	public String getURI(){
		return uri;
	}
	
	public String getPrefTerm() {
		return domain.getAnnotationString(domain.getClass(uri), 
				domain.getFactory().getOWLAnnotationProperty(IRI.create(OntologyConstants.PREF_LABEL)));
	}
	
	public String getPrefCode() {
		return domain.getAnnotationString(domain.getClass(uri), 
				domain.getFactory().getOWLAnnotationProperty(IRI.create(OntologyConstants.PREF_CUI)));
	}
	
	
	public ArrayList<String> getSynonym() {
		return domain.getAnnotationList(domain.getClass(uri), 
				domain.getFactory().getOWLAnnotationProperty(IRI.create(OntologyConstants.ALT_LABEL)));
	}
	
	public ArrayList<String> getMisspelling() {
		return domain.getAnnotationList(domain.getClass(uri), 
				domain.getFactory().getOWLAnnotationProperty(IRI.create(OntologyConstants.HIDDEN_LABEL)));
	}
	
	public ArrayList<String> getAbbreviation() {
		return domain.getAnnotationList(domain.getClass(uri), 
				domain.getFactory().getOWLAnnotationProperty(IRI.create(OntologyConstants.ABR_LABEL)));
	}
	
	public ArrayList<String> getSubjExp() {
		return domain.getAnnotationList(domain.getClass(uri), 
				domain.getFactory().getOWLAnnotationProperty(IRI.create(OntologyConstants.SUBJ_EXP_LABEL)));
	}
	
	public ArrayList<String> getRegex() {
		return domain.getAnnotationList(domain.getClass(uri), 
				domain.getFactory().getOWLAnnotationProperty(IRI.create(OntologyConstants.REGEX)));
	}
	
	public ArrayList<String> getAltCode() {
		return domain.getAnnotationList(domain.getClass(uri), 
				domain.getFactory().getOWLAnnotationProperty(IRI.create(OntologyConstants.ALT_CUI)));
	}
	
	@Override
	public String toString() {
		return "Term [prefTerm=" + this.getPrefTerm() + ", prefCode=" + this.getPrefCode()
				+  ", synonym=" + this.getSynonym()
				+ ", misspelling=" + this.getMisspelling()+ ", abbreviation="
				+ this.getAbbreviation() + ", subjExp=" + this.getSubjExp() + ", regex=" + this.getRegex()
				+ ", altCode=" + this.getAltCode() + "]";
	}
	
}
