package edu.utah.blulab.db.query;

import edu.utah.blulab.containers.AnnotationContainer;
import edu.utah.blulab.containers.DocumentContainer;
import edu.utah.blulab.containers.FeatureContainer;
import edu.utah.blulab.db.models.AnnotationResultsDao;
import edu.utah.blulab.db.models.DocumentIdentifierDao;
import edu.utah.blulab.db.models.NlpResultDocDao;
import edu.utah.blulab.db.models.NlpRunDefDao;
import edu.utah.blulab.db.models.NlpResultSnippetDao;
import edu.utah.blulab.db.models.NlpResultFeaturesDao;
import edu.utah.blulab.utilities.IevizUtilities;
import org.apache.log4j.Logger;
import org.hibernate.*;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.List;

public class QueryUtility {
    private static final Logger logger = Logger.getLogger(QueryUtility.class);

    public static void insertDocumentId(DocumentIdentifierDao doc) {
        SessionFactory sessionFactory = SessionHandler.getSessionFactory();
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();

        session.persist(doc);
        transaction.commit();
        session.close();

    }

    public static int getID(String documentName) {
        SessionFactory sessionFactory = SessionHandler.getSessionFactory();
        Session session = sessionFactory.openSession();

        Criteria docCriteria = session.createCriteria(DocumentIdentifierDao.class);
        docCriteria.add(Restrictions.eq("docName", documentName));

        List<?> rawResults = docCriteria.list();
        session.close();

        List<DocumentIdentifierDao> result = new ArrayList<>(rawResults.size());

        for (Object object : rawResults) {
            result.add((DocumentIdentifierDao) object);
        }

        if (IevizUtilities.isNullOrEmpty(result))
            return -1;
        else
            return result.get(0).getId();
    }

    public static void insertAnnotataions(AnnotationResultsDao entity) {
        SessionFactory sessionFactory = SessionHandler.getSessionFactory();
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();

        session.persist(entity);
        transaction.commit();
        session.close();
    }

    public List<DocumentContainer> processAnnotatedOutput(String content){
        String separator = "\t";

        String[] lines = content.split("\n");

        String idPrevLine = "-1";
        int minLocation = 999999;
        int maxLocation = -1;

        int typeColumn = 0;
        int idColumn = 0;
        int docColumn = 0;
        int variableColumn = 0;
        int propertyColumn = 0;
        int docValColumn = 0;
        int valPropColumn = 0;
        int annotationColumn = 0;

        List<DocumentContainer> docList = new ArrayList<DocumentContainer>();
        List<AnnotationContainer> annotationList = new ArrayList<AnnotationContainer>();

        // use header to identify columns
        String[] values = lines[0].replaceAll("\r", "").replaceAll("\n", "").split(separator,-1);
        for (int j = 1; j < values.length; j++) {
            switch (values[j]) {
                case "Type":
                    typeColumn = j;
                    break;
                case "Id":
                    idColumn = j;
                    break;
                case "Document":
                    docColumn = j;
                    break;
                case "Annotation_Variable":
                    variableColumn = j;
                    break;
                case "Property":
                    propertyColumn = j;
                    break;
                case "Document_Value":
                    docValColumn = j;
                    break;
                case "Value_Properties":
                    valPropColumn = j;
                    break;
                case "Annotations":
                    annotationColumn = j;
                    break;
            }
        }

        String docVal = "";
        String variableVal = "";
        String tempDocName = "XXXX";
        String prevDocName = tempDocName;
        List<FeatureContainer> fcArray = new ArrayList<FeatureContainer>();
        boolean firstLine = true;
        for (int i = 1; i < lines.length; i++) {
            values = lines[i].replaceAll("\r", "").replaceAll("\n", "").split(separator,-1);

            if (values[typeColumn].equals("Rejected")){
                continue;
            }

            String annotationVal;
            if (!values[idColumn].equals(idPrevLine)) { // if a new id is found
                // write out all of the data from the previous id
                if (!firstLine) { // don't do this for the very first line of the document
                    AnnotationContainer annotation = new AnnotationContainer();
                    annotation.setDoc(docVal);
                    annotation.setEndLoc(maxLocation);
                    annotation.setStartLoc(minLocation);
                    annotation.setFeatures(fcArray);
                    annotation.setMentionFeatures("");
                    annotation.setVariable(variableVal);
                    annotationList.add(annotation);
                } else {
                    firstLine = false;
                }

                // reset and increment variables
                minLocation = 999999;
                maxLocation = -1;
                fcArray = new ArrayList<>();

                // set the variable level values
                docVal = values[docColumn];
                variableVal = values[variableColumn];
            }

            // if a new document is found
            if (!values[docColumn].equals(prevDocName)) {
                // write out all of the data from the previous id
                DocumentContainer doc = new DocumentContainer();
                doc.setDocName(prevDocName);
                doc.setAnnotations(annotationList);
                docList.add(doc);


                // reset variables
                annotationList = new ArrayList<AnnotationContainer>();
                // todo: figure out how to set get the correct number of annotations for each document
                prevDocName = values[docColumn];


            }

            idPrevLine =  values[idColumn]; // set the previous line ID to the current line for use next iteration
            // fill with first with subsequent lines of data for current id

            // add the current feature to the feature array for this variable
            FeatureContainer fc = new FeatureContainer();
            fc.setProperty(values[propertyColumn]);
            fc.setPropertyValue(values[docValColumn]);
            fc.setPropertyValueNumeric(values[valPropColumn]);
            fcArray.add(fc);

            // calculate the start and stop locations - find the first start and last stop locations for all of a given variables features
            annotationVal = values[annotationColumn];
            String[] annoPairs = annotationVal.split(",");
            int startLoc = 99999;
            for (String pair : annoPairs) {
                String[] annoVals = pair.split("/");
                if (annoVals.length != 2) { // must be a pair - word and integer value
                    continue;
                }
                try {
                    startLoc = Integer.valueOf(annoVals[1].replaceAll(" ", ""));
                } catch (Exception ex) {
                    logger.error("Could not convert " + annoVals[1] + "  to an integer... skipping");
                    continue;
                }
                int endLoc = startLoc + annoVals[0].length() - 1; // end location is the start location + length of the annotation
                if (startLoc < minLocation) {
                    minLocation = startLoc;
                }
                if (endLoc > maxLocation) {
                    maxLocation = endLoc;
                }
            }


            // if last line, write the data to the container objects
            if (i == lines.length - 1){
                AnnotationContainer annotation = new AnnotationContainer();
                annotation.setDoc(docVal);
                annotation.setEndLoc(maxLocation);
                annotation.setStartLoc(minLocation);
                annotation.setFeatures(fcArray);
                annotation.setMentionFeatures("");
                annotation.setVariable(variableVal);
                annotationList.add(annotation);

                DocumentContainer doc = new DocumentContainer();
                doc.setDocName(values[docColumn]);
                doc.setAnnotations(annotationList);
                docList.add(doc);

                // remove the empty document that gets added at the beginning
                if (docList.get(0).getDocName().equals(tempDocName)){
                    docList.remove(0);
                }
            }
        }
        return docList;
    }

