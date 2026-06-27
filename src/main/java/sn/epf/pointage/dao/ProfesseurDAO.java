package sn.epf.pointage.dao;

import org.hibernate.Session;
import sn.epf.pointage.model.Professeur;

import java.util.List;
import java.util.Optional;

public class ProfesseurDAO extends AbstractDAO<Professeur, Long> {

    public ProfesseurDAO() {
        super(Professeur.class);
    }

    public Optional<Professeur> findByMatricule(String matricule) {
        try (Session session = getSession()) {
            return Optional.ofNullable(
                session.createQuery("FROM Professeur p WHERE p.matricule = :m", Professeur.class)
                       .setParameter("m", matricule).uniqueResult()
            );
        }
    }

    public List<Professeur> findByNom(String nom) {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM Professeur p WHERE LOWER(p.nom) LIKE LOWER(:n) OR LOWER(p.prenom) LIKE LOWER(:n)",
                Professeur.class).setParameter("n", "%" + nom + "%").getResultList();
        }
    }

    public List<Professeur> findAllActifs() {
        try (Session session = getSession()) {
            return session.createQuery(
                            "FROM Professeur p WHERE p.actif = true ORDER BY p.nom", Professeur.class)
                    .getResultList();
        }
    }
    

    public Optional<Professeur> findByEmail(String email) {
        try (Session session = getSession()) {
            return Optional.ofNullable(
                session.createQuery("FROM Professeur p WHERE p.email = :e", Professeur.class)
                       .setParameter("e", email).uniqueResult()
            );
        }
    }

    public List<Professeur> findByFiliere(String filiere) {
        try (Session session = getSession()) {
            return session.createQuery(
                "SELECT DISTINCT a.professeur FROM Assignation a WHERE a.cours.filiere = :f AND a.professeur.actif = true",
                Professeur.class).setParameter("f", filiere).getResultList();
        }
    }

    public long count() {
        try (Session session = getSession()) {
            return session.createQuery(
                    "SELECT COUNT(p) FROM Professeur p ",
                    long.class).getSingleResult();
        }
    }

}
