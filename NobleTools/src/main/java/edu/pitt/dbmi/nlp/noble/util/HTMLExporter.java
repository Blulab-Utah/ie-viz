package edu.pitt.dbmi.nlp.noble.util;

import edu.pitt.dbmi.nlp.noble.coder.NobleCoder;
import edu.pitt.dbmi.nlp.noble.coder.model.*;
import edu.pitt.dbmi.nlp.noble.eval.Analysis;
import edu.pitt.dbmi.nlp.noble.eval.AnnotationEvaluation;
import edu.pitt.dbmi.nlp.noble.extract.InformationExtractor;
import edu.pitt.dbmi.nlp.noble.extract.model.ItemInstance;
import edu.pitt.dbmi.nlp.noble.extract.model.Template;
import edu.pitt.dbmi.nlp.noble.extract.model.TemplateDocument;
import edu.pitt.dbmi.nlp.noble.extract.model.TemplateItem;
import edu.pitt.dbmi.nlp.noble.mentions.model.AnnotationVariable;
import edu.pitt.dbmi.nlp.noble.mentions.model.Composition;
import edu.pitt.dbmi.nlp.noble.mentions.model.DomainOntology;
import edu.pitt.dbmi.nlp.noble.mentions.model.Instance;
import edu.pitt.dbmi.nlp.noble.ontology.IInstance;
import edu.pitt.dbmi.nlp.noble.ontology.IOntology;
import edu.pitt.dbmi.nlp.noble.ontology.IProperty;
import edu.pitt.dbmi.nlp.noble.terminology.Annotation;
import edu.pitt.dbmi.nlp.noble.terminology.Concept;
import edu.pitt.dbmi.nlp.noble.tools.ConText;
import edu.pitt.dbmi.nlp.noble.tools.TextTools;

import java.io.*;
import java.util.*;

/**
 * The Class HTMLExporter.
 */
public class HTMLExporter {
	public static final String TERM_SERVLET = "http://slidetutor.upmc.edu/term/servlet/TerminologyServlet";
	public static final String HTML_REPORT_LOCATION = "reports";
	public static final String HTML_ERROR_LOCATION = "errors";
	public static final String HTML_EVAL_LOCATION = "evaluation";
	private String title = "";
	private File outputDirectory;
	private String resultFileName;
	private BufferedWriter htmlIndexWriter;
	private boolean createIndex,showFooter = true,showReport = true,showConceptList = true;
	private String terminologySerlvet;
	
	
	
	/**
	 * create HTML Exporter that writes out a set of HTML files
	 * to a given output directory.
	 *
	 * @param outputDirectory the output directory
	 */
	public HTMLExporter(File outputDirectory){
		setOutputDirectory(outputDirectory);
		resultFileName = CSVExporter.DEFAULT_RESULT_FILE;
		createIndex = true;
		terminologySerlvet = TERM_SERVLET;
	}
	
	/**
	 * create HTML Exporter that writes out a set of HTML files
	 * to a given output directory.
	 */
	public HTMLExporter(){
		resultFileName = CSVExporter.DEFAULT_RESULT_FILE;
		createIndex = false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		super.finalize();
		flush();
	}

	/**
	 * Checks if is show footer.
	 *
	 * @return true, if is show footer
	 */
	public boolean isShowFooter() {
		return showFooter;
	}

	/**
	 * Sets the show footer.
	 *
	 * @param showFooter the new show footer
	 */
	public void setShowFooter(boolean showFooter) {
		this.showFooter = showFooter;
	}


	/**
	 * Checks if is show report.
	 *
	 * @return true, if is show report
	 */
	public boolean isShowReport() {
		return showReport;
	}

	/**
	 * Sets the show report.
	 *
	 * @param showReport the new show report
	 */
	public void setShowReport(boolean showReport) {
		this.showReport = showReport;
	}

	/**
	 * Checks if is show concept list.
	 *
	 * @return true, if is show concept list
	 */
	public boolean isShowConceptList() {
		return showConceptList;
	}

	/**
	 * Sets the show concept list.
	 *
	 * @param showConceptList the new show concept list
	 */
	public void setShowConceptList(boolean showConceptList) {
		this.showConceptList = showConceptList;
	}

	/**
	 * Gets the terminology serlvet.
	 *
	 * @return the terminology serlvet
	 */
	public String getTerminologySerlvet() {
		return terminologySerlvet;
	}

	/**
	 * Sets the terminology serlvet.
	 *
	 * @param terminologySerlvet the new terminology serlvet
	 */
	public void setTerminologySerlvet(String terminologySerlvet) {
		this.terminologySerlvet = terminologySerlvet;
	}

	/**
	 * get the result file name that is being used.
	 *
	 * @return the result file name
	 */
	
	public String getResultFileName() {
		return resultFileName;
	}


	/**
	 * sget the result file name that is being used.
	 *
	 * @param resultFileName the new result file name
	 */
	public void setResultFileName(String resultFileName) {
		this.resultFileName = resultFileName;
	}


	/**
	 * should HTML index file be created.
	 *
	 * @return true, if is creates the index
	 */
	public boolean isCreateIndex() {
		return createIndex;
	}


	/**
	 * cretae an HTML index file.
	 *
	 * @param createIndex the new creates the index
	 */
	public void setCreateIndex(boolean createIndex) {
		this.createIndex = createIndex;
	}


	/**
	 * get output directory.
	 *
	 * @return the output directory
	 */

	public File getOutputDirectory() {
		return outputDirectory;
	}

