package sn.epf.pointage.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.mindrot.jbcrypt.BCrypt;
import sn.epf.pointage.dao.ProfesseurDAO;
import sn.epf.pointage.model.Professeur;
import sn.epf.pointage.model.Utilisateur;
import sn.epf.pointage.model.enums.Role;
import sn.epf.pointage.model.enums.TypeContrat;
import sn.epf.pointage.dao.ProfesseurDAO;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Year;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class ProfFormController {

    @FXML private Label        labelTitre;
    @FXML private ImageView    imagePhoto;
    @FXML private TextField    champNom;
    @FXML private TextField    champPrenom;
    @FXML private TextField    champEmail;
    @FXML private TextField    champTelephone;
    @FXML private DatePicker   dateEmbauche;
    @FXML private ComboBox<TypeContrat> comboContrat;
    @FXML private TextField    champTaux;
    @FXML private TextField    champLogin;
    @FXML private PasswordField champMotDePasse;
    @FXML private CheckBox     checkActif;
    @FXML private VBox         panneauCompte;
    @FXML private Button       btnEnregistrer;
    @FXML private Label        messageGlobal;

    // Labels d'erreur inline
    @FXML private Label erreurNom;
    @FXML private Label erreurPrenom;
    @FXML private Label erreurEmail;
    @FXML private Label erreurTelephone;
    @FXML private Label erreurContrat;
    @FXML private Label erreurTaux;
    @FXML private Label erreurLogin;
    @FXML private Label erreurMotDePasse;
    @FXML private Label erreurPhoto;

    private final ProfesseurDAO dao = new ProfesseurDAO();

    // null = création, non null = modification
    private Professeur professeurAModifier;
    private String     cheminPhoto;
    private boolean    modeCreation = true;

    // initialize()
    @FXML
    public void initialize() {
        comboContrat.getItems().setAll(TypeContrat.values());

        champNom.textProperty().addListener((o, old, val)
            -> validerChamp(champNom, erreurNom, val.trim().isEmpty(), "Le nom est obligatoire"));
        champPrenom.textProperty().addListener((o, old, val)
            -> validerChamp(champPrenom, erreurPrenom, val.trim().isEmpty(), "Le prénom est obligatoire"));
        champEmail.textProperty().addListener((o, old, val)
            -> validerChamp(champEmail, erreurEmail,
                !val.matches("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"),
                "Email invalide (ex: prenom.nom@epf.sn)"));
        champTaux.textProperty().addListener((o, old, val) -> {
            try { Double.parseDouble(val); retirerErreur(champTaux, erreurTaux); }
            catch (NumberFormatException e) { marquerErreur(champTaux, erreurTaux, "Montant invalide"); }
        });
        champLogin.textProperty().addListener((o, old, val)
            -> validerChamp(champLogin, erreurLogin, val.trim().length() < 3,
                "Login trop court (min. 3 caractères)"));
        champMotDePasse.textProperty().addListener((o, old, val)
            -> validerChamp(champMotDePasse, erreurMotDePasse, val.length() < 6,
                "Mot de passe trop court (min. 6 caractères)"));
    }

    public void setProfesseur(Professeur prof) {
        if (prof == null) {
            // MODE CRÉATION
            modeCreation = true;
            labelTitre.setText("Nouveau professeur");
            panneauCompte.setVisible(true);
            panneauCompte.setManaged(true);
        } else {
            modeCreation = false;
            professeurAModifier = prof;
            labelTitre.setText("Modifier — " + prof.getNom() + " " + prof.getPrenom());
            champNom.setText(prof.getNom());
            champPrenom.setText(prof.getPrenom());
            champEmail.setText(prof.getEmail());
            champTelephone.setText(prof.getTelephone());
            comboContrat.setValue(prof.getTypeContrat());
            checkActif.setSelected(prof.getActif());
            if (prof.getTauxHoraireXOF() != null)
                champTaux.setText(String.valueOf(prof.getTauxHoraireXOF().intValue()));
            if (prof.getDateEmbauche() != null)
                dateEmbauche.setValue(prof.getDateEmbauche());
            if (prof.getPhoto() != null && !prof.getPhoto().isEmpty()) {
                cheminPhoto = prof.getPhoto();
                try {
                    imagePhoto.setImage(new Image("file:" + cheminPhoto));
                } catch (Exception ignored) {}
            }

            panneauCompte.setVisible(false);
            panneauCompte.setManaged(false);
        }
    }

    // Choisir une photo
    @FXML
    private void choisirPhoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une photo de profil");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File fichier = chooser.showOpenDialog(imagePhoto.getScene().getWindow());
        if (fichier != null) {
            cheminPhoto = fichier.getAbsolutePath();
            imagePhoto.setImage(new Image(fichier.toURI().toString()));
            erreurPhoto.setText("");
        }
    }

    @FXML
    private void enregistrer() {
        if (!toutEstValide()) {
            messageGlobal.setText(" Corrige les erreurs avant d'enregistrer.");
            messageGlobal.setStyle("-fx-text-fill: #D0103A;");
            return;
        }

        try {
            if (modeCreation) {
                creerProfesseur();
            } else {
                modifierProfesseur();
            }
            messageGlobal.setText(" Enregistré avec succès !");
            messageGlobal.setStyle("-fx-text-fill: #1E8449;");
            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(e -> retourListe());
            pause.play();

        } catch (Exception e) {
            messageGlobal.setText(" Erreur : " + e.getMessage());
            messageGlobal.setStyle("-fx-text-fill: #D0103A;");
        }
    }

    private void creerProfesseur() {
        int annee = Year.now().getValue();
        String initiales = (champNom.getText().substring(0, 1)
                          + champPrenom.getText().substring(0, 1)).toUpperCase();
        long count = dao.findAll().size() + 1;
        String matricule = String.format("EPF-%d-%s-%03d", annee, initiales, count);

        Professeur p = new Professeur(
            matricule,
            champNom.getText().trim(),
            champPrenom.getText().trim(),
            champEmail.getText().trim(),
            comboContrat.getValue()
        );
        p.setTelephone(champTelephone.getText().trim());
        p.setTauxHoraireXOF(Double.parseDouble(champTaux.getText().trim()));
        p.setDateEmbauche(dateEmbauche.getValue());
        p.setPhoto(cheminPhoto);
        p.setActif(true);

        Professeur saved = dao.save(p);
        String hash = BCrypt.hashpw(champMotDePasse.getText(), BCrypt.gensalt());
        Utilisateur u = new Utilisateur(champLogin.getText().trim(), hash, Role.PROFESSEUR);
        u.setProfesseurLie(saved);
    }

    private void modifierProfesseur() {
        professeurAModifier.setNom(champNom.getText().trim());
        professeurAModifier.setPrenom(champPrenom.getText().trim());
        professeurAModifier.setEmail(champEmail.getText().trim());
        professeurAModifier.setTelephone(champTelephone.getText().trim());
        professeurAModifier.setTypeContrat(comboContrat.getValue());
        professeurAModifier.setTauxHoraireXOF(Double.parseDouble(champTaux.getText().trim()));
        professeurAModifier.setDateEmbauche(dateEmbauche.getValue());
        professeurAModifier.setActif(checkActif.isSelected());
        if (cheminPhoto != null) professeurAModifier.setPhoto(cheminPhoto);
        dao.update(professeurAModifier);
    }

    // Validation globale
    private boolean toutEstValide() {
        boolean ok = true;
        if (champNom.getText().trim().isEmpty())    { marquerErreur(champNom, erreurNom, "Obligatoire"); ok = false; }
        if (champPrenom.getText().trim().isEmpty())  { marquerErreur(champPrenom, erreurPrenom, "Obligatoire"); ok = false; }
        if (!champEmail.getText().matches("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) { marquerErreur(champEmail, erreurEmail, "Email invalide"); ok = false; }
        if (comboContrat.getValue() == null)         { marquerErreur(comboContrat, erreurContrat, "Choisir un type"); ok = false; }
        if (champTaux.getText().trim().isEmpty())    { marquerErreur(champTaux, erreurTaux, "Obligatoire"); ok = false; }
        if (modeCreation) {
            if (champLogin.getText().trim().length() < 3)    { marquerErreur(champLogin, erreurLogin, "Trop court"); ok = false; }
            if (champMotDePasse.getText().length() < 6)      { marquerErreur(champMotDePasse, erreurMotDePasse, "Min. 6 caractères"); ok = false; }
        }
        return ok;
    }

    private void validerChamp(Control champ, Label erreur, boolean invalide, String msg) {
        if (invalide) marquerErreur(champ, erreur, msg);
        else          retirerErreur(champ, erreur);
    }

    private void marquerErreur(Control champ, Label erreur, String msg) {
        champ.setStyle(champ.getStyle() + "; -fx-border-color: #D0103A; -fx-border-width: 1.5px;");
        erreur.setText("" + msg);
    }

    private void retirerErreur(Control champ, Label erreur) {
        champ.setStyle("");
        erreur.setText("");
    }

    @FXML
    public void retourListe() {
        try {
            Node vue = FXMLLoader.load(getClass().getResource("/fxml/professeurs.fxml"));
            StackPane contentArea = (StackPane) champNom.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(vue);
        } catch (IOException e) {
            messageGlobal.setText("Erreur retour liste : " + e.getMessage());
        }
    }
}
