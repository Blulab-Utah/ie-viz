package edu.pitt.dbmi.nlp.noble.eval.gold;

import edu.pitt.dbmi.nlp.noble.mentions.model.DomainOntology;
import edu.pitt.dbmi.nlp.noble.ontology.*;
import edu.pitt.dbmi.nlp.noble.ontology.owl.OOntology;
import edu.pitt.dbmi.nlp.noble.terminology.Concept;
import edu.pitt.dbmi.nlp.noble.terminology.Terminology;
import edu.pitt.dbmi.nlp.noble.terminology.TerminologyException;
import edu.pitt.dbmi.nlp.noble.terminology.impl.NobleCoderTerminology;
import edu.pitt.dbmi.nlp.noble.tools.TextTools;
import edu.pitt.dbmi.nlp.noble.util.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static edu.pitt.dbmi.nlp.noble.mentions.model.DomainOntology.*;


/**
 * convert Anafora XML to OWL instances of a given ontology
 * @author tseytlin
 *
 */
public class DeepPheToInstances {
	private final String DEFAULT_DOCUMENT_SUFFIX = ".txt"; 
	private Map<String,IClass> schemaMap;
	private IOntology ontology;
	private Terminology terminology;
	
	
	
	public static final String HAS_POLARITY = "hasPolarity";
	public static final String HAS_TEMPORALITY = "hasTemporality";
	public static final String HAS_EXPERIENCER = "hasExperiencer";
	public static final String HAS_CERTAINTY = "hasCertainty";
	
	public static final String HAS_CONTEXTUAL_MODALITY = "hasContextualModality";
	public static final String POLARITY_POSITIVE = "Positive_Polarity";
	public static final String POLARITY_NEGATIVE = "Negative_Polarity";
	public static final String EXPERIENCER_PATIENT = "Patient_Experiencer";
	public static final String EXPERIENCER_FAMILY_MEMBER = "FamilyMember_Experiencer";
	public static final String EXPERIENCER_DONOR_FAMILY_MEMBER = "DonorFamilyMember_Experiencer";
	public static final String EXPERIENCER_DONOR_OTHER_MEMBER = "DonorOtherMember_Experiencer";
	public static final String EXPERIENCER_OTHER_MEMBER = "OtherMember_Experiencer";
	
	public static final String TEMPORALITY_BEFORE = "Before_DocTimeRel";
	public static final String TEMPORALITY_BEFORE_OVERLAP = "Before-Overlap_DocTimeRel";
	public static final String TEMPORALITY_OVERLAP = "Overlap_DocTimeRel";
	public static final String TEMPORALITY_AFTER = "After_DocTimeRel";
	public static final String MODALITY_ACTUAL = "Actual_ContextualModality";
	public static final String MODALITY_GENERIC = "Generic_ContextualModality";
	public static final String MODALITY_HEDGED = "Hedged_ContextualModality";
	public static final String MODALITY_HYPOTHETICAL = "Hypothetical_ContextualModality";
	
	
	public static void main(String [] args) throws Exception{
		if(args.length > 3){
			String anaforaDirectory = args[0];
			String anaforaSuffix = args[1];
			String ontology = args[2];
			String instances = args[3];
			
			DeepPheToInstances a2i = new DeepPheToInstances();
			a2i.convert(anaforaDirectory,anaforaSuffix,ontology,instances);
			
		}else{
			System.err.println("Usage: "+DeepPheToInstances.class.getSimpleName()+" <anafora directory> <annotation suffix> <ontology> <instances>");
		}
	}

	/**
	 * create ontology instance URI
	 * @param file
	 * @return
	 * @throws IOntologyException
	 */
	private URI createOntologyInstanceURI(File file) throws IOntologyException{
		String ontologyURI = null;
		try {
			ontologyURI = ""+OntologyUtils.getOntologyURI(file);
		} catch (IOException e) {
			throw new IOntologyException("Unable get parent ontology URL "+file);
		}
		if(ontologyURI.endsWith(".owl"))
			ontologyURI = ontologyURI.substring(0,ontologyURI.length()-4);
		ontologyURI += "Instances.owl";
		return URI.create(ontologyURI);
	}
	
