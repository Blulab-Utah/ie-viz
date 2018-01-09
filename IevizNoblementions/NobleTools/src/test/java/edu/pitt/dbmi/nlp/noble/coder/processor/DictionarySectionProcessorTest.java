package edu.pitt.dbmi.nlp.noble.coder.processor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.pitt.dbmi.nlp.noble.coder.model.Document;
import edu.pitt.dbmi.nlp.noble.coder.model.Processor;
import edu.pitt.dbmi.nlp.noble.coder.model.Section;
import edu.pitt.dbmi.nlp.noble.ontology.IOntology;
import edu.pitt.dbmi.nlp.noble.ontology.IOntologyException;
import edu.pitt.dbmi.nlp.noble.ontology.owl.OOntology;
import edu.pitt.dbmi.nlp.noble.terminology.Terminology;
import edu.pitt.dbmi.nlp.noble.terminology.TerminologyException;
import edu.pitt.dbmi.nlp.noble.tools.DataHelper;

public class DictionarySectionProcessorTest {
	
	
	/**
	 * test section processor
	 * @throws IOntologyException 
	 * @throws TerminologyException 
	 * @throws IOException 
	 */
	public void testSectionProcessor() throws IOntologyException, TerminologyException, IOException{
		DictionarySectionProcessor processor = new DictionarySectionProcessor();
		
		//SectionProcessor processor = new SectionProcessor();
		Document doc = processor.process(new Document(DataHelper.getSampleDocumentText()));
		
		for(Section section: doc.getSections()){
			System.out.println("|"+section.getTitle()+"|"+((section.getHeader() != null)?section.getHeader().getConcept():"")+"|");
			System.out.println(section.getBody());
			System.out.println("-----------------------\n");
		}
		
		System.out.println("process time: "+processor.getProcessTime());
	}
	
	
	public void testSectionProcessor(File dir) throws Exception{
		List<Processor<Document>> list = new ArrayList<Processor<Document>>();
		list.add(new DictionarySectionProcessor());
		//new ReportProcessor(list).processFile(dir);
	}


	public static void main(String[] args) throws Exception {
		if(args.length > 0)
			new DictionarySectionProcessorTest().testSectionProcessor(new File(args[0]));
		else
			new DictionarySectionProcessorTest().testSectionProcessor();

	}

}
