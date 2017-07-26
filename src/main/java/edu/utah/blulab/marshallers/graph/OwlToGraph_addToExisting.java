package edu.utah.blulab.marshallers.graph;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.UniqueFactory;
import org.neo4j.io.fs.FileUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Created by Bill on 6/28/2017.
 *
 * Adds an ontology file to an existing graph
 */
public class OwlToGraph_addToExisting {

    public static void main(String[] args) throws Exception {

        File graphFileOrig = new File(args[0]);
        File ontFile = new File(args[1]);


        //File ontFile = new File(args[0]);
        //File f = new File("/Users/melissa/db/domain");
        String name = ontFile.getName();
        name = name.substring(0, name.lastIndexOf("."));

        File graphFile = new File(ontFile.getParent() + "/db/" + name);

        if(graphFile.exists()){
            try {
                FileUtils.deleteRecursively(graphFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        GraphDatabaseFactory dbFactory = new GraphDatabaseFactory();
        final GraphDatabaseService graphDB = dbFactory.newEmbeddedDatabase(graphFile);

        final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        final OWLDataFactory factory = manager.getOWLDataFactory();
        final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(ontFile);


        Transaction tx = graphDB.beginTx();
        try{
            //create Thing node in db
            final Node thingNode = getOrCreateNodeWithUniqueFactory("owl:Thing", NodeTypes.THING, graphDB);
            thingNode.setProperty("uri", "http://www.w3.org/2002/07/owl#Thing");

            //get all classes in domain and imported ontologies
            Set<OWLClass> classes = ontology.getClassesInSignature(true);

            //create node and attributes of each node
            for(OWLClass cls : classes){
                System.out.println(cls.getIRI());
                Node newNode = getOrCreateNodeWithUniqueFactory(cls.getIRI().getShortForm(), NodeTypes.CLASS, graphDB);
                newNode.setProperty("uri", cls.getIRI().toString());

                //get superclasses to link to or else link to owl:Thing
                Set<OWLClassExpression> superClasses = cls.getSuperClasses(manager.getOntologies());
                if(superClasses.isEmpty()){
                    newNode.createRelationshipTo(thingNode, RelationshipType.withName("IS_A"));
                }else{
                    for(OWLClassExpression exp : superClasses){
                        if(exp.getClassExpressionType().equals(ClassExpressionType.OWL_CLASS)){
                            Node parentNode = getOrCreateNodeWithUniqueFactory(exp.asOWLClass().getIRI().getShortForm(),
                                    NodeTypes.CLASS, graphDB);
                            newNode.createRelationshipTo(parentNode, RelationshipType.withName("IS_A"));
                        }
                    }
                }

                //get all annotation properties and create properties for each
                Set<OWLAnnotationAssertionAxiom> annProps = cls.getAnnotationAssertionAxioms(ontology);
                for(OWLAnnotationAssertionAxiom axiom : annProps){
                    //System.out.println(axiom.toString());
                    String relType = axiom.getProperty().getIRI().getShortForm();
                    OWLLiteral value = (OWLLiteral) axiom.getValue();
                    String valueStr = value.getLiteral();
                    newNode.setProperty(relType, valueStr);
                }

                //get all axioms on each class and create relationships for each
                Set<OWLClassExpression> classExpressions = cls.getEquivalentClasses(manager.getOntologies());
                classExpressions.addAll(cls.getSuperClasses(manager.getOntologies()));
                for(OWLClassExpression exp : classExpressions){
                    if(exp.getClassExpressionType().equals(ClassExpressionType.OBJECT_SOME_VALUES_FROM)){
                        OWLObjectSomeValuesFrom axiom = (OWLObjectSomeValuesFrom) exp;
                        OWLObjectPropertyExpression objPropertyExpression = axiom.getProperty();
                        String relation = objPropertyExpression.asOWLObjectProperty().getIRI().getShortForm();
                        OWLClassExpression fillerClass = axiom.getFiller();
                        if(fillerClass.getClassExpressionType().equals(ClassExpressionType.OWL_CLASS)){
                            String fillerName = fillerClass.asOWLClass().getIRI().getShortForm();
                            Node fillerNode = getOrCreateNodeWithUniqueFactory(fillerName, NodeTypes.CLASS, graphDB);
                            // newNode.createRelationshipTo(fillerNode, RelationshipType.withName(relation));
                            Relationship propRelation = newNode.createRelationshipTo(fillerNode,
                                    RelationshipType.withName(relation));
                            propRelation.setProperty("uri",
                                    objPropertyExpression.asOWLObjectProperty().getIRI().toString());

                        }
                    }
                }
            }

            tx.success();
        }finally {
            tx.close();
        }

    }

    public enum NodeTypes implements Label{
        THING, CLASS, INDIVIDUAL;

    }

    private static Node getOrCreateNodeWithUniqueFactory(String nodeName, final Label label,
                                                         GraphDatabaseService graphDb) {
        UniqueFactory<Node> factory = new UniqueFactory.UniqueNodeFactory(
                graphDb, "index") {
            @Override
            protected void initialize(Node created,
                                      Map<String, Object> properties) {
                created.addLabel(label);
                created.setProperty("name", properties.get("name"));
            }
        };

        return factory.getOrCreate("name", nodeName);
    }

//    private static Node createNode(String nodeName, final Label label,
//                                                         GraphDatabaseService graphDb) {
//
//        Node created = graphDb.createNode();
//        created.addLabel(label);
//        created.setProperty("name", nodeName);
//
//        return created;
//    }

    private static void getAllNodes(GraphDatabaseService graphDB, GraphDatabaseService graphDBOrig){
        Result result = graphDB.execute("MATCH (n) RETURN n");
        ResourceIterator<Node> iter = result.columnAs("n");
        while (iter.hasNext()){
            Node foundNode = iter.next();
            //graphDBOrig.createNode(foundNode);
        }
    }
}

