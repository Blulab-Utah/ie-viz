/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.utah.blulab.commandline;

import edu.utah.blulab.domainontology.DomainOntology;

/**
 *
 * @author leechristensen
 */
public class MoonstoneNLPTool extends NLPTool {
    DomainOntology ontology = null;
    String inputDirectory = null;
   // MoonstoneRuleInterface moonstoneRuleInterface = null;
    
    public MoonstoneNLPTool(DomainOntology ontology, String inputdir) {
       super("moonstone", ontology, inputdir);
    }
    
//    private MoonstoneRuleInterface moonstoneRuleInterface = null;
    
    public void doProcess() {
//        MoonstoneEHostXML mexml = new MoonstoneEHostXML(this.moonstoneRuleInterface);
//        mexml.readmissionGenerateEHostAnnotations(this.moonstoneRuleInterface, this.inputDirectory, this.outputDirectory, true, false);
    }

    public void setOntology(DomainOntology ontology) {
        this.ontology = ontology;
    }

    public void setInputDirectory(String inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public String getResults() {
        return null;
    }
}
