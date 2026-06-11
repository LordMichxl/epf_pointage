package sn.epf.pointage.config;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;


public class HibernateConfig {

    private static SessionFactory sessionFactory;

    private HibernateConfig() {}


    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null || sessionFactory.isClosed()) {
            try {
                System.out.println("Initialisation de Hibernate...");

                sessionFactory = new Configuration()
                        .configure("hibernate.cfg.xml")
                        .buildSessionFactory();

                System.out.println("Hibernate initialisé avec succès !");

            } catch (Exception e) {
                System.err.println("Erreur d'initialisation Hibernate : " + e.getMessage());
                throw new RuntimeException("Impossible d'initialiser Hibernate", e);
            }
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            System.out.println("Hibernate fermé proprement.");
        }
    }
}