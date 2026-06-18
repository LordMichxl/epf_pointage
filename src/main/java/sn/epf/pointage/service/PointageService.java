package sn.epf.pointage.service;

import sn.epf.pointage.dao.*;
import sn.epf.pointage.model.*;
import sn.epf.pointage.model.enums.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class PointageService {

    private final ProfesseurDAO professeurDAO = new ProfesseurDAO();
    private final SeanceDAO seanceDAO = new SeanceDAO();
    private final PointageDAO pointageDAO = new PointageDAO();


    public ResultatPointage pointer(Long seanceId, Long professeurId, TypePointage type) {

        SeancePlanifiee seance = seanceDAO.findById(seanceId)
                .orElseThrow(() -> new IllegalArgumentException("Séance introuvable"));

        Professeur prof = professeurDAO.findById(professeurId)
                .orElseThrow(() -> new IllegalArgumentException("Professeur introuvable"));
        if (!prof.getActif()) {
            return ResultatPointage.PROF_INACTIF;
        }


        long ecart = ChronoUnit.MINUTES.between(
                seance.getDateHeure(),   // A = heure prévue de la séance
                LocalDateTime.now()      // B = maintenant
        );

        if (ecart < -15) {
            return ResultatPointage.TROP_TOT;
        }

        Pointage pointage = new Pointage();
        pointage.setSeance(seance);
        pointage.setProfesseur(prof);
        pointage.setHeurePointage(LocalDateTime.now());
        pointage.setTypePointage(type);
        ResultatPointage resultat;

        if (ecart > 5) {
            pointage.setStatut(StatutPointage.EN_RETARD);
            resultat = ResultatPointage.EN_RETARD;

            System.out.println("ALERTE SCOLARITÉ : "
                    + prof.getPrenom() + " " + prof.getNom()
                    + " est en retard de " + ecart + " minutes"
                    + " pour la séance " + seanceId);

        } else {
            resultat = ResultatPointage.SUCCES;
        }
        pointageDAO.save(pointage);

        if (type == TypePointage.DEBUT) {
            seance.setStatut(StatutSeance.REALISEE);
            seanceDAO.update(seance);
        }
        return resultat;
    }
}