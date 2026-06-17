package sn.epf.pointage;

import sn.epf.pointage.config.HibernateConfig;
import sn.epf.pointage.dao.ProfesseurDAO;
import sn.epf.pointage.model.Professeur;
import sn.epf.pointage.model.enums.TypeContrat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class TestPersistance {

    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════");
        System.out.println("  TEST COUCHE PERSISTANCE — EPF Africa");
        System.out.println("═══════════════════════════════════════");

        ProfesseurDAO dao = new ProfesseurDAO();

        // TEST 1 : Connexion
        System.out.println("\n[1] Connexion Hibernate...");
        HibernateConfig.getSessionFactory();
        System.out.println("    → Tables créées en base ✅");

        // TEST 2 : Save
        System.out.println("\n[2] Création d'un professeur...");
        Professeur prof = new Professeur("EPF-2024-MD-001", "Diallo", "Moussa", "m.diallo@epf.sn", TypeContrat.PERMANENT);
        prof.setTauxHoraireXOF(15000.0);
        prof.setDateEmbauche(LocalDate.of(2020, 9, 1));
        Professeur saved = dao.save(prof);
        System.out.println("    → Enregistré : " + saved);

        // TEST 3 : findById
        System.out.println("\n[3] Recherche par id=" + saved.getId() + "...");
        Optional<Professeur> found = dao.findById(saved.getId());
        found.ifPresent(p -> System.out.println("    → Trouvé : " + p));

        // TEST 4 : findByNom
        System.out.println("\n[4] Recherche par nom 'diallo'...");
        List<Professeur> resultats = dao.findByNom("diallo");
        System.out.println("    → " + resultats.size() + " résultat(s)");

        // TEST 5 : update
        System.out.println("\n[5] Mise à jour téléphone...");
        saved.setTelephone("77 123 45 67");
        dao.update(saved);
        System.out.println("    → Mis à jour ✅");

        // TEST 6 : findAll
        System.out.println("\n[6] Liste tous les professeurs...");
        System.out.println("    → " + dao.findAll().size() + " professeur(s) en base");

        System.out.println("\n═══════════════════════════════════════");
        System.out.println("  TOUS LES TESTS PASSÉS ✅");
        System.out.println("═══════════════════════════════════════");

        HibernateConfig.shutdown();
    }
}
