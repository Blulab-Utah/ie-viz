package edu.utah.blulab.marshallers.pycontext;

import java.io.File;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
//import org.apache.commons.lang3.StringEscapeUtils;



import edu.utah.blulab.domainontology.*;

public class DOtoPyConText {
	
	//private File outputFileLex = new File("C:\\Users\\Bill\\Documents\\2015\\Chapman Lab\\Papers\\KA for JBMS\\Experiment\\KA_modifiers.txt");
	//private File outputFileCon = new File("C:\\Users\\Bill\\Documents\\2015\\Chapman Lab\\Papers\\KA for JBMS\\Experiment\\KA_lexical.txt");
	//private String inputDomainSchema = "C:\\Users\\Bill\\Documents\\2015\\Chapman Lab\\Papers\\KA for JBMS\\Experiment\\carotid stenosis.owl";
	private File outputFileLex = new File("C:\\Users\\Bill\\Documents\\2015\\Chapman Lab\\Projects\\Vivace Pilot\\Data\\Vivace_modifiers.txt");
	private File outputFileCon = new File("C:\\Users\\Bill\\Documents\\2015\\Chapman Lab\\Projects\\Vivace Pilot\\Data\\Vivace_lexical.txt");
	private String inputDomainSchema = "C:\\Users\\Bill\\Documents\\2015\\Chapman Lab\\Projects\\Vivace Pilot\\Data\\vivace.owl";

