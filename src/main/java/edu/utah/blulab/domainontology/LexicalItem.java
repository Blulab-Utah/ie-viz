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
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class LexicalItem {
	private DomainOntology domain;
	private String uri,  actionEn, actionSv, actionDe, prefTermEn; //, prefTermSv, prefTermDe
	private int windowSize;
	
	public LexicalItem(String itemURI, DomainOntology domain){
		uri = itemURI;
		this.domain = domain;
		
		/**if(getEnPrefLabel(item, manager, factory) != null){
			term.setPrefTerm(getEnPrefLabel(item, manager, factory));
		}
		if(getEnglishDataProperty(item, manager, factory.getOWLDataProperty(IRI.create(OntologyConstants.ALT_LABEL))) != null){
			term.setSynonym(getEnglishDataProperty(item, manager, factory.getOWLDataProperty(IRI.create(OntologyConstants.ALT_LABEL))));
		}
		if(getEnglishDataProperty(item, manager, factory.getOWLDataProperty(IRI.create(OntologyConstants.REGEX))) != null){
			term.setRegex(getEnglishDataProperty(item, manager, factory.getOWLDataProperty(IRI.create(OntologyConstants.REGEX))));
		}
		
		
		//Get English action associated with lexical item
		Set<OWLIndividual> enActions = item.getObjectPropertyValues(factory.getOWLObjectProperty(IRI.create(OntologyConstants.ACTION_EN)), 
				manager.getOntology(IRI.create(OntologyConstants.CT_PM)));
		if(!enActions.isEmpty()){
			for(OWLIndividual action : enActions){
				actionEn = action.asOWLNamedIndividual().getIRI().getShortForm();
			}
		}**/
		
		
	}
	
	
	
	/**private static ArrayList<String> getEnglishDataProperty(OWLIndividual ind, OWLOntologyManager manager,
			OWLDataPropertyExpression expression){
		ArrayList<String> items = new ArrayList<String>();
		Set<OWLLiteral> values = ind.getDataPropertyValues(expression, 
				manager.getOntology(IRI.create(OntologyConstants.CT_PM)));
		
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

	public String getPrefTerm(){
		return domain.getAnnotationString(domain.getIndividual(uri), 
				domain.getFactory().getOWLAnnotationProperty(IRI.create(OntologyConstants.PREF_LABEL)), "en");
	}
	
	public ArrayList<String> getPrefCode(){
		return domain.getAnnotationStringList(domain.getIndividual(uri), 
				domain.getFactory().getOWLAnnotationProperty(IRI.create(OntologyConstants.PREF_CUI)), "en");
	}
	
	public ArrayList<String> getSynonym(){
		return domain.getAnnotationStringList(domain.getIndividual(uri), 
				domain.getFactory().getOWLAnnotationProperty(IRI.create(OntologyConstants.ALT_LABEL)), "en");
	}
	
	public ArrayList<String>getMisspelling(){
		return domain.getAnnotationStringList(domain.getIndividual(uri), 
				domain.getFactory().getOWLAnnotationProperty(IRI.create(OntologyConstants.HIDDEN_LABEL)), "en");
	}
	
	public ArrayList<String>getAbbreviation(){
		return domain.getAnnotationStringList(domain.getIndividual(uri), 
				domain.getFactory().getOWLAnnotationProperty(IRI.create(OntologyConstants.ABR_LABEL)), "en");
	}
	
	public ArrayList<String>getSubjExp(){
		return domain.getAnnotationStringList(domain.getIndividual(uri), 
				domain.getFactory().getOWLAnnotationProperty(IRI.create(OntologyConstants.SUBJ_EXP_LABEL)), "en");
	}
	
	public ArrayList<String>getRegex(){
		return domain.getAnnotationStringList(domain.getIndividual(uri), 
				domain.getFactory().getOWLAnnotationProperty(IRI.create(OntologyConstants.REGEX)), "en");
	}
	
	public ArrayList<String>getAltCode(){
		return domain.getAnnotationStringList(domain.getIndividual(uri), 
				domain.getFactory().getOWLAnnotationProperty(IRI.create(OntologyConstants.ALT_CUI)), "en");
	}
	
	public String getActionEn(boolean returnDisplayName) {
		String str = domain.getObjectPropertyFiller(domain.getIndividual(uri),
				domain.getFactory().getOWLObjectProperty(IRI.create(OntologyConstants.ACTION_EN)));
		if(returnDisplayName){
			str = domain.getDisplayName(str);
		}
		return str;
	}

	public String getActionSv() {
		return actionSv;
	}

	public String getActionDe() {
		return actionDe;
	}
	
	public int getWindowSize(){
		String str = domain.getAnnotationString(domain.getIndividual(uri), 
				domain.getFactory().getOWLAnnotationProperty(IRI.create(OntologyConstants.WINDOW)));
		if(str.isEmpty() || str.equals(null)){
			return 8;
		}else{
			return Integer.parseInt(str);
		}
		
	}
	

	@Override
	public String toString() {
		return "LexicalItem [uri=" + uri + ", prefLabel=" + this.getPrefTerm() + 
				 ", regex=" + this.getRegex() +
				 ", altLabel=" + this.getSynonym() +
				", windowSize=" + this.getWindowSize() + ", actionEn=" + this.getActionEn(true) + "]";
	}
	
	
}
