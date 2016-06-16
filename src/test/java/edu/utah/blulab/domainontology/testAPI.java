package edu.utah.blulab.domainontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.utah.blulab.domainontology.DomainOntology;
import edu.utah.blulab.domainontology.Modifier;
import edu.utah.blulab.domainontology.Variable;

public class testAPI {

	public static void main(String[] args) throws Exception {
		DomainOntology domain = new DomainOntology("/Users/melissa/git/useCases/colonoscopyQuality.owl", false);
		//DomainOntology domain = new DomainOntology("/Users/mtharp/use_cases/DomainOntologies/pneumonia.owl");
		//DomainOntology domain = new DomainOntology("/Users/mtharp/Desktop/vincipneu.owl.xml");
		//DomainOntology domain = new DomainOntology("C:\\Users\\Bill\\Desktop\\carotid stenosis.owl"); 
		//DomainOntology domain = new DomainOntology("DomainOntologyAPI/src/main/resources/colonoscopy_20141001.owl");
		//DomainOntology domain = new DomainOntology("src/main/resources/colonoscopy_20141001.owl");
		//domain.getVariable("leukocytosis");
		//domain.getVariable("KA247");
		
		ArrayList<Variable> domainVariables = domain.getAllVariables();
		System.out.println("********** Domain Variables: **********");
		for(Variable var : domainVariables){
			System.out.println(var.toString());
		}
		
		/**System.out.println("********** Modifier Dictionary: **********");
		ArrayList<Modifier> modifierDictionary = domain.createModifierDictionary();
		for(Modifier modifier : modifierDictionary){
			System.out.println(modifier.toString());
		}**/

		System.out.println("********** Modifier Map: **********");
		HashMap<String, ArrayList<Modifier>> modifierMap = domain.createModifierMap();
		Iterator iterator = modifierMap.entrySet().iterator();
		while (iterator.hasNext()){
			Map.Entry<String, ArrayList<Modifier>> modifierEntry =
					(Map.Entry<String, ArrayList<Modifier>>)iterator.next();
			System.out.print(modifierEntry.getKey() + ":\t");
			for(Modifier modifier : modifierEntry.getValue()){
				System.out.print(modifier.getModName() + "  ");
			}
			System.out.println("");
		}
		
		/**System.out.println("********** Target Dictionary: **********");
		ArrayList<Term> targetDictionary = domain.createAnchorDictionary();
		for(Term target : targetDictionary){
			System.out.println(target.toString());
		}**/
	}

}
