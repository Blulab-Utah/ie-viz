package ontologyUtils;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Created by melissa on 7/14/16.
 */
public class owlToUIMADescriptor {
    private static final String CT_PM = "http://blulab.chpc.utah.edu/ontologies/v2/ConText.owl";
    private static final String SO_PM = "http://blulab.chpc.utah.edu/ontologies/v2/Schema.owl";
    private static final String TM_PM = "http://blulab.chpc.utah.edu/ontologies/TermMapping.owl";


    public static void main(String[] args) throws Exception {
        File inputFile;
        File outputFile;
        final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

        if(args.length < 2) {
            System.err.print("You must provide the input file and output file locations.");
        }
        inputFile = new File(args[0]);
        outputFile = new File(args[1]);

        if(!outputFile.exists()){
            outputFile.createNewFile();
        }

        final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        final OWLDataFactory factory = manager.getOWLDataFactory();
        final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(inputFile);

        Element typeDesc = new Element("typeSystemDescription", ontology.getOntologyID().getOntologyIRI().toString());

        Document doc = new Document(typeDesc);
        doc.setRootElement(typeDesc);

        Element name = new Element("name");
        String typeName = inputFile.getName().substring(0, inputFile.getName().indexOf("."));
        name.addContent(typeName + "TypeSystem");
        doc.getRootElement().addContent(name);

        Element description = new Element("description");
        doc.getRootElement().addContent(description);
        Element version = new Element("version");
        version.addContent("1.0");
        doc.getRootElement().addContent(version);
        Element vendor = new Element("vendor");
        doc.getRootElement().addContent(vendor);
        Element types = new Element("types");
        doc.getRootElement().addContent(types);

        //walk ontology classes to  build types
        ArrayList<OWLClass> subclasses = new ArrayList<OWLClass>();
        getSubClassHierarchy(manager, factory.getOWLThing(), new ArrayList<OWLClass>(),subclasses);

        for(OWLClass cls : subclasses){
            Element typeDescription = new Element("typeDescription");
            System.out.println(cls.getIRI().getShortForm());

            //set name of uima type = class name
            Element clsName = new Element("name");
            clsName.addContent("edu.utah.blulab.uima.types." + cls.getIRI().getShortForm());
            typeDescription.addContent(clsName);

            //set description = class definition
            String definition = getSingleAnnotationProperty(manager, ontology, cls, TM_PM+"#definition");
            Element defElement = new Element("description");
            if(!definition.isEmpty()){
                defElement.addContent(definition);
            }
            typeDescription.addContent(defElement);



            //set supertypeName = parent class or uima.cas.TOP if owl:Thing
            Element supertypeElement = new Element("supertypeName");
            Set<OWLClassExpression> superclassSet = cls.getSuperClasses(manager.getOntologies());
            for(OWLClassExpression exp : superclassSet){
                if(exp.isOWLThing()){
                    //System.out.println("uima.cas.TOP");
                    supertypeElement.addContent("uima.cas.TOP");
                }else if(exp.getClassExpressionType().equals(ClassExpressionType.OWL_CLASS)){
                    //System.out.println("edu.utah.blulab.uima.types." + exp.asOWLClass().getIRI().getShortForm());
                    supertypeElement.addContent("edu.utah.blulab.uima.types." +
                            exp.asOWLClass().getIRI().getShortForm());
                }
            }
            typeDescription.addContent(supertypeElement);


            //create features group
            Element featuresElement = new Element("features");
            typeDescription.addContent(featuresElement);

            //TODO: for every axiom on class create a featureDescription
            Set<OWLClassExpression> superClassExpressions = cls.getSuperClasses(manager.getOntologies());
            superClassExpressions.addAll(cls.getEquivalentClasses(manager.getOntologies()));
            for(OWLClassExpression exp : superClassExpressions){

                if(exp.getClassExpressionType().equals(ClassExpressionType.DATA_SOME_VALUES_FROM)){
                    Element featureDescElement = new Element("featureDescription");
                    featuresElement.addContent(featureDescElement);
                    //System.out.println("DATA: " + exp.toString());
                    OWLDataSomeValuesFrom data = (OWLDataSomeValuesFrom) exp;
                    String nameStr = data.getProperty().asOWLDataProperty().getIRI().getShortForm();
                    //System.out.println(nameStr);
                    //add name to featureDescription
                    Element featureName = new Element("name");
                    featureName.addContent(nameStr);
                    featureDescElement.addContent(featureName);

                    //add description to featureDescription
                    Element featureDescription = new Element("description");
                    featureDescElement.addContent(featureDescription);

                    //add rangeTypeName to featureDescription
                    Element rangeType = new Element("rangeTypeName");
                    rangeType.addContent("uima.cas.String");
                    featureDescElement.addContent(rangeType);


                    //add multipleReferencesAllowed to feature Description
                    Element multipleRefs = new Element("multipleReferencesAllowed");
                    multipleRefs.addContent("true");
                    featureDescElement.addContent(multipleRefs);

                }else if(exp.getClassExpressionType().equals(ClassExpressionType.OBJECT_SOME_VALUES_FROM)){
                    Element featureDescElement = new Element("featureDescription");
                    featuresElement.addContent(featureDescElement);
                    //System.out.println("OBJECT: " + exp.toString());
                    OWLObjectSomeValuesFrom object = (OWLObjectSomeValuesFrom) exp;
                    String nameStr = object.getProperty().asOWLObjectProperty().getIRI().getShortForm();
                    //System.out.println(nameStr);
                    //add name to featureDescription
                    Element featureName = new Element("name");
                    featureName.addContent(nameStr);
                    featureDescElement.addContent(featureName);

                    //add description to featureDescription
                    Element featureDescription = new Element("description");
                    featureDescElement.addContent(featureDescription);

                    //add rangeTypeName to featureDescription
                    Element rangeType = new Element("rangeTypeName");
                    rangeType.addContent("uima.cas.FSArray");
                    featureDescElement.addContent(rangeType);

                    //add elementType to featureDescription
                    Element elementType = new Element("elementType");
                    if(object.getFiller().getClassExpressionType().equals(ClassExpressionType.OWL_CLASS)){
                        elementType.addContent("edu.utah.blulab.uima.types." +
                                object.getFiller().asOWLClass().getIRI().getShortForm());
                    }else if(object.getFiller().getClassExpressionType().equals(ClassExpressionType.OBJECT_UNION_OF)){
                        //System.out.println("Find common ancestor and put it's value as content.");
                        OWLObjectUnionOf unionExp = (OWLObjectUnionOf) object.getFiller();
                        OWLClass ancestor = getCommonAncestor(unionExp.getOperandsAsList(), ontology);

                    }
                    featureDescElement.addContent(elementType);

                    //add multipleReferencesAllowed to feature Description
                    Element multipleRefs = new Element("multipleReferencesAllowed");
                    multipleRefs.addContent("true");
                    featureDescElement.addContent(multipleRefs);

                }

            }


            //add type description to set of types
            types.addContent(typeDescription);

        }


        XMLOutputter xmlOutput = new XMLOutputter();

        xmlOutput.setFormat(Format.getPrettyFormat());
        xmlOutput.output(doc, new FileWriter(outputFile));

        System.out.println("File Saved!");
    }

