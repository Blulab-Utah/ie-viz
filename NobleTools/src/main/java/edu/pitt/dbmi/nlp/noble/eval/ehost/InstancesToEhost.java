package edu.pitt.dbmi.nlp.noble.eval.ehost;

import edu.pitt.dbmi.nlp.noble.coder.model.Span;
import edu.pitt.dbmi.nlp.noble.mentions.model.DomainOntology;
import edu.pitt.dbmi.nlp.noble.ontology.*;
import edu.pitt.dbmi.nlp.noble.ontology.owl.OOntology;
import edu.pitt.dbmi.nlp.noble.tools.ConText;
import edu.pitt.dbmi.nlp.noble.tools.TextTools;
import edu.pitt.dbmi.nlp.noble.util.ColorTools;
import edu.pitt.dbmi.nlp.noble.util.FileTools;
import edu.pitt.dbmi.nlp.noble.util.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static edu.pitt.dbmi.nlp.noble.tools.ConText.CONTEXT_OWL;
import static edu.pitt.dbmi.nlp.noble.tools.ConText.SCHEMA_OWL;

public class InstancesToEhost {
	public static final String KNOWTATOR_SUFFIX = ".knowtator.xml";
	public static final String CONFIG_DIR = "config";
	public static final String CORPUS_DIR = "corpus";
	public static final String SAVED_DIR = "saved";
	public static final String ADJUDICATION_DIR = "adjudication";

	private File corpusDir;
	private File outputDir;
	private String annotator;	
	private Date creationDate;
	private int instanceCount = 0;
	private Set<IClass> annotationClasses;
	private List<String> classFilter;
	
