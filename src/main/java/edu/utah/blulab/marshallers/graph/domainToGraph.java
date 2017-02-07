package edu.utah.blulab.marshallers.graph;

import edu.utah.blulab.domainontology.*;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.UniqueFactory;
import org.neo4j.io.fs.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by melissa on 11/1/16.
 */
public class domainToGraph {

    public static void main(String[] args) throws Exception {

        File domainFile = new File(args[0]);

        String name = domainFile.getName();
        name = name.substring(0, name.lastIndexOf("."));

        File graphFile = new File(domainFile.getParent() + "/db/" + name);

        if(graphFile.exists()){
            try {
                FileUtils.deleteRecursively(graphFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        GraphDatabaseFactory dbFactory = new GraphDatabaseFactory();
        final GraphDatabaseService graphDB = dbFactory.newEmbeddedDatabase(graphFile);
        final DomainOntology domain = new DomainOntology(domainFile.getPath(), false);


        Transaction tx = graphDB.beginTx();

        try{
            //create Closure nodes
            for(Modifier closure : domain.createClosureDictionary()){
                Node  closureNode = createClosureNode(closure, graphDB);

                //create lexical item nodes
                for(LexicalItem item : closure.getItems()){
                    Node lexicalItemNode = createItemTypeNode(item, NodeTypes.LEXICAL_ITEM, graphDB);
                    lexicalItemNode.createRelationshipTo(closureNode, RelationshipType.withName("IS_MEMBER_OF"));
                }
            }
            //create PseudoModifier nodes
            for(Modifier pseudo : domain.createPseudoModifierDictionary()){
                Node pseudoNode = createPseudoModifierNode(pseudo, graphDB);

                //create lexical item nodes
                for(LexicalItem item : pseudo.getItems()){
                    Node lexicalItemNode = createItemTypeNode(item, NodeTypes.LEXICAL_ITEM, graphDB);
                    lexicalItemNode.createRelationshipTo(pseudoNode, RelationshipType.withName("IS_MEMBER_OF"));
                }
            }

            //create Modifier nodes
            HashMap<String, ArrayList<Modifier>> modifierMap = domain.createModifierTypeMap();
            for(Modifier modifier : modifierMap.get("Linguistic")){
                //System.out.println(modifier);
                createModifierNode(modifier, NodeTypes.LINGUISTC_MODIFIER, graphDB);
            }

            for(Modifier modifier : modifierMap.get("Semantic")){
                //System.out.println(modifier);
                createModifierNode(modifier, NodeTypes.SEMANTIC_MODIFIER, graphDB);
            }

            for(Modifier modifier : modifierMap.get("Numeric")){
                //System.out.println(modifier);
                createModifierNode(modifier, NodeTypes.NUMERIC_MODIFIER, graphDB);
            }

            //create anchor nodes
            for(Anchor anchor : domain.createAnchorDictionary()){
                Node anchorNode = createAnchorNode(anchor, graphDB);

                //link to parent classes
                for(Anchor parent : anchor.getDirectParents()){
                    Node parentNode = createAnchorNode(parent, graphDB);
                    anchorNode.createRelationshipTo(parentNode, RelationshipType.withName("IS_A"));
                }

                //link to pseudos
                for(Anchor pseudo : anchor.getPseudos()){
                    Node pseudoNode = createAnchorNode(pseudo, graphDB);
                    anchorNode.createRelationshipTo(pseudoNode, RelationshipType.withName("hasPseudo"));
                }
            }

            //create variable nodes
            for(Variable variable : domain.getAllVariables()){
                String varName = variable.getVarName();
                if(varName.isEmpty()){
                    varName = variable.getURI();
                    varName = varName.substring(varName.lastIndexOf("#")+1, varName.length());
                }
                Node variableNode = getOrCreateNodeWithUniqueFactory(varName, NodeTypes.VARIABLE, graphDB);
                variableNode.setProperty("uri", variable.getURI());

                for(LogicExpression<Anchor> exp : variable.getAnchor()){
                   for(Anchor anchor : exp){
                       Node anchorNode = getOrCreateNodeWithUniqueFactory(anchor.getPrefTerm(),
                               NodeTypes.ANCHOR, graphDB);
                       Relationship relationship = variableNode.createRelationshipTo(anchorNode,
                               RelationshipType.withName("hasAnchor"));
                       relationship.setProperty("uri", OntologyConstants.HAS_ANCHOR);
                   }


                }

                for(Map.Entry<String, LogicExpression<Modifier>> entry : variable.getModifiers().entrySet()){
                   String relationshipName = entry.getKey();
                    relationshipName = relationshipName.substring(relationshipName.lastIndexOf("#")+1,
                            relationshipName.length());
                   for(Modifier modifier : entry.getValue()){
                       NodeTypes type = null;
                       if(modifier.getModifierType().equals(Modifier.LINGUISTIC)){
                           type = NodeTypes.LINGUISTC_MODIFIER;
                       }else if(modifier.getModifierType().equals(Modifier.SEMANTIC)){
                           type = NodeTypes.SEMANTIC_MODIFIER;
                       }else if(modifier.getModifierType().equals(Modifier.NUMERIC)){
                           type = NodeTypes.NUMERIC_MODIFIER;
                       }else{
                           type = NodeTypes.QUALIFIER;
                       }
                       Node modifierNode = getOrCreateNodeWithUniqueFactory(modifier.getModName(),
                               type, graphDB);
                       Relationship relationship = variableNode.createRelationshipTo(modifierNode,
                               RelationshipType.withName(relationshipName));
                       relationship.setProperty("uri", entry.getKey());
                   }

                }
            }



            tx.success();
        }finally {
            tx.close();
        }

    }


    private static Node createModifierNode(Modifier modifier, NodeTypes type, GraphDatabaseService graphDB){
        Node modifierNode = getOrCreateNodeWithUniqueFactory(modifier.getModName(),
                type, graphDB);

        //set uri prop
        modifierNode.setProperty("uri", modifier.getUri());

        //set name
        modifierNode.setProperty("name", modifier.getModName());

        //create lexical item nodes
        for(LexicalItem item : modifier.getItems()){
            Node lexicalItemNode = createItemTypeNode(item, NodeTypes.LEXICAL_ITEM, graphDB);
            lexicalItemNode.createRelationshipTo(modifierNode, RelationshipType.withName("IS_MEMBER_OF"));
        }

        for(Modifier parent : modifier.getDirectParents()){
            Node parentNode = createModifierNode(parent, type, graphDB);
            modifierNode.createRelationshipTo(parentNode, RelationshipType.withName("IS_A"));
        }

        for(Modifier pseudo : modifier.getPseudos()){
            Node pseudoNode = createPseudoModifierNode(pseudo, graphDB);
            modifierNode.createRelationshipTo(pseudoNode, RelationshipType.withName("hasPseudo"));
        }
        for(Modifier closure: modifier.getClosures()){
            Node closureNode = createClosureNode(closure, graphDB);
            modifierNode.createRelationshipTo(closureNode, RelationshipType.withName("hasClosure"));
        }

        if(type.equals(NodeTypes.NUMERIC_MODIFIER)){
            //TODO: Add additional numeric attributes here
        }

        return modifierNode;
    }

    private static Node createPseudoModifierNode(Modifier modifier, GraphDatabaseService graphDB){
        Node modNode = createModifierNode(modifier, NodeTypes.PSEUDO_MODIFIER, graphDB);

        return modNode;
    }

    private static Node createClosureNode(Modifier modifier, GraphDatabaseService graphDB){
        Node modNode = createModifierNode(modifier, NodeTypes.CLOSURE, graphDB);

        return modNode;
    }

    private static Node createQualifierNode(Modifier modifier, GraphDatabaseService graphDB){
        Node modNode = createModifierNode(modifier, NodeTypes.QUALIFIER, graphDB);

        return modNode;
    }

    private static Node createAnchorNode(Anchor term, GraphDatabaseService graphDB){
        Node termNode = getOrCreateNodeWithUniqueFactory(term.getPrefTerm(), NodeTypes.ANCHOR, graphDB);

        //set uri prop
        termNode.setProperty("uri", term.getURI());
        termNode.setProperty("preferredTerm", term.getPrefTerm());
        termNode.setProperty("synonym", String.join(";", term.getSynonym()));
        termNode.setProperty("misspelling", String.join(";", term.getMisspelling()));
        termNode.setProperty("regex", String.join(";", term.getRegex()));
        termNode.setProperty("subjectiveExpression", String.join(";", term.getSubjExp()));
        termNode.setProperty("alternateCode", String.join(";", term.getAltCode()));
        termNode.setProperty("code", term.getPrefCode());

        //link to parent nodes

        return termNode;
    }

    private static Node createItemTypeNode(LexicalItem item, NodeTypes type, GraphDatabaseService graphDB){
        Node itemNode = getOrCreateNodeWithUniqueFactory(item.getPrefTerm(), type, graphDB);

        //set uri prop
        itemNode.setProperty("uri", item.getUri());
        itemNode.setProperty("preferredTerm", item.getPrefTerm());
        itemNode.setProperty("synonym", String.join(";", item.getSynonym()));
        itemNode.setProperty("misspelling", String.join(";", item.getMisspelling()));
        itemNode.setProperty("regex", String.join(";", item.getRegex()));
        itemNode.setProperty("subjectiveExpression", String.join(";", item.getSubjExp()));
        itemNode.setProperty("alternateCode", String.join(";", item.getAltCode()));
        itemNode.setProperty("code", String.join(";", item.getPrefCode()));
        itemNode.setProperty("windowSize", item.getWindowSize());
        if(item.getActionEn(true) != null) itemNode.setProperty("ActionEn", item.getActionEn(true));

        return itemNode;
    }

    public enum NodeTypes implements Label{
        //THING, CLASS, INDIVIDUAL;
        VARIABLE, MODIFIER, NUMERIC_MODIFIER, ANCHOR, LEXICAL_ITEM, CLOSURE, PSEUDO_MODIFIER, PSEUDO_ANCHOR,
        COMPOUND_ANCHOR, QUALIFIER, LINGUISTC_MODIFIER, SEMANTIC_MODIFIER;

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

