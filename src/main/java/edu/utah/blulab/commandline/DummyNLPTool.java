package edu.utah.blulab.commandline;

import edu.utah.blulab.domainontology.DomainOntology;
import java.io.File;

public class DummyNLPTool extends NLPTool {

    public DummyNLPTool(DomainOntology ontology, String inputdir, String annotator) {
    	super("dummy", ontology, inputdir, annotator);
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
        file = Utilities.getResourceFile(IevizCmd.class, "FakeReport1.txt.knowtator.xml");
        if (file != null && file.exists()) {
            results = Utilities.readFile(file);
        }
        return results;
    }

}