	/**
	 * convert anafora directory into instantiated ontology
	 * @param anaforaDirectory - anafora directory where the annotated files are
	 * @param anaforaSuffix - anafora suffix for annotated files
	 * @param ontolgyLocation - ontology location
	 * @param outputFile - output file
	 * @throws IOntologyException  - in case of error
	 * @throws IOException   - in case of error
	 * @throws TerminologyException   - in case of error
	 */
	public void convert(String anaforaDirectory, String anaforaSuffix, String ontolgyLocation, String outputFile) throws IOntologyException, IOException, TerminologyException {
		System.out.println("loading ontology .. "+ontolgyLocation);
		File parentOntology = new File(ontolgyLocation);
		ontology = OOntology.createOntology(createOntologyInstanceURI(parentOntology), parentOntology);
		// go over anafora directory
		for(File docDir : new File(anaforaDirectory).listFiles()){
			if(docDir.isDirectory()){
				String docName = docDir.getName();
				File docFile = new File(docDir,docName);
				File xmlFile = new File(docDir,docName+anaforaSuffix);
				if(docFile.exists() && xmlFile.exists()){
					System.out.println("converting "+docName+" ..");
					addDocumentInstance(docFile,xmlFile,ontology);
				}
			}
		}
		
		// write ontology
		System.out.println("writing ontology .. "+outputFile);
		ontology.write(new FileOutputStream(outputFile),IOntology.OWL_FORMAT);
		System.out.println("ok");
	}

	/**
	 * define Anafora annotation entity
	 * @author tseytlin
	 */
	private static class Entity {
		public String id,span,type,parentsType,text;
		public int start = -1, end = -1;
		public Map<String,Set<String>> properties;
		
		public int start(){
			if(span != null && start == -1){
				start = Integer.parseInt(span.split("[,:;]")[0]);
			}
			return start;
		}
		public int end(){
			if(span != null && end == -1){
				end = Integer.parseInt(span.split("[,:;]")[1]);
			}
			return end;
		}
		public boolean hasSpan(){
			return start() > -1;
		}
		
		public String toString(){
			return id+" type: "+type+" text: "+text+" "+properties;
		}
		
		/**
		 * load entity form XML element
		 * @param el - DOM element for Anafora entity
		 * @return entity element
		 */
		public static Entity load(Element el){
			Entity e = new Entity();
			for(Element i: XMLUtils.getChildElements(el,"id")){
				e.id = i.getTextContent().trim(); break;
			}
			for(Element i: XMLUtils.getChildElements(el,"span")){
				e.span = i.getTextContent().trim(); break;
			}
			for(Element i: XMLUtils.getChildElements(el,"type")){
				e.type = i.getTextContent().trim(); break;
			}
			for(Element i: XMLUtils.getChildElements(el,"parentsType")){
				e.parentsType = i.getTextContent().trim(); break;
			}
			for(Element i: XMLUtils.getChildElements(el,"properties")){
				for(Element j: XMLUtils.getChildElements(i)){
					e.addProperty(j.getTagName(),j.getTextContent().trim());
				}
			}
			return e;
		}
		/**
		 * add property
		 * @param tagName
		 * @param trim
		 */
		private void addProperty(String name, String value) {
			if(properties == null)
				 properties = new LinkedHashMap<String,Set<String>>();
			Set<String> set = properties.get(name);
			if(set == null){
				set = new LinkedHashSet<String>();
				properties.put(name,set);
			}
			set.add(value);
		}
		public String getProperty(String name) {
			if(properties == null)
				return null;
			Set<String> vals = properties.get(name);
			if(vals != null){
				for(String v: vals){
					return v;
				}
			}
			return null;
		}
		public boolean hasProperty(String name){
			return properties != null && properties.containsKey(name);
		}
		
		public Set<String> getProperties(String name) {
			Set<String> vals = properties.get(name);
			if(vals != null)
				return vals;
			return Collections.EMPTY_SET;
		}
	}
	
