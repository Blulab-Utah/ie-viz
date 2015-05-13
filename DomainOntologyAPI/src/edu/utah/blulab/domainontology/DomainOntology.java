package edu.utah.blulab.domainontology;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
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
	
	public DomainOntology(String fileLocation) throws Exception{
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		ontFile = new File(fileLocation);
		ontology = manager.loadOntologyFromOntologyDocument(ontFile);
		ontURI = ontology.getOntologyID().getOntologyIRI().toString();
		pm = new DefaultPrefixManager(ontURI + "#");
		
		System.out.println("Loaded " + ontURI);		
		
	}
	
	
	public Variable getVariable(String clsName){
		Variable var = new Variable();
		//Get OWL class
		OWLClass cls = factory.getOWLClass(clsName, pm);
		
		
		//Set variable ID (aka URI)
		var.setVarID(cls.getIRI().toString());
		
		//Set variable name using RDF:label
		var.setVarName(getAnnotationString(cls, factory.getRDFSLabel()));
		
		//Set preferred label for variable concept
		var.setPrefLabel(getAnnotationString(cls, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.PREF_LABEL))));
		
		//Set preferred CUIs for variable concept
		var.setPrefCUI(getAnnotationString(cls,
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.PREF_CUI))));
		
		//Set alternate CUIs for variable concept
		var.setAltCUIs(getAnnotationList(cls,
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.ALT_CUI))));
		
		//Set alternate labels
		var.setAltLabels(getAnnotationList(cls, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.ALT_LABEL))));
		
		//Set hidden labels
		var.setHiddenLabels(getAnnotationList(cls, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.HIDDEN_LABEL))));
		
		//Set abbreviation labels
		var.setAbbrLabels(getAnnotationList(cls,
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.ABR_LABEL))));
		
		//Set subjective expression labels
		var.setSubjExpLabels(getAnnotationList(cls,
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.SUBJ_EXP_LABEL))));
		
		//Set regex
		var.setRegex(getAnnotationList(cls, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.REGEX))));
		
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
		var.setSemanticCategories(cats);
		
		//Set window size if different from default, else leave window size = 6
		var.setWindowSize(Integer.parseInt(getAnnotationString(cls, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.WINDOW)))));
		
		//Get list of modifiers
		var.setModifiers(getModifiers(cls, ontology));

		System.out.println(var);
		return var;
	}
	
	public List<Variable> getAllVariables() {
		ArrayList<Variable> variables = new ArrayList<Variable>();
		
		return variables;
	}
	
	public ArrayList<IRI> getDomainVariableList(){
		
		return null;
	}
	
	public static Modifier getModifier(OWLClass modCls){
		Modifier mod = new Modifier();
		//Set modifier URI
		mod.setUri(modCls.getIRI().toString());
		//Set modifier pretty name
		mod.setModName(mod.getUri().substring(mod.getUri().indexOf("#")+1));
		//Set modifier prefCUI
		mod.setPrefCUI(getAnnotationString(modCls,
				factory.getOWLAnnotationProperty(
						IRI.create("http://blulab.chpc.utah.edu/ontologies/ModifierOntology.owl##prefCUI"))));
		//Set lexical variant items
		try {
			mod.setItems(getLexicalItemList(modCls.getIRI().toString()));
		} catch (OWLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Set closures with lexical items
		
		
		
		//System.out.println(mod.toString());
		return mod;
	}
	
	private static ArrayList<Modifier> getModifiers(OWLClass cls, OWLOntology ontology){
		//HashMap<String, ArrayList<Modifier>> mods = new HashMap<String, ArrayList<Modifier>>();
		ArrayList<Modifier> modifiers = new ArrayList<Modifier>();
		
		Set<OWLClassExpression> exp = cls.getSuperClasses(ontology);
		for(OWLClassExpression ce : exp){
			if(ce.getClassExpressionType().compareTo(ClassExpressionType.OBJECT_SOME_VALUES_FROM) == 0){
				
				Modifier mod = getModifier(getSomeObjectPropertyFiller(ce));
				modifiers.add(mod);
			}

		}
		
		return modifiers;
	}
	
	private static OWLClass getSomeObjectPropertyFiller(OWLClassExpression ce){
		//System.out.println(ce);
		OWLObjectSomeValuesFrom obj = (OWLObjectSomeValuesFrom) ce;
		OWLObjectPropertyExpression rest = obj.getProperty();
		//System.out.print(rest.toString());
		OWLClassExpression modClass = obj.getFiller();
		//System.out.println("  --->  " + modClass.toString());
		return modClass.asOWLClass();
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
	
	private static ArrayList<LexicalItem> getLexicalItemList(String modifierURI) throws OWLException{
		OWLOntologyManager manMO = OWLManager.createOWLOntologyManager();
		OWLDataFactory factMO = manMO.getOWLDataFactory();
		OWLOntology mo = manMO.loadOntologyFromOntologyDocument(IRI.create(OntologyConstants.MO_PM));
		
		ArrayList<LexicalItem> variants = new ArrayList<LexicalItem>();
		//Get all individuals for modifier class
		OWLClass modifier = factory.getOWLClass(IRI.create(modifierURI));
		Set<OWLIndividual> lexicalVariants = modifier.getIndividuals(mo);
		for(OWLIndividual ind : lexicalVariants){
			System.out.println(ind.toString());
		}
		
		return variants;
	}
	
	private static LexicalItem getLexcialItem(OWLIndividual lexicalItem){
		LexicalItem variant = new LexicalItem();
		
		return variant;
	}

	
}
