package edu.pitt.dbmi.nlp.noble.util;

import edu.pitt.dbmi.nlp.noble.ontology.*;
import edu.pitt.dbmi.nlp.noble.ontology.owl.OOntology;
import edu.pitt.dbmi.nlp.noble.terminology.TerminologyException;
import edu.pitt.dbmi.nlp.noble.tools.TermFilter;

import java.io.IOException;
import java.util.*;

public class OntologySynonymyCleaner {

	public static void main(String[] args) throws IOntologyException, IOException, TerminologyException {
		if(args.length == 0){
			System.out.println("Usage: java OntologySynonymyCleaner <file or link to ontology>");
			System.exit(1);
		}
		String ontology = args[0];
		System.out.println("loading ..");
		IOntology ont = OOntology.loadOntology(ontology);
		List<IProperty> props = Arrays.asList(ont.getProperty("synonym"),ont.getProperty("preferredTerm"));
		for(IClass cls: ont.getRoot().getSubClasses()){
			for(IProperty prop : props){
				cleanSynonymy(cls,prop);
				for(IInstance inst: cls.getDirectInstances()){
					cleanSynonymy(inst,prop);
				}
			}
		}
		System.out.println("saving ..");
		ont.save();
	}
	private static Set<String> toSet(Object [] synonyms){
		Set<String> list = new LinkedHashSet<String>();
		for(Object o: synonyms){
			list.add(o.toString());
		}
		return list;
	}

	private static void cleanSynonymy(IResource cls,IProperty prop) {
		Set<String> synonyms = toSet(cls.getPropertyValues(prop));
		if(!synonyms.isEmpty()){
			Set<String> newsym = TermFilter.filter(synonyms);
			if(!equals(newsym,synonyms)){
				System.out.println(cls.getName()+"\t"+synonyms.size()+" to "+newsym.size()+"\t"+getDelta(synonyms,newsym));
				cls.setPropertyValues(prop, newsym.toArray());
			}
		}
	}
	
	private static boolean equals(Collection<String> a, Collection<String> b){
		if(a.size() != b.size())
			return false;
		for(String c: a){
			if(!b.contains(c))
				return false;
		}
		return true;
	}
	
	private static String getDelta(Set<String> synonyms, Set<String> newsym) {
		StringBuffer list = new StringBuffer();
		for(String s: synonyms){
			if(!newsym.contains(s))
				list.append(s+"; ");
		}
		return list.toString();
	}
	
}
