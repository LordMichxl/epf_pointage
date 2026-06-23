package sn.epf.pointage.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import sn.epf.pointage.context.SessionContext;
import sn.epf.pointage.dao.PointageDAO;
import sn.epf.pointage.dao.SeanceDAO;
import sn.epf.pointage.model.Pointage;
import sn.epf.pointage.model.Professeur;
import sn.epf.pointage.model.SeancePlanifiee;
import sn.epf.pointage.model.Utilisateur;
import sn.epf.pointage.model.enums.ResultatPointage;
import sn.epf.pointage.model.enums.StatutPointage;
import sn.epf.pointage.model.enums.StatutSeance;
import sn.epf.pointage.model.enums.TypePointage;
import sn.epf.pointage.service.PointageService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
public class PointageController {

    @FXML private Label  labelDate;
    @FXML private Label  labelHeure;
    @FXML private Label  labelMessage;
    @FXML private Label  labelNbSeances;
    @FXML private Label  labelNbRealisees;
    @FXML private Label  labelNbEnAttente;
    @FXML private Label  labelNbRetards;
    @FXML private Label  labelSeanceCours;
    @FXML private Label  labelSeanceHeure;
    @FXML private Label  labelSeanceSalle;
    @FXML private Label  labelFenetreStatut;
    @FXML private Label  labelEcartTemps;
    @FXML private Button btnPointerDebut;
    @FXML private Button btnPointerFin;

    @FXML private TableView<SeancePlanifiee>           tableSeances;
    @FXML private TableColumn<SeancePlanifiee,String>  colHeure;
    @FXML private TableColumn<SeancePlanifiee,String>  colCours;
    @FXML private TableColumn<SeancePlanifiee,String>  colSalle;
    @FXML private TableColumn<SeancePlanifiee,Integer> colDuree;
    @FXML private TableColumn<SeancePlanifiee,String>  colStatut;
    @FXML private TableColumn<SeancePlanifiee,String>  colFenetre;

    private final SeanceDAO     seanceDAO     = new SeanceDAO();
    private final PointageDAO   pointageDAO   = new PointageDAO();
    private final PointageService pointageService = new PointageService();

    private Timeline horlogeTimeline;
    private Timeline actualiserTimeline;

