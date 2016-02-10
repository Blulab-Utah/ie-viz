package edu.utah.blulab.domainontology;

import java.util.ArrayList;

import org.semanticweb.owlapi.model.OWLClass;


public class Variable {
	private String uri;
	private Term concept;
	private DomainOntology domain;
	private ArrayList<String> relationships; //May need feeback on the best representation here.
	private ArrayList<String> rules; //This may change once SWRL rules are implemented in ontology.
	private ArrayList<Modifier> modifiers;
	private ArrayList<String> reportTypes; //may change once Document Ontology built
	private ArrayList<String> sectionHeadings; //may change once SecTag Ontology built
	private ArrayList<Variable> parents, children;
	
	public Variable(String clsURI, DomainOntology domain){
		this.domain = domain;
		uri = clsURI;
		concept = new Term();
		relationships = new ArrayList<String>();
		rules = new ArrayList<String>();
		modifiers = new ArrayList<Modifier>();
	}

	public String getVarID() {
		return domain.getClassURIString(domain.getClass(uri));
	}

	public String getVarName() {
		return domain.getAnnotationString(domain.getClass(uri), domain.getFactory().getRDFSLabel());
		
	}

	public ArrayList<String> getSemanticCategory(){
		ArrayList<String> categories = new ArrayList<String>();
		ArrayList<OWLClass> list = domain.getAllSuperClasses(domain.getClass(uri), false);
		for(OWLClass cls : list){
			if(domain.getSchemaClasses().contains(cls)){
				categories.add(cls.getIRI().getShortForm());
			}
		}
		return categories;
	}
	

	@Override
	public String toString() {
		return "Variable [varID=" + this.getVarID() + ", varName=" + this.getVarName()
				+ ", concept=" + concept + ", relationships=" + relationships + ", rules=" + rules
				+ ", modifiers=" + modifiers + ", semanticTypes=" + this.getSemanticCategory().toString() + ", sectionHeadings="
				+ sectionHeadings  + "]";
	}
	
	
	
	
}