	/**
	 * parse Anafora annotations from XML
	 * @param dom
	 * @return
	 */
	private Map<String, Entity> parseAnnotations(Document dom,String docText) {
		Map<String,Entity> map = new LinkedHashMap<String, Entity>();
		Element annotations = XMLUtils.getElementByTagName(dom.getDocumentElement(),"annotations");
		for(Element el: XMLUtils.getChildElements(annotations,"entity")){
			Entity entity = Entity.load(el);
			if(entity.hasSpan() && docText.length() > entity.end()){
				// get text first
				entity.text = docText.substring(entity.start(),entity.end());
				// then convert offsets
				entity.start = TextTools.convertCRLF_Offset(docText, entity.start());
				entity.end =   TextTools.convertCRLF_Offset(docText, entity.end());
				
				//System.out.println("TEXT: "+entity.text+"\t"+docText.equals(docText2)+"\t"+docText2.substring(entity.start,entity.end));
			}
			map.put(entity.id,entity);
		}
		return map;
	}
	
	
	
	/**
	 * add document instance from annotated xmlFile to ontology
	 * @param docFile - a document containing report text (just in case)
	 * @param xmlFile - an xml document containing annotations
	 * @param ontology - ontology that we are mapping things to
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws TerminologyException 
	 */
	private void addDocumentInstance(File docFile, File xmlFile, IOntology ontology) throws FileNotFoundException, IOException, TerminologyException {
		String documentTitle = docFile.getName();
		// add .txt suffix to align on name
		if(!documentTitle.endsWith(DEFAULT_DOCUMENT_SUFFIX))
			documentTitle = documentTitle+DEFAULT_DOCUMENT_SUFFIX;
		
		// get document text
		//String docText2 = FileTools.getText(new FileInputStream(docFile));
		String docText = new String(Files.readAllBytes(Paths.get(docFile.toURI())));
		
		Document dom = XMLUtils.parseXML(new FileInputStream(xmlFile));
		Map<String,Entity> annotations = parseAnnotations(dom,docText);
		//Map<String,IClass> schemaMap = getSchemaMap(ontology);
		
		// create an instance
		IInstance composition = ontology.getClass(COMPOSITION).createInstance(OntologyUtils.toResourceName(documentTitle));
		composition.addPropertyValue(ontology.getProperty(HAS_TITLE),documentTitle);
		
		
		
		// process annotations
		for(Entity entity: getUsefulAnnotations(annotations)){
			IClass cls = getClass(entity);
			if(cls !=  null && cls.hasSuperClass(ontology.getClass(ANNOTATION))){
				IInstance mentionAnnotation = getInstance(cls,entity,annotations);
				// add annotations
				if(mentionAnnotation != null && ontology.getClass(ANNOTATION).hasSubClass(mentionAnnotation.getDirectTypes()[0])){
					composition.addPropertyValue(ontology.getProperty(HAS_MENTION_ANNOTATION),mentionAnnotation);
				}
			}
		}
		
	}

	private Set<Entity> getUsefulAnnotations(Map<String,Entity> annotations){
		// create a list of all annotations except tumors that have associations
		Set<Entity> includedAnnotations = new LinkedHashSet<Entity>();
		for(Entity e: annotations.values()){
			if(!"Disease_Disorder".equals(e.type) && !"size_class".equals(e.type)){
				includedAnnotations.add(e);
				if(e.hasProperty("associated_neoplasm") && annotations.containsKey(e.getProperty("associated_neoplasm"))){
					includedAnnotations.add(annotations.get(e.getProperty("associated_neoplasm")));
				}
			}else if(e.hasProperty("sizes")){
				for(String id: e.getProperties("sizes")){
					Entity ee = annotations.get(id);
					if(ee != null){
						ee.addProperty("associated_neoplasm",e.id);
						includedAnnotations.add(ee);
					}
				}
			}
		}
		return includedAnnotations;
	}
	
