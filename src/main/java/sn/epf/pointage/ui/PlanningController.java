package sn.epf.pointage.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import sn.epf.pointage.context.SessionContext;
import sn.epf.pointage.dao.*;
import sn.epf.pointage.model.*;
import sn.epf.pointage.model.enums.Frequence;
import sn.epf.pointage.model.enums.Role;
import sn.epf.pointage.model.enums.StatutSeance;
import sn.epf.pointage.service.AccesService;
import sn.epf.pointage.service.EnrolementService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;

public class PlanningController {

    @FXML private ComboBox<Professeur> comboProfesseur;
    @FXML private ComboBox<Cours>      comboCours;
    @FXML private ComboBox<Salle>      comboSalle;
    @FXML private ComboBox<DayOfWeek>  comboJour;
    @FXML private TextField            champHeureDebut;
    @FXML private TextField            champHeureFin;
    @FXML private ComboBox<Frequence>  comboFrequence;
    @FXML private DatePicker           champDebutSemestre;
    @FXML private DatePicker           champFinSemestre;
    @FXML private Label                messageResultat;
    @FXML private GridPane             grillePlanning;
    @FXML private Label                labelSemaine;

    private Utilisateur utilisateur;

    private final ProfesseurDAO                professeurDAO    = new ProfesseurDAO();
    private final AbstractDAO<Cours, Long>     coursDAO         = new AbstractDAO<>(Cours.class) {};
    private final AbstractDAO<Salle, Long>     salleDAO         = new AbstractDAO<>(Salle.class) {};
    private final SeanceDAO                    seanceDAO        = new SeanceDAO();
    private final EnrolementService            enrolementService = new EnrolementService();

    private static final int         HEURE_DEBUT_GRILLE = 8;
    private static final int         HEURE_FIN_GRILLE   = 18;
    private static final DayOfWeek[] JOURS = {
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
    };

    private LocalDate debutSemaineAffichee;

    @FXML
    public void initialize() {
        utilisateur = SessionContext.getInstance().getUtilisateurConnecte();

        comboJour.getItems().setAll(JOURS);
        comboJour.setCellFactory(param -> new ListCell<>() {
            @Override protected void updateItem(DayOfWeek item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                String nom = item.getDisplayName(java.time.format.TextStyle.FULL, Locale.FRENCH);
                setText(nom.substring(0, 1).toUpperCase() + nom.substring(1));
            }
        });
        comboJour.setButtonCell(comboJour.getCellFactory().call(null));

        comboFrequence.getItems().setAll(Frequence.values());

        chargerListesDeroulantes();

        debutSemaineAffichee = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        rafraichirGrille();
    }

