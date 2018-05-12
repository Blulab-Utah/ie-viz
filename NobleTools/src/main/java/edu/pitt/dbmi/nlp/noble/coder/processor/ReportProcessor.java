package edu.pitt.dbmi.nlp.noble.coder.processor;

import edu.pitt.dbmi.nlp.noble.coder.model.Document;
import edu.pitt.dbmi.nlp.noble.coder.model.Processor;
import edu.pitt.dbmi.nlp.noble.terminology.TerminologyError;
import edu.pitt.dbmi.nlp.noble.terminology.TerminologyException;
import edu.pitt.dbmi.nlp.noble.tools.TextTools;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class DocumentProcessor.
 */
public class ReportProcessor implements Processor<Document> {
	private long time;
	private List<Processor<Document>> processors;
	
	
	/**
	 * initialize document processor with default (medical report) setting.
	 * sentence, paragraph and reg-ex based sectionazier
	 */
	public ReportProcessor(){
		addProcessor(new SectionProcessor());
		addProcessor(new SentenceProcessor());
		addProcessor(new ParagraphProcessor());
	}
	
	
	/**
	 * initilize document processor with custom set of document processors
	 * @param proc - list of processors
	 */
	public ReportProcessor(List<Processor<Document>> proc){
		setProcessor(proc);
	}
	
	
	/**
	 * get all processors associated with this document parser
	 * @return list of document processors
	 */
	public List<Processor<Document>> getProcessors(){
		if(processors == null)
			processors = new ArrayList<Processor<Document>>();
		return processors;
	}
	
	/**
	 * add another document processor
	 * @param proc - processor
	 */
	public void addProcessor(Processor<Document> proc){
		getProcessors().add(proc);
	}
	
	/**
	 * set document processors
	 * @param proc - list of document processors
	 */
	public void setProcessor(List<Processor<Document>> proc){
		processors = proc;
	}
	
	
	/**
	 * parse document into Sections and Sentences.
	 *
	 * @param file the file
	 * @return processed document
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Document process(File file) throws FileNotFoundException, IOException {
		Document doc = process(new FileInputStream(file));
		doc.setTitle(file.getName());
		doc.setLocation(file.getAbsolutePath());
		return doc;
	}
	
	/**
	 * parse document into Sections and Sentences.
	 *
	 * @param is the is
	 * @return processed document
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Document process(InputStream is) throws IOException {
		return process(TextTools.getText(is));
	}
	
	/**
	 * parse document into Sections and Sentences.
	 *
	 * @param text the text
	 * @return processed document
	 */
	public Document process(String text) {
		Document doc = new Document();
		doc.setText(text);
		return process(doc);
	}
		
	/**
	 * parse document into Sections and Sentences.
	 *
	 * @param doc the doc
	 * @return processed document
	 */
	public Document process(Document doc) {
		time = System.currentTimeMillis();
		
		// now lets parse sentences for different report types
		for(Processor<Document> processor: getProcessors()){
			try{
				processor.process(doc);
			}catch(TerminologyException ex){
				throw new TerminologyError("Unable to process document "+doc.getLocation(),ex);
			}
		}
	
		doc.setDocumentStatus(Document.STATUS_PARSED);
		time = System.currentTimeMillis() - time;
		doc.getProcessTime().put(getClass().getSimpleName(),time);
		return doc;
	}
	
	
	/**
	 * get the process time for the .
	 *
	 * @return the process time
	 */
	public long getProcessTime(){
		return time;
	}

}