	/**
	 * get class for a given entity
	 * @param entity
	 * @return
	 * @throws TerminologyException 
	 */
	private IClass getClass(Entity entity) throws TerminologyException {
		IClass cls = getSchemaMap().get(entity.type);
		//String code = entity.getProperty("associatedCode");
		
		// try to get more specific for procedures
		if("Procedure".equals(entity.type)){
			Concept c = getConcept(entity);
			if(c == null){
				return null;
			}else{
				IClass annotationCls = ontology.getClass(c.getCode());
				if(annotationCls != null){
					for(IRestriction r: annotationCls.getRestrictions(ontology.getProperty(DomainOntology.IS_ANCHOR_OF))){
						for(Object o: r.getParameter()){
							if(o instanceof IClass){
								return (IClass) o;
							}
						}
					}
				}
				return cls;
			}
		}else if("Medications/Drugs".equals(entity.type)){
			Concept c = getConcept(entity);
			if(c == null){
				return null;
			}
		}
		
		return cls;
	}
	
	private Concept getConcept(Entity entity) throws TerminologyException{
		String code = entity.getProperty("associatedCode");
		Concept c = getTerminology().lookupConcept(code);
		if(c == null){
			//System.out.println("\tCould not find CODE "+code+" for type: "+entity.type+" text: "+entity.text);
			for(Concept cc: getTerminology().search(entity.text)){
				IClass ccc = ontology.getClass(cc.getCode());
				if(ccc != null && ccc.hasSuperClass(ontology.getClass(DomainOntology.ANCHOR)))
					return cc;
				IInstance iii = ontology.getInstance(cc.getCode());
				if(iii != null && iii.getDirectTypes()[0].hasSuperClass(ontology.getClass(DomainOntology.MODIFIER)))
					return cc;
				
				
			}
			System.out.println("\tCould not find code or text "+code+" for type: "+entity.type+" text: "+entity.text);
		}
		return c;
	}
	
	

