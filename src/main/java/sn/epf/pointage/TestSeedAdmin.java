package sn.epf.pointage;

import org.mindrot.jbcrypt.BCrypt;
import sn.epf.pointage.dao.AbstractDAO;
import sn.epf.pointage.model.Utilisateur;
import sn.epf.pointage.model.enums.Role;

//Script pour le premier compte ADMIN
public class TestSeedAdmin {

    public static void main(String[] args) {

        AbstractDAO<Utilisateur, Long> utilisateurDAO =
                new AbstractDAO<>(Utilisateur.class) {};

        Utilisateur admin = new Utilisateur();
        admin.setLogin("admin@epf.sn");
        admin.setMotDePasseHash(BCrypt.hashpw("admin2024", BCrypt.gensalt()));
        admin.setRole(Role.ADMIN);
        utilisateurDAO.save(admin);

        Utilisateur scolarite = new Utilisateur();
        scolarite.setLogin("scolarite@epf.sn");
        scolarite.setMotDePasseHash(BCrypt.hashpw("scol2024", BCrypt.gensalt()));
        scolarite.setRole(Role.SCOLARITE);
        utilisateurDAO.save(scolarite);

        System.out.println("Comptes créés :");
        System.out.println("ADMIN : admin@epf.sn / admin2024");
        System.out.println("SCOLARITE : scolarite@epf.sn / scol2024");
    }
}