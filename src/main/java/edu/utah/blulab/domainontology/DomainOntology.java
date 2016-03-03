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
import org.semanticweb.owlapi.model.OWLAxiom;
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
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.AutoIRIMapper;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

public class DomainOntology {
	
	private static OWLOntologyManager manager;
	private static OWLOntology ontology;
	private static OWLDataFactory factory;
	private File ontFile;
	private String ontURI;
	private ArrayList<Term> conceptDictionary;
	private static HashMap<String, Modifier> modifierDictionary;
	private ArrayList<Modifier> closureDictionary;
	private ArrayList<Modifier> pseudoDictionary;
	private ArrayList<Modifier> relationshipDictionary;
	private Set<OWLOntology> imports;
	private static ArrayList<OWLObjectProperty> propertyList, lingPropList, semPropList, numPropList;
	private static ArrayList<OWLClass> schemaClassList;
	
	public DomainOntology(String fileLocation, boolean useLocalFiles) throws Exception{
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		ontFile = new File(fileLocation);
		if(useLocalFiles){
			File directory = ontFile.getParentFile();
			OWLOntologyIRIMapper autoIRIMapper = new AutoIRIMapper(directory, false);
			manager.addIRIMapper(autoIRIMapper);
		}
		ontology = manager.loadOntologyFromOntologyDocument(ontFile);
		ontURI = ontology.getOntologyID().getOntologyIRI().toString();
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
			propertyList.add(prop);
		}
		
