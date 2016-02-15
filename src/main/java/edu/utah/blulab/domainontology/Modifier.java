package edu.utah.blulab.domainontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class Modifier {
	private String uri;
	private DomainOntology domain;
	
	
	public Modifier(String clsURI, DomainOntology domain){
		uri = clsURI;
		this.domain = domain;
	}
	
	
	public String getModName() {
		return domain.getClass(uri).getIRI().getShortForm();
	}
	
	public String getUri() {
		return uri;
	}
	
	public ArrayList<LexicalItem> getItems() {
		ArrayList<LexicalItem> items = new ArrayList<LexicalItem>();
		for(String item : domain.getAllIndividualURIs(domain.getClass(uri))){
			items.add(new LexicalItem(item, domain));
		}
		return items;
	}
	
	public ArrayList<Modifier> getClosures() {
		ArrayList<Modifier> list = new ArrayList<Modifier>();
		ArrayList<OWLClass> clsList = domain.getObjectPropertyFillerList(domain.getClass(uri), 
				domain.getFactory().getOWLObjectProperty(IRI.create(OntologyConstants.HAS_TERMINATION)));
		for(OWLClass cls : clsList){
			list.add(new Modifier(cls.getIRI().toString(), domain));
		}
		return list;
	}
	
	public ArrayList<Modifier> getPseudos(){
		ArrayList<Modifier> list = new ArrayList<Modifier>();
		ArrayList<OWLClass> clsList = domain.getObjectPropertyFillerList(domain.getClass(uri), 
				domain.getFactory().getOWLObjectProperty(IRI.create(OntologyConstants.HAS_PSEUDO)));
		for(OWLClass cls : clsList){
			list.add(new Modifier(cls.getIRI().toString(), domain));
		}
		return list;
	}
	@Override
	public String toString() {
		return "Modifier [modName=" + this.getModName() + ", uri=" + uri  + ", items=" + this.getItems() 
		+ "\n, psuedos=" + this.getPseudos()
		+ "\n, closures=" + this.getClosures() + "]";
	}
	
	
	
}
