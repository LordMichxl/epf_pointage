package sn.epf.pointage.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;
import sn.epf.pointage.model.SeancePlanifiee;
import sn.epf.pointage.model.enums.StatutSeance;
import org.hibernate.Transaction;

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
                                    "LEFT JOIN FETCH s.assignation a " +
                                    "LEFT JOIN FETCH a.cours " +
                                    "LEFT JOIN FETCH a.professeur " +
                                    "WHERE s.dateHeure >= :d AND s.dateHeure <= :f " +
                                    "ORDER BY s.dateHeure",
                            SeancePlanifiee.class)
                    .setParameter("d", debutDateTime)
                    .setParameter("f", finDateTime)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    public List<SeancePlanifiee> findAllSeances() {
        try (Session session = getSession()) {
            return session.createQuery(
                            "FROM SeancePlanifiee s " +
                                    "LEFT JOIN FETCH s.assignation a " +
                                    "LEFT JOIN FETCH a.cours " +
                                    "LEFT JOIN FETCH a.professeur " +
                                    "ORDER BY s.dateHeure ASC",
                            SeancePlanifiee.class)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    public void updateStatut(Long seanceId, StatutSeance nouveauStatut) {
        Transaction transaction = null;
        try (Session session = getSession()) {
            transaction = session.beginTransaction();
            String hql = "UPDATE SeancePlanifiee s SET s.statut = :statut WHERE s.id = :id";
            Query<?> query = session.createQuery(hql);
            query.setParameter("statut", nouveauStatut);
            query.setParameter("id", seanceId);
            int result = query.executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw e;
        }
    }
    public List<SeancePlanifiee> findSansPointageDebut() {
        try (Session session = getSession()) {
            return session.createQuery(
                            "FROM SeancePlanifiee s " +
                                    "LEFT JOIN FETCH s.assignation a " +
                                    "LEFT JOIN FETCH a.cours " +
                                    "LEFT JOIN FETCH a.professeur " +
                                    "WHERE s.statut = :st AND s.dateHeure < :now " +
                                    "AND NOT EXISTS (SELECT p FROM Pointage p WHERE p.seance = s AND p.typePointage = 'DEBUT')",
                            SeancePlanifiee.class)
                    .setParameter("st", StatutSeance.PLANIFIEE)
                    .setParameter("now", java.time.LocalDateTime.now())
                    .getResultList();
        }
    }


}
