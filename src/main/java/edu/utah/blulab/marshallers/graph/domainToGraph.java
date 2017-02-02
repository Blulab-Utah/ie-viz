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
import java.util.Iterator;
import java.util.Map;

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
                Node pseudoNode = createPseudoModifierTypeNode(pseudo, graphDB);

                //create lexical item nodes
                for(LexicalItem item : pseudo.getItems()){
                    Node lexicalItemNode = createItemTypeNode(item, NodeTypes.LEXICAL_ITEM, graphDB);
                    lexicalItemNode.createRelationshipTo(pseudoNode, RelationshipType.withName("IS_MEMBER_OF"));
                }
            }

            //create Modifier nodes
            //TODO find numeric modifiers and create new nodes
            HashMap<String, ArrayList<Modifier>> modifierMap = domain.createModifierTypeMap();
            /**for(Modifier modifier : domain.createModifierDictionary()){
                Node modifierNode = createModifierTypeNode(modifier, NodeTypes.MODIFIER, graphDB);

                //create lexical item nodes
                for(LexicalItem item : modifier.getItems()){
                    Node lexicalItemNode = createItemTypeNode(item, NodeTypes.LEXICAL_ITEM, graphDB);
                    lexicalItemNode.createRelationshipTo(modifierNode, RelationshipType.withName("IS_MEMBER_OF"));
                }

                for(Modifier parent : modifier.getDirectParents()){
                    Node parentNode = createModifierTypeNode(parent, NodeTypes.MODIFIER, graphDB);
                    modifierNode.createRelationshipTo(parentNode, RelationshipType.withName("IS_A"));
                }

                for(Modifier pseudo : modifier.getPseudos()){
                    Node pseudoNode = createModifierTypeNode(pseudo, NodeTypes.PSEUDO_MODIFIER, graphDB);
                    modifierNode.createRelationshipTo(pseudoNode, RelationshipType.withName("hasPseudo"));
                }
                for(Modifier closure: modifier.getClosures()){
                    Node closureNode = createModifierTypeNode(closure, NodeTypes.CLOSURE, graphDB);
                    modifierNode.createRelationshipTo(closureNode, RelationshipType.withName("hasClosure"));
                }
            }**/

            //create anchor nodes
            for(Anchor anchor : domain.createAnchorDictionary()){
                Node anchorNode = createAnchorNode(anchor, graphDB);

                //link to parent classes
                for(Anchor parent : anchor.getDirectParents()){
                    Node parentNode = createAnchorNode(parent, graphDB);
                    anchorNode.createRelationshipTo(parentNode, RelationshipType.withName("IS_A"));
                }


                //TODO: fix this in ontology so that code can be consistent
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
                       Node modifierNode = getOrCreateNodeWithUniqueFactory(modifier.getModName(),
                               NodeTypes.MODIFIER, graphDB);
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

    private static Node createLinguisticModifierTypeNode(Modifier modifier, GraphDatabaseService graphDB){
        Node modNode = getOrCreateNodeWithUniqueFactory(modifier.getModName(),
                NodeTypes.LINGUISTC_MODIFIER, graphDB);

        //set uri prop
        modNode.setProperty("uri", modifier.getUri());

        return modNode;
    }

    private static Node createSemanticModifierTypeNode(Modifier modifier, GraphDatabaseService graphDB){
        Node modNode = getOrCreateNodeWithUniqueFactory(modifier.getModName(),
                NodeTypes.SEMANTIC_MODIFIER, graphDB);

        //set uri prop
        modNode.setProperty("uri", modifier.getUri());

        return modNode;
    }

    private static Node createNumericModifierTypeNode(Modifier modifier, GraphDatabaseService graphDB){
        Node modNode = getOrCreateNodeWithUniqueFactory(modifier.getModName(),
                NodeTypes.NUMERIC_MODIFIER, graphDB);

        //set uri prop
        modNode.setProperty("uri", modifier.getUri());

        return modNode;
    }

    private static Node createPseudoModifierTypeNode(Modifier modifier, GraphDatabaseService graphDB){
        Node modNode = getOrCreateNodeWithUniqueFactory(modifier.getModName(),
                NodeTypes.PSEUDO_MODIFIER, graphDB);

        //set uri prop
        modNode.setProperty("uri", modifier.getUri());

        return modNode;
    }

    private static Node createClosureNode(Modifier modifier, GraphDatabaseService graphDB){
        Node modNode = getOrCreateNodeWithUniqueFactory(modifier.getModName(), NodeTypes.CLOSURE, graphDB);

        //set uri prop
        modNode.setProperty("uri", modifier.getUri());

        return modNode;
    }

    private static Node createQualifierNode(Modifier modifier, GraphDatabaseService graphDB){
        Node modNode = getOrCreateNodeWithUniqueFactory(modifier.getModName(), NodeTypes.QUALIFIER, graphDB);

        //set uri prop
        modNode.setProperty("uri", modifier.getUri());

        return modNode;
    }

    private static Node createAnchorNode(Anchor term, GraphDatabaseService graphDB){
        Node termNode = getOrCreateNodeWithUniqueFactory(term.getPrefTerm(), NodeTypes.ANCHOR, graphDB);

        //set uri prop
        termNode.setProperty("uri", term.getURI());
        termNode.setProperty("preferredTerm", term.getPrefTerm());
        for(String synonym : term.getSynonym()){
            termNode.setProperty("synonym", synonym);
        }
        for(String misspelling : term.getMisspelling()){
            termNode.setProperty("misspelling", misspelling);
        }
        for(String regex : term.getRegex()){
            termNode.setProperty("regex", regex);
        }
        for(String subjExp : term.getSubjExp()){
            termNode.setProperty("subjectiveExpression", subjExp);
        }
        termNode.setProperty("code", term.getPrefCode());
        for(String altCode : term.getAltCode()){
            termNode.setProperty("alternateCode", altCode);
        }

        return termNode;
    }

    private static Node createItemTypeNode(LexicalItem item, NodeTypes type, GraphDatabaseService graphDB){
        Node itemNode = getOrCreateNodeWithUniqueFactory(item.getPrefTerm(), type, graphDB);

        //set uri prop
        itemNode.setProperty("uri", item.getUri());
        itemNode.setProperty("preferredTerm", item.getPrefTerm());
        for(String synonym : item.getSynonym()){
            itemNode.setProperty("synonym", synonym);
        }
        for(String misspelling : item.getMisspelling()){
            itemNode.setProperty("misspelling", misspelling);
        }
        for(String regex : item.getRegex()){
            itemNode.setProperty("regex", regex);
        }
        for(String subjExp : item.getSubjExp()){
            itemNode.setProperty("subjectiveExpression", subjExp);
        }
        for(String prefCode : item.getPrefCode()){
            itemNode.setProperty("code", prefCode);
        }
        for(String altCode : item.getAltCode()){
            itemNode.setProperty("alternateCode", altCode);
        }
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

