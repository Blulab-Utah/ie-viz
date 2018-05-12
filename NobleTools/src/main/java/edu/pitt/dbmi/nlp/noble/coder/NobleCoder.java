package edu.pitt.dbmi.nlp.noble.coder;


import edu.pitt.dbmi.nlp.noble.coder.model.Document;
import edu.pitt.dbmi.nlp.noble.coder.model.Mention;
import edu.pitt.dbmi.nlp.noble.coder.model.Processor;
import edu.pitt.dbmi.nlp.noble.coder.model.Sentence;
import edu.pitt.dbmi.nlp.noble.coder.processor.DocumentProcessor;
import edu.pitt.dbmi.nlp.noble.terminology.Terminology;
import edu.pitt.dbmi.nlp.noble.terminology.TerminologyError;
import edu.pitt.dbmi.nlp.noble.terminology.TerminologyException;
import edu.pitt.dbmi.nlp.noble.terminology.impl.NobleCoderTerminology;
import edu.pitt.dbmi.nlp.noble.tools.AcronymDetector;
import edu.pitt.dbmi.nlp.noble.tools.ConText;
import edu.pitt.dbmi.nlp.noble.tools.TextTools;
import edu.pitt.dbmi.nlp.noble.util.DeIDUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * NobleCoder
 * TODO: implement abbreviation whitelist/blacklist issues.
 */
public class NobleCoder implements Processor<Document>{
	public static int FILTER_DEID = 1;
	public static int FILTER_HEADER = 2;
	public static int FILTER_WORKSHEET = 4;
	
	//private final String ABBREV_TERMINOLOGY = "BiomedicalAbbreviations";
	//private DefaultRepository repository;
	private Terminology terminology; //,abbreviations;
	private Processor<Document> documentProcessor;
	private AcronymDetector acronymDetector;
	private ConText conText;
	private boolean handleAcronyms = true,handleNegation = true; //skipAbbrreviationLogic
	private int processFilter = FILTER_DEID|FILTER_HEADER;
	//private Map<String,String> abbreviationWhitelist;
	private long time;
	
	
	/**
	 * invoke NobleCoderTool pointing to a terminology .term direcotry
	 * all of the relevant settings should be set in .term/search.properties
	 *
	 * @param location the location
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public NobleCoder(File location) throws IOException {
		NobleCoderTerminology.setPersistenceDirectory(location.getParentFile());
		setTerminology(new NobleCoderTerminology(location.getName()));
	}
	
	/**
	 * invoke NobleCoderTool pointing to a terminology .term direcotry
	 * all of the relevant settings should be set in .term/search.properties
	 *
	 * @param name the name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public NobleCoder(String name) throws IOException {
		setTerminology(new NobleCoderTerminology(name));
	}
	
	
	/**
	 * invoke NobleCoderTool pointing to a terminology .term direcotry
	 * all of the relevant settings should be set in .term/search.properties
	 *
	 * @param term the term
	 */
	public NobleCoder(Terminology term) {
		setTerminology(term);
	}
	
	/**
	 * empty NobleCoder instance, need to use setTerminology() to specify
	 * terminology to use.
	 */
	public NobleCoder(){}
	
	
	/**
	 * get document processor for parsing documents.
	 *
	 * @return the document processor
	 */
	public Processor<Document> getDocumentProcessor() {
		if(documentProcessor == null)
			documentProcessor = new DocumentProcessor();
		return documentProcessor;
	}
	
	/**
	 * set document processor .
	 *
	 * @param documentProcessor the new document processor
	 */

	public void setDocumentProcessor(Processor<Document> documentProcessor) {
		this.documentProcessor = documentProcessor;
	}

	
	/**
	 * do not process certain aspects of input text
	 * Ex: DeID tags, headers, etc.. 
	 * FILTER_DEID   - filter out DeID tags 
	 * FILTER_HEADER - filter out section headers Ex: FINAL DIAGNOSIS, COMMENT etc.
	 * FILTER_WORKSHEET - do not process sentences that are marked as Sentence.TYPE_WORKSHEET
	 * return processFilter - a conjunction (OR) of filters
	 *
	 * @return the process filter
	 */
	public int getProcessFilter() {
		return processFilter;
	}

	/**
	 * do not process certain aspects of input text
	 * Ex: DeID tags, headers, etc.. 
	 * FILTER_DEID   - filter out DeID tags 
	 * FILTER_HEADER - filter out section headers Ex: FINAL DIAGNOSIS, COMMENT etc.
	 * FILTER_WORKSHEET - do not process sentences that are marked as Sentence.TYPE_WORKSHEET
	 * @param processFilter - a conjunction (OR) of filters
	 */
	public void setProcessFilter(int processFilter) {
		this.processFilter = processFilter;
	}

	/**
	 * set custom terminology.
	 *
	 * @param term the new terminology
	 */
	public void setTerminology(Terminology term) {
		terminology = term;
		try {
			setupAcronyms(new File(term.getLocation()));
		} catch (IOException e) {
			throw new TerminologyError("Unable to fine terminology location", e);
		}
	}

	/**
	 * get an instance of acronym detector that .
	 *
	 * @return the acronym detector
	 */
	
