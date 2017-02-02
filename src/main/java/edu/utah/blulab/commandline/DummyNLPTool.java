package edu.utah.blulab.commandline;

import edu.utah.blulab.domainontology.DomainOntology;
import java.io.File;

public class DummyNLPTool extends NLPTool {

    public DummyNLPTool(DomainOntology ontology, String inputdir) {
    	super("dummy", ontology, inputdir);
    }

    public void setOntology(DomainOntology ontology) {
    }

    public void setInputDirectory(String inputDirectory) {
    }

    public void setOutputDirectory(String outputDirectory) {
    }
    
    public void doProcess() {
        String results = getResults();
        System.out.println("Dummy NLP Tool Results: " + results);
    }

    public String getResults() {
        String results = null;
        File file = Utilities.getResourceFile(IevizCmd.class, "FakeReport1.txt.knowtator.xml");
        if (file != null && file.exists()) {
            results = Utilities.readFile(file);
        }
        return results;
    }

}
