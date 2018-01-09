package edu.pitt.dbmi.nlp.noble.coder.model;

/**
 * An interface that marks a span of text.
 *
 * @author tseytlin
 */
public interface Spannable {
	
	/**
	 * text covered by this span.
	 *
	 * @return the text
	 */
	public String getText();
	
	/**
	 * offset of this span from the start of the document.
	 *
	 * @return the start position
	 */
	public int getStartPosition();
	
	/**
	 * end offset of this span.
	 *
	 * @return the end position
	 */
	public int getEndPosition();
	
	
	/**
	 * is this spannable region contains another.
	 *
	 * @param s the s
	 * @return true, if successful
	 */
	public boolean contains(Spannable s);
	
	
	/**
	 * is this spannable region intersects another.
	 *
	 * @param s the s
	 * @return true, if successful
	 */
	public boolean intersects(Spannable s);

	/**
	 * is this spannable region before another.
	 *
	 * @param s the s
	 * @return true, if successful
	 */
	public boolean before(Spannable s);


	/**
	 * is this spannable region before another.
	 *
	 * @param s the s
	 * @return true, if successful
	 */
	public boolean after(Spannable s);


	/**
	 * Gets the length.
	 *
	 * @return the length
	 */
	public int getLength();
}
