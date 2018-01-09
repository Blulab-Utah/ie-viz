package edu.pitt.dbmi.nlp.noble.ontology;

import java.io.File;

import edu.pitt.dbmi.nlp.noble.ontology.owl.OOntology;

public class OntologyTest {

	public static void main(String[] args) throws IOntologyException {
		IOntology ont = OOntology.loadOntology(new File("/home/tseytlin/.noble/ontologies/socialRisk.owl"));
		IProperty hasAnchor = ont.getProperty("hasAnchor");
		IProperty isAnchorOf = ont.getProperty("isAnchorOf");
		for(IClass annotation : ont.getClass("Annotation").getSubClasses()){
			for(IRestriction r: annotation.getRestrictions(hasAnchor)){
				for(Object o: r.getParameter()){
					if(o instanceof IClass){
						IClass c = (IClass) o;
						IRestriction rr = ont.createRestriction(IRestriction.SOME_VALUES_FROM);
						rr.setProperty(isAnchorOf);
						rr.setParameter(annotation.getLogicExpression());
						
						c.addNecessaryRestriction(rr);
						
						System.out.println("adding "+rr+" to "+c);
					}
				}
			}
		}
		ont.save();

	}

}
