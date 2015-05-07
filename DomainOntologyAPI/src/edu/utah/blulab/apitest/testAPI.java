package edu.utah.blulab.apitest;

import edu.utah.blulab.domainontology.DomainOntology;

public class testAPI {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		//DomainOntology domain = new DomainOntology("/Users/mtharp/use_cases/DomainOntologies/pneumonia.owl");
		DomainOntology domain = new DomainOntology("/Users/mtharp/Desktop/vincipneu.owl.xml");
		
		//domain.getVariable("leukocytosis");
		domain.getVariable("KA247");
		
		
	}

}