    private static final DateTimeFormatter FMT_HEURE = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter FMT_DATE  = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy",
        java.util.Locale.FRENCH);
    private static final DateTimeFormatter FMT_HM    = DateTimeFormatter.ofPattern("HH:mm");

    // initialize()
    @FXML
    public void initialize() {
        configurerColonnes();
        chargerSeances();
        demarrerHorloge();
        configurerSelectionSeance();
        afficherDateJour();
    }

    private void demarrerHorloge() {
        horlogeTimeline = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> {
                labelHeure.setText(LocalDateTime.now().format(FMT_HEURE));
                SeancePlanifiee sel = tableSeances.getSelectionModel().getSelectedItem();
                if (sel != null) mettreAJourBoutons(sel);
            })
        );
        horlogeTimeline.setCycleCount(Timeline.INDEFINITE);
        horlogeTimeline.play();

        actualiserTimeline = new Timeline(
            new KeyFrame(Duration.seconds(60), e -> chargerSeances()));
        actualiserTimeline.setCycleCount(Timeline.INDEFINITE);
        actualiserTimeline.play();
    }

    private void afficherDateJour() {
        labelDate.setText(LocalDateTime.now().format(FMT_DATE));
        labelHeure.setText(LocalDateTime.now().format(FMT_HEURE));
    }

    private void configurerColonnes() {
        colHeure.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); return; }
                SeancePlanifiee s = getTableView().getItems().get(getIndex());
                setText(s.getDateHeure().format(FMT_HM));
            }
        });

        colCours.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); return; }
                SeancePlanifiee s = getTableView().getItems().get(getIndex());
                try {
                    setText(s.getAssignation().getCours().getIntitule());
                } catch (Exception e) { setText("—"); }
            }
        });

        colSalle.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); return; }
                SeancePlanifiee s = getTableView().getItems().get(getIndex());
                try {
                    setText(s.getAssignation().getSalle() != null
                        ? s.getAssignation().getSalle().getNom() : "—");
                } catch (Exception e) { setText("—"); }
            }
        });

        colDuree.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); return; }
                SeancePlanifiee s = getTableView().getItems().get(getIndex());
                setText(s.getDureeMinutes() != null ? s.getDureeMinutes() + " min" : "—");
            }
        });

        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); setStyle(""); return; }
                SeancePlanifiee s = getTableView().getItems().get(getIndex());
                switch (s.getStatut()) {
                    case PLANIFIEE  -> { setText("⏳ Planifiée");  setStyle("-fx-text-fill: #241654;"); }
                    case REALISEE   -> { setText("✅ Réalisée");   setStyle("-fx-text-fill: #1E8449; -fx-font-weight: bold;"); }
                    case ANNULEE    -> { setText("❌ Annulée");    setStyle("-fx-text-fill: #C0392B;"); }
                    case REPORTEE   -> { setText("🔁 Reportée");  setStyle("-fx-text-fill: #D35400;"); }
                }
            }
        });

        colFenetre.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); setStyle(""); return; }
                SeancePlanifiee s = getTableView().getItems().get(getIndex());
                long ecart = ChronoUnit.MINUTES.between(LocalDateTime.now(), s.getDateHeure());
                if (ecart > 15)       { setText("Trop tôt");    setStyle("-fx-text-fill: #888;"); }
                else if (ecart >= -5) { setText("Ouverte");      setStyle("-fx-text-fill: #1E8449; -fx-font-weight: bold;"); }
                else                  { setText("Fermée");       setStyle("-fx-text-fill: #C0392B;"); }
            }
        });
    }

    // Chargement des séances du jour
    private void chargerSeances() {
        Utilisateur user = SessionContext.getInstance().getUtilisateurConnecte();
        if (user == null) return;

        List<SeancePlanifiee> seances;
        if (user.getRole().name().equals("PROFESSEUR") && user.getProfesseurLie() != null) {
            seances = seanceDAO.findByProfesseurEtMois(
                user.getProfesseurLie().getId(),
                LocalDateTime.now().getMonthValue(),
                LocalDateTime.now().getYear()
            );
            seances = seances.stream()
                .filter(s -> s.getDateHeure().toLocalDate().equals(java.time.LocalDate.now()))
                .toList();
        } else {
            seances = seanceDAO.findSeancesDuJour();
        }

        tableSeances.setItems(FXCollections.observableArrayList(seances));
        labelNbSeances.setText("(" + seances.size() + ")");
        mettreAJourRecap(seances);
    }

    private void mettreAJourRecap(List<SeancePlanifiee> seances) {
        long realisees  = seances.stream().filter(s -> s.getStatut() == StatutSeance.REALISEE).count();
        long enAttente  = seances.stream().filter(s -> s.getStatut() == StatutSeance.PLANIFIEE).count();
        labelNbRealisees.setText(realisees + " séance(s) réalisée(s)");
        labelNbEnAttente.setText(enAttente + " séance(s) en attente");
    }

    private void configurerSelectionSeance() {
        tableSeances.getSelectionModel().selectedItemProperty()
            .addListener((obs, old, seance) -> {
                if (seance != null) afficherDetailSeance(seance);
            });
    }

    private void afficherDetailSeance(SeancePlanifiee seance) {
        try {
            labelSeanceCours.setText(seance.getAssignation().getCours().getIntitule());
            labelSeanceHeure.setText(seance.getDateHeure().format(FMT_HM)
                + " · " + seance.getDureeMinutes() + " min");
            labelSeanceSalle.setText((seance.getAssignation().getSalle() != null
                ? seance.getAssignation().getSalle().getNom() : "Salle non définie"));
        } catch (Exception e) {
            labelSeanceCours.setText("Séance #" + seance.getId());
        }
        mettreAJourBoutons(seance);
    }

    private void mettreAJourBoutons(SeancePlanifiee seance) {
        long ecart = ChronoUnit.MINUTES.between(LocalDateTime.now(), seance.getDateHeure());
        boolean fenetreOuverte = (ecart <= 15 && ecart >= -5);
        boolean dejaRealisee   = seance.getStatut() == StatutSeance.REALISEE;

        boolean debutExiste = pointageDAO.findBySeanceEtType(seance.getId(), TypePointage.DEBUT).isPresent();
        boolean finExiste   = pointageDAO.findBySeanceEtType(seance.getId(), TypePointage.FIN).isPresent();

        btnPointerDebut.setDisable(!fenetreOuverte || debutExiste);
        btnPointerFin.setDisable(!debutExiste || finExiste);

        if (ecart > 15) {
            labelFenetreStatut.setText("Pas encore ouverte");
            labelFenetreStatut.setStyle("-fx-text-fill: #888;");
            labelEcartTemps.setText("Ouvre dans " + ecart + " min");
        } else if (ecart >= -5) {
            labelFenetreStatut.setText("Fenêtre ouverte");
            labelFenetreStatut.setStyle("-fx-text-fill: #1E8449; -fx-font-weight: bold;");
            labelEcartTemps.setText(ecart >= 0 ? "Commence dans " + ecart + " min"
                : "Démarrée il y a " + (-ecart) + " min");
        } else {
            labelFenetreStatut.setText("Fenêtre fermée");
            labelFenetreStatut.setStyle("-fx-text-fill: #C0392B;");
            labelEcartTemps.setText("Dépassée de " + (-ecart - 5) + " min");
        }
    }

    @FXML
    private void pointerDebut() {
        SeancePlanifiee seance = tableSeances.getSelectionModel().getSelectedItem();
        if (seance == null) return;
        Utilisateur user = SessionContext.getInstance().getUtilisateurConnecte();
        if (user == null || user.getProfesseurLie() == null) return;

        ResultatPointage resultat = pointageService.pointer(
            seance.getId(), user.getProfesseurLie().getId(), TypePointage.DEBUT);
        afficherResultatPointage(resultat);
        chargerSeances();
    }

    @FXML
    private void pointerFin() {
        SeancePlanifiee seance = tableSeances.getSelectionModel().getSelectedItem();
        if (seance == null) return;
        Utilisateur user = SessionContext.getInstance().getUtilisateurConnecte();
        if (user == null || user.getProfesseurLie() == null) return;

        ResultatPointage resultat = pointageService.pointer(
            seance.getId(), user.getProfesseurLie().getId(), TypePointage.FIN);
        afficherResultatPointage(resultat);
        chargerSeances();
    }

    private void afficherResultatPointage(ResultatPointage resultat) {
        switch (resultat) {
            case SUCCES      -> afficherMessage("Pointage enregistré avec succès !", "#1E8449");
            case EN_RETARD   -> afficherMessage(" Pointage enregistré — vous êtes en retard. La scolarité a été notifiée.", "#D35400");
            case TROP_TOT    -> afficherMessage("Trop tôt ! La fenêtre de pointage n'est pas encore ouverte (RG-01).", "#C0392B");
            case PROF_INACTIF-> afficherMessage("Votre compte est inactif. Contactez la scolarité.", "#C0392B");
        }
    }

    private void afficherMessage(String msg, String couleur) {
        Platform.runLater(() -> {
            labelMessage.setText(msg);
            labelMessage.setStyle("-fx-text-fill: " + couleur + ";");
        });
    }

    @FXML
    private void actualiser() {
        chargerSeances();
        afficherMessage("Données actualisées", "#241654");
    }
}
