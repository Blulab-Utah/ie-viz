package edu.utah.blulab.containers;

import java.util.List;

public class DocumentContainer {

    private List<AnnotationContainer> annotations;
    String docName;

    public List<AnnotationContainer> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<AnnotationContainer> annotations) {
        this.annotations = annotations;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }
}
