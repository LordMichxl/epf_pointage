package sn.epf.pointage.dao;

import org.hibernate.Session;
import sn.epf.pointage.model.Pointage;
import sn.epf.pointage.model.enums.TypePointage;

import java.util.List;
import java.util.Optional;

public class PointageDAO extends AbstractDAO<Pointage, Long> {

    public PointageDAO() {
        super(Pointage.class);
    }

    public Optional<Pointage> findBySeanceEtType(Long seanceId, TypePointage type) {
        try (Session session = getSession()) {
            return Optional.ofNullable(
                session.createQuery(
                    "FROM Pointage p WHERE p.seance.id = :s AND p.typePointage = :t", Pointage.class)
                    .setParameter("s", seanceId).setParameter("t", type).uniqueResult()
            );
        }
    }

    public List<Pointage> findByProfesseurEtMois(Long profId, int mois, int annee) {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM Pointage p WHERE p.professeur.id = :p AND MONTH(p.heurePointage) = :m AND YEAR(p.heurePointage) = :a AND p.typePointage = 'DEBUT'",
                Pointage.class)
                .setParameter("p", profId).setParameter("m", mois).setParameter("a", annee)
                .getResultList();
        }
    }

    public Long countRetards(Long profId, int mois, int annee) {
        try (Session session = getSession()) {
            return session.createQuery(
                "SELECT COUNT(p) FROM Pointage p WHERE p.professeur.id = :p AND MONTH(p.heurePointage) = :m AND YEAR(p.heurePointage) = :a AND p.statut = 'EN_RETARD'",
                Long.class)
                .setParameter("p", profId).setParameter("m", mois).setParameter("a", annee)
                .uniqueResult();
        }
    }
}
