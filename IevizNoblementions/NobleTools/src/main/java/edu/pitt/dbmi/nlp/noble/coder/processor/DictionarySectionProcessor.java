package edu.pitt.dbmi.nlp.noble.coder.processor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.pitt.dbmi.nlp.noble.coder.model.Document;
import edu.pitt.dbmi.nlp.noble.coder.model.Mention;
import edu.pitt.dbmi.nlp.noble.coder.model.Processor;
import edu.pitt.dbmi.nlp.noble.coder.model.Section;
import edu.pitt.dbmi.nlp.noble.coder.model.Sentence;
import edu.pitt.dbmi.nlp.noble.ontology.IClass;
import edu.pitt.dbmi.nlp.noble.ontology.IOntology;
import edu.pitt.dbmi.nlp.noble.ontology.IOntologyException;
import edu.pitt.dbmi.nlp.noble.ontology.owl.OOntology;
import edu.pitt.dbmi.nlp.noble.terminology.Concept;
import edu.pitt.dbmi.nlp.noble.terminology.Terminology;
import edu.pitt.dbmi.nlp.noble.terminology.TerminologyError;
import edu.pitt.dbmi.nlp.noble.terminology.TerminologyException;
import edu.pitt.dbmi.nlp.noble.terminology.impl.NobleCoderTerminology;
import edu.pitt.dbmi.nlp.noble.util.ConceptImporter;

/**
 * section report based on a dictionary of sections
 * defined in the terminology 
 * @author tseytlin
 *
 */
public class DictionarySectionProcessor implements Processor<Document> {
	private static final String DOCUMENT_SECTION_ONTOLOGY = "http://blulab.chpc.utah.edu/ontologies/v2/DocumentSection.owl";
	private static final String DOCUMENT_SECTION = "DocumentSection";
	private static final String HEADER_SEPERATOR_CHAR = ":";
	private static final Pattern HEADER_SEPERATOR_PATTERN = Pattern.compile("\\s*"+HEADER_SEPERATOR_CHAR);
	private int HEADER_SIZE_LIMIT = 65;
	
	private long time;
	private Terminology terminology;
	
	/**
	 * initialize the section processor with default section names from:
	 * http://blulab.chpc.utah.edu/ontologies/v2/DocumentSection.owl
	 */
	public DictionarySectionProcessor(){
		try{
			// check if pre-existing terminology exists
			if(NobleCoderTerminology.hasTerminology(DOCUMENT_SECTION)){
				terminology = new NobleCoderTerminology(DOCUMENT_SECTION);
			}else{
				File file = new File(NobleCoderTerminology.getPersistenceDirectory(),DOCUMENT_SECTION+NobleCoderTerminology.TERM_SUFFIX);
				terminology = loadDocumentSections(OOntology.loadOntology(DOCUMENT_SECTION_ONTOLOGY),file);
				if(terminology instanceof NobleCoderTerminology)
					((NobleCoderTerminology)terminology).dispose();
				terminology = new NobleCoderTerminology(DOCUMENT_SECTION);
			}
		}catch(Exception ex){
			throw new TerminologyError("Unable to load ConText ontology", ex);
		}
	}
	
	
	
	/**
	 * initialize the section processor
	 * @param term - terminology that defines section names
	 */
	public DictionarySectionProcessor(Terminology term){
		terminology = term;
	}
	
	/**
	 * initialize the section processor
	 * @param ont - Schema.owl derived ontology that has DocumentSection class defined or ontology where everything is a section
	 * @throws IOntologyException - may thow exception
	 * @throws TerminologyException  - may thow exception
	 * @throws IOException  - may thow exception
	 */
	public DictionarySectionProcessor(IOntology ont) throws IOException, TerminologyException, IOntologyException{
		this(loadDocumentSections(ont,null));
	}
	
	/**
	 * return dictionary of document section headers
	 * @return dictionary of section headers that was loaded
	 */
	public Terminology getTerminology(){
		return terminology;
	}
	