	public Terminology getTerminology(){
		if(terminology == null){
			try {
				terminology = new NobleCoderTerminology(ontology.getClass("Lexicon"));
				/*TerminologyBrowser broswer = new TerminologyBrowser();
				broswer.setTerminologies(new Terminology []  {terminology});
				broswer.showDialog(null, "");*/
			} catch (IOException e) {
				e.printStackTrace();
			} catch (TerminologyException e) {
				e.printStackTrace();
			} catch (IOntologyException e) {
				e.printStackTrace();
			}
		}
		return terminology;
	}
	
	
	/**
	 * create annotation instance variable if possible
	 * @param entity - Anafaro entity
	 * @param annotations - list of anafora entities
	 * @param ontology - ontology
	 * @return instance
	 * @throws TerminologyException 
	 */
	private IInstance getInstance(IClass cls, Entity entity, Map<String, Entity> annotations) throws TerminologyException {
		// get instances if already defined
		String name = OntologyUtils.toResourceName(entity.id);
		if(ontology.hasResource(name))
			return ontology.getInstance(name);
		
		// need to find annotation class first
		IInstance inst = cls.createInstance(name);
		String code = entity.getProperty("associatedCode");
		
		// add anchor
		if(code != null){
			Concept c = getConcept(entity);
			if(c != null){
				// make sure that the anchor is within the scope of the annotation class
				IResource anchor = ontology.getResource(c.getCode());
				for(Object o: cls.getEquivalentRestrictions()){
					if(o instanceof IRestriction){
						IRestriction r = (IRestriction) o;
						if(r.getProperty().getName().equals("hasAnchor") && !r.getParameter().evaluate(anchor)){
							System.out.println("\tAnchor "+anchor.getName()+" ("+code+" ) doesn't fit the annotation "+cls.getName());
							return null;
						}
					}
				}
				
				inst.addPropertyValue( ontology.getProperty("hasAnchor"),getDefaultInstance(anchor));
			}
		}
		// add span
		inst.addPropertyValue( ontology.getProperty("hasSpan"),entity.start()+":"+entity.end());
		inst.addPropertyValue( ontology.getProperty("hasAnnotationType"),getDefaultInstance(ontology.getClass("MentionAnnotation")));
		inst.addPropertyValue( ontology.getProperty("hasAnnotationText"),entity.text);
		
		
		// get linguistic attributes
		Map<String,String> linguistics = convertLinguisticProperties(entity,annotations);
		for(String prop : linguistics.keySet()){
			IClass val = ontology.getClass(linguistics.get(prop));
			inst.addPropertyValue(ontology.getProperty(prop),getDefaultInstance(val));
		}
		
		
		// add body location
		if(entity.hasProperty("body_location")){
			Entity relatedEntity = annotations.get(entity.getProperty("body_location"));
			if(relatedEntity != null){
				Concept c = getConcept(relatedEntity);
				if(c != null){
					inst.addPropertyValue( ontology.getProperty("hasBodySite"),getDefaultInstance(ontology.getResource(c.getCode())));
				}
			}
		}
		
		// add receptor value
		if(entity.hasProperty("receptor_value")){
			Entity relatedEntity = annotations.get(entity.getProperty("receptor_value"));
			if(relatedEntity != null){
				IClass valCls = getReceptorCode(relatedEntity.getProperty("receptor_value_code"));
				if(valCls != null){
					inst.addPropertyValue(ontology.getProperty("hasInterpretation"), getDefaultInstance(valCls));
				}
			}
		}
		
		// add receptor value
		if(entity.hasProperty("associated_test")){
			Entity relatedEntity = annotations.get(entity.getProperty("associated_test"));
			if(relatedEntity != null){
				IClass test = getClass(relatedEntity);
				if(test != null){
					IInstance relatedInst = getInstance(test, relatedEntity, annotations);
					if(relatedInst != null)
						inst.addPropertyValue(ontology.getProperty("result_of"), relatedInst);
				}
			}
		}
		
		// add receptor value
		if(entity.hasProperty("associated_neoplasm")){
			Entity relatedEntity = annotations.get(entity.getProperty("associated_neoplasm"));
			if(relatedEntity != null){
				IInstance i = getInstance(getClass(relatedEntity), relatedEntity, annotations);
				if(i != null){
					// if this is a tumor size, then the tumor is an anchor
					if("size_mention".equals(cls.getName())){
						inst.addPropertyValue( ontology.getProperty("hasAnchor"),getDefaultInstance(ontology.getClass("Tumor_Size")));
					}else{
						inst.addPropertyValue(ontology.getProperty("associated_with"),i);
					}
				}
			}
		}

		// add neoplasm stage
		if(entity.hasProperty("stage_value")){
			Entity relatedEntity = annotations.get(entity.getProperty("stage_value"));
			if(relatedEntity != null && relatedEntity.text != null){
				String text = entity.text.trim()+" "+relatedEntity.text.trim();
				for(Concept c: getTerminology().search(text)){
					inst.addPropertyValue( ontology.getProperty("hasAnchor"),getDefaultInstance(ontology.getResource(c.getCode())));
					break;
				}
			}
		}
		
		/*// add tumor size 
		if(entity.hasProperty("dimension")){
			Entity relatedEntity = annotations.get(entity.getProperty("dimension"));
			System.out.println(relatedEntity);
			// do I need it
		}
		*/
		
		//check that we have an anchor
		if(!Arrays.asList(inst.getProperties()).contains(ontology.getProperty("hasAnchor"))){
			System.out.println("\tNo anchor for "+cls.getName()+" type: "+entity.type+" text: "+entity.text);
			return null;
		}
		
		
		return inst;
	}

	private IInstance getDefaultInstance(IResource r){
		if(r instanceof IInstance)
			return (IInstance) r;
		if(r instanceof IClass){
			IClass cls = (IClass) r;
			IInstance a = ontology.getInstance(cls.getName()+"_default");
			if(a == null)
				a = cls.createInstance(cls.getName()+"_default");
			return a;
		}
		return null;
	}
	
