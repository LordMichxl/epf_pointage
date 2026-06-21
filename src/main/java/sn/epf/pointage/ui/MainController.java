package sn.epf.pointage.ui;

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

import java.io.IOException;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Label labelUtilisateur;
    @FXML private Button btnProfesseurs;
    @FXML private Button btnPlanning;

    @FXML
    public void initialize() {
        Utilisateur u = SessionContext.getInstance().getUtilisateurConnecte();
        if (u != null) {
            labelUtilisateur.setText(u.getLogin() + " (" + u.getRole() + ")");
            configurerPourRole(u.getRole());
        }
        chargerVue("dashboard.fxml");
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

    private void configurerPourRole(Role role) {
        boolean estProf = (role == Role.PROFESSEUR);
        btnProfesseurs.setVisible(!estProf);
        btnProfesseurs.setManaged(!estProf);
        btnPlanning.setVisible(!estProf);
        btnPlanning.setManaged(!estProf);
    }
}