package sn.epf.pointage.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import sn.epf.pointage.dao.*;
import sn.epf.pointage.model.*;
import sn.epf.pointage.model.enums.Frequence;
import sn.epf.pointage.model.enums.StatutSeance;
import sn.epf.pointage.service.EnrolementService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

public class PlanningController {

    // ── Formulaire de création d'assignation ──────────────────────────
    @FXML private ComboBox<Professeur> comboProfesseur;
    @FXML private ComboBox<Cours> comboCours;
    @FXML private ComboBox<Salle> comboSalle;
    @FXML private ComboBox<DayOfWeek> comboJour;
    @FXML private TextField champHeureDebut;
    @FXML private TextField champHeureFin;
    @FXML private ComboBox<Frequence> comboFrequence;
    @FXML private DatePicker champDebutSemestre;
    @FXML private DatePicker champFinSemestre;
    @FXML private Label messageResultat;
    @FXML private GridPane grillePlanning;
    @FXML private Label labelSemaine;

    private final ProfesseurDAO professeurDAO = new ProfesseurDAO();
    private final AbstractDAO<Cours, Long> coursDAO = new AbstractDAO<>(Cours.class) {};
    private final AbstractDAO<Salle, Long> salleDAO = new AbstractDAO<>(Salle.class) {};
    private final SeanceDAO seanceDAO = new SeanceDAO();
    private final EnrolementService enrolementService = new EnrolementService();
    private static final int HEURE_DEBUT_GRILLE = 8;
    private static final int HEURE_FIN_GRILLE   = 18;

    private static final DayOfWeek[] JOURS = {
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
    };
    private LocalDate debutSemaineAffichee;

    @FXML
    public void initialize() {
        comboJour.getItems().setAll(DayOfWeek.values());
        comboFrequence.getItems().setAll(Frequence.values());
        chargerListesDeroulantes();
        debutSemaineAffichee = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        rafraichirGrille();
    }

    @FXML
    public void semainePrecedente() {
        debutSemaineAffichee = debutSemaineAffichee.minusWeeks(1);
        rafraichirGrille();
    }

    @FXML
    public void semaineSuivante() {
        debutSemaineAffichee = debutSemaineAffichee.plusWeeks(1);
        rafraichirGrille();
    }


    private void rafraichirGrille() {
        LocalDate finSemaine = debutSemaineAffichee.plusDays(JOURS.length);

        DateTimeFormatter fmtJour = DateTimeFormatter.ofPattern("dd/MM");
        labelSemaine.setText("Semaine du " + debutSemaineAffichee.format(fmtJour)
                + " au " + debutSemaineAffichee.plusDays(JOURS.length - 1).format(fmtJour));

        new Thread(() -> {
            LocalDateTime debutDateTime = debutSemaineAffichee.atStartOfDay();
            LocalDateTime finDateTime = finSemaine.atStartOfDay();
            List<SeancePlanifiee> seances = seanceDAO.findEntreDates(debutDateTime, finDateTime);

            Platform.runLater(() -> construireGrille(seances));
        }).start();
    }

    private void construireGrille(List<SeancePlanifiee> seances) {
        grillePlanning.getChildren().clear();
        grillePlanning.getColumnConstraints().clear();
        grillePlanning.getRowConstraints().clear();

        ColumnConstraints colHeures = new ColumnConstraints(70);
        grillePlanning.getColumnConstraints().add(colHeures);
        for (int i = 0; i < JOURS.length; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPrefWidth(150);
            col.setHgrow(Priority.ALWAYS);
            grillePlanning.getColumnConstraints().add(col);
        }

        grillePlanning.add(new Label(""), 0, 0);
        DateTimeFormatter fmtEntete = DateTimeFormatter.ofPattern("EEE dd/MM");
        for (int j = 0; j < JOURS.length; j++) {
            LocalDate dateJour = debutSemaineAffichee.plusDays(j);
            Label entete = new Label(dateJour.format(fmtEntete));
            entete.getStyleClass().add("grille-entete-jour");
            entete.setMaxWidth(Double.MAX_VALUE);
            entete.setAlignment(Pos.CENTER);
            grillePlanning.add(entete, j + 1, 0);
        }

        int nbLignes = HEURE_FIN_GRILLE - HEURE_DEBUT_GRILLE;
        for (int h = 0; h < nbLignes; h++) {
            RowConstraints row = new RowConstraints(60);
            grillePlanning.getRowConstraints().add(row);

            Label labelHeure = new Label(String.format("%02d:00", HEURE_DEBUT_GRILLE + h));
            labelHeure.getStyleClass().add("grille-label-heure");
            grillePlanning.add(labelHeure, 0, h + 1);
        }

        for (SeancePlanifiee seance : seances) {

            DayOfWeek jourSeance = seance.getDateHeure().getDayOfWeek();
            int colonne = indexDuJour(jourSeance);
            if (colonne == -1) continue;

            int heureDebut = seance.getDateHeure().getHour();
            int ligne = heureDebut - HEURE_DEBUT_GRILLE;
            if (ligne < 0 || ligne >= nbLignes) continue;
            int rowSpan = Math.max(1, (int) Math.ceil(seance.getDureeMinutes() / 60.0));

            Pane bloc = creerBlocSeance(seance);
            grillePlanning.add(bloc, colonne + 1, ligne + 1, 1, rowSpan);
        }
    }

