package edu.utah.blulab.commandline;

import edu.utah.blulab.domainontology.DomainOntology;
import tsl.utilities.FUtils;

import java.io.File;

public class DummyNLPTool extends NLPTool {

    public DummyNLPTool(DomainOntology ontology, String inputdir, String corpus, String annotator) {
    	super("dummy", ontology, inputdir, corpus, annotator);
    }

    public void setOntology(DomainOntology ontology) {
    }

    public void setInputDirectory(String inputDirectory) {
    }

    public void setOutputDirectory(String outputDirectory) {
    }

    public String processFile(File file) {
        String results = null;
        int x = 2;
        file = FUtils.getResourceFile(IevizCmd.class, "FakeReport1.txt.knowtator.xml");
        if (file != null && file.exists()) {
        	results = FUtils.readFile(file);
        }
        return results;
    }

}
