package edu.utah.blulab.domainontology;

import java.util.ArrayList;

import edu.utah.blulab.domainontology.DomainOntology;
import edu.utah.blulab.domainontology.Modifier;
import edu.utah.blulab.domainontology.Variable;

public class testAPI {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
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
		
		System.out.println("********** Modifier Dictionary: **********");
		ArrayList<Modifier> modifierDictionary = domain.createModifierDictionary();
		for(Modifier modifier : modifierDictionary){
			System.out.println(modifier.toString());
		}
		
		System.out.println("********** Target Dictionary: **********");
		ArrayList<Term> targetDictionary = domain.createAnchorDictionary();
		for(Term target : targetDictionary){
			System.out.println(target.toString());
		}
	}

}
