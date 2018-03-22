package edu.utah.blulab.db.query;

import edu.utah.blulab.db.models.AnnotationResultsDao;
import edu.utah.blulab.db.models.DocumentIdentifierDao;
import edu.utah.blulab.utilities.IevizUtilities;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.List;

public class QueryUtility {

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
}
