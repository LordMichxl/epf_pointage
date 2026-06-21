package sn.epf.pointage.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import sn.epf.pointage.dao.ProfesseurDAO;
import sn.epf.pointage.model.Professeur;
import sn.epf.pointage.model.enums.TypeContrat;

import java.io.IOException;
import java.util.List;

/*
 * Règle 4.2 : un contrôleur JavaFX n'appelle QUE les services/DAO
 * — jamais de requête Hibernate directement ici.
 * Toute modification UI depuis un thread secondaire passe par Platform.runLater().
 */
public class ProfesseursController {

    // Composants déclarés dans le FXML (injection @FXML)
    @FXML private TableView<Professeur>         tableProfesseurs;
    @FXML private TableColumn<Professeur,String> colMatricule;
    @FXML private TableColumn<Professeur,String> colNom;
    @FXML private TableColumn<Professeur,String> colPrenom;
    @FXML private TableColumn<Professeur,String> colEmail;
    @FXML private TableColumn<Professeur,String> colTelephone;
    @FXML private TableColumn<Professeur,String> colContrat;
    @FXML private TableColumn<Professeur,String> colFiliere;
    @FXML private TableColumn<Professeur,Boolean> colStatut;
    @FXML private TableColumn<Professeur,Void>   colActions;
    @FXML private TextField                       champRecherche;
    @FXML private ComboBox<String>                filtreContrat;
    @FXML private ComboBox<String>                filtreFiliere;
    @FXML private Label                           labelNbResultats;
    @FXML private Button                          btnAjouter;

    private final ProfesseurDAO professeurDAO = new ProfesseurDAO();

    // Liste observable : la TableView se met à jour automatiquement
    private ObservableList<Professeur> listeProfesseurs;
    private FilteredList<Professeur>   listeFiltree;