    public int persistRun(String runName){
        SessionFactory sessionFactory = SessionHandler.getSessionFactory();
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        Integer runID = null;


        try {
            tx = session.beginTransaction();
            NlpRunDefDao runTable = new NlpRunDefDao();
            runTable.setRunName(runName);
            runID = (Integer) session.save(runTable);
            tx.commit();

        } catch (HibernateException e) {
            if (tx!=null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        if (null != runID)
            return runID;
        else
            return -1;
    }

    public Integer persistAnnotation(DocumentContainer doc, int runID){
        SessionFactory sessionFactory = SessionHandler.getSessionFactory();
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        Integer snippetID = null;
        Integer docID = null;

        try {
            // add document name to doc table
            tx = session.beginTransaction();
            NlpResultDocDao docTable = new NlpResultDocDao();
            docTable.setDocSrc(doc.getDocName());
            docTable.setRunId(runID);
            docID = (Integer) session.save(docTable);
            tx.commit();


            for (AnnotationContainer ac : doc.getAnnotations()) {

                // todo: add mention_feature - a string containing all of the features (redundant with feature table
                // todo: add snippet text to snippet_1 column

                NlpResultSnippetDao snipTable = new NlpResultSnippetDao();
                snipTable.setTermSnippet1StartLoc(ac.getStartLoc());
                snipTable.setTermSnippet1EndLoc(ac.getEndLoc());
                snipTable.setMentionType(ac.getVariable());
                StringBuilder sb = new StringBuilder();
                for (FeatureContainer fc : ac.getFeatures()) {
                    sb.append(fc.getProperty()).append(":").append(fc.getPropertyValue()).append(":").append(fc.getPropertyValueNumeric()).append("\n");
                }
                snipTable.setMentionFeatures(sb.toString());
                snipTable.setResultDocId(docID);
                tx = session.beginTransaction();
                snippetID = (Integer) session.save(snipTable);
                tx.commit();

                // write the feature data
                for (FeatureContainer fc : ac.getFeatures()) {
                    tx = session.beginTransaction();
                    NlpResultFeaturesDao featureTable = new NlpResultFeaturesDao();
                    featureTable.setFeatureName(fc.getProperty());
                    featureTable.setFeatureValue(fc.getPropertyValue());
                    featureTable.setFeatureValueNumeric(fc.getPropertyValueNumeric());
                    featureTable.setResultDocId(docID);
                    featureTable.setSnippetId(snippetID);
                    tx.commit();
                }
            }

            // add features to feature table
            // todo: figure out a way to link snippet results with feature results. Jianlin seems to link through the results_doc table. I wonder why.


        } catch (HibernateException e) {
            if (tx!=null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        return snippetID;
    }
}
