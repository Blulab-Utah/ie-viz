package edu.pitt.dbmi.nlp.noble.eval.gold;

import static edu.pitt.dbmi.nlp.noble.mentions.model.DomainOntology.ANNOTATION;
import static edu.pitt.dbmi.nlp.noble.mentions.model.DomainOntology.COMPOSITION;
import static edu.pitt.dbmi.nlp.noble.mentions.model.DomainOntology.HAS_MENTION_ANNOTATION;
import static edu.pitt.dbmi.nlp.noble.mentions.model.DomainOntology.HAS_TITLE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.pitt.dbmi.nlp.noble.eval.AnnotationEvaluation.Span;
import edu.pitt.dbmi.nlp.noble.ontology.IClass;
import edu.pitt.dbmi.nlp.noble.ontology.IInstance;
import edu.pitt.dbmi.nlp.noble.ontology.IOntology;
import edu.pitt.dbmi.nlp.noble.ontology.IOntologyException;
import edu.pitt.dbmi.nlp.noble.ontology.IProperty;
import edu.pitt.dbmi.nlp.noble.ontology.OntologyUtils;
import edu.pitt.dbmi.nlp.noble.ontology.owl.OOntology;
import edu.pitt.dbmi.nlp.noble.terminology.TerminologyException;
import edu.pitt.dbmi.nlp.noble.util.FileTools;

public class NLMRiskFactorsToInstances {
	private static final String DEFAULT_DOCUMENT_SUFFIX = ".txt";
	private Map<String,IClass> schemaMap;
	private IOntology ontology;
	
