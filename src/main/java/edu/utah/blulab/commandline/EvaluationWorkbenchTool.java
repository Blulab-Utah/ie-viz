package edu.utah.blulab.commandline;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.ed.wew.api.AnnotatorImpl;
import com.ed.wew.api.AnnotatorReference;
import com.ed.wew.api.AnnotatorType;
import com.ed.wew.api.DocumentImpl;
import com.ed.wew.api.DocumentReference;
import com.ed.wew.api.Params;
import com.ed.wew.api.ResultTable;
import com.ed.wew.api.WEWManager;

import edu.utah.blulab.evaluationworkbenchmanager.WEWManagerInterface;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.knowledge.engine.StartupParameters;
import tsl.utilities.FUtils;
import workbench.api.gui.WBGUI;
import workbench.arr.EvaluationWorkbench;

public class EvaluationWorkbenchTool {
	private WBGUI evaluationWorkbench = null;
	private String corpusName = null;

	public EvaluationWorkbenchTool(String corpus) {
		this.corpusName = corpus;
		int x = 1;
		startupWorkbench();
	}

	public boolean startupWorkbench() {
		try {
			invokeWEWManagerMySQL();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public void invokeWEWManagerMySQL() throws CommandLineException {
		int x = 1;
		String pfilename = NLPTool.NLPDirectoryName + File.separatorChar + IevizCmd.TSLPropertiesFile;
		Properties properties = FUtils.readPropertiesFile(IevizCmd.class, pfilename);
		
		File pfile = FUtils.getResourceFile(IevizCmd.class, pfilename);
		String rootdir = pfile.getParent();
		properties.put("RootDirectory", rootdir);
		
		KnowledgeEngine ke = KnowledgeEngine.getCurrentKnowledgeEngine(false, properties);
		StartupParameters sp = ke.getStartupParameters();
		String inputTypeFirstAnnotator = sp.getPropertyValue(workbench.arr.StartupParameters.WorkbenchAnnotationFileTypeFirstAnnotator);
		String inputTypeSecondAnnotator = sp.getPropertyValue(workbench.arr.StartupParameters.WorkbenchAnnotationFileTypeSecondAnnotator);
		String format = inputTypeFirstAnnotator;
		String schemaFileName = sp.getPropertyValue(workbench.arr.StartupParameters.WorkbenchKnowtatorSchemaFile);
		String primaryAnnotatorName = sp.getPropertyValue(workbench.arr.StartupParameters.FirstAnnotatorName);
		String secondaryAnnotatorName = sp.getPropertyValue(workbench.arr.StartupParameters.SecondAnnotatorName);
		DocumentImpl schema = new DocumentImpl();
		File sfile = FUtils.getResourceFile(IevizCmd.class, schemaFileName);
		InputStreamReader sisr;
		try {
			sisr = new InputStreamReader(new FileInputStream(sfile));
		} catch (FileNotFoundException e) {
			throw new CommandLineException("Workbench: " + e.toString());
		}
		schema.setName(schemaFileName);
		schema.setReader(sisr);

		// Documents
		List<DocumentReference> documents = new ArrayList();
		List<String> dnames = MySQL.getMySQL().getDocumentNames(this.corpusName);
		for (String dname : dnames) {
			String text = MySQL.getMySQL().getDocumentText(dname, this.corpusName);
			InputStream dsis;
			try {
				dsis = new ByteArrayInputStream(text.getBytes("UTF_8"));
			} catch (UnsupportedEncodingException e) {
				throw new CommandLineException("Workbench: " + e.toString());
			}
			DocumentImpl d = new DocumentImpl();
			d.setName(dname);
			d.setReader(new InputStreamReader(dsis));
			documents.add(d);
		}

		// Primary
		int i = 0;
		List<AnnotatorReference> primary = new ArrayList();
		List<String> analyses = MySQL.getMySQL().getDocumentAnalyses(this.corpusName, primaryAnnotatorName);
		for (String analysis : analyses) {
			String aname = "Primary" + i;
			InputStream asis;
			try {
				asis = new ByteArrayInputStream(analysis.getBytes("UTF_8"));
			} catch (UnsupportedEncodingException e) {
				throw new CommandLineException("Workbench: " + e.toString());
			}
			AnnotatorImpl a1 = new AnnotatorImpl();
			a1.setAnnotatorType(AnnotatorType.Primary);
			a1.setName(aname);
			a1.setReader(new InputStreamReader(asis));
			primary.add(a1);
		}

		// Secondary
		i = 0;
		List<AnnotatorReference> secondary = new ArrayList();
		analyses = MySQL.getMySQL().getDocumentAnalyses(this.corpusName, secondaryAnnotatorName);
		for (String analysis : analyses) {
			String aname = "Secondary" + i;
			InputStream asis;
			try {
				asis = new ByteArrayInputStream(analysis.getBytes("UTF_8"));
			} catch (UnsupportedEncodingException e) {
				throw new CommandLineException("Workbench: " + e.toString());
			}
			AnnotatorImpl a2 = new AnnotatorImpl();
			a2.setAnnotatorType(AnnotatorType.Secondary);
			a2.setName(aname);
			a2.setReader(new InputStreamReader(asis));
			secondary.add(a2);
		}

		// Params
		Params params = new Params();
		params.putParam("format", format);
		params.putParam("firstAnnotator", primaryAnnotatorName);
		params.putParam("secondAnnotator", secondaryAnnotatorName);

		try {
			sisr.close();
			ResultTable result = WEWManager.load(schema, documents, primary, secondary, params);
		} catch (Exception e) {
			throw new CommandLineException("Workbench: " + e.toString());
		}
	}

}
