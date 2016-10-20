package edu.utah.blulab.marshallers.graph;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.UniqueFactory;
import org.neo4j.io.fs.FileUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLOntologyWalker;
import org.semanticweb.owlapi.util.OWLOntologyWalkerVisitor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.Map;


/**
 * Created by melissa on 8/11/16.
 */
public class owlToGraph {

    public static void main(String[] args) throws Exception {

        File ontFile = new File(args[0]);
        File f = new File("/Users/melissa/db/domain");
        if(f.exists()){
            try {
                FileUtils.deleteRecursively(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //TODO: create directory for graph database using the directory of domain file
        GraphDatabaseFactory dbFactory = new GraphDatabaseFactory();
        final GraphDatabaseService graphDB = dbFactory.newEmbeddedDatabase(f);



        final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        final OWLDataFactory factory = manager.getOWLDataFactory();
        final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(ontFile);


        Transaction tx = graphDB.beginTx();
        try{
            //create Thing node in db
            final Node thingNode = getOrCreateNodeWithUniqueFactory("owl:Thing", NodeTypes.THING, graphDB);

            //get all classes in domain and imported ontologies
            Set<OWLClass> classes = ontology.getClassesInSignature(true);

            //create node and attributes of each node
            for(OWLClass cls : classes){
                System.out.println(cls.getIRI());
                Node newNode = getOrCreateNodeWithUniqueFactory(cls.getIRI().getShortForm(), NodeTypes.CLASS, graphDB);

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




}
