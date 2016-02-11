package edu.utah.blulab.domainontology;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
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
	private static HashMap<String, Modifier> modifierDictionary;
	private ArrayList<Modifier> closureDictionary;
	private ArrayList<Modifier> pseudoDictionary;
	private ArrayList<Modifier> relationshipDictionary;
	private Set<OWLOntology> imports;
	private final static String MODIFIERS = "Modifiers";
	private final static String RULES = "Rules";
	private final static String RELATIONS = "Relationships";
	private final static String ANCHOR = "Anchors";
	private static ArrayList<OWLObjectProperty> propertyList, lingPropList, semPropList, numPropList;
	private static ArrayList<OWLClass> schemaClassList;
	
	public DomainOntology(String fileLocation) throws Exception{
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		ontFile = new File(fileLocation);
		ontology = manager.loadOntologyFromOntologyDocument(ontFile);
		ontURI = ontology.getOntologyID().getOntologyIRI().toString();
		pm = new DefaultPrefixManager(ontURI + "#");
		conceptDictionary = new ArrayList<Term>();
		modifierDictionary = new HashMap<String, Modifier>();
		closureDictionary = new ArrayList<Modifier>();
		relationshipDictionary = new ArrayList<Modifier>();
		imports = manager.getImports(ontology);
		propertyList = new ArrayList<OWLObjectProperty>();
		lingPropList = new ArrayList<OWLObjectProperty>();
		semPropList = new ArrayList<OWLObjectProperty>();
		numPropList = new ArrayList<OWLObjectProperty>();
		schemaClassList = this.getSchemaClasses();
		
		ArrayList<OWLObjectProperty> lingList = new ArrayList<OWLObjectProperty>();
		getObjectPropertyHierarchy(factory.getOWLObjectProperty(IRI.create(OntologyConstants.HAS_LING_ATTRIBUTE)), new ArrayList<OWLObjectProperty>(), lingList);
		for(OWLObjectProperty prop : lingList){
			//System.out.println(prop);
			lingPropList.add(prop);
			propertyList.add(prop);
		}
		
		ArrayList<OWLObjectProperty> semList = new ArrayList<OWLObjectProperty>();
		getObjectPropertyHierarchy(factory.getOWLObjectProperty(IRI.create(OntologyConstants.HAS_SEM_ATTRIBUTE)), new ArrayList<OWLObjectProperty>(), semList);
		for(OWLObjectProperty prop : semList){
			//System.out.println(prop);
			semPropList.add(prop);
			propertyList.add(prop);
		}
		
		ArrayList<OWLObjectProperty> numList = new ArrayList<OWLObjectProperty>();
		getObjectPropertyHierarchy(factory.getOWLObjectProperty(IRI.create(OntologyConstants.HAS_NUM_ATTRIBUTE)), new ArrayList<OWLObjectProperty>(), numList);
		for(OWLObjectProperty prop : numList){
			//System.out.println(prop);
			numPropList.add(prop);
			//propertyList.add(prop);
		}
		
		
	}
	
	public ArrayList<OWLClass> getSchemaClassList(){
		return schemaClassList;
	}
	
	public Variable getVariable(String clsName){
		String domainURI = ontology.getOntologyID().getOntologyIRI().toString();
		//System.out.println(domainURI);
		return new Variable(domainURI + "#" +clsName, this);
	}
	
	public Variable getVariable(OWLClass cls){
		return new Variable(cls.getIRI().toString(), this);
		
		/**var.setVarName(getAnnotationString(cls, factory.getRDFSLabel()));
		
		//Get list of anchors
		Iterator<String> iter = details.get(ANCHOR).iterator();
		while(iter.hasNext()){
			OWLClass termCls = factory.getOWLClass(IRI.create(iter.next().replaceAll("<|>", "")));
			//System.out.println("Create anchor for " + iter.next());
			Term term = new Term(termCls, manager, ontology);
			var.setTerm(term);
			break;
		}
		
		
		
		//Get list of modifiers
		ArrayList<Modifier> modifierList = new ArrayList<Modifier>();
		for(String mod : details.get(MODIFIERS)){
			Modifier modifier = new Modifier(mod, manager);
			if(!modifierList.contains(modifier)){
				modifierList.add(modifier);
			}
			if(!modifierDictionary.containsKey(modifier.getUri())){
				modifierDictionary.put(modifier.getUri(), modifier);
			}
		}
		var.setModifiers(modifierList);
		
		//Get list of relations
		var.setRelationships(details.get(RELATIONS));

		//System.out.println(var);
		return var;**/
	}
	
	public ArrayList<Variable> getAllVariables() {
		ArrayList<Variable> variables = new ArrayList<Variable>();
		ArrayList<OWLClass> elements = new ArrayList<OWLClass>();
		getSubClassHierarchy(factory.getOWLClass(IRI.create(OntologyConstants.SO_PM + "#Annotation")), new ArrayList<OWLClass>(), elements, true);
		//System.out.println("THESE ARE THE ELEMENTS IN THE DOMAIN ONTOLOGY...");
		for(OWLClass cls : elements){
			//System.out.println(cls.toString());
			variables.add(getVariable(cls));
		}
		return variables;
	}
	
	private void getSubClassHierarchy(OWLClass cls, ArrayList<OWLClass> visitedCls, ArrayList<OWLClass> clsList, boolean ignoreImports){
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
			if(ignoreImports){
				if(!subCls.asOWLClass().getIRI().getNamespace().equalsIgnoreCase(OntologyConstants.SO_PM+"#") &&
						!subCls.asOWLClass().getIRI().getNamespace().equalsIgnoreCase(OntologyConstants.CT_PM+"#")){
					clsList.add(subCls.asOWLClass());
				}
			}else{
				clsList.add(subCls.asOWLClass());
			}
			
			
			getSubClassHierarchy(subCls.asOWLClass(), visitedCls, clsList, ignoreImports);
		}
		
	}
	
	private void getSuperClassHierarchy(OWLClass cls, ArrayList<OWLClass> visitedCls, ArrayList<OWLClass> clsList, boolean ignoreImports){
		//make sure class exists and hasn't already been visited
		visitedCls.add(factory.getOWLClass(IRI.create(OntologyConstants.ANNOTATION)));
		if(!cls.isAnonymous()){
			if(cls == null || visitedCls.contains(cls) || cls.equals(factory.getOWLClass(IRI.create(OntologyConstants.ANNOTATION)))){
				return;
			}
			
			Set<OWLClassExpression> superExp = cls.getSuperClasses(manager.getOntologies());
			System.out.println("Class " + cls.asOWLClass().getIRI());
			for(OWLClassExpression superCls : superExp){
				System.out.println("Expression: " + superCls.asOWLClass().toString());
				if(!visitedCls.contains(cls.asOWLClass())){
					visitedCls.add(cls.asOWLClass());
				}
				if(ignoreImports){
					if(!superCls.asOWLClass().getIRI().getNamespace().equalsIgnoreCase(OntologyConstants.SO_PM+"#") &&
							!superCls.asOWLClass().getIRI().getNamespace().equalsIgnoreCase(OntologyConstants.CT_PM+"#")){
						if(!superCls.equals(factory.getOWLClass(IRI.create(OntologyConstants.ANNOTATION)))){
							clsList.add(superCls.asOWLClass());
						}
						
					}
				}else{
					if(!superCls.equals(factory.getOWLClass(IRI.create(OntologyConstants.ANNOTATION)))){
						clsList.add(superCls.asOWLClass());
					}
				}
				
				
				getSuperClassHierarchy(superCls.asOWLClass(), visitedCls, clsList, ignoreImports);
			}
		}
		
		
	}
		

	
	private static HashMap<String,ArrayList<String>> getClassDetails(OWLClass cls){
		HashMap<String, ArrayList<String>> details = new HashMap<String, ArrayList<String>>();
		ArrayList<String> modifiers = new ArrayList<String>();
		ArrayList<String> relations = new ArrayList<String>();
		ArrayList<String> anchors = new ArrayList<String>();
		
		Set<OWLClassExpression> exp = cls.getEquivalentClasses(ontology);
		//Set<OWLClassExpression> exp = cls.getSuperClasses(ontology);
		for(OWLClassExpression ce : exp){
			
			if(ce.getClassExpressionType().compareTo(ClassExpressionType.OBJECT_SOME_VALUES_FROM) == 0){
				OWLObjectSomeValuesFrom obj = (OWLObjectSomeValuesFrom) ce;
				OWLObjectPropertyExpression propExp = obj.getProperty();
				//System.out.println(propExp);
				if(propertyList.contains(propExp.asOWLObjectProperty())){
					OWLClassExpression modClass = obj.getFiller();
					//System.out.println(modClass.toString());
					//Modifier modifier = new Modifier(modClass.toString(), manager);
					modifiers.add(modClass.toString());
					details.put(MODIFIERS, modifiers);
				}else if(propExp.asOWLObjectProperty().equals(factory.getOWLObjectProperty(IRI.create(OntologyConstants.HAS_ANCHOR)))){
					OWLClassExpression anchorClass = obj.getFiller();
					//System.out.println("ANCHOR: " + anchorClass.toString());
					anchors.add(anchorClass.toString());
					details.put(ANCHOR, anchors);
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
	
	public OWLClass getEquivalentObjectPropertyFiller(OWLClass cls, OWLObjectProperty prop){
		OWLClass filler = null;
		Set<OWLClassExpression> exp = cls.getEquivalentClasses(ontology);
		for(OWLClassExpression ce : exp){
			if(ce.getClassExpressionType().compareTo(ClassExpressionType.OBJECT_SOME_VALUES_FROM) == 0){
				OWLObjectSomeValuesFrom obj = (OWLObjectSomeValuesFrom) ce;
				OWLObjectPropertyExpression propExp = obj.getProperty();
				//System.out.println(propExp);
				if(propExp.asOWLObjectProperty().equals(prop)){
					OWLClassExpression fillerClass = obj.getFiller();
					//System.out.println("ANCHOR: " + anchorClass.toString());
					filler = fillerClass.asOWLClass();
				}
			}
		}
		return filler;
	}

	public String getAnnotationString(OWLClass cls, OWLAnnotationProperty annotationProperty){
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
	

	
	public ArrayList<String> getAnnotationList(OWLClass cls, OWLAnnotationProperty annotationProperty){
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
		
		getSubClassHierarchy(factory.getOWLClass(IRI.create(OntologyConstants.SO_PM + "#Anchor")), new ArrayList<OWLClass>(), clsList, true);
		for(OWLClass cls : clsList){
			System.out.println(cls);
			Term term = new Term(cls, manager, ontology);
			if(!conceptDictionary.contains(term)){
				conceptDictionary.add(term);
			}
			
		}
		return conceptDictionary;
	}
	
	public ArrayList<Modifier> createModifierDictionary() throws Exception{
		ArrayList<Modifier> allMods = new ArrayList<Modifier>();
		
		for(Modifier mod : modifierDictionary.values()){
			allMods.add(mod);
		}
		
		return allMods;
	}
	
	public ArrayList<Modifier> createClosureDictionary(){
		return null;
	}
	
	private void getObjectPropertyHierarchy(OWLObjectProperty prop, ArrayList<OWLObjectProperty> visitedProp, ArrayList<OWLObjectProperty> propList){
		//make sure prop exists and hasn't already been visited
		
		if(prop == null || visitedProp.contains(prop)){
			return;
		}
		
		Set<OWLObjectPropertyExpression> subExp = prop.getSubProperties(manager.getOntologies());
		
		for(OWLObjectPropertyExpression subProp : subExp){
			
			if(!visitedProp.contains(prop.asOWLObjectProperty())){
				visitedProp.add(prop.asOWLObjectProperty());
			}
			
			propList.add(subProp.asOWLObjectProperty());
			
			getObjectPropertyHierarchy(subProp.asOWLObjectProperty(), visitedProp, propList);
		}
		
	}
	
	public OWLClass getClass(String uri){
		return factory.getOWLClass(IRI.create(uri));
	}
	
	public String getClassURIString(OWLClass cls){
		return cls.asOWLClass().getIRI().toURI().toString();
	}
	
	public OWLDataFactory getFactory(){
		return factory;
	}
	
	public ArrayList<OWLClass> getSchemaClasses(){
		ArrayList<OWLClass> list = new ArrayList<OWLClass>();
		ArrayList<OWLClass> schemaClassList = this.getAllSubClasses(factory.getOWLClass(IRI.create(OntologyConstants.ANNOTATION)), false);
		for(OWLClass cls : schemaClassList){
			//System.out.println(cls.getIRI().getNamespace());
			if(cls.getIRI().getNamespace().equalsIgnoreCase(OntologyConstants.SO_PM+ "#")){
				list.add(cls);
			}
		}
		
		return list;
	}
	
	private ArrayList<OWLClass> getAllSubClasses(OWLClass cls, boolean ignoreImports){
		ArrayList<OWLClass> list = new ArrayList<OWLClass>();
		
		getSubClassHierarchy(cls, new ArrayList<OWLClass>(), list, ignoreImports);
		
		return list;
	}
	
	ArrayList<OWLClass> getAllSuperClasses(OWLClass cls, boolean ignoreImports){
		ArrayList<OWLClass> list = new ArrayList<OWLClass>();
		
		getSuperClassHierarchy(cls, new ArrayList<OWLClass>(), list, ignoreImports);
		
		return list;
	}
	
	public ArrayList<Variable> getAllEvents(){
		ArrayList<Variable> events = new ArrayList<Variable>();
		ArrayList<OWLClass> list = this.getAllSubClasses(factory.getOWLClass(IRI.create(OntologyConstants.EVENT)), true);
		
		for(OWLClass cls : list){
			if(!schemaClassList.contains(cls)){
				events.add(new Variable(cls.getIRI().toString(), this));
			}
		}
		
		return events;
	}
	
		
	
	
	
	
	
}
