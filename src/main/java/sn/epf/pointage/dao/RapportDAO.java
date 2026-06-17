package sn.epf.pointage.dao;

import org.hibernate.Session;
import sn.epf.pointage.model.RapportMensuel;
import sn.epf.pointage.model.enums.StatutRapport;

import java.util.List;
import java.util.Optional;

public class RapportDAO extends AbstractDAO<RapportMensuel, Long> {

    public RapportDAO() {
        super(RapportMensuel.class);
    }

    public Optional<RapportMensuel> findByProfesseurEtPeriode(Long profId, int mois, int annee) {
        try (Session session = getSession()) {
            return Optional.ofNullable(
                session.createQuery(
                    "FROM RapportMensuel r WHERE r.professeur.id = :p AND r.mois = :m AND r.annee = :a",
                    RapportMensuel.class)
                    .setParameter("p", profId).setParameter("m", mois).setParameter("a", annee)
                    .uniqueResult()
            );
        }
    }

    public List<RapportMensuel> findNonPaies() {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM RapportMensuel r WHERE r.statut != :s ORDER BY r.annee DESC, r.mois DESC",
                RapportMensuel.class).setParameter("s", StatutRapport.PAYE).getResultList();
        }
    }

    public List<RapportMensuel> findAllByProfesseur(Long profId) {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM RapportMensuel r WHERE r.professeur.id = :p ORDER BY r.annee DESC, r.mois DESC",
                RapportMensuel.class).setParameter("p", profId).getResultList();
        }
    }
}
