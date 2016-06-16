package edu.utah.blulab.domainontology;

import java.util.*;

import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;


/**
 * @author Melissa Castine
 *
 */
public class Variable {
	private String uri;
	private DomainOntology domain;
	private ArrayList<String> relationships; //May need feeback on the best representation here.
	private ArrayList<String> rules; //This may change once SWRL rules are implemented in ontology.
	
	/**
	 * @param clsURI A string representing the variable class URI
	 * @param domain The DomainOntology object containing variable
	 */
	public Variable(String clsURI, DomainOntology domain){
		this.domain = domain;
		uri = clsURI;
		relationships = new ArrayList<String>();
		rules = new ArrayList<String>();
	}

	/**
	 * Returns the ID associated with a variable
	 * @return A string of the variable ID
	 */
	public String getVarID() {
		return domain.getClassURIString(domain.getClass(uri));
	}

	public String getVarName() {
		return domain.getAnnotationString(domain.getClass(uri), domain.getFactory().getRDFSLabel());
		
	}

	public String getURI(){
		return uri;
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
	
	public ArrayList<LogicExpression<Term>> getAnchor(){
		ArrayList<LogicExpression<Term>> anchorList = new ArrayList<LogicExpression<Term>>();
		ArrayList<OWLClassExpression>	 list = domain.getEquivalentObjectPropertyFillerList(domain.getClass(uri), 
				domain.getFactory().getOWLObjectProperty(IRI.create(OntologyConstants.HAS_ANCHOR)));
		for(OWLClassExpression cls : list){
			if(!cls.isAnonymous()){
				LogicExpression<Term> termExp = new LogicExpression<Term>("SINGLE");
				termExp.add(new Term(cls.asOWLClass().getIRI().toString(), domain));
				anchorList.add(termExp);
			}else{
				if(cls.getClassExpressionType().equals(ClassExpressionType.OBJECT_UNION_OF)){
					LogicExpression<Term> termExp = new LogicExpression<Term>("OR");
					OWLObjectUnionOf union = (OWLObjectUnionOf) cls;
					List<OWLClassExpression> filler = union.getOperandsAsList();
					for(OWLClassExpression c : filler){
						if(!c.isAnonymous()){
							termExp.add(new Term(c.asOWLClass().getIRI().toString(), domain));
						}
					}
					anchorList.add(termExp);
				}
			}
		}
		
		return anchorList;
	}
	
	public HashMap<String, LogicExpression<Modifier>> getModifiers(){
		HashMap<String, LogicExpression<Modifier>> mods = new HashMap<String, LogicExpression<Modifier>>();
		HashMap<String, ArrayList<OWLClassExpression>>	 list = domain.getEquivalentObjectPropertyFillerMap(domain.getClass(uri), domain.getNonNumericPropertyList());

		for(Map.Entry<String, ArrayList<OWLClassExpression>> entry : list.entrySet()){
			String property = entry.getKey();
			ArrayList<OWLClassExpression> expList = entry.getValue();

			for(OWLClassExpression cls : expList){
				if(!cls.isAnonymous()){
					LogicExpression<Modifier> modifierList =  new LogicExpression<Modifier>("SINGLE");
					modifierList.add(new Modifier(cls.asOWLClass().getIRI().toString(), domain));
					mods.put(property, modifierList);
				}else{
					if(cls.getClassExpressionType().equals(ClassExpressionType.OBJECT_UNION_OF)){
						LogicExpression<Modifier> modifierList = new LogicExpression<Modifier>("OR");
						OWLObjectUnionOf union = (OWLObjectUnionOf) cls;
						List<OWLClassExpression> filler = union.getOperandsAsList();
						for(OWLClassExpression c : filler){
							if(!c.isAnonymous()){
								modifierList.add(new Modifier(c.asOWLClass().getIRI().toString(), domain));
							}
						}
						mods.put(property, modifierList);
					}
				}
			}
		}

		return mods;
	}
	
	public HashMap<String, Variable> getRelationships(){
		HashMap<String, Variable> relations = new HashMap<String, Variable>();
		for(OWLObjectProperty prop : domain.getRelationsList()){
			ArrayList<OWLClass> list = domain.getObjectPropertyFillerList(domain.getClass(uri), prop);
			for(OWLClass cls: list){
				relations.put(prop.asOWLObjectProperty().getIRI().getShortForm(), new Variable(cls.getIRI().toString(), this.domain));
			}
			
		}
		return relations;
	}
	
	public ArrayList<NumericModifier> getNumericModifiers(){
		ArrayList<NumericModifier> mods = new ArrayList<NumericModifier>();
		ArrayList<OWLClassExpression>	 list = domain.getEquivalentObjectPropertyFillerList(domain.getClass(uri), domain.getNumericPropertyList());
		for(OWLClassExpression cls : list){
			if(!cls.isAnonymous()){
				mods.add(new NumericModifier(cls.asOWLClass().getIRI().toString(), domain));
			}
		}
		
		return mods;
	}
	
	public boolean hasNumericModifiers(){
		return !this.getNumericModifiers().isEmpty();
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
	
	
	/**
	 * @return
	 */
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

	public ArrayList<String> getAllParents(){
		ArrayList<String> parentAncestry = new ArrayList<String>();
		parentAncestry = domain.getAllSuperClasses(domain.getClass(uri), false);
		return parentAncestry;
	}


	@Override
	public String toString() {
		return "Variable [varID=" + this.getVarID() + ", varName=" + this.getVarName()
				//+ ", category=" + this.getSemanticCategory()
				//+ ", parentAncestry=" + this.getAllParents()
				+ ", concept=" + this.getAnchor().toString() 
				+ "\n\t, modifiers=" + this.getModifiers()
				//+ "\n\t, numerics=" + this.getNumericModifiers()
				//+ "\n\t, relations=" + this.getRelationships()
				+"]";
	}
	
	
	
	
}
