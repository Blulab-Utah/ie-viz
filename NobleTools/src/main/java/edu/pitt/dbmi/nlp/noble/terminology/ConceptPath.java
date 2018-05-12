package edu.pitt.dbmi.nlp.noble.terminology;

import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * describes a class path.
 *
 * @author tseytlin
 */
public class ConceptPath extends ArrayList<Concept>{
	
	/**
	 * Instantiates a new concept path.
	 */
	public ConceptPath(){
		super();
	}
	
	/**
	 * Instantiates a new concept path.
	 *
	 * @param c the c
	 */
	public ConceptPath(Collection<Concept> c){
		super(c);
	}
	
	/**
	 * Instantiates a new concept path.
	 *
	 * @param c the c
	 */
	public ConceptPath(Concept c){
		super(Collections.singletonList(c));
	}
	
	/**
	 * To tree path.
	 *
	 * @return the tree path
	 */
	public TreePath toTreePath(){
		return new TreePath(toArray(new Concept [0]));
	}
	
	/* (non-Javadoc)
	 * @see java.util.AbstractCollection#toString()
	 */
	public String toString(){
		String s = super.toString();
		s = s.substring(1,s.length()-1);
		return s.replaceAll(","," ->");
	}
}