	public File getCorpusDir() {
		return corpusDir;
	}
	public void setCorpusDir(File corpusDir) {
		this.corpusDir = corpusDir;
	}
	public File getOutputDir() {
		return outputDir;
	}
	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}
	
	public List<String> getClassFilter() {
		if(classFilter == null)
			classFilter = new ArrayList<String>();
		return classFilter;
	}
	public void setClassFilter(List<String> classFilter) {
		this.classFilter = classFilter;
	}
	public String getAnnotator() {
		if(annotator == null)
			annotator = "A1";
		return annotator;
	}
	public void setAnnotator(String annotator) {
		this.annotator = annotator;
	}
	
	public Date getCreationDate() {
		if(creationDate == null)
			creationDate = new Date();
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * get valid annotation classes
	 * @return set of annotation classes
	 */
	public Set<IClass> getAnnotationClasses() {
		if(annotationClasses == null)
			annotationClasses = new LinkedHashSet<IClass>();
		return annotationClasses;
	}
	
	/**
	 * convert instances from ontology to eHOST
	 * @param ontology - ontology instances
	 * @throws IOException - exception 
	 */
	public void convert(IOntology ontology) throws IOException{
		if(!outputDir.exists())
			outputDir.mkdirs();
		
		//get eHOST sub directories
		File config = new File(outputDir,CONFIG_DIR);
		if(!config.exists())
			config.mkdir();
		File saved = new File(outputDir,SAVED_DIR);
		if(!saved.exists())
			saved.mkdir();
	
		// copy corpus
		FileTools.copyDirectory(corpusDir, new File(outputDir,CORPUS_DIR));
		
		// generate schema
		File schemaFile = new File(config,"projectschema.xml");
		try {
			XMLUtils.writeXML(createSchema(ontology),new FileOutputStream(schemaFile));
		} catch (Exception e) {
			throw new IOException(e);
		}
		
		// copy saved annotations
		//Set<String> corpusFiles = new HashSet<String>();
		for(IInstance composition: getCompositions(ontology)){
			try {
				Document dom = XMLUtils.createDocument();  
				String title = getTitle(composition);
				convertComposition(composition,dom); 
				XMLUtils.writeXML(dom,new FileOutputStream(new File(saved,title+KNOWTATOR_SUFFIX)));
				//corpusFiles.add(title);
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
		
		/*// remove extra corpus files
		for(File f: new File(outputDir,CORPUS_DIR).listFiles()){
			if(!corpusFiles.contains(f.getName())){
				f.delete();
			}
		}*/
	}
	
	private List<IInstance> getCompositions(IOntology ontology){
		List<IInstance> list = new ArrayList<IInstance>();
		for(IInstance composition: ontology.getClass(DomainOntology.COMPOSITION).getDirectInstances()){
			list.add(composition);
		}
		Collections.sort(list,new Comparator<IInstance>() {
			public int compare(IInstance o1, IInstance o2) {
				String t1 = getTitle(o1);
				String t2 = getTitle(o2);
				return t1.compareTo(t2);
			}
		});
		
		return list;
	}
	
	
	/**
	 * add additional annotations as a different annotator
	 * @param ontology - ontology instances
	 * @param annotator - author name
	 * @throws IOException - exception
	 */
	public void addAnnotations(IOntology ontology, String annotator) throws IOException{
		setAnnotator(annotator);
		File saved = new File(outputDir,SAVED_DIR);
		if(!saved.exists())
			saved.mkdir();
		
		
		// copy saved annotations
		for(IInstance composition: getCompositions(ontology)){
			try {
				// create a dom version of composition
				String title = getTitle(composition);
				
				// see if there is an existing file available
				Document dom = null;
				File f = new File(saved,title+KNOWTATOR_SUFFIX);
				if(f.exists()){
					dom = XMLUtils.parseXML(new FileInputStream(f));
				}else{
					//dom = XMLUtils.createDocument();
					//skip documents that were not annotated
					//by another annotator
					continue;
				}
				
				// convert composition
				convertComposition(composition,dom); 
				
				
				XMLUtils.writeXML(dom,new FileOutputStream(new File(saved,getTitle(composition)+KNOWTATOR_SUFFIX)));
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
		
	}
	
	
	private String getTitle(IInstance composition){
		String name = composition.getName();
		for(Object o: composition.getPropertyValues(composition.getOntology().getProperty(DomainOntology.HAS_TITLE))){
			name = o.toString();
		}
		return name;
		
	}
	
	/**
	 * convert composition to Knowtator XML representation
	 * @param composition
	 * @param filter
	 * @return
	 * @throws ParserConfigurationException 
	 */
	private void convertComposition(IInstance composition, Document dom) throws ParserConfigurationException {
		IOntology ont = composition.getOntology();
		//Document dom = XMLUtils.createDocument(); 
		Element root = dom.getDocumentElement();
		if(root == null){
			root = dom.createElement("annotations");
			root.setAttribute("textSource",getTitle(composition));
			dom.appendChild(root);
		}
	
		// add annotations
		for(Object o: composition.getPropertyValues(ont.getProperty(DomainOntology.HAS_MENTION_ANNOTATION))){
			if(o instanceof IInstance){
				convertVariable(dom,root,(IInstance)o);
			}
		}
		
		// set some adjudication status settings
		Element adj = XMLUtils.createElement(dom,"eHOST_Adjudication_Status","version","1.0");
		adj.appendChild(XMLUtils.createElement(dom,"Adjudication_Selected_Annotators","version","1.0"));
		adj.appendChild(XMLUtils.createElement(dom,"Adjudication_Selected_Classes","version","1.0"));
		Element others = dom.createElement("Adjudication_Others");
		others.appendChild(XMLUtils.createElement(dom,"CHECK_OVERLAPPED_SPANS","false"));
		others.appendChild(XMLUtils.createElement(dom,"CHECK_ATTRIBUTES","false"));
		others.appendChild(XMLUtils.createElement(dom,"CHECK_RELATIONSHIP","false"));
		others.appendChild(XMLUtils.createElement(dom,"CHECK_CLASS","false"));
		others.appendChild(XMLUtils.createElement(dom,"CHECK_COMMENT","false"));
		adj.appendChild(others);
		root.appendChild(adj);
		
		
	}
	
	/**
	 * get spans from an instance
	 * @param var
	 * @return
	 */
	private List<Span> getSpans(IInstance var){
		IOntology ont = var.getOntology();
		List<Span> spans = new ArrayList<Span>();
		
		// get values
		Object [] spanVals =  var.getPropertyValues(ont.getProperty(DomainOntology.HAS_SPAN));
		Object [] textVals =  var.getPropertyValues(ont.getProperty(DomainOntology.HAS_ANNOTATION_TEXT));
		
		// check if we have an anchor, if so use just the anchor
		Object a = var.getPropertyValue(ont.getProperty(DomainOntology.HAS_ANCHOR));
		if(a != null){
			IInstance anchor = (IInstance) a;
			spanVals =  anchor.getPropertyValues(ont.getProperty(DomainOntology.HAS_SPAN));
			textVals =  anchor.getPropertyValues(ont.getProperty(DomainOntology.HAS_ANNOTATION_TEXT));
		}
		
		// get spans
		for(Object o: spanVals){
			for(String sp : o.toString().split("\\s+")){
				spans.add(Span.getSpan(sp));
			}
		}
		// get text (now we need to figure out if it is word based or not)
		int i = 0;
		if(spanVals.length == textVals.length && spanVals.length == spans.size()){
			for(Object o:  textVals){
				spans.get(i++).setText(o.toString());
			}
		}else{
			for(Object o:  textVals){
				for(String sp : o.toString().split("\\s+")){
					if(i < spans.size()){
						spans.get(i++).setText(sp);
					}
				}
			}
			
		}
		
		return spans;
	}
	
	
	/**
	 * convert variable
	 * @param var - annotation variable instance
	 * @param root - element root to add the info
	 */
	private void convertVariable(Document dom, Element root, IInstance var) {
		// create annotations
		IClass cls = var.getDirectTypes()[0];
		String instanceId = var.getName();
		StringBuffer text = new StringBuffer("");
		
		// only create variable if class is actually
		boolean validVar = getAnnotationClasses().contains(cls);
		if(!validVar){
			for(IClass c: getAnnotationClasses()){
				if(c.hasSubClass(cls)){
					validVar = true;
					cls = c; 
					break;
				}
			}
		}
		
		// if not valid
		if(!validVar)
			return;
		
		// get id
		String classId = cls.getName();
		
		// add annotation
		Element annotation = dom.createElement("annotation");
		annotation.appendChild(XMLUtils.createElement(dom,"mention","id",instanceId));
		annotation.appendChild(XMLUtils.createElement(dom,"annotator",Collections.singletonMap("id", getAnnotator()),getAnnotator()));
		annotation.appendChild(XMLUtils.createElement(dom,"creationDate",""+getCreationDate()));
		root.appendChild(annotation);
		for(Span span: getSpans(var)){
			Map<String,String> spanMap = new LinkedHashMap<String,String>();
			spanMap.put("start",""+span.getStartPosition());
			spanMap.put("end",""+span.getEndPosition());
			text.append(span.getText()+" ");
			annotation.appendChild(XMLUtils.createElement(dom,"span",spanMap));
			
		}
		annotation.appendChild(XMLUtils.createElement(dom,"spannedText",text.toString()));
	
		
		//fill in attributes
		List<String> slotsIds = new ArrayList<String>();
		for(IProperty prop: var.getProperties()){
			if(isRelevantAttributeProperty(prop) || DomainOntology.HAS_NUM_VALUE.equals(prop.getName())){
				for(Object val: var.getPropertyValues(prop)){
					String id = "EHOST_Instance_"+(instanceCount++);
					String value = ((IInstance) val).getDirectTypes()[0].getName();
					slotsIds.add(id);
					
					Element slot = XMLUtils.createElement(dom, "stringSlotMention","id",id);
					slot.appendChild(XMLUtils.createElement(dom,"mentionSlot","id",prop.getName()));
					slot.appendChild(XMLUtils.createElement(dom,"stringSlotMentionValue","value",value));
					root.appendChild(slot);
				}
			}
		}
		
		
		//fill in class
		Element classMention = XMLUtils.createElement(dom,"classMention","id",instanceId);
		for(String slot: slotsIds){
			classMention.appendChild(XMLUtils.createElement(dom,"hasSlotMention","id",slot));
		}
		classMention.appendChild(XMLUtils.createElement(dom,"mentionClass",Collections.singletonMap("id",classId),text.toString()));
		root.appendChild(classMention);
	}
	/**
	 * create schema for a given ontology
	 * @param composition
	 * @param filter
	 * @return
	 * @throws ParserConfigurationException 
	 */
	private Document createSchema(IOntology ontology) throws ParserConfigurationException {
		// create new document
		Document dom = XMLUtils.createDocument(); 
		Element root = dom.createElement("eHOST_Project_Configure");
		root.setAttribute("Version","1.0");
		dom.appendChild(root);
		
		// some options
		root.appendChild(XMLUtils.createElement(dom,"Handling_Text_Database","false"));
		root.appendChild(XMLUtils.createElement(dom,"OracleFunction_Enabled","false"));
		root.appendChild(XMLUtils.createElement(dom,"AttributeEditor_PopUp_Enabled","false"));
		root.appendChild(XMLUtils.createElement(dom,"OracleFunction","true"));
		root.appendChild(XMLUtils.createElement(dom,"AnnotationBuilder_Using_ExactSpan","false"));
		root.appendChild(XMLUtils.createElement(dom,"OracleFunction_Using_WholeWord","true"));
		root.appendChild(XMLUtils.createElement(dom,"GraphicAnnotationPath_Enabled","true"));
		root.appendChild(XMLUtils.createElement(dom,"Diff_Indicator_Enabled","trye"));
		root.appendChild(XMLUtils.createElement(dom,"Diff_Indicator_Check_CrossSpan","true"));
		root.appendChild(XMLUtils.createElement(dom,"Diff_Indicator_Check_Overlaps","false"));
		root.appendChild(XMLUtils.createElement(dom,"StopWords_Enabled","false"));
		root.appendChild(XMLUtils.createElement(dom,"Output_VerifySuggestions","false"));
		root.appendChild(XMLUtils.createElement(dom,"Pre_Defined_Dictionary_DifferentWeight","false"));
		
		// output attribute definitions
		Element attributes = dom.createElement("attributeDefs");
		root.appendChild(attributes);
		
		// get relevant attributes
		for(IProperty prop: getRelevantAttributeProperties(ontology)){
			attributes.appendChild(createAttributeDef(dom,prop));
		}
		
		// get relationships
		Element rels = dom.createElement("Relationship_Rules");
		root.appendChild(rels);
		
		// get relevant classes
		Element classDefs = dom.createElement("classDefs");
		root.appendChild(classDefs);
		for(IClass cls: getRelevantClasses(ontology)){
			classDefs.appendChild(createClassDef(dom,cls));
			getAnnotationClasses().add(cls);
		}
		
		
		return dom;
	}
	
	private Element createClassDef(Document dom, IClass cls) {
		Element e = dom.createElement("classDef");
		e.appendChild(XMLUtils.createElement(dom, "Name",cls.getName()));
		
		Color c = ColorTools.getChartColor();
		e.appendChild(XMLUtils.createElement(dom, "RGB_R",""+c.getRed()));
		e.appendChild(XMLUtils.createElement(dom, "RGB_G",""+c.getGreen()));
		e.appendChild(XMLUtils.createElement(dom, "RGB_B",""+c.getBlue()));
		
		e.appendChild(XMLUtils.createElement(dom, "InHerit_Public_Attributes","true"));
		e.appendChild(XMLUtils.createElement(dom, "Source",cls.getOntology().getName()));
		
		// check if there are some custom numeric attributes
		for(IRestriction r: cls.getEquivalentRestrictions().getRestrictions()){
			if(DomainOntology.HAS_NUM_VALUE.equals(r.getProperty().getName())){
				e.appendChild(createAttributeDef(dom,r));
			}
		}
		
		return e;
	}

	
	private List<IClass> getRange(IProperty prop){
		List<IClass> list = new ArrayList<IClass>();
		for(Object o: prop.getRange()){
			if(o instanceof IClass){
				for(IClass cls: ((IClass) o).getDirectSubClasses()){
					list.add(cls);
				}
			}
		}
		return list;
	}
	
	private List<IClass> getRange(ILogicExpression exp){
		return exp.getClasses();
	}
	
	private Element createAttributeDef(Document dom, IProperty prop) {
		return createAttributeDef(dom, prop,getRange(prop));
	}
	
	private Element createAttributeDef(Document dom, IRestriction rest) {
		return createAttributeDef(dom, rest.getProperty(),getRange(rest.getParameter()));
	}
	
	private Element createAttributeDef(Document dom, IProperty prop, List<IClass> range) {
		Element e = dom.createElement("attributeDef");
		e.appendChild(XMLUtils.createElement(dom, "Name",prop.getName()));
		
		e.appendChild(XMLUtils.createElement(dom, "is_Linked_to_UMLS_CUICode_and_CUILabel","false"));
		e.appendChild(XMLUtils.createElement(dom, "is_Linked_to_UMLS_CUICode","false"));
		e.appendChild(XMLUtils.createElement(dom, "is_Linked_to_UMLS_CUILabel","false"));
		
		String defaultValue = null;
		for(IClass cls: range){
			e.appendChild(XMLUtils.createElement(dom, "attributeDefOptionDef",cls.getName()));
			for(IRestriction rr: cls.getRestrictions(prop.getOntology().getProperty(ConText.PROP_IS_DEFAULT_VALUE))){
				defaultValue = cls.getName();
			}
			
		}
		if(defaultValue != null)
			e.appendChild(XMLUtils.createElement(dom, "defaultValue",defaultValue));
		
		return e;
	}
	
	private List<IClass> getRelevantClasses(IOntology ontology) {
		List<String> filter = getClassFilter();
		List<IClass> list = new ArrayList<IClass>();
		for(IClass cls: ontology.getClass(DomainOntology.ANNOTATION).getSubClasses()){
			// check if this is not a system class
			if(!cls.getURI().toString().matches(".*("+CONTEXT_OWL+"|"+SCHEMA_OWL+").*")){
				if(filter == null || filter.isEmpty() || filter.contains(cls.getName())){
					list.add(cls);
				}
			}
		}
		return list;
	}
	
	private boolean isRelevantAttributeProperty(IProperty prop){
		IClass ling = prop.getOntology().getClass(DomainOntology.LINGUISTIC_MODIFER);
		for(Object o: prop.getRange()){
			if(o instanceof IClass && ling.hasSubClass((IClass)o)){
				return true;
			}
		}
		return false;
	}
	
	
	private Set<IProperty> getRelevantAttributeProperties(IOntology ontology) {
		Set<IProperty> list = new LinkedHashSet<IProperty>();
		for(IClass cls : getRelevantClasses(ontology)){
			for(IRestriction r: cls.getEquivalentRestrictions().getRestrictions()){
				if(isRelevantAttributeProperty(r.getProperty())){
					list.add(r.getProperty());
				}
			}
		}
		return list;
	}
	
	
	
	public static void main(String[] args)  throws Exception{
		File outputDir = new File("/home/tseytlin/RiskFactors_eHOST/");
		File corpusDir = new File("/home/tseytlin/Data/NobleMentions/NLM_RiskFactors_Train");
		File listFile = new File("/home/tseytlin/Data/NobleMentions/Gold/HeartDiseaseRiskFactors/heartRiskAnnotationClasses.txt");
		
		File ontologyFile1 = new File("/home/tseytlin/Data/NobleMentions/Gold/HeartDiseaseRiskFactors/heartDiseaseInDiabeticsInstances.owl");
		File ontologyFile2 = new File("/home/tseytlin/Data/NobleMentions/Output/NLM_RiskFactors_Train/2017-05-08 16.25.16/heartDiseaseInDiabeticsInstances.owl");
	
		
		InstancesToEhost i2e = new InstancesToEhost();
		i2e.setOutputDir(outputDir);
		i2e.setCorpusDir(corpusDir);
		i2e.setClassFilter(Arrays.asList(TextTools.getText(new FileInputStream(listFile)).split("\n")));
		i2e.convert(OOntology.loadOntology(ontologyFile1));
		i2e.addAnnotations(OOntology.loadOntology(ontologyFile2),"A2");
		
		System.out.println("ok");

	}

}
