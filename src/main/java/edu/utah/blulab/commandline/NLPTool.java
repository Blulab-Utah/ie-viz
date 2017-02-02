package edu.utah.blulab.commandline;

import java.io.File;
import java.util.Vector;

import edu.utah.blulab.domainontology.DomainOntology;

public class NLPTool {
	private String toolName = null;
	private DomainOntology ontology = null;
	private String inputDirectoryName = null;
	
	public NLPTool(String tname, DomainOntology ont, String dname) {
		this.toolName = tname;
		this.inputDirectoryName = dname;
		this.ontology = ont;
	}
	
	public void processFiles() throws CommandLineException {
		Vector<File> files = Utilities.readFilesFromDirectory(this.inputDirectoryName);
		if (files != null) {
			for (File file : files) {
				String text = Utilities.readFile(file);
				MySQL.getMySQL().addDocumentText(this.toolName, file.getName(), "?", text);
				String results = this.processFile(file);
				if (results != null) {
					MySQL.getMySQL().addDocumentAnalysis(this.toolName, file.getName(), "?", results);
				}
			}
		}
	}
	
	public String processFile(File file) {
		return null;
	}

}
