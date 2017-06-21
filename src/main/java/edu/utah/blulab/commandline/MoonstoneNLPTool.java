/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.utah.blulab.commandline;

import java.io.File;
import java.util.Properties;
import java.util.Vector;

import edu.utah.blulab.domainontology.DomainOntology;
import moonstone.annotation.Annotation;
import moonstone.io.ehost.MoonstoneEHostXML;
import moonstone.io.readmission.Readmission;
import moonstone.rulebuilder.MoonstoneRuleInterface;
import tsl.utilities.FUtils;
import tsl.utilities.SeqUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;
import workbench.api.input.knowtator.KTSimpleInstance;

/**
 *
 * @author leechristensen
 */
public class MoonstoneNLPTool extends NLPTool {
	private MoonstoneRuleInterface moonstoneRuleInterface = null;

	public MoonstoneNLPTool(IevizCmd ieviz, String oname, String opath, String inputdir, String corpus, String url) {
		super("moonstone", ieviz, oname, opath, inputdir, corpus, url, "moonstone");
		String pfilename = NLPTool.NLPDirectoryName + File.separatorChar + IevizCmd.TSLPropertiesFile;
		Properties properties = FUtils.readPropertiesFile(IevizCmd.class, pfilename);
//		this.moonstoneRuleInterface = new MoonstoneRuleInterface(properties);
	}
	
//	public MoonstoneNLPTool(IevizCmd ieviz, String ontology, String corpus, String outfile) {
//		
//	}

	public String processFile(File file, String ontologyFilePath) throws CommandLineException {
		String xml = null;
		try {
			String fname = file.getName();
			String text = FUtils.readFile(file);
			xml = this.getIeviz().getRest().sendPost(this.getUrl(), text, ontologyFilePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return xml;
	}

	public String processFileBeforeREST(File file) {
		String fname = file.getName();
		MoonstoneEHostXML exml = new MoonstoneEHostXML(this.moonstoneRuleInterface);
		String text = FUtils.readFile(file);
		String xml = null;
		System.out.print("Processing : " + fname + "...");
		Vector<Annotation> targets = gatherTargetAnnotations(text, fname);
		if (targets != null) {
			try {
				String htmlfilename = StrUtils.textToHtml(file.getName());
				xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
				xml += "<annotations textSource=\"" + htmlfilename + "\">\n";
				xml += exml.toXML(targets);
				xml += "</annotations>\n";
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return xml;
	}

	public Vector<Annotation> gatherTargetAnnotations(String text, String fname) {
		Readmission readmission = this.moonstoneRuleInterface.getReadmission();
		Vector<Annotation> targets = null;
		Vector<Annotation> annotations = this.moonstoneRuleInterface.applyNarrativeGrammarToText(fname, text, true,
				true, true);
		annotations = this.moonstoneRuleInterface.getControl().getDocumentGrammar().getDisplayedAnnotations();
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				Vector<Annotation> relevant = null;
				if (annotation.getGoodness() < 0.25) {
					continue;
				}
				relevant = readmission.gatherAllRelevantAnnotations(annotation, false);
				if (relevant != null) {
					targets = appendIfSeparate(targets, relevant);
				}
			}
		}
		return targets;
	}

	public Vector<Annotation> appendIfSeparate(Vector<Annotation> annotations1, Vector<Annotation> annotations2) {
		if (annotations1 == null) {
			return annotations2;
		}
		if (annotations2 == null) {
			return annotations1;
		}
		Vector<Annotation> v = new Vector(annotations1);
		for (Annotation a2 : annotations2) {
			boolean foundDuplicate = false;
			for (Annotation a1 : v) {
				if (a1.getConcept().equals(a2.getConcept())
						&& SeqUtils.overlaps(a1.getTextStart(), a1.getTextEnd(), a2.getTextStart(), a2.getTextEnd())) {
					foundDuplicate = true;
					break;
				}
			}
			if (!foundDuplicate) {
				v.add(a2);
			}
		}
		return v;
	}

}
