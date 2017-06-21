package edu.utah.blulab.commandline;

import java.io.File;
import java.util.Vector;

import edu.utah.blulab.domainontology.DomainOntology;
import edu.utah.blulab.evaluationworkbenchmanager.EvaluationWorkbenchManager;
import tsl.utilities.FUtils;

public class NLPTool {
	private IevizCmd ieviz = null;
	private String toolName = null;
	private String ontologyFilePath = null;
	private String ontologyName = null;
	private DomainOntology ontology = null; // For later
	private String inputDirectoryPath = null;
	private String corpus = null;
	private String annotator = null;
	private String url = null;

	public static String NLPDirectoryName = "nlp";
	public static String DefaultDockerOntologyFilePath = "/nlp/resources/DockerOntology.owl";

	public NLPTool(String tool, IevizCmd ieviz, String oname, String opath, String inputdir, String corpus, String url,
			String annotator) {
		this.toolName = tool;
		this.ieviz = ieviz;
		this.ontologyFilePath = opath;
		this.inputDirectoryPath = inputdir;
		this.corpus = corpus;
		this.url = url;
		this.annotator = annotator;
	}

	public void storeOntologyToMySQL() throws CommandLineException {
		MySQL ms = MySQL.getMySQL();
		String ostr = FUtils.readFile(this.ontologyFilePath);
		if (ostr != null) {
			ms.addOntologyText(this.ontologyName, this.ontologyFilePath);
		}
	}

	public void storeOntologyToDefaultDockerFile() throws CommandLineException {
		String ostr = FUtils.readFile(this.ontologyFilePath);
		if (ostr != null && !DefaultDockerOntologyFilePath.equals(this.ontologyFilePath)) {
			FUtils.writeFile(DefaultDockerOntologyFilePath, ostr);
		}
	}

	public void processFiles() throws CommandLineException {
		try {
			System.out.println("NLPTool:  About to extract grammar rules from ontology; read files from: "
					+ this.inputDirectoryPath);
			Vector<File> files = FUtils.readFilesFromDirectory(this.inputDirectoryPath);
			if (files != null) {
				String ofilepath = this.ontologyFilePath;
				for (File file : files) {
					if (!isReportFile(file)) {
						continue;
					}
					String text = FUtils.readFile(file);
					if (text != null && text.length() > 10) {
						MySQL.getMySQL().addDocumentText(this, file.getName(), text);
						String results = this.processFile(file, ofilepath);
						ofilepath = null;
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
		if (fname != null && Character.isLetter(fname.charAt(0))) {
			fname = File.separatorChar + NLPTool.NLPDirectoryName + File.separatorChar + fname;
		}
		return fname;
	}

	public String processFile(File file, String ontologyFilePath) throws CommandLineException {
		return null;
	}

	public String getToolName() {
		return toolName;
	}

	public DomainOntology getOntology() {
		return ontology;
	}

	public String getInputDirectoryPath() {
		return inputDirectoryPath;
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

	public String getUrl() {
		return url;
	}

	public boolean isReportFile(File file) {
		String fname = file.getName();
		if (fname.charAt(0) == '.' || !fname.endsWith(".txt") || fname.contains("xml") || fname.contains("knowtator")) {
			return false;
		}
		return true;
	}

}
