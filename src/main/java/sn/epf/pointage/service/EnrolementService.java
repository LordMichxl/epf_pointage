package sn.epf.pointage.service;

import javafx.scene.control.Alert;
import org.mindrot.jbcrypt.BCrypt;
import sn.epf.pointage.dao.*;
import sn.epf.pointage.model.*;
import sn.epf.pointage.model.enums.Frequence;
import sn.epf.pointage.model.enums.Role;
import sn.epf.pointage.model.enums.StatutSeance;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public class EnrolementService {

    private final ProfesseurDAO professeurDAO = new ProfesseurDAO();
    private final AssignationDAO assignationDAO = new AssignationDAO();
    private final PeriodiciteCoursDAO periodiciteCoursDAO = new PeriodiciteCoursDAO();
    private final SeanceDAO seanceDAO = new SeanceDAO();
    private final AbstractDAO<Utilisateur, Long> utilisateurDAO =
            new AbstractDAO<>(Utilisateur.class) {};

    public Professeur enrollerProfesseur(Professeur prof, String motDePasse) {
        AccesService.exigerRole(Role.ADMIN, Role.SCOLARITE);

        if (prof.getNom() == null || prof.getNom().isBlank())
            throw new IllegalArgumentException("Le nom est obligatoire");
        if (prof.getEmail() == null || !prof.getEmail().contains("@"))
            throw new IllegalArgumentException("Email invalide");
        if (professeurDAO.findByEmail(prof.getEmail()).isPresent())
            throw new IllegalArgumentException("Cet email est déjà utilisé");

        String annee = String.valueOf(LocalDate.now().getYear());
        String initiales = ("" + prof.getPrenom().charAt(0) + prof.getNom().charAt(0)).toUpperCase();
        long seq = professeurDAO.count() + 1;
        prof.setMatricule(String.format("EPF-%s-%s-%03d", annee, initiales, seq));

        Professeur saved = professeurDAO.save(prof);

        Utilisateur u = new Utilisateur();
        u.setLogin(prof.getEmail());
        u.setMotDePasseHash(BCrypt.hashpw(motDePasse, BCrypt.gensalt()));
        u.setRole(Role.PROFESSEUR);
        u.setProfesseurLie(saved);
        utilisateurDAO.save(u);

        return saved;
    }

    public void desactiverProfesseur(Long id) {
        AccesService.exigerRole(Role.ADMIN);
        try {
            Professeur prof = professeurDAO.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Professeur introuvable"));
            prof.setActif(false);
            professeurDAO.update(prof);
        }catch (SecurityException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }

    }

    public Professeur mettreAJourProfil(Professeur profModifie) {
        AccesService.exigerRole(Role.ADMIN, Role.SCOLARITE);
        professeurDAO.findByEmail(profModifie.getEmail()).ifPresent(existing -> {
            if (!existing.getId().equals(profModifie.getId())) {
                throw new IllegalArgumentException("Email déjà utilisé par un autre professeur");
            }
        });

        return professeurDAO.update(profModifie);
    }

    public Assignation assignerCours(
            Professeur professeur,
            Cours cours,
            Salle salle,
            String anneeAcad,
            PeriodiciteCours periodicite,
            LocalDate debutSemestre,
            LocalDate finSemestre) {
        AccesService.exigerRole(Role.ADMIN, Role.SCOLARITE);
        Assignation assignation = new Assignation();
        assignation.setProfesseur(professeur);
        assignation.setCours(cours);
        assignation.setSalle(salle);
        assignation.setAnneeAcademique(anneeAcad);
        int dureeParSeanceMin = (int) Duration.between(
                periodicite.getHeureDebut(),
                periodicite.getHeureFin()
        ).toMinutes();

        Assignation assignationSaved = assignationDAO.save(assignation);

        periodicite.setAssignation(assignationSaved);
        periodiciteCoursDAO.save(periodicite);

        List<SeancePlanifiee> seances = genererSeances(
                assignationSaved,
                periodicite,
                debutSemestre,
                finSemestre,
                dureeParSeanceMin
        );

        seances.forEach(seanceDAO::save);

        int totalMinutesPrevues = seances.size() * dureeParSeanceMin;
        assignationSaved.setHeuresPrevues(totalMinutesPrevues / 60);
        assignationDAO.update(assignationSaved);
        System.out.println( seances.size() + " séances générées pour "
                + professeur.getNom() + ", professeur de " + cours.getIntitule());

        return assignationSaved;
    }

    private List<SeancePlanifiee> genererSeances(
            Assignation assignation,
            PeriodiciteCours periodicite,
            LocalDate debutSemestre,
            LocalDate finSemestre,
            int dureeMinutes) {

        List<SeancePlanifiee> seances = new ArrayList<>();
        LocalDate premiereDate = debutSemestre.with(
                TemporalAdjusters.nextOrSame(periodicite.getJourSemaine())
        );

        LocalDate dateCourante = premiereDate;

        while (!dateCourante.isAfter(finSemestre)) {

            LocalDateTime dateHeure = LocalDateTime.of(
                    dateCourante,
                    periodicite.getHeureDebut()
            );

            SeancePlanifiee seance = new SeancePlanifiee(assignation, dateHeure, dureeMinutes);
            seances.add(seance);

            dateCourante = switch (periodicite.getFrequence()) {
                case HEBDO     -> dateCourante.plusWeeks(1);
                case BIMENSUEL -> dateCourante.plusWeeks(2);
            };
        }

        return seances;
    }
}