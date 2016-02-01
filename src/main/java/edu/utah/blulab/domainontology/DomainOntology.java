package edu.utah.blulab.domainontology;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

public class DomainOntology {
	
	private static OWLOntologyManager manager;
	private static OWLOntology ontology;
	private static OWLDataFactory factory;
	private PrefixManager pm;
	private File ontFile;
	private String ontURI;
	private ArrayList<Term> conceptDictionary;
	private static ArrayList<String> modifierDictionary;
	private ArrayList<String> closureDictionary;
	private ArrayList<String> relationshipDictionary;
	private Set<OWLOntology> imports;
	private final static String MODIFIERS = "Modifiers";
	private final String RULES = "Rules";
	private final static String RELATIONS = "Relationships";
	
	public DomainOntology(String fileLocation) throws Exception{
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		ontFile = new File(fileLocation);
		ontology = manager.loadOntologyFromOntologyDocument(ontFile);
		ontURI = ontology.getOntologyID().getOntologyIRI().toString();
		pm = new DefaultPrefixManager(ontURI + "#");
		conceptDictionary = new ArrayList<Term>();
		modifierDictionary = new ArrayList<String>();
		closureDictionary = new ArrayList<String>();
		relationshipDictionary = new ArrayList<String>();
		imports = manager.getImports(ontology);
		
		System.out.println("Loaded " + ontURI);
		for(OWLOntology ont : imports){
			System.out.println("Imported: " + ont.getOntologyID().getOntologyIRI().toString());
			
		}
		
	}
	
	
	public Variable getVariable(String clsName){
		Variable var = new Variable();
		//Get OWL class
		OWLClass cls = factory.getOWLClass(clsName, pm);
		
		getVariable(cls);
		
		return var;
	}
	
