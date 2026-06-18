package sn.epf.pointage.dao;

import org.hibernate.Session;
import sn.epf.pointage.model.Assignation;

import java.util.List;

public class AssignationDAO extends AbstractDAO<Assignation, Long> {

    public AssignationDAO() {
        super(Assignation.class);
    }
    public List<Assignation> findByProfesseur(Long professeurId) {
        try (Session session = getSession()) {
            return session.createQuery(
                            "FROM Assignation a WHERE a.professeur.id = :profId",
                            Assignation.class
                    ).setParameter("profId", professeurId)
                    .getResultList();
        }
    }

    public List<Assignation> findByAnnee(String anneeAcademique) {
        try (Session session = getSession()) {
            return session.createQuery(
                            "FROM Assignation a WHERE a.anneeAcademique = :annee",
                            Assignation.class
                    ).setParameter("annee", anneeAcademique)
                    .getResultList();
        }
    }
}