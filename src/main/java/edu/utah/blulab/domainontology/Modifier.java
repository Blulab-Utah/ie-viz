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
	private ArrayList<LexicalItem> items;
	private ArrayList<String> closures;
	private ArrayList<Modifier> parents, children;
	
	
	public Modifier(String clsURI, DomainOntology domain){
		uri = clsURI;
		this.domain = domain;
		
		//Get list of closures if any
		/**Set<OWLClassExpression> superCls = modCls.getSuperClasses(manager.getOntologies());
		for(OWLClassExpression exp : superCls){
			if(exp.getClassExpressionType().compareTo(ClassExpressionType.OBJECT_SOME_VALUES_FROM) == 0){
				OWLObjectSomeValuesFrom obj = (OWLObjectSomeValuesFrom) exp;
				OWLObjectPropertyExpression propExp = obj.getProperty();
				if(propExp.asOWLObjectProperty().equals(factory.getOWLObjectProperty(IRI.create(OntologyConstants.HAS_CLOSURE)))){
					closures.add(obj.getFiller().toString());
				}
			}
		}
		
		
		}**/
		
		
		
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
		return null;
	}
	
	public ArrayList<Modifier> getPseudos(){
		return null;
	}
	@Override
	public String toString() {
		return "Modifier [modName=" + this.getModName() + ", uri=" + uri  + ", items=" + this.getItems() + ", closures=" + closures + "]";
	}
	
	
	
}
