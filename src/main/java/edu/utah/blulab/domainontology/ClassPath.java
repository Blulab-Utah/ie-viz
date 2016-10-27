package edu.utah.blulab.domainontology;

import java.util.ArrayList;
import org.semanticweb.owlapi.model.OWLClass;
import java.util.Collection;

import javax.swing.tree.TreePath;


/**
 * Created by melissa on 10/27/16.
 */
public class ClassPath extends ArrayList<OWLClass> {
    public ClassPath(){
        super();
    }

    public ClassPath(Collection<OWLClass> c){
        super(c);
    }

    public TreePath toTreePath(){
        return new TreePath(toArray(new OWLClass[0]));
    }

    public String toString(){
        String s = super.toString();
        s = s.substring(1,s.length()-1);
        return s.replaceAll(","," ->");
    }
}
