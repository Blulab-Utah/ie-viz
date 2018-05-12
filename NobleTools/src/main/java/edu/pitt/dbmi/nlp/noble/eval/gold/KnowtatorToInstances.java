package edu.pitt.dbmi.nlp.noble.eval.gold;

import edu.pitt.dbmi.nlp.noble.coder.model.Span;
import edu.pitt.dbmi.nlp.noble.ontology.*;
import edu.pitt.dbmi.nlp.noble.ontology.owl.OOntology;
import edu.pitt.dbmi.nlp.noble.util.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.pitt.dbmi.nlp.noble.mentions.model.DomainOntology.*;

/**
 * Created by tseytlin on 4/2/17.
 */
public class KnowtatorToInstances {
    protected IOntology ontology;
    protected DomainResolver resolver;

    public static void main(String [] args) throws Exception {
        if(args.length > 2){
            String knowtatorDirectory = args[0];
            String ontologyLocation = args[1];
            String instances = args[2];

            System.out.println("loading ontology .. "+ontologyLocation);
            IOntology ont = OOntology.createOntology(OntologyUtils.createOntologyInstanceURI(ontologyLocation), ontologyLocation);

            KnowtatorToInstances k2i = new KnowtatorToInstances(ont);
            k2i.setDomainResolver(new SmokerDomainResolver(ont));
            k2i.convert(knowtatorDirectory,instances);

        }else{
            System.err.println("Usage: "+DeepPheToInstances.class.getSimpleName()+" <knowtator directory> <ontology> <instances>");
        }
    }
    public KnowtatorToInstances(IOntology ont){
        ontology = ont;
    }

    public void setDomainResolver(DomainResolver r){
        resolver = r;
    }

    public void log(Object o){
        System.out.println(o);
    }

    /**
     * convert knowtator directory to instances file
     * @param knowtatorDirectory - knowtator directory that has config/ corpus/ saved/ subdirs
     * @param ontologyLocation - location of domain ontology File or URL
     * @param outputFile - location of instances file
     * @throws Exception
     */
    private void convert(String knowtatorDirectory, String outputFile) throws Exception {
        // go over knowtator directory
        File savedDir = new File(knowtatorDirectory+File.separator+"saved");
        if(!savedDir.exists())
            throw new IOException("Knowtator direcectory "+knowtatorDirectory+" doesn't have saved/ subbolder.");
        for(File docFile : savedDir.listFiles()){
            if(docFile.isFile() && docFile.getName().endsWith(".xml")){
                log("converting "+docFile.getName()+" ..");
                addDocumentInstance(docFile,ontology);
            }
        }

        // write ontology
        log("writing ontology .. "+outputFile);
        ontology.write(new FileOutputStream(outputFile),IOntology.OWL_FORMAT);
        log("ok");
    }

    /**
     * add document instance to ontology
     * @param docFile - knowtator XML file
     * @param ontology - instance ontology
     */
    private void addDocumentInstance(File docFile, IOntology ontology) throws IOException{
        String documentTitle = docFile.getName();
        if(documentTitle.endsWith(".knowtator.xml"))
            documentTitle = documentTitle.substring(0,documentTitle.length()-".knowtator.xml".length());

        Document dom = XMLUtils.parseXML(new FileInputStream(docFile));
        List<Entity> annotations = parseAnnotations(dom);

        // create an instance
        IInstance composition = ontology.getClass(COMPOSITION).createInstance(OntologyUtils.toResourceName(documentTitle));
        composition.addPropertyValue(ontology.getProperty(HAS_TITLE),documentTitle);


        // process annotations
        for(Entity entity: annotations){
            IClass cls = resolver.getClass(entity);
            if(cls !=  null && cls.hasSuperClass(ontology.getClass(ANNOTATION))){
                IInstance mentionAnnotation = resolver.getInstance(cls,entity);
                if(mentionAnnotation != null){
                    composition.addPropertyValue(ontology.getProperty(HAS_MENTION_ANNOTATION),mentionAnnotation);
                }
            }
        }

    }

    protected static  class Entity{
        public String id, text, mentionClass, document;
        public Span span;
    }

    protected interface DomainResolver {
        IClass getClass(Entity e);
        IInstance getInstance(IClass cls, Entity e);
    }


    /**
     * parse annotations from DOM
     * @param dom
     * @param docFile
     * @return
     */
    private List<Entity> parseAnnotations(Document dom ) {
        String title = dom.getDocumentElement().getAttribute("textSource");
        Map<String,String> classMap = new HashMap<String, String>();
        // load classes
        for(Element e: XMLUtils.getChildElements(dom.getDocumentElement(),"classMention")){
            String id = e.getAttribute("id");
            for(Element ee: XMLUtils.getChildElements(e,"mentionClass")){
                classMap.put(id,ee.getAttribute("id"));
                break;
            }
        }

        // load annotations
        List<Entity> list = new ArrayList<Entity>();
        for(Element e: XMLUtils.getChildElements(dom.getDocumentElement(),"annotation")){
            String id = null;
            String start = null, end = null;
            String text = null;

            for(Element m: XMLUtils.getChildElements(e,"mention")){
                id = m.getAttribute("id"); break;
            }

            for(Element m: XMLUtils.getChildElements(e,"span")){
                start = m.getAttribute("start");
                end = m.getAttribute("end");
                break;
            }

            for(Element m: XMLUtils.getChildElements(e,"spannedText")){
                text = m.getTextContent();
                break;
            }

            Entity entity = new Entity();
            entity.id = id;
            entity.text = text;
            entity.mentionClass = classMap.get(id);
            entity.document = title;
            entity.span = Span.getSpan(start,end);

            list.add(entity);

        }
        return list;
    }


    private static class SmokerDomainResolver implements DomainResolver {
        private IOntology ontology;
        public SmokerDomainResolver(IOntology o){
            ontology = o;
        }
        public IClass getClass(Entity e) {
            if("CURRENT SMOKER".equals(e.mentionClass)){
                return ontology.getClass("current_smoker_mention");
            }else if ("NON-SMOKER".equals(e.mentionClass)) {
                return ontology.getClass("non-smoker_mention");
            }else if ("PAST SMOKER".equals(e.mentionClass)) {
                return ontology.getClass("past_smoker_mention");
            }else if ("UNKNOWN".equals(e.mentionClass)) {
                return ontology.getClass("unknown_smoker_mention");
            }
            return null;
        }

        public IInstance getInstance(IClass cls, Entity e) {
            // get instances if already defined
            String name = OntologyUtils.toResourceName(e.id);
            if(ontology.hasResource(name))
                return ontology.getInstance(name);

            // need to find annotation class first
            IInstance inst = cls.createInstance(name);
            if(e.span != null)
                inst.addPropertyValue( ontology.getProperty("hasSpan"),e.span.start()+":"+e.span.end());
            inst.addPropertyValue( ontology.getProperty("hasAnnotationType"),getDefaultInstance(ontology.getClass("MentionAnnotation")));
            inst.addPropertyValue( ontology.getProperty("hasAnnotationText"),e.text);
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
    }

}
