package edu.utah.blulab.domainontology;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

public class DomainOntology {
	
	private OWLOntologyManager manager;
	private OWLOntology ontology;
	private OWLDataFactory factory;
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
		ArrayList<String> labelSet;
		
		//Set variable ID (aka URI)
		var.setVarID(cls.getIRI().toString());
		//Set variable name using RDF:label
		Set<OWLAnnotation> labels = cls.getAnnotations(ontology, factory.getRDFSLabel());
		if(!labels.isEmpty()){
			Iterator<OWLAnnotation> iter = labels.iterator();
			while(iter.hasNext()){
				OWLAnnotation label = iter.next();
				String temp = label.getValue().toString();
				temp = temp.substring(temp.indexOf("\"")+1, temp.lastIndexOf("\""));
				var.setVarName(temp);
				break;
			}
		}
		//Set preferred label for variable concept
		Set<OWLAnnotation> prefLabels = cls.getAnnotations(ontology, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.PREF_LABEL)));
		if(!prefLabels.isEmpty()){
			Iterator<OWLAnnotation> iter = prefLabels.iterator();
			while(iter.hasNext()){
				OWLAnnotation preflabel = iter.next();
				String temp1 = preflabel.getValue().toString();
				temp1 = temp1.substring(temp1.indexOf("\"")+1, temp1.lastIndexOf("\""));
				var.setPrefLabel(temp1);
				break;
			}
		}
		//Set preferred CUIs for variable concept
		Set<OWLAnnotation> prefCUIs = cls.getAnnotations(ontology, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.PREF_CUI)));
		if(!prefCUIs.isEmpty()){
			Iterator<OWLAnnotation> iter = prefCUIs.iterator();
			while(iter.hasNext()){
				OWLAnnotation prefCUI = iter.next();
				String temp = prefCUI.getValue().toString();
				temp = temp.substring(temp.indexOf("\"")+1, temp.lastIndexOf("\""));
				var.setPrefCUI(temp);
				break;
			}
		}
		
		//Set alternate CUIs for variable concept
		Set<OWLAnnotation> altCUIs = cls.getAnnotations(ontology, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.ALT_CUI)));
		if(!altCUIs.isEmpty()){
			Iterator<OWLAnnotation> iter = altCUIs.iterator();
			labelSet = var.getAltCUIs();
			while(iter.hasNext()){
				OWLAnnotation altCUI = iter.next();
				String temp = altCUI.getValue().toString();
				temp = temp.substring(temp.indexOf("\"")+1, temp.lastIndexOf("\""));
				labelSet.add(temp);
			}
			var.setAltCUIs(labelSet);
		}
		
		//Set alternate labels
		Set<OWLAnnotation> altLabels = cls.getAnnotations(ontology, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.ALT_LABEL)));
		if(!altLabels.isEmpty()){
			Iterator<OWLAnnotation> iter = altLabels.iterator();
			labelSet = var.getAltLabels();
			while(iter.hasNext()){
				OWLAnnotation altLabel = iter.next();
				String temp = altLabel.getValue().toString();
				temp = temp.substring(temp.indexOf("\"")+1, temp.lastIndexOf("\""));
				labelSet.add(temp);
			}
			var.setAltLabels(labelSet);
		}
		
		//Set hidden labels
		Set<OWLAnnotation> hiddenLabels = cls.getAnnotations(ontology, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.HIDDEN_LABEL)));
		if(!hiddenLabels.isEmpty()){
			Iterator<OWLAnnotation> iter = hiddenLabels.iterator();
			labelSet = var.getHiddenLabels();
			while(iter.hasNext()){
				OWLAnnotation hiddenLabel = iter.next();
				String temp = hiddenLabel.getValue().toString();
				temp = temp.substring(temp.indexOf("\"")+1, temp.lastIndexOf("\""));
				labelSet.add(temp);
			}
			var.setHiddenLabels(labelSet);
		}
		
		//Set abbreviation labels
		Set<OWLAnnotation> abbrLabels = cls.getAnnotations(ontology, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.ABR_LABEL)));
		if(!abbrLabels.isEmpty()){
			Iterator<OWLAnnotation> iter = abbrLabels.iterator();
			labelSet = var.getAbbrLabels();
			while(iter.hasNext()){
				OWLAnnotation abbrLabel = iter.next();
				String temp = abbrLabel.getValue().toString();
				temp = temp.substring(temp.indexOf("\"")+1, temp.lastIndexOf("\""));
				labelSet.add(temp);
			}
			var.setAbbrLabels(labelSet);
		}
		
		//Set subjective expression labels
		Set<OWLAnnotation> subExpLabels = cls.getAnnotations(ontology, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.SUBJ_EXP_LABEL)));
		if(!subExpLabels.isEmpty()){
			Iterator<OWLAnnotation> iter = subExpLabels.iterator();
			labelSet = var.getSubjExpLabels();
			while(iter.hasNext()){
				OWLAnnotation subjExpLabel = iter.next();
				String temp = subjExpLabel.getValue().toString();
				temp = temp.substring(temp.indexOf("\"")+1, temp.lastIndexOf("\""));
				labelSet.add(temp);
			}
			var.setSubjExpLabels(labelSet);
		}
		
		//Set regex
		Set<OWLAnnotation> regex = cls.getAnnotations(ontology, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.REGEX)));
		if(!regex.isEmpty()){
			Iterator<OWLAnnotation> iter = regex.iterator();
			labelSet = var.getRegex();
			while(iter.hasNext()){
				OWLAnnotation exp = iter.next();
				String temp = exp.getValue().toString();
				temp = temp.substring(temp.indexOf("\"")+1, temp.lastIndexOf("\""));
				labelSet.add(temp);
			}
			var.setRegex(labelSet);
		}
		
		//Set section headings
		Set<OWLAnnotation> headings = cls.getAnnotations(ontology, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.SEC_HEADING)));
		if(!headings.isEmpty()){
			Iterator<OWLAnnotation> iter = headings.iterator();
			labelSet = var.getSectionHeadings();
			while(iter.hasNext()){
				OWLAnnotation exp = iter.next();
				String temp = exp.getValue().toString();
				temp = temp.substring(temp.indexOf("\"")+1, temp.lastIndexOf("\""));
				labelSet.add(temp);
			}
			var.setSectionHeadings(labelSet);
		}
		
		//Set document types
		Set<OWLAnnotation> docTypes = cls.getAnnotations(ontology, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.DOC_TYPE)));
		if(!docTypes.isEmpty()){
			Iterator<OWLAnnotation> iter = docTypes.iterator();
			labelSet = var.getReportTypes();
			while(iter.hasNext()){
				OWLAnnotation exp = iter.next();
				String temp = exp.getValue().toString();
				temp = temp.substring(temp.indexOf("\"")+1, temp.lastIndexOf("\""));
				labelSet.add(temp);
			}
			var.setReportTypes(labelSet);
		}
		
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
		Set<OWLAnnotation> window = cls.getAnnotations(ontology, 
				factory.getOWLAnnotationProperty(IRI.create(OntologyConstants.WINDOW)));
		if(!window.isEmpty()){
			Iterator<OWLAnnotation> iter = window.iterator();
			while(iter.hasNext()){
				OWLAnnotation winsize = iter.next();
				String temp = winsize.getValue().toString();
				temp = temp.substring(temp.indexOf("\"")+1, temp.lastIndexOf("\""));
				
				var.setWindowSize(Integer.parseInt(temp));
				break;
			}
		}
		
		
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

	
}
