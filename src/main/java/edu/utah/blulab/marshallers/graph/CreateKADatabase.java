package edu.utah.blulab.marshallers.graph;

import java.io.File;
import java.util.*;

/**
 * Created by Bill on 7/27/2017.
 */
public class CreateKADatabase {
    static Map<OwlToGraph.NodeTypes, Integer> totalLabels = new LinkedHashMap<>();

    static void appendTotals(OwlToGraph otg) {

        for (OwlToGraph.NodeTypes type : otg.labelCounter.keySet()) {

            Integer total = totalLabels.get(type);
            if (total == null) {
                total = 0;
            }
            total += otg.labelCounter.get(type);
            totalLabels.put(type, total);
        }
    }

    static OwlToGraph makeFiles(String owlPath, OwlToGraph.NodeTypes ontologyType) throws Exception {
        File ontFile2 = new File(owlPath);
        String name2 = ontFile2.getName();
        name2 = name2.substring(0, name2.lastIndexOf("."));
        File graphFile = new File(ontFile2.getParent() + "/db/" + name2);
        OwlToGraph mainOnt2 = new OwlToGraph();
        mainOnt2.createNewDB(graphFile);
        mainOnt2.createDBfromOnt(ontFile2, ontologyType);
        mainOnt2.labelDomainOntology();
        appendTotals(mainOnt2);
        return mainOnt2;
    }

    public static void main(String[] args) throws Exception {

        String path = "G:\\SeaCore\\Blulab";
        String ontFileStr1 = path + "\\resource\\smoking.owl";
        String ontFileStr2 = path + "\\resource\\heartDiseaseInDiabetics.owl";
        String schemaFileStr = path + "\\resource\\Schema.owl";
        String modifierFileStr = path + "\\resource\\Modifier.owl";
        String rootDBName = path + "\\resource\\db\\KnowledgeAuthor";

        List<OwlToGraph> ontGraphs = new ArrayList<>();

        // create domain ontology DB
        ontGraphs.add(makeFiles(ontFileStr1, OwlToGraph.NodeTypes.ONTOLOGY));

        // create domain ontology 2 DB
        ontGraphs.add(makeFiles(ontFileStr2, OwlToGraph.NodeTypes.ONTOLOGY));

        // create Schema ontology DB
        ontGraphs.add(makeFiles(schemaFileStr, OwlToGraph.NodeTypes.SCHEMA_ONTOLOGY));

        // create Modifier ontology DB
        ontGraphs.add(makeFiles(modifierFileStr, OwlToGraph.NodeTypes.MODIFIER_ONTOLOGY));

        // create root db
        File graphFileRoot = new File(rootDBName);
        OwlToGraph mainRoot = new OwlToGraph();
        mainRoot.createNewDB(graphFileRoot);

        // add ontology databases to root DB
        for (OwlToGraph otg : ontGraphs) {
            mainRoot.makeCopy(otg.getGraphDB());
        }

        // delete ontology databases (?)
        System.out.println("\n\n*****TOTAL LABELS");
        OwlToGraph.printLabels(totalLabels);
    }
}
