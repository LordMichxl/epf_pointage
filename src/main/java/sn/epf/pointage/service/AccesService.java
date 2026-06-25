package sn.epf.pointage.service;

import sn.epf.pointage.context.SessionContext;
import sn.epf.pointage.model.Utilisateur;
import sn.epf.pointage.model.enums.Role;

public class AccesService {

    private AccesService() {}

    public static void exigerRole(Role... rolesAutorises) {
        Utilisateur u = SessionContext.getInstance().getUtilisateurConnecte();

        if (u == null) {
            throw new SecurityException("Aucun utilisateur connecté.");
        }

        for (Role role : rolesAutorises) {
            if (u.getRole() == role) return; // rôle trouvé → autorisé
        }

        throw new SecurityException(
                "Accès refusé : le rôle " + u.getRole() + " ne peut pas effectuer cette action."
        );
    }
}