    private int indexDuJour(DayOfWeek jour) {
        for (int i = 0; i < JOURS.length; i++) {
            if (JOURS[i] == jour) return i;
        }
        return -1;
    }

    private Pane creerBlocSeance(SeancePlanifiee seance) {
        VBox bloc = new VBox(2);
        bloc.getStyleClass().addAll("bloc-seance", classeCssSelonStatut(seance.getStatut()));

        Label titre = new Label(seance.getAssignation().getCours().getIntitule());
        titre.getStyleClass().add("bloc-seance-titre");
        titre.setWrapText(true);

        Label prof = new Label(seance.getAssignation().getProfesseur().getNom());
        prof.getStyleClass().add("bloc-seance-prof");

        bloc.getChildren().addAll(titre, prof);

        Tooltip tooltip = new Tooltip(
                seance.getAssignation().getCours().getIntitule() + "\n"
                        + seance.getAssignation().getProfesseur().getPrenom() + " "
                        + seance.getAssignation().getProfesseur().getNom() + "\n"
                        + "Statut : " + seance.getStatut()
        );
        Tooltip.install(bloc, tooltip);
        bloc.setOnMouseClicked(e -> afficherMenuStatut(seance, bloc));

        return bloc;
    }

    private String classeCssSelonStatut(StatutSeance statut) {
        return switch (statut) {
            case PLANIFIEE -> "bloc-planifiee";
            case REALISEE  -> "bloc-realisee";
            case ANNULEE   -> "bloc-annulee";
            case REPORTEE  -> "bloc-reportee";
        };
    }

    private void afficherMenuStatut(SeancePlanifiee seance, Pane source) {
        ContextMenu menu = new ContextMenu();

        MenuItem itemAnnuler = new MenuItem("Marquer ANNULÉE");
        itemAnnuler.setOnAction(e -> changerStatut(seance, StatutSeance.ANNULEE));

        MenuItem itemReporter = new MenuItem("Marquer REPORTÉE");
        itemReporter.setOnAction(e -> changerStatut(seance, StatutSeance.REPORTEE));

        MenuItem itemReplanifier = new MenuItem("Remettre PLANIFIÉE");
        itemReplanifier.setOnAction(e -> changerStatut(seance, StatutSeance.PLANIFIEE));

        menu.getItems().addAll(itemAnnuler, itemReporter, itemReplanifier);
        menu.show(source, Side.BOTTOM, 0, 0);
    }

    private void changerStatut(SeancePlanifiee seance, StatutSeance nouveauStatut) {
        seance.setStatut(nouveauStatut);
        seanceDAO.update(seance);
        rafraichirGrille();
    }

    // ── FORMULAIRE DE CRÉATION D'ASSIGNATION (inchangé) ─────────────────

    private void chargerListesDeroulantes() {
        new Thread(() -> {
            List<Professeur> profs = professeurDAO.findActifs();
            List<Cours> cours = coursDAO.findAll();
            List<Salle> salles = salleDAO.findAll();
            Platform.runLater(() -> {
                comboProfesseur.setItems(FXCollections.observableArrayList(profs));
                comboCours.setItems(FXCollections.observableArrayList(cours));
                comboSalle.setItems(FXCollections.observableArrayList(salles));
            });
        }).start();
    }

    @FXML
    public void creerAssignation() {
        try {
            PeriodiciteCours periodicite = new PeriodiciteCours();
            periodicite.setJourSemaine(comboJour.getValue());
            periodicite.setHeureDebut(LocalTime.parse(champHeureDebut.getText()));
            periodicite.setHeureFin(LocalTime.parse(champHeureFin.getText()));
            periodicite.setFrequence(comboFrequence.getValue());

            String anneeAcad = champDebutSemestre.getValue().getYear()
                    + "-" + champFinSemestre.getValue().getYear();

            enrolementService.assignerCours(
                    comboProfesseur.getValue(),
                    comboCours.getValue(),
                    comboSalle.getValue(),
                    anneeAcad,
                    periodicite,
                    champDebutSemestre.getValue(),
                    champFinSemestre.getValue()
            );

            messageResultat.setText("Assignation créée et séances générées");
            rafraichirGrille();

        } catch (Exception e) {
            messageResultat.setText(e.getMessage());
        }
    }
}