package edu.pitt.dbmi.nlp.noble.mentions;

import edu.pitt.dbmi.nlp.noble.ui.NobleMentionsTool;

import java.util.HashMap;
import java.util.Map;

public class IevizNobleMentionsConnectorTest {

    public static void main(String args[]) throws Exception {
        String ontology = "C:\\Users\\Deep\\Documents\\noble\\ont";
        String input = "C:\\Users\\Deep\\Documents\\noble\\input";
        String output = "C:\\Users\\Deep\\Documents\\noble\\output";

        Map<String, String> pathMap = new HashMap<String, String>();
        pathMap.put("ont", ontology);
        pathMap.put("input", input);
        pathMap.put("output", output);

        NobleMentionsTool nobleMentionsTool = new NobleMentionsTool();
        nobleMentionsTool.ievizProcess(pathMap);
    }
}