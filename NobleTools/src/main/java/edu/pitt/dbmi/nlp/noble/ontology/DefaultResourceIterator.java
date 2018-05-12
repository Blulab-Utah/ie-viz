package edu.pitt.dbmi.nlp.noble.ontology;

import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * resource iterator that wrapps regular iterator.
 *
 * @author tseytlin
 */
public class DefaultResourceIterator implements IResourceIterator {
	protected Iterator it;
	protected int offset,limit, count, total = -1;
	
	/**
	 * Instantiates a new default resource iterator.
	 *
	 * @param it the it
	 */
	public DefaultResourceIterator(Iterator it){
		this.it = it;
	}
	
	/**
	 * Instantiates a new default resource iterator.
	 *
	 * @param it the it
	 */
	public DefaultResourceIterator(Collection it){
		this.it = it.iterator();
		total = it.size();
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResourceIterator#getCount()
	 */
	public int getCount() {
		return count;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResourceIterator#getLimit()
	 */
	public int getLimit() {
		return limit;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResourceIterator#getOffset()
	 */
	public int getOffset() {
		return offset;
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public Object next() {
		count++;
		return (it.hasNext())?it.next():null;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResourceIterator#setLimit(int)
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResourceIterator#setOffset(int)
	 */
	public void setOffset(int offset) {
		this.offset = offset;
		//advance forward if offset is greater then current count
		if(offset > count){
			for(int i=count;i<offset && it.hasNext();i++,it.next());
		//if possible try to go backword
		}else if(offset < count){
			if(it instanceof ListIterator){
				ListIterator lit = (ListIterator) it;
				for(int i=count;i>=offset && lit.hasPrevious();i--,lit.previous());
			}
		}
		count = 0;
	}

	/**
	 * has next.
	 *
	 * @return true, if successful
	 */
	public boolean hasNext() {
		//TODO FIX THE MISTAKES OF THE WORLD EUGENE< YES U HAVE TO DO IT NOBODY ELSE WILL
		// Underlying it.hasNext() returns true even when it is empty, resulting in a
		//subsequent it.next call returning null
		if(limit > 0)
			return it.hasNext() && count < limit;
		return it.hasNext();
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		it.remove();
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IResourceIterator#getTotal()
	 */
	public int getTotal(){
		return total;
	}
}
