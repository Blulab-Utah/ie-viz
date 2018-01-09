package edu.pitt.dbmi.nlp.noble.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.*;

import edu.pitt.dbmi.nlp.noble.terminology.Concept;
import edu.pitt.dbmi.nlp.noble.terminology.TerminologyException;
import edu.pitt.dbmi.nlp.noble.terminology.impl.NobleCoderTerminology;
import edu.pitt.dbmi.nlp.noble.tools.TextTools;

public class TerminologyLookup {

	public static void main(String[] args) throws IOException, TerminologyException {
		if(args.length > 1){
			String terminology = args[0];
			String code = args[1];
			
			// init teminology
			NobleCoderTerminology term = new NobleCoderTerminology(terminology);
			
			List<String> codeList = new ArrayList<String>();
			File file = new File(code);
			if(file.exists()){
				for(String l:TextTools.getText(new FileInputStream(file)).split("\n")){
					if(l.trim().length() > 0)
						codeList.add(l.trim());
				}
			}else{
				codeList.add(code);
			}
			
			for(String cui: codeList){
				Concept c = term.lookupConcept(cui);
				if(c != null){
					System.out.println(c.getCode()+"\t"+c.getName());
				}
			}
		}else{
			System.err.println("Usage: java "+TerminologyLookup.class.getName()+" <terminology name> <cui|cui file>");
		}

	}

}