	public AcronymDetector getAcronymDetector() {
		if(acronymDetector == null)
			acronymDetector = new AcronymDetector();
		return acronymDetector;
	}

	
	/**
	 * setup acronyms.
	 *
	 * @param location the new up acronyms
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void setupAcronyms(File location) throws IOException{
		// load abbreviation information
		/*
		skipAbbrreviationLogic = true;
		File props = new File(location,"search.properties");
		if(props.exists()){
			Properties p = new Properties();
			p.load(new FileInputStream(props));
			if(Boolean.parseBoolean(p.getProperty("ignore.acronyms","false"))){
				File af = new File(p.getProperty("abbreviation.whitelist"));
				if(!af.exists()){
					af = new File(location,af.getName());
				
				}
				abbreviationWhitelist = TextTools.loadResource(af.getAbsolutePath());
				abbreviations = (NobleCoderTerminology) new NobleCoderTerminology(
						new File(location,p.getProperty("abbreviation.terminology",ABBREV_TERMINOLOGY)).getAbsolutePath());
				skipAbbrreviationLogic = false;
			}
		}
		*/
	}
	
	/**
	 * Gets the terminology.
	 *
	 * @return the terminology
	 */
	public Terminology getTerminology() {
		return terminology;
	}


	
	/**
	 * process abbreviations and acronyms.
	 *
	 * @return true, if is acronym expansion
	 */
	public boolean isAcronymExpansion() {
		return handleAcronyms;
	}

	/**
	 * handle acronym expansion.
	 *
	 * @param handleAcronyms the new acronym expansion
	 */
	public void setAcronymExpansion(boolean handleAcronyms) {
		this.handleAcronyms = handleAcronyms;
	}

	
	
	/**
	 * Checks if is context detection.
	 *
	 * @return true, if is context detection
	 */
	public boolean isContextDetection() {
		return handleNegation;
	}

	/**
	 * Sets the context detection.
	 *
	 * @param handleNegation the new context detection
	 */
	public void setContextDetection(boolean handleNegation) {
		this.handleNegation = handleNegation;
	}

	
	/**
	 * process document represented as a string.
	 *
	 * @param document the document
	 * @return the document
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 */
	public Document process(File document) throws FileNotFoundException, IOException, TerminologyException {
		Document doc = new Document(TextTools.getText(new FileInputStream(document)));
		doc.setLocation(document.getAbsolutePath());
		doc.setTitle(document.getName());
		return process(getDocumentProcessor().process(doc));
	}
	
	/**
	 * process a document represented as a document object.
	 *
	 * @param doc the doc
	 * @return the document
	 * @throws TerminologyException the terminology exception
	 */
	public Document process(Document doc) throws TerminologyException {
		time = System.currentTimeMillis();
		getAcronymDetector().clearAcronyms();
		
		// check if document has been parsed
		if(Document.STATUS_UNPROCESSED.equals(doc.getDocumentStatus())){
			doc = getDocumentProcessor().process(doc);
		}
		
		// go over all sentences  
		for(Sentence s : doc.getSentences()){
			if(!filterSentence(s))
				process(s);
		}
		doc.setDocumentStatus(Document.STATUS_CODED);
		
		// return processed document
		time = System.currentTimeMillis() - time;
		doc.getProcessTime().put(getClass().getSimpleName(),time);
		return doc;
	}
	
	/**
	 * process text string to get a list of mentions.
	 *
	 * @param text the text
	 * @return the list
	 * @throws TerminologyException the terminology exception
	 */
	public List<Mention> process(String text) throws TerminologyException {
		return process(new Sentence(text)).getMentions();
	}
	
	
	/**
	 * Process.
	 *
	 * @param sentence the sentence
	 * @return the sentence
	 * @throws TerminologyException the terminology exception
	 */
	public Sentence process(Sentence  sentence) throws TerminologyException {
		long time = System.currentTimeMillis();
		
		// optionally filter text
		String text = sentence.getText(); 
		sentence.setText(filterText(text));		
		
		// search for concepts from main terminology
		getTerminology().process(sentence);
		
		// handle acronyms that are mentioned in a document
		if(handleAcronyms){
			getAcronymDetector().process(sentence);
		}
		
		// now lets do negation detection
		if(handleNegation){
			getConText().process(sentence);
		}
		
		// set process time and roll-back oritinal text
		sentence.setText(text);
		sentence.getProcessTime().put(getClass().getSimpleName(),(System.currentTimeMillis()-time));
		return sentence;
	}
	
	
	
	/**
	 * Gets the con text.
	 *
	 * @return the con text
	 */
	public ConText getConText() {
		if(conText == null)
			conText = new ConText();
		return conText;
	}
	
	/**
	 * Sets the con text.
	 *
	 * @param ct the new con text
	 */
	public void setConText(ConText ct){
		conText = ct;
	}

	/**
	 * return true if sentence should not be parsed
	 * Ex: blank, section heading, de-id string etc..
	 *
	 * @param line the line
	 * @return true, if successful
	 */
	private boolean filterSentence(Sentence line){
		// skip blank lines
		if(line.getText().length() == 0){
			return true;
		}
		// don't process section headings
		if((getProcessFilter()&FILTER_HEADER) > 0 && Sentence.TYPE_HEADER.equals(line.getSentenceType())){
			return true;
		}
		
		// skip worksheet sentences
		if((getProcessFilter()&FILTER_WORKSHEET) > 0 && Sentence.TYPE_WORKSHEET.equals(line.getSentenceType())){
			return true;
		}
		// skip DeID header
		if((getProcessFilter()&FILTER_DEID) > 0 && DeIDUtils.isDeIDHeader(line.getText())){
			return true;
		}
		
		return false;
	}
	
	
	/**
	 * filter junk out.
	 *
	 * @param line the line
	 * @return the string
	 */
	private String filterText(String line){
		if((getProcessFilter()&FILTER_DEID) > 0)
			return DeIDUtils.filterDeIDTags(line);
		return line;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.coder.model.Processor#getProcessTime()
	 */
	public long getProcessTime() {
		return time;
	}

}