    private void configurerCellFactories() {

        comboProfesseur.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Professeur p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : p.getNom() + " " + p.getPrenom());
            }
        });
        comboProfesseur.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Professeur p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? "Professeur" : p.getNom() + " " + p.getPrenom());
            }
        });

        comboCours.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Cours c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getCode() + " - " + c.getIntitule());
            }
        });
        comboCours.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Cours c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? "Cours" : c.getCode() + " - " + c.getIntitule());
            }
        });

        comboSalle.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Salle s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); return; }
                String cap = s.getCapacite() != null ? " (" + s.getCapacite() + " pl.)" : "";
                setText(s.getNom() + cap);
            }
        });
        comboSalle.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Salle s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText("Salle"); return; }
                String cap = s.getCapacite() != null ? " (" + s.getCapacite() + " pl.)" : "";
                setText(s.getNom() + cap);
            }
        });
    }

    private void chargerListesDeroulantes() {

        configurerCellFactories();

        new Thread(() -> {
            List<Professeur> profs  = professeurDAO.findAllActifs();
            List<Cours>      cours  = coursDAO.findAll();
            List<Salle>      salles = salleDAO.findAll();

            Platform.runLater(() -> {
                comboProfesseur.setItems(FXCollections.observableArrayList(profs));
                comboCours.setItems(FXCollections.observableArrayList(cours));
                comboSalle.setItems(FXCollections.observableArrayList(salles));
            });
        }).start();
    }

    @FXML public void semainePrecedente() {
        debutSemaineAffichee = debutSemaineAffichee.minusWeeks(1);
        rafraichirGrille();
    }

    @FXML public void semaineSuivante() {
        debutSemaineAffichee = debutSemaineAffichee.plusWeeks(1);
        rafraichirGrille();
    }

    private void rafraichirGrille() {
        LocalDate finSemaine = debutSemaineAffichee.plusDays(JOURS.length - 1);
        DateTimeFormatter fmtJour = DateTimeFormatter.ofPattern("dd/MM", Locale.FRENCH);
        labelSemaine.setText("Semaine du " + debutSemaineAffichee.format(fmtJour)
                + " au " + finSemaine.format(fmtJour));

        new Thread(() -> {
            LocalDateTime debutDateTime = debutSemaineAffichee.atStartOfDay();
            LocalDateTime finDateTime   = finSemaine.atTime(23, 59, 59);
            List<SeancePlanifiee> seances;

            if (utilisateur != null && utilisateur.getRole() == Role.PROFESSEUR) {
                Long profId = null;
                try {
                    if (utilisateur.getProfesseurLie() != null) {
                        profId = utilisateur.getProfesseurLie().getId();
                    }
                } catch (Exception e) {
                    System.err.println("Lazy loading getProfesseurLie : " + e.getMessage());
                }
                seances = (profId != null)
                        ? seanceDAO.findEntreDatesEtProf(debutDateTime, finDateTime, profId)
                        : List.of();
            } else {
                seances = seanceDAO.findEntreDates(debutDateTime, finDateTime);
            }

            Platform.runLater(() -> construireGrille(seances));
        }).start();
    }

    private void construireGrille(List<SeancePlanifiee> seances) {
        grillePlanning.getChildren().clear();
        grillePlanning.getColumnConstraints().clear();
        grillePlanning.getRowConstraints().clear();

        grillePlanning.getColumnConstraints().add(new ColumnConstraints(70));

        for (int i = 0; i < JOURS.length; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPrefWidth(150);
            col.setHgrow(Priority.ALWAYS);
            grillePlanning.getColumnConstraints().add(col);
        }

        grillePlanning.add(new Label(""), 0, 0);

        DateTimeFormatter fmtEntete = DateTimeFormatter.ofPattern("EEE dd/MM", Locale.FRENCH);
        for (int j = 0; j < JOURS.length; j++) {
            LocalDate dateJour = debutSemaineAffichee.plusDays(j);
            Label entete = new Label(dateJour.format(fmtEntete));
            entete.getStyleClass().add("grille-entete-jour");
            entete.setMaxWidth(Double.MAX_VALUE);
            entete.setAlignment(Pos.CENTER);
            grillePlanning.add(entete, j + 1, 0);
        }

        int nbLignes = HEURE_FIN_GRILLE - HEURE_DEBUT_GRILLE;
        grillePlanning.getRowConstraints().add(new RowConstraints(40));

        for (int h = 0; h < nbLignes; h++) {
            grillePlanning.getRowConstraints().add(new RowConstraints(60));
            Label labelHeure = new Label(String.format("%02d:00", HEURE_DEBUT_GRILLE + h));
            labelHeure.getStyleClass().add("grille-label-heure");
            grillePlanning.add(labelHeure, 0, h + 1);
        }

        for (SeancePlanifiee seance : seances) {
            DayOfWeek jourSeance = seance.getDateHeure().getDayOfWeek();
            int colonne = indexDuJour(jourSeance);
            if (colonne == -1) continue;

            int heureDebut = seance.getDateHeure().getHour();
            int ligne      = heureDebut - HEURE_DEBUT_GRILLE;
            if (ligne < 0 || ligne >= nbLignes) continue;

            int rowSpan = Math.max(1,
                    (int) Math.ceil(seance.getDureeMinutes() / 60.0));

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
        bloc.getStyleClass().add(classeCssSelonStatut(seance.getStatut()));

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
        AccesService.exigerRole(Role.ADMIN, Role.SCOLARITE);
        ContextMenu menu = new ContextMenu();

        MenuItem itemAnnuler    = new MenuItem("Marquer ANNULÉE");
        MenuItem itemReporter   = new MenuItem("Marquer REPORTÉE");
        MenuItem itemReplanifier = new MenuItem("Remettre PLANIFIÉE");

        itemAnnuler.setOnAction(e    -> changerStatut(seance, StatutSeance.ANNULEE));
        itemReporter.setOnAction(e   -> changerStatut(seance, StatutSeance.REPORTEE));
        itemReplanifier.setOnAction(e -> changerStatut(seance, StatutSeance.PLANIFIEE));

        menu.getItems().addAll(itemAnnuler, itemReporter, itemReplanifier);
        menu.show(source, Side.BOTTOM, 0, 0);
    }

    private void changerStatut(SeancePlanifiee seance, StatutSeance nouveauStatut) {
        AccesService.exigerRole(Role.ADMIN, Role.SCOLARITE);
        new Thread(() -> {
            try {
                seance.setStatut(nouveauStatut);
                seanceDAO.updateStatut(seance.getId(), nouveauStatut);
                Platform.runLater(() -> {
                    if (messageResultat != null)
                        messageResultat.setText("Statut mis à jour avec succès !");
                    rafraichirGrille();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    if (messageResultat != null)
                        messageResultat.setText("Erreur lors du changement de statut.");
                });
            }
        }).start();
    }

    @FXML
    public void creerAssignation() {
        AccesService.exigerRole(Role.ADMIN, Role.SCOLARITE);
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

            messageResultat.setText("Assignation créée et séances générées !");
            rafraichirGrille();

        } catch (Exception e) {
            messageResultat.setText("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void ouvrirDialogueNouveauCours() {
        AccesService.exigerRole(Role.ADMIN, Role.SCOLARITE);
        Dialog<Cours> dialog = new Dialog<>();
        dialog.setTitle("Nouveau cours");
        dialog.setHeaderText("Créer un nouveau cours");

        ButtonType btnCreer = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnCreer, ButtonType.CANCEL);

        TextField champCode      = new TextField(); champCode.setPromptText("Ex: INFO301");
        TextField champIntitule  = new TextField(); champIntitule.setPromptText("Ex: Algorithmique Avancée");
        TextField champVolume    = new TextField(); champVolume.setPromptText("Ex: 45");
        TextField champNiveau    = new TextField(); champNiveau.setPromptText("Ex: L3");
        TextField champFiliere   = new TextField(); champFiliere.setPromptText("Ex: CSI");
        TextField champSemestre  = new TextField(); champSemestre.setPromptText("Ex: 1");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.addRow(0, new Label("Code *"),           champCode);
        grid.addRow(1, new Label("Intitulé *"),       champIntitule);
        grid.addRow(2, new Label("Volume horaire"),   champVolume);
        grid.addRow(3, new Label("Niveau"),           champNiveau);
        grid.addRow(4, new Label("Filière"),          champFiliere);
        grid.addRow(5, new Label("Semestre"),         champSemestre);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(bouton -> {
            if (bouton != btnCreer || champCode.getText().isBlank()
                    || champIntitule.getText().isBlank()) return null;
            Cours cours = new Cours();
            cours.setCode(champCode.getText());
            cours.setIntitule(champIntitule.getText());
            cours.setNiveauEtude(champNiveau.getText());
            cours.setFiliere(champFiliere.getText());
            try {
                cours.setVolumeHoraireTotal(Integer.parseInt(champVolume.getText()));
                cours.setSemestre(Integer.parseInt(champSemestre.getText()));
            } catch (NumberFormatException ignored) {}
            return cours;
        });

        dialog.showAndWait().ifPresent(nouveauCours -> {
            Cours saved = coursDAO.save(nouveauCours);
            comboCours.getItems().add(saved);
            comboCours.setValue(saved);
            messageResultat.setText("Cours créé : " + saved.getIntitule());
        });
    }

    @FXML
    public void ouvrirDialogueNouvelleSalle() {
        AccesService.exigerRole(Role.ADMIN, Role.SCOLARITE);
        Dialog<Salle> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle salle");
        dialog.setHeaderText("Créer une nouvelle salle");

        ButtonType btnCreer = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnCreer, ButtonType.CANCEL);

        TextField champNom         = new TextField(); champNom.setPromptText("Ex: Salle A12");
        TextField champCapacite    = new TextField(); champCapacite.setPromptText("Ex: 40");
        TextField champBatiment    = new TextField(); champBatiment.setPromptText("Ex: Bâtiment A");
        TextField champEquipements = new TextField(); champEquipements.setPromptText("Ex: Vidéoprojecteur");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.addRow(0, new Label("Nom *"),         champNom);
        grid.addRow(1, new Label("Capacité"),      champCapacite);
        grid.addRow(2, new Label("Bâtiment"),      champBatiment);
        grid.addRow(3, new Label("Équipements"),   champEquipements);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(bouton -> {
            if (bouton != btnCreer || champNom.getText().isBlank()) return null;
            Salle salle = new Salle();
            salle.setNom(champNom.getText());
            salle.setBatiment(champBatiment.getText());
            salle.setEquipements(champEquipements.getText());
            try {
                salle.setCapacite(Integer.parseInt(champCapacite.getText()));
            } catch (NumberFormatException ignored) {}
            return salle;
        });

        dialog.showAndWait().ifPresent(nouvelleSalle -> {
            Salle saved = salleDAO.save(nouvelleSalle);
            comboSalle.getItems().add(saved);
            comboSalle.setValue(saved);
            messageResultat.setText("Salle créée : " + saved.getNom());
        });
    }
}
