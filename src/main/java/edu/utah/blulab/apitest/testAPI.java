package edu.utah.blulab.apitest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.utah.blulab.domainontology.*;

public class testAPI {

	public static void main(String[] args) throws Exception {
		//DomainOntology domain = new DomainOntology("/Users/melissa/git/useCases/98_heartFailure.owl", false);
		boolean bool = false;
		if(args[1].equalsIgnoreCase("true")){
			bool = true;
		}
		DomainOntology domain = new DomainOntology(args[0], bool);
		
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
		
		System.out.println("********** Anchor Dictionary: **********");
		ArrayList<Term> anchorDictionary = domain.createAnchorDictionary();
		for(Term term : anchorDictionary){
			System.out.println(term.toString());
		}
		
		System.out.println("********** Pseudo Dictionary: **********");
		ArrayList<Modifier> pseudoDictionary = domain.createPseudoDictionary();
		for(Modifier term : pseudoDictionary){
			System.out.println(term.toString());
		}
		
		System.out.println("********** Closure Dictionary: **********");
		ArrayList<Modifier> closureDictionary = domain.createClosureDictionary();
		for(Modifier term : closureDictionary){
			System.out.println(term.toString());
		}

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
	}



}
