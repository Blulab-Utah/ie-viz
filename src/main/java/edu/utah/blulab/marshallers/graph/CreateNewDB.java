package edu.utah.blulab.marshallers.graph;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import java.io.File;

/**
 * Created by Bill on 7/19/2017.
 */
public class CreateNewDB {

    public static void main(String[] args) {

        File graphFile = new File("C:\\software_dev\\Code\\intellij_workspace\\ie-viz\\resource\\db\\knowledgeauthor");
        GraphDatabaseFactory dbFactory = new GraphDatabaseFactory();
        final GraphDatabaseService graphDB = dbFactory.newEmbeddedDatabase(graphFile);
    }
}