	private IClass getReceptorCode(String code){
		if("positive".equals(code)){
			return ontology.getClass("Positive_OrdinalInterpretation");
		}else if("negative".equals(code)){
			return ontology.getClass("Negative_OrdinalInterpretation");
		}else if("equivocal".equals(code)){
			return ontology.getClass("Indeterminate_OrdinalInterpretation");
		}else if("unknown".equals(code)){
			return ontology.getClass("Indeterminate_OrdinalInterpretation");
		}
		return ontology.getClass("Indeterminate_OrdinalInterpretation");
	}

	private Map<String, IClass> getSchemaMap() {
		if(schemaMap == null){
			schemaMap = new HashMap<String, IClass>();
			schemaMap.put("Disease_Disorder",ontology.getClass("associated_neoplasm_mention"));
			schemaMap.put("Finding_TNM",ontology.getClass("tnm_mention"));
			schemaMap.put("LabResult_Receptor",ontology.getClass("receptors_mention"));
			schemaMap.put("Medications/Drugs",ontology.getClass("medications_mention"));
			schemaMap.put("Metastasis",ontology.getClass("metastasis_mention"));
			schemaMap.put("Neoplasm_Stage",ontology.getClass("stage_mention"));
			schemaMap.put("size_class",ontology.getClass("size_mention"));
			schemaMap.put("Procedure",ontology.getClass("procedure_mention"));
		}
		return schemaMap;
	}

	
	private static Map<String, String> convertLinguisticProperties(Entity entity,Map<String,Entity> annotations) {
		Map<String,String> map = new LinkedHashMap<String,String>();
		
		map.put(HAS_CERTAINTY,getCertainty(entity));
		map.put(HAS_TEMPORALITY,getTemporality(entity));
		map.put(HAS_EXPERIENCER,getExperiencer(entity,annotations));
	
	
		return map;
	}
	
	public static String getTemporality(Entity entity) {
		String val = entity.getProperty("DocTimeRel");
		if(val != null){
			if("BEFORE".equals(val))
				return TEMPORALITY_BEFORE;
			if("OVERLAP".equals(val))
				return TEMPORALITY_OVERLAP;
			if("BEFORE/OVERLAP".equals(val))
				return TEMPORALITY_BEFORE_OVERLAP;
			if("AFTER".equals(val))
				return TEMPORALITY_AFTER;
			}
		return TEMPORALITY_OVERLAP;
	}
	
	private static String getCertainty(Entity entity) {
		String negationStr = entity.getProperty("negation_indicator");
		String uncertaintyStr = entity.getProperty("uncertainty_indicator");
		boolean negation = negationStr != null && negationStr.length() > 0;
		boolean uncertainty = 	uncertaintyStr != null && uncertaintyStr.length() > 0;
		if(negation && !uncertainty)
			return "DefiniteNegatedExistence_Certainty";
		if(negation && uncertainty)
			return "ProbableNegatedExistence_Certainty";
		if(!negation && uncertainty)
			return "ProbableExistence_Certainty";
		return "DefiniteExistence_Certainty";
	}
	
	/*
	private static String getPolarity(Map<String, String> properties) {
		String val = properties.get(LANGUAGE_ASPECT_NEGATED_URL);
		return val != null && Boolean.parseBoolean(val)?POLARITY_NEGATIVE:POLARITY_POSITIVE;
	}
	*/
	
	private static String getExperiencer(Entity entity,Map<String,Entity> annotations) {
		// convert experiencer
		String val = entity.getProperty("subject");
		if(val != null){
			Entity related = annotations.get(val);
			if(related != null){
				val = related.getProperty("subject_normalization");
				if("patient".equals(val))
					return EXPERIENCER_PATIENT;
				if("family_member".equals(val))
					return EXPERIENCER_FAMILY_MEMBER;
				if("donor_family_member".equals(val))
					return EXPERIENCER_DONOR_FAMILY_MEMBER;
				if("donor_other".equals(val))
					return EXPERIENCER_DONOR_OTHER_MEMBER;
				if("other".equals(val))
					return EXPERIENCER_OTHER_MEMBER;
			}
		}
		return EXPERIENCER_PATIENT;
	}

	
	
	
}
