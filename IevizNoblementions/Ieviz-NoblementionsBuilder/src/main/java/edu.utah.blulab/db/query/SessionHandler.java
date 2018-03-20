package edu.utah.blulab.db.query;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

//import org.hibernate.service.ServiceRegistryBuilder;


public class SessionHandler {
    private static SessionFactory sessionFactory;
    private static Logger logger = Logger.getLogger(SessionHandler.class);

    /**
     * @return
     */
    private static SessionFactory buildSessionFactory() {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            Configuration configuration = new Configuration();
            configuration.configure("compliance.hibernate.cfg.xml");
            logger.debug("Hibernate Configuration loaded");

            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
            logger.debug("Hibernate serviceRegistry created");

            return configuration.buildSessionFactory(serviceRegistry);
        } catch (Throwable ex) {
            logger.error("Initial SessionFactory creation failed." + ex);
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }

    protected static SessionFactory getSessionFactory() {
        if (null == sessionFactory) {
            sessionFactory = buildSessionFactory();
        }
        return sessionFactory;
    }

}
