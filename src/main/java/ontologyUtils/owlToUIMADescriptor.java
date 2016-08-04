package ontologyUtils;

import org.jdom.Namespace;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.*;

import java.io.*;
import java.util.Collections;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.semanticweb.owlapi.util.OWLOntologyWalker;
import org.semanticweb.owlapi.util.OWLOntologyWalkerVisitor;


/**
 * Created by melissa on 7/14/16.
 */
public class owlToUIMADescriptor {

    public static void main(String[] args) throws Exception {
        File inputFile;
        File outputFile;
        final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

        if(args.length < 2) {
            System.err.print("You must provide the input file and output file locations.");
        }
        inputFile = new File(args[0]);
        outputFile = new File(args[1]);

        if(!outputFile.exists()){
            outputFile.createNewFile();
        }

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(inputFile);

        Element typeDesc = new Element("typeSystemDescription", ontology.getOntologyID().getOntologyIRI().toString());

        Document doc = new Document(typeDesc);
        doc.setRootElement(typeDesc);

        Element name = new Element("name");
        String typeName = inputFile.getName().substring(0, inputFile.getName().indexOf("."));
        name.addContent(typeName + "TypeSystem");
        doc.getRootElement().addContent(name);

        Element description = new Element("description");
        doc.getRootElement().addContent(description);
        Element version = new Element("version");
        version.addContent("1.0");
        doc.getRootElement().addContent(version);
        Element vendor = new Element("vendor");
        doc.getRootElement().addContent(vendor);
        Element types = new Element("types");
        doc.getRootElement().addContent(types);

        //walk ontology classes to  build types
        OWLOntologyWalker walker = new OWLOntologyWalker(Collections.singleton(ontology));
        OWLOntologyWalkerVisitor<Object> visitor = new OWLOntologyWalkerVisitor<Object>(walker){

        };

        XMLOutputter xmlOutput = new XMLOutputter();

        xmlOutput.setFormat(Format.getPrettyFormat());
        xmlOutput.output(doc, new FileWriter(outputFile));

        System.out.println("File Saved!");
    }
}