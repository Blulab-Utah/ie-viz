package edu.utah.blulab.commandline;

import java.io.File;
import java.util.Vector;

import edu.utah.blulab.domainontology.DomainOntology;
import tsl.utilities.FUtils;

public class NLPTool {
	private String toolName = null;
	private DomainOntology ontology = null;
	private String inputDirectoryName = null;
	private String corpus = null;
	private String annotator = null;
	
    public static String NLPDirectoryName = "nlp";

	public NLPTool(String tool, DomainOntology ontology, String inputdir, String corpus, String annotator) {
		this.toolName = tool;
		this.ontology = ontology;
		this.inputDirectoryName = inputdir;
		this.corpus = corpus;
		this.annotator = annotator;
	}

	public void processFiles() throws CommandLineException {
		try {
			Vector<File> files = FUtils.readFilesFromDirectory(this.inputDirectoryName);
			if (files != null) {
				for (File file : files) {
					String text = FUtils.readFile(file);
					MySQL.getMySQL().addDocumentText(this, file.getName(), text);
					String results = this.processFile(file);
					if (results != null) {
						MySQL.getMySQL().addDocumentAnalysis(this, file.getName(), results);
					}
				}
			}
		} catch (Exception e) {
			throw new CommandLineException(e.toString());
		}
	}
	
	public static String getNLPDirectoryFilename(String fname) {
		return NLPTool.NLPDirectoryName + File.separatorChar + fname;
	}

	public String processFile(File file) {
		return null;
	}

	public String getToolName() {
		return toolName;
	}

	public DomainOntology getOntology() {
		return ontology;
	}

	public String getInputDirectoryName() {
		return inputDirectoryName;
	}

	public String getAnnotator() {
		return annotator;
	}

	public String getCorpus() {
		return corpus;
	}

}
