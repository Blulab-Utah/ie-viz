package edu.pitt.dbmi.nlp.noble.terminology;

import edu.pitt.dbmi.nlp.noble.ontology.IOntology;
import edu.pitt.dbmi.nlp.noble.ontology.owl.OOntology;
import edu.pitt.dbmi.nlp.noble.terminology.impl.NobleCoderTerminology;
import edu.pitt.dbmi.nlp.noble.tools.TextTools;

import java.util.*;


public class NobleCoderTerminologyTest {
	private NobleCoderTerminology terminoloy;
	
	public NobleCoderTerminologyTest(){}
	public NobleCoderTerminologyTest(NobleCoderTerminology term){
		terminoloy = term;
	}
	

	
	/**
	 * run all terms in terminology through search, to make sure that 
	 * we come up with the same concept codes
	 * @throws TerminologyException 
	 */
	public void testTerminologyTerms() throws TerminologyException{
		NobleCoderTerminology terminology = getTerminology();
		//terminology.setScoreConcepts(false);
		/*	terminology.setScoreConcepts(false);
		terminology.setIgnoreCommonWords(false);
		terminology.setIgnoreSmallWords(false);
		terminology.setIgnoreUsedWords(false);*/
		System.out.println("terminology is compacted: "+terminology.isCompacted());
		Map<String,Set<String>> allTerms = getAllTermsMap(terminology);
		
		// go over all terms
		
		int foundTerms = 0;
		int missedTerms = 0;
		int bigTerms = 0;
		long time = 0;
		for(String term: allTerms.keySet()){
			Set<String> cuis = allTerms.get(term);
			Set<String> foundCuis = new HashSet<String>();
			long t = System.currentTimeMillis();
			Concept [] concepts = terminology.search(term);
			time += (System.currentTimeMillis()-t);
			for(Concept c: concepts){
				if(cuis.contains(c.getCode())){
					foundCuis.add(c.getCode());
				}else if(!isSubTerm(c.getMatchedTerm(),term)){
					fail("'"+term+"' returned '"+c.getMatchedTerm()+"' ("+c.getCode()+") which was not expected");
				}
			}
			// if number of retunred cuis is the same as expected cuis
			foundTerms += (foundCuis.size() == cuis.size())?1:0;
			
			for(String cui: cuis){
				if(!foundCuis.contains(cui)){
					int numWords = TextTools.getWords(term).size();
					
					if(numWords < terminology.getMaximumWordsInTerm()){
						fail("'"+term+"' failed to return a "+cui+" instead returned "+foundCuis);
						missedTerms ++;
					}else{
						bigTerms++;
					}
					
				}
			}
			
		}
		
		System.out.println("total terms:\t"+allTerms.size());
		System.out.println("found terms:\t"+foundTerms);
		System.out.println("missed terms:\t"+missedTerms);
		System.out.println("missed big terms:\t"+bigTerms);
		System.out.println("total time:\t"+time+" ms");
	}
	
	private boolean isSubTerm(String subTerm, String term) {
		List<String> subWords = TextTools.getWords(subTerm);
		List<String> allWords = TextTools.getWords(term);
		return allWords.containsAll(subWords);
	}
	private void fail(String string) {
		System.err.println(string);
		
	}
	private Map<String,Set<String>> getAllTermsMap(NobleCoderTerminology terminology) throws TerminologyException {
		Map<String,Set<String>> allTerms = new HashMap<String, Set<String>>();
		for(Concept c: terminology.getConcepts()){
			for(String term: c.getSynonyms()){
				Set<String> codes = allTerms.get(term);
				if(codes == null)
					codes = new HashSet<String>();
				codes.add(c.getCode());
				allTerms.put(term,codes);
			}
		}
		return allTerms;
	}
	
	public NobleCoderTerminology getTerminology(){
		if(terminoloy == null){
			// create terminology
			terminoloy = new NobleCoderTerminology();
		}
		return terminoloy;
	}
	
	public static void main(String[] args) throws Exception {
		//String termFile = "/home/tseytlin/TestRepository2/NCI_Thesaurus.term";

		NobleCoderTerminology term = new NobleCoderTerminology();
		IOntology ont = OOntology.loadOntology("/Users/tseytlin/Work/domains/Pitt/heartDiseaseInDiabetics.owl");
		term.addConcept(ont.getClass("Stress_Electrocardiography").getConcept());

		for(Concept c: term.search("I see ETT here.")){
			c.printInfo(System.out);
		}

		/*
		String termFile = "NCI_Thesaurus";
		NobleCoderTerminology term = new NobleCoderTerminology(termFile);
		term.setSemanticTypeFilter("Neoplastic Process");
		for(Concept c : term.search("melanoma")){
			c.printInfo(System.out);
			System.out.println(Arrays.toString(c.getParentConcepts()));
			
			PathHelper helper = new PathHelper(term);
			System.out.println(helper.getAncestors(c));
			for(ConceptPath p: 	helper.getPaths(c)){
				System.out.println(p);
				for(Concept cc: p){
					System.out.println(cc.getName());
				}
			}
		}
		*/
		//NobleCoderTerminologyTest test = new NobleCoderTerminologyTest(term);
		//test.testTerminologyTerms();
	}

}