	public static void main(String[] args)  {
		System.out.println("Starting test app.");

		DOtoPyConText parser = new DOtoPyConText();
		try {
			parser.createPyConTextFiles();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	
	public void createPyConTextFiles() throws Exception {
		//DomainOntology domain = new DomainOntology("/Users/mtharp/use_cases/DomainOntologies/pneumonia.owl");
		//DomainOntology domain = new DomainOntology("/Users/mtharp/Desktop/vincipneu.owl.xml");
		DomainOntology domain = new DomainOntology(inputDomainSchema); 

		//List<String> conceptIDs = Arrays.asList("KA317", "KA318", "KA319", "KA320", "KA321");
		//domain.getVariable("leukocytosis");
		//Variable testVar = domain.getVariable("KA247"); 
		
		/*ArrayList<Variable> domainVariables = domain.getAllVariables();
		System.out.println("********** Domain Variables: **********");
		for(Variable var : domainVariables){
			System.out.println(var.toString());
		}*/
		
		//HashSet modMap = new HashSet();
		
		String lineSep = System.getProperty("line.separator");
		
		// write out the concept file
		Term testTerm = new Term();
		StringBuilder conceptFile = new StringBuilder();
		conceptFile.append("literal	 category	 regex	 rule\n");
		ArrayList<Variable> domainVariables = domain.getAllVariables();
		System.out.println("********** Domain Variables: **********");
		for(Variable var : domainVariables){
			System.out.println(var.toString());
		//for (String id: conceptIDs){
			//Variable testVar = domain.getVariable(id);
			//System.out.println(var.toString());
			
			// get the variable name, synonyms and misspellings and write them to string
			String varName = var.getVarName();
			testTerm = var.getConcept();
			
			// add prefLabel
			int index = 1;
			String prefLabel = testTerm.getPrefTerm();
			String category = prefLabel.replaceAll(" ", "_").toUpperCase();
			conceptFile.append(varName + index + "\t" + category + "\tr'''(" + prefLabel + ")'''\t\n"); // comment out for regular use
			index++;
			
			ArrayList<String> synonymList = testTerm.getSynonym();
			//conceptFile.append(varName + "\t" + "CAROTID_DISEASE\tr'''(" + varName); // uncomment for regular use
			
			// add synonyms
			for (String synonym : synonymList){
				if (!containsSpecialChars(synonym)){
					//System.out.println("Filtered Special Out: " + synonym);
					continue;
				}
				/*if (!containsAccentChars(synonym)){
					//System.out.println("Filtered Accent Out: " + synonym);
					continue;
				}*/
				//removeAccents(synonym);
				//System.out.println("Kept: " + synonym);

				conceptFile.append(varName + index + "\t" + category + "\tr'''(" + synonym + ")'''\t\n"); // comment out for regular use
				//conceptFile.append("|" + synonym); // uncomment for regular use
				index++;
			}
			
			// add misspellings
			ArrayList<String> misspellingList = testTerm.getMisspelling();
			for (String misspelling : misspellingList){
				if (!containsSpecialChars(misspelling)){
					continue;
				}
				conceptFile.append(varName + index + "\t" + category + "\tr'''(" + misspelling + ")'''\t\n"); 
				index++;
			}
			
			// add regexs
			ArrayList<String> regexList = testTerm.getRegex();
			for (String regex : regexList){
				if (!containsSpecialChars(regex)){
					continue;
				}
				conceptFile.append(varName + index + "\t" + category + "\tr'''(" + regex + ")'''\t\n"); 
				index++;
			}
			
			//conceptFile.append(")'''\t\n"); // uncomment for regular use
		}
		
		// write File
		FileUtils.write(outputFileCon, conceptFile);
		
		// ** note: this modifiers will not be written to the file. This may not be what is wanted for all cases
		HashSet<String> exclusionList = new HashSet<String>();
		exclusionList.add("<http://blulab.chpc.utah.edu/ontologies/ModifierOntology.owl#Patient_Experiencer>");
		exclusionList.add("<http://blulab.chpc.utah.edu/ontologies/ModifierOntology.owl#DefiniteExistence_Certainty>");
		exclusionList.add("<http://blulab.chpc.utah.edu/ontologies/ModifierOntology.owl#Overlap_DocTimeRel>");
		exclusionList.add("<http://blulab.chpc.utah.edu/ontologies/ModifierOntology.owl#DefiniteNegatedExistence_Certainty>");
		exclusionList.add("Patient_Experiencer");
		exclusionList.add("DefiniteExistence_Certainty");
		exclusionList.add("Overlap_DocTimeRel");
		exclusionList.add("DefiniteNegatedExistence_Certainty");
		int excSize = exclusionList.size();
		
		
		ArrayList<Modifier> modifierDictionary = domain.createModifierDictionary();
		System.out.println("********** Modifier Dictionary: **********");
		for(Modifier modifier : modifierDictionary){
			System.out.println(modifier.toString() + "      " + modifier.getModName());
		}
		
		StringBuilder modifierFile = new StringBuilder();
		modifierFile.append("literal	 category	 regex	 rule\n");
		
		// hack to get the modifiers
		HashMap<String, Modifier> modMap = new HashMap<String, Modifier>();
		for (Modifier mod : modifierDictionary){
			exclusionList.add(mod.getModName());
			//System.out.println("**" + mod + "**" + exclusionList.size() + "::" + excSize + "***");
			if (exclusionList.size() == excSize) { // skip the modifier if it is in the exclusion list
				continue;
			} 
			exclusionList.remove(mod);
			//System.out.println(mod);
			String modName = "<" + mod.getUri() + ">";
			//System.out.println("***modName: " + modName);
			modMap.put(modName, mod);

			/*ArrayList<LexicalItem> items = mod.getItems();
			for (LexicalItem item : items){
				System.out.println("Lexical Item: " + item.getTerm().getPrefTerm());
			}*/
		}
		
		excSize = exclusionList.size();
		for(Variable testVar : domainVariables){
			//Variable testVar = domain.getVariable(id);
			//System.out.println(testVar.toString());
			
			// get the variable name
			String varName = testVar.getVarName();
			
			// get the modifiers
			ArrayList<String> modList = testVar.getModifiers();
			for (String modStr : modList){
				//System.out.println(modStr);
				exclusionList.add(modStr);
				
				if (exclusionList.size() == excSize) { // skip the modifier if it is in the exclusion list
					continue;
				} 
				exclusionList.remove(modStr);
				
				//System.out.println("**" + modStr + "**" + exclusionList.size() + "::" + excSize + "***");
				Modifier mod = modMap.get(modStr);
				ArrayList<LexicalItem> items = mod.getItems();
				//System.out.println(mod);
				modifierFile.append(mod.getModName() + "\t" + varName.toUpperCase() + "\tr'''(");
				//System.out.println("varName: " + varName);
				int count = 0;
				for (LexicalItem item : items){
					if (count > 0){
						modifierFile.append("|");
					}
					modifierFile.append(item.getTerm().getPrefTerm()); 
					//System.out.println("Lex Preflabel: ***" + item.getTerm().getPrefTerm() + "***");
					count++;
				}
				modifierFile.append(")'''\tbidirectional\n");
			}
		}
		

		// write modifiers to lexical file
		String remainderOfFile = FileUtils.readFileToString(new File("C:\\Users\\Bill\\Dropbox\\Carotid Stenosis\\old_KB_pyConText\\docsConText_KnowledgeBase_lexical_subset.txt"));
		//String remainderOfFile = StringEscapeUtils.escapeJava(str);

		modifierFile.append(remainderOfFile);
		//System.out.println(modifierFile.toString());
		
		FileUtils.write(outputFileLex, modifierFile);

		//Term testTerm = testVar.getConcept();
		//System.out.println("Output:  " + sb.toString());

		
		System.out.println("Done!");
	}
	
	static boolean containsSpecialChars(String string)
	{
		/*if (string.contains(" ")){
			return false;
		} else {
			Pattern p = Pattern.compile("[\\W]");
		    Matcher m = p.matcher(string);
		    return m.find();
		}*/
		Pattern p = Pattern.compile("^[a-zA-Z0-9 -,]+$");
		//Pattern p = Pattern.compile(".*\\W+.*");
	    Matcher m = p.matcher(string);
	    return m.find();
		
	}
	
	static boolean containsAccentChars(String string)
	{
		Pattern p = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		//Pattern p = Pattern.compile(".*\\W+.*");
	    Matcher m = p.matcher(string);
	    return m.matches(); //m.find();
	}
	
	public static String removeAccents(String text) {
	    return text == null ? null :
	        Normalizer.normalize(text, Form.NFD)
	            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "***");
	}
}
