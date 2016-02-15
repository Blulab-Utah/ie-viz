package edu.utah.blulab.apitest;

import java.util.ArrayList;

import edu.utah.blulab.domainontology.DomainOntology;
import edu.utah.blulab.domainontology.Modifier;
import edu.utah.blulab.domainontology.Term;
import edu.utah.blulab.domainontology.Variable;

public class testAPI {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		DomainOntology domain = new DomainOntology("/Users/melissa/git/useCases/colonoscopyQuality.owl");
		//DomainOntology domain = new DomainOntology("/Users/melissa/Desktop/pneumonia.owl");
		//DomainOntology domain = new DomainOntology("/Users/melissa/Desktop/vincipneu.owl.xml");
		//DomainOntology domain = new DomainOntology("C:\\Users\\Bill\\Desktop\\carotid stenosis.owl"); 
		//DomainOntology domain = new DomainOntology("DomainOntologyAPI/src/main/resources/colonoscopy_20141001.owl");
		//DomainOntology domain = new DomainOntology("src/main/resources/colonoscopy_20141001.owl");
		//domain.getVariable("leukocytosis");
		System.out.println(domain.getVariable("KA_1004"));
		
		for(Variable v : domain.getVariable("KA_1004").getDirectParents()){
			System.out.println(v);
		}
		
		/**ArrayList<Variable> domainVariables = domain.getAllVariables();
		//ArrayList<Variable> domainVariables = domain.getAllEvents();
		System.out.println("********** Domain Variables: **********");
		for(Variable var : domainVariables){
			System.out.println(var.toString());
		}**/
		
		/**System.out.println("********** Modifier Dictionary: **********");
		ArrayList<Modifier> modifierDictionary = domain.createModifierDictionary();
		for(Modifier modifier : modifierDictionary){
			System.out.println(modifier.toString());
		}**/
		
		/**System.out.println("********** Anchor Dictionary: **********");
		ArrayList<Term> anchorDictionary = domain.createAnchorDictionary();
		for(Term term : anchorDictionary){
			System.out.println(term.toString());
		}**/
		
		/**System.out.println("********** Pseudo Dictionary: **********");
		ArrayList<Modifier> pseudoDictionary = domain.createPseudoDictionary();
		for(Modifier term : pseudoDictionary){
			System.out.println(term.toString());
		}**/
		
		/**System.out.println("********** Closure Dictionary: **********");
		ArrayList<Modifier> closureDictionary = domain.createClosureDictionary();
		for(Modifier term : closureDictionary){
			System.out.println(term.toString());
		}**/
	}

}
