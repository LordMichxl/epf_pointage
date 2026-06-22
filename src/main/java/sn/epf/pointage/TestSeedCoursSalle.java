package sn.epf.pointage;

import sn.epf.pointage.dao.AbstractDAO;
import sn.epf.pointage.model.Cours;
import sn.epf.pointage.model.Salle;

public class TestSeedCoursSalle {

    public static void main(String[] args) {

        AbstractDAO<Cours, Long> coursDAO = new AbstractDAO<>(Cours.class) {};
        AbstractDAO<Salle, Long> salleDAO = new AbstractDAO<>(Salle.class) {};

        // ── Cours (= "classes" L3-CSI, L2-CSI, etc.) ─────────────────
        creerCours(coursDAO, "INFO301", "Algorithmique Avancée", 45, "L3", "CSI", 1);
        creerCours(coursDAO, "INFO302", "Bases de Données", 40, "L3", "CSI", 1);
        creerCours(coursDAO, "INFO303", "Génie Logiciel", 35, "L3", "CSI", 1);
        creerCours(coursDAO, "INFO304", "Réseaux Informatiques", 30, "L3", "CSI", 2);
        creerCours(coursDAO, "INFO201", "Programmation Java", 40, "L2", "CSI", 1);
        creerCours(coursDAO, "INFO202", "Structures de Données", 35, "L2", "CSI", 1);
        creerCours(coursDAO, "GC301", "Résistance des Matériaux", 45, "L3", "GC", 1);
        creerCours(coursDAO, "GM301", "Thermodynamique", 40, "L3", "GM", 1);

        // ── Salles ─────────────────────────────────────────────────
        creerSalle(salleDAO, "Salle A12", 40, "Bâtiment A", "Vidéoprojecteur, Tableau");
        creerSalle(salleDAO, "Salle A13", 35, "Bâtiment A", "Vidéoprojecteur");
        creerSalle(salleDAO, "Salle B01", 50, "Bâtiment B", "Vidéoprojecteur, Climatisation");
        creerSalle(salleDAO, "Labo Info 1", 25, "Bâtiment C", "20 postes, Vidéoprojecteur");
        creerSalle(salleDAO, "Labo Info 2", 25, "Bâtiment C", "20 postes, Vidéoprojecteur");
        creerSalle(salleDAO, "Amphithéâtre", 120, "Bâtiment A", "Sonorisation, Vidéoprojecteur");

        System.out.println("Seeding terminé : cours et salles créés");
    }

    private static void creerCours(AbstractDAO<Cours, Long> dao, String code, String intitule,
                                   int volumeHoraire, String niveau, String filiere, int semestre) {
        Cours cours = new Cours();
        cours.setCode(code);
        cours.setIntitule(intitule);
        cours.setVolumeHoraireTotal(volumeHoraire);
        cours.setNiveauEtude(niveau);
        cours.setFiliere(filiere);
        cours.setSemestre(semestre);
        dao.save(cours);
        System.out.println("  + Cours : " + code + " - " + intitule);
    }

    private static void creerSalle(AbstractDAO<Salle, Long> dao, String nom, int capacite,
                                   String batiment, String equipements) {
        Salle salle = new Salle();
        salle.setNom(nom);
        salle.setCapacite(capacite);
        salle.setBatiment(batiment);
        salle.setEquipements(equipements);
        dao.save(salle);
        System.out.println("  + Salle : " + nom);
    }
}