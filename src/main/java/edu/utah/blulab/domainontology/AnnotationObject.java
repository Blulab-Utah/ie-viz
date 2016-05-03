package edu.utah.blulab.domainontology;

import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.util.ArrayList;

public class AnnotationObject {
    private String uri;
    private DomainOntology domain;

    public AnnotationObject(String annotationID, DomainOntology domain){
        this.domain = domain;
        uri = domain.getDomainURI() + "#" + "annotationID";
    }

    public String getUri() {
        return uri;
    }

    public void setAnnotationType(String type) throws OWLOntologyStorageException {
        domain.setDataProperty(domain.getIndividual(uri), OntologyConstants.HAS_ANNOTATION_TYPE, type);
    }

    public void setCorpus(String corpus){}

    public void setDocumentID(String documentID){}

    public void setSpan(ArrayList<String> spans){}
}
