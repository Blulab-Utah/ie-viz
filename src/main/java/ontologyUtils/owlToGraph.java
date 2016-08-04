package ontologyUtils;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;

/**
 * Created by melissa on 8/11/16.
 */
public class owlToGraph {

    public static void main(String[] args) throws Exception {

        File ontFile = new File(args[0]);

        //TODO: create directory for graph database using the directory of domain file
        GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(new File("/Users/melissa/db/domain"));

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();


    }


}
