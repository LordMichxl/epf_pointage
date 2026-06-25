package sn.epf.pointage.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import sn.epf.pointage.context.SessionContext;
import sn.epf.pointage.model.Utilisateur;
import sn.epf.pointage.model.enums.Role;
import sn.epf.pointage.dao.AbstractDAO;
import sn.epf.pointage.model.JournalConnexion;
import sn.epf.pointage.model.enums.TypeAction;
import sn.epf.pointage.util.ReseauUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import java.io.IOException;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Label labelUtilisateur;
    @FXML private Button btnProfesseurs;
    @FXML private Button btnDashboard;
    @FXML private Button btnPlanning;
    @FXML private Button btnPointage;

    private final AbstractDAO<JournalConnexion, Long> journalDAO =
            new AbstractDAO<>(JournalConnexion.class) {};

    @FXML
    public void initialize() {
        Utilisateur u = SessionContext.getInstance().getUtilisateurConnecte();
        if (u != null) {
            labelUtilisateur.setText(u.getLogin() + " (" + u.getRole() + ")");
            configurerPourRole(u);
        }
        if (u != null && u.getRole() == Role.PROFESSEUR) {
            chargerVue("pointage.fxml");
        } else {
            chargerVue("dashboard.fxml");
        }
        demarrerSurveillanceSession();
    }

    public void chargerVue(String fxmlPath) {
        try {
            Node vue = FXMLLoader.load(getClass().getResource("/fxml/" + fxmlPath));
            contentArea.getChildren().setAll(vue);
        } catch (IOException e) {
            System.err.println("Erreur chargement vue : " + fxmlPath);
            e.printStackTrace();
        }
    }

    @FXML public void ouvrirDashboard()   { chargerVue("dashboard.fxml"); }
    @FXML public void ouvrirProfesseurs() { chargerVue("professeurs.fxml"); }
    @FXML public void ouvrirPlanning()    { chargerVue("planning.fxml"); }
    @FXML public void ouvrirPointage()    { chargerVue("pointage.fxml"); }
    @FXML public void ouvrirRapports()    { chargerVue("rapports.fxml"); }

    @FXML
    public void seDeconnecter() {
        journaliserDeconnexion();
        SessionContext.getInstance().deconnecter();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(loader.load(), 400, 500);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void journaliserDeconnexion() {
        Utilisateur u = SessionContext.getInstance().getUtilisateurConnecte();
        if (u == null) return; // déjà déconnecté → on évite un doublon
        new Thread(() -> {
            String ip = ReseauUtils.getIpLocalAddress();
            journalDAO.save(new JournalConnexion(u, ip, TypeAction.DECONNEXION));
        }).start();
    }

    private void demarrerSurveillanceSession() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.minutes(1), e -> {
                    if (SessionContext.getInstance().estExpire()) {
                        deconnecterPourInactivite();
                    }
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        Platform.runLater(() -> {
            if (contentArea.getScene() != null) {
                contentArea.getScene().addEventFilter(MouseEvent.ANY,
                        e -> SessionContext.getInstance().rafraichir());
                contentArea.getScene().addEventFilter(KeyEvent.ANY,
                        e -> SessionContext.getInstance().rafraichir());
            }
        });
    }

    private void deconnecterPourInactivite() {
        Alert alerte = new Alert(Alert.AlertType.INFORMATION,
                "Votre session a expiré après 30 minutes d'inactivité.");
        alerte.showAndWait();
        seDeconnecter();
    }

    private void configurerPourRole(Utilisateur u) {
        Role role = u.getRole();
        boolean estProf = (role == Role.PROFESSEUR);

        boolean peutVoirDashboard = (role == Role.ADMIN || role == Role.SCOLARITE);
        btnDashboard.setVisible(peutVoirDashboard);
        btnDashboard.setManaged(peutVoirDashboard);

        boolean peutVoirProfesseurs = (role == Role.ADMIN || role == Role.SCOLARITE);
        btnProfesseurs.setVisible(peutVoirProfesseurs);
        btnProfesseurs.setManaged(peutVoirProfesseurs);

        boolean aUnProfesseurLie = (u.getProfesseurLie() != null);
        btnPointage.setVisible(aUnProfesseurLie);
        btnPointage.setManaged(aUnProfesseurLie);

    }
}