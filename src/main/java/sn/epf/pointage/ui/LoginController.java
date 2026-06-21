package sn.epf.pointage.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import sn.epf.pointage.context.SessionContext;
import sn.epf.pointage.dao.UtilisateurDAO;
import sn.epf.pointage.model.Utilisateur;

import java.io.IOException;

public class LoginController {

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageErreur;
    @FXML private Button btnConnexion;

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    @FXML
    public void seConnecter() {
        String login = loginField.getText().trim();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            afficherErreur("Veuillez remplir tous les champs");
            return;
        }

        Utilisateur utilisateur = utilisateurDAO.findByLogin(login).orElse(null);

        if (utilisateur == null || !BCrypt.checkpw(password, utilisateur.getMotDePasseHash())) {
            afficherErreur("Login ou mot de passe incorrect");
            return;
        }

        SessionContext.getInstance().connecter(utilisateur);

        ouvrirApplicationPrincipale();
    }

    private void afficherErreur(String message) {
        messageErreur.setText(message);
        messageErreur.setVisible(true);
    }

    private void ouvrirApplicationPrincipale() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = (Stage) btnConnexion.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);

        } catch (IOException e) {
            afficherErreur("Erreur lors de l'ouverture de l'application");
            e.printStackTrace();
        }
    }
}