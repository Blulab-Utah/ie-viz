package edu.pitt.dbmi.nlp.noble.eval;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.pitt.dbmi.nlp.noble.ontology.IInstance;
import edu.pitt.dbmi.nlp.noble.ontology.IProperty;
import edu.pitt.dbmi.nlp.noble.tools.TextTools;
import edu.pitt.dbmi.nlp.noble.util.HTMLExporter;

/**
 * Created by tseytlin on 3/25/17.
 * This class encapsulate analys
 */
public class Analysis {
    public static int MAX_ATTRIBUTE_SIZE = 10;
    public static enum ConfusionLabel {
        TP,FP,FN,TN
    }
    public static class ConfusionMatrix {
        public double TPP,TP,FP,FN,TN;
        private String label;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
        public void append(ConfusionMatrix c){
            TPP += c.TPP;
            TP += c.TP;
            FP += c.FP;
            FN += c.FN;
            TN += c.TN;
        }

        public double getPrecision(){
            return TP / (TP+ FP);
        }
        public double getRecall(){
            return  TP / (TP+ FN);
        }
        public double getFscore(){
            double precision = getPrecision();
            double recall = getRecall();
            return (2*precision*recall)/(precision + recall);
        }
        public double getAccuracy(){
            return (TP+TN) / (TP+TN+FP+FN);
        }

        public static void printHeader(PrintStream out){
            out.println(String.format("%1$-"+MAX_ATTRIBUTE_SIZE+"s","Label")+"\tTP\tTP'\tFP\tFN\tTN\tPrecis\tRecall\tAccur\tF1-Score");
        }
        public void print(PrintStream out,String label){
            out.println(String.format("%1$-"+MAX_ATTRIBUTE_SIZE+"s",label)+"\t"+
                    TextTools.toString(TP)+"\t"+TextTools.toString(TPP)+"\t"+TextTools.toString(FP)+"\t"+
                    TextTools.toString(FN)+"\t"+TextTools.toString(TN)+"\t"+
                    TextTools.toString(getPrecision())+"\t"+
                    TextTools.toString(getRecall())+"\t"+
                    TextTools.toString(getAccuracy())+"\t"+
                    TextTools.toString(getFscore()));
        }
        public String toString(){
            return "TP: "+TP+" ,FP: "+FP+", FN: "+FN;
        }

        public String getLabelFP(){
            return getLabel()+".FP";
        }
        public String getLabelFN(){
            return getLabel()+".FN";
        }
        public String getLabelTP(){
            return getLabel()+".TP";
        }

        public String getRowAsHTML(String label) {
            String fpErrors = "href=\""+ HTMLExporter.HTML_ERROR_LOCATION+"/"+getLabelFP()+".html\" target=\"_blank\"";
            String fnErrors = "href=\""+ HTMLExporter.HTML_ERROR_LOCATION+"/"+getLabelFN()+".html\" target=\"_blank\"";
            String tpErrors = "href=\""+ HTMLExporter.HTML_ERROR_LOCATION+"/"+getLabelTP()+".html\" target=\"_blank\"";
            String labelText = label;
            if(labelText.startsWith(" "))
            	labelText = "<b>"+label+"</b>";
            
            StringBuilder str = new StringBuilder();
            str.append("<tr>");
            str.append("<td>"+labelText+"</td>");
            if(TP > 0)
            	 str.append("<td><a "+tpErrors+">"+TextTools.toString(TP)+"</a></td>");
            else
            	str.append("<td>"+TextTools.toString(TP)+"</td>");
            str.append("<td>"+TextTools.toString(TPP)+"</td>");
            if(FP > 0)
                str.append("<td><a "+fpErrors+">"+TextTools.toString(FP)+"</a></td>");
            else
                str.append("<td>"+TextTools.toString(FP)+"</td>");
            if(FN > 0)
                str.append("<td><a "+fnErrors+">"+TextTools.toString(FN)+"</a></td>");
            else
                str.append("<td>"+TextTools.toString(FN)+"</td>");
            str.append("<td>"+TextTools.toString(TN)+"</td>");
            str.append("<td>"+TextTools.toString(getPrecision())+"</td>");
            str.append("<td>"+TextTools.toString(getRecall())+"</td>");
            str.append("<td>"+TextTools.toString(getAccuracy())+"</td>");
            str.append("<td>"+TextTools.toString(getFscore())+"</td>");
            str.append("</tr>");
            return str.toString();
        }
        public static String getHeaderAsHTML(){
            return "<tr><th>Label</th><th>TP</th><th>TP'</th><th>FP</th><th>FN</th><th>TN</th>" +
                    "<th>Precision</th><th>Recall</th><th>Accuracy</th><th>F1-Score</th></tr>";
        }
    }
    private String title;
    private Map<String,ConfusionMatrix> confusions;
    private Map<String,Map<String,List<IInstance>>> errorMap;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * get generated confusion matricies
     * @return map of confusion matricies
     */
    public Map<String,ConfusionMatrix> getConfusionMatricies(){
        if(confusions == null)
            confusions = new TreeMap<String, ConfusionMatrix>();
        return confusions;
    }

