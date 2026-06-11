package sn.epf.pointage;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import sn.epf.pointage.config.HibernateConfig;

public class TestConnexion {

    public static void main(String[] args) {
        System.out.println("=== TEST DE CONNEXION HIBERNATE ===");

        try {
            SessionFactory sf = HibernateConfig.getSessionFactory();

            Session session = sf.openSession();

            String version = (String) session
                    .createNativeQuery("SELECT VERSION()", String.class)
                    .getSingleResult();

            System.out.println("Connexion réussie !");
            System.out.println("Version MySQL : " + version);

            session.close();

            HibernateConfig.shutdown();

        } catch (Exception e) {
            System.err.println("ÉCHEC de la connexion !");
            System.err.println("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}