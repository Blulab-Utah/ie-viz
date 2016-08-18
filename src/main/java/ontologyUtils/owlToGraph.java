package ontologyUtils;

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
            ArrayList<OWLClass> subclasses = new ArrayList<OWLClass>();
            getSubClassHierarchy(manager, factory.getOWLThing(), new ArrayList<OWLClass>(),subclasses);

            for(OWLClass cls : subclasses){
                System.out.println(cls.toString());
            }

            tx.success();
        }finally {
            tx.close();
        }

    }

    public enum NodeTypes implements Label{
        THING, CLASS, INDIVIDUAL;

    }

    public enum RelationshipTypes implements RelationshipType {
        IS_A, HAS_MEMBER;


        public void createRelationshipType(String type){
            RelationshipTypes.valueOf(type);
        }
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
}
