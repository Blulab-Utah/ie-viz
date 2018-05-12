package edu.pitt.dbmi.nlp.noble.tools;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TextToolsTest {

	
	public void testPlurality(){
		System.out.println("testing plurality ..");
		for(String st: Arrays.asList("nevi","cells","buses","dolls","doors","margins","soldier")){ 
			System.out.println("\t"+st+" "+TextTools.isPlural(st));
		}
	}
	
	public void testAbbreviations(){
		System.out.println("testing abbreviations ..");
		for(String st: Arrays.asList("SKIN","mDNA","BRCA2","Dolls","No","I","help-me")){ 
			System.out.println("\t"+st+" "+TextTools.isLikelyAbbreviation(st));
		}
	}
	
	public void testDateParsing(){
		Date dt = new Date();
		System.out.println(dt);
		String st = dt.toString();
		Date d2 = TextTools.parseDate(st);
		System.out.println(d2);
	}
	
	public void testStringStats(){
		System.out.println(TextTools.getStringStats("Cancer."));
	}
	
	public void testWordSplit(){
		for(String s: Arrays.asList("A","Patient Name..................PatientX",
				"B.	NOTTINGHAM SCORE 8/9 (TUBULES 2, NUCLEAR GRADE 3, MITOTIC RATE 3.",
				"A.	INVASIVE DUCTAL CARCINOMA IN UPPER OUTER QUADRANT, 1.3 CM IN",
				"PART 1:  BREAST, LEFT, MODIFIED RADICAL MASTECTOMY..")){
			long t = System.currentTimeMillis();
			List<String> words = TextTools.getWords(s);
			t = System.currentTimeMillis() - t;
			System.out.println(s+"\n\t"+words+" "+t);
		}
	}
	
	
	/**
	 * The main method.
	 *
	 * @param s the arguments
	 * @throws Exception the exception
	 */
	public static void main(String [] s) throws Exception{
		TextToolsTest t = new TextToolsTest();
		t.testWordSplit();
	}

}
