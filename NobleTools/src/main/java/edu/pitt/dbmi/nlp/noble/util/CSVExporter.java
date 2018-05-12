package edu.pitt.dbmi.nlp.noble.util;

import edu.pitt.dbmi.nlp.noble.coder.model.Document;
import edu.pitt.dbmi.nlp.noble.coder.model.Mention;
import edu.pitt.dbmi.nlp.noble.extract.model.ItemInstance;
import edu.pitt.dbmi.nlp.noble.extract.model.Template;
import edu.pitt.dbmi.nlp.noble.extract.model.TemplateDocument;
import edu.pitt.dbmi.nlp.noble.extract.model.TemplateItem;
import edu.pitt.dbmi.nlp.noble.mentions.model.AnnotationVariable;
import edu.pitt.dbmi.nlp.noble.mentions.model.Composition;
import edu.pitt.dbmi.nlp.noble.mentions.model.DomainOntology;
import edu.pitt.dbmi.nlp.noble.mentions.model.Instance;
import edu.pitt.dbmi.nlp.noble.terminology.Annotation;
import edu.pitt.dbmi.nlp.noble.terminology.Concept;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.*;

/**
 * The Class CSVExporter.
 */
public class CSVExporter {
	public static final String DEFAULT_RESULT_FILE = "RESULTS.tsv";
	private File outputFile;
	private BufferedWriter csvWriter;
	private String S = "\t";
	
	/**
	 * Instantiates a new CSV exporter.
	 *
	 * @param file the file
	 */
	public CSVExporter(File file){
		if(file.isFile())
			outputFile = file;
		else if(file.isDirectory())
			outputFile = new File(file,DEFAULT_RESULT_FILE);
	}
	
	/**
	 * Gets the output file.
	 *
	 * @return the output file
	 */
	public File getOutputFile() {
		return outputFile;
	}

