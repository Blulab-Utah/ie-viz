package edu.utah.blulab.domainontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class Modifier {
	private String modName, uri;
	private ArrayList<LexicalItem> items;
	private ArrayList<String> closures;
	
	
	public Modifier(String name, OWLOntologyManager manager){
		OWLDataFactory factory = manager.getOWLDataFactory();
		closures = new ArrayList<String>();
		items = new ArrayList<LexicalItem>();
		
		//Get modifier class from name
		uri = name.replaceAll("<|>", "");
		OWLClass modCls = factory.getOWLClass(IRI.create(uri));
		//System.out.println(modCls.toString());
		//Set uri from name
		
		modName = modCls.getIRI().toString().substring(name.indexOf("#")).replaceAll(">", "");
		
		//Get list of closures if any
		Set<OWLClassExpression> superCls = modCls.getSuperClasses(manager.getOntologies());
		for(OWLClassExpression exp : superCls){
			if(exp.getClassExpressionType().compareTo(ClassExpressionType.OBJECT_SOME_VALUES_FROM) == 0){
				OWLObjectSomeValuesFrom obj = (OWLObjectSomeValuesFrom) exp;
				OWLObjectPropertyExpression propExp = obj.getProperty();
				if(propExp.asOWLObjectProperty().equals(factory.getOWLObjectProperty(IRI.create(OntologyConstants.HAS_CLOSURE)))){
					closures.add(obj.getFiller().toString());
				}
			}
		}
		
		Set<OWLIndividual> lexItems = modCls.getIndividuals(manager.getOntologies());
		for(OWLIndividual ind : lexItems){
			//System.out.println(ind.toString());
			items.add(new LexicalItem(ind, manager));
		}
		
		
	}
	
	
	public String getModName() {
		return modName;
	}
	public void setModName(String modName) {
		this.modName = modName;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public ArrayList<LexicalItem> getItems() {
		return items;
	}
	public void setItems(ArrayList<LexicalItem> items) {
		this.items = items;
	}
	public ArrayList<String> getClosures() {
		return closures;
	}
	public void setClosures(ArrayList<String> closures) {
		this.closures = closures;
	}
	@Override
	public String toString() {
		return "Modifier [modName=" + modName + ", uri=" + uri  + ", items=" + items + ", closures=" + closures + "]";
	}
	
	
	
}
