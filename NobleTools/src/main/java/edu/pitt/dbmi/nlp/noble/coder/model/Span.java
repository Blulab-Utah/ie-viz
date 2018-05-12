package edu.pitt.dbmi.nlp.noble.coder.model;

import edu.pitt.dbmi.nlp.noble.tools.TextTools;

import java.util.List;

/**
 * Created by tseytlin on 3/30/17.
 */
public class Span implements  Spannable , Comparable<Spannable> {
    private int start,end;
    private String text;

    public Span(int st, int en){
        start = st;
        end = en;
    }

    /**
     * get span from a string that has integers range defined
     * @param string - string representation of a span ex. 101:120
     * @return Span object
     */
    public static Span getSpan(String string){
        List<Integer> nums = TextTools.parseIntegerValues(string);
        if(nums.size() > 1){
            return new Span(nums.get(0),nums.get(1));
        }
        return null;
    }
    /**
     * get span from a string that has integers range defined
     * @param start - start offset
     * @param end - end offset
     * @return Span object
     */
    public static Span getSpan(String start, String end){
        return new Span(Integer.parseInt(start),Integer.parseInt(end));
     }

    public String getText() {
        return text;
    }

    public void setText(String text){
        this.text = text;
    }

    public int start(){
        return start;
    }

    public int end(){
        return end;
    }

    public int getStartPosition() {
        return start;
    }

    public int getEndPosition() {
        return end;
    }


    public int getLength() {
        return getEndPosition()-getStartPosition();
    }
    /* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.coder.model.Spannable#contains(edu.pitt.dbmi.nlp.noble.coder.model.Spannable)
	 */
    public boolean contains(Spannable s) {
        return getStartPosition() <= s.getStartPosition() && s.getEndPosition() <= getEndPosition();
    }

    /* (non-Javadoc)
     * @see edu.pitt.dbmi.nlp.noble.coder.model.Spannable#intersects(edu.pitt.dbmi.nlp.noble.coder.model.Spannable)
     */
    public boolean intersects(Spannable s) {
        //NOT this region ends before this starts or other region ends before this one starts
        return !(getEndPosition() < s.getStartPosition() || s.getEndPosition() < getStartPosition());
    }

    /**
     * same thing as intersects, just a different name
     * @param s - other span
     * @return true or false
     */
    public boolean overlaps(Spannable s){
        return intersects(s);
    }

    /**
     * if two spans overlap, how much?
     * @param s - other span
     * @return number of overlapping characters
     */
    public int getOverlapLength(Spannable s){
        if(overlaps(s)){
            return Math.min(end,s.getEndPosition()) - Math.max(start,s.getStartPosition());
        }
        return 0;
    }

    /* (non-Javadoc)
     * @see edu.pitt.dbmi.nlp.noble.coder.model.Spannable#before(edu.pitt.dbmi.nlp.noble.coder.model.Spannable)
     */
    public boolean before(Spannable s) {
        return getEndPosition() <= s.getStartPosition();
    }

    /* (non-Javadoc)
     * @see edu.pitt.dbmi.nlp.noble.coder.model.Spannable#after(edu.pitt.dbmi.nlp.noble.coder.model.Spannable)
     */
    public boolean after(Spannable s) {
        return s.getEndPosition() <= getStartPosition();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString(){
        return start+":"+end;
    }

    public int compareTo(Spannable o) {
        if(getStartPosition() != o.getStartPosition())
            return getStartPosition() - o.getStartPosition();
        return getEndPosition() - o.getEndPosition();
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public boolean equals(Object obj) {
        if(obj instanceof  Spannable){
            Spannable s = (Spannable) obj;
            return getStartPosition() == s.getStartPosition() && getEndPosition() == s.getEndPosition();
        }
        return super.equals(obj);
    }
}
