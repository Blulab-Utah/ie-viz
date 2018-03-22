package edu.utah.blulab.editors;

import java.beans.PropertyEditorSupport;
import java.io.FileNotFoundException;
import java.util.List;

import edu.utah.blulab.model.Ontology;
import edu.utah.blulab.utilities.IevizUtilities;

public class OntologyRetriever extends PropertyEditorSupport {
    @Override
    public void setAsText(String id) {
        Ontology d;
        List<String> filenames = null;
        int value = Integer.parseInt(id);
        String file = "C:\\Users\\Deep\\Documents\\noble\\test\\ontologyFileList.txt";
        try {
            filenames = IevizUtilities.getOntologyNames(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (null != filenames)
            d = new Ontology(value, filenames.get(value-1));
        else
            d = null;
//		switch(Integer.parseInt(id))
//		{
//			case 1:
//				d = new Ontology(1,  "Human Resource"); break;
//			case 2:
//				d = new Ontology(2,  "Finance"); break;
//			case 3:
//				d = new Ontology(3,  "Information Technology"); break;
//			default:
//				d = null;
//		}
        this.setValue(d);
    }
}