    /**
     * get confusion matrix for a given label, create one if not there
     * @param name - name of the confusion matrix
     * @return confusion matrix
     */
    public ConfusionMatrix getConfusionMatrix(String name) {
        ConfusionMatrix confusion = getConfusionMatricies().get(name);
        if(confusion == null){
            confusion = new ConfusionMatrix();
            confusion.setLabel(name);
            getConfusionMatricies().put(name,confusion);
            if(name.length() > MAX_ATTRIBUTE_SIZE)
                MAX_ATTRIBUTE_SIZE = name.length();
        }
        return confusion;
    }

    /**
     * get error map for a given analysis
     * @return map of labels to document to instances
     */
    public Map<String,Map<String,List<IInstance>>> getErrorMap(){
        if(errorMap == null)
            errorMap = new LinkedHashMap<String, Map<String, List<IInstance>>>();
        return  errorMap;
    }

    /**
     * get error map for a given label
     * @param label - the label
     * @return map of files to instance lists
     */
    public Map<String,List<IInstance>> getErrorMap(String label){
        Map<String,List<IInstance>> errors = getErrorMap().get(label);
        if(errors == null) {
            errors = new TreeMap<String, List<IInstance>>();
            getErrorMap().put(label,errors);
        }
        return errors;
    }

    /**
     * add error to an analysis
     * @param label - label s.a. Mention or Class name or Mention.TP
     * @param report - report where error has happend
     * @param inst - instance of an error
     */
    public void addError(String label, String report, IInstance inst){
        List<IInstance> list = getErrorMap(label).get(report);
        if(list == null){
            list = new ArrayList<IInstance>();
            getErrorMap(label).put(report,list);
        }
        list.add(inst);
    }

    /**
     * print result table to print stream
     * @param out - print stream s.a. System.out
     */
    public void printResultTable(PrintStream out){
        ConfusionMatrix.printHeader(out);
        for(String label: getConfusionMatricies().keySet()){
            getConfusionMatricies().get(label).print(out,label);
        }
    }
    
    /**
     * get result table as string
     * @return string object
     */
    public String getResultTableAsText(){
    	ByteArrayOutputStream os = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(os);
		printResultTable(ps);
		return os.toString();
    }
    
    /**
     * get result table as HTML
     * @return String representing HTML table
     */
    public String getResultTableAsHTML() {
        StringBuilder str = new StringBuilder();
        str.append("<table border=1 cellspacing=0 cellpadding=0>");
        str.append(ConfusionMatrix.getHeaderAsHTML());
        for(String label: getConfusionMatricies().keySet()){
            str.append(getConfusionMatricies().get(label).getRowAsHTML(label));
        }
        str.append("</table>");
        return str.toString();
    }

    /**
     * get error information for each label
     * @param label - label for a given error HTML
     * @return HTML description
     */
    public String getErrorsAsHTML(String label) {
        StringBuilder str = new StringBuilder();
        for(String report :getErrorMap(label).keySet()){
            String name = report;
            if(name.endsWith(".txt"))
                name = name.substring(0,name.length()-4);
            str.append("<p><h3>");
            str.append("<a href=\"../"+HTMLExporter.HTML_REPORT_LOCATION+"/"+name);
            str.append(".html\" target=\"frame\">"+report+"</a>");
            str.append("</h3><ul>");
            for(IInstance inst: getErrorMap(label).get(report)){
                str.append(toHTML(inst));
            }
            str.append("</ul></p>");
        }

        return str.toString();
    }

    /**
     * return HTML view of an instance
     * @param inst - instance
     * @return HTML string
     */
    private String toHTML(IInstance inst){
        StringBuilder str = new StringBuilder();
        str.append("<li>"+inst.getDirectTypes()[0].getName()+"<ul>");
        for(IProperty p: inst.getProperties()){
            str.append("<li>"+p.getName()+": ");
            for(Object o: inst.getPropertyValues(p)){
                if(o instanceof IInstance){
                    str.append(((IInstance)o).getDirectTypes()[0].getName()+" ");
                }else{
                    str.append(o+" ");
                }
            }
            str.append("</li>");
        }
        str.append("</ul></li>");
        return str.toString();
    }

}