	public Variable getVariable(OWLClass cls){
		Variable var = new Variable();
				
		//Set variable ID (aka URI)
		var.setVarID(cls.getIRI().toString());
		
		//Set variable name using RDF:label
		var.setVarName(getAnnotationString(cls, factory.getRDFSLabel()));
		
		//Create term for target concept belonging to variable
		Term term = new Term(cls, manager, ontology);
		//Add target to variable
		var.setTerm(term);
		//Set section headings
		var.setSectionHeadings(getAnnotationList(cls, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.SEC_HEADING))));
		
		//Set document types
		var.setReportTypes(getAnnotationList(cls, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.DOC_TYPE))));
		
		//Get semantic categories of class
		ArrayList<String> cats = new ArrayList<String>();
		Set<OWLClassExpression> parents = cls.getSuperClasses(ontology);
		for(OWLClassExpression parentCls : parents){
			//System.out.println("TYPE: " + parentCls.getClassExpressionType().getName() + "   "  + parentCls.getClassExpressionType().compareTo(ClassExpressionType.OWL_CLASS));
			if(parentCls.getClassExpressionType().compareTo(ClassExpressionType.OWL_CLASS) == 0){
				cats.add(parentCls.toString());
			}
		}
		var.setSemanticCategory(cats);
		
		//Set window size if different from default, else leave window size = 6
		String temp = getAnnotationString(cls, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.WINDOW)));
		if(!temp.isEmpty()){
			var.setWindowSize(Integer.parseInt(temp));
		}
		
		HashMap<String, ArrayList<String>> details = getClassDetails(cls);
		
		//Get list of modifiers
		//var.setModifiers(getModifiers(cls));
		var.setModifiers(details.get(MODIFIERS));
		
		//Get list of relations
		var.setRelationships(details.get(RELATIONS));

		//System.out.println(var);
		return var;
	}
	
	public ArrayList<Variable> getAllVariables() {
		ArrayList<Variable> variables = new ArrayList<Variable>();
		ArrayList<OWLClass> elements = new ArrayList<OWLClass>();
		getClassHierarchy(factory.getOWLClass(IRI.create(OntologyConstants.SO_PM + "#Annotation")), new ArrayList<OWLClass>(), elements);
		System.out.println("THESE ARE THE ELEMENTS IN THE DOMAIN ONTOLOGY...");
		for(OWLClass cls : elements){
			System.out.println(cls.toString());
			variables.add(getVariable(cls));
		}
		return variables;
	}
	
	private void getClassHierarchy(OWLClass cls, ArrayList<OWLClass> visitedCls, ArrayList<OWLClass> clsList){
		//make sure class exists and hasn't already been visited
		
		if(cls == null || visitedCls.contains(cls)){
			return;
		}
		
		Set<OWLClassExpression> subExp = cls.getSubClasses(manager.getOntologies());
		//System.out.println("Class " + cls.asOWLClass().getIRI());
		for(OWLClassExpression subCls : subExp){
			//System.out.println("Expression: " + subCls.asOWLClass().toString());
			if(!visitedCls.contains(cls.asOWLClass())){
				visitedCls.add(cls.asOWLClass());
			}
			if(!subCls.asOWLClass().getIRI().getNamespace().equalsIgnoreCase(OntologyConstants.SO_PM+"#") &&
					!subCls.asOWLClass().getIRI().getNamespace().equalsIgnoreCase(OntologyConstants.CT_PM+"#")){
				clsList.add(subCls.asOWLClass());
			}
			
			getClassHierarchy(subCls.asOWLClass(), visitedCls, clsList);
		}
		
	}
	
	
		
	private static HashMap<String,ArrayList<String>> getClassDetails(OWLClass cls){
		HashMap<String, ArrayList<String>> details = new HashMap<String, ArrayList<String>>();
		ArrayList<String> modifiers = new ArrayList<String>();
		ArrayList<String> relations = new ArrayList<String>();
		
		Set<OWLClassExpression> exp = cls.getSuperClasses(ontology);
		for(OWLClassExpression ce : exp){
			if(ce.getClassExpressionType().compareTo(ClassExpressionType.OBJECT_SOME_VALUES_FROM) == 0){
				OWLObjectSomeValuesFrom obj = (OWLObjectSomeValuesFrom) ce;
				OWLObjectPropertyExpression propExp = obj.getProperty();
				
				if(propExp.asOWLObjectProperty().equals(factory.getOWLObjectProperty(IRI.create(OntologyConstants.HAS_SEM_ATTRIBUTE))) |
						propExp.asOWLObjectProperty().equals(factory.getOWLObjectProperty(IRI.create(OntologyConstants.HAS_LING_ATTRIBUTE)))){
					OWLClassExpression modClass = obj.getFiller();
					//System.out.println(modClass.toString());
					modifiers.add(modClass.toString());
					details.put(MODIFIERS, modifiers);
					//add modifier to dictionary list
					if(!modifierDictionary.contains(modClass.toString())){
						modifierDictionary.add(modClass.toString());
					}
				}else{
					//Get remaining axioms and parse out the relation and object to add to variable description
					//System.out.println(obj.toString());
					String relation = obj.getProperty().getNamedProperty().getIRI().getShortForm();
					String object = obj.getFiller().toString();
					//System.out.println(object);
					relations.add(relation + "|" + object);
					details.put(RELATIONS, relations);
				}
				
			}

		}
		
		return details;
	}
	
	private static String getAnnotationString(OWLClass cls, OWLAnnotationProperty annotationProperty){
		String str = "";
		Set<OWLAnnotation> labels = cls.getAnnotations(ontology, annotationProperty);
		if(!labels.isEmpty()){
			Iterator<OWLAnnotation> iter = labels.iterator();
			while(iter.hasNext()){
				OWLAnnotation label = iter.next();
				String temp = label.getValue().toString();
				temp = temp.substring(temp.indexOf("\"")+1, temp.lastIndexOf("\""));
				str = temp;
				break;
			}
			
		}
		return str;
	}
	

	
	private static ArrayList<String> getAnnotationList(OWLClass cls, OWLAnnotationProperty annotationProperty){
		ArrayList<String> labelSet = new ArrayList<String>();
		Set<OWLAnnotation> annotations = cls.getAnnotations(ontology, annotationProperty);
		if(!annotations.isEmpty()){
			Iterator<OWLAnnotation> iter = annotations.iterator();
			while(iter.hasNext()){
				OWLAnnotation ann = iter.next();
				String temp = ann.getValue().toString();
				temp = temp.substring(temp.indexOf("\"")+1, temp.lastIndexOf("\""));
				labelSet.add(temp);
			}
		}
		return labelSet;
	}
	
	public ArrayList<Term> createTargetDictionary(){
		ArrayList<OWLClass> clsList = new ArrayList<OWLClass>();
		
		getClassHierarchy(factory.getOWLClass(IRI.create(OntologyConstants.SO_PM + "#Anchor")), new ArrayList<OWLClass>(), clsList);
		LinkedList<OWLClass> list = new LinkedList<OWLClass>();
		list.addAll(clsList);
		
		
			
		
		return conceptDictionary;
	}
	
	public ArrayList<Modifier> createModifierDictionary() throws Exception{
		ArrayList<Modifier> modifiers = new ArrayList<Modifier>();
		for(String cls : modifierDictionary){
			
			//Modifier mod = new Modifier(cls, manager);
			modifiers.add(new Modifier(cls, manager));
			//System.out.println(mod.toString());
		}
		
		return modifiers;
	}
	
	public ArrayList<Modifier> createClosureDictionary(){
		return null;
	}
	
	private static ArrayList<OWLClass> getChildren(OWLClass cls){
		ArrayList<OWLClass> list = new ArrayList<OWLClass>();
		Set<OWLClassExpression> subCls = cls.getSubClasses(ontology);
		for(OWLClassExpression sub : subCls){
			list.add(sub.asOWLClass());
		}
		return list;
	}
	
	private static void createTargetHierarchy(OWLClass cls, LinkedList<OWLClass> list, ArrayList<Term> hierarchy){
		if(list.isEmpty()){
			return;
		}
		Term term = new Term(cls, manager, ontology);
		ArrayList<OWLClass> subs = getChildren(list.removeFirst());
		if(subs.isEmpty()){
			return;
		}
		ArrayList<Term> children = new ArrayList<Term>();
		for(OWLClass c : subs){
			
			list.remove(c);
			createTargetHiearchy(c, list, hierarchy);
		}
		
		
	}
	
	
	
}
