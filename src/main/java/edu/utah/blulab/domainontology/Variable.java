package edu.utah.blulab.domainontology;

import java.util.ArrayList;

import org.semanticweb.owlapi.model.IRI;


public class Variable {
	private String uri;
	private Term concept;
	private DomainOntology domain;
	private ArrayList<String> relationships; //May need feeback on the best representation here.
	private ArrayList<String> rules; //This may change once SWRL rules are implemented in ontology.
	private ArrayList<Modifier> modifiers;
	
	public Variable(String clsURI, DomainOntology domain){
		this.domain = domain;
		uri = clsURI;
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

	/**public ArrayList<String> getSemanticCategory(){
		ArrayList<String> categories = new ArrayList<String>();
		ArrayList<OWLClass> list = domain.getAllSuperClasses(domain.getClass(uri), false);
		for(OWLClass cls : list){
			ArrayList<OWLClass> clsList = domain.getSchemaClasses();
			if(domain.getSchemaClasses().contains(cls)){
				categories.add(cls.getIRI().getShortForm());
			}
		}
		return categories;
	}**/
	
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

	@Override
	public String toString() {
		return "Variable [varID=" + this.getVarID() + ", varName=" + this.getVarName()
				+ ", concept=" + this.getAnchor().toString() + ", relationships=" + relationships + ", rules=" + rules
				+ ", modifiers=" + modifiers +   ", sectionHeadings="
				+ this.getSectionHeadings() + "]";
	}
	
	
	
	
}
