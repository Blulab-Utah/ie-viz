/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.utah.blulab.commandline;

import java.io.File;
import java.util.Properties;

import edu.utah.blulab.domainontology.DomainOntology;
import moonstone.io.ehost.MoonstoneEHostXML;
import moonstone.rulebuilder.MoonstoneRuleInterface;
import tsl.utilities.FUtils;

/**
 *
 * @author leechristensen
 */
public class MoonstoneNLPTool extends NLPTool {
	MoonstoneRuleInterface moonstoneRuleInterface = null;

	public MoonstoneNLPTool(DomainOntology ontology, String inputdir, String corpus, String annotator) {
		super("moonstone", ontology, inputdir, corpus, annotator);
		String pfilename = NLPTool.NLPDirectoryName + File.separatorChar + IevizCmd.TSLPropertiesFile;
		Properties properties = FUtils.readPropertiesFile(IevizCmd.class, pfilename);
		File pfile = FUtils.getResourceFile(IevizCmd.class, pfilename);
		String rootdir = pfile.getParent();
		properties.put("RootDirectory", rootdir);
		this.moonstoneRuleInterface = new MoonstoneRuleInterface(properties);
	}

    public String processFile(File file) {
		MoonstoneEHostXML mexml = new MoonstoneEHostXML(this.moonstoneRuleInterface);
//		mexml.readmissionGenerateEHostAnnotations(this.moonstoneRuleInterface, this.inputDirectory,
//				this.outputDirectory, true, false);
		return "???";
    }

}