	/**
	 * Sets the output file.
	 *
	 * @param outputFile the new output file
	 */
	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}



	/**
	 * Gets the delimeter.
	 *
	 * @return the delimeter
	 */
	public String getDelimeter() {
		return S;
	}

	/**
	 * Sets the delimeter.
	 *
	 * @param s the new delimeter
	 */
	public void setDelimeter(String s) {
		S = s;
	}


	/**
	 * create codes csv report.
	 *
	 * @param doc the doc
	 * @throws Exception the exception
	 */
	public void export(TemplateDocument doc )  throws Exception{
		String name = doc.getTitle();
		Map<Template,List<ItemInstance>> resultMap = doc.getItemInstances();
		BufferedWriter writer = getCSVWriter(outputFile,resultMap.keySet());
		writer.write(name);
		for(Template template: resultMap.keySet()){
			for(TemplateItem temp: template.getTemplateItems()){
				for(String question: temp.getQuestions()){
					TemplateItem attribute = temp.getAttribute(question);
					List<ItemInstance> instances = attribute == null?doc.getItemInstances(temp):doc.getItemInstances(temp, attribute);
					StringBuilder b = new StringBuilder();
					for(ItemInstance inst :instances){
						b.append((inst.getAnswer(false))+" ;"); 
					}
					writer.write(S+b.toString().trim());
				}
			}
		}
		writer.write("\n");
		writer.flush();
	}
	
	
	/**
	 * create codes csv report.
	 *
	 * @param doc the doc
	 * @throws Exception the exception
	 */
	public void export(Document doc)  throws Exception{
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(3);
		BufferedWriter writer = getCSVWriter(outputFile);
		
		// go over all of the mentions
		for(Mention m: doc.getMentions()){
			Concept c = m.getConcept();
			String s = Arrays.toString(c.getSemanticTypes());
			StringBuilder a = new StringBuilder();
			for(Annotation an : m.getAnnotations()){
				a.append(an.getText()+"/"+(+an.getOffset())+", ");
			}
			if(a.length()> 0){
				a = new StringBuilder(a.substring(0,a.length()-2));
			}
			writer.write(doc.getTitle()+S+m.getText()+S+c.getCode()+S+c.getName()+S+s.substring(1,s.length()-1)+S+a+getModifierValues(m)+"\n");
		}
		writer.flush();
	}	
	
	
	/**
	 * save tab delimted document for a Composition object
	 * @param doc - the document to be exported
	 * @throws Exception if something goes wrong
	 */
	public void export(Composition doc) throws Exception {
		BufferedWriter writer = getCSVWriterForComposition(outputFile);
		int n = 1;
		for(AnnotationVariable var: doc.getAnnotationVariables()){
			write(var,doc.getTitle(),"Accepted",n++,writer);
		}
		for(AnnotationVariable var: doc.getRejectedAnnotationVariables()){
			write(var,doc.getTitle(),"Rejected",n++,writer);
		}
		
		writer.flush();
	}
	
	
	private void write(AnnotationVariable var, String docName, String type, int n, Writer writer) throws Exception {
		for(String prop: var.getModifierInstances().keySet()){
			for(Instance inst: var.getModifierInstances().get(prop)){
				StringBuilder value = new StringBuilder(inst.getLabel());
				StringBuilder valueProp = new StringBuilder();
				if(!DomainOntology.HAS_ANCHOR.equals(prop)){
					for(Instance ii: inst.getModifierInstanceList()){
						valueProp.append(ii.getLabel()+", ");
					}
					if(valueProp.length() > 2){
						valueProp.delete(valueProp.length()-2,valueProp.length());
					}
				}
				writer.write(docName+S+type+S+n+S+var.getLabel()+S+prop+S+value+S+valueProp+S+getAnnotations(inst.getAnnotations())+"\n");
			}
		}
	}
	
	
	
	/**
	 * get a list of annotations
	 * @param annotations
	 * @return
	 */
	private String getAnnotations(Collection<Annotation> annotations){
		StringBuilder a = new StringBuilder();
		for(Annotation an : annotations){
			a.append(an.getText()+"/"+(+an.getOffset())+", ");
		}
		if(a.length()> 0){
			a = new StringBuilder(a.substring(0,a.length()-2));
		}
		return a.toString();
	}
	
	
	/**
	 * flush all writers.
	 *
	 * @throws Exception the exception
	 */
	public void flush() throws Exception {
		if(csvWriter != null){
			csvWriter.close();
		}
		csvWriter = null;
	}
	
	/**
	 * Gets the modifier types.
	 *
	 * @return the modifier types
	 */
	private String getModifierTypes(){
		StringBuilder st = new StringBuilder();
		for(String s: Mention.getLinguisticModifierTypes()){
			st.append(S+s);
		}
		return st.toString();
	}
	
	/**
	 * Gets the modifier values.
	 *
	 * @param m the m
	 * @return the modifier values
	 */
	private String getModifierValues(Mention m){
		StringBuilder st = new StringBuilder();
		for(String s: Mention.getLinguisticModifierTypes()){
			String v = m.getModifierValue(s);
			st.append(S+(v == null?"":v));
		}
		return st.toString();
	}
	
	/**
	 * Gets the CSV writer.
	 *
	 * @param out the out
	 * @return the CSV writer
	 * @throws Exception the exception
	 */
	private BufferedWriter getCSVWriter(File out) throws Exception {
		if(csvWriter == null){
			csvWriter = new BufferedWriter(new FileWriter(out));
			csvWriter.write("Document"+S+"Matched Term"+S+"Code"+S+"Concept Name"+S+"Semantic Type"+S+"Annotations"+getModifierTypes()+"\n");
		}
		return csvWriter;
	}
	
	/**
	 * Gets the CSV writer.
	 *
	 * @param out the out
	 * @return the CSV writer
	 * @throws Exception the exception
	 */
	private BufferedWriter getCSVWriterForComposition(File out) throws Exception {
		if(csvWriter == null){
			csvWriter = new BufferedWriter(new FileWriter(out));
			csvWriter.write("Document"+S+"Type"+S+"Id"+S+"Annotation_Variable"+S+"Property"+S+"Document_Value"+S+"Value_Properties"+S+"Annotations\n");
		}
		return csvWriter;
	}
	
	/**
	 * Gets the CSV writer.
	 *
	 * @param out the out
	 * @param templates the templates
	 * @return the CSV writer
	 * @throws Exception the exception
	 */
	private BufferedWriter getCSVWriter(File out,Set<Template> templates) throws Exception {
		if(csvWriter == null){
			csvWriter = new BufferedWriter(new FileWriter(out));
			csvWriter.write("Report");
			for(Template template: templates){
				for(TemplateItem temp: template.getTemplateItems()){
					for(String question: temp.getQuestions()){
						csvWriter.write(S+question);
					}		
					//if(!temp.getUnits().isEmpty())
					//	csvWriter.write(S+getQuestion(temp)+" (units)");
				}
			}
			csvWriter.write("\n");
		}
		return csvWriter;
	}
}
