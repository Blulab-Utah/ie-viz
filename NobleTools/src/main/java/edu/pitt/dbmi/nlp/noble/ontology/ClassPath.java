package edu.pitt.dbmi.nlp.noble.ontology;

import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Collection;

/**
 * describes a class path.
 *
 * @author tseytlin
 */
public class ClassPath extends ArrayList<IClass>{
	
	/**
	 * Instantiates a new class path.
	 */
	public ClassPath(){
		super();
	}
	
	/**
	 * Instantiates a new class path.
	 *
	 * @param c the c
	 */
	public ClassPath(Collection<IClass> c){
		super(c);
	}
	
	/**
	 * To tree path.
	 *
	 * @return the tree path
	 */
	public TreePath toTreePath(){
		return new TreePath(toArray(new IClass [0]));
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
