package sn.epf.pointage.service;

import sn.epf.pointage.dao.AbstractDAO;
import sn.epf.pointage.dao.ProfesseurDAO;
import sn.epf.pointage.model.*;
import sn.epf.pointage.model.enums.Frequence;
import sn.epf.pointage.model.enums.TypeContrat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public class TestServices {
    public static void main(String[] args) {

        // Récupérer des objets existants en base (créés avant dans TestDAO)
        EnrolementService service = new EnrolementService();

        // Créer le professeur
        Professeur prof = new Professeur();
        prof.setNom("Diallo");
        prof.setPrenom("Amadou");
        prof.setEmail("a.diallo@epf.sn");
        prof.setTypeContrat(TypeContrat.PERMANENT);
        prof.setTauxHoraireXOF(5000.0);

        // Enrôler
        Professeur saved = service.enrollerProfesseur(prof, "epf2024");

        System.out.println("Prof créé !");
        System.out.println(" ID  : " + saved.getId());
        System.out.println(" Matricule : " + saved.getMatricule());
        System.out.println(" Login : " + saved.getEmail());
        ProfesseurDAO profDAO = new ProfesseurDAO();

        AbstractDAO<Cours, Long> coursDAO = new AbstractDAO<>(Cours.class) {};
        AbstractDAO<Salle, Long> salleDAO = new AbstractDAO<>(Salle.class) {};


        // Créer un cours de test
        Cours cours = new Cours();
        cours.setCode("INFO301");
        cours.setIntitule("Algorithmique Avancée");
        cours.setVolumeHoraireTotal(45);
        cours.setNiveauEtude("L3");
        cours.setFiliere("CSI");
        cours.setSemestre(1);
        coursDAO.save(cours);

        // Créer une salle de test
        Salle salle = new Salle();
        salle.setNom("Salle A12");
        salle.setCapacite(40);
        salle.setBatiment("Bâtiment A");
        salleDAO.save(salle);

        // Définir la périodicité : chaque lundi de 08h à 10h
        PeriodiciteCours periodicite = new PeriodiciteCours();
        periodicite.setJourSemaine(DayOfWeek.MONDAY);
        periodicite.setHeureDebut(LocalTime.of(8, 0));   // 08:00
        periodicite.setHeureFin(LocalTime.of(10, 0));    // 10:00
        periodicite.setFrequence(Frequence.HEBDO);

        // Dates du semestre 1
        LocalDate debut = LocalDate.of(2025, 9, 1);
        LocalDate fin   = LocalDate.of(2025, 12, 19);

        // Appeler le service
        Assignation result = service.assignerCours(
                prof, cours, salle, "2025-2026", periodicite, debut, fin
        );

        System.out.println("Assignation créée, ID = " + result.getId());
        System.out.println("   Vérifie la table seances_planifiees dans MySQL !");
    }
}
