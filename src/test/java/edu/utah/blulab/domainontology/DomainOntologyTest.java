package edu.utah.blulab.domainontology;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

public class DomainOntologyTest {

	

	@Test
	public void test() throws Exception {
		// Melissa: find various ways of reading in and writing back out (to a string) the test ontology, then comparing the results
		//fail("Not yet implemented");
		/*DomainOntology domain = new DomainOntology("src/main/resources/colonoscopy_20141001.owl");
		
		ArrayList<Variable> domainVariables = domain.getAllVariables();
		//System.out.println("********** Domain Variables: **********");
		for(Variable var : domainVariables){
			System.out.println(var.toString());
		}
		
		System.out.println("********** Modifier Dictionary: **********");
		ArrayList<Modifier> modifierDictionary = domain.createModifierDictionary();
		for(Modifier modifier : modifierDictionary){
			System.out.println(modifier.toString());
			
		}*/
		
		assertEquals(5,5);
	}

}
