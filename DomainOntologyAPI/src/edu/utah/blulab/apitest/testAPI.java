package edu.utah.blulab.apitest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.utah.blulab.domainontology.DomainOntology;
import edu.utah.blulab.domainontology.Modifier;
import edu.utah.blulab.domainontology.Term;
import edu.utah.blulab.domainontology.Variable;

public class testAPI {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		//DomainOntology domain = new DomainOntology("/Users/mtharp/use_cases/DomainOntologies/pneumonia.owl");
		DomainOntology domain = new DomainOntology("/Users/mtharp/Desktop/vincipneu.owl.xml");
		
		//domain.getVariable("leukocytosis");
		Variable leukocytosis = domain.getVariable("KA247");
		System.out.println(leukocytosis.toString());
		
		Variable gasExchange = domain.getVariable("KA253");
		System.out.println(gasExchange.toString());
		
		ArrayList<Term> concepts = domain.createConceptDictionary();
		System.out.println("****** CONCEPT DICTIONARY ******");
		for(Term concept: concepts){
			System.out.println(concept.toString());
		}
		
		ArrayList<Modifier> modifiers = domain.createModifierDictionary();
		/**System.out.println("****** MODIFIER DICTIONARY ******");
		
		
		ArrayList<Modifier> closures = domain.createClosureDictionary();
		System.out.println("****** CLOSURES DICTIONARY ******");
		if(closures != null){
			for(Modifier closure: closures){
				System.out.println(closure.toString());
			}
		}**/
	}

}
