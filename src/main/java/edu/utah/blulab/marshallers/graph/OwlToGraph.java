package edu.utah.blulab.marshallers.graph;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.UniqueFactory;
import org.neo4j.io.fs.FileUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.io.IOException;
import java.util.*;


/**
 * Created by Bill on 6/28/2017.
 */
public class OwlToGraph {



    private GraphDatabaseService graphDB = null;
    private String ontologyIRI;
    private final String schemaURI = "http://blulab.chpc.utah.edu/ontologies/v2/Schema.owl";

    public static void main(String[] args) throws Exception {
        File ontFile = new File(args[0]);
        //File f = new File("/Users/melissa/db/domain");
        String name = ontFile.getName();
        name = name.substring(0, name.lastIndexOf("."));

        File graphFile = new File(ontFile.getParent() + "/db/" + name);

        OwlToGraph main = new OwlToGraph();
        main.createNewDB(graphFile);
        main.createDBfromOnt(ontFile);
        main.labelDomainOntology();
        main.makeCopy(main.getGraphDB());
    }

    public void createNewDB(File graphFile){
        if(graphFile.exists()){
            try {
                FileUtils.deleteRecursively(graphFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        GraphDatabaseFactory dbFactory = new GraphDatabaseFactory();
        graphDB = dbFactory.newEmbeddedDatabase(graphFile);
    }

    public void createDBfromOnt(File ontFile) throws Exception {

//        File ontFile = new File(args[0]);
//        //File f = new File("/Users/melissa/db/domain");
//        String name = ontFile.getName();
//        name = name.substring(0, name.lastIndexOf("."));
//
//        File graphFile = new File(ontFile.getParent() + "/db/" + name);

//        if(graphFile.exists()){
//            try {
//                FileUtils.deleteRecursively(graphFile);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        GraphDatabaseFactory dbFactory = new GraphDatabaseFactory();
//        graphDB = dbFactory.newEmbeddedDatabase(graphFile);

        final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        final OWLDataFactory factory = manager.getOWLDataFactory();
        final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(ontFile);

        ontologyIRI = ontology.getOntologyID().getOntologyIRI().toString();

        Transaction tx = null;
        try {
            tx = graphDB.beginTx();
        } catch (NullPointerException e) {
            System.out.println("**** You much create the database with createNewDB first! ****");
            e.printStackTrace();
        }
        try{
            //create Thing node in db
            final Node thingNode = getOrCreateNodeWithUniqueFactory("Thing", NodeTypes.THING, graphDB);
            thingNode.setProperty("uri", "http://www.w3.org/2002/07/owl#Thing");

            // ontology node in db and connect to thing node
            final Node ontologyNode = getOrCreateNodeWithUniqueFactory(ontologyIRI, NodeTypes.ONTOLOGY, graphDB);
            ontologyNode.setProperty("uri", ontologyIRI);
            thingNode.createRelationshipTo(ontologyNode, RelationshipType.withName("IS_A"));

            //get all classes in domain and imported ontologies
            Set<OWLClass> classes = ontology.getClassesInSignature(true);

            System.out.println(ontologyIRI);
            //create node and attributes of each node
            for(OWLClass cls : classes){
                System.out.println(cls.getIRI());
                Node newNode = getOrCreateNodeWithUniqueFactory(cls.getIRI().getShortForm(), graphDB);
                newNode.setProperty("uri", cls.getIRI().toString());
                //newNode.addLabel(OwlToGraph.NodeTypes.ANCHOR);
//                if (cls.getIRI().toString().equals("http://blulab.chpc.utah.edu/ontologies/v2/ConText.owl#Lexicon")){
//                    int xxx = 1;
//                }

                //get superclasses to link to or else link to owl:Thing
                Set<OWLClassExpression> superClasses = cls.getSuperClasses(manager.getOntologies());
                int classCount = 0;
                if(superClasses.isEmpty()){
                    newNode.createRelationshipTo(thingNode, RelationshipType.withName("IS_A"));
                }else{
                    for(OWLClassExpression exp : superClasses){
                        if(exp.getClassExpressionType().equals(ClassExpressionType.OWL_CLASS)){
                            //System.out.println(exp.getClassExpressionType());
                            Node parentNode = getOrCreateNodeWithUniqueFactory(exp.asOWLClass().getIRI().getShortForm(),
                                    graphDB);
                            newNode.createRelationshipTo(parentNode, RelationshipType.withName("IS_A"));
                            //newNode.addLabel(NodeTypes.CLASS);
                            classCount++;
                        } else {
                            //System.out.println(exp.getClassExpressionType());
                        }
                    }
                }
                if (!superClasses.isEmpty() & classCount == 0){ // bug fix to fix case where there are superclasses but they are not of type Class
                    newNode.createRelationshipTo(thingNode, RelationshipType.withName("IS_A"));
                    //System.out.println("HERE");
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
                            Node fillerNode = getOrCreateNodeWithUniqueFactory(fillerName, graphDB);
                            // newNode.createRelationshipTo(fillerNode, RelationshipType.withName(relation));
                            Relationship propRelation = newNode.createRelationshipTo(fillerNode,
                                    RelationshipType.withName(relation));
                            propRelation.setProperty("uri",
                                    objPropertyExpression.asOWLObjectProperty().getIRI().toString());

                        }
                    }
                }
            }

//            while ( result.hasNext() )
//            {
//                Map<String, Object> row = result.next();
//                for ( String key : result.columns() )
//                {
//                    System.out.printf( "%s = %s%n", key, row.get( key ) );
//                    String nodeStr = row.get( key ).toString();
//                    int xx = 1;
//                }
//            }
            //makeCopy(graphDB);

            tx.success();
        }finally {
            tx.close();
        }

    }

    public void labelDomainOntology(){
        Transaction tx = graphDB.beginTx();
        try {
            addAnchorLabels(graphDB);
            addLinguisticModifierLabels(graphDB);
            addVariableLabels(graphDB);
            addNumericModifierLabels(graphDB);
            addLexicalItemLabels(graphDB);
            addClosureLabels(graphDB);
            addPseudoModifierLabels(graphDB);
            addPseudoAnchorLabels(graphDB);
            addQualifierLabels(graphDB);
            addSemanticModifierLabels(graphDB);
            addSemanticCategoryLabels(graphDB);
            labelUnlabeled(graphDB);
            tx.success();
        }finally {
            tx.close();
        }
    }

    public void makeCopy(GraphDatabaseService copyDB) {

        Transaction tx_copy = copyDB.beginTx();
        Transaction tx = graphDB.beginTx();
        try {
            /*MATCH (n:Node {name: 'abc'})
            WITH n as map
            create (copy:Node)
            set copy=map return copy*/
            Result result = copyDB.execute(
                    "MATCH (n) RETURN n");

            List<Node> nodeListOrig = new ArrayList<Node>();
            Map<Long, Long> nodeIDMap = new HashMap<Long, Long>();
            Map<Node, Node> nodeMap = new HashMap<Node, Node>();
            ResourceIterator<Node> iter = result.columnAs("n");
            while (iter.hasNext()){
                Node foundNode = iter.next();
                nodeListOrig.add(foundNode);
                Node newNode = graphDB.createNode();
                nodeIDMap.put(foundNode.getId(), newNode.getId()); // key: original node ID, value: new node ID
                nodeMap.put(foundNode, newNode);
                // add labels
                Iterator<Label> labelIter = foundNode.getLabels().iterator();
                while (labelIter.hasNext()){
                    Label lab = labelIter.next();
                    //System.out.println(lab.toString());
                    newNode.addLabel(lab);
                }
                //newNode.addLabel(NodeTypes.COPY);
                // add properties to the new node
                Map<String, Object> props = foundNode.getAllProperties();
                for (String key : props.keySet()){
                    newNode.setProperty(key, props.get(key));
                    //System.out.println(key+" "+ props.get(key));
                }
                System.out.println("Copied node " + foundNode.getId());
            }

            // add the relationships
            for (Node origNode : nodeMap.keySet()) {
                Node newNode = nodeMap.get(origNode);
                Iterator<Relationship> relIter = origNode.getRelationships().iterator();

                //            Iterator<Relationship> relIter2 = newNode.getRelationships().iterator();
                //            while (relIter2.hasNext()) {
                //                Relationship relation2 = relIter2.next();
                //                System.out.println("New Node: " + relation2.toString());
                //            }

                while (relIter.hasNext()) {
                    Relationship relation = relIter.next();
                    //System.out.println("Orig Node: " + relation.toString());
                    // figure out if the original node is the 1st or 2nd node in the relationsihp
                    Node otherNode;
                    if (relation.getStartNodeId() == origNode.getId()) { // if the original node is the 1st node
                        otherNode = nodeMap.get(relation.getEndNode());
                    } else { // if the original node is the 2nd node
                        continue; // skip it. It will be added by the other node
                        //otherNode = nodeMap.get(relation.getStartNode());
                    }
                    Relationship propRelation = newNode.createRelationshipTo(otherNode,
                            relation.getType());

                    if (newNode.getId() == otherNode.getId()) {
                        int xxx = 1;
                    }

                    if (isRedundant(propRelation, newNode)) {
                        propRelation.delete();
                    } else {
                        //System.out.println("Not redundant");
                    }

                    // add relationship properties
                    Map<String, Object> relProperties = relation.getAllProperties();
                    for (String key : relProperties.keySet()) {
                        propRelation.setProperty(key, relProperties.get(key));
                    }
                    //                String uri;
                    //                try {
                    //                    uri = uriObj.toString();
                    //                    propRelation.setProperty("uri", relation.getAllProperties().get("uri"));
                    //                } catch(Exception ex) {
                    //
                    //                }

                }
            }

            tx_copy.success();
            tx.success();
        }finally {
            tx_copy.close();
            tx.close();
        }
//        for (Node origNode : nodeListOrig){
//            Iterator<Relationship> relIter = origNode.getRelationships().iterator();
//            while (relIter.hasNext()){
//                Relationship relation = relIter.next();
//                System.out.println(relation.toString());
//                //
////                Relationship propRelation = foundNode.createRelationshipTo(fillerNode,
////                        relation.getType());
////                propRelation.setProperty("uri",
////                        objPropertyExpression.asOWLObjectProperty().getIRI().toString());
//            }
        //       }
    }

    public void labelSchemaOnt(){
        Transaction tx = graphDB.beginTx();
        try {
            Result result = graphDB.execute(
                    "MATCH (n) RETURN n");

            ResourceIterator<Node> iter = result.columnAs("n");
            while (iter.hasNext()) {
                Node foundNode = iter.next();
                foundNode.addLabel(NodeTypes.SCHEMA_ONTOLOGY);
            }
            tx.success();
        }finally {
            tx.close();
        }
    }

    public void labelModifierOnt(){
        Transaction tx = graphDB.beginTx();
        try {
            Result result = graphDB.execute(
                    "MATCH (n) RETURN n");

            ResourceIterator<Node> iter = result.columnAs("n");
            while (iter.hasNext()) {
                Node foundNode = iter.next();
                foundNode.addLabel(NodeTypes.MODIFIER_ONTOLOGY);
            }
            tx.success();
        }finally {
            tx.close();
        }
    }

    private boolean isRedundant(Relationship rel, Node nd){
        Iterator<Relationship> relIter = nd.getRelationships().iterator();
        boolean answer = false;
        int matchCount = 0;
        while (relIter.hasNext()) {
            Relationship nodeRel = relIter.next();
            String relToString = rel.getStartNodeId() + rel.getType().toString() + rel.getEndNodeId();
            String nodeRelToString = nodeRel.getStartNodeId() + nodeRel.getType().toString() + nodeRel.getEndNodeId();
            if (relToString.equals(nodeRelToString)) {
                matchCount++;
                //System.out.println("Redundant: " + relToString + "   " + nodeRelToString);
            }
            if (matchCount > 1){
                answer = true;
                break;
            }
            //System.out.println(relToString);
        }

        return answer;
    }

    public enum NodeTypes implements Label{
        THING, INDIVIDUAL, COPY, ONTOLOGY, NOS, SCHEMA_ONTOLOGY, MODIFIER_ONTOLOGY,
        VARIABLE, NUMERIC_MODIFIER, ANCHOR, LEXICAL_ITEM, CLOSURE, PSEUDO_MODIFIER,
        QUALIFIER, LINGUISTIC_MODIFIER, SEMANTIC_MODIFIER,
        MODIFIER, PSEUDO_ANCHOR, COMPOUND_ANCHOR, SEMANTIC_CATEGORY;
    }

    private Node getOrCreateNodeWithUniqueFactory(String nodeName, final Label label,
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

    private static Node getOrCreateNodeWithUniqueFactory(String nodeName, GraphDatabaseService graphDb) { // without adding a label
        UniqueFactory<Node> factory = new UniqueFactory.UniqueNodeFactory(
                graphDb, "index") {
            @Override
            protected void initialize(Node created,
                                      Map<String, Object> properties) {
                created.setProperty("name", properties.get("name"));
            }
        };

        return factory.getOrCreate("name", nodeName);
    }

    private void addLinguisticModifierLabels(GraphDatabaseService graphDB){
        // todo: mode more robust to LinguisticModifiers named "LinguisticModifier"
        Result result = graphDB.execute(
                "MATCH (nd {name:'LinguisticModifier'})<-[:IS_A*..15]-(child)" +
                        "WHERE child.uri =~ '.*" + ontologyIRI + ".*' " +
                        "RETURN child");
        ResourceIterator<Node> iter = result.columnAs("child");
        while (iter.hasNext()){
            Node foundNode = iter.next();
            foundNode.addLabel(NodeTypes.LINGUISTIC_MODIFIER);
        }
    }

    private void addAnchorLabels(GraphDatabaseService graphDB){
        // todo: mode more robust to Anchors named "Anchor"
        Result result = graphDB.execute(
                "MATCH (nd {name:'Anchor'})<-[:IS_A*..15]-(child)" +
                        "WHERE child.uri =~ '.*" + ontologyIRI + ".*' " +
                        "RETURN child");
        ResourceIterator<Node> iter = result.columnAs("child");
        while (iter.hasNext()){
            Node foundNode = iter.next();
            Iterable<Relationship> relIter1 =  foundNode.getRelationships();
            Iterator<Relationship> relIter = relIter1.iterator();
            while (relIter.hasNext()){
                Relationship rel = relIter.next();
                //System.out.println(rel.toString());
            }
            foundNode.addLabel(NodeTypes.ANCHOR);
        }
    }

    private void addVariableLabels(GraphDatabaseService graphDB){
        // todo: mode more robust to Variables named "Annotation"
        Result result = graphDB.execute(
                "MATCH (nd {name:'Annotation'})<-[:IS_A*..15]-(child) " +
                        //"WHERE NOT child.uri =~ '.*http://blulab.chpc.utah.edu/ontologies/v2/Schema.owl.*' " +
                        "WHERE child.uri =~ '.*" + ontologyIRI + ".*' " +
                        "RETURN child");
        ResourceIterator<Node> iter = result.columnAs("child");
        while (iter.hasNext()){
            Node foundNode = iter.next();
            foundNode.addLabel(NodeTypes.VARIABLE);
        }
    }

    private void addNumericModifierLabels(GraphDatabaseService graphDB){
        // todo: make more robust to NumericModifiers named "NumericModifier"
        Result result = graphDB.execute(
                "MATCH (nd {name:'NumericModifier'})<-[:IS_A*..25]-(child)" +
                        "WHERE child.uri =~ '.*" + ontologyIRI + ".*' " +
                        "RETURN child");
        ResourceIterator<Node> iter = result.columnAs("child");
        while (iter.hasNext()){
            Node foundNode = iter.next();
            foundNode.addLabel(NodeTypes.NUMERIC_MODIFIER);
        }
    }

    private void addLexicalItemLabels(GraphDatabaseService graphDB){
        // todo: make more robust to LexicalItems named "LexicalItem"
        // todo: more work needs to be done to get this working
        Result result = graphDB.execute(
                "MATCH (nd {name:'LexicalItem'})<-[:IS_A*..15]-(child)" +
                        "WHERE child.uri =~ '.*" + ontologyIRI + ".*' " +
                        "RETURN child");
        ResourceIterator<Node> iter = result.columnAs("child");
        while (iter.hasNext()){
            Node foundNode = iter.next();
            foundNode.addLabel(NodeTypes.LEXICAL_ITEM);
        }
    }

    private void addClosureLabels(GraphDatabaseService graphDB){
        // todo: make more robust to Closures named "Closure"
        Result result = graphDB.execute(
                "MATCH (nd {name:'Closure'})<-[:IS_A*..15]-(child)" +
                        "WHERE child.uri =~ '.*" + ontologyIRI + ".*' " +
                        "RETURN child");
        ResourceIterator<Node> iter = result.columnAs("child");
        while (iter.hasNext()){
            Node foundNode = iter.next();
            foundNode.addLabel(NodeTypes.CLOSURE);
        }
    }

    private void addPseudoModifierLabels(GraphDatabaseService graphDB){
        // todo: make more robust to PseudoModifiers named "PseudoModifier"
        Result result = graphDB.execute(
                "MATCH (nd {name:'PseudoModifier'})<-[:IS_A*..15]-(child)" +
                        "WHERE child.uri =~ '.*" + ontologyIRI + ".*' " +
                        "RETURN child");
        ResourceIterator<Node> iter = result.columnAs("child");
        while (iter.hasNext()){
            Node foundNode = iter.next();
            foundNode.addLabel(NodeTypes.PSEUDO_MODIFIER);
        }
    }

    private void addPseudoAnchorLabels(GraphDatabaseService graphDB){
        // todo: make more robust to PseudoModifiers named "PseudoModifier"
        Result result = graphDB.execute(
                "MATCH (nd {name:'PseudoAnchor'})<-[:IS_A*..15]-(child)" +
                        "WHERE child.uri =~ '.*" + ontologyIRI + ".*' " +
                        "RETURN child");
        ResourceIterator<Node> iter = result.columnAs("child");
        while (iter.hasNext()){
            Node foundNode = iter.next();
            foundNode.addLabel(NodeTypes.PSEUDO_ANCHOR);
        }
    }

    private void addQualifierLabels(GraphDatabaseService graphDB){
        // todo: make more robust to Qualifiers named "Qualifier"
        Result result = graphDB.execute(
                "MATCH (nd {name:'Qualifier'})<-[:IS_A*..15]-(child)" +
                        "WHERE child.uri =~ '.*" + ontologyIRI + ".*' " +
                        "RETURN child");
        ResourceIterator<Node> iter = result.columnAs("child");
        while (iter.hasNext()){
            Node foundNode = iter.next();
            foundNode.addLabel(NodeTypes.QUALIFIER);
        }
    }

    private void addSemanticModifierLabels(GraphDatabaseService graphDB){
        // todo: make more robust to SemanticModifiers named "SemanticModifier"
        Result result = graphDB.execute(
                "MATCH (nd {name:'SemanticModifier'})<-[:IS_A*..15]-(child)" +
                        "WHERE child.uri =~ '.*" + ontologyIRI + ".*' " +
                        "RETURN child");
        ResourceIterator<Node> iter = result.columnAs("child");
        while (iter.hasNext()){
            Node foundNode = iter.next();
            foundNode.addLabel(NodeTypes.SEMANTIC_MODIFIER);
        }
    }

    private void addSemanticCategoryLabels(GraphDatabaseService graphDB){
        // todo: make more robust to SemanticCategory named "SemanticCategory"
        Result result = graphDB.execute(
                "MATCH (nd {name:'Event'})<-[:IS_A*..15]-(child)" +
                        "WHERE child.uri =~ '.*" + schemaURI + ".*' " +
                        "MATCH (child) " +
                        "WHERE NOT (()-[:IS_A]->(child)) " + // match node that does not have any children
                        "RETURN child");



        ResourceIterator<Node> iter = result.columnAs("child");
        while (iter.hasNext()){
            Node foundNode = iter.next();
            foundNode.addLabel(NodeTypes.SEMANTIC_CATEGORY);
        }

        // match notes that have children that are not from the Schema ontology
        result = graphDB.execute(
                "MATCH (nd {name:'Event'})<-[:IS_A*..15]-(child) " +
                        "WHERE child.uri =~ '.*" + schemaURI + ".*' " +
                        "MATCH ((newchild)-[:IS_A]->(child)) " +
                        "WHERE NOT newchild.uri =~ '.*" + schemaURI + ".*' " +
                        "RETURN child");

        ResourceIterator<Node> iter2 = result.columnAs("child");
        while (iter2.hasNext()){
            Node foundNode = iter2.next();
            foundNode.addLabel(NodeTypes.SEMANTIC_CATEGORY);
        }

    }

    private void labelUnlabeled(GraphDatabaseService graphDB) {
        Result result = graphDB.execute(
                "MATCH (n) " +
                        "WHERE size(labels(n)) = 0\n" +
                        "RETURN n");
        ResourceIterator<Node> iter = result.columnAs("n");
        while (iter.hasNext()){
            Node foundNode = iter.next();
            foundNode.addLabel(NodeTypes.NOS);
        }

    }

    public GraphDatabaseService getGraphDB() {
        return graphDB;
    }
}
