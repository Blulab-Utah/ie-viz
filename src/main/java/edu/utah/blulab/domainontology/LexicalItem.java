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
	private int windowSize;
	
	public LexicalItem(OWLIndividual item, OWLOntologyManager manager){
		OWLDataFactory factory = manager.getOWLDataFactory();
		
		//Set uri
		uri = item.asOWLNamedIndividual().getIRI().toString();
		
		//Create term associated with lexical item
		term = new Term();
		term.setPrefTerm(getAnnotationProperty(item, manager, factory, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.PREF_TERM)), OntologyConstants.ENGLISH,
				IRI.create(OntologyConstants.CT_PM)));
		//term.setSynonym(getEnglishDataProperty(item, manager, factory.getOWLDataProperty(IRI.create(OntologyConstants.ALT_TERM))));
		//term.setRegex(getEnglishDataProperty(item, manager, factory.getOWLDataProperty(IRI.create(OntologyConstants.EN_REGEX))));
		
		//Get English action associated with lexical item
		/**Set<OWLIndividual> enActions = item.getObjectPropertyValues(factory.getOWLObjectProperty(IRI.create(OntologyConstants.ACTION_EN)), 
				manager.getOntology(IRI.create(OntologyConstants.MO_PM)));
		for(OWLIndividual action : enActions){
			actionEn = action.asOWLNamedIndividual().getIRI().getShortForm();
		}**/
		
		
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
	
	private static String getAnnotationProperty(OWLIndividual ind, OWLOntologyManager manager, OWLDataFactory factory
			, OWLAnnotationProperty property, String language, IRI ontology){
		String str = "";
		Set<OWLAnnotation> annotations = ind.asOWLNamedIndividual().getAnnotations(manager.getOntology(ontology), 
				property);
		
		if(!annotations.isEmpty()){
			for(OWLAnnotation ann : annotations){
				System.out.println(ann);
			}
		}
			
		return str;
	}
	
	/**private static ArrayList<String> getAnnotationPropertyList(OWLIndividual ind, OWLOntologyManager manager, OWLDataFactory factory
			, OWLAnnotationProperty property, String language){
		ArrayList<String> items = new ArrayList<String>();
		Set<OWLLiteral> values = ind.getDataPropertyValues(expression, 
				manager.getOntology(IRI.create(OntologyConstants.MO_PM)));
		
		for(OWLLiteral lit : values){
			if(lit.hasLang("en")){
				//System.out.println(lit.getLiteral());
				items.add(lit.getLiteral());
			}
			
		}
		return items;
	}**/
	
	
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
	
	public Term getTerm(){
		return term;
	}
	
	public void setTerm(Term term){
		this.term = term;
	}
	
	public int getWindowSize(){
		return windowSize;
	}
	
	public void setWindowSize(int windowSize){
		this.windowSize = windowSize;
	}

	@Override
	public String toString() {
		return "LexicalItem [uri=" + uri + ", term=" + term 
				+ ", windowSize=" + windowSize +", actionEn=" + actionEn + "]";
	}
	
	
}
