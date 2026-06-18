package sn.epf.pointage.service;

import sn.epf.pointage.dao.*;
import sn.epf.pointage.model.*;
import sn.epf.pointage.model.enums.*;

import java.util.List;

public class RapportService {

    private final ProfesseurDAO professeurDAO = new ProfesseurDAO();
    private final SeanceDAO seanceDAO = new SeanceDAO();
    private final AbstractDAO<RapportMensuel, Long> rapportDAO =
            new AbstractDAO<>(RapportMensuel.class) {};

    public RapportMensuel genererRapportMensuel(Long professeurId, int mois, int annee) {

        Professeur prof = professeurDAO.findById(professeurId)
                .orElseThrow(() -> new IllegalArgumentException("Professeur introuvable"));

        long restantes = seanceDAO.countSeancesPlanifieesRestantes(mois, annee, professeurId);

        if (restantes > 0) {
            throw new IllegalStateException(
                    "Rapport bloqué : " + restantes + " séance(s) encore en statut PLANIFIEE."
            );
        }


        List<SeancePlanifiee> toutesLesSeances =
                seanceDAO.findPlanifieesParMois(professeurId, mois, annee);

        List<SeancePlanifiee> realisees = toutesLesSeances.stream()
                .filter(s -> s.getStatut() == StatutSeance.REALISEE)
                .toList();

        double totalMinutes = realisees.stream()
                .mapToInt(SeancePlanifiee::getDureeMinutes)
                .sum();

        double heuresExactes = totalMinutes / 60.0;

        double heuresArrondies = Math.ceil(heuresExactes * 4) / 4.0;

        double montant = heuresArrondies * prof.getTauxHoraireXOF();

        RapportMensuel rapport = new RapportMensuel();
        rapport.setProfesseur(prof);
        rapport.setMois(mois);
        rapport.setAnnee(annee);
        rapport.setHeuresRealisees(heuresArrondies);
        rapport.setMontantXOF((long) montant);
        rapport.setStatut(StatutRapport.EN_ATTENTE);
        return rapportDAO.save(rapport);
    }

    public void validerRapport(Long rapportId) {
        RapportMensuel rapport = rapportDAO.findById(rapportId)
                .orElseThrow(() -> new IllegalArgumentException("Rapport introuvable"));
        rapport.setStatut(StatutRapport.VALIDE);
        rapportDAO.update(rapport);
        System.out.println("Rapport numero " + rapportId + " validé.");
    }

    public void marquerCommePaye(Long rapportId) {
        RapportMensuel rapport = rapportDAO.findById(rapportId)
                .orElseThrow(() -> new IllegalArgumentException("Rapport introuvable"));

        if (rapport.getStatut() != StatutRapport.VALIDE) {
            throw new IllegalStateException("Le rapport doit être VALIDE avant d'être payé.");
        }

        rapport.setStatut(StatutRapport.PAYE);
        rapportDAO.update(rapport);
    }
}