		System.out.println("Loaded ontology:" + ontology.getOntologyID().getOntologyIRI().toString());
		System.out.println("Loaded imports: ");
		for(OWLOntology ont : ontology.getImports()){
			System.out.println(ont.getOntologyID().getOntologyIRI().toString());
		}
       
        
	}
	
	
	
	public ArrayList<OWLClass> getSchemaClassList(){
		return schemaClassList;
	}
	
	public ArrayList<OWLObjectProperty> getPropertyList(){
		return propertyList;
	}
	
	public Variable getVariable(String clsDisplayName){
		String domainURI = ontology.getOntologyID().getOntologyIRI().toString();
		//System.out.println(domainURI);
		return new Variable(domainURI + "#" +clsDisplayName, this);
	}
	
	public Variable getVariable(OWLClass cls){
		return new Variable(cls.getIRI().toString(), this);
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
				if(!subCls.asOWLClass().isAnonymous()){
					clsList.add(subCls.asOWLClass());
				}
				
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
	
	public String getObjectPropertyFillerIndividual(OWLIndividual indiv, OWLObjectProperty prop){
		String item = null;
		OWLObjectPropertyExpression exp = (OWLObjectPropertyExpression) prop;
		Set<OWLIndividual> enActions = indiv.asOWLNamedIndividual().getObjectPropertyValues(exp, ontology);
		Iterator<OWLIndividual> iter = enActions.iterator();
		while(iter.hasNext()){
			OWLIndividual in = iter.next();
			item = in.asOWLNamedIndividual().getIRI().toString();
			break;
		}
		return item;
		
	}
	
	public ArrayList<OWLClass> getEquivalentObjectPropertyFillerList(OWLClass cls, ArrayList<OWLObjectProperty> props){
		ArrayList<OWLClass> filler = new ArrayList<OWLClass>();
		Set<OWLClassExpression> exp = cls.getEquivalentClasses(ontology);
		for(OWLClassExpression ce : exp){
			if(ce.getClassExpressionType().compareTo(ClassExpressionType.OBJECT_SOME_VALUES_FROM) == 0){
				OWLObjectSomeValuesFrom obj = (OWLObjectSomeValuesFrom) ce;
				OWLObjectPropertyExpression propExp = obj.getProperty();
				//System.out.println(propExp);
				if(props.contains(propExp.asOWLObjectProperty())){
					OWLClassExpression fillerClass = obj.getFiller();
					//System.out.println("FILLER: " + fillerClass.toString());
					if(!fillerClass.isAnonymous()){
						filler.add(fillerClass.asOWLClass());
					}
					
				}
				
			}
		}
		return filler;
	}
	
	public ArrayList<OWLClass> getObjectPropertyFillerList(OWLClass cls, OWLObjectProperty prop){
		ArrayList<OWLClass> filler = new ArrayList<OWLClass>();
		Set<OWLClassExpression> superCls = cls.getSuperClasses(ontology);
		for(OWLClassExpression ce : superCls){
			if(ce.getClassExpressionType().compareTo(ClassExpressionType.OBJECT_SOME_VALUES_FROM) == 0){
				OWLObjectSomeValuesFrom obj = (OWLObjectSomeValuesFrom) ce;
				OWLObjectPropertyExpression propExp = obj.getProperty();
				//System.out.println(propExp);
				if(prop.equals(propExp.asOWLObjectProperty())){
					OWLClassExpression fillerClass = obj.getFiller();
					//System.out.println("FILLER: " + fillerClass.toString());
					if(!fillerClass.isAnonymous()){
						filler.add(fillerClass.asOWLClass());
					}
					
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
	
	public String getAnnotationString(OWLIndividual ind, OWLAnnotationProperty annotationProperty, String lang){
		String str = "";
		Set<OWLAnnotation> labels = ind.asOWLNamedIndividual().getAnnotations(ontology, annotationProperty);
		if(!labels.isEmpty()){
			Iterator<OWLAnnotation> iter = labels.iterator();
			while(iter.hasNext()){
				OWLAnnotation label = iter.next();
				OWLLiteral literal = (OWLLiteral) label.getValue();
				if(literal.getLang().equals(lang)){
					str = literal.getLiteral();
					break;
				}
				
			}
			
		}
		return str;
	}
	
	public String getAnnotationString(OWLIndividual ind, OWLAnnotationProperty annotationProperty){
		String str = "";
		Set<OWLAnnotation> labels = ind.asOWLNamedIndividual().getAnnotations(ontology, annotationProperty);
		if(!labels.isEmpty()){
			Iterator<OWLAnnotation> iter = labels.iterator();
			while(iter.hasNext()){
				OWLAnnotation label = iter.next();
				OWLLiteral literal = (OWLLiteral) label.getValue();
				str = literal.getLiteral();
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
	
	public ArrayList<String> getAnnotationStringList(OWLIndividual ind, OWLAnnotationProperty annotationProperty, String lang){
		ArrayList<String> list = new ArrayList<String>();
		Set<OWLAnnotation> labels = ind.asOWLNamedIndividual().getAnnotations(ontology, annotationProperty);
		if(!labels.isEmpty()){
			for(OWLAnnotation label : labels){
				OWLLiteral literal = (OWLLiteral) label.getValue();
				if(literal.getLang().equals(lang)){
					list.add(literal.getLiteral());
					
				}
			}
			
		}
		return list;
	}
	
	public ArrayList<Term> createAnchorDictionary(){
		ArrayList<Term> clsList = new ArrayList<Term>();
		
		for(OWLClass cls: this.getAllSubClasses((factory.getOWLClass(IRI.create(OntologyConstants.TARGET))), false)){
			clsList.add(new Term(cls.getIRI().toString(), this));
		}
		return clsList;
	}
	
	public ArrayList<Modifier> createModifierDictionary() throws Exception{
		ArrayList<Modifier> allMods = new ArrayList<Modifier>();
		
		for(OWLClass cls : this.getAllSubClasses(factory.getOWLClass(IRI.create(OntologyConstants.LINGUISTIC_MODIFIER)), true)){
			allMods.add(new Modifier(cls.getIRI().toString(), this));
		}
		for(OWLClass cls : this.getAllSubClasses(factory.getOWLClass(IRI.create(OntologyConstants.SEMANTIC_MODIFIER)), true)){
			allMods.add(new Modifier(cls.getIRI().toString(), this));
		}
		for(OWLClass cls : this.getAllSubClasses(factory.getOWLClass(IRI.create(OntologyConstants.NUMERIC_MODIFIER)), true)){
			allMods.add(new Modifier(cls.getIRI().toString(), this));
		}
		return allMods;
	}
	
	public ArrayList<Modifier> createClosureDictionary(){
		ArrayList<Modifier> clsList = new ArrayList<Modifier>();
		
		for(OWLClass cls: this.getAllSubClasses((factory.getOWLClass(IRI.create(OntologyConstants.CLOSURE))), false)){
			clsList.add(new Modifier(cls.getIRI().toString(), this));
		}
		return clsList;
	}
	
	public ArrayList<Modifier> createPseudoDictionary(){
		ArrayList<Modifier> clsList = new ArrayList<Modifier>();
		
		for(OWLClass cls: this.getAllSubClasses((factory.getOWLClass(IRI.create(OntologyConstants.PSEUDO))), false)){
			clsList.add(new Modifier(cls.getIRI().toString(), this));
		}
		return clsList;
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
	
	public OWLIndividual getIndividual(String uri){
		return factory.getOWLNamedIndividual(IRI.create(uri));
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
	
	public ArrayList<Variable> getAllConditions(){
		ArrayList<Variable> events = new ArrayList<Variable>();
		ArrayList<OWLClass> list = this.getAllSubClasses(factory.getOWLClass(IRI.create(OntologyConstants.CONDITION)), true);
		
		for(OWLClass cls : list){
			if(!schemaClassList.contains(cls)){
				events.add(new Variable(cls.getIRI().toString(), this));
			}
		}
		
		return events;
	}
	
	public ArrayList<Variable> getAllDiseaseDisorders(){
		ArrayList<Variable> events = new ArrayList<Variable>();
		ArrayList<OWLClass> list = this.getAllSubClasses(factory.getOWLClass(IRI.create(OntologyConstants.DISEASE)), true);
		
		for(OWLClass cls : list){
			if(!schemaClassList.contains(cls)){
				events.add(new Variable(cls.getIRI().toString(), this));
			}
		}
		
		return events;
	}
	
	public ArrayList<Variable> getAllSignSymptoms(){
		ArrayList<Variable> events = new ArrayList<Variable>();
		ArrayList<OWLClass> list = this.getAllSubClasses(factory.getOWLClass(IRI.create(OntologyConstants.SYMPTOM)), true);
		
		for(OWLClass cls : list){
			if(!schemaClassList.contains(cls)){
				events.add(new Variable(cls.getIRI().toString(), this));
			}
		}
		
		return events;
	}
	
	public ArrayList<Variable> getAllFindings(){
		ArrayList<Variable> events = new ArrayList<Variable>();
		ArrayList<OWLClass> list = this.getAllSubClasses(factory.getOWLClass(IRI.create(OntologyConstants.FINDING)), true);
		
		for(OWLClass cls : list){
			if(!schemaClassList.contains(cls)){
				events.add(new Variable(cls.getIRI().toString(), this));
			}
		}
		
		return events;
	}
	
	public ArrayList<Variable> getAllEncounters(){
		ArrayList<Variable> events = new ArrayList<Variable>();
		ArrayList<OWLClass> list = this.getAllSubClasses(factory.getOWLClass(IRI.create(OntologyConstants.ENCOUNTER)), true);
		
		for(OWLClass cls : list){
			if(!schemaClassList.contains(cls)){
				events.add(new Variable(cls.getIRI().toString(), this));
			}
		}
		
		return events;
	}
	
	public ArrayList<Variable> getAllMedications(){
		ArrayList<Variable> events = new ArrayList<Variable>();
		ArrayList<OWLClass> list = this.getAllSubClasses(factory.getOWLClass(IRI.create(OntologyConstants.MEDICATION)), true);
		
		for(OWLClass cls : list){
			if(!schemaClassList.contains(cls)){
				events.add(new Variable(cls.getIRI().toString(), this));
			}
		}
		
		return events;
	}
	
	public ArrayList<Variable> getAllObservations(){
		ArrayList<Variable> events = new ArrayList<Variable>();
		ArrayList<OWLClass> list = this.getAllSubClasses(factory.getOWLClass(IRI.create(OntologyConstants.OBSERVATION)), true);
		
		for(OWLClass cls : list){
			if(!schemaClassList.contains(cls)){
				events.add(new Variable(cls.getIRI().toString(), this));
			}
		}
		
		return events;
	}
	
	public ArrayList<Variable> getAllProcedures(){
		ArrayList<Variable> events = new ArrayList<Variable>();
		ArrayList<OWLClass> list = this.getAllSubClasses(factory.getOWLClass(IRI.create(OntologyConstants.PROCEDURE)), true);
		
		for(OWLClass cls : list){
			if(!schemaClassList.contains(cls)){
				events.add(new Variable(cls.getIRI().toString(), this));
			}
		}
		
		return events;
	}
	
	public ArrayList<Variable> getAllDianosticProcedures(){
		ArrayList<Variable> events = new ArrayList<Variable>();
		ArrayList<OWLClass> list = this.getAllSubClasses(factory.getOWLClass(IRI.create(OntologyConstants.DIAGNOSTIC_PROCEDURE)), true);
		
		for(OWLClass cls : list){
			if(!schemaClassList.contains(cls)){
				events.add(new Variable(cls.getIRI().toString(), this));
			}
		}
		
		return events;
	}
	
	public ArrayList<Variable> getAllTherapeuticProcedures(){
		ArrayList<Variable> events = new ArrayList<Variable>();
		ArrayList<OWLClass> list = this.getAllSubClasses(factory.getOWLClass(IRI.create(OntologyConstants.THERAPEUTIC_PROCEDURE)), true);
		
		for(OWLClass cls : list){
			if(!schemaClassList.contains(cls)){
				events.add(new Variable(cls.getIRI().toString(), this));
			}
		}
		
		return events;
	}
	
	public ArrayList<Variable> getAllEntities(){
		ArrayList<Variable> events = new ArrayList<Variable>();
		ArrayList<OWLClass> list = this.getAllSubClasses(factory.getOWLClass(IRI.create(OntologyConstants.ENTITY)), true);
		
		for(OWLClass cls : list){
			if(!schemaClassList.contains(cls)){
				events.add(new Variable(cls.getIRI().toString(), this));
			}
		}
		
		return events;
	}
	
	public ArrayList<Variable> getAllPatients(){
		ArrayList<Variable> events = new ArrayList<Variable>();
		ArrayList<OWLClass> list = this.getAllSubClasses(factory.getOWLClass(IRI.create(OntologyConstants.PATIENT)), true);
		
		for(OWLClass cls : list){
			if(!schemaClassList.contains(cls)){
				events.add(new Variable(cls.getIRI().toString(), this));
			}
		}
		
		return events;
	}
	
	public ArrayList<Variable> getAllAllergies(){
		ArrayList<Variable> events = new ArrayList<Variable>();
		ArrayList<OWLClass> list = this.getAllSubClasses(factory.getOWLClass(IRI.create(OntologyConstants.ALLERGY)), true);
		
		for(OWLClass cls : list){
			if(!schemaClassList.contains(cls)){
				events.add(new Variable(cls.getIRI().toString(), this));
			}
		}
		
		return events;
	}
	
	public ArrayList<String> getAllIndividualURIs(OWLClass cls){
		ArrayList<String> indURIs = new ArrayList<String>();
		Set<OWLIndividual> list = cls.getIndividuals(ontology);
		for(OWLIndividual ind : list){
			indURIs.add(ind.asOWLNamedIndividual().getIRI().toString());
		}
		return indURIs;
	}
		
	
	public String getDisplayName(String iri){
		if(iri != null){
			IRI fullIRI = IRI.create(iri);
			return fullIRI.getShortForm();
		}else{
			return null;
		}
		
	}
	
	public ArrayList<String> getDirectSuperClasses(OWLClass cls){
		ArrayList<String> list = new ArrayList<String>();
		Set<OWLClassExpression> superList = cls.getSuperClasses(ontology);
		for(OWLClassExpression c : superList){
			list.add(c.asOWLClass().getIRI().toString());
		}
		return list;
	}
	
	public ArrayList<String> getDirectSubClasses(OWLClass cls){
		ArrayList<String> list = new ArrayList<String>();
		Set<OWLClassExpression> subList = cls.getSubClasses(ontology);
		for(OWLClassExpression c : subList){
			list.add(c.asOWLClass().getIRI().toString());
		}
		return list;
	}
}
