package edu.pitt.dbmi.nlp.noble.tools;

import java.io.*;
import java.util.*;

import edu.pitt.dbmi.nlp.noble.terminology.Concept;
import edu.pitt.dbmi.nlp.noble.terminology.Terminology;
import edu.pitt.dbmi.nlp.noble.terminology.TerminologyException;
import edu.pitt.dbmi.nlp.noble.terminology.impl.NobleCoderTerminology;

public class TermFilterTest {

	/**
	 * Test term filter
	 */
	public void testTermFilter(){
		
		List<String> terms = Arrays.asList(
				"Alzheimer’s disease”","Failure, Renal","Selective Serotonin Reuptake Inhibitors (SSRIs)","Chondria <beetle>","Surgical intervention (finding)",
				"[V] Alcohol use","10*9/L","ADHESIVE @@ BANDAGE","EC 2.7.1.112","Unclassified sequences","Melanoma,NOS","Other findings","Head and Neck Squamous Cell Carcinoma",
				"structure of breast","entire breast","left breast");
		System.out.println("before:\t"+terms);
		System.out.println("after:\t"+TermFilter.filter(terms));
	}
	
	public void testWithTerminology() throws IOException, TerminologyException{
		/*
		NobleCoderTerminology term = new NobleCoderTerminology("NCI_Metathesaurus");
		for(String code: Arrays.asList("C0025202","C0005823")){
			Concept c = term.lookupConcept(code);
			if(c != null){
				System.out.println(c.getName()+" ("+code+")");
				System.out.println("synonyms before:\t"+Arrays.asList(c.getSynonyms()));
				System.out.println("synonyms after:\t"+TermFilter.filter(c.getSynonyms()));
			}
		}
		*/
	}
	
	/*
	public void testIncludedTerms(File file) throws IOException{
		BufferedReader r = new BufferedReader(new FileReader(file));
		for(String l=r.readLine();l != null;l=r.readLine()){
			for(String s: TermFilter.filter(l.trim())){
				System.out.println(s);
			}
		}
		r.close();
	}
	
	public void testExcludedTerms(File file) throws IOException{
		BufferedReader r = new BufferedReader(new FileReader(file));
		for(String l=r.readLine();l != null;l=r.readLine()){
			if(TermFilter.filter(l.trim()).isEmpty()){
				System.out.println(l);
			}
		}
		r.close();
	}
	
	
	public void testRandomConcepts() throws IOException, TerminologyException{
		final String l="\t";
		NobleCoderTerminology term = new NobleCoderTerminology("NCI_Metathesaurus");
		Set<String> codes = term.getAllConcepts();
		
		Map<String,List<Concept>> conceptMap = new TreeMap<String, List<Concept>>();
		
		int i=0;
		int N_SEM_TYPE=10;
		int N_CONCEPTS=1000;
		
		for(String cui: codes){
			Concept c = term.lookupConcept(cui);
			String semType = c.getSemanticTypes()[0].getName();
			
			List<Concept> list = conceptMap.get(semType);
			if(list == null){
				list = new ArrayList<Concept>();
				conceptMap.put(semType,list);
			}
			if(list.size() < N_SEM_TYPE){
				list.add(c);
			}
			
			if(conceptMap.size() > 100){
				break;
			}
		}
		
		System.out.println("Name"+l+"Semantic Type"+l+"Synonym"+l+"Filtered Synonym");
		for(String semType: conceptMap.keySet()){
			for(Concept c: conceptMap.get(semType)){
				for(String s: c.getSynonyms()){
					Set<String> ss = TermFilter.filter(s);
					if(ss.isEmpty()){
						System.out.println(c.getName()+l+semType+l+s+l);
					}else{
						for(String sss: ss ){
							System.out.println(c.getName()+l+semType+l+s+l+sss);
						}
					}
					
				}
			}
		}
		
	}
	*/
	
	public static void main(String[] args) throws IOException, TerminologyException {
		TermFilterTest test = new TermFilterTest();
		test.testTermFilter();
		//test.testWithTerminology();
		//test.testIncludedTerms(new File("/home/tseytlin/Data/Terminologies/NCI_Metathesaurus/NCI_Metathesaurus-201203D/all.uniq.terms.txt"));
		//test.testExcludedTerms(new File("/home/tseytlin/Data/Terminologies/NCI_Metathesaurus/NCI_Metathesaurus-201203D/all.uniq.terms.txt"));
		//test.testRandomConcepts();
	}

}
