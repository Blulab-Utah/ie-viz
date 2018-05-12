package edu.pitt.dbmi.nlp.noble.coder.processor;


import edu.pitt.dbmi.nlp.noble.coder.model.Document;
import edu.pitt.dbmi.nlp.noble.coder.model.Processor;
import edu.pitt.dbmi.nlp.noble.terminology.TerminologyException;

/**
 * Created by tseytlin on 5/31/17.
 */
public class TableProcessor implements Processor<Document> {
    private long time;

    public Document process(Document doc) throws TerminologyException {
        time = System.currentTimeMillis();

        // lets do sectioning first
        int offset = 0;

        for(String s: doc.getText().split("\n")){
            //TODO: implement


            // increment offset
            offset += s.length()+1;
        }


        time = System.currentTimeMillis() - time;
        doc.getProcessTime().put(getClass().getSimpleName(),time);
        return doc;
    }

    public long getProcessTime() {
        return time;
    }
}
