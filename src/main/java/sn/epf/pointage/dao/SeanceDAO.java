package sn.epf.pointage.dao;

import org.hibernate.Session;
import sn.epf.pointage.model.SeancePlanifiee;
import sn.epf.pointage.model.enums.StatutSeance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class SeanceDAO extends AbstractDAO<SeancePlanifiee, Long> {

    public SeanceDAO() {
        super(SeancePlanifiee.class);
    }

    public List<SeancePlanifiee> findSeancesDuJour() {
        try (Session session = getSession()) {
            LocalDateTime debut = LocalDate.now().atStartOfDay();
            LocalDateTime fin   = debut.plusDays(1);
            return session.createQuery(
                "FROM SeancePlanifiee s WHERE s.dateHeure >= :d AND s.dateHeure < :f ORDER BY s.dateHeure",
                SeancePlanifiee.class)
                .setParameter("d", debut).setParameter("f", fin).getResultList();
        }
    }

    public List<SeancePlanifiee> findByProfesseurEtMois(Long profId, int mois, int annee) {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM SeancePlanifiee s WHERE s.assignation.professeur.id = :p AND MONTH(s.dateHeure) = :m AND YEAR(s.dateHeure) = :a",
                SeancePlanifiee.class)
                .setParameter("p", profId).setParameter("m", mois).setParameter("a", annee)
                .getResultList();
        }
    }

    public List<SeancePlanifiee> findPlanifieesParMois(Long profId, int mois, int annee) {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM SeancePlanifiee s WHERE s.assignation.professeur.id = :p AND MONTH(s.dateHeure) = :m AND YEAR(s.dateHeure) = :a AND s.statut = :st",
                SeancePlanifiee.class)
                .setParameter("p", profId).setParameter("m", mois).setParameter("a", annee)
                .setParameter("st", StatutSeance.PLANIFIEE).getResultList();
        }
    }

    public List<SeancePlanifiee> findSansPointageDebut() {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM SeancePlanifiee s WHERE s.statut = :st AND s.dateHeure < :now AND NOT EXISTS (SELECT p FROM Pointage p WHERE p.seance = s AND p.typePointage = 'DEBUT')",
                SeancePlanifiee.class)
                .setParameter("st", StatutSeance.PLANIFIEE)
                .setParameter("now", LocalDateTime.now()).getResultList();
        }
    }
    public long countSeancesPlanifieesRestantes(int profId, int mois, Long annee) {
        try (Session session = getSession()) {
            return session.createQuery(
                            "SELECT COUNT(s) FROM SeancePlanifiee s " +
                                    "WHERE s.assignation.professeur.id = :p " +
                                    "AND MONTH(s.dateHeure) = :m " +
                                    "AND YEAR(s.dateHeure) = :a " +
                                    "AND s.statut = :st",
                            Long.class)
                    .setParameter("p", profId)
                    .setParameter("m", mois)
                    .setParameter("a", annee)
                    .setParameter("st", StatutSeance.PLANIFIEE)
                    .getSingleResult();
        }
    }

    public List<SeancePlanifiee> findEntreDates(LocalDateTime debutDateTime, LocalDateTime finDateTime) {
        try (Session session = getSession()) {
            return session.createQuery(
                    "FROM SeancePlanifiee s " +
                            "WHERE s.assignation.professeur.id = :p " +
                            "AND s.dateHeure >= :d AND s.dateHeure < :f ORDER BY s.dateHeure",
                    SeancePlanifiee.class).setParameter("d", debutDateTime).setParameter("f", finDateTime).getResultList();
        }
    }
}
