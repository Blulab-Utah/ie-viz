package edu.pitt.dbmi.nlp.noble.coder.processor;

import java.io.IOException;
import java.util.List;

import edu.pitt.dbmi.nlp.noble.coder.model.Document;
import edu.pitt.dbmi.nlp.noble.coder.model.Paragraph;
import edu.pitt.dbmi.nlp.noble.coder.model.Section;
import edu.pitt.dbmi.nlp.noble.ontology.IOntologyException;
import edu.pitt.dbmi.nlp.noble.terminology.TerminologyException;
import edu.pitt.dbmi.nlp.noble.tools.DataHelper;

public class ParagraphProcessorTest {

	/**
	 * test section processor
	 * @throws IOntologyException 
	 * @throws TerminologyException 
	 * @throws IOException 
	 */
	public void testParagraphProcessor() throws TerminologyException {
		ParagraphProcessor processor = new ParagraphProcessor();
		
		//SectionProcessor processor = new SectionProcessor();
		Document doc = processor.process(new Document(DataHelper.getSampleDocumentText()));
		
		for(Paragraph section: doc.getParagraphs()){
			System.out.println("|"+section.getPart()+"|");
			System.out.println(section.getText());
			System.out.println("-----------------------\n");
		}
		
		System.out.println("process time: "+processor.getProcessTime());
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws TerminologyException the terminology exception
	 * @throws IOException 
	 * @throws IOntologyException 
	 */
	public static void main(String [] args) throws TerminologyException, IOntologyException, IOException{
		new ParagraphProcessorTest().testParagraphProcessor();
	}
	
}
