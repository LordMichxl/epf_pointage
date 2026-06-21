package sn.epf.pointage.dao;

import org.hibernate.Session;
import sn.epf.pointage.model.Utilisateur;

import java.util.Optional;

public class UtilisateurDAO extends AbstractDAO<Utilisateur, Long> {

    public UtilisateurDAO() {
        super(Utilisateur.class);
    }

    public Optional<Utilisateur> findByLogin(String login) {
        try (Session session = getSession()) {
            return session.createQuery(
                    "FROM Utilisateur u WHERE u.login = :login", Utilisateur.class
            ).setParameter("login", login).uniqueResultOptional();
        }
    }
}