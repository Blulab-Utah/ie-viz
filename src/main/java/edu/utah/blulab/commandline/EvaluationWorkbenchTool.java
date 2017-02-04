package edu.utah.blulab.commandline;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.ed.wew.api.AnnotatorImpl;
import com.ed.wew.api.AnnotatorReference;
import com.ed.wew.api.AnnotatorType;
import com.ed.wew.api.DocumentImpl;
import com.ed.wew.api.DocumentReference;
import com.ed.wew.api.Params;
import com.ed.wew.api.ResultTable;
import com.ed.wew.api.WEWManager;

import edu.utah.blulab.evaluationworkbenchmanager.EvaluationWorkbenchManager;
import edu.utah.blulab.evaluationworkbenchmanager.WEWManagerInterface;
import tsl.utilities.FUtils;
import workbench.api.gui.WBGUI;
import workbench.arr.EvaluationWorkbench;

public class EvaluationWorkbenchTool {
	private WBGUI workbench = null;
	
	public EvaluationWorkbenchTool() {
		startupWorkbench();
	}
	
	public boolean startupWorkbench() {
		try {
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public static void invokeWEWManagerMySQL(String corpus) throws Exception {

		EvaluationWorkbench wb = WEWManagerInterface.initializeOldWorkbench(false);

		String format = wb.getStartupParameters().getInputTypeFirstAnnotator();
		
		// Schema
		DocumentImpl schema = new DocumentImpl();
		File sfile = Utilities.getResourceFile(IevizCmd.class, "projectschema.xml");
		InputStreamReader sisr = new InputStreamReader(new FileInputStream(sfile));
		schema.setName("projectschema.xml");
		schema.setReader(sisr);
		
		
		String schemaFileName = wb.getStartupParameters()
				.getKnowtatorSchemaFile();
		String primaryAnnotatorName = wb.getStartupParameters()
				.getFirstAnnotatorName();
		String secondaryAnnotatorName = wb.getStartupParameters()
				.getSecondAnnotatorName();

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
		
		&&& GOT THIS FAR 2/3/2017 &&&&&
	
		// Primary
		List<AnnotatorReference> primary = new ArrayList();
		files = tsl.utilities.FUtils
				.readFilesFromDirectory(primaryAnnotationDir);
		if (files != null) {
			for (File f : files) {
				String sname = f.getName();
				String lname = f.getAbsolutePath();
				AnnotatorImpl a1 = new AnnotatorImpl();
				a1.setAnnotatorType(AnnotatorType.Primary);
				a1.setName(sname);
				a1.setReader(new FileReader(lname));
				primary.add(a1);
			}
		}

		// Secondary
		List<AnnotatorReference> secondary = new ArrayList();
		files = tsl.utilities.FUtils
				.readFilesFromDirectory(secondaryAnnotationDir);
		if (files != null) {
			for (File f : files) {
				String sname = f.getName();
				String lname = f.getAbsolutePath();
				AnnotatorImpl a1 = new AnnotatorImpl();
				a1.setAnnotatorType(AnnotatorType.Secondary);
				a1.setName(sname);
				a1.setReader(new FileReader(lname));
				secondary.add(a1);
			}
		}

		// Params
		Params params = new Params();
		params.putParam("format", format);
		params.putParam("firstAnnotator", primaryAnnotatorName);
		params.putParam("secondAnnotator", secondaryAnnotatorName);

		ResultTable result = WEWManager.load(schema, documents, primary,
				secondary, params);

		String text = result.toString();

		FUtils.writeFile("/Users/leechristensen/Desktop/WEWManagerResult.txt",
				text);
		// System.out.println("\n\n" + result + "\n\n");
	}

	
}
