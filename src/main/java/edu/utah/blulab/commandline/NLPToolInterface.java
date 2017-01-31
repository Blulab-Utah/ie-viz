/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.utah.blulab.commandline;

import edu.utah.blulab.domainontology.DomainOntology;

/**
 *
 * @author leechristensen, 1/30/2017
 */
public interface NLPToolInterface {
    
    public void setOntology(DomainOntology ontology);
    public void setInputDirectory(String inputDirectory);
    public void setOutputDirectory(String outputDirectory);
    public void doProcess();
    public String getResults();
    
}