    //  initialize() : appelé automatiquement par FXMLLoader
    @FXML
    public void initialize() {
        configurerColonnes();
        configurerFiltres();
        chargerProfesseurs();

        // Double-clic sur une ligne → ouvre le formulaire de modification
        tableProfesseurs.setRowFactory(tv -> {
            TableRow<Professeur> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    ouvrirFormulairePour(row.getItem());
                }
            });
            return row;
        });
    }

    // Configuration des colonnes
    private void configurerColonnes() {
        colMatricule.setCellValueFactory(new PropertyValueFactory<>("matricule"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));

        // Contrat avec style coloré
        colContrat.setCellValueFactory(new PropertyValueFactory<>("typeContrat"));
        colContrat.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle(item.equals("PERMANENT")
                    ? "-fx-text-fill: #1E8449; -fx-font-weight: bold;"
                    : "-fx-text-fill: #D35400; -fx-font-weight: bold;");
            }
        });

        // Filière via l'assignation (info non directe sur Professeur → affichage vide si non chargé)
        colFiliere.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : "—");
            }
        });

        // Statut actif/inactif avec couleur
        colStatut.setCellValueFactory(new PropertyValueFactory<>("actif"));
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean actif, boolean empty) {
                super.updateItem(actif, empty);
                if (empty || actif == null) { setText(null); setStyle(""); return; }
                if (actif) {
                    setText(" Actif");
                    setStyle("-fx-text-fill: #1E8449; -fx-font-weight: bold;");
                } else {
                    setText(" Inactif");
                    setStyle("-fx-text-fill: #C0392B; -fx-font-weight: bold;");
                }
            }
        });

        // Colonne actions : boutons Modifier / Désactiver
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnModifier   = new Button("✏");
            private final Button btnDesactiver = new Button("🚫");
            private final HBox boite          = new HBox(6, btnModifier, btnDesactiver);

            {
                btnModifier.setStyle(
                    "-fx-background-color: #241654; -fx-text-fill: white;" +
                    "-fx-background-radius: 4px; -fx-cursor: hand; -fx-padding: 3 8;");
                btnDesactiver.setStyle(
                    "-fx-background-color: #D0103A; -fx-text-fill: white;" +
                    "-fx-background-radius: 4px; -fx-cursor: hand; -fx-padding: 3 8;");
                btnDesactiver.setTooltip(new Tooltip("Désactiver ce professeur"));

                btnModifier.setOnAction(e -> {
                    Professeur p = getTableView().getItems().get(getIndex());
                    ouvrirFormulairePour(p);
                });
                btnDesactiver.setOnAction(e -> {
                    Professeur p = getTableView().getItems().get(getIndex());
                    confirmerDesactivation(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Professeur p = getTableView().getItems().get(getIndex());
                // Si déjà inactif, désactiver le bouton désactivation
                btnDesactiver.setDisable(p != null && !p.getActif());
                setGraphic(boite);
            }
        });
    }

    //  Chargement des données depuis la base
    private void chargerProfesseurs() {
        List<Professeur> tous = professeurDAO.findAll();
        listeProfesseurs = FXCollections.observableArrayList(tous);
        listeFiltree     = new FilteredList<>(listeProfesseurs, p -> true);
        tableProfesseurs.setItems(listeFiltree);
        mettreAJourCompteur();
    }

    // Configuration de la recherche et des filtres
    private void configurerFiltres() {
        filtreContrat.setItems(FXCollections.observableArrayList(
            "Tous", "PERMANENT", "VACATAIRE"));
        filtreContrat.getSelectionModel().selectFirst();

        filtreFiliere.setItems(FXCollections.observableArrayList(
            "Toutes", "CSI", "GC", "GME", "GEI", "GMP"));
        filtreFiliere.getSelectionModel().selectFirst();

        // Écouteur sur la barre de recherche — filtre en temps réel
        champRecherche.textProperty().addListener((obs, old, val) -> appliquerFiltres());
        filtreContrat.valueProperty().addListener((obs, old, val) -> appliquerFiltres());
        filtreFiliere.valueProperty().addListener((obs, old, val) -> appliquerFiltres());
    }

    private void appliquerFiltres() {
        String recherche = champRecherche.getText().toLowerCase().trim();
        String contrat   = filtreContrat.getValue();
        String filiere   = filtreFiliere.getValue();

        listeFiltree.setPredicate(p -> {
            // Filtre recherche texte
            boolean matchRecherche = recherche.isEmpty()
                || p.getNom().toLowerCase().contains(recherche)
                || p.getPrenom().toLowerCase().contains(recherche)
                || p.getMatricule().toLowerCase().contains(recherche)
                || (p.getEmail() != null && p.getEmail().toLowerCase().contains(recherche));

            // Filtre contrat
            boolean matchContrat = contrat == null || contrat.equals("Tous")
                || p.getTypeContrat().name().equals(contrat);

            return matchRecherche && matchContrat;
        });

        mettreAJourCompteur();
    }

    @FXML
    private void reinitialiserFiltres() {
        champRecherche.clear();
        filtreContrat.getSelectionModel().selectFirst();
        filtreFiliere.getSelectionModel().selectFirst();
    }

    private void mettreAJourCompteur() {
        int nb = listeFiltree != null ? listeFiltree.size() : 0;
        labelNbResultats.setText(nb + " professeur(s)");
    }

    // Navigation vers le formulaire
    @FXML
    private void ouvrirFormulaire() {
        ouvrirFormulairePour(null); // null = nouveau professeur
    }

    private void ouvrirFormulairePour(Professeur professeur) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/prof_form.fxml"));
            Node vue = loader.load();

            ProfFormController ctrl = loader.getController();
            ctrl.setProfesseur(professeur); // null = création, sinon modification

            // Charger dans le contentArea du MainController
            StackPane contentArea = (StackPane) tableProfesseurs
                .getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(vue);
            }
        } catch (IOException e) {
            afficherErreur("Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    // Désactivation avec confirmation
    private void confirmerDesactivation(Professeur prof) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Désactiver le professeur");
        alert.setHeaderText("Désactiver " + prof.getNom() + " " + prof.getPrenom() + " ?");
        alert.setContentText(
            "Cette action révoquera son accès et annulera ses séances futures.\n" +
            "L'historique sera conservé.");
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                prof.setActif(false);
                professeurDAO.update(prof);
                chargerProfesseurs(); // Rafraîchir la liste
            }
        });
    }

    private void afficherErreur(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
