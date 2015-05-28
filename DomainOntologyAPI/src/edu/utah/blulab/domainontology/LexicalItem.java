package edu.utah.blulab.domainontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class LexicalItem {
	private String uri,  actionEn, actionSv, actionDe; //prefTermEn, prefTermSv, prefTermDe
	private Term term;
	private ArrayList<String> source, creator; 
	
	public LexicalItem(OWLIndividual item, OWLOntologyManager manager){
		OWLDataFactory factory = manager.getOWLDataFactory();
		
		//Set uri
		uri = item.asOWLNamedIndividual().getIRI().toString();
		
		//Create term associated with lexical item
		term = new Term();
		term.setPrefTerm(getEnPrefLabel(item, manager, factory));
		term.setSynonym(getEnglishDataProperty(item, manager, factory.getOWLDataProperty(IRI.create(OntologyConstants.ALT_TERM))));
		term.setRegex(getEnglishDataProperty(item, manager, factory.getOWLDataProperty(IRI.create(OntologyConstants.EN_REGEX))));
		
		//Get English action associated with lexical item
		Set<OWLIndividual> enActions = item.getObjectPropertyValues(factory.getOWLObjectProperty(IRI.create(OntologyConstants.ACTION_EN)), 
				manager.getOntology(IRI.create(OntologyConstants.MO_PM)));
		for(OWLIndividual action : enActions){
			actionEn = action.asOWLNamedIndividual().getIRI().getFragment();
		}
		
		source = getAnnotationProperty(item, manager, factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.SOURCE)));
		creator = getAnnotationProperty(item, manager, factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.CREATOR)));
		
	}
	
	private ArrayList<String> getAnnotationProperty(OWLIndividual ind, OWLOntologyManager manager, OWLAnnotationProperty property){
		ArrayList<String> labels = new ArrayList<String>();
		
		Set<OWLAnnotation> annotations = ind.asOWLNamedIndividual().getAnnotations(manager.getOntology(IRI.create(OntologyConstants.MO_PM)), 
				property);
		if(!annotations.isEmpty()){
			Iterator<OWLAnnotation> iter = annotations.iterator();
			while(iter.hasNext()){
				OWLAnnotation ann = iter.next();
				String temp = ann.getValue().toString();
				temp = temp.substring(temp.indexOf("\"")+1, temp.lastIndexOf("\""));
				labels.add(temp);
			}
		}
		
		return labels;
	}
	
	private static String getEnPrefLabel(OWLIndividual ind, OWLOntologyManager manager, OWLDataFactory factory){
		String str = "";
		Set<OWLLiteral> values = ind.getDataPropertyValues(factory.getOWLDataProperty(IRI.create(OntologyConstants.PREF_TERM)), 
				manager.getOntology(IRI.create(OntologyConstants.MO_PM)));
		
		for(OWLLiteral lit : values){
			if(lit.hasLang("en")){
				//System.out.println(lit.getLiteral());
				str = lit.getLiteral();
			}
			
		}
		return str;
	}
	
	private static ArrayList<String> getEnglishDataProperty(OWLIndividual ind, OWLOntologyManager manager,
			OWLDataPropertyExpression expression){
		ArrayList<String> items = new ArrayList<String>();
		Set<OWLLiteral> values = ind.getDataPropertyValues(expression, 
				manager.getOntology(IRI.create(OntologyConstants.MO_PM)));
		
		for(OWLLiteral lit : values){
			if(lit.hasLang("en")){
				System.out.println(lit.getLiteral());
				items.add(lit.getLiteral());
			}
			
		}
		return items;
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
