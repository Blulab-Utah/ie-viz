package edu.utah.blulab.commandline;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.ed.wew.api.AnnotatorImpl;
import com.ed.wew.api.AnnotatorReference;
import com.ed.wew.api.AnnotatorType;
import com.ed.wew.api.DocumentImpl;
import com.ed.wew.api.DocumentReference;
import com.ed.wew.api.Params;
import com.ed.wew.api.ResultTable;
import com.ed.wew.api.WEWManager;

import edu.utah.blulab.evaluationworkbenchmanager.WEWManagerInterface;
import workbench.api.gui.WBGUI;
import workbench.arr.EvaluationWorkbench;

public class EvaluationWorkbenchTool {
	private NLPTool nlpTool = null;
	private WBGUI workbench = null;

	public EvaluationWorkbenchTool(NLPTool tool) {
		this.nlpTool = tool;
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

	public void invokeWEWManagerMySQL() throws Exception {
		String corpus = this.nlpTool.getCorpus();
		EvaluationWorkbench wb = WEWManagerInterface.initializeOldWorkbench(false);
		String format = wb.getStartupParameters().getInputTypeFirstAnnotator();
		String schemaFileName = wb.getStartupParameters().getKnowtatorSchemaFile();
		String primaryAnnotatorName = wb.getStartupParameters().getFirstAnnotatorName();
		String secondaryAnnotatorName = wb.getStartupParameters().getSecondAnnotatorName();
		// Schema
		DocumentImpl schema = new DocumentImpl();
		File sfile = Utilities.getResourceFile(IevizCmd.class, schemaFileName);
		InputStreamReader sisr = new InputStreamReader(new FileInputStream(sfile));
		schema.setName(schemaFileName);
		schema.setReader(sisr);

		// Documents
		List<DocumentReference> documents = new ArrayList();
		List<String> dnames = MySQL.getMySQL().getDocumentNames(corpus);
		for (String dname : dnames) {
			String text = MySQL.getMySQL().getDocumentText(dname, corpus);
			InputStream dsis = new ByteArrayInputStream(text.getBytes("UTF_8"));
			DocumentImpl d = new DocumentImpl();
			d.setName(dname);
			d.setReader(new InputStreamReader(dsis));
			documents.add(d);
		}

		// Primary
		int i = 0;
		List<AnnotatorReference> primary = new ArrayList();
		List<String> analyses = MySQL.getMySQL().getDocumentAnalyses(corpus, primaryAnnotatorName);
		for (String analysis : analyses) {
			String aname = "Primary" + i;
			InputStream asis = new ByteArrayInputStream(analysis.getBytes("UTF_8"));
			AnnotatorImpl a1 = new AnnotatorImpl();
			a1.setAnnotatorType(AnnotatorType.Primary);
			a1.setName(aname);
			a1.setReader(new InputStreamReader(asis));
			primary.add(a1);
		}

		// Secondary
		i = 0;
		List<AnnotatorReference> secondary = new ArrayList();
		analyses = MySQL.getMySQL().getDocumentAnalyses(corpus, secondaryAnnotatorName);
		for (String analysis : analyses) {
			String aname = "Secondary" + i;
			InputStream asis = new ByteArrayInputStream(analysis.getBytes("UTF_8"));
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

		ResultTable result = WEWManager.load(schema, documents, primary, secondary, params);
	}

}
