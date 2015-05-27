package edu.utah.blulab.domainontology;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
	
	private OWLOntologyManager manager;
	private static OWLOntology ontology;
	private static OWLDataFactory factory;
	private PrefixManager pm;
	private File ontFile;
	private String ontURI;
	private ArrayList<Term> conceptDictionary;
	private static ArrayList<String> modifierDictionary;
	private ArrayList<String> closureDictionary;
	private Set<OWLOntology> imports;
	
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
		imports = manager.getImports(ontology);
		
		System.out.println("Loaded " + ontURI);
		for(OWLOntology ont : imports){
			System.out.println("Imported: " + ont.getOntologyID().getOntologyIRI().toString());
			
		}
		
	}
	
	
	public Variable getVariable(String clsName){
		Variable var = new Variable();
		Term term = new Term();
		//Get OWL class
		OWLClass cls = factory.getOWLClass(clsName, pm);
		
		
		//Set variable ID (aka URI)
		var.setVarID(cls.getIRI().toString());
		
		//Set variable name using RDF:label
		var.setVarName(getAnnotationString(cls, factory.getRDFSLabel()));
		
		//Set preferred label for variable concept
		term.setPrefTerm(getAnnotationString(cls, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.PREF_LABEL))));
		
		//Set preferred CUIs for variable concept
		term.setPrefCode(getAnnotationString(cls,
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.PREF_CUI))));
		
		//Set alternate CUIs for variable concept
		term.setAltCode(getAnnotationList(cls,
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.ALT_CUI))));
		
		//Set alternate labels
		term.setSynonym(getAnnotationList(cls, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.ALT_LABEL))));
		
		//Set hidden labels
		term.setMisspelling(getAnnotationList(cls, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.HIDDEN_LABEL))));
		
		//Set abbreviation labels
		term.setAbbreviation(getAnnotationList(cls,
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.ABR_LABEL))));
		
		//Set subjective expression labels
		term.setSubjExp(getAnnotationList(cls,
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.SUBJ_EXP_LABEL))));
		
		//Set regex
		term.setRegex(getAnnotationList(cls, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.REGEX))));
		
		//Add concept to variable and concept dictionary
		var.setTerm(term);
		conceptDictionary.add(term);
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
		
		//Get list of modifiers
		var.setModifiers(getModifiers(cls));

		//System.out.println(var);
		return var;
	}
	
	public Variable getVariable(OWLClass cls){
		Variable var = new Variable();
		Term term = new Term();
		
		
		
		//Set variable ID (aka URI)
		var.setVarID(cls.getIRI().toString());
		
		//Set variable name using RDF:label
		var.setVarName(getAnnotationString(cls, factory.getRDFSLabel()));
		
		//Set preferred label for variable concept
		term.setPrefTerm(getAnnotationString(cls, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.PREF_LABEL))));
		
		//Set preferred CUIs for variable concept
		term.setPrefCode(getAnnotationString(cls,
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.PREF_CUI))));
		
		//Set alternate CUIs for variable concept
		term.setAltCode(getAnnotationList(cls,
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.ALT_CUI))));
		
		//Set alternate labels
		term.setSynonym(getAnnotationList(cls, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.ALT_LABEL))));
		
		//Set hidden labels
		term.setMisspelling(getAnnotationList(cls, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.HIDDEN_LABEL))));
		
		//Set abbreviation labels
		term.setAbbreviation(getAnnotationList(cls,
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.ABR_LABEL))));
		
		//Set subjective expression labels
		term.setSubjExp(getAnnotationList(cls,
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.SUBJ_EXP_LABEL))));
		
		//Set regex
		term.setRegex(getAnnotationList(cls, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.REGEX))));
		
		//Add concept to variable and concept dictionary
		var.setTerm(term);
		conceptDictionary.add(term);
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
		
		//Get list of modifiers
		var.setModifiers(getModifiers(cls));

		//System.out.println(var);
		return var;
	}
	
	public ArrayList<Variable> getAllVariables() {
		ArrayList<Variable> variables = new ArrayList<Variable>();
		ArrayList<OWLClass> elements = new ArrayList<OWLClass>();
		getVariableList(factory.getOWLClass(IRI.create(OntologyConstants.SO_PM + "#Element")), new ArrayList<OWLClass>(), elements);
		System.out.println("THESE ARE THE ELEMENTS IN THE DOMAIN ONTOLOGY...");
		for(OWLClass cls : elements){
			System.out.println(cls.toString());
			variables.add(getVariable(cls));
		}
		return variables;
	}
	
	private void getVariableList(OWLClass cls, ArrayList<OWLClass> elements, ArrayList<OWLClass> varList){
		//make sure class exists and hasn't already been visited
		OWLClass c = factory.getOWLClass(cls.getIRI());
		if(cls == null || elements.contains(cls)){
			return;
		}
		
		//if class belongs to Schema Ontology then do not add to list
		/**if(cls.getIRI().getNamespace().equalsIgnoreCase(OntologyConstants.SO_PM+"#")){
			System.out.println("This belongs to SO.");
		}**/
		
		Set<OWLClassExpression> subExp = cls.getSubClasses(manager.getOntologies());
		//System.out.println("Class " + cls.asOWLClass().getIRI());
		for(OWLClassExpression subCls : subExp){
			//System.out.println("Expression: " + subCls.asOWLClass().toString());
			if(!elements.contains(cls.asOWLClass())){
				elements.add(cls.asOWLClass());
			}
			if(!subCls.asOWLClass().getIRI().getNamespace().equalsIgnoreCase(OntologyConstants.SO_PM+"#")){
				varList.add(subCls.asOWLClass());
			}
			
			getVariableList(subCls.asOWLClass(), elements, varList);
		}
		
		
		
	}
	
	
	
	/**public void getElementCategoryList(OWLClass elementCls, ArrayList<OWLClass> elements){
		
		
		if(elementCls == null || elements.contains(elementCls)){
			return;
		}
		
		//if class belongs to Schema Ontology don't add to list
		
		Set<OWLClassExpression> parentExp = elementCls.getSubClasses(manager.getOntologies());
		System.out.println("Class " + elementCls.asOWLClass().getIRI());
		for(OWLClassExpression subCls : parentExp){
			System.out.println("Expression: " + subCls.asOWLClass().toString());
			if(!elements.contains(elementCls.asOWLClass())){
				elements.add(elementCls.asOWLClass());
			}
			getElementCategoryList(subCls.asOWLClass(), elements);
		}
		
		
	}**/
	
	
	
	private static ArrayList<String> getModifiers(OWLClass cls){
		//HashMap<String, ArrayList<Modifier>> mods = new HashMap<String, ArrayList<Modifier>>();
		ArrayList<String> modifiers = new ArrayList<String>();
		
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
					
					//add modifier to dictionary list
					if(!modifierDictionary.contains(modClass.toString())){
						modifierDictionary.add(modClass.toString());
					}
				}
				
			}

		}
		
		return modifiers;
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
	
	public ArrayList<Term> createConceptDictionary(){
		return conceptDictionary;
	}
	
	public ArrayList<Modifier> createModifierDictionary() throws Exception{
		OWLOntology modOnt = manager.loadOntology(IRI.create(OntologyConstants.MO_PM));
		//System.out.println(modOnt.toString());
		for(String cls : modifierDictionary){
			
			Modifier mod = new Modifier(cls, manager, factory, modOnt);
			System.out.println(mod.toString());
		}
		
		return null;
	}
	
	public ArrayList<Modifier> createClosureDictionary(){
		return null;
	}
	
	
	
}
