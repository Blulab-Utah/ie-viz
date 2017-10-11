package edu.utah.blulab.nlp;

import java.util.List;

/**
 * Created by Bill on 10/11/2017.
 */
public interface NLPToolInterface {

    public void initialize(String ontology);

    public List<AnnotationNLP> annotate(String doc);
}
