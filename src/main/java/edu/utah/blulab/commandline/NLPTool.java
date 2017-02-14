package edu.utah.blulab.commandline;

import java.io.File;
import java.util.Vector;

import edu.utah.blulab.domainontology.DomainOntology;
import edu.utah.blulab.evaluationworkbenchmanager.EvaluationWorkbenchManager;
import tsl.utilities.FUtils;

public class NLPTool {
	private IevizCmd ieviz = null;
	private String toolName = null;
	private DomainOntology ontology = null;
	private String inputDirectoryName = null;
	private String corpus = null;
	private String annotator = null;

	public static String NLPDirectoryName = "nlp";

	public NLPTool(String tool, IevizCmd ieviz, DomainOntology ontology, String inputdir, String corpus,
			String annotator) {
		this.toolName = tool;
		this.ieviz = ieviz;
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
					if (!isReportFile(file)) {
						continue;
					}
					String text = FUtils.readFile(file);
					if (text != null && text.length() > 10) {
						MySQL.getMySQL().addDocumentText(this, file.getName(), text);
						String results = this.processFile(file);
						if (results != null) {
							MySQL.getMySQL().addDocumentAnalysis(this, file.getName(), results);
						}
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

	public IevizCmd getIeviz() {
		return ieviz;
	}

	public boolean isReportFile(File file) {
		String fname = file.getName();
		if (fname.charAt(0) == '.' || !fname.endsWith(".txt") || fname.contains("xml") || fname.contains("knowtator")) {
			return false;
		}
		return true;
	}

}
