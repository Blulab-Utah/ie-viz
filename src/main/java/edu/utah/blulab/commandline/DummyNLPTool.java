package edu.utah.blulab.commandline;

import edu.utah.blulab.domainontology.DomainOntology;
import java.io.File;

public class DummyNLPTool implements NLPToolInterface {

    public DummyNLPTool(DomainOntology ontology, String inputdir, String outputdir) {

    }

    @Override
    public void setOntology(DomainOntology ontology) {
    }

    @Override
    public void setInputDirectory(String inputDirectory) {
    }

    @Override
    public void setOutputDirectory(String outputDirectory) {
    }

    @Override
    public void doProcess() {
    }

    @Override
    public String getResults() {
        String results = null;
        File file = Utilities.getResourceFile(IevizCmd.class, "FakeReport1.txt.knowtator.xml");
        if (file != null && file.exists()) {
            results = Utilities.readFile(file);
        }
        return results;
    }

}
