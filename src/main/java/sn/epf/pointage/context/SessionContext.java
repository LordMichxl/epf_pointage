package sn.epf.pointage.context;

import sn.epf.pointage.model.Utilisateur;

public class SessionContext {
    private static SessionContext instance;
    private Utilisateur utilisateurConnecte;
    private long dernierAcces;
    private SessionContext() {}

    public static SessionContext getInstance() {
        if (instance == null) instance = new SessionContext();
        return instance;
    }

    public void connecter(Utilisateur u) {
        this.utilisateurConnecte = u;
        this.dernierAcces = System.currentTimeMillis();
    }

    public void deconnecter() {
        this.utilisateurConnecte = null;
    }

    public Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    public boolean estConnecte() {
        return utilisateurConnecte != null;
    }

    public void rafraichir() {
        this.dernierAcces = System.currentTimeMillis();
    }

    public boolean estExpire() {
        // System.currentTimeMillis() retourne des millisecondes et on divise par 60000 pour les avoir en min
        return (System.currentTimeMillis() - dernierAcces) / 60000 > 30;
    }
}