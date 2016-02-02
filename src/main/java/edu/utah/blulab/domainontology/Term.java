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
	private String prefTerm, prefCode, definition;
	private ArrayList<String> synonym, misspelling, abbreviation, subjExp, regex, altCode;
	private ArrayList<Term> parents, children;
	
	public Term(){
		
	}
	
	public Term(OWLClass cls, OWLOntologyManager manager, OWLOntology ontology){
		OWLDataFactory factory = manager.getOWLDataFactory();
		parents = new ArrayList<Term>();
		children = new ArrayList<Term>();
		
		//Set preferred label for target concept
		prefTerm = getAnnotationString(cls, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.PREF_LABEL)), ontology);
		
		//Set preferred CUIs for variable concept
		prefCode = getAnnotationString(cls,
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.PREF_CUI)), ontology);
		
		//Set alternate CUIs for variable concept
		altCode = getAnnotationList(cls,
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.ALT_CUI)), ontology);
		
		//Set alternate labels
		synonym = getAnnotationList(cls, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.ALT_LABEL)), ontology);
		
		//Set hidden labels
		misspelling = getAnnotationList(cls, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.HIDDEN_LABEL)), ontology);
		
		//Set abbreviation labels
		abbreviation = getAnnotationList(cls,
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.ABR_LABEL)), ontology);
		
		//Set subjective expression labels
		subjExp = getAnnotationList(cls,
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.SUBJ_EXP_LABEL)), ontology);
		
		//Set regex
		regex = getAnnotationList(cls, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.REGEX)), ontology);
	}
	
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
	
	public ArrayList<Term> getChildren(){
		return children;
	}
	
	public void setChildren(ArrayList<Term> children){
		this.children = children;
	}
	
	public ArrayList<Term> getParents(){
		return parents;
	}
	
	public void setParents(ArrayList<Term> parents){
		this.parents = parents;
	}
	
	public boolean hasChildren(){
		return children.isEmpty();
	}
	
	public boolean hasParents(){
		return parents.isEmpty();
	}
	@Override
	public String toString() {
		return "Term [prefTerm=" + prefTerm + ", prefCode=" + prefCode
				+ ", children=" + children + ", synonym=" + synonym
				+ ", misspelling=" + misspelling + ", abbreviation="
				+ abbreviation + ", subjExp=" + subjExp + ", regex=" + regex
				+ ", altCode=" + altCode + "]";
	}
	
	private static String getAnnotationString(OWLClass cls, OWLAnnotationProperty annotationProperty, OWLOntology ontology){
		String str = "";
		Set<OWLAnnotation> labels = cls.getAnnotations(ontology, annotationProperty);
		if(!labels.isEmpty()){
			Iterator<OWLAnnotation> iter = labels.iterator();
			while(iter.hasNext()){
				OWLAnnotation label = iter.next();
				String temp = label.getValue().toString();
				temp = temp.substring(temp.indexOf("\"")+1, temp.lastIndexOf("\""));
				str = temp;
				break;
			}
			
		}
		return str;
	}
	

	
	private static ArrayList<String> getAnnotationList(OWLClass cls, OWLAnnotationProperty annotationProperty, OWLOntology ontology){
		ArrayList<String> labelSet = new ArrayList<String>();
		Set<OWLAnnotation> annotations = cls.getAnnotations(ontology, annotationProperty);
		if(!annotations.isEmpty()){
			Iterator<OWLAnnotation> iter = annotations.iterator();
			while(iter.hasNext()){
				OWLAnnotation ann = iter.next();
				String temp = ann.getValue().toString();
				temp = temp.substring(temp.indexOf("\"")+1, temp.lastIndexOf("\""));
				labelSet.add(temp);
			}
		}
		return labelSet;
	}
	
}
