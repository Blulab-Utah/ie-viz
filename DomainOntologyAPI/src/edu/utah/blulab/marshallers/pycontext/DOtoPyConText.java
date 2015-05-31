package edu.utah.blulab.marshallers.pycontext;

import java.io.File;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import edu.utah.blulab.domainontology.*;

public class DOtoPyConText {

	public static void main(String[] args) throws Exception {
		System.out.println("Starting test app.");
		// TODO Auto-generated method stub
		//DomainOntology domain = new DomainOntology("/Users/mtharp/use_cases/DomainOntologies/pneumonia.owl");
		//DomainOntology domain = new DomainOntology("/Users/mtharp/Desktop/vincipneu.owl.xml");
		DomainOntology domain = new DomainOntology("C:\\Users\\Bill\\Desktop\\carotid stenosis.owl"); 

		List<String> conceptIDs = Arrays.asList("KA317", "KA318", "KA319", "KA320", "KA321");
		//domain.getVariable("leukocytosis");
		//Variable testVar = domain.getVariable("KA247"); 
		
		//HashSet modMap = new HashSet();
		
		// write out the concept file
		Term testTerm = new Term();
		StringBuilder sb = new StringBuilder();
		sb.append("literal	 category	 regex	 rule\n");
		for (String id: conceptIDs){
			Variable testVar = domain.getVariable(id);
			System.out.println(testVar.toString());
			
			// get the variable name and synonyms and write them to string
			String varName = testVar.getVarName();
			testTerm = testVar.getConcept();
			ArrayList<String> synonymList = testTerm.getSynonym();
			sb.append(varName + "\t" + "CAROTID_DISEASE\tr'''(" + varName);
			for (String synonym : synonymList){
				sb.append(" | " + synonym);
			}
			sb.append("'')\t");
			
		}
		
		HashSet<String> exclusionList = new HashSet<String>();
		exclusionList.add("<http://blulab.chpc.utah.edu/ontologies/ModifierOntology.owl#Patient_Experiencer>");
		exclusionList.add("<http://blulab.chpc.utah.edu/ontologies/ModifierOntology.owl#DefiniteExistence_Certainty>");
		exclusionList.add("<http://blulab.chpc.utah.edu/ontologies/ModifierOntology.owl#Overlap_DocTimeRel>");
		int excSize = exclusionList.size();
		
		StringBuilder lexicalFile = new StringBuilder();
		lexicalFile.append("literal	 category	 regex	 rule\n");
		for (String id: conceptIDs){
			Variable testVar = domain.getVariable(id);
			System.out.println(testVar.toString());
			
			// get the variable name
			String varName = testVar.getVarName();
			
			// get the modifiers
			ArrayList<String> modList = testVar.getModifiers();
			for (String mod : modList){
				//System.out.println(mod);
				exclusionList.add(mod);
				//System.out.println("**" + mod + "**" + exclusionList.size() + "::" + excSize + "***");
				if (exclusionList.size() == excSize) { // skip the modifier if it is in the exclusion list
					continue;
				} 
				exclusionList.remove(mod);
				System.out.println(mod);
				lexicalFile.append(mod + "\t" + varName + "\tr''''''\t");
			}
		}
		
		// write modifiers to lexical file
		//String str = FileUtils.readFileToString(new File("C:\\Users\\Bill\\Dropbox\\Carotid Stenosis\\old_KB_pyConText\\docsConText_KnowledgeBase_lexical_subset.txt"));
		//String remainderOfFile = StringEscapeUtils.escapeJava(str);
		//lexicalFile.append(remainderOfFile);
		System.out.println(lexicalFile.toString());

		//Term testTerm = testVar.getConcept();
		//System.out.println("Output:  " + sb.toString());

		
		System.out.println("Done!");
	}
}
