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
 */
public class OwlToGraph_Labeled {

    public static void main(String[] args) throws Exception {

        File ontFile = new File(args[0]);
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
            final Node thingNode = getOrCreateNodeWithUniqueFactory("owl:Thing", OwlToGraph_Labeled.NodeTypes.THING, graphDB);
            thingNode.setProperty("uri", "http://www.w3.org/2002/07/owl#Thing");

            //get all classes in domain and imported ontologies
            Set<OWLClass> classes = ontology.getClassesInSignature(true);

            //create node and attributes of each node
            for(OWLClass cls : classes){
                System.out.println(cls.getIRI());
                Node newNode = getOrCreateNodeWithUniqueFactory(cls.getIRI().getShortForm(), OwlToGraph_Labeled.NodeTypes.CLASS, graphDB);
                newNode.setProperty("uri", cls.getIRI().toString());
                //newNode.addLabel(OwlToGraph_Labeled.NodeTypes.ANCHOR);

                //get superclasses to link to or else link to owl:Thing
                Set<OWLClassExpression> superClasses = cls.getSuperClasses(manager.getOntologies());
                if(superClasses.isEmpty()){
                    newNode.createRelationshipTo(thingNode, RelationshipType.withName("IS_A"));
                }else{
                    for(OWLClassExpression exp : superClasses){
                        if(exp.getClassExpressionType().equals(ClassExpressionType.OWL_CLASS)){
                            //System.out.println(exp.getClassExpressionType());
                            Node parentNode = getOrCreateNodeWithUniqueFactory(exp.asOWLClass().getIRI().getShortForm(),
                                    OwlToGraph_Labeled.NodeTypes.CLASS, graphDB);
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
                            Node fillerNode = getOrCreateNodeWithUniqueFactory(fillerName, OwlToGraph_Labeled.NodeTypes.CLASS, graphDB);
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

            addAnchorLabels(graphDB);
            addLinguisticModifierLabels(graphDB);
            addVariableLabels(graphDB);
            addNumericModifierLabels(graphDB);
            addLexicalItemLabels(graphDB);
            addClosureLabels(graphDB);
            addPseudoModifierLabels(graphDB);
            addQualifierLabels(graphDB);
            addSemanticModifierLabels(graphDB);

            tx.success();
        }finally {
            tx.close();
        }

    }

    public enum NodeTypes implements Label{
        THING, CLASS, INDIVIDUAL,
        VARIABLE, NUMERIC_MODIFIER, ANCHOR, LEXICAL_ITEM, CLOSURE, PSEUDO_MODIFIER,
        QUALIFIER, LINGUISTIC_MODIFIER, SEMANTIC_MODIFIER;
        // MODIFIER, PSEUDO_ANCHOR, COMPOUND_ANCHOR
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

    private static void addLinguisticModifierLabels(GraphDatabaseService graphDB){
        // todo: mode more robust to LinguisticModifiers named "LinguisticModifier"
        Result result = graphDB.execute(
                "MATCH (nd {name:'LinguisticModifier'})<-[:IS_A*..15]-(child)" +
                        "RETURN child");
        ResourceIterator<Node> iter = result.columnAs("child");
        while (iter.hasNext()){
            Node foundNode = iter.next();
            foundNode.addLabel(NodeTypes.LINGUISTIC_MODIFIER);
        }
    }

    private static void addAnchorLabels(GraphDatabaseService graphDB){
        // todo: mode more robust to Anchors named "Anchor"
        Result result = graphDB.execute(
                "MATCH (nd {name:'Anchor'})<-[:IS_A*..15]-(child)" +
                        "RETURN child");
        ResourceIterator<Node> iter = result.columnAs("child");
        while (iter.hasNext()){
            Node foundNode = iter.next();
            foundNode.addLabel(NodeTypes.ANCHOR);
        }
    }

    private static void addVariableLabels(GraphDatabaseService graphDB){
        // todo: mode more robust to Variables named "Annotation"
        Result result = graphDB.execute(
                "MATCH (nd {name:'Annotation'})<-[:IS_A*..15]-(child)" +
                        "RETURN child");
        ResourceIterator<Node> iter = result.columnAs("child");
        while (iter.hasNext()){
            Node foundNode = iter.next();
            foundNode.addLabel(NodeTypes.VARIABLE);
        }
    }

    private static void addNumericModifierLabels(GraphDatabaseService graphDB){
        // todo: mode more robust to NumericModifiers named "NumericModifier"
        Result result = graphDB.execute(
                "MATCH (nd {name:'NumericModifier'})<-[:IS_A*..15]-(child)" +
                        "RETURN child");
        ResourceIterator<Node> iter = result.columnAs("child");
        while (iter.hasNext()){
            Node foundNode = iter.next();
            foundNode.addLabel(NodeTypes.NUMERIC_MODIFIER);
        }
    }

    private static void addLexicalItemLabels(GraphDatabaseService graphDB){
        // todo: mode more robust to LexicalItems named "LexicalItem"
        Result result = graphDB.execute(
                "MATCH (nd {name:'LexicalItem'})<-[:IS_A*..15]-(child)" +
                        "RETURN child");
        ResourceIterator<Node> iter = result.columnAs("child");
        while (iter.hasNext()){
            Node foundNode = iter.next();
            foundNode.addLabel(NodeTypes.LEXICAL_ITEM);
        }
    }

    private static void addClosureLabels(GraphDatabaseService graphDB){
        // todo: mode more robust to Closures named "Closure"
        Result result = graphDB.execute(
                "MATCH (nd {name:'Closure'})<-[:IS_A*..15]-(child)" +
                        "RETURN child");
        ResourceIterator<Node> iter = result.columnAs("child");
        while (iter.hasNext()){
            Node foundNode = iter.next();
            foundNode.addLabel(NodeTypes.CLOSURE);
        }
    }

    private static void addPseudoModifierLabels(GraphDatabaseService graphDB){
        // todo: mode more robust to PseudoModifiers named "PseudoModifier"
        Result result = graphDB.execute(
                "MATCH (nd {name:'PseudoModifier'})<-[:IS_A*..15]-(child)" +
                        "RETURN child");
        ResourceIterator<Node> iter = result.columnAs("child");
        while (iter.hasNext()){
            Node foundNode = iter.next();
            foundNode.addLabel(NodeTypes.PSEUDO_MODIFIER);
        }
    }

    private static void addQualifierLabels(GraphDatabaseService graphDB){
        // todo: mode more robust to Qualifiers named "Qualifier"
        Result result = graphDB.execute(
                "MATCH (nd {name:'Qualifier'})<-[:IS_A*..15]-(child)" +
                        "RETURN child");
        ResourceIterator<Node> iter = result.columnAs("child");
        while (iter.hasNext()){
            Node foundNode = iter.next();
            foundNode.addLabel(NodeTypes.QUALIFIER);
        }
    }

    private static void addSemanticModifierLabels(GraphDatabaseService graphDB){
        // todo: mode more robust to SemanticModifiers named "SemanticModifier"
        Result result = graphDB.execute(
                "MATCH (nd {name:'SemanticModifier'})<-[:IS_A*..15]-(child)" +
                        "RETURN child");
        ResourceIterator<Node> iter = result.columnAs("child");
        while (iter.hasNext()){
            Node foundNode = iter.next();
            foundNode.addLabel(NodeTypes.SEMANTIC_MODIFIER);
        }
    }

}