	/**
	 * set output directory.
	 *
	 * @param outputDirectory the new output directory
	 */
	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
		File reports = new File(this.outputDirectory,HTML_REPORT_LOCATION);
		if(!reports.isDirectory())
			reports.mkdirs();
	}


	/**
	 * create pretty CAP template.
	 *
	 * @param doc the doc
	 * @return the string
	 */
	private String createTemplate(TemplateDocument doc){
		StringBuilder cap = new StringBuilder();
		
		for(Template template: doc.getItemInstances().keySet()){
			int num = 1;
			cap.append("<h3>"+template.getName()+"</h3>");
			cap.append("<table border=0 cellspacing=0 cellpadding=2>");
			for(TemplateItem temp: template.getTemplateItems()){
				List<ItemInstance> items = doc.getItemInstances(temp);
				String name = (items.isEmpty())?temp.getName():codeTemplateItem(items.get(0));
				Map<String,Collection<ItemInstance>> names = new LinkedHashMap<String,Collection<ItemInstance>>();
				if(temp.getAttributeValues().isEmpty()){
					names.put(name,items);
				}else{
					for(TemplateItem attr : temp.getAttributes()){
						Set<ItemInstance> list = new LinkedHashSet<ItemInstance>();
						for(ItemInstance item: items){
							for(ItemInstance a: item.getAttributes()){
								if(a.getTemplateItem().equals(attr)){
									list.addAll(item.getAttributeValues(a));
								}
							}
						}
						names.put(name+" "+attr.getName(),list);
					}
				}	
				for(String nm: names.keySet()){
					cap.append("<tr><td> <font color=\"#E0E0E0 \">"+(num++)+"</font> </td><th align=left> "+nm+" </th><td align=left style=\"padding-left:20px;\">");
					String br = "";
					for(ItemInstance item: names.get(nm)){
						cap.append(br+codeConcept(item));
						br="<br>";
					}
				}
				cap.append("</td></tr>");
			}
			cap.append("</table>");
		}
		return cap.toString();
	}
	

	/**
	 * code label.
	 *
	 * @param l the l
	 * @param mentions the mentions
	 * @return the string
	 */
	private String codeLabel(Annotation l, Set<Mention> mentions){
		String lid = ""+l.getOffset();
		String word = l.getText().replaceAll("\n","<br>");
		List<String> codes = new ArrayList<String>();
		StringBuilder tip = new StringBuilder();
		String color = "green";
		for(Mention m: mentions){
			Concept c = m.getConcept();
			String p = (m.isNegated())?"N":(m.isHedged()?"U":"");
			codes.add("'"+p+m.getConcept().getCode()+"'");
			tip.append(c.getName()+" ("+c.getCode()+")\n  "+Arrays.toString(c.getSemanticTypes())+"\n");
			
			// add modifiers
			tip.append(getModifiers(m));

			// add span
			tip.append("  Span: "+l.getStartPosition()+":"+l.getEndPosition());
			
			if("green".equals(color)) {
				if (isModifiers(m)) {
					color = "#FF8C00";
				}/* else if (!isDefaultLinguisticModifiers(m)) {
					color = "#556B2F"; //"#994d00";
				}*/
			}
		}
		return "<label id=\""+lid+"\" style=\"color:"+color+";\" onmouseover=\"h("+codes+");\" onmouseout=\"u("+codes+
				");\" title=\""+TextTools.escapeHTML(tip.toString())+"\">"+word+"</label>";
	}
	
	/**
	 * Checks if is default linguistic modifiers.
	 *
	 * @param m the m
	 * @return true, if is default modifiers
	 */
	private boolean isDefaultLinguisticModifiers(Mention m) {
		for(String type: Mention.getLinguisticModifierTypes()){
			for(Modifier mod: m.getModifiers(type)){
				if(!mod.isDefaultValue()){
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Checks if is default linguistic modifiers.
	 *
	 * @param m the m
	 * @return true, if is default modifiers
	 */
	private boolean isModifiers(Mention m) {
		return ConText.isTypeOf(m,ConText.MODIFIER);
	}

	/**
	 * Gets the modifiers.
	 *
	 * @param m the m
	 * @return the modifiers
	 */
	private String getModifiers(Mention m){
		StringBuilder st = new StringBuilder();
		for(String type: Arrays.asList(
				ConText.MODIFIER_TYPE_CERTAINTY,	ConText.MODIFIER_TYPE_POLARITY,
				ConText.MODIFIER_TYPE_EXPERIENCER,ConText.MODIFIER_TYPE_TEMPORALITY)){
			if(m.getModifierValue(type) != null)
				st.append("  "+type+" : "+m.getModifierValue(type)+"\n");
		}
		
		return st.toString();
	}
	
	
	/**
	 * code individual concept.
	 *
	 * @param c the c
	 * @param color the color
	 * @param aa the aa
	 * @return the string
	 */
	private String codeConcept(Concept c, String color,List<Annotation> aa){
		String p = "";
		List<String> ids = new ArrayList<String>();
		for(Annotation a: aa){
			ids.add("'"+a.getOffset()+"'");
		}
		String code = c.getCode();
		StringBuilder sy = new StringBuilder("\nterms:  ");
		for(String s: c.getSynonyms())
			sy.append(s+"; ");
		String tip = c.getCode()+" "+Arrays.toString(c.getSemanticTypes())+"\n"+c.getDefinition()+sy;
		String term = c.getTerminology().getName();
		StringBuilder out = new StringBuilder();
		out.append("<a style=\"color:"+color+";\" onmouseover=\"h("+ids+");t=setTimeout(function(){j("+ids+");},2000);\" ");
		out.append(	"onmouseout=\"u("+ids+"); clearTimeout(t);\" id=\""+p+code+"\"");
		out.append(" href=\""+terminologySerlvet+"?action=lookup_concept&term="+term+"&code="+code);
		out.append("\" target=\"_blank\" title=\""+TextTools.escapeHTML(tip)+"\">"+TextTools.escapeHTML(c.getName())+"</a> &nbsp; ");
		return out.toString();
	}
	
	
	/**
	 * code individual concept.
	 *
	 * @param c the c
	 * @param color the color
	 * @param aa the aa
	 * @return the string
	 */
	private String codeVariable(AnnotationVariable var){
		String color = "blue";
		String code = var.getConceptClass().getName();
		String tip = var.getDefinedConstraints();
		
		StringBuilder out = new StringBuilder();
		
		out.append("<table><tr><th colspan=3 align=left>");
		out.append(codeEntity(var.getLabel(),code, tip, color,var.getAnnotations()));
		out.append("</th></tr>");
	
		// add text and span
		out.append("<tr><td>&nbsp;</td><td><span style=\" color:black;\">"+DomainOntology.HAS_ANNOTATION_TEXT+"</span></td><td>"+var.getText()+"</td></tr>");
		out.append("<tr><td>&nbsp;</td><td><span style=\" color:black;\">"+DomainOntology.HAS_SPAN+"</span></td><td>"+var.getInstanceSpan()+"</td></tr>");
	
		Map<String,Set<Instance>> modifiers = var.getModifierInstances();
		for(String prop: modifiers.keySet()){
			String pc = "black";
			Set<Instance> instances = modifiers.get(prop);
			// don't include components for an anchor
			boolean includeComponents = !DomainOntology.HAS_ANCHOR.equals(prop);
			// if we have a modifier that is annatation variable, then don't display components as well
			for(Instance i: instances){
				if(i instanceof AnnotationVariable)
					includeComponents = false;
				if(i.isReasonForFail())
					pc = "red";
			}
			String val =  codeEntities(instances,includeComponents);
			out.append("<tr><td>&nbsp;</td><td><span style=\" color:"+pc+";\">"+prop+"</span>");
			out.append("</td><td>"+val+"</td></tr>");
		}
		
		// find properties that are missing
		for(String prop: var.findMissingDefinedProperties()){
			out.append("<tr><td>&nbsp;</td><td><span style=\" color:red;\">"+prop+"</span>");
			out.append("</td><td> not found </td></tr>");
		}

		out.append("</table>");
		
		return out.toString();
	}
	
	/**
	 * code multiple entities
	 * @param instances
	 * @return
	 */
	private String codeEntities(Collection<Instance> instances){
		return codeEntities(instances,true);
	}
	
	/**
	 * code multiple entities
	 * @param instances
	 * @return
	 */
	private String codeEntities(Collection<Instance> instances,boolean includeComponents){
		StringBuilder out = new StringBuilder();
		for(Instance inst: instances){
			String color = inst.getAnnotations().isEmpty()?"black":"blue";
			out.append(codeEntity(inst.getLabel(),inst.getName(),"",color,inst.getAnnotations()));
			if(includeComponents){
				String props = codeEntities(inst.getModifierInstanceList());
				if(props.length() > 0)
					out.append(" ("+props+")");
			}
			out.append(", ");
		}
		// remove the last comma and space
		if(out.length() > 2)
			out.replace(out.length()-2, out.length(), "");
		return out.toString();
	}
	
	
	/**
	 * create a coded entity
	 * @param label
	 * @param code
	 * @param tip
	 * @param color
	 * @param annnotations
	 * @return
	 */
	private String codeEntity(String label, String code, String tip, String color, Collection<Annotation> annnotations){
		List<String> ids = new ArrayList<String>();
		for(Annotation a: annnotations){
			ids.add("'"+a.getOffset()+"'");
		}
		return codeEntity(label,code,tip, color,ids);
	}
	
	/**
	 * create a coded entity
	 * @param label
	 * @param code
	 * @param tip
	 * @param color
	 * @param annnotations
	 * @return
	 */
	private String codeEntity(String label, String id, String tip, String color,List<String> associatedIds){
		StringBuilder out = new StringBuilder();
		out.append("<span style=\"color:"+color+";\" onmouseover=\"h("+associatedIds+");t=setTimeout(function(){j("+associatedIds+");},2000);\" ");
		out.append(	"onmouseout=\"u("+associatedIds+"); clearTimeout(t);\" id=\""+id+"\"");
		out.append(" title=\""+TextTools.escapeHTML(tip)+"\">"+TextTools.escapeHTML(label)+"</span> ");
		return out.toString();
	}
	
	
	/**
	 * group annotations in a sentence.
	 *
	 * @param s the s
	 * @return the map
	 */
	private Map<Annotation,Set<Mention>> groupAnnotations(Sentence s) {
		Map<Annotation,Set<Mention>> map = new TreeMap<Annotation, Set<Mention>>();
		for(Mention m: s.getMentions()){
			// this takes care of main mentions
			for(Annotation a: m.getAnnotations()){
				addAnnotation(a, m, s, map);
			}
			// what about modifiers for each mention?
			for(Modifier mmm: m.getModifiers()){
				for(Annotation a: mmm.getAnnotations()){
					addAnnotation(a, mmm.getMention(), s, map);
				}
				for(Annotation a: mmm.getQualifierAnnotations()){
					addAnnotation(a, mmm.getMention(), s, map);
				}
			}
			
		}
		return map;
	}

	/**
	 * add annotation to a map
	 * @param a - annotation
	 * @param m - mention 
	 * @param s - sentence
	 * @param map - map to put it in
	 */
	private void addAnnotation(Annotation a,  Mention m, Sentence s, Map<Annotation,Set<Mention>> map){
		if(s.contains(a) && !intersects(a,map.keySet())){
			Set<Mention> mm = map.get(a);
			if(mm == null){
				mm = new LinkedHashSet<Mention>();
				map.put(a,mm);
			}
			mm.add(m);
		}
	}
	
	
	/**
	 * Intersects.
	 *
	 * @param an the an
	 * @param aa the aa
	 * @return true, if successful
	 */
	private boolean intersects(Annotation an, Set<Annotation> aa) {
		for(Annotation a: aa){
			if(!a.equals(an) && (a.contains(an) || an.contains(a)))
				return true;
		}
		return false;
	}

	/**
	 * create an HTML representation of Sentence.
	 *
	 * @param s the s
	 * @return the string
	 */
	private String codeSentence(Sentence s) {
		StringBuilder str = new StringBuilder();
		if(Sentence.TYPE_HEADER.equals(s.getSentenceType())){
			String sid = "";
			// if this is a section header, see if we have a mention
			Section sec = s.getSection();
			if(sec != null && sec.getTitleOffset() == s.getOffset() && sec.getHeader() != null){
				sid = " id="+sec.getHeader().getStartPosition()+" ";
			}
			str.append("<b"+sid+">"+s.getText()+"</b><br>");
		}else{
			int offs = 0;
			String content = s.getText();
			Map<Annotation,Set<Mention>> annotations = groupAnnotations(s);
			for(Annotation l : annotations.keySet()){
				try{
					int o = l.getOffset()-s.getOffset();
					str.append(content.substring(offs,o).replaceAll("\n", "<br>"));
					str.append(codeLabel(l,annotations.get(l)));
					offs = o+l.getLength();
				}catch(StringIndexOutOfBoundsException ex){
					//THIS SHIT HAPPENS, IT IS OK
					/*System.err.print("Error: "+ex.getMessage()+"\t");
					System.err.print("Sentence:\t"+content.trim()+"/"+s.getOffset()+"\t");
					System.err.println("Label:\t"+l);*/
					//ex.printStackTrace();
				}
			}
			str.append(content.substring(offs).replaceAll("\n", "<br>"));
		}
		return str.toString();
	}

	/**
	 * code a list of mentions of a given modality.
	 *
	 * @param mentions the mentions
	 * @return the string
	 */
	
	private String codeMentions(List<Mention> mentions){
		Map<Concept,List<Annotation>> map = new TreeMap<Concept, List<Annotation>>(new Comparator<Concept>() {
			public int compare(Concept o1, Concept o2) {
				int x = o1.compareTo(o2);
				return (x == 0)?o1.getCode().compareTo(o2.getCode()):x;
			}
		});
		StringBuilder str = new StringBuilder();
		// load and sort mentions
		for(Mention m: mentions){
			List<Annotation> list = map.get(m.getConcept());
			if(list == null){
				list = new ArrayList<Annotation>();
				map.put(m.getConcept(),list);
			}
			list.addAll(m.getAnnotations());
			
		}
		// now create a list of concepts
		boolean alt = true;
		for(Concept c: map.keySet()){
			String color = (alt)?"blue":"black";
			alt ^= true;
			str.append(codeConcept(c,color,map.get(c)));
		}
		return (str.length() > 0)?"<p><b>Concepts</b><br>"+str+"</p>":"";
	}
	
	
	/**
	 * code annotation variable
	 * @param variables - variable list
	 * @param start - start offset
	 * @return HTML string
	 */
	
	private String codeVariables(List<AnnotationVariable> variables, int start){
		StringBuilder str = new StringBuilder();
		str.append("<ol start=\""+start+"+\">");
		for(AnnotationVariable var: variables){
			str.append("<li>"+codeVariable(var)+"</li>");
		}
		str.append("</ol>");
		return str.toString();
	}


	/**
	 * get index buffer.
	 *
	 * @return the index
	 * @throws Exception the exception
	 */
	private BufferedWriter getIndex() throws Exception {
		return getIndex("index.html",true);
	}

	/**
	 * get index buffer.
	 *
	 * @return the index
	 * @throws Exception the exception
	 */
	private BufferedWriter getIndex(String outFile,boolean includeHeader) throws Exception {
		if(htmlIndexWriter == null){
			// write header 
			htmlIndexWriter = new BufferedWriter(new FileWriter(new File(outputDirectory,outFile)));
			htmlIndexWriter.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
			htmlIndexWriter.write("<head><title>"+title+"</title>\n");
			htmlIndexWriter.write(getJavaScript(includeHeader));
			htmlIndexWriter.write("</head><body style=\"overflow: hidden;\" bgcolor=\"#EEEEFF\" onload=\"l();\" onresize=\"l();\">\n");
			String height = "100%";
			if(includeHeader){
				htmlIndexWriter.write("<center><h3>"+title+" Output [<a href=\""+resultFileName+"\" ");
				htmlIndexWriter.write("title=\"Download the entire result in Tab Seperated Values (.tsv) format \">TSV</a>]</h3></center>\n");
				height = "96%";
			}
			htmlIndexWriter.write("<center><table bgcolor=\"#FFFFF\" width=\"100%\" height=\""+height+"\" border=0>\n");
			htmlIndexWriter.write("<tr><td align=\"left\" valign=\"top\" width=\"200px\" style=\"white-space: nowrap\">\n");
			htmlIndexWriter.write("<div class=\"container\" style=\"overflow: auto; max-height: 800px;\"><div style=\"border-style:solid; border-color: #EEEEFF; padding:10px 10px;\">");
		}
		return htmlIndexWriter;
	}
	
	/**
	 * flush all writers.
	 *
	 * @throws Exception the exception
	 */
	public void flush() throws Exception {
		if(htmlIndexWriter != null){
			htmlIndexWriter.write("</div></div></td><td valign=top><iframe bgcolor=white frameborder=\"0\" scrolling=\"auto\" name=\"frame\" width=\"100%\" height=\"100%\"></iframe>\n");
			htmlIndexWriter.write("</td></tr></table></center></body></html>\n");
			htmlIndexWriter.flush();
			htmlIndexWriter.close();
		}
		htmlIndexWriter = null;
	}
	
	/**
	 * create a coded html report.
	 *
	 * @param doc the doc
	 * @throws Exception the exception
	 */
	public void export(Document doc) throws Exception {
		String name = doc.getTitle();
		if(name.endsWith(".txt"))
			name = name.substring(0,name.length()-".txt".length());
		File out = new File(outputDirectory.getAbsolutePath()+File.separator+HTML_REPORT_LOCATION+File.separator+name+".html");
		BufferedWriter htmlWriter = new BufferedWriter(new FileWriter(out));
		export(doc,htmlWriter);
	}

	/**
	 * create a coded html report.
	 *
	 * @param doc the doc
	 * @throws Exception the exception
	 */
	public void export(Composition doc) throws Exception {
		String name = doc.getTitle();
		if(name.endsWith(".txt"))
			name = name.substring(0,name.length()-".txt".length());
		File out = new File(outputDirectory.getAbsolutePath()+File.separator+HTML_REPORT_LOCATION+File.separator+name+".html");
		BufferedWriter htmlWriter = new BufferedWriter(new FileWriter(out));
		export(doc,htmlWriter);
	}
	
	/**
	 * create a coded html report.
	 *
	 * @param doc the doc
	 * @param htmlWriter the html writer
	 * @throws Exception the exception
	 */
	public void export(Document doc, Writer htmlWriter) throws Exception {
		title = "Noble Coder";
		// build report
		String content = doc.getText();
		StringBuilder text = new StringBuilder();
		int offs = 0;
		for(Sentence s: doc.getSentences()){
			int o = s.getOffset();
			text.append(content.substring(offs,o).replaceAll("\n","<br>"));
			text.append(codeSentence(s));
			offs = o+s.getLength();
		}
		if(offs < content.length())
			text.append(content.substring(offs).replaceAll("\n", "<br>"));
			
		StringBuilder result = new StringBuilder();
		result.append(codeMentions(doc.getMentions()));
			
		// get report representation and cap protocol
		String report = text.toString(); //convertToHTML(text.toString());
		
		StringBuilder info = new StringBuilder();
		Long time = doc.getProcessTime().get(NobleCoder.class.getSimpleName());
		info.append("report process time: <b>"+((time != null)?time.longValue():-1)+"</b> ms , ");
		info.append("found items: <b>"+doc.getMentions().size()+"</b>");
		
		// write out results
		String name = null; 
		if(doc.getTitle() != null){
			name = doc.getTitle();
			if(name.endsWith(".txt"))
				name = name.substring(0,name.length()-".txt".length());
		}
		htmlWriter.write(createHTMLHeader("Report Processor Output",true));
		htmlWriter.write("<body onload=\"l();\" onresize=\"l();\"><table width=\"100%\" style=\"table-layout:fixed; \" cellspacing=\"5\">\n"); //word-wrap:break-word;
		if(name != null)
			htmlWriter.write("<tr><td colspan=2 align=center><h3>"+name+"</h3></td></tr>\n");
		
		String sz = "50%";
		if(showReport ^ showConceptList)
			sz = "100%";
		
		if(showReport)
			htmlWriter.write("<tr><td width=\""+sz+"\" valign=middle><div class=\"container\" style=\"overflow: auto; max-height: 800px; \">"+report+"</div></td>");
		if(showConceptList)
			htmlWriter.write("<td width=\""+sz+"\" valign=top><div class=\"container\" style=\"overflow: auto; max-height: 800px;\">"+result+"</div></td></tr>\n");
		if(showFooter)
			htmlWriter.write("<tr><td colspan=2 align=center>"+info+"</td></tr>\n");
		htmlWriter.write("<tr><td colspan=2 align=center></td></tr>\n");
		htmlWriter.flush();
		
		// finish up
		htmlWriter.write("<tr><td colspan=2></td></tr>\n");
		htmlWriter.write("</table></body></html>\n");
		htmlWriter.flush();
		htmlWriter.close();

		// add link to index
		if(createIndex){
			getIndex().write("<span style=\"max-width: 190px; font-size: 90%; overflow: hidden; display:block;\">");
			getIndex().write("<a href=\""+HTML_REPORT_LOCATION+"/"+name+".html\" target=\"frame\">"+doc.getTitle()+"</a></span>\n");
			getIndex().flush();
		}
	}
	

	/**
	 * create a coded html report.
	 *
	 * @param doc the doc
	 * @throws Exception the exception
	 */
	public void export(TemplateDocument doc) throws Exception {
		title = "Information Extraction";
		
		// create cap protocol
		String cap =  createTemplate(doc);
		
		
		// build report
		String content = doc.getText();
		StringBuilder text = new StringBuilder();
		int offs = 0;
		for(Sentence s: doc.getSentences()){
			int o = s.getOffset();
			text.append(content.substring(offs,o).replaceAll("\n","<br>"));
			text.append(codeSentence(s));
			offs = o+s.getLength();
		}
		text.append(content.substring(offs).replaceAll("\n", "<br>"));
		
		int n = 0;
		for(Template t: doc.getItemInstances().keySet()){
			n += doc.getItemInstances().get(t).size();
		}
		
		// get report representation and cap protocol
		String report = text.toString(); //convertToHTML(text.toString());
		
		StringBuilder info = new StringBuilder();
		Long time = doc.getProcessTime().get(InformationExtractor.class.getSimpleName());
		info.append("report process time: <b>"+((time != null)?time.longValue():-1)+"</b> ms , ");
		info.append("found items: <b>"+n+"</b>");
		
		// write out results
		String name = doc.getTitle();
		if(name.endsWith(".txt"))
			name = name.substring(0,name.length()-".txt".length());
		File out = new File(outputDirectory.getAbsolutePath()+File.separator+HTML_REPORT_LOCATION+File.separator+name+".html");
		BufferedWriter htmlWriter = new BufferedWriter(new FileWriter(out));

		htmlWriter.write(createHTMLHeader("Report Processor Output",true));
		htmlWriter.write("<body onload=\"l();\" onresize=\"l();\"><table width=\"100%\" style=\"table-layout:fixed; \" cellspacing=\"5\">\n"); //word-wrap:break-word;
		htmlWriter.write("<tr><td colspan=2 align=center><h3>"+name+"</h3></td></tr>\n");
		htmlWriter.write("<tr><td width=\"50%\" valign=middle><div class=\"container\" style=\"overflow: auto; max-height: 800px; \">"+report+"</div></td>");
		htmlWriter.write("<td width=\"50%\" valign=top><div class=\"container\" style=\"overflow: auto; max-height: 800px;\">"+cap+"</div></td></tr>\n");
		htmlWriter.write("<tr><td colspan=2 align=center>"+info+"</td></tr>\n");
		htmlWriter.write("<tr><td colspan=2 align=center></td></tr>\n");
		htmlWriter.flush();
		
		// finish up
		htmlWriter.write("<tr><td colspan=2></td></tr>\n");
		htmlWriter.write("</table></body></html>\n");
		htmlWriter.flush();
		htmlWriter.close();

		// add link to index
		if(createIndex){
			getIndex().write("<span style=\"max-width: 190px; font-size: 90%; overflow: hidden; display:block;\">");
			getIndex().write("<a href=\""+HTML_REPORT_LOCATION+"/"+name+".html\" target=\"frame\">"+doc.getTitle()+"</a></span>\n");
			getIndex().flush();
		}
	}
	
	
	/**
	 * create a coded html report.
	 *
	 * @param doc the doc
	 * @param htmlWriter - where to write to
	 * @throws Exception the exception
	 */
	public void export(Composition doc, Writer htmlWriter) throws Exception {
		title = "Noble Mentions";
		// build report
		String content = doc.getText();
		StringBuilder text = new StringBuilder();
		int offs = 0;
		for(Sentence s: doc.getSentences()){
			int o = s.getOffset();
			text.append(content.substring(offs,o).replaceAll("\n","<br>"));
			text.append(codeSentence(s));
			offs = o+s.getLength();
		}
		if(offs < content.length())
			text.append(content.substring(offs).replaceAll("\n", "<br>"));
			
		// build up results
		StringBuilder result = new StringBuilder();
		result.append("<p><b>Annotations</b><p>");
		result.append(codeVariables(doc.getAnnotationVariables(),1));
		result.append("</p>");
		
		// create list of rejected variables
		if(!doc.getRejectedAnnotationVariables().isEmpty()){
			result.append("<p><b><a href=\"\" onclick=\"showHide('failedVariables'); return false\" >Rejected Annotations</a> ..</b>");
			result.append("<div id=\"failedVariables\" style=\"visibility: hidden\">");
			result.append(codeVariables(doc.getRejectedAnnotationVariables(),doc.getAnnotationVariables().size()+1));
			result.append("</div></p>");
		}
		
		
		// get report representation and cap protocol
		String report = text.toString(); //convertToHTML(text.toString());
		
		StringBuilder info = new StringBuilder();
		Long time = doc.getProcessTime().get(NobleCoder.class.getSimpleName());
		info.append("report process time: <b>"+((time != null)?time.longValue():-1)+"</b> ms , ");
		info.append("found items: <b>"+doc.getMentions().size()+"</b>");
		
		// write out results
		String name = null; 
		if(doc.getTitle() != null){
			name = doc.getTitle();
			if(name.endsWith(".txt"))
				name = name.substring(0,name.length()-".txt".length());
		}
		//File out = new File(outputDirectory.getAbsolutePath()+File.separator+HTML_REPORT_LOCATION+File.separator+name+".html");
		//BufferedWriter htmlWriter = new BufferedWriter(new FileWriter(out));

		htmlWriter.write(createHTMLHeader("Report Processor Output",true));
		htmlWriter.write("<body onload=\"l();\" onresize=\"l();\"><table width=\"100%\" style=\"table-layout:fixed; \" cellspacing=\"5\">\n"); //word-wrap:break-word;
		if(name != null)
			htmlWriter.write("<tr><td colspan=2 align=center><h3>"+name+"</h3></td></tr>\n");
		
		String sz = "50%";
		if(showReport ^ showConceptList)
			sz = "100%";
		
		if(showReport)
			htmlWriter.write("<tr><td width=\""+sz+"\" valign=middle><div class=\"container\" style=\"overflow: auto; max-height: 800px; \">"+report+"</div></td>");
		if(showConceptList)
			htmlWriter.write("<td width=\""+sz+"\" valign=top><div class=\"container\" style=\"overflow: auto; max-height: 800px;\">"+result+"</div></td></tr>\n");
		if(showFooter)
			htmlWriter.write("<tr><td colspan=2 align=center>"+info+"</td></tr>\n");
		htmlWriter.write("<tr><td colspan=2 align=center></td></tr>\n");
		htmlWriter.flush();
		
		// finish up
		htmlWriter.write("<tr><td colspan=2></td></tr>\n");
		htmlWriter.write("</table></body></html>\n");
		htmlWriter.flush();
		htmlWriter.close();

		// add link to index
		if(createIndex){
			getIndex().write("<span style=\"max-width: 190px; font-size: 90%; overflow: hidden; display:block;\">");
			getIndex().write("<a href=\""+HTML_REPORT_LOCATION+"/"+name+".html\" target=\"frame\">"+doc.getTitle()+"</a></span>\n");
			getIndex().flush();
		}
	}
	
	
	
	/**
	 * is this a valid annotation belonging to found items.
	 *
	 * @param previous the previous
	 * @param l the l
	 * @param map the map
	 * @return true, if successful
	 */
	private boolean checkAnnotation(List<Annotation> previous, Annotation l, Map<Template, List<ItemInstance>> map) {
		// if span is with previous one, don't include
		int fromIndex = (previous.size()>5)?previous.size()-5:0; 
		for(Annotation last: previous.subList(fromIndex, previous.size())){
			if(((last.getStartPosition() <= l.getStartPosition() && l.getEndPosition() <= last.getEndPosition()) ||
				(l.getStartPosition() <= last.getStartPosition() && last.getEndPosition() <= l.getEndPosition())))
				return false;
		}
		// check if it was mentioned
		boolean include = false;
		for(Template t: map.keySet()){
			for(ItemInstance i: map.get(t)){
				if(i.getAnnotations().contains(l)){
					include = true;
					break;
				}
			}
		}
		
		return include;
	}
	
	/**
	 * code label.
	 *
	 * @param e the e
	 * @return the string
	 */
	private String codeConcept(ItemInstance e){
		String lid = e.getName();
		String text = e.getAnswer();
		String tip = e.getDescription();
		List<String> codes = new ArrayList<String>();
		try{
			for(Annotation l: e.getAnnotations()){
				codes.add("'"+l.getOffset()+"'");
			}
		}catch(Exception ex){}
		return "<label id=\""+lid+"\" style=\"color:blue;\" onmouseover=\"h("+codes+");\" onmouseout=\"u("+codes+");\" onclick=\"j("+codes+")\" title=\""+tip+"\">"+text+"</label>";
	}

	/**
	 * code label.
	 *
	 * @param e the e
	 * @return the string
	 */
	private String codeTemplateItem(ItemInstance e){
		String lid = e.getName();
		String text = e.getQuestion();
		String tip = e.getTemplateItem().getDescription();
		List<String> codes = new ArrayList<String>();
		try{
			for(Annotation l: e.getAnnotations()){
				codes.add("'"+l.getOffset()+"'");
			}
		}catch(Exception ex){}
		return "<label id=\""+lid+"\" style=\"color:blue;\" onmouseover=\"h("+codes+");\" onmouseout=\"u("+codes+");\" onclick=\"j("+codes+")\" title=\""+tip+"\">"+text+"</label>";
	}

	/**
	 * convert regular text report to HTML
	 * 
	 * @param txt
	 * @return
	 *
	public static String convertToHTML(String txt) {
		return (txt + "\n").replaceAll("\n", "<br>");
		//.replaceAll("(^|<br>)([A-Z ]+:)<br>", "$1<b>$2</b><br>")
		//.replaceAll("(^|<br>)(\\[[A-Za-z ]+\\])<br>", "$1<b>$2</b><br>");
	}
	*/
	
	/**
	 * get javascript definitions
	 * @return javascript definitions
	 */
	
	private String getJavaScript(){
		return getJavaScript(true);
	}
	
	/**
	 * get javascript definitions
	 * @return javascript definitions
	 */
	
	private String getJavaScript(boolean includeHeader){
		String delta = "10";
		if(includeHeader)
			delta= "100";
		
		return "<script type=\"text/javascript\">"+
				// hightlight annotations
				"function h(id){ for(i=0;i<id.length;i++){if(document.getElementById(id[i])!=null){document.getElementById(id[i]).style.backgroundColor=\"yellow\";}}}\n"+
				// un-hightlight annotations
				"function u(id){for(i=0;i<id.length;i++){if(document.getElementById(id[i])!=null){document.getElementById(id[i]).style.backgroundColor=\"white\";}}}\n"+
				// is element in view
				"function inView(id) {\n" + 
				"	var el = document.getElementById(id);\n" + 
				"	if(el == null){return true;}\n" + 
				"    var tp = el.getBoundingClientRect().top;\n" + 
				"    var bt = el.getBoundingClientRect().bottom;\n" + 
				"    return (tp >= 0) && (bt <= window.innerHeight);\n" + 
				"}\n\n" + 
				// jump to annotation if not in view
				"function j(id){\n" + 
				"	if(!inView(id[0])){\n" + 
				"		location.href=\"#\"+id[0];\n" + 
				"	}\n" + 
				"}\n" + 
				// resize viewport 
				"function l(){var h=800;if(!window.innerWidth){\n"+
				"if(!(document.documentElement.clientWidth == 0)){\n h = document.documentElement.clientHeight;\n"+
				"}else{h = document.body.clientHeight;}}else{ h = window.innerHeight;} var hd = (h-"+delta+")+\"px\";\n"+
				//"document.getElementById(\"d1\").style.maxHeight=hd;document.getElementById(\"d2\").style.maxHeight=hd;document.getElementById(\"d0\").style.maxHeight=hd;"+
				"var cont = document.getElementsByClassName(\"container\");\n" + 
				"	for(i = 0; i < cont.length; i++) {\n" + 
				"		cont[i].style.maxHeight=hd;\n" + 
				"		cont[i].style.height=hd;\n" + 
				"	}"+
				"}"+
				
				// show/hide element
				"function showHide(id){ var a = \"hidden\"; if(document.getElementById(id).style.visibility == \"hidden\"){ a = \"visible\";}"+
				"document.getElementById(id).style.visibility = a;}"+
				"</script>\n";
	}

	/**
	 * get HTML header
	 * @param title = title
	 * @param includeJavaScript - include JavaScript or not
	 * @return
	 */
	private String createHTMLHeader(String title, boolean includeJavaScript){
		return 	"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"+
				"<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"+
				"<head><title>"+title+"</title>\n"+(includeJavaScript?getJavaScript():"")+"</head>\n";
	}


	
	/**
	 * export analysis object to HTML
	 * @param analysis object
	 * @throws IOException in case something goes wrong
	 */
	public void export(Analysis analysis) throws IOException{
		File out = new File(outputDirectory,AnnotationEvaluation.ANALYSIS_HTML);
		BufferedWriter htmlWriter = new BufferedWriter(new FileWriter(out));

		// create analysis HTML
		htmlWriter.write(createHTMLHeader("Analysis",false));
		htmlWriter.write("<body bgcolor=\"#EEEEFF\" ><center>");
		htmlWriter.write("<h3>"+analysis.getTitle()+" [<a href=\""+AnnotationEvaluation.ANALYSIS_TSV+"\">TSV</a>]</h3>");
		htmlWriter.write(analysis.getResultTableAsHTML());
		htmlWriter.write("</center></body></html>\n");
		htmlWriter.flush();
		htmlWriter.close();

		// create error files
		for(String label: analysis.getErrorMap().keySet()){
			out = new File(outputDirectory,HTML_ERROR_LOCATION+File.separator+label+".html");
			if(!out.getParentFile().exists())
				out.getParentFile().mkdirs();
			htmlWriter = new BufferedWriter(new FileWriter(out));

			// create analysis HTML
			htmlWriter.write(createHTMLHeader(label,true));
			htmlWriter.write("<body onload=\"l();\" onresize=\"l();\""); 
			htmlWriter.write("<center><h2>"+label+"</h2></center>");
			htmlWriter.write("<center><table bgcolor=\"#FFFFF\" width=\"100%\" height=\"95%\" border=0>\n");
			htmlWriter.write("<tr><td align=\"left\" valign=\"top\" width=\"400px\" style=\"white-space: nowrap\">\n");
			htmlWriter.write("<div class=\"container\" style=\"overflow: auto; max-height: 800px; max-width: 400px;\"><div style=\"border-style:solid; border-color: #EEEEFF; padding:10px 10px;\">");

			htmlWriter.write(analysis.getErrorsAsHTML(label));

			htmlWriter.write("</div></div></td><td valign=top width=\"100%\" height=\"100%\"><iframe bgcolor=white frameborder=\"0\" scrolling=\"auto\" name=\"frame\" width=\"100%\" height=\"100%\"></iframe>\n");
			htmlWriter.write("</td></tr></table></center></body></html>\n");

			htmlWriter.flush();
			htmlWriter.close();
		}

	}

	/**
	 * export annotation comparison between gold and system composition instances
	 * @param textFile - file that has original text
	 * @param goldInst - gold composition instance
	 * @param sysInst - system composition instance
	 * @throws Exception in case something goes wrong
	 */
	public void export(File textFile, IInstance goldInst, IInstance sysInst) throws Exception{
		String eval = AnnotationEvaluation.EVALUATION_HTML;
		String name = FileTools.stripExtension(textFile.getName());

		File out = new File(outputDirectory.getAbsolutePath()+File.separator+HTML_EVAL_LOCATION+File.separator+name+".html");
		if(!out.getParentFile().exists())
			out.getParentFile().mkdirs();
		BufferedWriter htmlWriter = new BufferedWriter(new FileWriter(out));


		// build report
		TreeMap<Span,Set<String>> spanMap =new TreeMap<Span,Set<String>>();
		addAnnotationSpans(goldInst,spanMap,"g_");
		addAnnotationSpans(sysInst,spanMap,"s_");

		// get report representation and cap protocol
		String content = FileTools.getText(textFile);
		String report = convertToHTML(content,spanMap);

		// build up results
		StringBuilder goldResult = new StringBuilder();
		goldResult.append("<p><b>Gold Annotations</b><p>");
		goldResult.append(codeCompositionInstance(goldInst,"green"));
		goldResult.append("</p>");

		StringBuilder sysResult = new StringBuilder();
		sysResult.append("<p><b>System Annotations</b><p>");
		sysResult.append(codeCompositionInstance(sysInst,"blue"));
		sysResult.append("</p>");

		htmlWriter.write(createHTMLHeader("Evaluation Output",true));
		htmlWriter.write("<body onload=\"l();\" onresize=\"l();\">");
		htmlWriter.write("<table width=\"100%\" style=\"table-layout:fixed;  display:inline-block;\" cellspacing=\"5\">\n"); //word-wrap:break-word;
		htmlWriter.write("<tr><td colspan=3 align=center><h3>"+name+"</h3></td></tr>\n");

		htmlWriter.write("<tr><td width=\"20%\" valign=top><div class=\"container\" style=\"overflow: auto; max-height: 800px;\">"+goldResult+"</div></td>\n");
		htmlWriter.write("<td width=\"60%\" valign=middle><div class=\"container\" style=\"overflow: auto; max-height: 800px; \">"+report+"</div></td>\n");
		htmlWriter.write("<td width=\"20%\" valign=top><div class=\"container\" style=\"overflow: auto; max-height: 800px;\">"+sysResult+"</div></td></tr>\n");
		htmlWriter.write("<tr><td colspan=3 align=center></td></tr>\n");
		htmlWriter.write("</table></body></html>\n");
		htmlWriter.flush();
		htmlWriter.close();

		// add link to index
		if(createIndex){
			getIndex(eval,false).write("<span style=\"max-width: 190px; font-size: 90%; overflow: hidden; display:block;\">");
			getIndex(eval,false).write("<a href=\""+HTML_EVAL_LOCATION+"/"+name+".html\" target=\"frame\">"+name+"</a></span>\n");
			getIndex(eval,false).flush();
		}
	}

	/**
	 * given an instanceo of a Composition, get spans for all mention level annotations
	 * @param inst - composition instance
	 * @param spanMap - span map to add to
	 */
	private void addAnnotationSpans(IInstance inst, TreeMap<Span, Set<String>> spanMap, String prefix) {
		if(inst == null)
			return;
		
		// add mention level values
		IOntology ont = inst.getOntology();
		for(Object o: inst.getPropertyValues(ont.getProperty(DomainOntology.HAS_MENTION_ANNOTATION))){
			if(o instanceof IInstance){
				IInstance var = (IInstance) o;
				for(Span span : getVariableSpans(var)){
					// insert span
					if(spanMap.containsKey(span)){
						spanMap.get(span).add(prefix+var.getName());
					}else{
						Span overlappingSpan = null;
						
						// find overlapping span
						for(Span spn: spanMap.keySet()){
							if(spn.overlaps(span)){
								overlappingSpan = spn;
								break;
							}
						}
						// if overlapping span is null, just insert the new one
						if(overlappingSpan == null){
							Set<String> set = new HashSet<String>();
							set.add(prefix+var.getName());
							spanMap.put(span,set);
						// else try to split up existing span	
						}else{
							Span spn = overlappingSpan;
							Set<String> ids = spanMap.get(spn);
							List<Span> overlapSpans = new ArrayList<Span>();
							
							// break up existing spans into parts
							if(spn.start() ==  span.start()){
								overlapSpans.add(new Span(spn.start(),Math.min(span.end(),spn.end())));
								overlapSpans.add(new Span(Math.min(span.end(),spn.end()),Math.max(span.end(),spn.end())));
							}else if(spn.end() == span.end()){
								overlapSpans.add(new Span(Math.min(span.start(),spn.start()),Math.max(span.start(),spn.start())));
								overlapSpans.add(new Span(Math.max(span.start(),spn.start()),span.end()));
							}else{
								overlapSpans.add(new Span(Math.min(span.start(),spn.start()),Math.max(span.start(),spn.start())));
								overlapSpans.add(new Span(Math.max(span.start(),spn.start()),Math.min(span.end(),spn.end())));
								overlapSpans.add(new Span(Math.min(span.end(),spn.end()),Math.max(span.end(),spn.end())));
							}
							
							// remove original span
							spanMap.remove(spn);
							// add new span
							for(Span s: overlapSpans){
								Set<String> nids = new HashSet<String>(ids);
								if(span.contains(s)){
									nids.add(prefix+var.getName());
								}
								spanMap.put(s,nids);
							}	
						}
					}
				}
			}
		}
	}

	private List<Span> getVariableSpans(IInstance var){
		List<Span> spans = new ArrayList<Span>();
		for(Object oo : var.getPropertyValues(var.getOntology().getProperty(DomainOntology.HAS_SPAN))){
			for(String sp: oo.toString().split("\\s+")) {
				spans.add(Span.getSpan(sp));
			}
		}
		return spans;
	}
	
	
	private String codeCompositionInstance(IInstance composition,String color){
		if(composition == null)
			return "";
		IOntology ont = composition.getOntology();
		StringBuilder str = new StringBuilder();
		str.append("<ol><p>");
		List<IInstance> vars = new ArrayList<IInstance>();
		for(Object oa: composition.getPropertyValues(ont.getProperty(DomainOntology.HAS_MENTION_ANNOTATION))){
			if(oa instanceof IInstance){
				vars.add(((IInstance)oa));
			}
		}
		Collections.sort(vars,new Comparator<IInstance>() {
			public int compare(IInstance o1, IInstance o2) {
				List<Span> l1 = getVariableSpans(o1);
				List<Span> l2 = getVariableSpans(o2);
				if(l1.isEmpty())
					return -1;
				if(l2.isEmpty())
					return 1;
				return l1.get(0).compareTo(l2.get(0));
			}
		});
		for(IInstance i: vars)
			str.append(codeVariable(i,color)+"\n");
        str.append("</ol></p>");
		return str.toString();
	}

	/**
	 * code individual concept.
	 *
	 * @param c the c
	 * @param color the color
	 * @param aa the aa
	 * @return the string
	 */
	private String codeVariable(IInstance inst,String color){
		StringBuilder str = new StringBuilder();
		List<String> ids = new ArrayList<String>();
		for(Span span: getVariableSpans(inst)){
			ids.add("'"+span.start()+"'");
			ids.add("'"+span.end()+"'");
		}
		str.append("<li>"+codeEntity(inst.getDirectTypes()[0].getName(), inst.getName(), "",color,ids)+"<ul>");
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
	
	
	
	/**
	 * convert TEXT to html with highlighted spans
	 * @param text - original text document
	 * @param spanMap - sorted map of non-overlapping spans
	 * @return HTML document
	 */
	private String convertToHTML(String text, TreeMap<Span,Set<String>> spanMap){
		StringBuilder str = new StringBuilder();
		int offs = 0;
		for(Span s: spanMap.keySet()){
			//if we fucked up, just skip the span
			if(s.start() < offs)
				continue;
			str.append(text.substring(offs,s.start()).replaceAll("\n","<br>"));
			str.append(codeSpan(s.start(),text.substring(s.start(),s.end()),spanMap.get(s)));
			offs = s.end();
		}
		if(offs < text.length()){
			str.append(text.substring(offs).replaceAll("\n", "<br>"));
		}
		return str.toString();
	}

	private String codeSpan(int offset, String text, Set<String> ids) {
		String lid = ""+offset;
		List<String> codes = new ArrayList<String>();
		StringBuilder tip = new StringBuilder();
		tip.append("span: "+offset+":"+(offset+text.length()));
		
		// replace newlines
		text = text.replaceAll("\n","<br>");
		
		String color = null;
		String commonColor = "#9e2bef";// "#4f989e";
		for(String id: ids){
			// strip suffix
			if(id.startsWith("g_")){
				id = id.substring(2);
				color = (color == null || "green".equals(color))?"green":commonColor;
			}else if(id.startsWith("s_")){
				id = id.substring(2);
				color = (color == null || "blue".equals(color))?"blue":commonColor;
			}
			codes.add("'"+id+"'");
		}
		return "<label id=\""+lid+"\" style=\"color:"+color+";\" onmouseover=\"h("+codes+");\" onmouseout=\"u("+codes+");\" title=\""+TextTools.escapeHTML(tip.toString())+"\">"+text+"</label>";
	}
}
