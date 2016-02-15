package edu.utah.blulab.domainontology;

import java.util.ArrayList;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;


public class Variable {
	private String uri;
	private Term concept;
	private DomainOntology domain;
	private ArrayList<String> relationships; //May need feeback on the best representation here.
	private ArrayList<String> rules; //This may change once SWRL rules are implemented in ontology.
	
	public Variable(String clsURI, DomainOntology domain){
		this.domain = domain;
		uri = clsURI;
		relationships = new ArrayList<String>();
		rules = new ArrayList<String>();
	}

	public String getVarID() {
		return domain.getClassURIString(domain.getClass(uri));
	}

	public String getVarName() {
		return domain.getAnnotationString(domain.getClass(uri), domain.getFactory().getRDFSLabel());
		
	}

	public ArrayList<String> getSemanticCategory(){
		
		return domain.getDirectSuperClasses(domain.getFactory().getOWLClass(IRI.create(uri)));
	}
	
	public ArrayList<String> getSectionHeadings(){
		ArrayList<String> headings = domain.getAnnotationList(domain.getClass(uri), 
				domain.getFactory().getOWLAnnotationProperty(IRI.create(OntologyConstants.SEC_HEADING)));
		return headings;
	}
	
	public ArrayList<String> getReportTypes(){
		ArrayList<String> types = domain.getAnnotationList(domain.getClass(uri), 
				domain.getFactory().getOWLAnnotationProperty(IRI.create(OntologyConstants.DOC_TYPE)));
		return types;
	}
	
	public Term getAnchor(){
		Term anchor = new Term(domain.getEquivalentObjectPropertyFiller(domain.getClass(uri), 
				domain.getFactory().getOWLObjectProperty(IRI.create(OntologyConstants.HAS_ANCHOR))).getIRI().toString(), 
				domain);
		return anchor;
	}
	
	public ArrayList<Modifier> getModifiers(){
		ArrayList<Modifier> mods = new ArrayList<Modifier>();
		ArrayList<OWLClass>	 list = domain.getEquivalentObjectPropertyFillerList(domain.getClass(uri), domain.getPropertyList());
		for(OWLClass cls : list){
			mods.add(new Modifier(cls.getIRI().toString(), domain));
		}
		
		return mods;
	}
	
	public ArrayList<Variable> getDirectParents(){
		ArrayList<Variable> parents = new ArrayList<Variable>();
		ArrayList<String> clsStrings = domain.getDirectSuperClasses(domain.getClass(uri));
		for(String str : clsStrings){
			if(!domain.getClass(str).asOWLClass().getIRI().getNamespace().equalsIgnoreCase(OntologyConstants.SO_PM+"#") &&
					!domain.getClass(str).asOWLClass().getIRI().getNamespace().equalsIgnoreCase(OntologyConstants.CT_PM+"#")){
				parents.add(new Variable(str, domain));
			}
		}
		return parents;
	}
	
	public ArrayList<Variable> getDirectChildren(){
		ArrayList<Variable> children = new ArrayList<Variable>();
		ArrayList<String> clsStrings = domain.getDirectSubClasses(domain.getClass(uri));
		for(String str : clsStrings){
			if(!domain.getClass(str).asOWLClass().getIRI().getNamespace().equalsIgnoreCase(OntologyConstants.SO_PM+"#") &&
					!domain.getClass(str).asOWLClass().getIRI().getNamespace().equalsIgnoreCase(OntologyConstants.CT_PM+"#")){
				children.add(new Variable(str, domain));
			}
		}
		return children;
	}

	@Override
	public String toString() {
		return "Variable [varID=" + this.getVarID() + ", varName=" + this.getVarName()
				//+ ", category=" + this.getSemanticCategory()
				+ ", concept=" + this.getAnchor().toString() 
				//+ ", modifiers=" + this.getModifiers() +   ", sectionHeadings="
				+ this.getSectionHeadings() + "]";
	}
	
	
	
	
}
