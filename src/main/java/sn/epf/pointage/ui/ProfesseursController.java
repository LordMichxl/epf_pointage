package sn.epf.pointage.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import sn.epf.pointage.dao.ProfesseurDAO;
import sn.epf.pointage.model.Professeur;

import java.io.IOException;
import java.util.List;

public class ProfesseursController {

    @FXML private TableView<Professeur>          tableProfesseurs;
    @FXML private TableColumn<Professeur,String> colMatricule;
    @FXML private TableColumn<Professeur,String> colNom;
    @FXML private TableColumn<Professeur,String> colPrenom;
    @FXML private TableColumn<Professeur,String> colEmail;
    @FXML private TableColumn<Professeur,String> colTelephone;
    @FXML private TableColumn<Professeur,String> colContrat;
    @FXML private TableColumn<Professeur,String> colFiliere;
    @FXML private TableColumn<Professeur,String> colStatut;
    @FXML private TableColumn<Professeur,Void>   colActions;
    @FXML private TextField                       champRecherche;
    @FXML private ComboBox<String>                filtreContrat;
    @FXML private ComboBox<String>                filtreFiliere;
    @FXML private Label                           labelNbResultats;
    @FXML private Button                          btnAjouter;

    private final ProfesseurDAO professeurDAO = new ProfesseurDAO();
    private ObservableList<Professeur> listeProfesseurs;
    private FilteredList<Professeur>   listeFiltree;

    @FXML
    public void initialize() {
        configurerColonnes();
        configurerFiltres();
        chargerProfesseurs();

        tableProfesseurs.setRowFactory(tv -> {
            TableRow<Professeur> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty())
                    ouvrirFormulairePour(row.getItem());
            });
            return row;
        });
    }

    private void configurerColonnes() {
        // ── Colonnes simples avec lambda → SimpleStringProperty ──
        // C'est la bonne façon : on convertit chaque valeur en String explicitement
        colMatricule.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getMatricule()));

        colNom.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNom()));

        colPrenom.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getPrenom()));

        colEmail.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getEmail()));

        colTelephone.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getTelephone() != null ? data.getValue().getTelephone() : "—"));

        // ── Colonne contrat : enum → String + couleur ─────────────
        colContrat.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getTypeContrat().name()));
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

        // ── Colonne filière : toujours "—" (pas sur Professeur directement) ──
        colFiliere.setCellValueFactory(data -> new SimpleStringProperty("—"));

        // ── Colonne statut : Boolean → String + couleur ───────────
        colStatut.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getActif() ? "Actif" : "Inactif"));
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                if (item.equals("Actif")) {
                    setText(" Actif");
                    setStyle("-fx-text-fill: #1E8449; -fx-font-weight: bold;");
                } else {
                    setText(" Inactif");
                    setStyle("-fx-text-fill: #C0392B; -fx-font-weight: bold;");
                }
            }
        });

        // ── Colonne actions : boutons Modifier / Désactiver ───────
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnModifier   = new Button(" Modifier");
            private final Button btnDesactiver = new Button(" Susprendre");
            private final HBox   boite        = new HBox(6, btnModifier, btnDesactiver);
            {
                btnModifier.setStyle(
                        "-fx-background-color: #241654; -fx-text-fill: white;" +
                                "-fx-background-radius: 4px; -fx-cursor: hand; -fx-padding: 3 8; -fx-font-size: 11px;");
                btnDesactiver.setStyle(
                        "-fx-background-color: #D0103A; -fx-text-fill: white;" +
                                "-fx-background-radius: 4px; -fx-cursor: hand; -fx-padding: 3 8;");
                btnModifier.setOnAction(e ->
                        ouvrirFormulairePour(getTableView().getItems().get(getIndex())));
                btnDesactiver.setOnAction(e ->
                        confirmerDesactivation(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Professeur p = getTableView().getItems().get(getIndex());
                btnDesactiver.setDisable(p != null && !p.getActif());
                setGraphic(boite);
            }
        });
    }

    private void chargerProfesseurs() {
        List<Professeur> tous = professeurDAO.findAll();
        listeProfesseurs = FXCollections.observableArrayList(tous);
        listeFiltree     = new FilteredList<>(listeProfesseurs, p -> true);
        tableProfesseurs.setItems(listeFiltree);
        mettreAJourCompteur();
    }

    private void configurerFiltres() {
        filtreContrat.setItems(FXCollections.observableArrayList("Tous", "PERMANENT", "VACATAIRE"));
        filtreContrat.getSelectionModel().selectFirst();
        filtreFiliere.setItems(FXCollections.observableArrayList("Toutes","CSI","GC","GME","GEI","GMP"));
        filtreFiliere.getSelectionModel().selectFirst();
        champRecherche.textProperty().addListener((obs, old, val) -> appliquerFiltres());
        filtreContrat.valueProperty().addListener((obs, old, val) -> appliquerFiltres());
        filtreFiliere.valueProperty().addListener((obs, old, val) -> appliquerFiltres());
    }

    private void appliquerFiltres() {
        String recherche = champRecherche.getText().toLowerCase().trim();
        String contrat   = filtreContrat.getValue();

        listeFiltree.setPredicate(p -> {
            boolean matchRecherche = recherche.isEmpty()
                    || p.getNom().toLowerCase().contains(recherche)
                    || p.getPrenom().toLowerCase().contains(recherche)
                    || p.getMatricule().toLowerCase().contains(recherche)
                    || (p.getEmail() != null && p.getEmail().toLowerCase().contains(recherche));

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

    @FXML
    private void ouvrirFormulaire() { ouvrirFormulairePour(null); }

    private void ouvrirFormulairePour(Professeur professeur) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/prof_form.fxml"));
            Node vue = loader.load();
            ProfFormController ctrl = loader.getController();
            ctrl.setProfesseur(professeur);
            StackPane contentArea = (StackPane) tableProfesseurs.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(vue);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur ouverture formulaire : " + e.getMessage()).showAndWait();
        }
    }

    private void confirmerDesactivation(Professeur prof) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Désactiver le professeur");
        alert.setHeaderText("Désactiver " + prof.getNom() + " " + prof.getPrenom() + " ?");
        alert.setContentText("Son accès sera révoqué. L'historique sera conservé.");
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                prof.setActif(false);
                professeurDAO.update(prof);
                chargerProfesseurs();
            }
        });
    }
}