	/**
	 * creates and save a section dictionary from the ontology that extends Schema.owl or
	 * simply has a DocumentSection class defined
	 * @param ont - ontology file from which to load sections
	 * @param saveLocation - location where to save terminology, if null, then don't save it
	 * @return terminology that was built
	 * @throws IOntologyException  - may thow exception
	 * @throws TerminologyException - may thow exception
	 * @throws IOException - may thow exception
	 */
	public static Terminology loadDocumentSections(IOntology ont, File saveLocation) throws IOException, TerminologyException, IOntologyException {
		NobleCoderTerminology term = null;
		if(saveLocation != null){
			term = new NobleCoderTerminology(saveLocation,false);
		}else{
			term = new NobleCoderTerminology();
		}
		term.setSelectBestCandidate(false);
		term.setScoreConcepts(false);
		term.setDefaultSearchMethod(NobleCoderTerminology.PRECISE_MATCH);
		term.setStemWords(false);
		term.setIgnoreDigits(false);
		term.setHandlePossibleAcronyms(true);
		term.setStripStopWords(false);
		term.setIgnoreUsedWords(true);
		
		
		IClass sectionRoot = ont.getClass(DOCUMENT_SECTION);
		if(sectionRoot == null)
			sectionRoot = ont.getRoot();
		
		for(IClass cls: sectionRoot.getSubClasses()){
			Concept c = ConceptImporter.getInstance().createConcept(cls);
			ConceptImporter.getInstance().addConcept(term,c);
			if(cls.hasDirectSuperClass(sectionRoot))
				term.getStorage().getRootMap().put(c.getCode(),"");
			
		}
		
		if(saveLocation != null){
			ConceptImporter.getInstance().compact(term);
			term.save();
		}
		
		return term;
	}

	/**
	 * process document
	 * @param doc - document
	 * @return doc - same document
	 * @throws TerminologyException - if anything happens, throw it
	 */
	public Document process(Document doc) throws TerminologyException {
		time = System.currentTimeMillis();
		
		// lets do sectioning first
		int offset = 0;
		int sectionStart = 0, bodyStart = 0;
		Mention lastHeader = null;
		for(String s: doc.getText().split("\n")){
			// see if we have section header was found
			Mention header = detectHeader(offset,s);
			if(header != null){
				// save previous header
				if(lastHeader != null){
					Section sec = new Section(doc,sectionStart,bodyStart,offset); //-1
					sec.setHeader(lastHeader);
					doc.addSection(sec);
				}
				sectionStart = offset;
				bodyStart = getBodyStart(header);
				lastHeader = header;
			}
			
			// increment offset
			offset += s.length()+1;
		}
		// wrap up with last section
		if(lastHeader != null){
			Section sec = new Section(doc,sectionStart,bodyStart,offset); //-1
			sec.setHeader(lastHeader);
			doc.addSection(sec);
		}
		
		time = System.currentTimeMillis() -time;
		doc.getProcessTime().put(getClass().getSimpleName(),time);
		return doc;
	}
	
	/**
	 * get body start position
	 * @param header
	 * @return
	 */
	private int getBodyStart(Mention header){
		// is there a valid "header" character???
		Sentence s = header.getSentence();
		String text = s.getText().substring(header.getEndPosition()-s.getOffset());
		Matcher mt = HEADER_SEPERATOR_PATTERN.matcher(text);
		if(mt.find() && mt.start() == 0)
			return mt.end()+header.getEndPosition();
		return header.getEndPosition();
	}
	

	/**
	 * detect if this string starts with section heading
	 * @param text
	 * @return
	 * @throws TerminologyException 
	 */
	private Mention detectHeader(int offs, String text) throws TerminologyException {
		// resize input line to be more efficient
		if(text.length() > HEADER_SIZE_LIMIT){
			text = text.substring(0,HEADER_SIZE_LIMIT);
			// skip lines that have more then header size limit and 
			// no : as they will violate the condition later on anyhow
			if(text.indexOf(HEADER_SEPERATOR_CHAR) < 0)
				return null;
		}
		
		// do search
		Sentence s = terminology.process(new Sentence(text,offs,Sentence.TYPE_HEADER));
		for(Mention m: s.getMentions()){
			// i only care of mentions that start at the begining of the sentence almost verbatum
			if(startsWith(s,m)){
				// OK, I need to be a bit cute here
				// header either has to span the entire sentence
				if(m.getEndPosition() + 2 >= s.getEndPosition())
					return m;
				// OR header has to be seperated from the rest of text in line by : or some such thing 
				Matcher mt = HEADER_SEPERATOR_PATTERN.matcher(text.substring(m.getEndPosition()-offs));
				if(mt.find() && mt.start() == 0)
					return m;
			}
		}
		
		return null;
	}

	/**
	 * does this sentence starts with this mention 
	 * accounting for indentation
	 * @param s - sentence
	 * @param m - mention
	 * @return true or false
	 */
	private boolean startsWith(Sentence s, Mention m){
		if(s.getStartPosition() == m.getStartPosition())
			return true;
		String prefix = s.getText().substring(0,m.getStartPosition()-s.getStartPosition());
		// if we have whitespace or TIES special case [ ]
		if(prefix.trim().length() == 0 || prefix.equals("["))
			return true;
		return false;
	}
	
	public long getProcessTime() {
		return time;
	}

}
