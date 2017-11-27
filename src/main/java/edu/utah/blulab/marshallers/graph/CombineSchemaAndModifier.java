package edu.utah.blulab.marshallers.graph;

import java.io.File;

/**
 * Created by Bill on 10/17/2017.
 */
public class CombineSchemaAndModifier {

    public static void main(String[] args) throws Exception {

        String schemaFileStr = "C:\\software_dev\\Code\\intellij_workspace\\ie-viz\\resource\\Schema.owl";
        String modifierFileStr = "C:\\software_dev\\Code\\intellij_workspace\\ie-viz\\resource\\Modifier.owl";
        String rootDBName = "C:\\software_dev\\Code\\intellij_workspace\\ie-viz\\resource\\db\\KnowledgeAuthor";

        // create Schema ontology DB
        File schemaFile = new File(schemaFileStr);
        String name = schemaFile.getName();
        name = name.substring(0, name.lastIndexOf("."));
        File graphFile = new File(schemaFile.getParent() + "/db/" + name);
        OwlToGraph mainSchema = new OwlToGraph();
        mainSchema.createNewDB(graphFile);
        mainSchema.createDBfromOnt(schemaFile, OwlToGraph.NodeTypes.SCHEMA_ONTOLOGY);
        mainSchema.labelSchemaOnt();

        // create Modifier ontology DB
        File modifierFile = new File(modifierFileStr);
        name = modifierFile.getName();
        name = name.substring(0, name.lastIndexOf("."));
        graphFile = new File(modifierFile.getParent() + "/db/" + name);
        OwlToGraph mainModifier = new OwlToGraph();
        mainModifier.createNewDB(graphFile);
        mainModifier.createDBfromOnt(modifierFile, OwlToGraph.NodeTypes.MODIFIER_ONTOLOGY);
        mainModifier.labelModifierOnt();

        // create root db
        File graphFileRoot = new File(rootDBName);
        OwlToGraph mainRoot = new OwlToGraph();
        mainRoot.createNewDB(graphFileRoot);

        // add ontology databases to root DB
        mainRoot.makeCopy(mainSchema.getGraphDB());
        mainRoot.makeCopy(mainModifier.getGraphDB());

        // delete ontology databases (?)

    }
}