	public static void main(String[] args) throws IOntologyException, IOException, TerminologyException {
		if(args.length > 2){
			String bratDirectory = args[0];
			String ontology = args[1];
			String instances = args[2];
			
			NLMRiskFactorsToInstances r2i = new NLMRiskFactorsToInstances(ontology);
			r2i.convert(bratDirectory,instances);
			
		}else{
			System.err.println("Usage: "+RiskFactorsToInstances.class.getSimpleName()+" <xml directory> <ontology> <instances>");
		}
	}
	public NLMRiskFactorsToInstances(String ontologyLocation) throws IOntologyException{
		System.out.println("loading ontology .. "+ontologyLocation);
		File parentOntology = new File(ontologyLocation);
		ontology = OOntology.createOntology(createOntologyInstanceURI(parentOntology), parentOntology);
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
	 * convert RiskFactors directory of BRAT gold annotations to instnaces in a target ontology
	 * @param bratDirectory - location of annotated XML documents
	 * @param outputFile - output file
	 * @throws IOntologyException  - exception that may be thrown
	 * @throws IOException - exception 
	 * @throws TerminologyException  - exception
	 */
	public void convert(String bratDirectory, String outputFile) throws IOntologyException, IOException, TerminologyException {
	
		// go over anafora directory
		for(String docTitle :getDocumentList(bratDirectory)){
			System.out.println("converting "+docTitle+" ..");
			addDocumentInstance(docTitle,bratDirectory,ontology);
		}
		// write ontology
		System.out.println("writing ontology .. "+outputFile);
		ontology.write(new FileOutputStream(outputFile),IOntology.OWL_FORMAT);
		System.out.println("ok");
		
	}
	
	private Set<String> getDocumentList(String bratDir){
		Set<String> list = new TreeSet<String>();
		for(File d: new File(bratDir).listFiles()){
			if(d.isDirectory()){
				for(File f: d.listFiles()){
					list.add(FileTools.stripExtension(f.getName()));
				}
			}
		}
		return list;
	}
	
	
	/**
	 * define Anafora annotation entity
	 * @author tseytlin
	 */
	private static class Entity {
		public Map<String,String> properties = new LinkedHashMap<String,String>();
		private Span span;
		public String get(String name){
			return properties.get(name);
		}
		public void put(String key, String val){
			properties.put(key,val);
		}
		public String toString(){
			return properties.toString();
		}
		public Span getSpan(){
			if(span == null){
				String st = get("start");
				String en = get("end");
				if(st != null && en != null){
					span = Span.getSpan(st, en);
				}
			}
			return span;
		}
		public boolean equals(Object obj){
			return toString().equals(obj.toString());
		}
		public int hashCode() {
			return toString().hashCode();
		}
		public static Entity parseLine(String l) {
			Entity e = new Entity();
			String [] t = l.split("\t");
			if(t.length < 2)
				return null;
			
			String label = t[1];
			e.put("id",t[0]);
			e.put("label",label);
			
			if(t.length > 2){
				e.put("text",t[2].trim());
				Pattern p = Pattern.compile("(.+)\\s+(\\d+)\\s+(\\d+)");
				Matcher m = p.matcher(label);
				if(m.matches()){
					e.put("label",m.group(1));
					e.put("start",m.group(2));
					e.put("end",m.group(3));
				}
			}else{
				Pattern p = Pattern.compile("(.+)\\s+(T\\d+)");
				Matcher m = p.matcher(label);
				if(m.matches()){
					e.put("label",m.group(1));
					e.put("featureId",m.group(2));
				}
			}
			return e;
		}
		public boolean isAttribute() {
			return getId().startsWith("A");
		}
		public String getFeatureId() {
			return get("featureId");
		}
		public String getId() {
			return get("id");
		}
		public String getTag(){
			return get("tag");
		}
		public String getLabel(){
			return get("label");
		}
		public void addAttribute(Entity a) {
			put(a.getId(),a.getLabel());
			
		}
		public String getTime() {
			String time = null;
			for(String t: properties.values()){
				if(t.startsWith("Time_")){
					// assign time, or if multiple times, just assign before/during
					if(time == null)
						time = t;
					else						
						time = "Time_Before-During";
				}
			}
			return time;
		}
		public String getNegation() {
			for(String n: properties.values()){
				if("Negation".equals(n))
					return n;
			}
			return null;
		}
		public String getModality() {
			for(String n: properties.values()){
				if("Valid".equals(n) || "Invalid".equals(n))
					return n;
			}
			return null;
		}
	}
	
	
	private void addDocumentInstance(String documentTitle, String bratDir, IOntology ontology) throws FileNotFoundException, IOException, TerminologyException, IOntologyException {
		String documentName = FileTools.stripExtension(documentTitle);
		
		// add .txt suffix to align on name
		if(!documentTitle.endsWith(DEFAULT_DOCUMENT_SUFFIX))
			documentTitle = documentTitle+DEFAULT_DOCUMENT_SUFFIX;
		
		// get document text
		//String docText = FileTools.getText(new FileInputStream(docFile));
		//Document dom = XMLUtils.parseXML(new FileInputStream(xmlFile));
		List<Entity> annotations = parseAnnotations(bratDir,documentName);
		
		// create an instance
		IInstance composition = ontology.getClass(COMPOSITION).createInstance(OntologyUtils.toResourceName(documentTitle));
		composition.addPropertyValue(ontology.getProperty(HAS_TITLE),documentTitle);
		
		// process annotations
		for(Entity entity : annotations){
			IClass cls = getClass(entity);
			if(cls !=  null && cls.hasSuperClass(ontology.getClass(ANNOTATION))){
				IInstance mentionAnnotation = getInstance(cls,entity,annotations,ontology);
				// add annotations
				if(mentionAnnotation != null){
					composition.addPropertyValue(ontology.getProperty(HAS_MENTION_ANNOTATION),mentionAnnotation);
				}else{
					System.out.println("WARNING: couln't create instance, skipped "+entity);
				}
			}else{
				System.out.println("WARNING: couln't find class, skipped "+entity);
			}
		}
	}
	private IInstance getInstance(IClass cls, Entity entity, List<Entity> annotations, IOntology ontology2) {
		IInstance inst = cls.createInstance(OntologyUtils.toResourceName(cls.getName()+"_"+entity.get("document")+"_"+entity.getId()));
		IProperty hasSpan = ontology.getProperty("hasSpan");
		inst.addPropertyValue(ontology.getProperty("hasAnnotationType"),getDefaultInstance(ontology.getClass("MentionAnnotation")));
			
		// don't create annotation if you got no span
		if(entity.get("start") != null && entity.get("end") != null)
			inst.addPropertyValue(hasSpan,entity.get("start")+":"+entity.get("end"));
		else
			return null;
		
		// add temporality
		IClass temporality = ontology.getClass("Overlap_DocTimeRel");
		if(entity.getTime() != null &&  getSchemaMap().get(entity.getTime()) != null){
			temporality =  getSchemaMap().get(entity.getTime());
		}
		inst.addPropertyValue(ontology.getProperty("hasTemporality"),getDefaultInstance(temporality));
		
		
		// add experiencer
		inst.addPropertyValue(ontology.getProperty("hasExperiencer"),getDefaultInstance(ontology.getClass("Patient_Experiencer")));
		
		// add certainty
		IClass certainty =ontology.getClass("DefiniteExistence_Certainty");
		if(entity.getNegation() != null)
			certainty = getSchemaMap().get(entity.getNegation());
		inst.addPropertyValue(ontology.getProperty("hasCertainty"),getDefaultInstance(certainty));
		
		// add modality
		//IClass modality  =ontology.getClass("Actual_ContextualModality");
		if(entity.getModality() != null){
			//skip Invalid modality
			if("Invalid".equals(entity.getModality()))
				return null;
			//modality = getSchemaMap().get(entity.getModality());
		}
		//inst.addPropertyValue(ontology.getProperty("hasContextualModality"),getDefaultInstance(modality));
		
		
		if(entity.get("text") != null){
			IProperty hasText = ontology.getProperty("hasAnnotationText");
			inst.addPropertyValue(hasText,entity.get("text"));
		}
		return inst;
	}
	
	private IInstance getDefaultInstance(IClass time) {
		String name = time.getName()+"_default";
		IInstance inst = time.getOntology().getInstance(name);
		if(inst == null){
			inst = time.createInstance(name);
		}
		return inst;
	}
	
	private Map<String, IClass> getSchemaMap() {
		if(schemaMap == null){
			schemaMap = new HashMap<String, IClass>();
			
			schemaMap.put("A1C",ontology.getClass("High_A1c_mention"));
			schemaMap.put("BloodPressure",ontology.getClass("High_blood_pressure_over_140_over_90_mm_per_hg"));
			schemaMap.put("BMI",ontology.getClass("BMI_over_18_mention"));
			schemaMap.put("CADEvent",ontology.getClass("CAD_event_mention"));
			schemaMap.put("CADMention",ontology.getClass("CAD_mention"));
			schemaMap.put("CADSymptom",ontology.getClass("CAD_symptom"));
			schemaMap.put("CADTestResult",ontology.getClass("CAD_test_mention"));
			schemaMap.put("Cholesterol",ontology.getClass("High_cholesterol_over_240_mention"));
			schemaMap.put("DiabetesMention",ontology.getClass("Diabetes_mention"));
			
			schemaMap.put("Glucose",ontology.getClass("High_glucose_mention"));
			schemaMap.put("HyperlipidemiaMention",ontology.getClass("Hyperlipidemia_mention"));
			schemaMap.put("HypertensionMention",ontology.getClass("Hypertension_mention"));
			schemaMap.put("LDL",ontology.getClass("High_LDL_measurement_over_100_mg_per_dL_mention"));
			
			schemaMap.put("Medication1",ontology.getClass("MedicationStatement"));
			schemaMap.put("Medication2",ontology.getClass("MedicationStatement"));
			schemaMap.put("ObeseMention",ontology.getClass("Obesity_mention"));
			schemaMap.put("SmokerMention",ontology.getClass("Smoker_ever_mention"));
			
			// hard-code some values
			schemaMap.put("Time_After",ontology.getClass("After_DocTimeRel"));
			schemaMap.put("Time_Before",ontology.getClass("Before_DocTimeRel"));
			schemaMap.put("Time_During",ontology.getClass("Overlap_DocTimeRel"));
			schemaMap.put("Time_Before-During",ontology.getClass("Before-Overlap_DocTimeRel"));
			
			schemaMap.put("Valid",ontology.getClass("Actual_ContextualModality"));
			schemaMap.put("Invalid",ontology.getClass("Generic_ContextualModality"));
			
			schemaMap.put("Negation",ontology.getClass("DefiniteNegatedExistence_Certainty"));
			
			// load up everything by label
			/*
			for(IClass cls: ontology.getClass("Annotation").getSubClasses()){
				schemaMap.put(cls.getLabel().toLowerCase(),cls);
			}
			*/
			
		}
		return schemaMap;
	}
	
	private IClass getClass(Entity entity) {
		return getSchemaMap().get(entity.getTag());
	}
	
	/**
	 * parse BRAT annotations 
	 * @param bratDir - BRAT directory
	 * @param documentName - document name
	 * @return list of annotations
	 * @throws IOException 
	 */
	private List<Entity> parseAnnotations(String bratDir, String documentName) throws IOException {
		List<Entity> annotations = new ArrayList<Entity>();
		for(File ann: new File(bratDir).listFiles()){
			if(ann.isDirectory()){
				for(File file: ann.listFiles()){
					if(file.getName().startsWith(documentName)){
						annotations.addAll(parseAnnotations(file));
					}
				}
			}
		}
		return annotations;
	}

	
	private Collection<Entity> parseAnnotations(File file) throws IOException {
		Map<String,Entity> annotations = new LinkedHashMap<String,Entity>();
		String tag = file.getParentFile().getName();
		String text = FileTools.getText(file);
		for(String l: text.split("\n")){
			Entity e = Entity.parseLine(l);
			e.put("document",FileTools.stripExtension(file.getName()));
			e.put("tag",tag);
			if(e.isAttribute()){
				Entity f  = annotations.get(e.getFeatureId());
				f.addAttribute(e);
			}else{
				annotations.put(e.getId(),e);
			}
		}
		return annotations.values();
	}
	
}
