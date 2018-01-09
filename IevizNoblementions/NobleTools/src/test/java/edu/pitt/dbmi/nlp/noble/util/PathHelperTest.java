package edu.pitt.dbmi.nlp.noble.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import edu.pitt.dbmi.nlp.noble.terminology.Concept;
import edu.pitt.dbmi.nlp.noble.terminology.ConceptPath;
import edu.pitt.dbmi.nlp.noble.terminology.Terminology;
import edu.pitt.dbmi.nlp.noble.terminology.impl.NobleCoderTerminology;

public class PathHelperTest {
	
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		//NobleCoderTerminology.setPersistenceDirectory(new File("/home/tseytlin/Data/Terminologies/IndexFinder"));
		Terminology terminology = new NobleCoderTerminology("NCI_Metathesaurus");
		//Terminology terminology = new NobleCoderTerminology("NCI_Thesaurus");
		PathHelper ph = new PathHelper(terminology);
		//ph.createAncestryCache(); //,"nevus","skin","margin"
		for(String text: Arrays.asList("melanoma")){
			for(Concept c: terminology.search(text)){
				long t = System.currentTimeMillis();
				// lookup paths
				/*List<ConceptPath> path = ph.getPaths(c);
				t = System.currentTimeMillis()-t;
				System.out.println(c.getName()+" ["+c+"] ("+t+" ms)  number of paths: "+path.size());
				for(ConceptPath p: path)
					System.out.println("\t"+p);*/
				
				// lookup ancestors
				/*t = System.currentTimeMillis();
				//Map<String,Integer> ancestors = ph.getAncestors(c,100);
				t = System.currentTimeMillis()-t;
				System.out.println(c.getName()+" ["+c+"] ("+t+" ms) number of ancestors: "+ancestors.size());
				//System.out.println("\t"+ph.toString(ancestors));
				System.out.println("\t"+ancestors);*/
				
				List<ConceptPath> path = ph.findPaths(c);
				t = System.currentTimeMillis()-t;
				System.out.println(c.getName()+" ["+c+"] ("+t+" ms)  number of paths: "+path.size());
				for(ConceptPath p: path)
					System.out.println("\t"+p);
				Map<Concept,Integer> ancestors = ph.findAncestors(c);
				System.out.println(c.getName()+" ["+c+"] ("+t+" ms)  number of ancestors: "+ancestors.size());
				for(Concept cc: ancestors.keySet()){
					System.out.println("\t"+cc.getName()+" ("+cc.getCode()+")\t"+ancestors.get(cc));
				}
				Concept ancestor = terminology.lookupConcept("C1511989");
				System.out.println(c.getName()+" has an ancestor "+ancestor.getName()+" ? ... "+ph.hasAncestor(c,ancestor));
			}
		}

	}
}
