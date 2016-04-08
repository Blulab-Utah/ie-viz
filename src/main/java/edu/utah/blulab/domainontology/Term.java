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
	
	public ArrayList<Term> getPseudos(){
		ArrayList<Term> pseudos = new ArrayList<Term>();
		ArrayList<OWLClass> pseudoList = domain.getObjectPropertyFillerList(domain.getClass(uri), 
				domain.getFactory().getOWLObjectProperty(IRI.create(OntologyConstants.HAS_PSEUDO)));
		for(OWLClass pseudo : pseudoList){
			pseudos.add(new Term(pseudo.getIRI().toString(), domain));
		}
		return pseudos;
	}
	
	public ArrayList<Term> getDirectParents(){
		ArrayList<Term> parents = new ArrayList<Term>();
		ArrayList<String> clsStrings = domain.getDirectSuperClasses(domain.getClass(uri));
		for(String str : clsStrings){
			if(!domain.getClass(str).asOWLClass().getIRI().getNamespace().equalsIgnoreCase(OntologyConstants.SO_PM+"#") &&
					!domain.getClass(str).asOWLClass().getIRI().getNamespace().equalsIgnoreCase(OntologyConstants.CT_PM+"#")){
				parents.add(new Term(str, domain));
			}
		}
		return parents;
	}
	
	public ArrayList<Term> getDirectChildren(){
		ArrayList<Term> children = new ArrayList<Term>();
		ArrayList<String> clsStrings = domain.getDirectSubClasses(domain.getClass(uri));
		for(String str : clsStrings){
			if(!domain.getClass(str).asOWLClass().getIRI().getNamespace().equalsIgnoreCase(OntologyConstants.SO_PM+"#") &&
					!domain.getClass(str).asOWLClass().getIRI().getNamespace().equalsIgnoreCase(OntologyConstants.CT_PM+"#")){
				children.add(new Term(str, domain));
			}
		}
		return children;
	}

	public ArrayList<String> getAllParents(){
		ArrayList<String> parentAncestry = new ArrayList<String>();
		parentAncestry = domain.getAllSuperClasses(domain.getClass(uri), false);
		return parentAncestry;
	}
	
	@Override
	public String toString() {
		return "Term [prefTerm=" + this.getPrefTerm() + ", prefCode=" + this.getPrefCode()
				+  ", synonym=" + this.getSynonym()
				+ ", misspelling=" + this.getMisspelling()//+ ", abbreviation="
				//+ this.getAbbreviation() + ", subjExp=" + this.getSubjExp() + ", regex=" + this.getRegex()
				//+ ", altCode=" + this.getAltCode() 
				+ ", pseudos=" + this.getPseudos()
				+ "]";
	}
	
}
