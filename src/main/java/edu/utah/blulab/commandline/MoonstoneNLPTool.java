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
   // MoonstoneRuleInterface moonstoneRuleInterface = null;
    
    public MoonstoneNLPTool(DomainOntology ontology, String inputdir, String corpus, String annotator) {
       super("moonstone", ontology, inputdir, corpus, annotator);
    }
    
//    private MoonstoneRuleInterface moonstoneRuleInterface = null;
    
    public void doProcess() {
//        MoonstoneEHostXML mexml = new MoonstoneEHostXML(this.moonstoneRuleInterface);
//        mexml.readmissionGenerateEHostAnnotations(this.moonstoneRuleInterface, this.inputDirectory, this.outputDirectory, true, false);
    }

    public String getResults() {
        return null;
    }
}
