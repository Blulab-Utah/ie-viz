package edu.pitt.dbmi.nlp.noble.mentions;

import edu.pitt.dbmi.nlp.noble.mentions.model.AnnotationVariable;
import edu.pitt.dbmi.nlp.noble.mentions.model.Composition;
import edu.pitt.dbmi.nlp.noble.mentions.model.DomainOntology;
import edu.pitt.dbmi.nlp.noble.mentions.model.Instance;
import edu.pitt.dbmi.nlp.noble.ontology.IOntologyException;
import edu.pitt.dbmi.nlp.noble.terminology.TerminologyException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

public class NobleMentionsTest {
	
	/**
	 * 
	 * @param args
	 * @throws TerminologyException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws IOntologyException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException, TerminologyException, IOntologyException {
		
		
			/*
			 * File file = new File("/home/tseytlin/Data/BiRADS/pathology/reports/realDCIS.txt");
			 * Composition doc = noble.process(file);
			for(AnnotationVariable var: doc.getAnnotationVariables()){
				System.out.println(var);
			}
			System.out.println(doc.getProcessTime());
			
			// visualize terminologies
			TerminologyBrowser browser = new TerminologyBrowser();
			browser.setTerminologies(domainOntology.getTerminologies());
			browser.showDialog(null,"NobleMentions");*/
		
		
		if(args.length > 1){
			DomainOntology domainOntology = new DomainOntology(args[0]);
			//DomainOntology domainOntology = new DomainOntology("/home/tseytlin/Data/BiRADS/ontology/pathologicDx.owl");
			NobleMentions noble = new NobleMentions(domainOntology);
			
			final String I = "|";
			File [] files = new File(args[1]).listFiles();
			Arrays.sort(files);
			for(File f: files){
				if(f.getName().endsWith(".txt")){
					Composition doc = noble.process(f);
					for(AnnotationVariable var: doc.getAnnotationVariables()){
						for(Instance body: var.getModifierInstances("hasBodySite")){
							System.out.println(f.getName()+I+var.getName()+I+body.getName()+I+toString(body.getModifierInstances("hasBodySide"))+I+toString(body.getModifierInstances("hasClockfacePosition"))+I+var.getAnnotations());
						}
					}
				}
			}
			domainOntology.write(new File(System.getProperty("user.home")+File.separator+domainOntology.getName()+".owl"));
		}else{
			System.err.println("Usage: java NobleMentionsCmd.jar <owl url> <files>");
			System.exit(1);
		}
	}

	private static String toString(Collection obj){
		if(obj == null)
			return "";
		String s = obj.toString();
		return s.substring(1, s.length()-1);
	}
	

}