    private static void getSubClassHierarchy(OWLOntologyManager manager, OWLClass cls, ArrayList<OWLClass> visitedCls, ArrayList<OWLClass> clsList){
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
            }else{
                if(!subCls.asOWLClass().isAnonymous()){
                    clsList.add(subCls.asOWLClass());
                }

            }


            getSubClassHierarchy(manager, subCls.asOWLClass(), visitedCls, clsList);
        }

    }

    private static String getSingleAnnotationProperty(OWLOntologyManager manager, OWLOntology ontology, OWLClass cls,
                                               String annotationProp){
        ArrayList<String> annotations = getAnnotationProperties(manager, ontology, cls, annotationProp);

        if(!annotations.isEmpty()){
            return annotations.get(0);
        }else{
            return "";
        }

    }

    private static ArrayList<String> getAnnotationProperties(OWLOntologyManager manager, OWLOntology ontology, OWLClass cls,
                                                      String annotationProperty){
        ArrayList<String> annotations = new ArrayList<String>();
        Set<OWLAnnotation> annotationSet = cls.getAnnotations(ontology,
                manager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(annotationProperty)));
        if(!annotationSet.isEmpty()){
            for(OWLAnnotation label : annotationSet){
                OWLLiteral literal = (OWLLiteral) label.getValue();
                annotations.add(literal.getLiteral());
            }

        }

        return annotations;
    }

    private static OWLClass getCommonAncestor(List<OWLClassExpression> list, OWLOntology ontology){
        boolean sharedParent = false;
        ArrayList<OWLClass> ancestors = new ArrayList<OWLClass>();
        getSuperClassHierarchy(list.get(0).asOWLClass(), ancestors, new ArrayList<OWLClass>(),
                ontology.getOWLOntologyManager());
        for(OWLClass cls : ancestors){
            System.out.println("ANCESTOR: " + cls.toString());
            if(!sharedParent){
                for(OWLClassExpression exp : list){
                    //TODO: Figure out how to check for common ancestor
                }
            }
        }

        return null;
    }

    private static void getSuperClassHierarchy(OWLClass cls, ArrayList<OWLClass> visitedCls, ArrayList<OWLClass> clsList, OWLOntologyManager manager){
        //make sure class exists and hasn't already been visited
        //visitedCls.add(factory.getOWLClass(IRI.create(OntologyConstants.ANNOTATION)));

        if(!cls.isAnonymous()){
            if(cls == null || visitedCls.contains(cls)){
                return;
            }

            Set<OWLClassExpression> superExp = cls.getSuperClasses(manager.getOntologies());
            //System.out.println("Class " + cls.asOWLClass().getIRI());
            for(OWLClassExpression superCls : superExp){
                //System.out.println("Expression: " + superCls.asOWLClass().toString());
                if(!superCls.isAnonymous()){
                    if(!visitedCls.contains(cls.asOWLClass())){
                        visitedCls.add(cls.asOWLClass());
                    }

                    getSuperClassHierarchy(superCls.asOWLClass(), visitedCls, clsList, manager);
                }


            }
        }


    }

}